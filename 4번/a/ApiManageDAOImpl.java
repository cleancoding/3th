/*
 * @(#)ApiManageDAOImpl.java $version 2012. 7. 4.
 *
 * Copyright 2011 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.weather.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.naver.weather.admin.model.LicenseServerInfo;

/**
 * @author yousun.choi
 */
public class ApiManageDAOImpl extends AbstractDAO implements ApiManageDAO {
    private static final String NAMESPACE = "ApiManage";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public LicenseServerInfo selectLicenseServerInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceNo", licenseServerInfo.getServiceNo());
        params.put("apiNo", licenseServerInfo.getApiNo());
        params.put("ip", licenseServerInfo.getIp());
        
        return (LicenseServerInfo) sqlMapClient.queryForObject(NAMESPACE + ".selectLicenseServerInfo", params);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int selectLicenseServerInfoCount(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("licenseNo", licenseServerInfo.getLicenseNo());
        params.put("serviceNo", licenseServerInfo.getServiceNo());
        params.put("apiNo", licenseServerInfo.getApiNo());
        params.put("ip", licenseServerInfo.getIp());
        
        return ((Integer) sqlMapClient.queryForObject(NAMESPACE + ".selectLicenseServerInfoCount", params)).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<LicenseServerInfo> selectLicenseServerInfoList(String serviceId, String actionName, String ip, int startRowNum, int pageSize) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        params.put("actionName", actionName);
        params.put("ip", ip);
        params.put("startRowNum", startRowNum);
        params.put("pageSize", pageSize);
        
        return sqlMapClient.queryForList(NAMESPACE + ".selectLicenseInfoList", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertLicenseServerInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceNo", licenseServerInfo.getServiceNo());
        params.put("apiNo", licenseServerInfo.getApiNo());
        params.put("ip", licenseServerInfo.getIp());
        params.put("description", licenseServerInfo.getDescription());
        params.put("adminId", licenseServerInfo.getAdminId());
        
        sqlMapClient.insert(NAMESPACE + ".insertLicenseServerInfo", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLicenseServerInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("licenseNo", licenseServerInfo.getLicenseNo());
        params.put("serviceNo", licenseServerInfo.getServiceNo());
        params.put("apiNo", licenseServerInfo.getApiNo());
        params.put("ip", licenseServerInfo.getIp());
        params.put("description", licenseServerInfo.getDescription());
        
        sqlMapClient.update(NAMESPACE + ".updateLicenseServerInfo", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteLicenseServerInfo(int licenseNo) throws Exception {
        sqlMapClient.delete(NAMESPACE + ".deleteLicenseServerInfo", licenseNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectServiceInfoCount(int serviceNo, String serviceId) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceNo", serviceNo);
        params.put("serviceId", serviceId);
        
        return ((Integer)sqlMapClient.queryForObject(NAMESPACE + ".selectServiceInfoCount", params)).intValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<LicenseServerInfo> selectServiceInfoList(String serviceId, String orderType) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        params.put("orderType", orderType);
        return sqlMapClient.queryForList(NAMESPACE + ".selectServiceInfoList", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertServiceInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", licenseServerInfo.getServiceId());
        params.put("description", licenseServerInfo.getDescription());
        params.put("adminId", licenseServerInfo.getAdminId());
        
        sqlMapClient.insert(NAMESPACE + ".insertServiceInfo", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceNo", licenseServerInfo.getServiceNo());
        params.put("serviceId", licenseServerInfo.getServiceId());
        params.put("description", licenseServerInfo.getDescription());
        
        sqlMapClient.update(NAMESPACE + ".updateServiceInfo", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteServiceInfo(int serviceNo) throws Exception {
        sqlMapClient.delete(NAMESPACE + ".deleteServiceInfo", serviceNo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int selectApiActionInfoCount(int apiNo, String actionName) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("apiNo", apiNo);
        params.put("actionName", actionName);
        
        return ((Integer)sqlMapClient.queryForObject(NAMESPACE + ".selectApiActionInfoCount", params)).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<LicenseServerInfo> selectApiActionInfoList(String actionName, String orderType) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("actionName", actionName);
        params.put("orderType", orderType);
        return sqlMapClient.queryForList(NAMESPACE + ".selectApiActionInfoList", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertApiActionInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("actionName", licenseServerInfo.getActionName());
        params.put("description", licenseServerInfo.getDescription());
        params.put("adminId", licenseServerInfo.getAdminId());
        
        sqlMapClient.insert(NAMESPACE + ".insertApiActionInfo", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateApiActionInfo(LicenseServerInfo licenseServerInfo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("apiNo", licenseServerInfo.getApiNo());
        params.put("actionName", licenseServerInfo.getActionName());
        params.put("description", licenseServerInfo.getDescription());
        
        sqlMapClient.update(NAMESPACE + ".updateApiActionInfo", params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteApiActionInfo(int apiNo) throws Exception {
        sqlMapClient.delete(NAMESPACE + ".deleteApiActionInfo", apiNo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int selectApiMappingCount(String serviceId, String actionName, String ip) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        params.put("actionName", actionName);
        params.put("ip", ip);
        
        return ((Integer)sqlMapClient.queryForObject(NAMESPACE + ".selectApiMappingCount", params)).intValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int selectReferredMappingData(int serviceNo, int apiNo) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceNo", serviceNo);
        params.put("apiNo", apiNo);
        
        return ((Integer)sqlMapClient.queryForObject(NAMESPACE + ".selectReferredMappingData", params)).intValue();
    }
    
}
