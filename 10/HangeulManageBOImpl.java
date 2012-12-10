/*
 * @(#)HangeulManageBOImpl.java $version 2012. 9. 21.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.nhncorp.hangeul.admin.manage.bo;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import com.nhncorp.hangeul.admin.manage.dao.HangeulManageDAO;
import com.nhncorp.hangeul.admin.manage.model.Article;
import com.nhncorp.hangeul.admin.manage.model.ArticleParam;

/**
 */
@Service
public class HangeulManageBOImpl implements HangeulManageBO {
	private static Logger LOG = Logger.getLogger(HangeulManageBOImpl.class);
	@Autowired
	HangeulManageDAO hangeulWorkManageDAO;

	/**
	 * 참여 1, 2 목록 전체 개수 조회
	 * @return
	 * @see com.nhncorp.hangeul.admin.work.bo.HangeulWorkManageBO#getHangeulWorkListCount()
	 */
	@Override
	public int getHangeulWorkListCount(ArticleParam articleParam) {
		if (articleParam == null || StringUtils.isEmpty(articleParam.getHangeulWorkType())) {
			return 0;
		}

		return hangeulWorkManageDAO.selectHangeulWorkListCount(articleParam);
	}

	/**
	 * 참여 1, 2 목록 조회 (페이징 처리)
	 * @param pagerInfo
	 * @see com.nhncorp.hangeul.admin.work.bo.HangeulWorkManageBO#getHangeulWorkList(com.nhncorp.lucy.spring.core.web.helper.PagerInfo)
	 */
	@Override
	public List<Article> getHangeulWorkList(ArticleParam articleParam) {
		if (articleParam == null || StringUtils.isEmpty(articleParam.getHangeulWorkType())) {
			return null;
		}

		return hangeulWorkManageDAO.selectHangeulWorkList(articleParam);
	}

	/**
	 * 참여 1, 2 목록 조회 (전체)
	 * @see com.nhncorp.hangeul.admin.work.bo.HangeulWorkManageBO#getTextListForExcel()
	 */
	@Override
	public List<Article> getHangeulWorkListAll(ArticleParam articleParam) {
		if (articleParam == null || StringUtils.isEmpty(articleParam.getHangeulWorkType())) {
			return null;
		}

		return hangeulWorkManageDAO.selectHangeulWorkListAll(articleParam);
	}

	/**
	 * 참여 1, 2 삭제
	 * @param articleParam
	 * @return
	 * @see com.nhncorp.hangeul.admin.work.bo.HangeulWorkManageBO#deleteArticles(com.nhncorp.hangeul.admin.work.model.ArticleParam)
	 */
	@Override
	@Transactional
	public int deleteArticles(int entySeq, String hangeulWorkType, String imgPath) throws Exception {
		int deleteCount = 0;
		boolean fileDeleteResult = false;
		String fileId = null;

		LOG.debug("********** hangeulWorkType == " + hangeulWorkType + ", imgPath == " + imgPath);
		if (StringUtils.isEmpty(hangeulWorkType) || StringUtils.isEmpty(imgPath)) {
			return deleteCount;
		}

		if (StringUtils.equals(hangeulWorkType, "IMAGE")) {
			fileId = hangeulWorkManageDAO.selectHangeulWork(entySeq, hangeulWorkType);
			LOG.debug("*** fileId == " + fileId);
		}

		// 파일 삭제
		if (StringUtils.equals(hangeulWorkType, "IMAGE")) {
			File file = ResourceUtils.getFile(imgPath + fileId);
			boolean exists = file.exists();
			LOG.debug("********** fileFullPath == " + imgPath + fileId);
			LOG.debug("********** exists == " + exists);
			if (exists == true) {
				fileDeleteResult = file.delete();
				LOG.debug("********** 삭제 후 결과, fileDeleteResult == " + fileDeleteResult);
			}
		} else {
			fileDeleteResult = true;
		}

		// DB 삭제
		if (fileDeleteResult == true) {
			hangeulWorkManageDAO.deleteArticle(entySeq, hangeulWorkType);
			deleteCount++;
		}

		return deleteCount;
	}
}
