package com.nbp.nsight.apache.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nbp.nsight.common.analyzer.OpenApiException;
import com.nbp.nsight.common.dto.SearchParam;

@Service
public interface ApacheManager {
	
	/**
	 * Apache 모니터링 항목을 삭제한다. 
	 * @param list 
	 * @param applPorts 
	 */
	void updateApacheMonitoring(ArrayList list) throws SQLException;
	
	/**
	 * Apache 모니터링 항목을 등록한다. 
	 * @param map :  
	 * map.put("statusUrl",page); : status 페이지 url
	   map.put("applPort",port); : 어플리케이션 port
	   map.put("modrEmpNo","NB10108"); : 담당자 사번
	 * @param hostIds : 등록할 hostIds 들 
	 * @param ports 
	 */
	String addApacheMonitoring(HashMap map, String[] hostIds, String[] ports) throws SQLException;
	
	/**
	 * Ajax 전체공통 Apache의 status를 리턴한다.
	 * @param SearchParam search
	 * @return List<HashMap>
	 */
	List<HashMap> getApacheStatus(SearchParam search) throws SQLException;
	
	/**
	 * Ajax 전체공통 Apache의 status 결과값을 리턴한다.
	 * @param SearchParam search
	 * @return int
	 */
	int getApacheStatusCount(SearchParam search) throws SQLException;
	
	/**
	 * 각 아파치 host 상세정보를 리턴한다.
	 * @param 
	 * @return List<HashMap>
	 */
	List<HashMap> getApacheHostInformation(SearchParam search) throws SQLException;

	/**
	 * 각 아파치 운영담당자의 상세정보를 리턴한다.
	 * @param 
	 * @return List<HashMap>
	 */
	List<HashMap> getApacheHostOperatorInformation(SearchParam search) throws SQLException;

	/**
	 * Ajax Apache의 process performance를 를  처리한다.
	 * @param hostId (apache hostId) 
	 * @return Apache의 process performance last udpate date
	 */
	Date getLastUpdateTime(String hostId) throws SQLException;
	
	/**
	 * Analyzer Data Access
	 * Ajax Apache의 process performance를 를  처리한다.
	 * @param hostId (apache hostId), lastUpdateTime (apache perfomance 마지막 update 시간) 
	 * @param search 
	 * @return List<Map> (JSon 형식으로 return)
	 */
	List<Map> getApacheProcessPerformances(String hostId, SearchParam search, Date lastUpdateTime) throws OpenApiException;

	List<Map> getApacheMonitoring(SearchParam search)  throws SQLException;

	int getApacheMonitoringCount(SearchParam search) throws SQLException;

	

}