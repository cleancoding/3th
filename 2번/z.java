
    long notifyMallEmail(String emailAddr, String mallNm, CleanGrade cleanGrade, String adsrTypeCd, int totWarnCardCnt) {
        String cardGrdNm = cleanGrade.toString() + "등급"; // 2등급, 3등급..
        List<String> mailParams = new ArrayList<String>();
        mailParams.add(mallNm);
        mailParams.add(cardGrdNm);          
        mailParams.add(cardGrdNm);          
        mailParams.add(String.valueOf(totWarnCardCnt));

