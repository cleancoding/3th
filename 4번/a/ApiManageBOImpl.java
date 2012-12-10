
public class ApiManageBOImpl implements ApiManageBO {
    private ApiManageDAO apiManageDAO;

    public void setApiManageDAO(ApiManageDAO apiManageDAO) {
        this.apiManageDAO = apiManageDAO;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLicenseServerInfo(String serviceNoList, String apiNoList, String ipList) throws Exception {
        String[] serviceNo = serviceNoList.split(",");
        String[] apiNo = apiNoList.split(",");
        String[] ip = ipList.split(",");
        LicenseServerInfo licenseServerInfo = new LicenseServerInfo();
        boolean result = false;
        
        for (int i = 0; i < serviceNo.length; i++) {        //REVIEW: L*M*N 코드
            for (int j = 0; j < apiNo.length; j++) {
                for (int k = 0; k < ip.length; k++) {
                    licenseServerInfo.setServiceNo(Integer.parseInt(serviceNo[i]));
                    licenseServerInfo.setApiNo(Integer.parseInt(apiNo[j]));
                    licenseServerInfo.setIp(ip[k]);
                    if (apiManageDAO.selectLicenseServerInfo(licenseServerInfo) != null) {
                        return true;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLicenseServerInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        boolean result = false; //REVIEW: 불필요한 임시변수 , 그런데 전반적으로 본인이 할 일이 얼마 없는 슬픈 클래스네요.
        
        if (apiManageDAO.selectLicenseServerInfoCount(licenseServerInfo) > 0) {
            result = true;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LicenseServerInfo> getLicenseServerInfoList(String serviceId, String actionName, String ip, int startRowNum, int pageSize) throws Exception {
        return apiManageDAO.selectLicenseServerInfoList(serviceId, actionName, ip, startRowNum, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registLicenseServerInfo(String serviceNoList, String apiNoList, String ipList, String description, String adminId) throws Exception {
        String[] serviceNo = serviceNoList.split(",");
        String[] apiNo = apiNoList.split(",");
        String[] ip = ipList.split(",");
        LicenseServerInfo licenseServerInfo = new LicenseServerInfo();
        
        for (int i = 0; i < serviceNo.length; i++) {
            for (int j = 0; j < apiNo.length; j++) {
                for (int k = 0; k < ip.length; k++) {
                    licenseServerInfo.setServiceNo(Integer.parseInt(serviceNo[i]));
                    licenseServerInfo.setApiNo(Integer.parseInt(apiNo[j]));
                    licenseServerInfo.setDescription(description);
                    licenseServerInfo.setAdminId(adminId);
                    if (ip[k].contains("-")) {
                        for (String eachIp : parseHostIp(ip[k])) {
                            licenseServerInfo.setIp(eachIp);
                            apiManageDAO.insertLicenseServerInfo(licenseServerInfo);
                        }
                    } else {
                        licenseServerInfo.setIp(ip[k]);
                        apiManageDAO.insertLicenseServerInfo(licenseServerInfo);
                    }
                }
            }
        }
    }
    
    /** 
     * C-Class Host IP를 Parsing하여 List형태로 반환  
     * 
     * @param ip
     * @return List
     * @throws Exception
     */
    private List<String> parseHostIp(String ipScope) throws Exception {
        int lastDelimiterIdx = ipScope.lastIndexOf(".");
        String netIp = ipScope.substring(0, lastDelimiterIdx + 1);      // Network IP (Network IP와 Host IP 분리)
        String hostIpList = ipScope.substring(lastDelimiterIdx + 1);    // Host IP List (Network IP와 Host IP 분리)
        
        String[] hostIp = hostIpList.split("-");        // Host IP 범위 값 분리   //REVIEW: 메소드로 뽑아요!
        int startHostIp = Integer.parseInt(hostIp[0]);  // Start Host IP
        int endHostIp = Integer.parseInt(hostIp[1]);    // End Host IP
        
        List<String> ipList = new ArrayList<String>();  
        
        for (int i = startHostIp; i <= endHostIp; i++) {
            ipList.add(netIp.concat(Integer.toString(i)));
        }
        return ipList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyLicenseServerInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        apiManageDAO.updateLicenseServerInfo(licenseServerInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLicenseServerInfo(String licenseNo) throws Exception {
        String[] lno = licenseNo.split(",");    //REVIEW: for in 쓰죠!
        for (int i = 0; i < lno.length; i++) {
            apiManageDAO.deleteLicenseServerInfo(Integer.parseInt(lno[i]));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServiceInfo(int serviceNo, String serviceId) throws Exception {
        boolean result = false;
        
        if (apiManageDAO.selectServiceInfoCount(serviceNo, serviceId) > 0) {
            result = true;
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<LicenseServerInfo> getServiceInfoList(String serviceId, String orderType) throws Exception {
        return apiManageDAO.selectServiceInfoList(serviceId, orderType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registServiceInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        apiManageDAO.insertServiceInfo(licenseServerInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyServiceInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        apiManageDAO.updateServiceInfo(licenseServerInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeServiceInfo(int serviceNo) throws Exception {
        apiManageDAO.deleteServiceInfo(serviceNo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApiActionInfo(int apiNo, String actionName) throws Exception {
        boolean result = false;
        
        if (apiManageDAO.selectApiActionInfoCount(apiNo, actionName) > 0) {
            result = true;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LicenseServerInfo> getApiActionInfoList(String actionName, String orderType) throws Exception {
        return apiManageDAO.selectApiActionInfoList(actionName, orderType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registApiActionInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        apiManageDAO.insertApiActionInfo(licenseServerInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyApiActionInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        apiManageDAO.updateApiActionInfo(licenseServerInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeApiActionInfo(int apiNo) throws Exception {
        apiManageDAO.deleteApiActionInfo(apiNo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getApiMappingCount(String serviceId, String actionName, String ip) throws Exception {
        return apiManageDAO.selectApiMappingCount(serviceId, actionName, ip);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReferredMappingData(int serviceNo, int apiNo) throws Exception {
        boolean result = false;
        
        if (apiManageDAO.selectReferredMappingData(serviceNo, apiNo) > 0) {
            result = true;
        }
        return result;
    }

}
