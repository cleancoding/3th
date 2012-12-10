
Magic Number
---
    Long addMallWarnCard(MallWarnCard card, String jobYmd) {
        try {
            card.setStrtYmd(DateUtils.addDays(jobYmd, -7));
            card.setEndYmd(DateUtils.addDays(jobYmd, -1));

