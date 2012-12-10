	@Authorization(exceptLevel = {UserLevel.DB_READER, UserLevel.CP_USER, UserLevel.DATA_EDITOR, UserLevel.DB_MANAGER, UserLevel.DB_READER, UserLevel.ALL_READER})
	public String addClassificationDownloadByCondition() throws Exception {
		Map<String, String> conditionMap = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(params.getString("dbKorName"))) {
			conditionMap.put("dbKorNm", params.getString("dbKorName"));
		}
		if (StringUtils.isNotEmpty(params.getString("id"))) {
			conditionMap.put("id", params.getString("id"));
		}
		if (StringUtils.isNotEmpty(params.getString("title"))) {
			conditionMap.put("title", params.getString("title"));
		}
		if (StringUtils.isNotEmpty(params.getString("priority"))) {
			conditionMap.put("priority", params.getString("priority"));
		}
		if (StringUtils.isNotEmpty(params.getString("regDtFrom"))) {
			conditionMap.put("regDtFrom", params.getString("regDtFrom"));
		}
		if (StringUtils.isNotEmpty(params.getString("regDtTo"))) {
			conditionMap.put("regDtTo", params.getString("regDtTo"));
		}
		if (StringUtils.isNotEmpty(params.getString("modDtFrom"))) {
			conditionMap.put("modDtFrom", params.getString("modDtFrom"));
		}
		if (StringUtils.isNotEmpty(params.getString("modDtTo"))) {
			conditionMap.put("modDtTo", params.getString("modDtTo"));
		}
		JSONObject json = new JSONObject();
		json.putAll(conditionMap);
		ClassificationDownload insertParam = new ClassificationDownload();
		insertParam.setDnldTp(params.getString("dnldTp"));
		insertParam.setCatId(params.getLong("catId"));
		
		int dataCnt = 0;
		if ("I".equals(params.getString("dnldTp"))) {
			Classification selectParam = new Classification();
			List<Long> childCatIdList = classificationExtractBO.selectChildCatIdList(params.getLong("catId"));			
			selectParam.setCatIdArr(childCatIdList.toArray(new Long[0]));
			dataCnt = classificationBO.getClassificationCount(selectParam);
		} else {
			dataCnt = params.getInt("dataCnt");
		} 
		
		if (conditionMap.size() > 0) {
			insertParam.setCond(json.toString());
		}
		
		insertParam.setDataCnt(dataCnt);
		if (dataCnt > 300000) {
			insertParam.setStat(ClassificationDownloadConstants.DOWNLOAD_STAT_ERROR); // W(대기)/P(진행중)/C(완료)/E(오류)
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			insertParam.setLog(date + " :: 다운로드 데이터 수가 30만 개를 초과합니다.</br>컨텐츠DB개발팀에 문의해주세요.");
		} else {
			insertParam.setStat(ClassificationDownloadConstants.DOWNLOAD_STAT_WAIT); // W(대기)/P(진행중)/C(완료)/E(오류)
			insertParam.setLog("");
		}
		insertParam.setReqId(getUserInfo().getId());
		insertParam.setSmsSndYn("Y");
		insertParam.setMailSndYn("Y");

		classificationDownloadBO.insertClassificationDownload(insertParam);
		return SUCCESS;
	}
