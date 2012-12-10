package jp.naver.cafe.android.activity.applink;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import jp.naver.android.commons.nstat.NstatFactory;
import jp.naver.cafe.R;
import jp.naver.cafe.android.ConstFields;
import jp.naver.cafe.android.activity.cafe.CafePostListActivity;
import jp.naver.cafe.android.activity.post.PostDetailActivity;
import jp.naver.cafe.android.activity.user.UserInfoActivity;
import jp.naver.cafe.android.api.model.board.BoardModel;
import jp.naver.cafe.android.api.model.cafe.CafeItemModel;
import jp.naver.cafe.android.lang.NaverCafeStringUtils;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.webkit.URLUtil;

/**
 * Cafe Post End Page에서 Link 클릭시 InApp으로 연결.
 */
public class LinkInAppActivity extends Activity {
	private static final String NCLICK_AREA_CODE_PST_MAI = "pst_mai";
	private static final String NCLICK_AREA_CODE_PST_FDB = "pst_fdb";
	private static final String NCLICK_ITEM_CODE_BODYLINK = "bodylink";
	private static final String NCLICK_ITEM_CODE_SUMMONEDUSER = "summoneduser";

	private static final String STR_CAFE_URL = ConstFields.SERVICE_URL + "/";
	private static final String STR_PARAM_USER = "user/";
	private static final String STR_PARAM_CAFES = "cafes/";
	private static final String STR_PARAM_HIROBA = "hiroba/";
	private static final String STR_PARAM_MY = "my/";
	private static final String STR_PARAM_POSTS_CAFE_ID = "/posts?cafeId=";
	private static final String STR_PARAM_POSTS = "/posts";

	private static final int NONE_STRING = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inapplink_page);

		String dataUrl = getIntent().getDataString();
		if (NaverCafeStringUtils.isEmpty(dataUrl)) {
			return;
		}

		int scheme = dataUrl.indexOf(ConstFields.VALUE_STRING_IN_APP_LINK_SCHEME);
		if (scheme != NONE_STRING) {
			dataUrl = dataUrl.substring(scheme + ConstFields.VALUE_STRING_IN_APP_LINK_SCHEME.length(), dataUrl.length());
		}

		if (dataUrl.startsWith("@") || dataUrl.startsWith("＠")) { // 유저소환
			NstatFactory.click(NCLICK_AREA_CODE_PST_FDB, NCLICK_ITEM_CODE_SUMMONEDUSER);
			Intent intent = new Intent(this, UserInfoActivity.class);
			String name = dataUrl.substring(1, dataUrl.length());
			try {
				name = URLDecoder.decode(name, "utf-8");
				name = URLEncoder.encode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			intent.putExtra(ConstFields.KEY_USER_NAME, name);
			startActivity(intent);
			finish();
			return;
		}

		NstatFactory.click(NCLICK_AREA_CODE_PST_MAI, NCLICK_ITEM_CODE_BODYLINK);
		if (dataUrl.startsWith(STR_CAFE_URL)) {
			// URL 주소에 쓰레기 값 제거
			if (dataUrl.contains("#")) {
				dataUrl = dataUrl.substring(0, dataUrl.indexOf("#"));
			}
			if (dataUrl.endsWith("/")) {
				dataUrl = dataUrl.substring(0, dataUrl.length() - 1);
			}

			// 카페 URL이 http://cafe.naver.jp/ 상태로 끝난경우 이에 대한 처리.
			if (STR_CAFE_URL.length() > dataUrl.length()) {
				startBrowser(dataUrl);
				finish();
				return;
			}

			String linkUrl = dataUrl.substring(STR_CAFE_URL.length(), dataUrl.length());
			if (isNotLinkParam(linkUrl)) {
				startBrowser(dataUrl);
			} else if (linkUrl.startsWith(STR_PARAM_USER)) {
				Intent intent = new Intent(this, UserInfoActivity.class);
				String name = null;
				long cafeId = 0L;
				linkUrl = linkUrl.substring(STR_PARAM_USER.length(), linkUrl.length());
				name = linkUrl.substring(0, linkUrl.indexOf("/") == -1 ? linkUrl.length() : linkUrl.indexOf("/"));
				linkUrl = linkUrl.substring(name.length(), linkUrl.length());
				try {
					name = URLDecoder.decode(name, "utf-8");
					name = URLEncoder.encode(name, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				intent.putExtra(ConstFields.KEY_USER_NAME, name);
				if (linkUrl.startsWith(STR_PARAM_POSTS_CAFE_ID)) {
					cafeId = Long.valueOf(linkUrl.substring(linkUrl.indexOf("=") + 1, linkUrl.length()));
					intent.putExtra(ConstFields.KEY_CAFE_ID, cafeId);
				}
				startActivity(intent);
			} else {
				String cafeUrl = linkUrl.substring(0, (linkUrl.indexOf("/") == -1 ? linkUrl.length()
					: linkUrl.indexOf("/")));
				linkUrl = linkUrl.substring(cafeUrl.length(), linkUrl.length());
				try {
					cafeUrl = URLDecoder.decode(cafeUrl, "utf-8");
					cafeUrl = URLEncoder.encode(cafeUrl, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				cafeUrl = cafeUrl.replaceAll("[+]", "%20");
				Intent intent;
				if (linkUrl.indexOf("/") == NONE_STRING) {
					intent = new Intent(this, CafePostListActivity.class);
					CafeItemModel cafe = new CafeItemModel();
					cafe.setUrl(cafeUrl);
					intent.putExtra(ConstFields.KEY_CAFE, (Parcelable)cafe);
				} else if (linkUrl.indexOf(STR_PARAM_POSTS) != NONE_STRING) { // boardId가 있는 경우 boardId 추출
					intent = new Intent(this, CafePostListActivity.class);
					long boardId = Long.valueOf(linkUrl.substring(linkUrl.indexOf("/") + 1, linkUrl.indexOf(STR_PARAM_POSTS)));
					CafeItemModel cafe = new CafeItemModel();
					cafe.setUrl(cafeUrl);
					BoardModel board = new BoardModel();
					board.setId(boardId);
					intent.putExtra(ConstFields.KEY_CAFE, (Parcelable)cafe);
					intent.putExtra(ConstFields.KEY_SELECTED_BOARD, (Parcelable)board);
				} else { // postId 추출
					intent = new Intent(this, PostDetailActivity.class);
					long postId;
					try {
						postId = Long.valueOf(linkUrl.substring(linkUrl.indexOf("/") + 1, linkUrl.length()));
						intent.putExtra(ConstFields.KEY_POST_ID, postId);
						intent.putExtra(ConstFields.KEY_CAFE_URL, cafeUrl);
					} catch (NumberFormatException e) {
						// Malformed URL : http://cafe2.beta.naver.jp//I%20love%20Jang%20Keun-Suk
						startBrowser(dataUrl);
						finish();
						return;
					}
				}

				startActivity(intent);
			}

		} else {
			startBrowser(dataUrl);
		}
		finish();
	}

	private void startBrowser(String url) {

		if (URLUtil.isHttpUrl(url) && !url.startsWith(ConstFields.VALUE_STRING_URL_HTTP_SCHEME)) {
			url = ConstFields.VALUE_STRING_URL_HTTP_SCHEME
				+ url.substring(ConstFields.VALUE_STRING_URL_HTTP_SCHEME.length(), url.length());
		} else if (URLUtil.isHttpsUrl(url)) {
			url = ConstFields.VALUE_STRING_URL_HTTPS_SCHEME
				+ url.substring(ConstFields.VALUE_STRING_URL_HTTPS_SCHEME.length(), url.length());
		}

		if (!url.startsWith(ConstFields.VALUE_STRING_URL_HTTP_SCHEME) &&
			!url.startsWith(ConstFields.VALUE_STRING_URL_HTTPS_SCHEME)) {
			url = ConstFields.VALUE_STRING_URL_HTTP_SCHEME + url;
		}

		Uri uri = Uri.parse(url);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(uri);
		startActivity(intent);
	}

	private boolean isNotLinkParam(String data) {
		return data.startsWith(STR_PARAM_HIROBA) || data.startsWith(STR_PARAM_CAFES) || data.startsWith(STR_PARAM_MY);
	}
}
