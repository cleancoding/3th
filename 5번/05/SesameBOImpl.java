/*
 * @(#)SesameBOImpl.java $version 2012. 08. 29.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.jr.study.bo.sesame;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.naver.jr.common.bo.video.VideoRemoteBO;
import com.naver.jr.common.bo.vote.VoteBO;
import com.naver.jr.common.cache.CacheApply;
import com.naver.jr.common.model.vote.Vote;
import com.naver.jr.common.util.JrCommonUtil;
import com.naver.jr.study.dao.hbstudy.HbEnglishEventDAO;
import com.naver.jr.study.dao.sesame.SesameCharacterDAO;
import com.naver.jr.study.dao.sesame.SesameDAO;
import com.naver.jr.study.dao.sesame.SesameSeasonVideoDAO;
import com.naver.jr.study.model.hbstudy.HbEnglishEvent;
import com.naver.jr.study.model.sesame.SesameCharacter;
import com.naver.jr.study.model.sesame.SesameContents;
import com.naver.jr.study.model.sesame.SesameSeasonVideo;
import com.naver.jr.study.model.sesame.SesameStep;
import com.naver.jr.study.model.sesame.StepEnum;

/**
 * 
 * @author leesunyoung@nhn.com
 */
@Service("SesameBO")
public class SesameBOImpl implements SesameBO, VoteBO {
	@Autowired
	private SesameDAO sesameDAO;
	@Autowired
	private SesameSeasonVideoDAO sesameSeasonVideoDAO;
	@Autowired
	private SesameCharacterDAO sesameCharacterDAO;
	@Autowired
	private HbEnglishEventDAO hbEnglishEventDAO;
	@Autowired
	private VideoRemoteBO videoRemoteBO;

	@Override
	public List<SesameContents> getMainStepList() {
		List<SesameContents> contentList = new ArrayList<SesameContents>();
		contentList = getMainStepList(contentList, StepEnum.BABY.getName());
		contentList = getMainStepList(contentList, StepEnum.ELMO.getName());
		contentList = getMainStepList(contentList, StepEnum.STORY.getName());

		return contentList;
	}

	private List<SesameContents> getMainStepList(List<SesameContents> contentList, String stepName) {
		List<SesameContents> sesameContentList = sesameDAO.selectMainStepList(stepName);
		Collections.shuffle(sesameContentList);

		if (CollectionUtils.isNotEmpty(sesameContentList)) {
			contentList.add(sesameContentList.get(0));
		}

		return contentList;
	}

	@Override
	public List<SesameStep> getMainThemeList() {
		List<SesameStep> sesameStepList = sesameDAO.selectMainThemeList();
		Collections.shuffle(sesameStepList);

		if (CollectionUtils.isNotEmpty(sesameStepList) && sesameStepList.size() >= 3) {
			sesameStepList = sesameStepList.subList(0, 3);
		}

		return sesameStepList;
	}

	@Override
	public SesameSeasonVideo getSeasonVideo(int seasonVideoNo) throws IOException, SAXException, SQLException {
		SesameSeasonVideo seasonVideo = sesameSeasonVideoDAO.selectSeasonVideo(seasonVideoNo);
		if (seasonVideo == null) {
			return seasonVideo;
		}

		if (StringUtils.equals(JrCommonUtil.getMobileDevice(), "iphone")) {
			String vid = seasonVideo.getVideoId();
			seasonVideo.setVideoId(videoRemoteBO.getVideoId(vid, "270P_480_500_64_1"));
		}

		seasonVideo.setInKey(JrCommonUtil.createInKey(StringUtils.trim(seasonVideo.getVideoId())));
		return seasonVideo;
	}

	@Override
	@CacheApply(expire = CacheApply.SECOND * 10)
	public List<SesameSeasonVideo> getSeasonVideoList() {
		return sesameSeasonVideoDAO.selectSeasonVideoList();
	}

	@Override
	@CacheApply(expire = CacheApply.MINUTE * 10)
	public HbEnglishEvent getEvent(String serviceId) {
		return hbEnglishEventDAO.selectEvent(serviceId);
	}

	@Override
	@CacheApply(expire = CacheApply.SECOND * 10)
	public List<SesameStep> getThemeList(String stepName) {
		return sesameDAO.selectThemeList(stepName);
	}

	@Override
	public List<SesameContents> getContentList(SesameStep sesameStep) {
		int pageSize = getContentCount(sesameStep);
		sesameStep.setOffset(0);
		sesameStep.setPageSize(pageSize);

		return sesameDAO.selectContentList(sesameStep);
	}

	@Override
	public List<SesameContents> getContentList(SesameStep sesameStep, int offset, int pageSize) {
		sesameStep.setOffset(offset);
		sesameStep.setPageSize(pageSize);

		return sesameDAO.selectContentList(sesameStep);
	}

	@Override
	public int getContentCount(SesameStep sesameStep) {
		return sesameDAO.selectContentCount(sesameStep);
	}

	@Override
	public SesameContents getContent(int contentNo) throws IOException, SAXException, SQLException {
		return replaceVideoId(sesameDAO.selectContent(contentNo));
	}

	@Override
	public SesameContents replaceVideoId(SesameContents sesameContents) throws SQLException, IOException, SAXException {
		if (sesameContents == null) {
			return sesameContents;
		}

		if (StringUtils.equals(sesameContents.getContentType(), "vod")) {
			if (StringUtils.equals(JrCommonUtil.getMobileDevice(), "iphone")) {
				String vid = sesameContents.getContent();
				sesameContents.setContent(videoRemoteBO.getVideoId(vid, "270P_480_500_64_1"));
			}

			sesameContents.setInKey(JrCommonUtil.createInKey(StringUtils.trim(sesameContents.getContent())));
		}
		return sesameContents;
	}

	@Override
	public void addViewCount(int contentNo) {
		sesameDAO.updateViewCount(contentNo);
	}

	@Override
	public Vote getVoteCount(int contentNo) {
		return sesameDAO.selectVoteCount(contentNo);
	}

	@Override
	public void voteContents(int contentNo, String voteType) {
		if (StringUtils.equals("good", voteType)) {
			sesameDAO.updateGoodCount(contentNo);
		} else if (StringUtils.equals("bad", voteType)) {
			sesameDAO.updateBadCount(contentNo);
		}
	}

	@Override
	@CacheApply(expire = CacheApply.SECOND * 10)
	public List<SesameCharacter> getCharacterList() {
		return sesameCharacterDAO.selectCharacterList();
	}

	@Override
	public SesameCharacter getCharacter(int charNo) {
		return sesameCharacterDAO.selectCharacter(charNo);
	}

	@Override
	public void voteCharacter(int charNo) {
		sesameCharacterDAO.updateGoodCount(charNo);
	}

	@Override
	public List<SesameContents> getBigViewList(int rownum, int pageSize) {
		List<SesameContents> imageList = new ArrayList<SesameContents>();

		int offset = rownum - 1;
		pageSize += 2; // "+ 2" : 이전 컨텐츠(1) + 다음 컨텐츠(1)

		imageList.add(new SesameContents());
		imageList.addAll(sesameDAO.selectPagedImageList(offset, pageSize));

		return imageList;
	}

	@Override
	public SesameContents getVodByPostKey(int postKey) throws SQLException, IOException, SAXException {
		return replaceVideoId(sesameDAO.selectVodByPostKey(postKey));
	}
}
