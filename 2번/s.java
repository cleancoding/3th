
    /**
     * 클린요약정보를 조회
     * 최근의 클린정보가 없을 경우에는 1등급으로 반환
     */
    MallWarnCard getCurrCleanBrief(String mallSeq) {
        String currYmd = DateUtils.date(DateUtils.Formats.yyyyMMdd);
        MallWarnCard cleanBrief = mallWarnCardRepository.selectCleanBrief(mallSeq, getLatestMonday(currYmd), getLatestSunday(currYmd));
        
        if (cleanBrief == null) {
            cleanBrief = new MallWarnCard();
            cleanBrief.setCardGrd(CleanGrade.CLEAN1.getValue());
            cleanBrief.setVldWarnCardCnt(0);
        }
        
        return cleanBrief;
    }

