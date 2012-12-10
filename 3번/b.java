        List<CashConfigDetailView> cashConfigDetailViewList = new ArrayList<CashConfigDetailView>();
        List<CashConfig> cashConfigList = cashConfigService.getCashConfigListWithTradeSummaryForExcel(convertFormToCriteria(form));
        long totalCount = (cashConfigList != null) ? cashConfigList.size() : 0;
        for (CashConfig cashConfig : cashConfigList) {
            if (totalCount > 10000) {
                break;
            } 
            
            List<CashTrade> cashTradeList = cashTradeService.getAccumulateListForExcel(cashConfig.getId());
            totalCount += (cashTradeList != null) ? cashTradeList.size() : 0;
            for (CashTrade cashTrade : cashTradeList) {
                NCashVO cashInfo = remoteNCashService.getCashInfo(cashTrade.getMemberIdNo());
                CashConfigDetailView column = new CashConfigDetailView();
                
                column.setAccumulateCode(cashConfig.getAccumulateCode());
                column.setAcmConfigerCode(cashConfig.getAcmConfigerCode());
                column.setAcmStatusCode(cashConfig.getAcmStatusCode());
                column.setName(cashConfig.getName());
                column.setAcmStartYMD(cashConfig.getAcmStartYMD());
                column.setAcmEndYMD(cashConfig.getAcmEndYMD());
                column.setTradeCount(cashConfig.getTradeCount());
                column.setAcmConfigerName(cashConfig.getAcmConfigerName());
                column.setAcmConfigerId(cashConfig.getAcmConfigerId());
                column.setConfigDate(cashConfig.getConfigDate());
                column.setModDate(cashConfig.getModDate());
                column.setManegerMessage(cashConfig.getManegerMessage());
                column.setMemberName(cashTrade.getMemberName());
                column.setMemberId(cashTrade.getMemberId());
                column.setCashTradeAmount(cashTrade.getCashTradeAmount());
                column.setCashTradeDate(cashTrade.getCashTradeDate());
                column.setCurrentCash(cashInfo.getAmount());
                
                cashConfigDetailViewList.add(column);
            }
        }