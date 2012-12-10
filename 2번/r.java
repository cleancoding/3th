    /**
     * 특정일자를 지정해서 배치를 수행할 수 있다.
     * 배치수행일자는 매주 월요일로, 지난주 월요일(-7d)에서 이번주 일요일(-1d)까지의 데이터를 한주 단위로 처리를 수행한다.
     * @param jobYmd
     */ 
    public void makeWeeklyMallWarnCards(String jobYmd) {
        // 실행조건 : 잡 실행일자 == 매주 월요일
        if (isRunnableCondition(jobYmd) == false) {
            throw new IllegalArgumentException("jobYmd is not satisfied runnable condition!");
        }
        
        // 해당 일자로 생성된 클린등급정보를 삭제함 (배치 재실행 시 중복 생성 방지용)
        cleanMallWarnCards(jobYmd);
        
        /*
         * (오늘 일자 전일부터) 최근 4주간 오류가 보고된 몰의 SEQ 목록을 조회한다
         * (조건 : 최종 상태 코드가 '처리'인 오류)
         * 약 350건 (2011.04 기준)
         */
        List<String> errMallSeqs = prodErrRepository.select4WeekErrReportedMallSeqs(jobYmd); 

        /* 
         * 오류신고된 쇼핑몰에 대해 순회적으로 클린경고등급을 연산하여 몰경고카드를 발급한다.
         * 순회 건별로 트랜잭션이 적용되며, 건별 처리도중 예외가 발생할 시 트랜잭션을 롤백하고 오류횟수를 증가시킨다.
         */
        int succCnt = 0;
        int errCnt = 0;
        for (String mallSeq : errMallSeqs) {
            /* 
             * 트랜잭션 시작
             */
            TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
            
            try {
                // 몰정보 가져오기
                Adsr adsr = adsrService.getAdsrAllInfo(mallSeq);
                // 몰정보가 존재하지 않는 경우
                if (adsr == null) {
                    throw new RuntimeException("mallInfo not found!");
                }
                
                // 해당 몰의 총경고카드개수, 평균상품수, 최근4주간의 오류보고수와 평가점수합계 가져오기
                MallEvalInfo mallEvalInfo = prodErrRepository.selectErrReportedMallEvalInfo(mallSeq, jobYmd); 

                /* 
                 * 4주간의 오류 신고회수와 평가점수의 합계를 판단해서 경고카드 개수를 구한다.
                 * 유효경고카드개수와 총경고카드개수를 증가시킨다.
                 */
                CleanCalculateResult cleanResult = calculateCleanCard(mallEvalInfo);
                
                // 유효경고갯수로 몰클린등급을 구함
                CleanGrade cleanGrade = evalVldWarnCardCntCleanGrade(cleanResult.getVldWarnCardCnt()); 

                // 몰경고카드 생성
                MallWarnCard mallWarnCard = makeMallWarnCard(adsr, cleanGrade, cleanResult, mallEvalInfo);
                
                // 광고주 클린등급 업데이트
                Adsr updateAdsrParam = new Adsr();
                updateAdsrParam.setMallSeq(mallSeq);
                updateAdsrParam.setClenGrd(Integer.parseInt(cleanGrade.getLabel()));
                adsrService.updateAdsr(updateAdsrParam);
                

                /*
                 * 몰경고카드 등록
                 */
                long mallWarnCardSeq = addMallWarnCard(mallWarnCard, jobYmd);

                /*
                 * 클린등급이 2등급 이하면 몰 담당자(AdsrChgr)에게 Email, SMS 전송
                 */
                if (cleanGrade.getValue() >= CleanGrade.CLEAN2.getValue()) {
                    /*
                     * 몰경고카드 등록 > 메시지(메일, SMS) 발송 > 메일ID 업데이트 순
                     * Email 전송 후 messageId를 받아 warnCard에 업데이트 
                     * 클린등급 메일 발송내역 확인용.
                     */
                    // 몰 담당자 정보
                    Chrgr chrgr = chrgrService.getOne(mallSeq); 
                    if (chrgr == null) { // 실제로는 null이 나오지 않을 것이다.(테스트용) 
                        logger.warn("chrgr not found!");
                        continue;
                    }
                    
                    // 메일 메시지 발송 (메일 메시지큐ID를 받아서 몰경고카드 업데이트용으로 전달)
                    long mailMsgQueId = notifyMallEmail(chrgr.getEmailAddr(), adsr.getMallNm(), cleanGrade, adsr.getAdsrTypeCd(), cleanResult.getTotWarnCardCnt());
                    
                    // SMS 메시지 발송
                    notifyMallSms(chrgr.getTelNo());
                    
                    // 메일ID 업데이트
                    modifyMallWarnCardMailMsgQueId(mallWarnCardSeq, mailMsgQueId);
                }
                
                /*
                 * (성공시) 
                 * 트랜잭션 커밋
                 */
                txManager.commit(txStatus);
                // 성공 카운트
                succCnt++;
            } catch (Exception e) {
                /* 
                 * 트랜잭션 롤백
                 */
                txManager.rollback(txStatus);
                logger.error("make MallWarnCard failed.", e);
                // 오류횟수 증가
                errCnt++;
            }
            
        }
        
        /*
         * 관리자에게 배치 실행 결과 메시지를 발송한다.
         */
        notifyResultToAdmin(succCnt, errCnt);
    }
