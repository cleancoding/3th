@SuppressWarnings("serial")
public class ApiAclMappingManageAction extends BaseAction implements PagerInfoAware {
    private ApiManageBO apiManageBO;
    protected PagerInfo pagerInfo;
    protected int page;
    protected int pageSize;
    
    private boolean isUseIIMS2 = true;

    @Inject(beanId = "apiManageBO")
    public void setApiManageBO(ApiManageBO apiManageBO) {
        this.apiManageBO = apiManageBO;
    }

    public PagerInfo getPagerInfo() {
        return pagerInfo;
    }

    public void setPagerInfo(PagerInfo pagerInfo) {
        this.pagerInfo = pagerInfo;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public void setUseIIMS2(boolean isUseIIMS2) {
        this.isUseIIMS2 = isUseIIMS2;
    }

    /**
     * API ACL 맵핑 목록 조회
     * 
     * @return
     * @throws Exception
     * @see com.nhncorp.lucy.web.actions.NoneContinuableActionSupport#execute()
     */
    @Override
    public String execute() throws Exception {
        String serviceId = "";
        String actionName = "";
        String ip = "";
        String searchCond = params.getString("searchCond", "svc_id");
        String searchKeyword = params.getString("keyword", "");
        List<LicenseServerInfo> serviceInfo = new ArrayList<LicenseServerInfo>();
        List<LicenseServerInfo> actionInfo = new ArrayList<LicenseServerInfo>();
        List<LicenseServerInfo> licenseServerInfo = new ArrayList<LicenseServerInfo>();
        
        if (searchCond.equals("svc_id")) {      //REVIEW: 만약 조건이 늘어나거나 바뀌게 된다면
            serviceId = searchKeyword;  //REVIEW: 이 조건에 맞아떨어져서 setting 되지 않는 변수들 actionName, ip의 경우에는 null이어야 하나 null string 이어야 하나를 알 수 없다.
        } else if (searchCond.equals("act_nm")) {
            actionName = searchKeyword;
        } else if (searchCond.equals("ip")) {
            ip = searchKeyword;
        }
        
        pagerInfo.init("default", apiManageBO.getApiMappingCount(serviceId, actionName, ip));
        pagerInfo.setQueryString(PagerTag.getQueryString());
        
        serviceInfo = apiManageBO.getServiceInfoList("", "serviceId");
        actionInfo = apiManageBO.getApiActionInfoList("", "actionName");
        licenseServerInfo = apiManageBO.getLicenseServerInfoList(serviceId, actionName, ip, pagerInfo.getStartRownum() - 1, pagerInfo.getPageSize());
        
        ServiceContext.setAttribute("service_list", serviceInfo);
        ServiceContext.setAttribute("action_list", actionInfo);
        ServiceContext.setAttribute("license_server_list", licenseServerInfo);
        ServiceContext.setAttribute("keyword", searchKeyword);
        ServiceContext.setAttribute("searchCond", searchCond);
        
        return SUCCESS;
    }
    
    /**
     * API ACL 맵핑 등록
     * 
     * @throws Exception
     */
    public void regist() throws Exception {
        String serviceNoList = params.getString("serviceNo", "0");
        String apiNoList = params.getString("apiNo", "0");
        String ipList = params.getString("ip", "");
        String description = params.getString("description", "");
        String adminId = "";
        
        if (isUseIIMS2) {   //REVIEW: getter, setter를 만든이유가..
            adminId = StringUtils.defaultIfEmpty(IIMS2UserHelper.getInstance().getUserId(), "KR00000");
        } else {
            adminId = "KR00000";    //REVIEW: 의미는?
        }
        
        if (apiManageBO.isLicenseServerInfo(serviceNoList, apiNoList, ipList)) {
            ServiceContext.writeResponse("already_exist");  //REVIEW: magic string
        } else {
            apiManageBO.registLicenseServerInfo(serviceNoList, apiNoList, ipList, description, adminId);
            ServiceContext.writeResponse("success");
        }
    }
    
    /**
     * API ACL 맵핑 정보 수정
     * 
     * @throws Exception
     */
    public void modify() throws Exception {
        int licenseNo = params.getInt("licenseNo", 0);  // magic number 0
        int serviceNo = params.getInt("serviceNo", 0);
        int apiNo = params.getInt("apiNo", 0);
        String ip = params.getString("ip", "");
        String description = params.getString("description", "");
        LicenseServerInfo licenseServerInfo = new LicenseServerInfo();
        
        licenseServerInfo.setLicenseNo(licenseNo);          //REVIEW: 모두다 세팅해야 하나? 아니면 일부는 안해도 되나?
        licenseServerInfo.setServiceNo(serviceNo);
        licenseServerInfo.setApiNo(apiNo);
        licenseServerInfo.setIp(ip);
        licenseServerInfo.setDescription(description);
        
        if (apiManageBO.isLicenseServerInfo(licenseServerInfo)) {
            ServiceContext.writeResponse("already_exist");
        } else {
            apiManageBO.modifyLicenseServerInfo(licenseServerInfo);
            ServiceContext.writeResponse("success");
        }
    }
    
    /**
     * API ACL 맵핑 정보 삭제
     * 
     * @throws Exception
     */
    public void remove() throws Exception {
        String licenseNo = params.getString("licenseNo", "0");
        
        apiManageBO.removeLicenseServerInfo(licenseNo);
        
        ServiceContext.writeResponse("success");
    }

}
