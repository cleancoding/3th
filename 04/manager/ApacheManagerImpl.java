package com.nbp.nsight.apache.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nbp.nsight.apache.dao.ApacheDao;
import com.nbp.nsight.apache.dao.ApacheDaoIBatisImpl;
import com.nbp.nsight.common.analyzer.Analyzer;
import com.nbp.nsight.common.analyzer.ApiParam;
import com.nbp.nsight.common.analyzer.FieldCode;
import com.nbp.nsight.common.analyzer.OpenApiException;
import com.nbp.nsight.common.base.BaseDao;
import com.nbp.nsight.common.dto.SearchParam;
import com.nbp.nsight.server.dao.ServerDao;
import com.nbp.nsight.server.dao.ServerDaoIBatisImpl;

//@Service(value = "apacheManager")
@SuppressWarnings("unchecked")
@Service
public class ApacheManagerImpl implements ApacheManager {

	// @Autowired
	private ApacheDao apacheDao = new ApacheDaoIBatisImpl();	
	private ServerDao serverDao = new ServerDaoIBatisImpl();
	
	@Autowired
	private Analyzer analyzer;
	

	@Override
	public void updateApacheMonitoring(ArrayList list) throws SQLException {
		apacheDao.updateApacheMonitoring(list);		
	}

	@Override
	public List<Map> getApacheMonitoring(SearchParam search) throws SQLException {
		return apacheDao.selectApacheMonitoring(search);
	}

	@Override
	public int getApacheMonitoringCount(SearchParam search) throws SQLException {
		return apacheDao.selectApacheMonitoringCount(search);
	}
	
	@Override
	public String addApacheMonitoring(HashMap map, String[] hostIds, String[] ports) throws SQLException {
		int i;
		String errCode="";
		try {	
			
				for (i = 0; i < hostIds.length; i++) {
						map.put("hostId", hostIds[i]);
						map.put("applPort",ports[i]);
						map.put("mntrnObjId",serverDao.insertMntrnObj("APP"));
						apacheDao.insertApacheMonitoring(map);
						
						//map은 키의 중복 허용이 되지 않는다.
						map.remove("hostId");
						map.remove("applPort");
						map.remove("mntrnObjId");
				}
				
		}catch (Exception e) {
			errCode="Exception 발생";
			e.printStackTrace();
		}
		return errCode;
	}
	
	@Override
	public List<HashMap> getApacheStatus(SearchParam search) throws SQLException {
		return apacheDao.selectApacheStatus(search);
	}
	
	@Override
	public int getApacheStatusCount(SearchParam search) throws SQLException {
		return apacheDao.selectApacheStatusCount(search);
	}

	@Override
	public List<HashMap> getApacheHostInformation(SearchParam search) throws SQLException {
		return apacheDao.selectApacheHostInformation(search);
	}
	
	@Override
	public List<HashMap> getApacheHostOperatorInformation(SearchParam search) throws SQLException {
		return apacheDao.selectApacheHostOperatorInformation(search);
	}
		
	@Override
	public Date getLastUpdateTime(String hostId) throws SQLException {
		return apacheDao.selectLastUpdateTime(hostId);
	}
	
	
	
	/**
	 * Analyzer Data Access 
	 * Ajax Apache의 process performance를 를  처리한다.
	 * @param hostId (apache hostId), lastUpdateTime (apache perfomance 마지막 update 시간) 
	 * @return List<Map> (JSon 형식으로 return)
	 */	
	@Override
	public List<Map> getApacheProcessPerformances(String hostId,SearchParam search, Date lastUpdateTime)
			throws OpenApiException {
		
		Map colModels = new HashMap<String, Integer>();
		
		String[] colModelsNm
		= new String[] {"procTxMb","clntIp","cpuRto","oprMdeTpCd","slotTxMb","conTxKb","reqUri",
						"execRcntReqExecTmSs","pid","execAprchCnt","virtHostNm","execRcntStrtTmSs"};
		
		colModels.put("pid", FieldCode.ApacheProcess.pid); //459264
		colModels.put("execAprchCnt", FieldCode.ApacheProcess.exec_aprch_cnt); //459776
		colModels.put("oprMdeTpCd", FieldCode.ApacheProcess.opr_mde_tp_cd); //460032
		colModels.put("cpuRto", FieldCode.ApacheProcess.cpu_rto); // 460288
		colModels.put("execRcntStrtTmSs", FieldCode.ApacheProcess.exec_rcnt_strt_tm_ss); //460544
		colModels.put("execRcntReqExecTmSs", FieldCode.ApacheProcess.exec_rcnt_req_exec_tm_ss); //460800
		colModels.put("conTxKb", FieldCode.ApacheProcess.con_tx_kb); // 459520
		colModels.put("procTxMb", FieldCode.ApacheProcess.proc_tx_mb); //461056
		colModels.put("slotTxMb", FieldCode.ApacheProcess.slot_tx_mb); //461312
		colModels.put("clntIp", FieldCode.ApacheProcess.clnt_ip); //461568
		colModels.put("virtHostNm", FieldCode.ApacheProcess.virt_host_nm); //461824
		colModels.put("reqUri", FieldCode.ApacheProcess.req_uri); //462080
		
		ApiParam apiParam = new ApiParam();
		
		Date startDt = lastUpdateTime;
		Date endDt = lastUpdateTime;
		
		if(search != null){
			for(int i=0 ; i < colModelsNm.length ; i++){
				if(colModelsNm[i].equals(search.getSortIdx())){
					String orderby="";
					
					//만약 내림차순이면 -를 붙인다.
					if(search.getSortDrt().equals("desc")){
						orderby += "-";
					}					 
					
					//인덱스의 가장 처음은 2로 시작하므로 기본 i에 +2를 넣는다.
					apiParam.setOrderby(orderby + Integer.toString(i+2));
					break;
				}
			}
		}
		

		apiParam.setMethod("retrieveperf");
		apiParam.setKeyType("hostId");
		apiParam.setKeys(new String[] { hostId });
		
		apiParam.setStartTime(startDt);
		apiParam.setEndTime(endDt);
		apiParam.setInterval("1");
		apiParam.setGrouping("0");
		apiParam.setFormat("complete");
		
		return analyzer.execute(apiParam, colModels);
		
	}



}
