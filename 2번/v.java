    /**
     * 경고카드 판단
     * 1) 상품개수      ~100,000 - 오류신고회수 3 이상 && 평가점수 25 이상
     * 2) 상품개수 100,000~300,000 - 오류신고회수 3 이상 && 평가점수 100 이상
     * 3) 상품개수 300,000~      - 오류신고회수 3 이상 && 평가점수 300 이상
     * @param rptCnt 오류신고회수
     * @param evalPnt 평가점수
     * @param prodCnt 평균상품수   
     */
    boolean isWarnCardLevel(int rptCnt, int evalPnt, long prodCnt) {
        if ( (prodCnt <= PROD_CNT_1ST && rptCnt >= RPT_CNT_MAX && evalPnt >= EVAL_PNT_MAX_A)
            || (prodCnt <= PROD_CNT_2ND && rptCnt >= RPT_CNT_MAX && evalPnt >= EVAL_PNT_MAX_B)
            || (prodCnt > PROD_CNT_2ND && rptCnt >= RPT_CNT_MAX && evalPnt >= EVAL_PNT_MAX_C) ) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 2단계 경고카드 판단
     * 1) 상품개수      ~100,000 - 오류신고회수 5 이상 && 평가점수 50 이상
     * 2) 상품개수 100,000~300,000 - 오류신고회수 5 이상 && 평가점수 200 이상
     * 3) 상품개수 300,000~      - 오류신고회수 5 이상 && 평가점수 500 이상 
     * @param rptCnt 오류신고회수
     * @param evalPnt 평가점수
     * @param prodCnt 평균상품수   
     */
    boolean isWarnCardLevel2nd(int rptCnt, int evalPnt, long prodCnt) {
        if ( (prodCnt <= PROD_CNT_1ST && rptCnt >= RPT_CNT_MAX_2ND && evalPnt >= EVAL_PNT_MAX_A_2ND)
            || (prodCnt <= PROD_CNT_2ND && rptCnt >= RPT_CNT_MAX_2ND && evalPnt >= EVAL_PNT_MAX_B_2ND)
            || (prodCnt > PROD_CNT_2ND && rptCnt >= RPT_CNT_MAX_2ND && evalPnt >= EVAL_PNT_MAX_C_2ND) ) {
            return true;
        } else {
            return false;
        }
    }


