/*
 * @(#)QueryParameter.java $version 2012. 7. 31.
 * Copyright 2012 NHN Corp. All rights Reserved.
 * 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.blog.functionalservice.search;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import clover.org.jfree.util.Log;

import com.naver.blog.foundation.DefaultPage;
import com.naver.blog.foundation.Page;
import com.naver.blog.foundation.util.DateUtil;
import com.naver.blog.mylog.bloguser.RelationType;
import com.nhn.lucy.model.BaseObject;

/**
 * 검색에 질의할 파라미터들
 * 
 * @since 2012. 7. 31.
 */
@SuppressWarnings("serial")
public class QueryParameter extends BaseObject {
	private static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
	private final String query;
	private final Integer blogNo;

	private final Page page;
	private final SortType sortType;

	private final RelationType relationType;
	private final TermType termType;

	private final Date startDate;
	private final Date endDate;

	private final HighLightType highLightType;
	private final Integer contentLength;
	private final Integer titleLength;

	private final PresentationCodeType presentationCodeType;
	private final SearchTargetAsCollectionType searchTargetAsCollectionType;
	private final SearchMehtodType searchMehtodType;
	private final ResultProcessingType resultProcessingType;
	
	private final Boolean isAdult;
	private final Boolean isForbidden;

	public String getQuery() {
		return query;
	}

	public Integer getBlogNo() {
		return blogNo;
	}

	public RelationType getRelationType() {
		return relationType;
	}

	public Page getPage() {
		return page;
	}

	public SortType getSortType() {
		return sortType;
	}

	public TermType getTermType() {
		return termType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public HighLightType getHighLightType() {
		return highLightType;
	}

	public Integer getContentLength() {
		return contentLength;
	}

	public PresentationCodeType getPresentationCodeType() {
		return presentationCodeType;
	}

	public SearchTargetAsCollectionType getSearchTargetAsCollectionType() {
		return searchTargetAsCollectionType;
	}

	public SearchMehtodType getSearchMehtodType() {
		return searchMehtodType;
	}

	public ResultProcessingType getResultProcessingType() {
		return resultProcessingType;
	}

	public Integer getTitleLength() {
		return titleLength;
	}
	
	public Boolean getIsAdult() {
		return isAdult;
	}
	
	public Boolean getIsForbidden() {
		return isForbidden;
	}

	/**
	 * builder
	 * 
	 * @since 2012. 8. 20.
	 */
	public static class Builder {
		private String query;

		private Integer blogNo;
		private RelationType relationType;
		private Page page;

		private SortType sortType;

		private TermType termType;
		private Date startDate;
		private Date endDate;

		private HighLightType highLightType;
		private Integer contentLength;
		private Integer titleLength;

		private PresentationCodeType presentationCodeType;
		private SearchTargetAsCollectionType searchTargetAsCollectionType = SearchTargetAsCollectionType.BLOG;
		private SearchMehtodType searchMehtodType = SearchMehtodType.POST;
		private ResultProcessingType resultProcessingType;
		
		private Boolean isAdult;
		private Boolean isForbidden;
		
		/**
		 * 필수값들
		 * 
		 * @param query
		 * @param presentationCodeType
		 * @param searchTargetAsCollectionType
		 * @param searchMehtodType
		 * @param resultProcessingType
		 * @param sortType
		 * @param page
		 * @param highLightType
		 */
		public Builder(String query, PresentationCodeType presentationCodeType,
			SearchTargetAsCollectionType searchTargetAsCollectionType, SearchMehtodType searchMehtodType,
			ResultProcessingType resultProcessingType, SortType sortType, Page page, HighLightType highLightType) {
			this.query = query;
			this.presentationCodeType = presentationCodeType;
			this.searchTargetAsCollectionType = searchTargetAsCollectionType;
			this.searchMehtodType = searchMehtodType;
			this.resultProcessingType = resultProcessingType;
			this.sortType = sortType;
			this.page = page;
			this.highLightType = highLightType;
		}

		public Builder blogNo(Integer blogNo) {
			this.blogNo = blogNo;
			return this;
		}

		public Builder relationType(RelationType relationType) {
			this.relationType = relationType;
			return this;
		}

		public Builder termType(TermType termType) {
			this.termType = termType;
			return this;
		}

		public Builder startDate(Date startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder endDate(Date endDate) {
			this.endDate = endDate;
			return this;
		}

		public Builder contentLength(Integer contentLength) {
			this.contentLength = contentLength;
			return this;
		}

		public Builder titleLength(Integer titleLength) {
			this.titleLength = titleLength;
			return this;
		}
		
		public Builder isAdult(Boolean isAdult) {
			this.isAdult = isAdult;
			return this;
		}
		
		public Builder isForbidden(Boolean isForbidden) {
			this.isForbidden = isForbidden;
			return this;
		}

		public QueryParameter build() {
			return new QueryParameter(this);
		}
	}

	private QueryParameter(Builder builder) {
		query = builder.query;
		blogNo = builder.blogNo;
		relationType = builder.relationType;
		page = builder.page;
		sortType = builder.sortType;
		termType = builder.termType;
		startDate = builder.startDate;
		endDate = builder.endDate;
		highLightType = builder.highLightType;
		contentLength = builder.contentLength;
		titleLength = builder.titleLength;

		presentationCodeType = builder.presentationCodeType;
		searchTargetAsCollectionType = builder.searchTargetAsCollectionType;
		searchMehtodType = builder.searchMehtodType;
		resultProcessingType = builder.resultProcessingType;
		
		isAdult= builder.isAdult;
		isForbidden = builder.isForbidden;
	}

	public Map<String, String> getParamters() {
		Map<String, String> parameters = new HashMap<String, String>();
		// 고정값들 
		safeAddToMapIfKeyOrValueIsBlank(parameters, "version", "1.0.0");
		safeAddToMapIfKeyOrValueIsBlank(parameters, "q_enc", "utf-8");
		safeAddToMapIfKeyOrValueIsBlank(parameters, "r_enc", "utf-8");
		safeAddToMapIfKeyOrValueIsBlank(parameters, "r_format", "xml");
		safeAddToMapIfKeyOrValueIsBlank(parameters, "ic", "post");

		safeAddToMapIfKeyOrValueIsBlank(parameters, "gk_adt", isAdult == null ? "0" : BooleanUtils.isTrue(isAdult) ? "0" : "1");
		safeAddToMapIfKeyOrValueIsBlank(parameters, "gk_fbd", isForbidden == null ? "0" : BooleanUtils.isTrue(isForbidden) ? "0" : "1");
		safeAddToMapIfKeyOrValueIsBlank(parameters, "gk_qvt", "0");
		
		// 검색 query
		safeAddToMapIfKeyOrValueIsBlank(parameters, "q", query);

		// 약간의 조작이 필요한 값들 
		safeAddToMapIfKeyOrValueIsBlank(parameters, "st_blogno", blogNo == null ? null : "exist:" + blogNo);
		
		safeAddToMapIfKeyOrValueIsBlank(parameters, "display",
			(page != null && page.getCountPerPage() != null) ? String.valueOf(page.getCountPerPage()) : null);

		safeAddToMapIfKeyOrValueIsBlank(parameters, "start",
			(page == null || page.getRowCountToObatinBasicRow() == null) ? 1 : page.getRowCountToObatinBasicRow() + 1);
		safeAddToMapIfKeyOrValueIsBlank(parameters, "rp", makeResultProcessingType(contentLength));
		safeAddToMapIfKeyOrValueIsBlank(parameters, "r_psglen", makeLength(titleLength, contentLength));
		safeAddToMapIfKeyOrValueIsBlank(parameters, "st_adddate",
			makeAddDate(calcStartDate(termType, startDate), calcEndDate(termType, endDate)));

		// {@link ParameterType} 으로 분류할 수 있는 값들
		safeParameterTypeAddToMapIfKeyOrValueIsBlank(parameters, "pr", presentationCodeType);
		safeParameterTypeAddToMapIfKeyOrValueIsBlank(parameters, "st", searchTargetAsCollectionType);
		safeParameterTypeAddToMapIfKeyOrValueIsBlank(parameters, "sm", searchMehtodType);
		safeParameterTypeAddToMapIfKeyOrValueIsBlank(parameters, "so", sortType);
		safeParameterTypeAddToMapIfKeyOrValueIsBlank(parameters, "hl", highLightType);
		safeParameterTypeAddToMapIfKeyOrValueIsBlank(parameters, "st_openmode", QueryOpenType.find(relationType));
		return parameters;
	}

	private String makeAddDate(String startDate, String endDate) {
		if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
			return null;
		}
		return "range:" + startDate + ":" + endDate;
	}

	private String makeResultProcessingType(Integer contentLength) {
		if (resultProcessingType == null || ResultProcessingType.NONE == resultProcessingType || contentLength == null) {
			return ResultProcessingType.NONE.getType();
		} else if (ResultProcessingType.REMOVE_DUPLICATION == resultProcessingType) {
			return ResultProcessingType.REMOVE_DUPLICATION.getType() + "." + contentLength;
		}
		return ResultProcessingType.NONE.getType();
	}

	private String makeLength(Integer titleLength, Integer contentLength) {
		if (titleLength == null || contentLength == null) {
			return null;
		}

		return "title." + titleLength + ":contents." + contentLength;
	}

	private String calcStartDate(TermType termType, Date startDate) {
		if (startDate != null) {
			try {
				Date truncatedStartDate = DateUtils.truncate(startDate, Calendar.DATE);
				return DateUtil.formatDate(truncatedStartDate, YYYYMMDDHHMM);
			} catch (Exception e) {
				Log.warn(e.getMessage(), e);
				return null;
			}
		}

		if (termType == null) {
			return null;
		}

		Calendar cal = Calendar.getInstance();
		switch (termType) {
			case IN_ONE_WEEK:
				cal.add(Calendar.DATE, -7);
				Date aWeekAgo = DateUtils.truncate(new Date(cal.getTimeInMillis()), Calendar.DATE);
				return DateUtil.formatDate(aWeekAgo, YYYYMMDDHHMM);
			case IN_ONE_MONTH:
				cal.add(Calendar.MONTH, -1);
				Date aMonthAgo = DateUtils.truncate(new Date(cal.getTimeInMillis()), Calendar.DATE);
				return DateUtil.formatDate(aMonthAgo, YYYYMMDDHHMM);
			default:
				return null;
		}
	}

	private String calcEndDate(TermType termType, Date endDate) {
		if (endDate != null) {
			try {
				Date ceiledEndDate = DateUtils.ceiling(endDate, Calendar.DATE);
				ceiledEndDate = DateUtils.addMinutes(ceiledEndDate, -1);
				return DateUtil.formatDate(ceiledEndDate, YYYYMMDDHHMM);
			} catch (Exception e) {
				Log.warn(e.getMessage(), e);
				return null;
			}
		}

		if (termType == null) {
			return null;
		}

		Calendar cal = Calendar.getInstance();
		switch (termType) {
			case IN_ONE_WEEK:
			case IN_ONE_MONTH:
				Date todayMidnight = DateUtils.ceiling(new Date(cal.getTimeInMillis()), Calendar.DATE);
				todayMidnight = DateUtils.addMinutes(todayMidnight, -1);
				return DateUtil.formatDate(todayMidnight, YYYYMMDDHHMM);
			default:
				return null;
		}
	}

	private void safeParameterTypeAddToMapIfKeyOrValueIsBlank(Map<String, String> parameters, String key, 
		ParameterType parameterType) {
		if (StringUtils.isBlank(key) || parameterType == null) {
			return; 
		}

		safeAddToMapIfKeyOrValueIsBlank(parameters, key, parameterType.getValue());
	}

	private void safeAddToMapIfKeyOrValueIsBlank(Map<String, String> parameters, String key, Object value) {
		if (StringUtils.isBlank(key) || value == null) {
			return;
		}
		parameters.put(key, String.valueOf(value));
	}
}
