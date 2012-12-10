/*
 * @(#)NcrFoodMaterialDataWriter.java $version 2012. 11. 9.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.sample.batch.ncr.main;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.oscache.util.StringUtil;

import com.naver.sample.batch.ncr.bo.BatchManagementBO;
import com.naver.sample.batch.ncr.bo.NcrFoodMaterialDataBO;
import com.naver.sample.batch.ncr.model.FoodFileReadResultModel;
import com.naver.sample.batch.ncr.model.FoodMtrlModel;
import com.naver.sample.common.handler.ExposablePropertyPlaceholderConfigurer;
import com.naver.sample.common.type.FoodMaterialType;
import com.naver.sample.common.type.FoodMaterialXmlFile;
import com.naver.sample.common.type.ServiceExposureType;

// TODO: Auto-generated Javadoc
/**
 * The Class NcrFoodMaterialDataWriter.
 *
 */
public class NcrFoodMaterialDataWriter implements ItemWriter<Object> {

	/** The Constant LOG. */
	private final Log logger = LogFactory.getLog(NcrFoodMaterialDataWriter.class);

	/** The ncr export file name. */
	private String ncrFoodMaterialFileName = "kitchen_sample.xml";

	public static final String SQLMAP_KEY = "kitchen";

	/** The property configurer. */
	@Autowired
	private ExposablePropertyPlaceholderConfigurer propertyConfigurer;

	@Autowired
	private NcrFoodMaterialDataBO ncrFoodMaterialDataBO;

	@Autowired
	private BatchManagementBO batchManagementBO;

	/**
	 * Write.
	 *
	 * @param data the data
	 * @throws Exception the exception
	 * @see ItemWriter#write(Object)
	 */
	public void write(List<? extends Object> data) throws Exception {
		String strFoodMaterialType = getFoodMaterialType(ncrFoodMaterialFileName);

		try {

			if (data != null && !StringUtil.isEmpty(strFoodMaterialType)) {

				for (int i = 0; i < data.size(); i++) {
					// xml파일 모델객체 셋팅
					FoodFileReadResultModel foodFileReadResultModel = (FoodFileReadResultModel)data.get(i);

					if (foodFileReadResultModel != null) {

						List<FoodMtrlModel> ls = foodFileReadResultModel.getFoodMtrlModel();

						FoodMtrlModel foodMtrlModel = null;

						Iterator<FoodMtrlModel> ite = (Iterator<FoodMtrlModel>)ls.iterator();
						while (ite.hasNext()) {

							foodMtrlModel = (FoodMtrlModel)ite.next();
							String strSvcExpsTp = foodMtrlModel.getKitchenServiceExposeType();
							String foodId = foodMtrlModel.getId();

							// 레시피는 별도 노출여부 설정이 없어 모든 데이터를 입고처리하고,
							// 다른 음식재료는 노출여부가 1,2,3 인 데이터만 입고하도록 되어 있다. 
							
							// 하지만 증분데이터에 대한 처리로직이 추가되면서 기존 데이터들의 서비스노출여부가 
							// 바뀌게 되므로 노출여부에 대한 체크는 별도로 수행하지 않는쪽으로 수정한다.
							ncrFoodMaterialDataBO.insertFoodDB(foodMtrlModel, foodId, strFoodMaterialType);
							
						}
					}
				}
			}

		} catch (Exception e) {
//          로컬 개발시에만 아래 주석 해제		
//			e.printStackTrace();
//			throw e;
		}
	}

	/**
	 * Sets the ncr food material file name.
	 *
	 * @param ncrFoodMaterialFileName the new ncr food material file name
	 */
	public void setNcrFoodMaterialFileName(String ncrFoodMaterialFileName) {
		this.ncrFoodMaterialFileName = ncrFoodMaterialFileName;
	}

	/**
	 * Gets the food material type.
	 *
	 * @param foodMaterialFileName the food material file name
	 * @return the food material type
	 */
	private String getFoodMaterialType(String foodMaterialFileName) {
		String foodMaterialTypeCode = "";

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.RECIPE.getText())) {
			return FoodMaterialType.RECIPE.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.ALCOHOL.getText())) {
			return FoodMaterialType.ALCOHOL.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.ICE.getText())) {
			return FoodMaterialType.ICE.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.BRAED.getText())) {
			return FoodMaterialType.BRAED.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.COOKIE.getText())) {
			return FoodMaterialType.COOKIE.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.DISH.getText())) {
			return FoodMaterialType.DISH.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.DRINK.getText())) {
			return FoodMaterialType.DRINK.getCode();
		}

		if (foodMaterialFileName.equals(FoodMaterialXmlFile.MATERIAL.getText())) {
			return FoodMaterialType.MATERIAL.getCode();
		}

		return foodMaterialTypeCode;

	}
}
