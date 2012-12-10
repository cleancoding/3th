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
		
		//�Ϻ� �ִ� 2�ǰ�������
		LinkedHashMap<String, ArrayList<ExtendedMap>> hash = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		for(ExtendedMap data : calData) {
			if(hash.containsKey(data.getString("cal_date")) && hash.get(data.getString("cal_date")).size() < 2 && !"N".equals(data.getString("bltn_yn"))) {
				//�̹� ����Ʈ�� �����ϴ°��, �ִ� 2���� �߰��Ѵ�.
				if(data.getString("ins_nm") != null) {
					if(data.getString("ins_nm").length() > 5) {
						data.put("ins_nm", data.getString("ins_nm").substring(0, 5) + "..");
					} else {
						data.put("ins_nm", data.getString("ins_nm"));
					}
				}
				hash.get(data.getString("cal_date")).add(data);
			} else if(!hash.containsKey(data.getString("cal_date"))) {
				if("N".equals(data.getString("bltn_yn"))) {//����� �������ΰ��� ������������ �����Ű�� �ʱ����Ͽ� "" ��ĭ���� ������
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
		lastDay.set(ym/100, ym%100, 1);	//���ÿ����� ������
		lastDay.add(Calendar.DATE, -1);	//���ÿ��� ���������ڰ��
		
		//�� ��¥ ����
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
		
		//�Ϻ� �ִ� 2�ǰ������� (������ ��¥ �������ذ� ������ ������� 3���� ����)
		for(ExtendedMap data : calData) {
			if(hash.get(data.getString("cal_date")).size() < 3 && !"N".equals(data.getString("bltn_yn"))) {
				//�̹� ����Ʈ�� �����ϴ°��, �ִ� 2���� �߰��Ѵ�.
				if(data.getString("ins_nm") != null) {
					if(data.getString("ins_nm").length() > 5) {
						data.put("ins_nm", data.getString("ins_nm").substring(0, 5) + "..");
					} else {
						data.put("ins_nm", data.getString("ins_nm"));
					}
				}
				hash.get(data.getString("cal_date")).add(data);
			} else if(!hash.containsKey(data.getString("cal_date"))) {
				if("N".equals(data.getString("bltn_yn"))) {//����� �������ΰ��� ������������ �����Ű�� �ʱ����Ͽ� "" ��ĭ���� ������
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
	
	/*å���� �湮�� ���� count*/
	public int getVstDataListCount(List<String> sido, String type) {
		int resultCnt = bookBusActiveStateViewDAO.selectVstDataListCount(sido, type);
		
		if ("vst".equals(type)) {
			//2012�� 4�� ������ ���ⵥ���͸� �ܱ�� �������鼭 �������� �湮Ƚ�� ������Ŵ
			if (sido.contains("����%")) {
				resultCnt += 247;
			} else if (sido.contains("���%")) {
				resultCnt += 298;
			} else if (sido.contains("���%")) {
				resultCnt += 308;
			} else if (sido.contains("�泲%")) {
				resultCnt += 433;
			} else if (sido.contains("���%")) {
				resultCnt += 225;
			} else if (sido.contains("�泲%")) {
				resultCnt += 468;
			} else if (sido.contains("����%")) {
				resultCnt += 307;
			} else if (sido.contains("����%")) {
				resultCnt += 310;
			} else if (sido.contains("����%")) {
				resultCnt += 81;
			} else {
				resultCnt += 2677;
			} 
		}
		return resultCnt;
	}

	/*å���� �湮�� ���� ����Ʈ*/
	public LinkedHashMap<String, ArrayList<ExtendedMap>> getVstDataList(List<String> sido, PagerInfo pagerInfo) {
		//��¥ �켱 ��������
		List<ExtendedMap> ymdList = bookBusActiveStateViewDAO.selectVstYmdList(sido, pagerInfo);
		List<String> ymd = new ArrayList<String>();
		
		//��¥���� ����
		for(ExtendedMap data : ymdList) {
			ymd.add(data.getString("ymd"));
		}
		//������ ������ ��¥�� �ش��ϴ� �����Ͱ�������
		List<ExtendedMap> vstDataList = bookBusActiveStateViewDAO.selectVstDataList(sido, ymd);
		LinkedHashMap<String, ArrayList<ExtendedMap>> resultHash = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		
		for(ExtendedMap data : vstDataList) {
			if(resultHash.containsKey(data.getString("ymd"))) {
				if (resultHash.get(data.getString("ymd")).size() < 6) {
					//�̹� �ؽÿ� �����ϴ� ��� �ִ� 6������ �߰�
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
