/*
 * @(#)ListAction.java $version 2012. 08. 30.
 *
 * Copyright 2009 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.jr.study.action.sesame;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.naver.jr.common.action.JrBaseAction;
import com.naver.jr.common.util.JrCommonUtil;
import com.naver.jr.study.bo.sesame.SesameBO;
import com.naver.jr.study.model.sesame.SesameContents;
import com.naver.jr.study.model.sesame.SesameStep;
import com.naver.jr.study.model.sesame.StepEnum;
import com.naver.jr.study.util.PageUtil;

import com.nhncorp.lucy.web.helper.PagerInfo;
import com.nhncorp.lucy.web.interceptor.PagerInfoAware;

/**
 * 세서미 단계별 메인
 */
@SuppressWarnings("serial")
public class ListAction extends JrBaseAction implements PagerInfoAware {
	private static final int INDEX_SIZE = 10;
	private static final int PAGE_SIZE = 12;
	private static final String PAGER_TYPE = "sesameList";

	@Autowired
	private SesameBO sesameBO;

	private String stepName;
	private List<SesameContents> imageList;
	private List<List<SesameContents>> vodList = new ArrayList<List<SesameContents>>();

	private int page;
	private int offset;
	private boolean hasNext;
	private PagerInfo pagerInfo;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String execute() throws Exception {
		List<SesameStep> themeList = setThemeList();
		if (CollectionUtils.isEmpty(themeList)) {
			return "noSuchContent";
		}

		setContentList(themeList);

		return SUCCESS;
	}

	/**
	 * 단계별 테마 목록
	 * @return
	 */
	private List<SesameStep> setThemeList() {
		List<SesameStep> themeList = sesameBO.getThemeList(stepName);
		return themeList;
	}

	/**
	 * 테마별 컨텐츠 목록
	 * @param themeList
	 */
	private void setContentList(List<SesameStep> themeList) {
		if (StringUtils.equals(StepEnum.IMAGE.getName(), stepName)) {
			SesameStep sesameStep = themeList.get(0);
			int totalCount = sesameBO.getContentCount(sesameStep);
			setContentPaging(totalCount);

			imageList = sesameBO.getContentList(sesameStep, offset, PAGE_SIZE);
		} else {
			List<SesameContents> contentList = new ArrayList<SesameContents>();
			for (SesameStep step : themeList) {
				contentList = sesameBO.getContentList(step);
				vodList.add(contentList);
			}
		}
	}

	/**
	 * 이미지목록 페이징 처리
	 * @param totalCount
	 */
	private void setContentPaging(int totalCount) {
		pagerInfo.init(PAGER_TYPE, totalCount, PAGE_SIZE, INDEX_SIZE);
		page = pagerInfo.getPage();
		offset = JrCommonUtil.getPageOffset(page, PAGE_SIZE);
		hasNext = PageUtil.hasNext(page, totalCount, PAGE_SIZE);
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public String getStepName() {
		return stepName;
	}

	public List<SesameContents> getImageList() {
		return imageList;
	}

	public List<List<SesameContents>> getVodList() {
		return vodList;
	}

	public int getPage() {
		return page;
	}

	public boolean getHasNext() {
		return hasNext;
	}

	@Override
	public PagerInfo getPagerInfo() {
		return pagerInfo;
	}

	@Override
	public void setPagerInfo(PagerInfo pagerInfo) {
		this.pagerInfo = pagerInfo;
	}
}
