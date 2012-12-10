
// Too many comments


/*
 * @(#)FoodMaterialBO.java $version 2012. 11. 12.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.kitchen.batch.ncr.bo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naver.kitchen.batch.ncr.dao.FoodAddInfoDAO;
import com.naver.kitchen.batch.ncr.dao.FoodAlterMaterialDAO;
import com.naver.kitchen.batch.ncr.dao.FoodAreaProductDAO;
import com.naver.kitchen.batch.ncr.dao.FoodCompareDAO;
import com.naver.kitchen.batch.ncr.dao.FoodCookStepDAO;
import com.naver.kitchen.batch.ncr.dao.FoodCookStepImageDAO;
import com.naver.kitchen.batch.ncr.dao.FoodEfficacyDAO;
import com.naver.kitchen.batch.ncr.dao.FoodEfficacyTypeDAO;
import com.naver.kitchen.batch.ncr.dao.FoodHarmonyDAO;
import com.naver.kitchen.batch.ncr.dao.FoodKeepMethodDAO;
import com.naver.kitchen.batch.ncr.dao.FoodMaterialDAO;
import com.naver.kitchen.batch.ncr.dao.FoodMaterialInfoDAO;
import com.naver.kitchen.batch.ncr.dao.FoodNutrientDAO;
import com.naver.kitchen.batch.ncr.dao.FoodRelWordDAO;
import com.naver.kitchen.batch.ncr.model.FoodAreaProdModel;
import com.naver.kitchen.batch.ncr.model.FoodCompareModel;
import com.naver.kitchen.batch.ncr.model.FoodCookStepImageModel;
import com.naver.kitchen.batch.ncr.model.FoodCookStepModel;
import com.naver.kitchen.batch.ncr.model.FoodEfficacyModel;
import com.naver.kitchen.batch.ncr.model.FoodEfficacyTypeModel;
import com.naver.kitchen.batch.ncr.model.FoodHarmonyModel;
import com.naver.kitchen.batch.ncr.model.FoodKeepMethodModel;
import com.naver.kitchen.batch.ncr.model.FoodMaterialAlterModel;
import com.naver.kitchen.batch.ncr.model.FoodMaterialModel;
import com.naver.kitchen.batch.ncr.model.FoodMtrlModel;
import com.naver.kitchen.batch.ncr.model.FoodNutrientModel;
import com.naver.kitchen.batch.ncr.model.FoodRelWordModel;
import com.naver.kitchen.common.type.FoodMaterialAddInfoType;

/**
 */
@Service
public class NcrFoodMaterialDataBO {

	/** The Constant LOG. */
	private final Log logger = LogFactory.getLog(NcrFoodMaterialDataBO.class);

	@Autowired
	FoodMaterialUtilBO foodMaterialUtilBO;

	@Autowired
	FoodMaterialDAO foodMaterialDAO;

	@Autowired
	FoodAddInfoDAO foodAddInfoDAO;

	@Autowired
	FoodKeepMethodDAO foodKeepMethodDAO;

	@Autowired
	FoodAreaProductDAO foodAreaProductDAO;

	@Autowired
	FoodHarmonyDAO foodHarmonyDAO;

	@Autowired
	FoodRelWordDAO foodRelWordDAO;

	@Autowired
	FoodCompareDAO foodCompareDAO;

	@Autowired
	FoodNutrientDAO foodNutrientDAO;

	@Autowired
	FoodEfficacyTypeDAO foodEfficacyTypeDAO;

	@Autowired
	FoodEfficacyDAO foodEfficacyDAO;

	@Autowired
	FoodMaterialInfoDAO foodMaterialInfoDAO;

	@Autowired
	FoodAlterMaterialDAO foodAlterMaterialDAO;

	@Autowired
	FoodCookStepDAO foodCookStepDAO;

	@Autowired
	FoodCookStepImageDAO foodCookStepImageDAO;

	/**
	 * 	음식재료정보 DB에 저장
	 * 
	 * @param foodMtrlModel foodMtrlModel
	 * @param foodMtrlTp foodMtrlTp
	 * @return iRtn iRtn
	 * @throws SQLException SQLException
	 */
	public void insertFoodDB(FoodMtrlModel foodMtrlModel, String foodId, String foodMaterialType) throws SQLException {

		// 음식 재료정보 입력 (tb_food_mtrl INSERT/UPDATE)
		insertFoodMaterial(foodId, foodMaterialType, foodMtrlModel);

		// 음식 부가정보 11종 입력 (tb_food_add_info INSERT/UPDATE)
		// 국가(NA), 계절(SS), 조리방법(CM), 상황별(CI), 상황별대상(CO), 식사구분(ME), 생산방법(PM), 기능별(FU), 일반요리팁(CT), 시간(TI), 성별(SE)
		insertFoodAddInfo(foodId, foodMaterialType, foodMtrlModel);

		// KeepMethod 보관방법 (tb_keep_mthd DELETE => INSERT/UPDATE)
		insertFoodKeepMethodModel(foodId, foodMaterialType, foodMtrlModel);

		// 지역산지 정보 입력  (tb_area_prod DELETE => INSERT/UPDATE)
		insertFoodAreaProdModel(foodId, foodMaterialType, foodMtrlModel);

		// 궁합음식정보 입력  (tb_hmny DELETE => INSERT/UPDATE)
		insertFoodHarmonyModel(foodId, foodMaterialType, foodMtrlModel);

		// 연관검색어정보 입력  (tb_rel_word DELETE => INSERT/UPDATE)
		insertFoodRelWordModel(foodId, foodMaterialType, foodMtrlModel);

		// 비교정보 입력  (tb_cmpr_mtrl DELETE => INSERT/UPDATE)
		insertFoodCmprMtrlModel(foodId, foodMaterialType, foodMtrlModel);

		// Nutrient정보 입력  (tb_ntrnt DELETE => INSERT/UPDATE)
		insertFoodNtrntModel(foodId, foodMaterialType, foodMtrlModel);

		// 효능타입 - 효능  (tb_efc_tp / tb_efc DELETE => INSERT/UPDATE)
		insertFoodEfcTpModel(foodId, foodMaterialType, foodMtrlModel);

		// 재료 - 대체재료  (tb_mtrl / tb_alter_mtrl DELETE => INSERT/UPDATE)
		insertFoodMtrlModel(foodId, foodMaterialType, foodMtrlModel);

		// 조리단계 - 조리단계 이미지 ( (tb_cook_step / tb_cook_step_img DELETE => INSERT/UPDATE)
		insertCookStepModel(foodId, foodMaterialType, foodMtrlModel);
	}

	/**
	 * 음식 재료정보 입력 (tb_food_mtrl INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	public void insertFoodMaterial(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// Map에 XML로 부터 읽어온 데이터를 세팅한다. 
		Map map = new HashMap();
		map = foodMaterialUtilBO.setFoodMaterialDB(foodMaterialModel, foodMaterialType);

		// tb_food_mtrl INSERT INTO ~ DUPLICATE UPDATE
		foodMaterialDAO.update(map);
	}

	/**
	 * 	음식 부가정보 11종 입력 (tb_food_add_info INSERT/UPDATE)
	 *  국가(NA), 계절(SS), 조리방법(CM), 상황별(CI), 상황별대상(CO), 식사구분(ME), 생산방법(PM), 기능별(FU), 일반요리팁(CT), 시간(TI), 성별(SE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food mtrl model
	 */
	private void insertFoodAddInfo(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {

		// 음식부가정보 
		String foodAddInfoType[] = {FoodMaterialAddInfoType.COUNTRY.getCode(), FoodMaterialAddInfoType.SEASON.getCode(), FoodMaterialAddInfoType.COOK_METHOD.getCode(), FoodMaterialAddInfoType.CIRCUM.getCode(), FoodMaterialAddInfoType.CIRCM_OBJECT.getCode(), FoodMaterialAddInfoType.MEAL.getCode(), FoodMaterialAddInfoType.PRODUCT_METHOD.getCode(), FoodMaterialAddInfoType.FUNCTION.getCode(), FoodMaterialAddInfoType.GENERAL_COOKTIP.getCode(), FoodMaterialAddInfoType.TIME_DAY.getCode(),
			FoodMaterialAddInfoType.SEX.getCode()};

		// foodid의 foodMaterialType에 해당하는 부가정보 모두 삭제처리
		foodAddInfoDAO.deleteFoodAddInfo(foodId, foodMaterialType);

		// 11종 음식부가정보 유형에 대해 모두 insert 한다.
		for (int i = 0; i < foodAddInfoType.length; i++) {

			String foodAddInfoTypeCode = foodAddInfoType[i];

			// 부가정보 유형코드에 해당하는 데이터가 존재하는지 확인한다.
			if (foodMaterialUtilBO.isNotEmptyFoodAddInfo(foodMaterialModel, foodAddInfoTypeCode)) {

				// 음식 부가정보를 INSERT/UPDATE
				foodMaterialUtilBO.insertAddInfo(foodAddInfoDAO, foodMaterialModel, foodId, foodMaterialType, foodAddInfoTypeCode);
			}
		}

	}

	/**
	 * KeepMethod 보관방법 (tb_keep_mthd DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodKeepMethodModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {

		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodKeepMethodDAO.delete(foodId, foodMaterialType);

		List<FoodKeepMethodModel> keepMethodModel = foodMaterialModel.getKeepMethodModel();
		if (CollectionUtils.isEmpty(keepMethodModel)) {
			return;
		}

		for (FoodKeepMethodModel keepMothodInfo : keepMethodModel) {
			foodKeepMethodDAO.update(foodId, foodMaterialType, keepMothodInfo);
		}
	}

	/**
	 * 지역산지 정보 입력  (tb_area_prod DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodAreaProdModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {

		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodAreaProductDAO.delete(foodId, foodMaterialType);

		List<FoodAreaProdModel> areaProductModel = foodMaterialModel.getAreaProdModel();
		if (CollectionUtils.isEmpty(areaProductModel)) {
			return;
		}

		for (FoodAreaProdModel areaProductInfo : areaProductModel) {
			foodAreaProductDAO.update(foodId, foodMaterialType, areaProductInfo);
		}

	}

	/**
	 * 궁합음식정보 입력  (tb_hmny DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodHarmonyModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodHarmonyDAO.delete(foodId, foodMaterialType);

		List<FoodHarmonyModel> harmonyModel = foodMaterialModel.getHarmonyModel();
		if (CollectionUtils.isEmpty(harmonyModel)) {
			return;
		}

		for (FoodHarmonyModel harmonyInfo : harmonyModel) {
			foodHarmonyDAO.update(foodId, foodMaterialType, harmonyInfo);
		}
	}

	/**
	 * 연관검색어정보 입력  (tb_rel_word DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodRelWordModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodRelWordDAO.delete(foodId, foodMaterialType);

		List<FoodRelWordModel> relWordModel = foodMaterialModel.getRelWordModel();
		if (CollectionUtils.isEmpty(relWordModel)) {
			return;
		}

		for (FoodRelWordModel relWordInfo : relWordModel) {
			foodRelWordDAO.update(foodId, foodMaterialType, relWordInfo);
		}
	}

	/**
	 * 비교정보 입력  (tb_cmpr_mtrl DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodCmprMtrlModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodCompareDAO.delete(foodId, foodMaterialType);

		List<FoodCompareModel> compareModel = foodMaterialModel.getCompareModel();
		if (CollectionUtils.isEmpty(compareModel)) {
			return;
		}

		for (FoodCompareModel compareInfo : compareModel) {
			foodCompareDAO.update(foodId, foodMaterialType, compareInfo);
		}
	}

	/**
	 * 영양성분(Nutrient)정보 입력  (tb_ntrnt DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodNtrntModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodNutrientDAO.delete(foodId, foodMaterialType);

		List<FoodNutrientModel> nutrientModel = foodMaterialModel.getNutrientModel();
		if (CollectionUtils.isEmpty(nutrientModel)) {
			return;
		}

		for (FoodNutrientModel nurtientInfo : nutrientModel) {
			foodNutrientDAO.update(foodId, foodMaterialType, nurtientInfo);
		}
	}

	/**
	 * 효능타입 - 효능  (tb_efc_tp / tb_efc DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodEfcTpModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodEfficacyTypeDAO.delete(foodId, foodMaterialType);
		foodEfficacyDAO.delete(foodId, foodMaterialType);

		List<FoodEfficacyTypeModel> efficacyTypeModel = foodMaterialModel.getEfficacyTypeModel();

		if (CollectionUtils.isEmpty(efficacyTypeModel)) {
			return;
		}

		for (FoodEfficacyTypeModel efficacyTypeInfo : efficacyTypeModel) {
			foodEfficacyTypeDAO.update(foodId, foodMaterialType, efficacyTypeInfo);
			String efficacySeq = efficacyTypeInfo.getEfficacyTypeSequence();

			List<FoodEfficacyModel> efficacyModel = efficacyTypeInfo.getEfficacyModel();

			if (CollectionUtils.isNotEmpty(efficacyModel)) {
				logger.debug("+++++++++++++++efficacyTypeModel:" + efficacyTypeModel.size() + "`efficacyModel:" + efficacyModel.size());
				for (FoodEfficacyModel efficacyInfo : efficacyModel) {
					foodEfficacyDAO.update(foodId, foodMaterialType, efficacySeq, efficacyInfo);
				}
			}
		}

	}

	/**
	 * 재료 - 대체재료  (tb_mtrl / tb_alter_mtrl DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertFoodMtrlModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodMaterialInfoDAO.delete(foodId, foodMaterialType);
		foodAlterMaterialDAO.delete(foodId, foodMaterialType);

		List<FoodMaterialModel> materialModel = foodMaterialModel.getMaterialModel();
		if (CollectionUtils.isEmpty(materialModel)) {
			return;
		}

		for (FoodMaterialModel materialInfo : materialModel) {
			foodMaterialInfoDAO.update(foodId, foodMaterialType, materialInfo);
			String materialSeq = materialInfo.getMaterialSeq();

			List<FoodMaterialAlterModel> materialAlterModel = materialInfo.getMaterialAlterModel();

			if (CollectionUtils.isNotEmpty(materialAlterModel)) {
				for (FoodMaterialAlterModel materialAlterInfo : materialAlterModel) {
					foodAlterMaterialDAO.update(foodId, foodMaterialType, materialSeq, materialAlterInfo);
				}
			}
		}
	}

	/**
	 * 조리단계 - 조리단계 이미지 ( (tb_cook_step / tb_cook_step_img DELETE => INSERT/UPDATE)
	 *
	 * @param foodId the food id
	 * @param foodMaterialType the food material type
	 * @param foodMaterialModel the food material model
	 */
	private void insertCookStepModel(String foodId, String foodMaterialType, FoodMtrlModel foodMaterialModel) {
		// FoodCookStepDAO foodCookStepDAO;
		// foodCookStepImageDAO
		// INSERT 하지 않더라도 삭제해야 한다. (NCR에서 데이터가 삭제될 경우가 있기 때문에....)
		foodCookStepDAO.delete(foodId, foodMaterialType);
		foodCookStepImageDAO.delete(foodId, foodMaterialType);

		List<FoodCookStepModel> cookStepModel = foodMaterialModel.getCookStepModel();
		if (CollectionUtils.isEmpty(cookStepModel)) {
			return;
		}

		for (FoodCookStepModel cookStepInfo : cookStepModel) {
			foodCookStepDAO.update(foodId, foodMaterialType, cookStepInfo);
			String cookStep = cookStepInfo.getCookStep();

			List<FoodCookStepImageModel> cookStepImageModel = cookStepInfo.getCookStepImageModel();

			if (CollectionUtils.isNotEmpty(cookStepImageModel)) {
				logger.debug("+++++++++++++++cookStepImageModel:" + cookStepImageModel.size());
				for (FoodCookStepImageModel cookStepImageInfo : cookStepImageModel) {
					foodCookStepImageDAO.update(foodId, foodMaterialType, cookStep, cookStepImageInfo);
				}
			}
		}

	}

}
