/**
 * FileName  : BookBusActiveStateAction.java
 * Date      : 2012. 5. 2.
 * Copyright : Copyright NHN Corp. Copyright The Beautiful Foundation. All Rights Reserved.
 */
package com.naver.bookcampaign.bookbus.bus2012.service.active.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naver.bookcampaign.bookbus.bus2012.service.active.bo.BookBusActiveStateViewBO;
import com.naver.bookcampaign.common.util.ActionForward;
import com.nhncorp.lucy.common.util.DataMap;
import com.nhncorp.lucy.common.util.ExtendedMap;
import com.nhncorp.lucy.web.actions.BaseAction;
import com.nhncorp.lucy.web.context.ServiceContext;
import com.nhncorp.lucy.web.helper.PagerInfo;
import com.nhncorp.lucy.web.interceptor.PagerInfoAware;

/**     
 * 
 */
public class BookBusActiveStateAction extends BaseAction implements PagerInfoAware {
	private static Log log = LogFactory.getLog(BookBusActiveStateAction.class);
	private BookBusActiveStateViewBO bookBusActiveStateViewBO;
	
	private static final String ACTIVE_PHRS_CD = "PHRS1";
	private static final String APLY_COMPLETE_CD = "BSAP3";
	
	private PagerInfo pagerInfo;
	private static final String PAGER_NAME = "bookbus-vst-list";
	private static final int PAGE_SIZE = 7;
	private static final int INDEX_SIZE = 5;
	
	/* (non-Javadoc)
	 * @see com.nhncorp.lucy.web.actions.NoneContinuableActionSupport#execute()
	 */
	@Override
	public String execute() throws Exception {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat today = new SimpleDateFormat("yyyyMM");
		
		LinkedHashMap<String, ArrayList<ExtendedMap>> calData = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
			
		int ym = params.getInt("mainSelectYm", 0);
		String busNo = params.getString("busNo", "1");
		
		if(ym != 0) { //메인에서 달력 클릭하여 접근한 경우
			String nowYm = today.format(cal.getTime());
			ServiceContext.setAttribute("mainSelectYm", ym);
			ServiceContext.setAttribute("mainSelectBusNo", busNo);

			if(ym < Integer.parseInt(nowYm)) {
				//이전 데이터는 신청가능일 상관없이 가져오기
				calData = bookBusActiveStateViewBO.getApplyBeforeCalInfo(ym, busNo);
			} else {
				//현재 이후데이터는 신청가능여부 확인하여 데이터 가져오기 (달력데이터)
				calData = bookBusActiveStateViewBO.getApplyCalInfo(ym, busNo);
			}
			//월별 활동현황 문구
			String monthContent = bookBusActiveStateViewBO.getMonthContent(ym+"", ACTIVE_PHRS_CD);
			ServiceContext.setAttribute("monthContent", monthContent);
			ServiceContext.setAttribute("calData", calData);
		} else {	//활동현황 메뉴로 첫 접근시
			String nowYm = today.format(cal.getTime());
			//달력에 표기할 데이터
			calData = bookBusActiveStateViewBO.getApplyCalInfo(Integer.parseInt(nowYm), "1");

			//월별 활동현황 문구
			String monthContent = bookBusActiveStateViewBO.getMonthContent(nowYm, ACTIVE_PHRS_CD);
			ServiceContext.setAttribute("calData", calData);
			ServiceContext.setAttribute("monthContent", monthContent);
		}
		return SUCCESS;
	}
	
	/**활동현황 달력부분 데이터
	 * @return
	 */
	public String calDataAjax() {
		String busNo = params.getString("busNo");
		int ym = params.getInt("ym");
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat today = new SimpleDateFormat("yyyyMM");
   		String nowYm = today.format(cal.getTime());
		
		LinkedHashMap<String, ArrayList<ExtendedMap>> calData = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		if(ym < Integer.parseInt(nowYm)) {
			//이전 데이터는 신청가능일 상관없이 가져오기
			calData = bookBusActiveStateViewBO.getApplyBeforeCalInfo(ym, busNo);
			ServiceContext.setAttribute("calData", calData);
			return "before";			
		} else {
			//현재 이후데이터는 신청가능여부 확인하여 데이터 가져오기 (달력데이터)
			calData = bookBusActiveStateViewBO.getApplyCalInfo(ym, busNo);
			ServiceContext.setAttribute("calData", calData);
			return "after";
		}
	}
	
	/**활동현황 월별문구
	 * @return
	 * @throws Exception 
	 */
	public String monthPhrsAjax() throws Exception {
		String ym = params.getString("ym");
		
		//월별 활동현황 문구
		String monthContent = bookBusActiveStateViewBO.getMonthContent(ym, ACTIVE_PHRS_CD);
		ServiceContext.setAttribute("monthContent", monthContent);
		
		return SUCCESS;
	}
	
	/**활동현황 프린트
	 * @return
	 * @throws Exception 
	 */
	public String printCalendar() throws Exception {
		String busNo = params.getString("busNo");
		int ym = params.getInt("ym");
	
		String yyyy = ym/100 + "";
		String mm = ym%100 + "";
		if(ym%100 < 10) {
			mm = "0" + ym%100;
		}
		
		LinkedHashMap<String, ArrayList<ExtendedMap>> calData = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		
		calData = bookBusActiveStateViewBO.getApplyBeforeCalInfo(ym, busNo);
		
		String monthContent = bookBusActiveStateViewBO.getMonthContent(ym+"", ACTIVE_PHRS_CD);

		ServiceContext.setAttribute("calData", calData);
		ServiceContext.setAttribute("monthContent", monthContent);
		ServiceContext.setAttribute("busNo", busNo);
		ServiceContext.setAttribute("yyyy", yyyy);
		ServiceContext.setAttribute("mm", mm);
		
		return SUCCESS;
	}
	
	
	/**활동현황 1차확인 상태변경
	 * @return
	 */
	public String changeState() {
		String aplyNo = params.getString("aplyNo");
		String ymd = params.getString("ymd");
		
		bookBusActiveStateViewBO.setAplyStatCd(APLY_COMPLETE_CD, aplyNo, ymd);
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat today = new SimpleDateFormat("yyyyMM");
		String ym = today.format(cal.getTime());
		
		//달력에 표기할 데이터
		LinkedHashMap<String, ArrayList<ExtendedMap>> calData = bookBusActiveStateViewBO.getApplyCalInfo(Integer.parseInt(ym), "1");

		//월별 활동현황 문구
		ServiceContext.setAttribute("calData", calData);
		
		return ActionForward.alertAndBack("접수가 모두 완료되었습니다.");
	}
	
	/**활동현황 하단 지도부분
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String mapAjax() throws UnsupportedEncodingException {
		String sido = params.getString("sido");
		String sidoKr = "";
		String sidoEn = "";
		List<String> sidoMap = new ArrayList<String>();
		
		//시도 세팅
		if("kangwondo".equals(sido)) {
			sidoKr = "강원도";
			sidoMap.add("강원%");
		} else if("kynggido".equals(sido)) {
			sidoKr = "경기도";
			sidoMap.add("경기%");
			sidoMap.add("인천%");
			sidoMap.add("서울%");
		} else if("chungbuk".equals(sido)) {
			sidoKr = "충청북도";
			sidoMap.add("충북%");
		} else if("chungnam".equals(sido)) {
			sidoKr = "충청남도";
			sidoMap.add("충남%");
			sidoMap.add("대전%");
		} else if("kyngbuk".equals(sido)) {
			sidoKr = "경상북도";
			sidoMap.add("경북%");
		} else if("kyngnam".equals(sido)) {
			sidoKr = "경상남도";
			sidoMap.add("경남%");
			sidoMap.add("대구%");
			sidoMap.add("부산%");
			sidoMap.add("울산%");
		} else if("jeonbuk".equals(sido)) {
			sidoKr = "전라북도";
			sidoMap.add("전북%");
		} else if("jeonnam".equals(sido)) {
			sidoKr = "전라남도";
			sidoMap.add("전남%");
			sidoMap.add("광주%");
		} else if("jeju".equals(sido)) {
			sidoKr = "제주도";
			sidoMap.add("제주%");
		} else if("all".equals(sido)) {
			sidoKr = "전체";
			String classNm = "btn_ch";
			ServiceContext.setAttribute("classNm", classNm);
		} 
		
		//날짜리스트 개수
		int totalRows = bookBusActiveStateViewBO.getVstDataListCount(sidoMap, "ymd");
		pagerInfo.init(PAGER_NAME, totalRows, PAGE_SIZE, INDEX_SIZE);
		
		//버스 방문리스트 개수
		int vstRows = bookBusActiveStateViewBO.getVstDataListCount(sidoMap, "vst");
		
		//key : 날짜, value : 날짜별신청리스트
		LinkedHashMap<String, ArrayList<ExtendedMap>>  dataList = new LinkedHashMap<String, ArrayList<ExtendedMap>>();
		dataList = bookBusActiveStateViewBO.getVstDataList(sidoMap, pagerInfo);
		
		ServiceContext.setAttribute("dataList", dataList);
		ServiceContext.setAttribute("sidoKr", sidoKr);
		ServiceContext.setAttribute("sidoEn", sido);
		ServiceContext.setAttribute("dataCount", totalRows);
		ServiceContext.setAttribute("vstCount", vstRows);
		return SUCCESS;
	}
	
	public void setBookBusActiveStateViewBO(BookBusActiveStateViewBO bookBusActiveStateViewBO) {
		this.bookBusActiveStateViewBO = bookBusActiveStateViewBO;
	}

	public PagerInfo getPagerInfo() {
		return pagerInfo;
	}

	public void setPagerInfo(PagerInfo pagerInfo) {
		this.pagerInfo = pagerInfo;
	}
}
