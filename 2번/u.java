    /**
     * 해당 주간 오류신고회수를 구함
     */
    int getMallEvalInfoRptCnt(MallEvalInfo mallEvalInfo, int week) {
        if (week == 1) {
            return mallEvalInfo.getWeek1Cnt();
        } else if (week == 2) {
            return mallEvalInfo.getWeek2Cnt();
        } else if (week == 3) {
            return mallEvalInfo.getWeek3Cnt();
        } else if (week == 4) { 
            return mallEvalInfo.getWeek4Cnt();
        } else {
            throw new RuntimeException("getMallEvalInfoRptCnt failed.");
        }
    }
