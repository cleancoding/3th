
    /**
     * 몰경고카드 생성
     */
    MallWarnCard makeMallWarnCard(Adsr adsr, CleanGrade cleanGrade, CleanCalculateResult cleanResult, MallEvalInfo mallEvalInfo) {
        MallWarnCard mallWarnCard = new MallWarnCard(); 
        mallWarnCard.setMallId(adsr.getMallId());
        // (몰SEQ는 반드시 수로 변환되겠지?)
        mallWarnCard.setMallSeq(Long.parseLong(adsr.getMallSeq()));
        // (몰외부등급은 앞으로 안씀?)
//      mallWarnCard.setOuterGrd(adsr.getMallGrdTpCd()); 
        // 클린등급의 경고카드 값
        mallWarnCard.setCardGrd(cleanGrade.getValue()); 
        mallWarnCard.setTotWarnCardCnt(cleanResult.getTotWarnCardCnt());
        mallWarnCard.setVldWarnCardCnt(cleanResult.getVldWarnCardCnt());
        mallWarnCard.setTotErrRptCnt(cleanResult.getTotErrRptCnt());
        mallWarnCard.setTotErrRptEvalPnt(cleanResult.getTotErrRptEvalPnt());
        mallWarnCard.setAvgSvcProdCnt(mallEvalInfo.getAvgProdCnt());
        
        return mallWarnCard;
    }

