/*
 * @(#)NcrFoodMaterialDataWriter.java $version 2012. 11. 9.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.kitchen.batch.ncr.main;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.oscache.util.StringUtil;

import com.naver.kitchen.batch.ncr.bo.BatchManagementBO;
import com.naver.kitchen.batch.ncr.bo.NcrFoodMaterialDataBO;
import com.naver.kitchen.batch.ncr.model.FoodFileReadResultModel;
import com.naver.kitchen.batch.ncr.model.FoodMtrlModel;
import com.naver.kitchen.common.handler.ExposablePropertyPlaceholderConfigurer;
import com.naver.kitchen.common.type.FoodMaterialType;
import com.naver.kitchen.common.type.FoodMaterialXmlFile;
import com.naver.kitchen.common.type.ServiceExposureType;

// TODO: Auto-generated Javadoc
/**
 * The Class NcrFoodMaterialDataWriter.
 *
 * @author crom7142
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
					FoodFileReadResultModel foodFileReadResultModel = (FoodFileReadResultModel)data.get(i);

					if (foodFileReadResultModel != null) {

						// 배치 시작전에 배치관리 테이블에 insert하고 mgmtId를 리턴 받는다.
						//int mgmtId = batchManagementBO.insertBatchManagement();
						//logger.info("+++++++++++++++++++++++===>mgmtId: [" + mgmtId + "]");

						List<FoodMtrlModel> ls = foodFileReadResultModel.getFoodMtrlModel();

						FoodMtrlModel foodMtrlModel = null;

						Iterator<FoodMtrlModel> ite = (Iterator<FoodMtrlModel>)ls.iterator();
						while (ite.hasNext()) {

							foodMtrlModel = (FoodMtrlModel)ite.next();
							String strSvcExpsTp = foodMtrlModel.getKitchenServiceExposeType();
							String foodId = foodMtrlModel.getId();

							// 레시피는 별도 노출여부 설정이 없어 모든 데이터를 입고처리하고,
							// 다른 음식재료는 노출여부가 1,2,3 인 데이터만 입고하도록 되어 있다. 
							if (checkInsertFoodDb(strFoodMaterialType, strSvcExpsTp)) {
								ncrFoodMaterialDataBO.insertFoodDB(foodMtrlModel, foodId, strFoodMaterialType);
							}
						}

						// 배치 종료시 해당 ID를 Y로 업데이트한다. 
						//batchManagementBO.updateBatchManagement(mgmtId, "Y");

						// mgmtId가 아닌 데이터에 대해 삭제처리한다. 
						// ncrFoodMaterialDataBO.deleteFoodDB(strFoodMaterialType, mgmtId);

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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

	/**
	 * Check insert food db.
	 *
	 * @param strFoodMtrlTp the str food mtrl tp
	 * @param strSvcExpsTp the str svc exps tp
	 * @return true, if successful
	 */
	private boolean checkInsertFoodDb(String strFoodMtrlTp, String strSvcExpsTp) {

		// 레시피는 별도 노출여부 설정이 없어 모든 데이터를 입고처리하고,
		// 다른 음식재료는 노출여부가 1,2,3 인 데이터만 입고하도록 되어 있다. 
		if (strFoodMtrlTp.equals(FoodMaterialType.RECIPE.getCode())) {
			return true;
		}

		if (strSvcExpsTp.equals(ServiceExposureType.DISPLAY.getCode()) || strSvcExpsTp.equals(ServiceExposureType.HIDDEN.getCode()) || strSvcExpsTp.equals(ServiceExposureType.QA.getCode())) {
			return true;
		}

		return false;
	}

}
