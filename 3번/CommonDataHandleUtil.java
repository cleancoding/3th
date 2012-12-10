/*
 * @(#)CommonDataHandleUtil.java $version 2011. 4. 11.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.nbp.nmp.benefit.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;

import com.nbp.nmp.category.CategoryVO;

/**
 * 공통으로 쓰일만한 기능들을 모아놓자
 */
public class CommonDataHandleUtil {
	/**
	 * categoryBig, categoryMiddle, categorySmall, categoryDetail의 key로
	 * 카테고리 이름을 구분해넣은 Map을 리턴한다.
	 * @param categoryVO
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> getCategoryNameForViewMap(CategoryVO categoryVO) {
		Map<String, Object> targetMap = new HashMap<String, Object>();
		String[] fullCategoryName = categoryVO.getWholeCategoryName().split(">");

		if (categoryVO.getLevel() > 0) {
			targetMap.put("categoryBig", fullCategoryName[0]);
		}
		if (categoryVO.getLevel() > 1) {
			targetMap.put("categoryMiddle", fullCategoryName[1]);
		}
		if (categoryVO.getLevel() > 2) {
			targetMap.put("categorySmall", fullCategoryName[2]);
		}
		if (categoryVO.getLevel() > 3) {
			targetMap.put("categoryDetail", fullCategoryName[3]);
		}

		return targetMap;
	}

	/**
	 * 문자열날짜를 원하는 형태로 포멧팅하여 반환
	 * @param date
	 * @param fromFormatString
	 * @param toFormatString
	 * @return
	 */
	public static String stringDateFormatter(String date, String fromFormatString, String toFormatString) {
		String[] fromParttern = {fromFormatString};
		FastDateFormat toFormatter = FastDateFormat.getInstance(toFormatString);

		Date fromDate = null;
		try {
			fromDate = DateUtils.parseDate(date, fromParttern);
		} catch (Exception e) {
			return "";
		}
		return toFormatter.format(fromDate);
	}

	/**
	 * 날짜를 원하는 형태의 포매팅 문자열로 반환
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateFormatter(Date date, String format) {
		if (date == null) {
			return "";
		}
		FastDateFormat toFormatter = FastDateFormat.getInstance(format);
		return toFormatter.format(date);
	}

	/**
	 * 문자열을 date형으로 변환하여 리턴 변환불가능하면 널리턴
	 * 디폴트 데이트 포멧은 "yyyyMMdd"
	 * @param dateString
	 * @param dateFormat
	 * @return
	 */
	public static Date stringToDate(String dateString, String dateFormat) {
		if (dateFormat == null) {
			dateFormat = "yyyyMMdd";
		}
		String[] parttern = {dateFormat};
		
		try {
			return DateUtils.parseDate(dateString, parttern);
		} catch (Exception e) {
			return null;
		}
	}
}
