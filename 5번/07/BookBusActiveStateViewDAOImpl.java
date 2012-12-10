/**
 * FileName  : BookBusActiveStateViewDAOImpl.java
 * Author    : MyUserName
 * Date      : 2012. 5. 3.
 * Copyright : Copyright NHN Corp. Copyright The Beautiful Foundation. All Rights Reserved.
 */
package com.naver.bookcampaign.bookbus.bus2012.service.active.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naver.bookcampaign.base.dao.BookcampaignBaseDAO;
import com.nhncorp.lucy.common.util.DataMap;
import com.nhncorp.lucy.common.util.ExtendedMap;
import com.nhncorp.lucy.web.helper.PagerInfo;

/**
 * @sqlmap.resource id="bookcampaign" file="BookBusActiveStateView.xml"
 */
public class BookBusActiveStateViewDAOImpl extends BookcampaignBaseDAO implements BookBusActiveStateViewDAO {
	private static Log log = LogFactory.getLog(BookBusActiveStateViewDAOImpl.class);
	private static final String NAMESPACE = "com.naver.bookcampaign.bookbus.bus2012.service.active.BookBusActiveStateView.";

	public List<ExtendedMap> selectApplyCalInfo(int ym, String busNo) {
		ExtendedMap data = new DataMap();
		data.put("ym", ym+"%");
		data.put("busNo", busNo);
		
		return queryForList(NAMESPACE + "selectApplyCalInfo", data);
	}

	public List<ExtendedMap> selectApplyBeforeCalInfo(int ym, String busNo) {
		ExtendedMap data = new DataMap();
		data.put("ym", ym+"%");
		data.put("busNo", busNo);
		
		return queryForList(NAMESPACE + "selectApplyBeforeCalInfo", data);
	}

	public void updateAplyStatCd(String aplyStateCd, String aplyNo, String ymd) {
		ExtendedMap data = new DataMap();
		data.put("aplyStateCd", aplyStateCd);
		data.put("aplyNo", aplyNo);
		data.put("ymd", ymd);
		update(NAMESPACE + "updateAplyStatCd", data);
	}

	public int selectVstDataListCount(List<String> sido, String type) {
		ExtendedMap data = new DataMap();
		data.put("sido", sido);
		if ("ymd".equals(type)) {
			return (Integer)queryForObject(NAMESPACE + "selectVstDayCount" , data);
		} else {
			return (Integer)queryForObject(NAMESPACE + "selectVstDataListCount" , data);
		}
	}

	public List<ExtendedMap> selectVstDataList(List<String> sido, List<String> ymd) {
		ExtendedMap data = new DataMap();
		data.put("sido", sido);
		data.put("ymd", ymd);
		return queryForList(NAMESPACE + "selectVstDataList", data);
	}

	public List<ExtendedMap> selectVstYmdList(List<String> sido, PagerInfo pagerInfo) {
		ExtendedMap data = new DataMap();
		data.put("sido", sido);
		data.put("startRow", pagerInfo.getStartRownum());
		data.put("endRow", pagerInfo.getEndRownum());
		return queryForList(NAMESPACE + "selectVstYmdList", data);
	}
}
