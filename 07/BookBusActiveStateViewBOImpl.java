/**
 * FileName  : BookBusActiveStateViewBOImpl.java
 * Author    : MyUserName
 * Date      : 2012. 5. 3.
 * Copyright : Copyright NHN Corp. Copyright The Beautiful Foundation. All Rights Reserved.
 */
package com.naver.bookcampaign.bookbus.bus2012.service.active.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naver.bookcampaign.bookbus.bus2012.service.active.dao.BookBusActiveStateViewDAO;
import com.naver.bookcampaign.dao.book.BcBkBusMmbyPhrsDAO;
import com.nhncorp.lucy.common.util.DataMap;
import com.nhncorp.lucy.common.util.ExtendedMap;
import com.nhncorp.lucy.web.helper.PagerInfo;

/**
 * 
 */
public class BookBusActiveStateViewBOImpl implements BookBusActiveStateViewBO {
	private static Log log = LogFactory.getLog(BookBusActiveStateViewBOImpl.class);

	private BookBusActiveStateViewDAO bookBusActiveStateViewDAO;
	private BcBkBusMmbyPhrsDAO bcBkBusMmbyPhrsDAO;
	
	public LinkedHashMap<String, ArrayList<ExtendedMap>> getApplyCalInfo(int ym, String busNo) {
		List<ExtendedMap> calData = bookBusActiveStateViewDAO.selectApplyCalInfo(ym, busNo);
		
		//일별 최대 2건가져오기
		LinkedHashMap<String, ArrayList<ExtendedMap>> hash = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		for(ExtendedMap data : calData) {
			if(hash.containsKey(data.getString("cal_date")) && hash.get(data.getString("cal_date")).size() < 2 && !"N".equals(data.getString("bltn_yn"))) {
				//이미 리스트에 존재하는경우, 최대 2개만 추가한다.
				if(data.getString("ins_nm") != null) {
					if(data.getString("ins_nm").length() > 5) {
						data.put("ins_nm", data.getString("ins_nm").substring(0, 5) + "..");
					} else {
						data.put("ins_nm", data.getString("ins_nm"));
					}
				}
				hash.get(data.getString("cal_date")).add(data);
			} else if(!hash.containsKey(data.getString("cal_date"))) {
				if("N".equals(data.getString("bltn_yn"))) {//비공개 데이터인경우는 서비스페이지에 노출시키지 않기위하여 "" 빈칸으로 내보냄
					data.put("bus_no", "");
					data.put("spt_aply_stat_cd", "");
					data.put("strt_tm", "");
					data.put("end_tm", "");
					data.put("ins_nm", "");
					data.put("spt_aply_no", "");
					data.put("aplyr_mbr_id", "");
				}
				if(data.getString("ins_nm") != null) {
					if(data.getString("ins_nm").length() > 5) {
						data.put("ins_nm", data.getString("ins_nm").substring(0, 5) + "..");
					} else {
						data.put("ins_nm", data.getString("ins_nm"));
					}
				}
				ArrayList<ExtendedMap> list = new ArrayList<ExtendedMap>();
				list.add(data);
				hash.put(data.getString("cal_date"), list);
			}
		}
		return hash;
	}

	public LinkedHashMap<String, ArrayList<ExtendedMap>> getApplyBeforeCalInfo(int ym, String busNo) {
		List<ExtendedMap> calData = bookBusActiveStateViewDAO.selectApplyBeforeCalInfo(ym, busNo);
		LinkedHashMap<String, ArrayList<ExtendedMap>> hash = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		
		Calendar lastDay = Calendar.getInstance();
		lastDay.set(ym/100, ym%100, 1);	//선택연월의 다음달
		lastDay.add(Calendar.DATE, -1);	//선택연월 마지막일자계산
		
		//빈 날짜 세팅
		for(int date = 1 ; date <= lastDay.get(Calendar.DATE); date ++) {
			String dd = "";
			if(date < 10) {
				dd = "0" + date;
			} else {
				dd = "" + date;
			}
			ArrayList<ExtendedMap> tmpList = new ArrayList<ExtendedMap>();
			ExtendedMap map = new DataMap();
			map.put("psblYn", "N");
			map.put("ym", ym);
			map.put("cal_date", dd);
			map.put("spt_aply_stat_cd", null);
			tmpList.add(map);
				
			hash.put(dd, tmpList);
		}
		
		//일별 최대 2건가져오기 (이전에 빈날짜 세팅해준것 때문에 사이즈는 3개가 정상)
		for(ExtendedMap data : calData) {
			if(hash.get(data.getString("cal_date")).size() < 3 && !"N".equals(data.getString("bltn_yn"))) {
				//이미 리스트에 존재하는경우, 최대 2개만 추가한다.
				if(data.getString("ins_nm") != null) {
					if(data.getString("ins_nm").length() > 5) {
						data.put("ins_nm", data.getString("ins_nm").substring(0, 5) + "..");
					} else {
						data.put("ins_nm", data.getString("ins_nm"));
					}
				}
				hash.get(data.getString("cal_date")).add(data);
			} else if(!hash.containsKey(data.getString("cal_date"))) {
				if("N".equals(data.getString("bltn_yn"))) {//비공개 데이터인경우는 서비스페이지에 노출시키지 않기위하여 "" 빈칸으로 내보냄
					data.put("bus_no", "");
					data.put("spt_aply_stat_cd", "");
					data.put("strt_tm", "");
					data.put("end_tm", "");
					data.put("ins_nm", "");
					data.put("spt_aply_no", "");
					data.put("aplyr_mbr_id", "");
				}				
				if(data.getString("ins_nm") != null) {
					if(data.getString("ins_nm").length() > 5) {
						data.put("ins_nm", data.getString("ins_nm").substring(0, 5) + "..");
					} else {
						data.put("ins_nm", data.getString("ins_nm"));
					}
				}
				ArrayList<ExtendedMap> list = new ArrayList<ExtendedMap>();
				list.add(data);
				hash.put(data.getString("cal_date"), list);
			}
		}
		return hash;
	}
	
	public String getMonthContent(String ym, String phrsCd) throws Exception {
		ExtendedMap data = new DataMap();
		data.put("ym", ym);
		data.put("phrs_tp_cd", phrsCd);
		
		ExtendedMap contentMap = bcBkBusMmbyPhrsDAO.selectData(data);
		
		if(contentMap == null) {
			return "";
		} else {
			return contentMap.getString("phrs_cont");
		}
	}

	public void setAplyStatCd(String aplyStateCd, String aplyNo, String ymd) {
		bookBusActiveStateViewDAO.updateAplyStatCd(aplyStateCd, aplyNo, ymd);
	}
	
	/*책버스 방문한 지역 count*/
	public int getVstDataListCount(List<String> sido, String type) {
		int resultCnt = bookBusActiveStateViewDAO.selectVstDataListCount(sido, type);
		
		if ("vst".equals(type)) {
			//2012년 4월 이전의 정기데이터를 단기로 가져오면서 지역별로 방문횟수 증가시킴
			if (sido.contains("강원%")) {
				resultCnt += 247;
			} else if (sido.contains("경기%")) {
				resultCnt += 298;
			} else if (sido.contains("충북%")) {
				resultCnt += 308;
			} else if (sido.contains("충남%")) {
				resultCnt += 433;
			} else if (sido.contains("경북%")) {
				resultCnt += 225;
			} else if (sido.contains("경남%")) {
				resultCnt += 468;
			} else if (sido.contains("전북%")) {
				resultCnt += 307;
			} else if (sido.contains("전남%")) {
				resultCnt += 310;
			} else if (sido.contains("제주%")) {
				resultCnt += 81;
			} else {
				resultCnt += 2677;
			} 
		}
		return resultCnt;
	}

	/*책버스 방문한 지역 리스트*/
	public LinkedHashMap<String, ArrayList<ExtendedMap>> getVstDataList(List<String> sido, PagerInfo pagerInfo) {
		//날짜 우선 가져오기
		List<ExtendedMap> ymdList = bookBusActiveStateViewDAO.selectVstYmdList(sido, pagerInfo);
		List<String> ymd = new ArrayList<String>();
		
		//날짜부터 세팅
		for(ExtendedMap data : ymdList) {
			ymd.add(data.getString("ymd"));
		}
		//위에서 세팅한 날짜에 해당하는 데이터가져오기
		List<ExtendedMap> vstDataList = bookBusActiveStateViewDAO.selectVstDataList(sido, ymd);
		LinkedHashMap<String, ArrayList<ExtendedMap>> resultHash = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		
		for(ExtendedMap data : vstDataList) {
			if(resultHash.containsKey(data.getString("ymd"))) {
				if (resultHash.get(data.getString("ymd")).size() < 6) {
					//이미 해시에 존재하는 경우 최대 6개까지 추가
					resultHash.get(data.getString("ymd")).add(data);
				} else {
					break;
				}
			} else {
				ArrayList<ExtendedMap> list = new ArrayList<ExtendedMap>();
				list.add(data);
				resultHash.put(data.getString("ymd"), list);
			}
		}
		return resultHash;
	}
	
	public void setBookBusActiveStateViewDAO(BookBusActiveStateViewDAO bookBusActiveStateViewDAO) {
		this.bookBusActiveStateViewDAO = bookBusActiveStateViewDAO;
	}

	public void setBcBkBusMmbyPhrsDAO(BcBkBusMmbyPhrsDAO bcBkBusMmbyPhrsDAO) {
		this.bcBkBusMmbyPhrsDAO = bcBkBusMmbyPhrsDAO;
	}
}
