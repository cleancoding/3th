package jp.naver.cafe.search;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.on;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jp.naver.cafe.api.MoreableResponse;
import jp.naver.cafe.api.PageableResponse;
import jp.naver.cafe.api.PageableSearchResponse;
import jp.naver.cafe.approval.ApprovalHistory;
import jp.naver.cafe.approval.ApprovalHistoryType;
import jp.naver.cafe.board.Board;
import jp.naver.cafe.board.BoardService;
import jp.naver.cafe.cafe.AbstractShardCafeService;
import jp.naver.cafe.cafe.Cafe;
import jp.naver.cafe.cafe.CafeNoTransactionService;
import jp.naver.cafe.cafe.CafeService;
import jp.naver.cafe.cafe.membership.CafeMembership;
import jp.naver.cafe.cafe.membership.CafeMembershipService;
import jp.naver.cafe.cafe.membership.CafeMembershipSortType;
import jp.naver.cafe.cafe.membership.CafeMembershipType;
import jp.naver.cafe.category.Category;
import jp.naver.cafe.category.CategoryService;
import jp.naver.cafe.comment.Comment;
import jp.naver.cafe.comment.CommentService;
import jp.naver.cafe.language.LanguageCode;
import jp.naver.cafe.like.Like;
import jp.naver.cafe.like.LikeService;
import jp.naver.cafe.piDecrementCafe.PIDecrementCafeService;
import jp.naver.cafe.post.Post;
import jp.naver.cafe.post.PostService;
import jp.naver.cafe.referer.RefererService;
import jp.naver.cafe.searchForbiddenWord.SearchForbiddenWordService;
import jp.naver.cafe.support.cache.CacheUtils;
import jp.naver.cafe.user.User;
import jp.naver.cafe.user.UserService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl extends AbstractShardCafeService implements SearchService {

	private static final String SEARCH_FORBIDDEN_LIMIT_USER_ACCEPT_LANGUAGE = "ja";
	private static final CafeMembershipType DEFAULT_CAFE_MEMBERSHIP_TYPE = CafeMembershipType.MEMBER;
	private static final CafeMembershipSortType DEFAULT_CAFE_MEMBER_SORT_TYPE = CafeMembershipSortType.NAME;

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);

	public static final int MAX_SUMMON_SUGGEST_POST_ACTION_USER_FETCH_SIZE = 100;
	public static final int MAX_SUMMON_SUGGEST_USER_FETCH_SIZE = 10;
	public static final int MAX_HIROBA_CAFES_FETCH_SIZE = 100;
	private static final int MAX_OSUSUME_PAGE_SIZE = 20;
	private static final int MAX_CATEGORY_CAFE_PAGE_SIZE = 20;

	@Autowired
	private BoardService boardService;

	@Autowired
	private PostService postService;

	@Autowired
	private SearchCacheClient searchCacheClient;

	@Autowired
	private CommentService commentService;

	@Autowired
	private LikeService likeService;

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CafeService cafeService;

	@Autowired
	private CafeMembershipService cafeMembershipService;

	@Autowired
	private CafeNoTransactionService cafeNoTransactionService;

	@Autowired
	private PIDecrementCafeService pIDecrementCafeService;

	@Autowired
	private SearchForbiddenWordService searchForbiddenWordService;

	@Autowired
	private RefererService refererService;

	@Override
	public PageableSearchResponse<User> findSummonSuggestUsersWithNokeyword(long cafeId, long postId,
			long parentCommentId) {

		Cafe foundCafe = cafeNoTransactionService.findOrExceptionFromCache(cafeId);

		Set<User> postActionUserSet = new LinkedHashSet<User>();

		Post foundPost = postId > 0 ? postService.find(postId) : null;
		Comment parentComment = parentCommentId > 0 ? commentService.find(parentCommentId) : null;

		if (foundPost != null) {
			postActionUserSet.add(foundPost.getInternalOwner());
		}

		if (parentComment != null) {
			postActionUserSet.add(parentComment.getInternalOwner());
		}

		// 조회 할 기준 카운트에서 현재 조회된 유저 수 만큼 코멘트 조회
		fillCommentActionUsers(postId, postActionUserSet);

		fillLikeActionUsers(postId, postActionUserSet);

		if (postActionUserSet.size() >= MAX_SUMMON_SUGGEST_USER_FETCH_SIZE) {

			return toSuggestUsers(postActionUserSet);
		}

		List<String> exceptPostActionUserHashes = User.extractUserHashes(postActionUserSet);

		fillCafeMembers(foundCafe, postActionUserSet, exceptPostActionUserHashes);

		return toSuggestUsers(postActionUserSet);
	}

	private PageableSearchResponse<User> toSuggestUsers(Set<User> postActionUserSet) {

		List<String> extractUserHashes = User.extractUserHashes(postActionUserSet);

		if (CollectionUtils.isNotEmpty(extractUserHashes)) {

			List<User> foundUsers = userService.findByUserHashesOrderByUserHashField(extractUserHashes);

			if (CollectionUtils.isNotEmpty(foundUsers)) {

				forEach(foundUsers).simplifyForList();
			}

			return new PageableSearchResponse<User>(new ArrayList<User>(foundUsers), new PageRequest(0, MAX_SUMMON_SUGGEST_USER_FETCH_SIZE), foundUsers.size());
		}

		return new PageableSearchResponse<User>(new ArrayList<User>(), new PageRequest(0, MAX_SUMMON_SUGGEST_USER_FETCH_SIZE), 0);
	}

	private void fillCafeMembers(Cafe foundCafe, Set<User> postActionUserSet, List<String> postActionUserHashes) {

		int cafeMembershipFetchSize = MAX_SUMMON_SUGGEST_USER_FETCH_SIZE - postActionUserSet.size();
		List<CafeMembership> foundCafeMemberShips = cafeMembershipService.findMembershipsByCafeIdAndExceptUserHashesOrderByNameASC(foundCafe.getId(), cafeMembershipFetchSize, postActionUserHashes);

		if (CollectionUtils.isNotEmpty(foundCafeMemberShips)) {

			List<User> extractUser = CafeMembership.extractUser(foundCafeMemberShips);

			if (CollectionUtils.isNotEmpty(extractUser)) {
				postActionUserSet.addAll(extractUser);
			}

		}
	}

	private void fillLikeActionUsers(long postId, Set<User> postActionUserSet) {

		int likeFetchSize = MAX_SUMMON_SUGGEST_USER_FETCH_SIZE - postActionUserSet.size();

		if (likeFetchSize > 0) {

			List<Like> foundLikes = likeService.findLikes(postId, likeFetchSize);

			if (CollectionUtils.isNotEmpty(foundLikes)) {

				postActionUserSet.addAll(Like.extractUser(foundLikes));
			}
		}
	}

	private void fillCommentActionUsers(long postId, Set<User> postActionUserSet) {

		int commentFetchSize = MAX_SUMMON_SUGGEST_USER_FETCH_SIZE - postActionUserSet.size();
		List<Comment> foundComments = commentService.findCommentsByPostIdExceptDuplicateUserHash(postId, commentFetchSize);

		if (CollectionUtils.isNotEmpty(foundComments)) {

			postActionUserSet.addAll(Comment.extractUser(foundComments));

		}
	}

	@Override
	public PageableSearchResponse<User> findSummonSuggestUsers(String query, long cafeId, long boardId,
			long postId, long parentCommentId, SearchFixType searchFixType) {

		Cafe foundCafe = cafeNoTransactionService.findOrExceptionFromCache(cafeId);
		Board foundBoard = boardService.findOrExceptionAndStatusCheck(boardId);

		Set<String> postActionUserHashSet = new LinkedHashSet<String>();

		SearchUserMessage groupA = new SearchUserMessage();
		SearchUserMessage groupB = new SearchUserMessage();
		SearchUserMessage groupC = new SearchUserMessage();

		fillPostAndCommentUserHashes(postId, parentCommentId, postActionUserHashSet);

		Set<String> foundCommentAndLikeUserHashSet = findCommentAndLikeActionUserHashes(postId, (MAX_SUMMON_SUGGEST_POST_ACTION_USER_FETCH_SIZE - postActionUserHashSet.size()));
		postActionUserHashSet.addAll(foundCommentAndLikeUserHashSet);

		if (postActionUserHashSet.isEmpty() == false) {

			groupA = findSummonSuggestPostActionUser(query, searchFixType, postActionUserHashSet);

			fillPostActionUserHashSetFrom(postActionUserHashSet, groupA);
		}

		if (groupA.getSearchUserResult().getSearchUserItemList().getSearchUserItems().size() < MAX_SUMMON_SUGGEST_USER_FETCH_SIZE) {

			groupB = searchCafeMember(query, searchFixType, foundCafe, postActionUserHashSet, DEFAULT_CAFE_MEMBER_SORT_TYPE, DEFAULT_CAFE_MEMBERSHIP_TYPE, new PageRequest(0, MAX_SUMMON_SUGGEST_POST_ACTION_USER_FETCH_SIZE));

			fillPostActionUserHashSetFrom(postActionUserHashSet, groupB);
		}

		int groupABTotalItemSize = groupA.getSearchUserResult().getSearchUserItemList().getSearchUserItems().size()
			+ groupB.getSearchUserResult().getSearchUserItemList().getSearchUserItems().size();

		if (foundBoard.isMemberPublicType()
			|| groupABTotalItemSize >= MAX_SUMMON_SUGGEST_USER_FETCH_SIZE) {

			return toSuggestUsers(groupA, groupB, groupC);
		}

		PageRequest groupCPageRequest = new PageRequest(0, MAX_SUMMON_SUGGEST_USER_FETCH_SIZE
			- groupABTotalItemSize);

		groupC = findNaverCafeUser(query, searchFixType, postActionUserHashSet, groupCPageRequest);

		return toSuggestUsers(groupA, groupB, groupC);
	}

	private SearchUserMessage searchCafeMember(String query, SearchFixType searchFixType, Cafe foundCafe,
			Set<String> postActionUserHashSet, CafeMembershipSortType cafeMembershipSortType,
			CafeMembershipType cafeMembershipType, Pageable pageable) {

		String cacheKey = makeCafeMemberCacheKey(query, searchFixType, foundCafe, postActionUserHashSet, cafeMembershipSortType, cafeMembershipType, pageable);

		return searchCacheClient.findCafeMember(foundCafe.getId(), query, postActionUserHashSet, cafeMembershipSortType, cafeMembershipType, searchFixType, pageable, cacheKey);
	}

	private SearchUserMessage findSummonSuggestPostActionUser(String query, SearchFixType searchFixType,
			Set<String> postActionUserHashSet) {

		String cacheKey = makeSummonSuggestPostActionUserCacheKey(query, searchFixType, postActionUserHashSet);

		return searchCacheClient.findSummonSuggestPostActionUser(query, postActionUserHashSet, searchFixType, cacheKey);
	}

	void fillPostActionUserHashSetFrom(Set<String> postActionUserHashSet, SearchUserMessage searchUserMessage) {

		if (searchUserMessage.getSearchUserResult().getSearchUserItemList().getSearchUserItems().size() > 0) {

			List<SearchUserItem> items = searchUserMessage.getSearchUserResult().getSearchUserItemList().getSearchUserItems();

			for (SearchUserItem searchUserResult : items) {

				postActionUserHashSet.add(searchUserResult.getUserHashMd5());
			}
		}
	}

	void fillPostAndCommentUserHashes(long postId, long parentCommentId, Set<String> postActionUserHashSet) {

		Post foundPost = postId > 0 ? postService.find(postId) : null;
		Comment parentComment = parentCommentId > 0 ? commentService.find(parentCommentId) : null;

		if (foundPost != null) {
			postActionUserHashSet.add(foundPost.getInternalOwner().buildUserHashMD5Hex());
		}

		if (parentComment != null) {
			postActionUserHashSet.add(parentComment.getInternalOwner().buildUserHashMD5Hex());
		}
	}

	Set<String> findCommentAndLikeActionUserHashes(long postId, int fetchSize) {

		Set<String> userHashesSet = new LinkedHashSet<String>();

		List<Comment> foundComments = commentService.findCommentsByPostIdExceptDuplicateUserHash(postId, fetchSize);
		fillCommentUserHashes(userHashesSet, foundComments);

		LOGGER.info("commentPostActionUserCount - " + foundComments.size());

		int likeFetchSize = (fetchSize - userHashesSet.size());

		if (likeFetchSize > 0) {

			List<Like> foundLikes = likeService.findLikes(postId, likeFetchSize);
			fillLikesUserHashes(userHashesSet, foundLikes);

			LOGGER.info("likePostActionUserCount - " + foundLikes.size());
		}

		return userHashesSet;
	}

	private void fillCommentUserHashes(Set<String> postActionUserHashSet, List<Comment> foundComments) {

		if (CollectionUtils.isEmpty(foundComments)) {
			return;
		}

		for (Comment comment : foundComments) {
			postActionUserHashSet.add(comment.getInternalOwner().buildUserHashMD5Hex());
		}
	}

	private void fillLikesUserHashes(Set<String> postActionUserHashSet, List<Like> foundLikes) {

		if (CollectionUtils.isEmpty(foundLikes)) {
			return;
		}

		for (Like like : foundLikes) {
			postActionUserHashSet.add(like.getOwner().buildUserHashMD5Hex());
		}
	}

	private PageableSearchResponse<User> toSuggestUsers(SearchUserMessage groupA, SearchUserMessage groupB,
			SearchUserMessage groupC) {

		List<SearchUserItem> searchUserItems = new ArrayList<SearchUserItem>();

		searchUserItems.addAll(groupA.getSearchUserResult().getSearchUserItemList().getSearchUserItems());
		searchUserItems.addAll(groupB.getSearchUserResult().getSearchUserItemList().getSearchUserItems());
		searchUserItems.addAll(groupC.getSearchUserResult().getSearchUserItemList().getSearchUserItems());

		if (CollectionUtils.isEmpty(searchUserItems)) {

			return new PageableSearchResponse<User>(new ArrayList<User>(), new PageRequest(0, MAX_SUMMON_SUGGEST_USER_FETCH_SIZE), 0);
		}

		if (searchUserItems.size() > MAX_SUMMON_SUGGEST_USER_FETCH_SIZE) {
			searchUserItems = searchUserItems.subList(0, MAX_SUMMON_SUGGEST_USER_FETCH_SIZE);
		}

		List<User> foundUsers = userService.findByUserHashesOrderByUserHashField(extract(searchUserItems, on(SearchUserItem.class).getUserHash()));

		if (CollectionUtils.isNotEmpty(foundUsers)) {

			fillHighlightNameAndTags(searchUserItems, foundUsers);

			forEach(foundUsers).simplifyForList();
		}

		return new PageableSearchResponse<User>(foundUsers, new PageRequest(0, MAX_SUMMON_SUGGEST_USER_FETCH_SIZE), foundUsers.size());
	}

	@Override
	public PageableSearchResponse<User> findNaverCafeUsers(long cafeId, String query, SearchFixType searchFixType,
			Pageable pageable) {

		SearchUserMessage searchUserMessage = findNaverCafeUser(query, searchFixType, null, pageable);
		List<SearchUserItem> searchUserItems = searchUserMessage.getSearchUserResult().getSearchUserItemList().getSearchUserItems();

		if (CollectionUtils.isEmpty(searchUserItems)) {

			return new PageableSearchResponse<User>(new ArrayList<User>(), pageable, 0);
		}

		List<User> foundUsers = userService.findByUserHashesOrderByUserHashField(extract(searchUserItems, on(SearchUserItem.class).getUserHash()));

		if (CollectionUtils.isNotEmpty(foundUsers)) {

			if (cafeId > 0L) {
				cafeMembershipService.applyJoined(cafeId, foundUsers);
			}

			fillHighlightNameAndTags(searchUserItems, foundUsers);

			forEach(foundUsers).simplifyForList();
		}

		return new PageableSearchResponse<User>(foundUsers, pageable, Long.parseLong(searchUserMessage.getSearchUserResult().getTotalCount()));
	}

	private void fillHighlightNameAndTags(List<SearchUserItem> searchUserItems, List<User> foundUsers) {

		for (User user : foundUsers) {

			for (SearchUserItem item : searchUserItems) {

				if (user.getUserHash().equals(item.getUserHash())) {
					user.setHighlightName(item.getName());
					user.setHighlightTags(SearchUserMessage.HIGHLIGHT_TAGS);
					break;
				}
			}
		}
	}

	@Override
	public PageableSearchResponse<ApprovalHistory> findApprovalHistories(long cafeId, String query,
			ApprovalHistoryType approvalHistoryType,
			CafeMembershipSortType cafeMembershipSortType, SearchFixType searchFixType, Pageable pageable) {

		List<ApprovalHistory> approvalHistories = new ArrayList<ApprovalHistory>();

		Cafe foundCafe = cafeService.findOrExceptionAndStatusCheck(cafeId);

		SearchApprovalHistoryMessage searchApprovalHistoryMessage = searchApprovalHistories(foundCafe, query, approvalHistoryType, cafeMembershipSortType, searchFixType, pageable);
		List<SearchApprovalHistoryItem> searchApprovalHistoryItems = searchApprovalHistoryMessage.getSearchApprovalHistoryResult().getSearchApprovalHistoryItemList().getSearchApprovalHistoryItems();

		if (CollectionUtils.isEmpty(searchApprovalHistoryItems)) {

			return new PageableSearchResponse<ApprovalHistory>(approvalHistories, pageable, 0);
		}

		List<User> foundUsers = userService.findByUserHashesOrderByUserHashField(extract(searchApprovalHistoryItems, on(SearchApprovalHistoryItem.class).getUserHash()));

		if (CollectionUtils.isNotEmpty(foundUsers)) {

			for (SearchApprovalHistoryItem item : searchApprovalHistoryItems) {

				for (User user : foundUsers) {

					if (item.getUserHash().equals(user.getUserHash())) {

						user.setHighlightName(item.getUserName());
						user.setHighlightTags(SearchUserMessage.HIGHLIGHT_TAGS);
						user.simplifyForList();

						approvalHistories.add(item.convertToApprovalHistory(user));

						break;
					}
				}
			}

		}

		return new PageableSearchResponse<ApprovalHistory>(approvalHistories, pageable, Long.parseLong(searchApprovalHistoryMessage.getSearchApprovalHistoryResult().getTotalCount()));
	}

	private SearchApprovalHistoryMessage searchApprovalHistories(Cafe foundCafe, String query,
			ApprovalHistoryType approvalHistoryType,
			CafeMembershipSortType cafeMembershipSortType, SearchFixType searchFixType, Pageable pageable) {

		String cacheKey = makeApprovalHistoriesCacheKey(foundCafe, query, approvalHistoryType, cafeMembershipSortType, searchFixType, pageable);

		return searchCacheClient.findApprovalHistories(foundCafe.getId(), query, approvalHistoryType, cafeMembershipSortType, searchFixType, pageable, cacheKey);
	}

	@Override
	public PageableSearchResponse<CafeMembership> findCafeMembers(String query, long cafeId,
			SearchFixType searchFixType, CafeMembershipSortType cafeMemberSortType,
			CafeMembershipType cafeMemberShipType,
			Pageable pageable) {

		Cafe foundCafe = cafeNoTransactionService.findOrExceptionFromCache(cafeId);

		SearchUserMessage searchUserMessage = searchCafeMember(query, searchFixType, foundCafe, null, cafeMemberSortType, cafeMemberShipType, pageable);
		List<SearchUserItem> searchUserItems = searchUserMessage.getSearchUserResult().getSearchUserItemList().getSearchUserItems();

		if (CollectionUtils.isEmpty(searchUserItems)) {

			return new PageableSearchResponse<CafeMembership>(new ArrayList<CafeMembership>(), pageable, 0);
		}

		List<CafeMembership> foundCafeMembers = cafeMembershipService.findByUserHashesOrderByUserHashField(foundCafe.getId(), extract(searchUserItems, on(SearchUserItem.class).getUserHash()));

		if (searchUserItems.size() != foundCafeMembers.size()) {
			LOGGER.info("foundCafeMembers Size :  - " + foundCafeMembers.size() + " searchUserItems Size :  - "
				+ searchUserItems.size());
		}

		for (CafeMembership cafeMembership : foundCafeMembers) {

			for (SearchUserItem searchUserItem : searchUserItems) {

				if (cafeMembership.getUser().getUserHash().equals(searchUserItem.getUserHash())) {
					cafeMembership.getUser().setHighlightName(searchUserItem.getName());
					cafeMembership.getUser().setHighlightTags(SearchUserMessage.HIGHLIGHT_TAGS);
					break;
				}
			}
		}

		return new PageableSearchResponse<CafeMembership>(foundCafeMembers, pageable, Long.parseLong(searchUserMessage.getSearchUserResult().getTotalCount()));

	}

	@Override
	public PageableSearchResponse<Cafe> findCafes(User user, String query, List<LanguageCode> languageCodes,
			Pageable pageable, String acceptLanguage, String userAgent) {

		boolean isAndoridJapan = StringUtils.containsIgnoreCase(userAgent, "android")
			&& StringUtils.containsIgnoreCase(userAgent, SEARCH_FORBIDDEN_LIMIT_USER_ACCEPT_LANGUAGE);

		if ((StringUtils.containsIgnoreCase(acceptLanguage, SEARCH_FORBIDDEN_LIMIT_USER_ACCEPT_LANGUAGE) || isAndoridJapan)
			&& searchForbiddenWordService.isValideSearchWord(query) == false) {

			LOGGER.info("### matched SearchForbiddenWord query - " + query + " language : " + acceptLanguage + " ua : "
				+ userAgent);

			/* XXX 다음 배포때 QA 하고 풀 예정
			if (isForbiddenKeywordResponseException(userAgent)) {
				throw SearchException.searchForbiddenKeyword();
			}
			*/

			return new PageableSearchResponse<Cafe>(new ArrayList<Cafe>(), pageable, 0);
		}

		String cacheKey = makeFindCafeCacheKey(query, languageCodes, pageable);

		SearchCafeMessage searchCafeMessage = searchCacheClient.findCafesByNameAndDescription(query, languageCodes, pageable, cacheKey);

		List<Cafe> cafes = searchCafeMessageMergeToDbCafe(searchCafeMessage);

		for (Cafe cafe : cafes) {
			cafe.simplifyForCategoryCafes();
		}

		return new PageableSearchResponse<Cafe>(cafes, pageable, Long.parseLong(searchCafeMessage.getSearchResult().getTotalCount()));
	}

	@SuppressWarnings("unused")
	private boolean isForbiddenKeywordResponseException(String userAgent) {

		if (StringUtils.isBlank(userAgent)) {
			return true;
		}

		if (userAgent.toLowerCase().contains("cafe") == false) {
			return true;
		}

		/* 안드로이드 2.1.5 이상만 exception */
		if (userAgent.toLowerCase().contains("android")) {

			if (refererService.compareVersion(userAgent, new int[]{2, 1, 5}) < 0) {
				return false;
			}
			return true;
		}

		/* 아이폰 2.1.2 이상만 exception */
		if (userAgent.toLowerCase().contains("iphone")) {

			if (refererService.compareVersion(userAgent, new int[]{2, 1, 2}) < 0) {
				return false;
			}
			return true;
		}

		return true;
	}

	@Override
	public MoreableResponse<Cafe> findHirobaNewCafes(User user, List<LanguageCode> languageCodes) {

		String cacheKey = makeHirobaNewCacheKey(languageCodes);

		SearchCafeMessage searchCafeMessage = searchCacheClient.findHirobaNewCafes(languageCodes, 0, MAX_HIROBA_CAFES_FETCH_SIZE, cacheKey);

		List<Cafe> cafes = searchCafeMessageMergeToDbCafesWithNameAndDescription(user, searchCafeMessage);

		List<Cafe> applyExceptDecrementCafes = pIDecrementCafeService.applyExceptDecrementCafes(cafes);

		List<Cafe> excludePrivateAndBlindCafes = Cafe.excludePrivateAndBlindCafes(applyExceptDecrementCafes);

		for (Cafe cafe : excludePrivateAndBlindCafes) {
			cafe.simplifyForBoardPostList();
		}

		return new MoreableResponse<Cafe>(excludePrivateAndBlindCafes);
	}

	private String makeHirobaNewCacheKey(List<LanguageCode> languageCodes) {
		return CacheUtils.makeCacheKeyFromVarArg(StringUtils.join(languageCodes, CacheUtils.CACHE_WORD_SPLIT_CHAR)
							+ MAX_HIROBA_CAFES_FETCH_SIZE);
	}

	@Override
	public MoreableResponse<Cafe> findHirobaHotCafes(User user, List<LanguageCode> languageCodes) {

		String cacheKey = makeHirobaHotCafesCacheKey(languageCodes);

		SearchCafeMessage searchCafeMessage = searchCacheClient.findHirobaHotCafes(languageCodes, 0, MAX_HIROBA_CAFES_FETCH_SIZE, cacheKey);

		List<Cafe> cafes = searchCafeMessageMergeToDbCafesWithNameAndDescription(user, searchCafeMessage);

		List<Cafe> applyExceptDecrementCafes = pIDecrementCafeService.applyExceptDecrementCafes(cafes);

		List<Cafe> excludePrivateAndBlindCafes = Cafe.excludePrivateAndBlindCafes(applyExceptDecrementCafes);

		for (Cafe cafe : excludePrivateAndBlindCafes) {
			cafe.simplifyForBoardPostList();
		}

		return new MoreableResponse<Cafe>(excludePrivateAndBlindCafes);
	}

	@Override
	public PageableResponse<Cafe> findOsusumeCafesAsPageableResponse(List<LanguageCode> languageCodes,
			Pageable pageable) {

		pageable = fixPageableParameter(pageable, MAX_OSUSUME_PAGE_SIZE);

		String cacheKey = makeOsusumeCafesCacheKey(languageCodes, pageable);

		SearchCafeMessage searchCafeMessage = searchCacheClient.findOsusumeCafes(languageCodes, pageable, cacheKey);

		List<Cafe> cafes = searchCafeMessageMergeToDbCafesWithNameAndDescription(searchCafeMessage);

		simplifyForHirobaCafes(cafes);

		return new PageableSearchResponse<Cafe>(cafes, pageable, Long.parseLong(searchCafeMessage.getSearchResult().getTotalCount()));
	}

	private void simplifyForHirobaCafes(List<Cafe> cafes) {

		for (Cafe cafe : cafes) {
			cafe.simplifyForHirobaCafes();
		}
	}

	@Override
	public List<Cafe> findOsusumeCafes(List<LanguageCode> languageCodes, Pageable pageable) {

		pageable = fixPageableParameter(pageable, MAX_OSUSUME_PAGE_SIZE);

		String cacheKey = makeOsusumeCafesCacheKey(languageCodes, pageable);

		SearchCafeMessage searchCafeMessage = searchCacheClient.findOsusumeCafes(languageCodes, pageable, cacheKey);

		return searchCafeMessage.convertToCafes();
	}

	private Pageable fixPageableParameter(Pageable pageable, int maxFetchSize) {

		if (pageable.getPageSize() > maxFetchSize) {
			return new PageRequest(pageable.getPageNumber(), maxFetchSize);
		}

		return pageable;
	}

	@Override
	public PageableResponse<Cafe> findCafesByCategory(User user, int categoryId, List<LanguageCode> languageCodes,
			Pageable pageable) {

		pageable = fixPageableParameter(pageable, MAX_CATEGORY_CAFE_PAGE_SIZE);

		SearchCafeMessage searchCafeMessage = null;

		String cacheKey = makeFindByCategoryCacheKey(categoryId, languageCodes, pageable);

		if (categoryId > 0) {

			Category foundCategory = categoryService.findOrException(categoryId);

			searchCafeMessage = searchCacheClient.findCafesByCategory(languageCodes, foundCategory.getId(), pageable, cacheKey);

		} else {

			searchCafeMessage = searchCacheClient.findCafesOrderByNew(languageCodes, pageable, cacheKey);
		}

		List<Cafe> cafes = searchCafeMessageMergeToDbCafesWithNameAndDescription(searchCafeMessage);

		simplifyForHirobaCafes(cafes);

		return new PageableResponse<Cafe>(cafes, pageable, Long.parseLong(searchCafeMessage.getSearchResult().getTotalCount()));
	}

	List<Cafe> searchCafeMessageMergeToDbCafesWithNameAndDescription(SearchCafeMessage searchCafeMessage) {
		return searchCafeMessageMergeToDbCafesWithNameAndDescription(User.unauthenticatedUser(), searchCafeMessage);
	}

	List<Cafe> searchCafeMessageMergeToDbCafe(SearchCafeMessage searchCafeMessage) {
		return searchCafeMessageMergeToDbCafe(User.unauthenticatedUser(), searchCafeMessage);
	}

	List<Cafe> searchCafeMessageMergeToDbCafe(User user, SearchCafeMessage searchCafeMessage) {

		List<Long> cafeIds = prepareForMerge(searchCafeMessage);

		if (CollectionUtils.isNotEmpty(cafeIds)) {

			String cacheKey = makeFetchCafesCacheKey(user, cafeIds);

			List<Cafe> foundCafes = cafeNoTransactionService.fetchCafeAdditionalAndSortAndUserJoinFlagByCafeIdsWithCache(user, cafeIds, cacheKey);

			return searchCafeMessage.convertToCafes(foundCafes);
		}

		return Collections.emptyList();
	}

	List<Cafe> searchCafeMessageMergeToDbCafesWithNameAndDescription(User user, SearchCafeMessage searchCafeMessage) {

		List<Long> cafeIds = prepareForMerge(searchCafeMessage);

		if (CollectionUtils.isNotEmpty(cafeIds)) {

			String cacheKey = makeFetchCafesCacheKey(user, cafeIds);

			List<Cafe> foundCafes = cafeNoTransactionService.fetchCafeAdditionalAndSortAndUserJoinFlagByCafeIdsWithCache(user, cafeIds, cacheKey);

			return searchCafeMessage.convertToDbCafesWithNameAndDescription(foundCafes);
		}

		return Collections.emptyList();
	}

	private List<Long> prepareForMerge(SearchCafeMessage searchCafeMessage) {

		List<SearchCafeItem> searchCafeResult = searchCafeMessage.getSearchResult().getSearchCafeItemList().getItems();

		if (CollectionUtils.isEmpty(searchCafeResult)) {
			return Collections.emptyList();
		}

		return SearchCafeItem.extractCafeIds(searchCafeResult);
	}

	private SearchUserMessage findNaverCafeUser(String query, SearchFixType searchFixType,
			Set<String> postActionUserHashSet, Pageable pageable) {

		String cacheKey = makeNaverCafeUserCacheKey(query, searchFixType, postActionUserHashSet, pageable);

		return searchCacheClient.findNaverCafeUser(query
													, postActionUserHashSet
													, searchFixType
													, pageable, cacheKey
													);
	}

	String makeNaverCafeUserCacheKey(String query, SearchFixType searchFixType,
			Set<String> postActionUserHashSet,
			Pageable pageable) {

		return CacheUtils.makeCacheKeyFromVarArg(query
									, StringUtils.join(postActionUserHashSet, CacheUtils.CACHE_WORD_SPLIT_CHAR)
									, searchFixType.getCode()
									, pageable.getPageNumber()
									, pageable.getPageSize()
									);
	}

	String makeCafeMemberCacheKey(String query, SearchFixType searchFixType, Cafe foundCafe,
			Set<String> postActionUserHashSet, CafeMembershipSortType cafeMembershipSortType,
			CafeMembershipType cafeMembershipType, Pageable pageable) {

		return CacheUtils.makeCacheKeyFromVarArg(foundCafe.getId()
									, query
									, StringUtils.join(postActionUserHashSet, CacheUtils.CACHE_WORD_SPLIT_CHAR)
									, cafeMembershipSortType.getCode()
									, cafeMembershipType.getCode()
									, searchFixType.getCode()
									, pageable.getPageNumber()
									, pageable.getPageSize()
									);
	}

	String makeSummonSuggestPostActionUserCacheKey(String query, SearchFixType searchFixType,
			Set<String> postActionUserHashSet) {

		return CacheUtils.makeCacheKeyFromVarArg(query
												, StringUtils.join(postActionUserHashSet, CacheUtils.CACHE_WORD_SPLIT_CHAR)
												, searchFixType.getCode()
												);
	}

	private String makeApprovalHistoriesCacheKey(Cafe foundCafe, String query,
			ApprovalHistoryType approvalHistoryType,
			CafeMembershipSortType cafeMembershipSortType, SearchFixType searchFixType, Pageable pageable) {

		return CacheUtils.makeCacheKeyFromVarArg(foundCafe.getId()
												, query
												, approvalHistoryType.getCode()
												, cafeMembershipSortType.getCode()
												, searchFixType.getCode()
												, pageable.getPageNumber()
												, pageable.getPageSize()
												);
	}

	private String makeFindCafeCacheKey(String query, List<LanguageCode> languageCodes, Pageable pageable) {
		return CacheUtils.makeCacheKeyFromVarArg(query
												, StringUtils.join(languageCodes, CacheUtils.CACHE_WORD_SPLIT_CHAR)
												, pageable.getPageNumber()
												, pageable.getPageSize());
	}

	private String makeHirobaHotCafesCacheKey(List<LanguageCode> languageCodes) {
		return CacheUtils.makeCacheKeyFromVarArg(StringUtils.join(languageCodes, CacheUtils.CACHE_WORD_SPLIT_CHAR)
												, MAX_HIROBA_CAFES_FETCH_SIZE
												);
	}

	private String makeOsusumeCafesCacheKey(List<LanguageCode> languageCodes, Pageable pageable) {
		return CacheUtils.makeCacheKeyFromVarArg(StringUtils.join(languageCodes, CacheUtils.CACHE_WORD_SPLIT_CHAR)
												, pageable.getPageNumber()
												, pageable.getPageSize()
												);
	}

	private String makeFindByCategoryCacheKey(int categoryId, List<LanguageCode> languageCodes, Pageable pageable) {

		return CacheUtils.makeCacheKeyFromVarArg(StringUtils.join(languageCodes, CacheUtils.CACHE_WORD_SPLIT_CHAR)
												, categoryId
												, pageable.getPageNumber()
												, pageable.getPageSize()
												);
	}

	private String makeFetchCafesCacheKey(User user, List<Long> cafeIds) {
		return CacheUtils.makeCacheKeyFromVarArg(user.getUserHash()
			+ StringUtils.join(cafeIds, CacheUtils.CACHE_WORD_SPLIT_CHAR));
	}

}
