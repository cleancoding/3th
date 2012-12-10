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
		
		if(ym != 0) { //���ο��� �޷� Ŭ���Ͽ� ������ ���
			String nowYm = today.format(cal.getTime());
			ServiceContext.setAttribute("mainSelectYm", ym);
			ServiceContext.setAttribute("mainSelectBusNo", busNo);

			if(ym < Integer.parseInt(nowYm)) {
				//���� �����ʹ� ��û������ ������� ��������
				calData = bookBusActiveStateViewBO.getApplyBeforeCalInfo(ym, busNo);
			} else {
				//���� ���ĵ����ʹ� ��û���ɿ��� Ȯ���Ͽ� ������ �������� (�޷µ�����)
				calData = bookBusActiveStateViewBO.getApplyCalInfo(ym, busNo);
			}
			//���� Ȱ����Ȳ ����
			String monthContent = bookBusActiveStateViewBO.getMonthContent(ym+"", ACTIVE_PHRS_CD);
			ServiceContext.setAttribute("monthContent", monthContent);
			ServiceContext.setAttribute("calData", calData);
		} else {	//Ȱ����Ȳ �޴��� ù ���ٽ�
			String nowYm = today.format(cal.getTime());
			//�޷¿� ǥ���� ������
			calData = bookBusActiveStateViewBO.getApplyCalInfo(Integer.parseInt(nowYm), "1");

			//���� Ȱ����Ȳ ����
			String monthContent = bookBusActiveStateViewBO.getMonthContent(nowYm, ACTIVE_PHRS_CD);
			ServiceContext.setAttribute("calData", calData);
			ServiceContext.setAttribute("monthContent", monthContent);
		}
		return SUCCESS;
	}
	
	/**Ȱ����Ȳ �޷ºκ� ������
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
			//���� �����ʹ� ��û������ ������� ��������
			calData = bookBusActiveStateViewBO.getApplyBeforeCalInfo(ym, busNo);
			ServiceContext.setAttribute("calData", calData);
			return "before";			
		} else {
			//���� ���ĵ����ʹ� ��û���ɿ��� Ȯ���Ͽ� ������ �������� (�޷µ�����)
			calData = bookBusActiveStateViewBO.getApplyCalInfo(ym, busNo);
			ServiceContext.setAttribute("calData", calData);
			return "after";
		}
	}
	
	/**Ȱ����Ȳ ��������
	 * @return
	 * @throws Exception 
	 */
	public String monthPhrsAjax() throws Exception {
		String ym = params.getString("ym");
		
		//���� Ȱ����Ȳ ����
		String monthContent = bookBusActiveStateViewBO.getMonthContent(ym, ACTIVE_PHRS_CD);
		ServiceContext.setAttribute("monthContent", monthContent);
		
		return SUCCESS;
	}
	
	/**Ȱ����Ȳ ����Ʈ
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
	
	
	/**Ȱ����Ȳ 1��Ȯ�� ���º���
	 * @return
	 */
	public String changeState() {
		String aplyNo = params.getString("aplyNo");
		String ymd = params.getString("ymd");
		
		bookBusActiveStateViewBO.setAplyStatCd(APLY_COMPLETE_CD, aplyNo, ymd);
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat today = new SimpleDateFormat("yyyyMM");
		String ym = today.format(cal.getTime());
		
		//�޷¿� ǥ���� ������
		LinkedHashMap<String, ArrayList<ExtendedMap>> calData = bookBusActiveStateViewBO.getApplyCalInfo(Integer.parseInt(ym), "1");

		//���� Ȱ����Ȳ ����
		ServiceContext.setAttribute("calData", calData);
		
		return ActionForward.alertAndBack("������ ��� �Ϸ�Ǿ����ϴ�.");
	}
	
	/**Ȱ����Ȳ �ϴ� �����κ�
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String mapAjax() throws UnsupportedEncodingException {
		String sido = params.getString("sido");
		String sidoKr = "";
		String sidoEn = "";
		List<String> sidoMap = new ArrayList<String>();
		
		//�õ� ����
		if("kangwondo".equals(sido)) {
			sidoKr = "������";
			sidoMap.add("����%");
		} else if("kynggido".equals(sido)) {
			sidoKr = "��⵵";
			sidoMap.add("���%");
			sidoMap.add("��õ%");
			sidoMap.add("����%");
		} else if("chungbuk".equals(sido)) {
			sidoKr = "��û�ϵ�";
			sidoMap.add("���%");
		} else if("chungnam".equals(sido)) {
			sidoKr = "��û����";
			sidoMap.add("�泲%");
			sidoMap.add("����%");
		} else if("kyngbuk".equals(sido)) {
			sidoKr = "���ϵ�";
			sidoMap.add("���%");
		} else if("kyngnam".equals(sido)) {
			sidoKr = "��󳲵�";
			sidoMap.add("�泲%");
			sidoMap.add("�뱸%");
			sidoMap.add("�λ�%");
			sidoMap.add("���%");
		} else if("jeonbuk".equals(sido)) {
			sidoKr = "����ϵ�";
			sidoMap.add("����%");
		} else if("jeonnam".equals(sido)) {
			sidoKr = "���󳲵�";
			sidoMap.add("����%");
			sidoMap.add("����%");
		} else if("jeju".equals(sido)) {
			sidoKr = "���ֵ�";
			sidoMap.add("����%");
		} else if("all".equals(sido)) {
			sidoKr = "��ü";
			String classNm = "btn_ch";
			ServiceContext.setAttribute("classNm", classNm);
		} 
		
		//��¥����Ʈ ����
		int totalRows = bookBusActiveStateViewBO.getVstDataListCount(sidoMap, "ymd");
		pagerInfo.init(PAGER_NAME, totalRows, PAGE_SIZE, INDEX_SIZE);
		
		//���� �湮����Ʈ ����
		int vstRows = bookBusActiveStateViewBO.getVstDataListCount(sidoMap, "vst");
		
		//key : ��¥, value : ��¥����û����Ʈ
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
