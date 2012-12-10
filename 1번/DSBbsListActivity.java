package com.hangame.publishing.etc;

import static com.hangame.publishing.etc.constant.Constants.KEY_COMMENT_COUNT;
import static com.hangame.publishing.etc.constant.Constants.KEY_READ_COUNT;
import static com.hangame.publishing.etc.constant.Constants.KEY_SUMMARY;
import static com.hangame.publishing.etc.constant.Constants.KEY_THUMBNAIL_URL;
import static com.hangame.publishing.etc.constant.Constants.KEY_TITLE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hangame.publishing.etc.constant.Constants;
import com.hangame.publishing.etc.util.XMLParser;
import com.hangame.publishing.etc.R;

public class DSBbsListActivity extends ListActivity {

	private String xmlData = "";
	String url = "http://ds.hangame.com/xbbs/api/list.nhn";
	List<Map<String, String>> menuItems = null;
	XMLParser parser = null;

	// path view 따라하기
	private ListView list;
	private SimpleAdapter adapter;

	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Splash
		// startActivity(new Intent(this, SplashActivity.class));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pathlist);
		parser = new XMLParser();

		list = (ListView) findViewById(android.R.id.list);
		progressBar = (ProgressBar) findViewById(R.id.progressbar);

		new HttpRequestTask().execute();

		// postDataUsingXmlParser(url);

		/*
		 * Log.d("noir", menuItems.toString()); // Adding menuItems to ListView
		 * 
		 * String[] keyArray = new String[] { KEY_NO, KEY_TITLE, KEY_SUMMARY,
		 * KEY_COMMENT_COUNT, KEY_READ_COUNT, KEY_REGIST_DATE, KEY_THUMBNAIL_URL
		 * }; int[] idArray = new int[] { R.id.tv_bbs_no, R.id.tv_bbs_title,
		 * R.id.tv_bbs_summary, R.id.tv_bbs_comment_count,
		 * R.id.tv_bbs_read_count, R.id.tv_bbs_register_date,
		 * R.id.tv_bbs_thumbnail_url };
		 * 
		 * ListAdapter adapter = new SimpleAdapter(this, menuItems,
		 * R.layout.ds_bbs_rows, keyArray, idArray);
		 * 
		 * setListAdapter(adapter);
		 */

	}

	private void postDataUsingXmlParser(String url) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("bbsId", "K_DSTRIKER_FREE"));

		String xml = parser.getXmlFromUrl(url, nameValuePairs); // getting XML
		makeXml(xml);

	}

	private void makeXml(String xml) {
		Document doc = parser.getDomElement(xml); // getting DOM element

		NodeList nl = doc.getElementsByTagName(Constants.KEY_ARTICLE);

		menuItems = new ArrayList<Map<String, String>>();
		// looping through all item nodes <item>
		for (int i = 0; i < nl.getLength(); i++) {
			// creating new HashMap
			HashMap<String, String> map = new HashMap<String, String>();
			Element e = (Element) nl.item(i);
			// adding each child node to HashMap key => value
			// map.put(KEY_NO, parser.getValue(e, KEY_NO));
			String title = parser.getText(e, KEY_TITLE);
			map.put(KEY_TITLE, (title.length() > 12 ? title.substring(0, 12)
					+ "..." : title)
					+ "(" + parser.getValue(e, KEY_COMMENT_COUNT) + ")");
			map.put(KEY_SUMMARY, parser.getText(e, KEY_SUMMARY));
			map.put(KEY_COMMENT_COUNT, parser.getValue(e, KEY_COMMENT_COUNT));
			map.put(KEY_READ_COUNT, parser.getValue(e, KEY_COMMENT_COUNT));
			// map.put(KEY_REGIST_DATE, parser.getText(e, KEY_REGIST_DATE));
			map.put(KEY_THUMBNAIL_URL, parser.getText(e, KEY_THUMBNAIL_URL));

			// adding HashList to ArrayList
			menuItems.add(map);
			Log.d("noir", map.toString());
		}
	}

	private void postData(String url) {
		// Create a new HttpClient and Post Header
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
				HttpVersion.HTTP_1_1);

		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);

		HttpClient httpClient = new DefaultHttpClient(params);
		HttpPost httpPost = new HttpPost(url);
		HttpResponse response = null;

		try {
			// add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("bbsId",
					"K_DSTRIKER_FREE"));
			nameValuePairs.add(new BasicNameValuePair("pageSize", "20"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			response = httpClient.execute(httpPost);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// response
		InputStream is = null;
		String result = "";

		try {
			if (response != null && response.getEntity() != null) {
				HttpEntity entityResponse = response.getEntity();
				xmlData = EntityUtils.toString(entityResponse);
			} else {
				Log.d("noir", "no result");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		makeXml(xmlData);

	}

	private class HttpRequestTask extends AsyncTask<Void, String, Void> {
		int progress;

		@Override
		protected void onPreExecute() {
			progress = 0;
		}

		// 상속받은 클래스를 사용하려면 반드시 구현해야 하는 메소드로 지정된
		// 작업을 마칠때까지 필요한 시간동안 계속해서 실행된다. 끝나지 않고 무한정
		// 실행해야 하는 작업은 AsyncTask로 실행하기에 적당하지 않다
		@Override
		protected Void doInBackground(Void... unused) {
			postData(url);

			while (progress < 100) {
				progress += 10;
				publishProgress(String.valueOf(progress));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return (null);
		}

		// doInBackground() 메소드에서 publishProgress() 메소드를 호출하면
		// onProgressUpdate() 메소드에서 인자로 넘겨준 값을 받게된다
		// (사용자 인터페이스에서 실행되므로 주의)
		@Override
		protected void onProgressUpdate(String... item) {
			// ((ArrayAdapter)getListAdapter()).add(item[0]);
			progressBar.setProgress(Integer.parseInt(item[0]));
		}

		// doInBackground() 메소드의 작업이 완료된 직후 호출됨
		@Override
		protected void onPostExecute(Void unused) {
			progressBar.setVisibility(View.INVISIBLE);
			Toast.makeText(DSBbsListActivity.this, "Done!", Toast.LENGTH_SHORT)
					.show();

			// Path 아답타
			String[] keyArray = { KEY_TITLE, KEY_SUMMARY };
			int[] idArray = { R.id.title, R.id.content };

			adapter = new SimpleAdapter(DSBbsListActivity.this, menuItems,
					R.layout.path_list_item, keyArray, idArray);
			list.setAdapter(adapter);

			// selecting single ListView item
			// ListView lv = getListView();
			// listening to single listitem click
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String content = ((TextView) view
							.findViewById(R.id.content)).getText().toString();
					Toast.makeText(DSBbsListActivity.this,
							"Content:" + content, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
