/*
 * @(#)TeraBattleEventAction.java.java $version 2012. 8. 8
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.hangame.tera.action.event;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibatis.common.jdbc.exception.NestedSQLException;

import com.hangame.common.security.user.CommonUser;
import com.hangame.tera.action.TeraBaseAction;
import com.hangame.tera.bo.event.TeraEventNewBO;
import com.hangame.tera.constants.TeraBattleCodeConstants;
import com.hangame.tera.constants.TeraErrorCodeConstants;
import com.hangame.tera.model.event.TeraBattleEventModel;
import com.hangame.tera.util.IDPUtil;

import com.nhncorp.lucy.common.data.DataHandlerFactory;
import com.nhncorp.lucy.db.dao.SqlExecutionException;

/**
 * 전장이벤트
 * 상시로 진행되어 차수로 구분하여 처리
 *
 */
public class TeraBattleEventAction extends TeraBaseAction {
	private static final long serialVersionUID = -7320674339866798964L;

	private static final Log LOG = LogFactory.getLog(TeraBattleEventAction.class);
	
	private TeraEventNewBO teraEventNewBO;
	private int poll;
	private int myPoll;
	private String eventNo;

	public int getPoll() {
		return poll;
	}

	public void setPoll(int poll) {
		this.poll = poll;
	}	

	public int getMyPoll() {
		return myPoll;
	}

	public void setMyPoll(int myPoll) {
		this.myPoll = myPoll;
	}
	
	public String getEventNo() {
		return eventNo;
	}

	public void setEventNo(String eventNo) {
		this.eventNo = eventNo;
	}

	public void setTeraEventNewBO(TeraEventNewBO teraEventNewBO) {
		this.teraEventNewBO = teraEventNewBO;
	}	

	/**
	 * 전장이벤트 1차. 투표현황조회.
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String execute() throws Exception {
		String eventCode = TeraBattleCodeConstants.POLL1.getEventCode();
		
		if ("2".equals(getEventNo())) {
			eventCode = TeraBattleCodeConstants.POLL2.getEventCode();
		} else if ("2_bj".equals(getEventNo())) {
			eventCode = TeraBattleCodeConstants.POLL2_BJ.getEventCode();
		}
		
		return getPollList(eventCode);
	}
	
	/**
	 * 투표현황 조회.
	 *
	 * @param eventCd 이벤트코드
	 * @return 
	 */
	protected String getPollList(String eventCd) {
		CommonUser user = getUser();
		
		if (user != null && user.isLoggedin()) {
			//내가 투표한 길드
			setMyPoll(teraEventNewBO.selectTeraMyPoll(user.getMemberno(), eventCd));
			
			if (getMyPoll() > 0) {	//로그인하고 투표한 사람만 투표현황을 보여줌
				//테라 투표 통계
				List<TeraBattleEventModel> pollList = teraEventNewBO.selectTeraPollList(eventCd);
				
				for (int i = 0, k = pollList.size(); i < k; i++) {
					TeraBattleEventModel teraBattleEventModel = pollList.get(i);
					
					int poll = teraBattleEventModel.getPoll();
					
					setAttribute("teraPollCount" + poll, teraBattleEventModel.getCount());
					setAttribute("teraPollPercent" + poll, teraBattleEventModel.getPercent());
				}
			}
		}

		return SUCCESS;
	}
	
	/**
	 * 전장이벤트 투표처리(1인 1회가능).
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	public String ajaxPoll() throws Exception {
		String eventCode = TeraBattleCodeConstants.POLL1.getEventCode();
		String eventStartTime = null;	//이벤트 시작기간이 필요한 경우만 사용
		String eventEndTime = TeraBattleCodeConstants.POLL1.getEventEndTime();
		int maximumPoll = TeraBattleCodeConstants.POLL1.getMaximumPoll();
		
		if ("2".equals(getEventNo())) {
			eventCode = TeraBattleCodeConstants.POLL2.getEventCode();
			eventEndTime = TeraBattleCodeConstants.POLL2.getEventEndTime();
			maximumPoll = TeraBattleCodeConstants.POLL2.getMaximumPoll();
		} else if ("2_bj".equals(getEventNo())) {
			eventCode = TeraBattleCodeConstants.POLL2_BJ.getEventCode();
			eventStartTime = DataHandlerFactory.getDataHandler().get("url/" + eventCode.toLowerCase() + "_start_time");
			eventEndTime = TeraBattleCodeConstants.POLL2_BJ.getEventEndTime();
			maximumPoll = TeraBattleCodeConstants.POLL2_BJ.getMaximumPoll();
		}
		
		return addPoll(eventCode, eventStartTime, eventEndTime, maximumPoll);
	}
	
	/**
	 * 투표 저장.
	 *
	 * @param eventCode 이벤트코드
	 * @param eventEndTime 이벤트종료일시
	 * @param maximumPoll 설문갯수
	 * @return the string
	 * @throws Exception the exception
	 */
	protected String addPoll(String eventCode, String eventStartTime, String eventEndTime, int maximumPoll) throws Exception {
		
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			setAttribute("ERROR", TeraErrorCodeConstants.NOT_POST);
			return "ajaxPoll";		
		}
		
		CommonUser user = getUser();
		
		String validResult = teraEventNewBO.pollValidCheck(user, eventCode, eventStartTime, eventEndTime, getPoll(), maximumPoll);
		
		if ("success".equals(validResult)) {
			String playnetId = null;
			
			if (IDPUtil.isPlaynet()) {	//플넷의 경우 viewId를 추가로 저장
				playnetId = user.getViewid();
			}
			
			try {
				//투표처리
				teraEventNewBO.insertTeraPoll(user.getMemberid(), user.getMemberno(), playnetId, getPoll(), eventCode, null, null, null);
			} catch (SqlExecutionException e) {
				NestedSQLException se = (NestedSQLException)e.getCause();

				if (se.getErrorCode() == NumberUtils.toInt(TeraErrorCodeConstants.MSSQL_PK_ERROR)) {	//키중복
					setAttribute("ERROR", TeraErrorCodeConstants.DUPLICATION);
					return "ajaxPoll";	
				} else {
					LOG.error("전장이벤트 투표처리 오류", e);
					setAttribute("ERROR", TeraErrorCodeConstants.DB_ERROR);
					return "ajaxPoll";
				}
			}
		} else {
			setAttribute("ERROR", validResult);
		}
		
		return "ajaxPoll";
	}
}