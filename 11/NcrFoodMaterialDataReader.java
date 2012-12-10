/*
 * @(#)NcrFoodMaterialDataReader.java $version 2012. 11. 9.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.sample.batch.ncr.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.naver.sample.batch.common.util.DigesterParser;
import com.naver.sample.batch.common.util.KitchenConstant;
import com.naver.sample.batch.ncr.model.FoodFileReadResultModel;
import com.naver.sample.common.handler.ExposablePropertyPlaceholderConfigurer;

/**
 */
public class NcrFoodMaterialDataReader implements ItemReader<FoodFileReadResultModel> {

	private final Log logger = LogFactory.getLog(NcrFoodMaterialDataReader.class);

	/** The index. */
	private int index = 0;

	/** The Constant INTEGER_ZERO. */
	private static final int INTEGER_ZERO = 0;
	private static final String NCR_FOOD_MATERIAL_XML_RULE = "/com/naver/kitchen/batch/ncr/model/FoodMaterialFileReadRule.xml";
	
	// 배치용 xml파일 경로(properties파일의 변수명)
	private static final String NCR_FULL_BATCH_FILE_PATH = "ncrKitchentData";
	private static final String NCR_INCREAMENT_BATCH_FILE_PATH = "ncrKitchentIncreamentData";
	
	/** The ncr export file name. */
	private String ncrFoodMaterialFileName = "sample.xml";
	
	// 스프링배치를 통해 전달받는 BatchJobParam
	// BATCH_FLAG_FULL 혹은 BATCH_FLAG_INCREAMENT 값을 가지게 된다
	private String batchJobFlag = "N";
	
	// 배치job의 종류(Full 혹은 증분)
	private static final String BATCH_FLAG_FULL = "full";
	private static final String BATCH_FLAG_INCREAMENT = "increament";

	/** The property configurer. */
	@Autowired
	private ExposablePropertyPlaceholderConfigurer propertyConfigurer;

	/**
	 * Reads next record from input
	 */

	public FoodFileReadResultModel read() throws Exception {

		FoodFileReadResultModel foodMaterialData = new FoodFileReadResultModel();

		if (index > INTEGER_ZERO) {
			return null;
		}

		String dataFilePath;
		
		if(isIncreamentBatchJob()) {
			dataFilePath = propertyConfigurer.getResolvedProps().get(NCR_INCREAMENT_BATCH_FILE_PATH);
		} else {
			dataFilePath = propertyConfigurer.getResolvedProps().get(NCR_FULL_BATCH_FILE_PATH);
		}

		try {
			logger.info("==> File Name : " + dataFilePath + ncrFoodMaterialFileName);
			foodMaterialData = (FoodFileReadResultModel)DigesterParser.parse(NCR_FOOD_MATERIAL_XML_RULE, new File(dataFilePath + ncrFoodMaterialFileName), KitchenConstant.CHARSET_TYPE_UTF_8);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			//throw e;

		} finally {
			logger.info("+++++++++++++++++++++++===>" + foodMaterialData.getFoodMtrlModel().size());
			index++;
		}

		return foodMaterialData;
	}

	/**
	 * Sets the ncr food material file name.
	 *
	 * @param ncrFoodMaterialFileName the new ncr food material file name
	 */
	public void setNcrFoodMaterialFileName(String ncrFoodMaterialFileName) {
		this.ncrFoodMaterialFileName = ncrFoodMaterialFileName;
	}
	
	private boolean isIncreamentBatchJob() {
		if(batchJobFlag.equals(BATCH_FLAG_INCREAMENT)) {
			return true;
		}
		return false;
	}

	public void setBatchJobFlag(String batchJobFlag) {
		this.batchJobFlag = batchJobFlag;
	}

}
