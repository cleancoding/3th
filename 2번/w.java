    /**
     * 총오류신고회수 합산
     * @param mallEvalInfo
     * @return
     */
    int getTotWeekCnt(MallEvalInfo mallEvalInfo) {
        return mallEvalInfo.getWeek1Cnt() 
            + mallEvalInfo.getWeek2Cnt() 
            + mallEvalInfo.getWeek3Cnt() 
            + mallEvalInfo.getWeek4Cnt();
    }
