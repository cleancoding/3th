package com.nhn.android.blog.setting.theme;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nhn.android.blog.BaseActivity;
import com.nhn.android.blog.BlogRequestCode;
import com.nhn.android.blog.R;
import com.nhn.android.blog.SplashActivity;
import com.nhn.android.blog.theme.BlogTheme;
import com.nhn.android.blog.theme.ThemeInfo;

public class ThemeListSettingsActivity extends BaseActivity {
	private BlogTheme blogTheme = BlogTheme.getInstance();
	private List<ThemeInfo> themeList = new ArrayList<ThemeInfo>();

	private ImageView bannerImage;
	private Button defaultThemeApplyButton;
	private Button defaultThemeDelButton;
	private ThemeRowAdapter adapter;
	private ListView themeListView;
	private int checkedPosition = -1;

	private String toDeletePackageName = "";
	private ThemeInfo currentTheme;

	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_theme_settings_list);
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View bannerView = inflater.inflate(R.layout.layout_theme_settings_banner, null);
		View defaultThemeView = inflater.inflate(R.layout.layout_theme_settings_list_header, null);
		
		Button btnDone = (Button)findViewById(R.id.button_done);
		btnDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		themeListView = (ListView)findViewById(R.id.theme_list);
		defaultThemeApplyButton = (Button)defaultThemeView.findViewById(R.id.btn_theme_apply);
		defaultThemeDelButton = (Button)defaultThemeView.findViewById(R.id.btn_theme_del);
		TextView defaultThemeVersion = (TextView)defaultThemeView.findViewById(R.id.tv_theme_version);
		bannerImage = (ImageView)bannerView.findViewById(R.id.theme_banner);
		
		themeList = blogTheme.findInstalledThemeInfoList();
		checkedPosition = findCheckedList();
		adapter = new ThemeRowAdapter();
		themeListView.addHeaderView(defaultThemeView);
		themeListView.addFooterView(bannerView);
		themeListView.setAdapter(adapter);
		
		defaultThemeDelButton.setVisibility(View.GONE);
		defaultThemeVersion.setText(findThisAppVersion());

		ThemeInfo currentTheme = blogTheme.findCurrentTheme(getApplicationContext());
		if (StringUtils.isBlank(currentTheme.getPackageName())) {
			defaultThemeApplyButton.setSelected(true);
			defaultThemeApplyButton.setEnabled(false);
			Drawable checkIcon = getResources().getDrawable(R.drawable.icon_check);
			checkIcon.setBounds(0, 0, checkIcon.getIntrinsicWidth(), checkIcon.getIntrinsicHeight());
			defaultThemeApplyButton.setCompoundDrawables(checkIcon, null, null, null);
			defaultThemeApplyButton.setText("적용됨");
		}

		defaultThemeApplyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				checkedPosition = -1;

				adapter.notifyDataSetChanged();

				defaultThemeApplyButton.setSelected(true);
				defaultThemeApplyButton.setEnabled(false);
				Drawable checkIcon = getResources().getDrawable(R.drawable.icon_check);
				checkIcon.setBounds(0, 0, checkIcon.getIntrinsicWidth(), checkIcon.getIntrinsicHeight());
				defaultThemeApplyButton.setCompoundDrawables(checkIcon, null, null, null);
				defaultThemeApplyButton.setText("적용됨");

				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				}, 300);
			}
		});
		
		if (themeList.size() == 2) {
			bannerImage.setVisibility(View.GONE);
		}

		applyTheme();
	}

	private void applyTheme() {
		blogTheme.init(this.getApplicationContext());
		blogTheme.replaceTextViewColor("theme_postwrite_header", (TextView)findViewById(R.id.setting_theme_list_title));
		blogTheme.replaceBackgroundColor("theme_postwrite_headerline",
			(ImageView)findViewById(R.id.setting_theme_headerline));
	}

	private String findThisAppVersion() {
		try {
			return getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(),
				0).versionName;
		} catch (Exception e) {
			return "1.0";
		}
	}

	private int findCheckedList() {
		int i = 0;
		for (ThemeInfo themeInfo : themeList) {
			if (StringUtils.equals(themeInfo.getPackageName(),
				blogTheme.findCurrentTheme(getApplicationContext()).getPackageName())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	class ThemeRowAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if (themeList == null) {
				return 0;
			}
			return themeList.size();
		}

		@Override
		public Object getItem(int position) {
			return themeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater factory = LayoutInflater.from(ThemeListSettingsActivity.this);
				convertView = (LinearLayout)factory.inflate(R.layout.layout_theme_settings_row, null);
			}
			TextView tvPackageTitle = (TextView)convertView.findViewById(R.id.tv_theme_name);
			Button applyButton = (Button)convertView.findViewById(R.id.btn_theme_apply);
			Button delButton = (Button)convertView.findViewById(R.id.btn_theme_del);
			ImageView themeThumbImageView = (ImageView)convertView.findViewById(R.id.theme_thumb);
			TextView themeVersionTextView = (TextView)convertView.findViewById(R.id.tv_theme_version);
			ThemeInfo themeInfo = (ThemeInfo)themeList.get(position);

			tvPackageTitle.setText(themeInfo.getName());
			if (themeInfo.getThumbnail() == null) {
				themeThumbImageView.setImageDrawable(getResources().getDrawable(R.drawable.theme_thumb_none));
			} else {
				themeThumbImageView.setImageDrawable(themeInfo.getThumbnail());
			}
			themeVersionTextView.setText(themeInfo.getVersion());

			boolean checked = false;
			String applyButtonText = "적용하기";
			Drawable checkIcon = null;
			if (position == checkedPosition) {
				checked = true;
				applyButtonText = "적용됨";
				checkIcon = getResources().getDrawable(R.drawable.icon_check);
				checkIcon.setBounds(0, 0, checkIcon.getIntrinsicWidth(), checkIcon.getIntrinsicHeight());
			}
			applyButton.setSelected(checked);
			applyButton.setEnabled(!checked);
			applyButton.setText(applyButtonText);
			applyButton.setCompoundDrawables(checkIcon, null, null, null);

			applyButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					defaultThemeApplyButton.setSelected(false);
					defaultThemeApplyButton.setCompoundDrawables(null, null, null, null);
					defaultThemeApplyButton.setText("적용하기");

					checkedPosition = position;

					notifyDataSetChanged();
					blogTheme.saveCurrentTheme(getApplicationContext(), themeList.get(position));

					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					}, 300);
				}
			});

			delButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ThemeInfo themeInfo = themeList.get(position);
					toDeletePackageName = themeInfo.getPackageName();
					currentTheme = blogTheme.findCurrentTheme(getApplicationContext());

					//					blogTheme.saveCurrentTheme(getApplicationContext(), new ThemeInfo("", "기본테마"));

					Intent intent = new Intent(Intent.ACTION_DELETE);
					intent.setData(Uri.parse("package:" + toDeletePackageName));
					startActivityForResult(intent, BlogRequestCode.SETTINGS_THEME_DELETE);
				}
			});
			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BlogRequestCode.SETTINGS_THEME_DELETE) {
			List<ThemeInfo> currentThemeList = blogTheme.findInstalledThemeInfoList();

			if (isExistToDeleteTheme(currentThemeList)) { // 지우지 않고 취소를 누른 경우
				return;
			}

			blogTheme.saveCurrentTheme(getApplicationContext(), new ThemeInfo("", "기본테마"));
			if (isNotCurrentTheme()) { // 현재 사용중이지 않는 테마 삭제 경우
				themeList = currentThemeList;
				bannerImage.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				return;
			}


			Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	private boolean isNotCurrentTheme() {
		return !StringUtils.equals(currentTheme.getPackageName(), toDeletePackageName);
	}

	private boolean isExistToDeleteTheme(List<ThemeInfo> currentThemeList) {
		for (ThemeInfo currentTheme : currentThemeList) {
			if (StringUtils.equals(currentTheme.getPackageName(), toDeletePackageName)) {
				return true;
			}
		}
		return false;
	}
}
