package jp.naver.cafe.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import jp.naver.cafe.approval.ApprovalHistoryType;
import jp.naver.cafe.cafe.membership.CafeMembershipSortType;
import jp.naver.cafe.cafe.membership.CafeMembershipType;
import jp.naver.cafe.exception.GeneralServiceException;
import jp.naver.cafe.exception.SearchException;
import jp.naver.cafe.exception.WrongParameterException;
import jp.naver.cafe.language.LanguageCode;
import jp.naver.cafe.support.httpclient.HttpClientTemplate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SearchClientImpl implements SearchClient, InitializingBean {

	private static JAXBContext searchCafeContext = null;
	private static JAXBContext searchUserContext = null;
	private static JAXBContext searchApprovalHistoryContext = null;

	static {
		try {
			searchCafeContext = JAXBContext.newInstance(SearchCafeMessage.class);
			searchUserContext = JAXBContext.newInstance(SearchUserMessage.class);
			searchApprovalHistoryContext = JAXBContext.newInstance(SearchApprovalHistoryMessage.class);

		} catch (JAXBException e) {
			throw GeneralServiceException.internalError(e);
		}
	}

	private static final String CAFE_QUERY_SEPARATOR = " ";
	private static final String CAFE_LANGUAGE_CODE_SEPARATOR = ":";
	private static final String USER_HASH_MD5HEX_SEPARATOR = ":";

	private HttpClientTemplate httpClientTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchClientImpl.class);

	@Value(value = "#{applicationProperty['search.server.url']}")
	private String searchServerUrl;

	@Value(value = "#{applicationProperty['search.server.pr']}")
	private String searchServerPr;

	@Override
	public void afterPropertiesSet() throws Exception {

		if (StringUtils.isBlank(searchServerUrl)) {
			throw new IllegalStateException("searchServerUrl can not be blank!");
		}

		if (StringUtils.isBlank(searchServerPr)) {
			throw new IllegalStateException("searchServerPr can not be blank!");
		}

		httpClientTemplate = new HttpClientTemplate();
		httpClientTemplate.initialize();

	}

	private Map<String, Object> createParameterCafeSvcCommonParam() {

		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("version", "1.0.0");
		parameters.put("pr", searchServerPr);
		parameters.put("r_format", "xml");

		return parameters;
	}

	protected HttpClientTemplate getHttpClientTemplate() {
		return httpClientTemplate;
	}

	public String getSearchServerUrl() {
		return searchServerUrl;
	}

	public void setSearchServerUrl(String searchServerUrl) {
		this.searchServerUrl = searchServerUrl;
	}

	@Override
	public SearchCafeMessage findCafesByCategory(List<LanguageCode> languageCodes, int categoryId, Pageable pageable) {

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		List<String> codes = prepareforLanguageCodesFilterJob(languageCodes);

		String query = StringUtils.join(codes, CAFE_QUERY_SEPARATOR);
		String st_lang = StringUtils.join(codes, CAFE_LANGUAGE_CODE_SEPARATOR);

		fillCommonCafeSearchParam(parameters, query);

		parameters.put("st_pi", "notexist:0");
		parameters.put("st_category", "exist:" + categoryId);
		parameters.put("so", "score.dsc");
		parameters.put("st_lang", "exist:" + st_lang);

		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());

		String response = postProcessSearchServerCall(parameters);

		return toSearchCafeMessage(response);
	}

	@Override
	public SearchCafeMessage findCafesByNameAndDescription(String query, List<LanguageCode> languageCodesType,
			Pageable pageable) {

		String splitedQuery = SearchKeywordUtil.splitSearchKeywordByWidth(SearchKeywordUtil.removeExceptCharacters(query), SearchFixType.NONE);
		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		List<String> codes = prepareforLanguageCodesFilterJob(languageCodesType);

		String st_lang = StringUtils.join(codes, CAFE_LANGUAGE_CODE_SEPARATOR);

		parameters.put("st_pi", "notexist:0");
		parameters.put("st", "cafe");
		parameters.put("st_type", "exist:C:A:F");
		parameters.put("st_public_type", "exist:P");
		parameters.put("sm", "name.basic");
		parameters.put("q", splitedQuery);

		parameters.put("hl", "all." + SearchCafeMessage.HIGHLIGHT_TAGS);
		parameters.put("st_status", "exist:N");
		parameters.put("st_lang", "exist:" + st_lang);
		parameters.put("so", "score.dsc");

		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());

		String response = postProcessSearchServerCall(parameters);

		return toSearchCafeMessage(response);
	}

	@Override
	public SearchCafeMessage findHirobaHotCafes(List<LanguageCode> languageCodes, int pageNumber,
			int fetchSize) {

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		List<String> codes = prepareforLanguageCodesFilterJob(languageCodes);

		String query = StringUtils.join(codes, CAFE_QUERY_SEPARATOR);

		fillCommonCafeSearchParam(parameters, query);

		parameters.put("st_pi", "notexist:0");
		parameters.put("so", "score.dsc");
		parameters.put("start", ((pageNumber * fetchSize) + 1));
		parameters.put("display", fetchSize);

		String response = postProcessSearchServerCall(parameters);

		return toSearchCafeMessage(response);
	}

	@Override
	public SearchCafeMessage findHirobaNewCafes(List<LanguageCode> languageCodesType, int pageNumber,
			int fetchSize) {

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		List<String> codes = prepareforLanguageCodesFilterJob(languageCodesType);

		String query = StringUtils.join(codes, CAFE_QUERY_SEPARATOR);

		fillCommonCafeSearchParam(parameters, query);

		parameters.put("st_pi", "notexist:0");
		parameters.put("so", "last_posted.dsc");
		parameters.put("st_last_posted", "range:20000101:");
		parameters.put("start", ((pageNumber * fetchSize) + 1));
		parameters.put("display", fetchSize);

		String response = postProcessSearchServerCall(parameters);

		return toSearchCafeMessage(response);
	}

	@Override
	public SearchCafeMessage findCafesOrderByNew(List<LanguageCode> languageCodes, Pageable pageable) {

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		List<String> codes = prepareforLanguageCodesFilterJob(languageCodes);

		String query = StringUtils.join(codes, CAFE_QUERY_SEPARATOR);
		String st_lang = StringUtils.join(codes, CAFE_LANGUAGE_CODE_SEPARATOR);

		fillCommonCafeSearchParam(parameters, query);

		parameters.put("st_pi", "notexist:0");
		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());
		parameters.put("st_lang", "exist:" + st_lang);
		parameters.put("so", "created.dsc");

		String response = postProcessSearchServerCall(parameters);

		return toSearchCafeMessage(response);
	}

	private void fillCommonCafeSearchParam(Map<String, Object> parameters, String query) {

		parameters.put("st", "cafe");
		parameters.put("sm", "lang.basic");
		parameters.put("q", query);
		parameters.put("st_type", "exist:C:A:F");
		parameters.put("st_status", "exist:N");
		parameters.put("st_public_type", "exist:P");
	}

	@Override
	public SearchCafeMessage findOsusumeCafes(List<LanguageCode> languageCodes, Pageable pageable) {

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		List<String> codes = prepareforLanguageCodesFilterJob(languageCodes);

		String query = StringUtils.join(codes, CAFE_QUERY_SEPARATOR);

		fillCommonCafeSearchParam(parameters, query);

		parameters.put("st_pi", "notexist:0");
		parameters.put("so", "score.dsc");
		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());

		String response = postProcessSearchServerCall(parameters);

		return toSearchCafeMessage(response);
	}

	@Override
	public SearchUserMessage findSummonSuggestPostActionUser(String query,
			Set<String> postActionUserHashSet,
			SearchFixType searchFixType) {

		String splitedQuery = SearchFixType.findSearchSyntax(searchFixType, SearchKeywordUtil.removeExceptCharacters(query));
		String postActionUserHashMD5Hex = prepareForPostActionParam(postActionUserHashSet);

		checkPostActoinUserHash(postActionUserHashMD5Hex);

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		parameters.put("st", "user");
		parameters.put("sm", "name.basic.trunc");
		parameters.put("q", splitedQuery);
		parameters.put("st_status", "exist:N");

		if (isEmptyPostActionUserHashMD5(postActionUserHashMD5Hex) == false) {
			parameters.put("st_suggest_user", "exist:" + postActionUserHashMD5Hex);
		}
		parameters.put("so", "rel.dsc");
		parameters.put("hl", "name." + SearchCafeMessage.HIGHLIGHT_TAGS);

		parameters.put("start", "1");
		parameters.put("display", SearchServiceImpl.MAX_SUMMON_SUGGEST_USER_FETCH_SIZE);

		String response = postProcessSearchServerCall(parameters);

		return toSearchUserMessage(response);
	}

	@Override
	public SearchUserMessage findCafeMember(long cafeId, String query,
			Set<String> postActionUserHashSet, CafeMembershipSortType cafeMemberSortType,
			CafeMembershipType cafeMemberShipType,
			SearchFixType searchFixType, Pageable pageable) {

		String splitedQuery = SearchFixType.findSearchSyntax(searchFixType, SearchKeywordUtil.removeExceptCharacters(query));
		String postActionUserHashMD5Hex = prepareForPostActionParam(postActionUserHashSet);

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		parameters.put("st", "cafe_member");
		parameters.put("sm", "name.basic.trunc");
		parameters.put("q", splitedQuery);
		parameters.put("q_cafe_id", cafeId);

		if (cafeMemberShipType.isJoinedMember()) {
			parameters.put("st_mem_level", "range:2:9");
		} else {
			parameters.put("st_mem_level", "range:0:0");
		}

		parameters.put("so", cafeMemberSortType.getSortType());
		if (isEmptyPostActionUserHashMD5(postActionUserHashMD5Hex) == false) {
			parameters.put("st_user_hash_md5", "notexist:" + postActionUserHashMD5Hex);
		}
		parameters.put("st_status", "exist:N");
		parameters.put("hl", "name." + SearchCafeMessage.HIGHLIGHT_TAGS);
		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());

		String response = postProcessSearchServerCall(parameters);

		return toSearchUserMessage(response);
	}

	@Override
	public SearchApprovalHistoryMessage findApprovalHistories(long cafeId, String query,
			ApprovalHistoryType approvalHistoryType,
			CafeMembershipSortType cafeMembershipSortType,
			SearchFixType searchFixType, Pageable pageable) {

		String splitedQuery = SearchFixType.findSearchSyntax(searchFixType, SearchKeywordUtil.removeExceptCharacters(query));

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		parameters.put("st", "approval");
		parameters.put("sm", "name.basic.trunc");
		parameters.put("q", splitedQuery);
		parameters.put("so", cafeMembershipSortType.getSortType());
		parameters.put("q_cafe_id", cafeId);
		parameters.put("st_approval_type", "exist:" + approvalHistoryType.getCode());

		parameters.put("hl", "name." + SearchCafeMessage.HIGHLIGHT_TAGS);
		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());

		String response = postProcessSearchServerCall(parameters);

		return toSearchApprovalhistoryMessage(response);
	}

	private String prepareForPostActionParam(Set<String> postActionUserHashSet) {

		if (CollectionUtils.isEmpty(postActionUserHashSet)) {
			return null;
		}

		return StringUtils.join(postActionUserHashSet, USER_HASH_MD5HEX_SEPARATOR);
	}

	@Override
	public SearchUserMessage findNaverCafeUser(String query, Set<String> postActionUserHashSet,
			SearchFixType searchFixType, Pageable pageable) {

		String splitedQuery = SearchFixType.findSearchSyntax(searchFixType, SearchKeywordUtil.removeExceptCharacters(query));
		String postActionUserHashMD5Hex = prepareForPostActionParam(postActionUserHashSet);

		Map<String, Object> parameters = createParameterCafeSvcCommonParam();

		parameters.put("st", "user");
		parameters.put("sm", "name.basic.trunc");
		parameters.put("q", splitedQuery);
		parameters.put("so", "username.asc");
		if (isEmptyPostActionUserHashMD5(postActionUserHashMD5Hex) == false) {
			parameters.put("st_user_hash_md5", "notexist:" + postActionUserHashMD5Hex);
		}
		parameters.put("st_status", "exist:N");
		parameters.put("hl", "name." + SearchCafeMessage.HIGHLIGHT_TAGS);
		parameters.put("start", (pageable.getOffset() + 1));
		parameters.put("display", pageable.getPageSize());

		String response = postProcessSearchServerCall(parameters);

		return toSearchUserMessage(response);
	}

	private SearchUserMessage toSearchUserMessage(String response) {

		try {
			return (SearchUserMessage)searchUserContext.createUnmarshaller().unmarshal(new StringReader(response));
		} catch (JAXBException e) {
			LOGGER.warn("fail to unmarshal XML to object.  XML : " + response);
			throw GeneralServiceException.internalError(e);
		}
	}

	private SearchCafeMessage toSearchCafeMessage(String response) {

		try {
			return (SearchCafeMessage)searchCafeContext.createUnmarshaller().unmarshal(new StringReader(response));
		} catch (JAXBException e) {
			LOGGER.warn("fail to unmarshal XML to object.  XML : " + response);
			throw GeneralServiceException.internalError(e);
		}
	}

	private SearchApprovalHistoryMessage toSearchApprovalhistoryMessage(String response) {

		try {
			return (SearchApprovalHistoryMessage)searchApprovalHistoryContext.createUnmarshaller().unmarshal(new StringReader(response));
		} catch (JAXBException e) {
			LOGGER.warn("fail to unmarshal XML to object.  XML : " + response);
			throw GeneralServiceException.internalError(e);
		}
	}

	private void checkPostActoinUserHash(String postActionUserHashMD5Hex) {
		if (isEmptyPostActionUserHashMD5(postActionUserHashMD5Hex)) {
			throw new WrongParameterException("postActionUserHashMD5Hex is Empty.");
		}
	}

	private boolean isEmptyPostActionUserHashMD5(String postActionUserHashMD5Hex) {
		return StringUtils.isBlank(StringUtils.remove(postActionUserHashMD5Hex, USER_HASH_MD5HEX_SEPARATOR));
	}

	private String postProcessSearchServerCall(Map<String, Object> parameters) {

		String response = StringUtils.EMPTY;

		try {
			response = getHttpClientTemplate().executeGet(searchServerUrl, parameters);

			LOGGER.info("parameters -" + parameters.values());
		} catch (Exception e) {
			LOGGER.error("searchServer call failed. response - " + response
				+ " parameters - " + parameters.values());
		}

		checkSearchServerError(response);

		return response;
	}

	private void checkSearchServerError(String response) {
		if (StringUtils.containsIgnoreCase(StringUtils.deleteWhitespace(response), "<code>508</code>")) {
			LOGGER.info("response - " + response);
			throw SearchException.searchServerIsError();
		}
	}

	private List<String> prepareforLanguageCodesFilterJob(List<LanguageCode> languageCodes) {

		if (CollectionUtils.isEmpty(languageCodes)) {
			throw new WrongParameterException("languageCodes is empty");
		}

		List<String> codes = new ArrayList<String>();

		for (LanguageCode languageCode : languageCodes) {
			codes.add(languageCode.getCode());
		}
		return codes;
	}

	public String getSearchServerPr() {
		return searchServerPr;
	}

	public void setSearchServerPr(String searchServerPr) {
		this.searchServerPr = searchServerPr;
	}

}
