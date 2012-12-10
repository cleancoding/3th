/*
 * @(#)ColumnBO.java $version 2011. 2. 24.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.health.column.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naver.health.column.dao.ColumnDAO;
import com.naver.health.column.model.ColumnCategory;
import com.naver.health.column.model.ColumnConstants;
import com.naver.health.column.model.ColumnContent;
import com.nhncorp.lucy.bloc.annotation.BRANCH;
import com.nhncorp.lucy.bloc.annotation.ELEMENT;
import com.nhncorp.lucy.bloc.annotation.Params;
import com.nhncorp.lucy.bloc.annotation.Procedure;
import com.nhncorp.lucy.bloc.annotation.Resource;

/**
 * NAML을 이용해서 구성되는 컬럼류
 */
@Service
@Resource(name = "columnBO", singleton = true)
public class ColumnBO {
	@Autowired
	private ColumnDAO columnDAO;
	
	@Procedure(description = "컬럼류 HOME을 구성하기 위한 prodecure")
	@Params(value = {
		@ELEMENT(name = "topCategoryCode", type = String.class, branch = BRANCH.BASE, description = "최상단카테고리코드(임신,육아)"),
		@ELEMENT(name = "categoryLevel", type = String.class, branch = BRANCH.BASE, description = "가져오려는 카테고리 리스트의 level(depth)")})
	public Map<String, Object> getColumnHomeInfo(Map<String, Object> params) {
		List<ColumnCategory> largeCategoryList = columnDAO.selectColumnCategoryList(MapUtils.getString(params, "topCategoryCode"), ColumnConstants.CATEGORY_LEVEL_LARGE);
		addRepresentContentCodeToList(largeCategoryList);
		
		List<List<ColumnCategory>> middleCategoryList = makeMiddleCategoryList(largeCategoryList);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("largeCategoryList", largeCategoryList);
		resultMap.put("middleCategoryList", middleCategoryList);
		return resultMap;
	}

	@Procedure(description = "컬럼류 END를 구성하기 위한 prodecure")
	@Params(value = {
		@ELEMENT(name = "topCategoryCode", type = String.class, branch = BRANCH.BASE, description = "최상단카테고리코드(임신,육아)(필수값)"),
		@ELEMENT(name = "categoryCode", type = String.class, branch = BRANCH.BASE, description = "선택된카테고리코드(필수값)"),
		@ELEMENT(name = "contentCode", type = String.class, branch = BRANCH.BASE, description = "컨텐츠코드(필수값)")})
	public Map<String, Object> getColumnDetail(Map<String, Object> params) {
		String topCategoryCode = MapUtils.getString(params, "topCategoryCode");
		String categoryCode = MapUtils.getString(params, "categoryCode");
		String contentCode = MapUtils.getString(params, "contentCode");
		
		String largeCategoryCode = getLargeCategoryCode(categoryCode);
		
		List<ColumnCategory> largeCategoryList = columnDAO.selectColumnCategoryList(topCategoryCode, ColumnConstants.CATEGORY_LEVEL_LARGE);
		addRepresentContentCodeToList(largeCategoryList);
		
		List<ColumnCategory> middleCategoryList = columnDAO.selectColumnCategoryList(largeCategoryCode, ColumnConstants.CATEGORY_LEVEL_MIDDLE);
		addRepresentContentCodeToList(middleCategoryList);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("content", columnDAO.selectColumnContentDetailInfo(contentCode)); // content detail
		resultMap.put("largeCategoryList", largeCategoryList); // 대분류 카테고리 리스트
		resultMap.put("middleCategoryList", middleCategoryList); // 대분류 카테고리 리스트
		resultMap.put("contentTitleList", getContentTitleList(middleCategoryList));
		return resultMap;
	}
	
	/**
	 * 넘어온 카테고리 정보를 기준으로 대분류 카테고리를 가져옵니다. <br/>
	 * 2가지 case 존재 (대분류 카테고리가 넘어오거나, 중분류 카테고리가 넘어오거나)
	 * 
	 * @param categoryCode
	 * @return
	 */
	private String getLargeCategoryCode(String categoryCode) {
		ColumnCategory category = columnDAO.selectColumnCategory(categoryCode);
		
		if (StringUtils.equals(category.getCategoryLevel(), ColumnConstants.CATEGORY_LEVEL_LARGE)) {
			return category.getCategoryCode();
		} else {
			return category.getUpperCategoryCode();
		}
	}
	
	/**
	 * categoryList에서 각각의 Category에 대표 컨텐츠 코드를 추가합니다.
	 * @param categoryList
	 */
	private void addRepresentContentCodeToList(List<ColumnCategory> categoryList) {
		for (ColumnCategory category : categoryList) {
			category.setRepresentContentCode(getRepresentContentCode(category.getCategoryCode()));
		}
	}
	
	/**
	 * 카테고리 정보를 통해서 대표 contentCode를 가져옵니다. <br/><br/>
	 * 
	 * 대분류를 선택했을 때는 대표 중분류를 가져오고 <br/>
	 * 해당 대표 중분류의 대표 컨텐츠 코드를 가져옵니다. <br/><br/>
	 * 
	 * 중분류를 선택했을 경우는 해당 중분류의 대표 컨텐츠 코드를 가져옵니다. <br/>
	 * 
	 * point! contentCode를 알려면 중분류의 카테고리를 알아야합니다.
	 * 
	 * @param categoryCode
	 * @return
	 */
	private String getRepresentContentCode(String categoryCode) {
		ColumnCategory category = columnDAO.selectColumnCategory(categoryCode);
		
		if (StringUtils.equals(category.getCategoryLevel(), ColumnConstants.CATEGORY_LEVEL_LARGE)) {
			category = columnDAO.selectRepresentColumnCategory(categoryCode, ColumnConstants.CATEGORY_LEVEL_MIDDLE);
		}
		
		return columnDAO.selectRepresentContentCode(category.getCategoryCode());
	}

	/**
	 * 대분류 카테고리(1차원배열)을 기준으로 중분류 카테고리(2차원배열)을 생성합니다.
	 * 
	 * @param largeCategoryList[]
	 * @return middleCategoryList[][]
	 */
	private List<List<ColumnCategory>> makeMiddleCategoryList(List<ColumnCategory> largeCategoryList) {
		List<List<ColumnCategory>> middleCategoryList = new ArrayList<List<ColumnCategory>>();
		
		for (ColumnCategory category : largeCategoryList) {
			List<ColumnCategory> middleCategory = columnDAO.selectColumnCategoryList(category.getCategoryCode(), ColumnConstants.CATEGORY_LEVEL_MIDDLE);
			addRepresentContentCodeToList(middleCategory);
			
			middleCategoryList.add(middleCategory);
		}
		
		return middleCategoryList;
	}

	/**
	 * middleCategory를 통해서 해당 middleCategory에 속한 컨텐츠들의 정보를 <br/>
	 * 이중 배열로 구성하여 return 합니다. <br/><br/>
	 * 
	 * middleCategoryList (ColumnCategory[] List)<br/>
	 * 1100 | 1200 (List) <br/><br/>
	 * 
	 * contentTitleList (ColumnContent[][] List)<br/>
	 * 0 ----- 1101 | 1102 | 1103 (List) <br/>
	 * 1 ----- 1201 | 1202 | 1203 (List) <br/>
	 * (List) <br/>
	 * @param middleCategoryList
	 * @return middleCategory에 속한 컨텐츠들의 이중배열
	 */
	private List<List<ColumnContent>> getContentTitleList(List<ColumnCategory> middleCategoryList) {
		List<List<ColumnContent>> contentTitleList = new ArrayList<List<ColumnContent>>();
		
		for (ColumnCategory category : middleCategoryList) {
			contentTitleList.add(columnDAO.selectContentInfoList(category.getCategoryCode()));
		}
		
		return contentTitleList;
	}
}
