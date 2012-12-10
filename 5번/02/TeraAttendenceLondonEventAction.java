/*
 * @(#TeraAttendenceLondonEventAction.java.java $version 2012. 7. 19
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.hangame.tera.action.event;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hangame.common.security.user.CommonUser;
import com.hangame.tera.action.TeraBaseAction;
import com.hangame.tera.bo.event.TeraEventNewBO;

import com.nhncorp.lucy.common.util.DataMap;

/**
 * 런던올림픽 출석체크 이벤트
 *
 */
public class TeraAttendenceLondonEventAction extends TeraBaseAction {
	private static final long serialVersionUID = -4569763004188541999L;
	private static final Log LOG = LogFactory.getLog(TeraAttendenceLondonEventAction.class);
	private TeraEventNewBO teraEventNewBO;

	public void setTeraEventNewBO(TeraEventNewBO teraEventNewBO) {
		this.teraEventNewBO = teraEventNewBO;
	}

	/**
	 * 출석현황조회.
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String execute() throws Exception {

		if (!getUser().isLoggedin()) {
			setAttribute("MACBOOK", 0);
			setAttribute("GRAPHICCARD", 0);
			setAttribute("MUNSANG", 0);
			setAttribute("HANCOIN", 0);
			return SUCCESS;
		}
		
		CommonUser currentUser = (CommonUser)getUser();
		
		String memberId = currentUser.getMemberid();
		String memberNo = currentUser.getMemberno();

		try {
			List<DataMap> listDays = teraEventNewBO.selectUserAttendenceDays(memberId);
			
			int allPoint = 0;
			int usePoint = 0;
			try {
				usePoint = teraEventNewBO.selectUserAttendenceUsePoint(memberId);
			} catch (Exception e) {
				LOG.debug("런던올림픽 이벤트 포인트조회 오류", e);
			}
			
			LinkedHashMap<Integer, Integer> mapDays = new LinkedHashMap<Integer, Integer>();
			
			allPoint = setMapDays(listDays, mapDays);
			
			setEnterInfo(memberId);
			
			Calendar todayCal = Calendar.getInstance();

			int todayD = todayCal.get(Calendar.DATE);
			
			setAttribute("today", todayD);
			setAttribute("attendenceDays", mapDays);
			setAttribute("allPoint", allPoint);
			setAttribute("usePoint", usePoint);
			setAttribute("availablePoint", ((allPoint - usePoint) < 0 ? 0 : (allPoint - usePoint)));
		} catch (Exception e) {
			LOG.error("출석체크 list 조회 실패 memberId : " + memberId + " memberNo : " + memberNo, e);
		}
		
		return SUCCESS;
	}
	
	/**
	 * 출석현황 셋팅 및 출석포인트 계산
	 *
	 * @param listDays 유저의 출석현황
	 * @param mapDays 이벤트기간
	 * @return 출석포인트
	 */
	private int setMapDays(List<DataMap> listDays, LinkedHashMap<Integer, Integer> mapDays) {
		
		int allPoint = 0;
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 6, 25);	//이벤트시작일 지정
		
		for (int i = 0; i < 22; i++) {	//이벤트진행기간 22일
			cal.add(Calendar.DATE, 1);
			
			int day = cal.get(Calendar.DATE);
			boolean isEmpty = true; 
			
			for (int k = 0, size = listDays.size(); k < size; k++) {
				
				DataMap dmap = listDays.get(k);
				
				String addInfo = dmap.getString("add_info", "");
				
				if (NumberUtils.toInt(addInfo.substring(6, 8)) == day) {
					
					int point = dmap.getInt("point", 0);
					allPoint += point;
					
					mapDays.put(day, point);
					listDays.remove(k);
					isEmpty = false;
					break;
				}
			}
			
			if (isEmpty) {
				mapDays.put(day, 0);
			}
		}
		
		return allPoint;
	}
	
	/**
	 * 응모현황 셋팅
	 *
	 * @param memberId 아이디
	 */
	private void setEnterInfo(String memberId) {
		List<DataMap> enterList = teraEventNewBO.selectUserEnterCnt(memberId);
		
		int macbook = 0;
		int graphicCard = 0;
		int munSang = 0;
		int hanCoin = 0;
		if (enterList != null && enterList.size() > 0) {	//나의 응모횟수 조회
			for (int i = 0, size = enterList.size(); i < size; i++) {
				DataMap data = enterList.get(i);
				if ("MACBOOK".equals(data.getString("prod_id"))) {
					macbook = data.getInt("enter_cnt", 0);
				} else if ("GRAPHICCARD".equals(data.getString("prod_id"))) {
					graphicCard = data.getInt("enter_cnt", 0);
				} else if ("MUNSANG".equals(data.getString("prod_id"))) {
					munSang = data.getInt("enter_cnt", 0);
				} else if ("HANCOIN".equals(data.getString("prod_id"))) {
					hanCoin = data.getInt("enter_cnt", 0);
				} 
			}
		}
		setAttribute("MACBOOK", macbook);
		setAttribute("GRAPHICCARD", graphicCard);
		setAttribute("MUNSANG", munSang);
		setAttribute("HANCOIN", hanCoin);
	}
	
	/**
	 * 
	 * 출석체크(1일 1회)
	 * 
	 * @return
	 * @throws Exception
	 */
	/*
	public String attendenceCheck() throws Exception {
		
		if (!getUser().isLoggedin()) {
			return SUCCESS;
		}
		
		CommonUser currentUser = (CommonUser)getUser();
		
		String memberId = currentUser.getMemberid();
		String memberNo = currentUser.getMemberno();
		
		
		try {
			
			int point = 1;
			
			Calendar cal = Calendar.getInstance();
			
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);	//1-일   2-월   3-화   4-수   5-목   6-금   7-토
			
			if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {	//주말은 2포인트
				point = 2;
			}
			
			teraEventNewBO.insertUserAttendenceDay(memberId, memberNo, point);

		} catch (Exception e) {
			LOG.error("이벤트(런던올림픽이벤트) 출석체크 실패 : ", e);
		} 
		
		setAttribute("MSG", "오늘 출석이 되었습니다.");
		setAttribute("REDIRECT_URL", "/event/london.nhn");
		
		return "redirect";
	}
	*/
	
	/**
	 * 
	 * 이벤트 응모
	 * 
	 * @return
	 * @throws Exception
	 */
	/*
	public String enter() throws Exception {
		
		if (!getUser().isLoggedin() || !"POST".equalsIgnoreCase(request.getMethod())) {
			return SUCCESS;
		}
		
		CommonUser currentUser = (CommonUser)getUser();
		
		String memberId = currentUser.getMemberid();
		String memberNo = currentUser.getMemberno();
		
		int type = params.getInt("type", 0);
		
		if (type == 0 || type > 4) {
			return "redirect";
		}
		
		int point = 5;
		String prodId = "HANCOIN";
		String prodName = "한코인 5만원";
		
		if (type == 1) {
			point = 20;
			prodId = "MACBOOK";
			prodName = "맥북에어 MacBook Air";
		} else if (type == 2) {
			point = 15;
			prodId = "GRAPHICCARD";
			prodName = "최신형 그래픽카드";
		} else if (type == 3) {
			point = 10;
			prodId = "MUNSANG";
			prodName = "문화상품권 10만원";
		}
		
		int allPoint = 0;		//총 출첵포인트
		int usePoint = 0;		//총 사용포인트
		int beforePoint = 0;	//총 응모가능포인트
		
		TransactionManager tm = new TransactionManager();
		
		try {
			tm.putItem(new SqlMapTransactionItem("tera_event"));
			tm.start();
			
			try {
				allPoint = teraEventNewBO.selectUserAttendenceAllPoint(memberId);
				usePoint = teraEventNewBO.selectUserAttendenceUsePoint(memberId);
			} catch (Exception e) {
				
			}
		
			beforePoint = (allPoint - usePoint);	//현재까지의 출첵포인트와 사용한 포인트를 뺀 현재 응모가능포인트		
			
			if (beforePoint >= point) {
				teraEventNewBO.insertEnterHistory(memberId, memberNo, prodId, point, beforePoint, "", "");
			} else {
				setAttribute("MSG", "응모가능포인트가 부족합니다.");
				setAttribute("REDIRECT_URL", "/event/london.nhn");
				
				return "redirect";
			}
			
			tm.commit();
			
		} catch (Exception e) {
			LOG.error("출석이벤트 응모실패", e);
			tm.rollback();
		} finally {
			tm.end();
		}	
		
		setAttribute("MSG", prodName + " 상품에 응모되었습니다.");
		setAttribute("REDIRECT_URL", "/event/london.nhn");
		
		return "redirect";
	}
	*/
}
