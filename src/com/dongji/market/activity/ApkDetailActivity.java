package com.dongji.market.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.myjson.JSONException;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.ApkDetailImageAdapter;
import com.dongji.market.adapter.DownloadAdapter;
import com.dongji.market.adapter.GuessLikeAdapter;
import com.dongji.market.adapter.OnDownloadChangeStatusListener;
import com.dongji.market.application.AppMarket;
import com.dongji.market.cache.FileService;
import com.dongji.market.database.MarketDatabase;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.HistoryApkItem;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.receiver.CommonReceiver;
import com.dongji.market.widget.CustomGalleryDialog;

/**
 * 详情界面
 * 
 * @author quhm
 * 
 */
public class ApkDetailActivity extends PublicActivity implements AConstDefine,
		OnDownloadChangeStatusListener, OnToolBarBlankClickListener {

	private static final int EVENT_SCROLL_TOBOTTOM = 0;

	private static final int EVENT_REQUEST_DETAIL_DATA = 1;
	private static final int EVENT_NO_NETWORK_ERROR = 2;
	private static final int EVENT_REQUEST_DATA_ERROR = 3;

	private static final int EVENT_GRADE = 4;
	// private static final int EVENT_REQUEST_DETAIL_BY_PACKAGENAME = 5;

	private static final int FLAG_OPENFROMOWN = 6;
	private static final int FLAG_OPENFROMOTHER = 7;

	// private ExpandableListView exLv_appdetail;
	// private LinearLayout llHeaderView;
	private LinearLayout llPrintScreen;
	private TextView tvDetailAbstruct;
	private LinearLayout llPermission;
	private LinearLayout llOldVersion;
	private LinearLayout llgrade_click;
	private LinearLayout llgrade_noclick;
	private LinearLayout llLikeApp;
	private ImageView ivGroupSelector_permission;
	private ImageView ivGroupSelector_oldVersion;
	private ImageView ivGroupSelector_grade;
	private ImageView ivGalleryLeft;
	private ImageView ivGalleryRight;
	private LinearLayout llGroup_permission;
	private LinearLayout llGroup_oldVersions;
	private LinearLayout llGroup_grade;
	// private GridView mGuessLikeGrid;

	private RatingBar commitCommentRatingBar;
	private RatingBar displayCommentRatingBar;
	private Button btnGrade;

	private ScrollView svApkDetail;

	private ApkItem apkItem;
	List<ApkItem> likeApkItems = new ArrayList<ApkItem>();

	private TextView tvApkName;
	private TextView tvApkVersion;
	private TextView tvApkSize;
	private TextView tvApkPublishDate;
	private TextView tvApkInstallCount;
	private TextView tvApkDeveloper;

	private TextView btnInstall;

	private TitleUtil titleUtil;
	private View mTopView;

	private Bitmap defaultBitmap_icon;
	private Bitmap defaultBitmap_gallery;

	private AppMarket mApp;

	private MyHandler mDataHandler;

	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;

	private boolean isFirstResume = true;

	private View mMaskView;

	private String packageName;
	private int versionCode;

	private MarketDatabase db;
	private float rating;

	private ImageView ivdongji_head;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apkdetail);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

		// Intent intent=getIntent();
		// System.out.println(intent.getDataString());

		mApp = (AppMarket) getApplication();

		// checkFirstLauncherDetail();

		// llHeaderView = (LinearLayout) findViewById(R.id.detail_detailhead);
		llPrintScreen = (LinearLayout) findViewById(R.id.llPrintScreen);
		tvDetailAbstruct = (TextView) findViewById(R.id.tvDetailAbstruct);
		llGroup_permission = (LinearLayout) findViewById(R.id.llGroup_permission);
		ivGroupSelector_permission = (ImageView) findViewById(R.id.ivGroupSelector_permission);
		llPermission = (LinearLayout) findViewById(R.id.llPermission);

		llGroup_oldVersions = (LinearLayout) findViewById(R.id.llGroup_oldVersions);
		ivGroupSelector_oldVersion = (ImageView) findViewById(R.id.ivGroupSelector_oldVersion);
		llOldVersion = (LinearLayout) findViewById(R.id.llOldversion);

		llGroup_grade = (LinearLayout) findViewById(R.id.llGroup_grade);
		ivGroupSelector_grade = (ImageView) findViewById(R.id.ivGroupSelector_grade);
		llgrade_click = (LinearLayout) findViewById(R.id.llgrade_click);
		commitCommentRatingBar = (RatingBar) findViewById(R.id.commitCommentRatingBar);
		btnGrade = (Button) findViewById(R.id.btnGrade);
		llgrade_noclick = (LinearLayout) findViewById(R.id.llgrade_noclick);
		displayCommentRatingBar = (RatingBar) findViewById(R.id.displayCommentRatingBar);

		llLikeApp = (LinearLayout) findViewById(R.id.llLikeApp);

		svApkDetail = (ScrollView) findViewById(R.id.svApkDetail);

		ivGalleryLeft = (ImageView) findViewById(R.id.ivGalleryLeft);
		ivGalleryRight = (ImageView) findViewById(R.id.ivGalleryRight);

		mTopView = findViewById(R.id.detail_top);

		ivdongji_head = (ImageView) findViewById(R.id.ivdongji_head);

		try {
			InputStream is = getResources().openRawResource(
					R.drawable.gallery_default);
			defaultBitmap_gallery = BitmapFactory.decodeStream(is);
			// defaultBitmap_gallery =
			// BitmapFactory.decodeResource(getResources(),
			// R.drawable.gallery_default);
		} catch (OutOfMemoryError e) {
			if (defaultBitmap_gallery != null
					&& !defaultBitmap_gallery.isRecycled()) {
				defaultBitmap_gallery.recycle();
			}
		}

		try {
			InputStream is = getResources().openRawResource(
					R.drawable.app_default_icon);
			defaultBitmap_icon = BitmapFactory.decodeStream(is);
			// mDefaultBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.app_default_icon);

		} catch (OutOfMemoryError e) {
			if (defaultBitmap_icon != null && !defaultBitmap_icon.isRecycled()) {
				defaultBitmap_icon.recycle();
			}
		}

		int temp = initData();
		if (temp != -1) {
			initHandler(temp);
		} else {
			finish();
		}

		initLoadingView();
		if (null != apkItem) {
			titleUtil = new TitleUtil(ApkDetailActivity.this, mTopView,
					apkItem.appName, getIntent().getExtras(), this);
		}

	}

	private int initData() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			apkItem = bundle.getParcelable("apkItem");
			return FLAG_OPENFROMOWN;
		} else {
			String tempString = getIntent().getDataString();
			if (tempString != null && tempString.length() > 20) {
				packageName = tempString.substring(20);
				// packageName = "com.dongji.market";
				versionCode = DJMarketUtils
						.getInstalledAppVersionCodeByPackageName(
								ApkDetailActivity.this, packageName);
				return FLAG_OPENFROMOTHER;
			}
		}
		return -1;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isFirstResume) {
			if (apkItem != null) {
				System.out.println("detail " + apkItem.appName + ", "
						+ apkItem.status);
				apkItem = setApkStatus(apkItem);
				displayApkStatus(btnInstall, apkItem.status);
			}
		}
		if (titleUtil != null) {
			titleUtil.sendRefreshHandler();
		}
		isFirstResume = false;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
				mMaskView.setVisibility(View.GONE);
				return true;
			}
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
		return super.onKeyDown(keyCode, event);
	}

	// private void checkFirstLauncherDetail() {
	// SharedPreferences mSharedPreferences = getSharedPreferences(
	// this.getPackageName() + "_temp", Context.MODE_PRIVATE);
	// boolean firstLaunch = mSharedPreferences.getBoolean(
	// ShareParams.FIRST_LAUNCHER_DETAIL, true);
	// if (firstLaunch) {
	// SharedPreferences.Editor mEditor = mSharedPreferences.edit();
	// mEditor.putBoolean(ShareParams.FIRST_LAUNCHER_DETAIL, false);
	// mEditor.commit();
	// mMaskView = findViewById(R.id.layout_detail_mask);
	// mMaskView.setVisibility(View.VISIBLE);
	// mMaskView.setOnTouchListener(new OnTouchListener() {
	//
	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	// mMaskView.setVisibility(View.GONE);
	// return false;
	// }
	// });
	// }
	// }

	private void initViews() {
		svApkDetail.setVisibility(View.VISIBLE);

		initHeaderViews();
		// llHeaderView.addView(initHeaderViews(), new
		// LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.FILL_PARENT));
		llPrintScreen.addView(initPrintScreenView());
		llLikeApp.addView(initLikeAppView());
		if (null == apkItem.discription
				|| apkItem.discription.trim().equals("")) {

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			tvDetailAbstruct.setLayoutParams(lp);
			tvDetailAbstruct.setGravity(Gravity.CENTER);
			tvDetailAbstruct.setTextColor(getResources().getColor(
					android.R.color.black));
			tvDetailAbstruct.setPadding(15, 15, 15, 15);
			tvDetailAbstruct.setTextSize(15);
			tvDetailAbstruct.setText(R.string.none_abstruct);
		} else {
			tvDetailAbstruct.setText(apkItem.discription);
		}

		initPermissionView(llPermission);
		initOldVersionView(llOldVersion);
		llPermission.setVisibility(View.GONE);
		llOldVersion.setVisibility(View.GONE);

		llGroup_permission.setOnClickListener(new onIVClickListener());
		llGroup_oldVersions.setOnClickListener(new onIVClickListener());
		llGroup_grade.setOnClickListener(new onIVClickListener());
		ivGalleryLeft.setOnClickListener(new onIVClickListener());
		ivGalleryRight.setOnClickListener(new onIVClickListener());

	}

	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mLoadingProgressBar = findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					int temp = initData();
					if (temp != -1) {
						Message msg = new Message();
						msg.what = EVENT_REQUEST_DETAIL_DATA;
						msg.arg1 = temp;
						mDataHandler.sendMessage(msg);
					} else {
						finish();
					}
				}
				return false;
			}
		});
	}

	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	/**
	 * 使用3G下载是否提示过用户
	 * 
	 * @return
	 */
	public boolean is3GDownloadPromptUser() {
		return mApp.isIs3GDownloadPrompt();
	}

	/**
	 * 使用3G下载已提示用户
	 */
	public void set3GDownloadPromptUser() {
		mApp.setIs3GDownloadPrompt(true);
	}

	// private class onMyItemClickListener implements OnItemClickListener {
	// private ApkItem apkItem;
	//
	// public onMyItemClickListener(ApkItem apkItem) {
	// this.apkItem = apkItem;
	// }
	//
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// Intent intent = new Intent();
	// Bundle bundle = new Bundle();
	// bundle.putParcelable("apkItem", apkItem);
	// intent.putExtras(bundle);
	// intent.setClass(ApkDetailActivity.this, ApkDetailActivity.class);
	// startActivity(intent);
	//
	// }
	// }

	private class onMyClickListener implements OnClickListener {
		private ApkItem apkItem;

		public onMyClickListener(ApkItem apkItem) {
			this.apkItem = apkItem;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putParcelable("apkItem", apkItem);
			intent.putExtras(bundle);
			intent.setClass(ApkDetailActivity.this, ApkDetailActivity.class);
			startActivity(intent);
			finish();
			System.out.println("-------finish");
		}
	}

	private class onIVClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ivGroupSelector_permission:
			case R.id.llGroup_permission:
				if (llPermission.getVisibility() == View.GONE) {
					llPermission.setVisibility(View.VISIBLE);
					llPermission.setFocusable(true);
					llPermission.requestFocus();
					llPermission.setFocusableInTouchMode(true);
					// llPermission.invalidate();
					ivGroupSelector_permission.setImageDrawable(getResources()
							.getDrawable(R.drawable.pic_down));
					mHandler.sendEmptyMessageDelayed(EVENT_SCROLL_TOBOTTOM, 50L);
				} else {
					llPermission.setVisibility(View.GONE);
					ivGroupSelector_permission.setImageDrawable(getResources()
							.getDrawable(R.drawable.pic_up));
				}
				break;
			case R.id.ivGroupSelector_oldVersion:
			case R.id.llGroup_oldVersions:
				if (llOldVersion.getVisibility() == View.GONE) {
					llOldVersion.setVisibility(View.VISIBLE);
					ivGroupSelector_oldVersion.setImageDrawable(getResources()
							.getDrawable(R.drawable.pic_down));
				} else {
					llOldVersion.setVisibility(View.GONE);
					ivGroupSelector_oldVersion.setImageDrawable(getResources()
							.getDrawable(R.drawable.pic_up));
				}
				break;
			case R.id.ivGroupSelector_grade:
			case R.id.llGroup_grade:
				if (rating == -1) {
					if (llgrade_click.getVisibility() == View.GONE) {
						llgrade_click.setVisibility(View.VISIBLE);
						ivGroupSelector_grade.setImageDrawable(getResources()
								.getDrawable(R.drawable.pic_down));
					} else {
						llgrade_click.setVisibility(View.GONE);
						ivGroupSelector_grade.setImageDrawable(getResources()
								.getDrawable(R.drawable.pic_up));
					}
				} else {
					if (llgrade_noclick.getVisibility() == View.GONE) {
						llgrade_noclick.setVisibility(View.VISIBLE);
						ivGroupSelector_grade.setImageDrawable(getResources()
								.getDrawable(R.drawable.pic_down));
					} else {
						llgrade_noclick.setVisibility(View.GONE);
						ivGroupSelector_grade.setImageDrawable(getResources()
								.getDrawable(R.drawable.pic_up));
					}
				}
				break;
			case R.id.ivGalleryLeft:
				int flagInt = AndroidUtils.dip2px(ApkDetailActivity.this, 170);
				if (mScrollView.getScrollX() % flagInt == 0
						|| mScrollView.getScrollX() == 0) {
					mScrollView.scrollBy(-flagInt, 0);
				} else {
					mScrollView
							.scrollBy(-mScrollView.getScrollX() % flagInt, 0);
				}

				break;
			case R.id.ivGalleryRight:
				flagInt = AndroidUtils.dip2px(ApkDetailActivity.this, 170);
				// if(mScrollView.getScrollX()%flagInt==0||
				// mScrollView.getScrollX()==0){
				// mScrollView.scrollBy(flagInt, 0);
				// }else{
				mScrollView.scrollBy(flagInt - mScrollView.getScrollX()
						% flagInt, 0);
				// }

				break;
			}

		}

	}

	private LinearLayout mContentLayout2;

	private View initLikeAppView() {

		for (int i = 0; i < apkItem.likeList.size(); i++) {
			if (null != apkItem.likeList.get(i)) {
				likeApkItems.add(apkItem.likeList.get(i));
			}
		}

		if (null == likeApkItems || likeApkItems.size() == 0) {
			TextView tvTips = new TextView(ApkDetailActivity.this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			tvTips.setLayoutParams(lp);
			tvTips.setGravity(Gravity.CENTER);
			tvTips.setTextColor(getResources().getColor(android.R.color.black));
			int padding = AndroidUtils.dip2px(this, 5.0f);
			tvTips.setPadding(0, padding, 0, padding);
			tvTips.setTextSize(15);
			tvTips.setText(R.string.none_likeapp);
			return tvTips;
		}

		HorizontalScrollView mScrollView2 = (HorizontalScrollView) LayoutInflater
				.from(this).inflate(R.layout.layout_detail_scrollview, null);
		mContentLayout2 = (LinearLayout) mScrollView2
				.findViewById(R.id.contentlayout);

		// mContentLayout2 = (LinearLayout) findViewById(R.id.show_gridview);
		int columnWidth = AndroidUtils.dip2px(this, 48);
		// int horizontalSpacing = AndroidUtils.dip2px(this, 10);
		int padding_10 = AndroidUtils.dip2px(this, 10);
		// int padding_5 = AndroidUtils.dip2px(this, 5);
		// mGuessLikeGrid = (GridView) findViewById(R.id.guess_gridview);
		LayoutParams params = new LayoutParams(likeApkItems.size()
				* columnWidth + likeApkItems.size() * padding_10, columnWidth
				+ padding_10 * 4);
		GridView gvPrintScreen = new GridView(this);
		gvPrintScreen.setLayoutParams(params);
		gvPrintScreen.setColumnWidth(columnWidth);
		gvPrintScreen.setHorizontalSpacing(padding_10);
		// mGuessLikeGrid.setVerticalSpacing(0);
		gvPrintScreen.setNumColumns(likeApkItems.size());
		// mGuessLikeGrid.setPadding(0, padding, 0, padding);
		gvPrintScreen.setSelector(android.R.color.transparent);
		gvPrintScreen.setStretchMode(GridView.NO_STRETCH);
		// gvPrintScreen
		// .setOnItemClickListener(new onMyItemClickListener(likeApkItems.));
		GuessLikeAdapter adapter = new GuessLikeAdapter(this, likeApkItems,
				defaultBitmap_icon);
		gvPrintScreen.setAdapter(adapter);
		mContentLayout2.addView(gvPrintScreen);

		return mScrollView2;

	}

	private TextView[] mOldTextViews;

	private void initOldVersionView(LinearLayout llOldVersion) {

		// List<> oldversionList = getOldVersionList();
		HistoryApkItem[] historyApkItems = apkItem.historys;
		if (null == historyApkItems || historyApkItems.length == 0) {
			TextView tvTips = new TextView(ApkDetailActivity.this);
			tvTips.setGravity(Gravity.CENTER);
			tvTips.setTextColor(getResources().getColor(android.R.color.black));
			int padding = AndroidUtils.dip2px(this, 5.0f);
			tvTips.setPadding(0, padding, 0, padding);
			tvTips.setTextSize(15);
			tvTips.setText(R.string.none_history);
			llOldVersion.addView(tvTips);
			return;
		}
		LinearLayout itemOldVersionView;
		int size = historyApkItems.length > 4 ? 5 : historyApkItems.length;
		mOldTextViews = new TextView[size];
		for (int i = 0; i < size; i++) {
			itemOldVersionView = (LinearLayout) LayoutInflater.from(this)
					.inflate(R.layout.item_list_oldversion, null);
			ImageView ivOldVersionLogo = (ImageView) itemOldVersionView
					.findViewById(R.id.ivOldVersionLogo);
			ImageView ivdongji_oldversion = (ImageView) itemOldVersionView
					.findViewById(R.id.ivdongji_oldversion);
			TextView tvOldVersionName = (TextView) itemOldVersionView
					.findViewById(R.id.tvOldVersionName);
			TextView tvOldVersionVersion = (TextView) itemOldVersionView
					.findViewById(R.id.tvOldVersionVersion);
			ImageView ivOldVersionType = (ImageView) itemOldVersionView
					.findViewById(R.id.ivOldVersionType);
			TextView tvOldVersionPublishDate = (TextView) itemOldVersionView
					.findViewById(R.id.tvOldVersionPublishDate);
			TextView tvOldVersionApkSize = (TextView) itemOldVersionView
					.findViewById(R.id.tvOldVersionApkSize);
			TextView tvOldVersionInstallCount = (TextView) itemOldVersionView
					.findViewById(R.id.tvOldVersionInstallCount);
			mOldTextViews[i] = (TextView) itemOldVersionView
					.findViewById(R.id.btnInstall);
			mOldTextViews[i].setOnClickListener(new OldOnClickListener(i));

			switch (historyApkItems[i].status) {
			case STATUS_APK_UNINSTALL:
				setvisibleInstallTextView(mOldTextViews[i], true,
						R.string.install,
						R.drawable.button_has_border_selector, Color.BLACK);
				break;
			case STATUS_APK_INSTALL:
			case STATUS_APK_UPDATE:
				setvisibleInstallTextView(mOldTextViews[i], true,
						R.string.cancel, R.drawable.cancel_selector,
						Color.parseColor("#7f5100"));
				break;
			case STATUS_APK_INSTALL_DONE:
				setvisibleInstallTextView(mOldTextViews[i], false,
						R.string.has_installed,
						R.drawable.button_has_border_selector,
						Color.parseColor("#999999"));
				break;
			case STATUS_APK_UNUPDATE:
				setvisibleInstallTextView(mOldTextViews[i], true,
						R.string.update, R.drawable.update_selector,
						Color.parseColor("#0e567d"));
				break;
			}

			if (historyApkItems[i].heavy > 0) {
				ivdongji_oldversion.setVisibility(View.VISIBLE);
			} else {
				ivdongji_oldversion.setVisibility(View.GONE);
			}

			FileService.getBitmap(apkItem.appIconUrl, ivOldVersionLogo,
					defaultBitmap_icon, 0);

			tvOldVersionName.setText(historyApkItems[i].appName);
			tvOldVersionVersion.setText("V" + historyApkItems[i].versionName);
			ivOldVersionType
					.setImageResource(getOldVersionApkTypeImage(historyApkItems[i].appType));
			tvOldVersionPublishDate.setText(historyApkItems[i].updateDate);
			tvOldVersionApkSize.setText(NetTool
					.sizeFormat((int) historyApkItems[i].appSize));
			tvOldVersionInstallCount.setText(NetTool
					.numberFormat(historyApkItems[i].downloadNum));
			itemOldVersionView.setClickable(true);
			ApkItem tempApkItem = new ApkItem();
			tempApkItem.category = historyApkItems[i].category;
			tempApkItem.appId = historyApkItems[i].appId;
			tempApkItem.appName = historyApkItems[i].appName;
			itemOldVersionView.setOnClickListener(new onMyClickListener(
					tempApkItem));
			itemOldVersionView
					.setBackgroundResource(R.drawable.android_listselector);
			llOldVersion.addView(itemOldVersionView);
		}
	}

	private class OldOnClickListener implements OnClickListener {
		int position;

		OldOnClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			HistoryApkItem historyItem = apkItem.historys[position];
			ApkItem item = new ApkItem();
			item.appIconUrl = historyItem.appIconUrl;
			item.appId = historyItem.appId;
			item.appName = historyItem.appName;
			item.fileSize = historyItem.appSize;
			item.category = historyItem.category;
			item.status = historyItem.status;
			item.updateDate = historyItem.updateDate;
			item.apkUrl = historyItem.url;
			item.versionCode = historyItem.versionCode;
			item.version = historyItem.versionName;
			item.packageName = apkItem.packageName;
			if (historyItem.status == STATUS_APK_UNINSTALL
					|| historyItem.status == STATUS_APK_UNUPDATE) {
				// DJMarketUtils.checkDownload(ApkDetailActivity.this,
				// apkItem, btnInstall, ApkDetailActivity.this, null);
				DownloadUtils.checkDownload(ApkDetailActivity.this, item,
						btnInstall, ApkDetailActivity.this, null);
				// historyItem=
			} else {
				/*
				 * DJMarketUtils.cancelListDownload(ApkDetailActivity.this,
				 * apkItem);
				 */
				Intent intent = new Intent(
						DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
				DownloadEntity entity = new DownloadEntity(item);
				Bundle bundle = new Bundle();
				bundle.putParcelable(DownloadConstDefine.DOWNLOAD_ENTITY,
						entity);
				intent.putExtras(bundle);
				sendBroadcast(intent);
			}
		}

	}

	private int getOldVersionApkTypeImage(int apkType) {
		int returnTypeValue = R.drawable.language_chinese;
		switch (apkType) {
		case 1:
			returnTypeValue = R.drawable.language_chinese;
			break;
		case 2:
			returnTypeValue = R.drawable.language_english;
			break;
		case 3:
			returnTypeValue = R.drawable.language_multinational;
			break;
		}
		return returnTypeValue;
	}

	private void initPermissionView(LinearLayout llPermission) {
		List<String> permissionList = apkItem.permisions;
		if (null == permissionList || permissionList.size() == 0) {
			TextView tvTips = new TextView(ApkDetailActivity.this);
			tvTips.setGravity(Gravity.CENTER);
			tvTips.setTextColor(getResources().getColor(android.R.color.black));
			int padding = AndroidUtils.dip2px(this, 5.0f);
			tvTips.setPadding(0, padding, 0, padding);
			tvTips.setTextSize(15);
			tvTips.setText(R.string.none_permission);
			llPermission.addView(tvTips);
			return;
		}
		int n = 0;
		LayoutInflater mInflater = LayoutInflater.from(this);
		DisplayMetrics dm = AndroidUtils.getScreenSize(this);
		int maxWidth = dm.widthPixels - AndroidUtils.dip2px(this, 10.0f);
		int imageWidth = AndroidUtils.dip2px(this, 30.0f);
		int spacing = AndroidUtils.dip2px(this, 10.0f);
		while (n < permissionList.size()) {
			LinearLayout mLinearLayout = getLinearLayout();
			LinearLayout mChildLayout = (LinearLayout) mInflater.inflate(
					R.layout.layout_permission, null);
			TextView mTextView = (TextView) mChildLayout
					.findViewById(R.id.textview);
			float textWidth = mTextView.getPaint().measureText(
					permissionList.get(n));
			mTextView.setText(permissionList.get(n++));
			mLinearLayout.addView(mChildLayout);
			float tempWidth = imageWidth;
			textWidth += (spacing + imageWidth);
			while (textWidth + tempWidth < maxWidth
					&& n < permissionList.size()) {
				LinearLayout mChildLayout2 = (LinearLayout) mInflater.inflate(
						R.layout.layout_permission, null);
				TextView mTextView2 = (TextView) mChildLayout2
						.findViewById(R.id.textview);
				ImageView mImageView = (ImageView) mChildLayout2
						.findViewById(R.id.imageview);
				tempWidth = mTextView2.getPaint().measureText(
						permissionList.get(n));
				tempWidth += (spacing + imageWidth);
				if (textWidth + tempWidth <= maxWidth) {
					mTextView2.setText(permissionList.get(n++));
					LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) mImageView
							.getLayoutParams();
					mParams.leftMargin = spacing;
					mLinearLayout.addView(mChildLayout2);
					textWidth += tempWidth;
					tempWidth = 0;
				} else {
					break;
				}
			}
			llPermission.addView(mLinearLayout);
		}
	}

	private LinearLayout getLinearLayout() {
		LinearLayout mLinearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mLinearLayout.setLayoutParams(mParams);
		return mLinearLayout;
	}

	private HorizontalScrollView mScrollView;
	private LinearLayout mContentLayout;

	private View initPrintScreenView() {

		final List<String> arr = apkItem.appScreenshotUrl;

		if (null == arr || arr.size() == 0) {
			TextView tvTips = new TextView(ApkDetailActivity.this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			tvTips.setLayoutParams(lp);
			tvTips.setGravity(Gravity.CENTER);
			tvTips.setTextColor(getResources().getColor(android.R.color.black));
			int padding = AndroidUtils.dip2px(this, 5.0f);
			tvTips.setPadding(0, padding, 0, padding);
			tvTips.setTextSize(15);
			tvTips.setText(R.string.none_printscreen);
			ivGalleryLeft.setEnabled(false);
			ivGalleryRight.setEnabled(false);
			return tvTips;
		}

		mScrollView = (HorizontalScrollView) LayoutInflater.from(this).inflate(
				R.layout.layout_detail_scrollview, null);
		mContentLayout = (LinearLayout) mScrollView
				.findViewById(R.id.contentlayout);

		/*
		 * LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
		 * AndroidUtils.dip2px(this, 160), AndroidUtils.dip2px(this, 240));
		 * llParams.leftMargin = 5; llParams.rightMargin = 5;
		 * LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
		 * AndroidUtils.dip2px(this, 155), AndroidUtils.dip2px(this, 235));
		 * imageViews = new ImageView[arr.size()]; mGuessLikeGrid = new
		 * GridView(ApkDetailActivity.this);
		 * mGuessLikeGrid.setLayoutParams(llParams);
		 * mGuessLikeGrid.setBackgroundResource(R.drawable.bg_printscreen); for
		 * (int i = 0; i < arr.size(); i++) { // LinearLayout llIV = new
		 * LinearLayout(this);
		 * 
		 * // ImageView mImageView = new ImageView(this); imageViews[i] = new
		 * ImageView(this); imageViews[i].setLayoutParams(ivParams);
		 * imageViews[i].setScaleType(ScaleType.FIT_XY);
		 * 
		 * try { FileService.getBitmap(arr.get(i), imageViews[i], defaultBitmap,
		 * 0); } catch (OutOfMemoryError e) { if (defaultBitmap != null &&
		 * !defaultBitmap.isRecycled()) { defaultBitmap.recycle(); } } final int
		 * num = i; imageViews[i].setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { showGalleryDetailDialog(arr,
		 * num); } }); mGuessLikeGrid.addView(imageViews[i]); }
		 * mContentLayout.addView(mGuessLikeGrid);
		 */

		int columnWidth = AndroidUtils.dip2px(this, 160);
		// int horizontalSpacing = AndroidUtils.dip2px(this, 10);
		int padding = AndroidUtils.dip2px(this, 10);
		// mGuessLikeGrid = (GridView) findViewById(R.id.guess_gridview);
		LayoutParams params = new LayoutParams(arr.size() * columnWidth
				+ arr.size() * padding, AndroidUtils.dip2px(this, 240));
		GridView mGuessLikeGrid = new GridView(ApkDetailActivity.this);
		mGuessLikeGrid.setLayoutParams(params);
		mGuessLikeGrid.setColumnWidth(columnWidth);
		// mGuessLikeGrid.setHorizontalSpacing(horizontalSpacing);
		mGuessLikeGrid.setNumColumns(arr.size());
		// mGuessLikeGrid.setHorizontalSpacing(padding);
		mGuessLikeGrid.setHorizontalSpacing(padding);
		mGuessLikeGrid.setVerticalSpacing(padding / 2);
		mGuessLikeGrid.setSelector(R.drawable.bg_printscreen);
		// mGuessLikeGrid.setPadding(padding, padding, padding, padding);
		// mGuessLikeGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		mGuessLikeGrid.setStretchMode(GridView.NO_STRETCH);
		mGuessLikeGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				showGalleryDetailDialog(arr, (int) arg3);
			}
		});
		ApkDetailImageAdapter adapter = new ApkDetailImageAdapter(this, arr,
				defaultBitmap_gallery);
		mGuessLikeGrid.setAdapter(adapter);
		mContentLayout.addView(mGuessLikeGrid);

		mHandler.sendEmptyMessage(REFERENSH_PROGRESS);
		return mScrollView;
	}

	/*
	 * private void init() {
	 * 
	 * final List<String> arr = apkItem.appScreenshotUrl;
	 * LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
	 * AndroidUtils.dip2px(this, 160), AndroidUtils.dip2px(this, 240));
	 * llParams.leftMargin = 5; llParams.rightMargin = 5;
	 * LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
	 * AndroidUtils.dip2px(this, 155), AndroidUtils.dip2px(this, 235));
	 * imageViews = new ImageView[arr.size()]; for (int i = 0; i < arr.size();
	 * i++) { LinearLayout llIV = new LinearLayout(this);
	 * llIV.setLayoutParams(llParams);
	 * llIV.setBackgroundResource(R.drawable.bg_printscreen); // ImageView
	 * mImageView = new ImageView(this); imageViews[i] = new ImageView(this);
	 * imageViews[i].setLayoutParams(ivParams);
	 * imageViews[i].setScaleType(ScaleType.FIT_XY);
	 * 
	 * try { FileService.getBitmap(arr.get(i), imageViews[i], defaultBitmap, 0);
	 * } catch (OutOfMemoryError e) { if (defaultBitmap != null &&
	 * !defaultBitmap.isRecycled()) { defaultBitmap.recycle(); } } final int num
	 * = i; imageViews[i].setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { showGalleryDetailDialog(arr,
	 * num); } }); llIV.addView(imageViews[i]); mContentLayout.addView(llIV); }
	 * 
	 * }
	 */
	private CustomGalleryDialog mGalleryDialog;

	private void showGalleryDetailDialog(List<String> arr, int position) {
		if (!isFinishing()) {
			if (mGalleryDialog == null) {
				mGalleryDialog = new CustomGalleryDialog(ApkDetailActivity.this);
				mGalleryDialog.setImageSource(arr);
			}
			if (mGalleryDialog != null) {
				mGalleryDialog.showPosition(position);
			}
		}
	}

	private ImageView mIvApkIcon;

	private View initHeaderViews() {
		/*
		 * View headView = LayoutInflater.from(this).inflate(
		 * R.layout.layout_apkdetailhead, null);
		 */
		View headView = findViewById(R.id.detail_head_view);
		mIvApkIcon = (ImageView) headView.findViewById(R.id.ivApkIcon);
		ivdongji_head = (ImageView) headView.findViewById(R.id.ivdongji_head);
		RatingBar detail_head_RatingBar = (RatingBar) headView
				.findViewById(R.id.detail_head_RatingBar);

		FileService.getBitmap(apkItem.appIconUrl, mIvApkIcon,
				defaultBitmap_icon, 0);

		tvApkName = (TextView) headView.findViewById(R.id.tvApkName);
		tvApkVersion = (TextView) headView.findViewById(R.id.tvApkVersion);
		tvApkSize = (TextView) headView.findViewById(R.id.tvApkSize);
		tvApkPublishDate = (TextView) headView
				.findViewById(R.id.tvApkPublishDate);
		tvApkInstallCount = (TextView) headView
				.findViewById(R.id.tvApkInstallCount);
		tvApkDeveloper = (TextView) headView.findViewById(R.id.tvApkDeveloper);
		btnInstall = (TextView) headView.findViewById(R.id.btnInstall);

		for (int i = 0; i < DownloadAdapter.rootApkList.size(); i++) {
			if (DownloadAdapter.rootApkList.get(i).equals(packageName)) {
				btnInstall.setFocusable(false);
				btnInstall.setFocusableInTouchMode(false);
				btnInstall.setEnabled(false);
				btnInstall.setClickable(false);
			}
		}

		// if (DownloadAdapter.isRootInstalling > 0) {
		// btnInstall.setFocusable(false);
		// btnInstall.setFocusableInTouchMode(false);
		// btnInstall.setEnabled(false);
		// btnInstall.setClickable(false);
		// }

		tvApkName.setText(apkItem.appName);
		tvApkVersion.setText("V" + apkItem.version);
		float rating = getRating();
		detail_head_RatingBar.setRating(rating);

		tvApkSize.setText(NetTool.sizeFormat((int) apkItem.fileSize));
		if (null == apkItem.updateDate || apkItem.updateDate.trim().equals("")) {
			tvApkPublishDate.setText(R.string.none);
		} else {
			tvApkPublishDate.setText(apkItem.updateDate);
		}
		tvApkInstallCount.setText(DJMarketUtils.convertionInstallNumber(this,
				apkItem.downloadNum));
		if (null == apkItem.company || apkItem.company.trim().equals("")) {
			tvApkDeveloper.setText(R.string.none);
		} else {
			tvApkDeveloper.setText(apkItem.company);
		}
		displayApkStatus(btnInstall, apkItem.status);

		if (apkItem.heavy > 0) {
			ivdongji_head.setVisibility(View.VISIBLE);
		} else {
			ivdongji_head.setVisibility(View.GONE);
		}

		btnInstall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (apkItem.status == STATUS_APK_UNINSTALL
						|| apkItem.status == STATUS_APK_UNUPDATE) {
					// DJMarketUtils.checkDownload(ApkDetailActivity.this,
					// apkItem, btnInstall, ApkDetailActivity.this, null);
					DownloadUtils.checkDownload(ApkDetailActivity.this,
							apkItem, btnInstall, ApkDetailActivity.this, null);
				} else {
					/*
					 * DJMarketUtils.cancelListDownload(ApkDetailActivity.this,
					 * apkItem);
					 */
					Intent intent = new Intent(
							DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
					DownloadEntity entity = new DownloadEntity(apkItem);
					Bundle bundle = new Bundle();
					bundle.putParcelable(DownloadConstDefine.DOWNLOAD_ENTITY,
							entity);
					intent.putExtras(bundle);
					sendBroadcast(intent);
				}
				// NetTool.onDownloadBtnClick(ApkDetailActivity.this,
				// new ADownloadApkItem(apkItem, STATUS_OF_DOWNLOADING));

				// DJMarketUtils.checkDownload("ApkDetailActivity",ApkDetailActivity.this,
				// apkItem.appId, new ADownloadApkItem(apkItem,
				// STATUS_OF_DOWNLOADING));
			}
		});

		return headView;
	}

	private float getRating() {
		return (float) apkItem.score;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("test");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
			mMaskView.setVisibility(View.GONE);
		}
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}

	@Override
	protected void onDestroy() {
		if (titleUtil != null) {
			titleUtil.unregisterMyReceiver(this);
		}
		if (null != mContentLayout && null != mContentLayout.getChildAt(0)) {
			GridView gridView = (GridView) mContentLayout.getChildAt(0);
			for (int i = 0; i < gridView.getCount(); i++) {
				View view = gridView.getChildAt(i);
				if (null != view) {
					ImageView mIconImage = (ImageView) view
							.findViewById(R.id.app_icon);
					if (null != mIconImage) {
						BitmapDrawable bitmapDrawable = (BitmapDrawable) mIconImage
								.getDrawable();

						if (null != bitmapDrawable
								&& null != bitmapDrawable.getBitmap()) {
							Bitmap bitmap = bitmapDrawable.getBitmap();
							if (null != bitmap && !bitmap.isRecycled()) {
								bitmap.recycle();
								bitmap = null;
							}
							mIconImage.setImageBitmap(null);
						}
					}
				}
			}
		}

		if (null != mContentLayout2 && null != mContentLayout2.getChildAt(0)) {
			GridView gridView = (GridView) mContentLayout2.getChildAt(0);
			for (int i = 0; i < gridView.getCount(); i++) {
				View view = gridView.getChildAt(i);
				if (null != view) {
					ImageView mIconImage = (ImageView) view
							.findViewById(R.id.app_icon);
					if (null != mIconImage) {
						BitmapDrawable bitmapDrawable = (BitmapDrawable) mIconImage
								.getDrawable();

						if (null != bitmapDrawable
								&& null != bitmapDrawable.getBitmap()) {
							Bitmap bitmap = bitmapDrawable.getBitmap();
							if (null != bitmap && !bitmap.isRecycled()) {
								bitmap.recycle();
								bitmap = null;
							}
							mIconImage.setImageBitmap(null);
						}
					}
				}
			}
		}

		if (defaultBitmap_gallery != null
				&& !defaultBitmap_gallery.isRecycled()) {
			defaultBitmap_gallery.recycle();
			defaultBitmap_gallery = null;
		}
		if (defaultBitmap_icon != null && !defaultBitmap_icon.isRecycled()) {
			defaultBitmap_icon.recycle();
			defaultBitmap_icon = null;
		}
		if (mIvApkIcon != null) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) mIvApkIcon
					.getDrawable();
			if (null != bitmapDrawable) {
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (null != bitmap && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				mIvApkIcon.setImageBitmap(null);
			}
		}

		if (null != llOldVersion) {
			for (int i = 0; i < llOldVersion.getChildCount(); i++) {
				ImageView ivOldVersionLogo = (ImageView) llOldVersion
						.getChildAt(i).findViewById(R.id.ivOldVersionLogo);
				if (null != ivOldVersionLogo) {
					BitmapDrawable bitmapDrawable = (BitmapDrawable) ivOldVersionLogo
							.getDrawable();

					if (null != bitmapDrawable
							&& null != bitmapDrawable.getBitmap()) {
						Bitmap bitmap = bitmapDrawable.getBitmap();
						if (null != bitmap && !bitmap.isRecycled()) {
							bitmap.recycle();
							bitmap = null;
						}
						ivOldVersionLogo.setImageBitmap(null);
					}
				}
			}
		}
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		System.out.println("------onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDownload(ApkItem item, TextView mTextView,
			Map<String, Object> map) {
		if (item.status == STATUS_APK_UNINSTALL) {
			item.status = STATUS_APK_INSTALL;
		} else if (item.status == STATUS_APK_UNUPDATE) {
			item.status = STATUS_APK_UPDATE;
		}
		displayApkStatus(mTextView, item.status);
	}

	private void displayApkStatus(TextView mTextView, int status) {
		switch (status) {
		case STATUS_APK_UNINSTALL:
			setvisibleInstallTextView(mTextView, true, R.string.install,
					R.drawable.button_has_border_selector, Color.BLACK);
			break;
		case STATUS_APK_INSTALL:
		case STATUS_APK_UPDATE:
			setvisibleInstallTextView(mTextView, true, R.string.cancel,
					R.drawable.cancel_selector, Color.parseColor("#7f5100"));
			break;
		case STATUS_APK_INSTALL_DONE:
			// if (DJMarketUtils.IS_INSTALLING) {
			// DJMarketUtils.IS_INSTALLING = false;
			// }
			setvisibleInstallTextView(mTextView, false, R.string.has_installed,
					R.drawable.button_has_border_selector,
					Color.parseColor("#999999"));
			break;
		case STATUS_APK_UNUPDATE:
			setvisibleInstallTextView(mTextView, true, R.string.update,
					R.drawable.update_selector, Color.parseColor("#0e567d"));
			break;
		}
		System.out.println("=======dadasdasdasdas");
		if (apkItem.historys != null && apkItem.historys.length > 0
				&& mOldTextViews != null) {
			for (int i = 0; i < apkItem.historys.length; i++) {
				HistoryApkItem historyItem = apkItem.historys[i];
				switch (historyItem.status) {
				case STATUS_APK_UNINSTALL:
					setvisibleInstallTextView(mOldTextViews[i], true,
							R.string.install,
							R.drawable.button_has_border_selector, Color.BLACK);
					break;
				case STATUS_APK_INSTALL:
				case STATUS_APK_UPDATE:
					setvisibleInstallTextView(mOldTextViews[i], true,
							R.string.cancel, R.drawable.cancel_selector,
							Color.parseColor("#7f5100"));
					break;
				case STATUS_APK_INSTALL_DONE:
					setvisibleInstallTextView(mOldTextViews[i], false,
							R.string.has_installed,
							R.drawable.button_has_border_selector,
							Color.parseColor("#999999"));
					break;
				case STATUS_APK_UNUPDATE:
					setvisibleInstallTextView(mOldTextViews[i], true,
							R.string.update, R.drawable.update_selector,
							Color.parseColor("#0e567d"));
					break;
				}
			}
		}
	}

	private void setvisibleInstallTextView(TextView mTextView, boolean enable,
			int rId, int resid, int textColor) {
		if (mTextView != null) {
			mTextView.setEnabled(enable);
			mTextView.setText(rId);
			mTextView.setBackgroundResource(resid);
			mTextView.setTextColor(textColor);

			// if (DJMarketUtils.IS_INSTALLING) {
			// mTextView.setEnabled(false);
			// } else {
			// mTextView.setEnabled(true);
			// }
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_SCROLL_TOBOTTOM:
				int y = svApkDetail.getChildAt(0).getHeight()
						- svApkDetail.getHeight();
				// svApkDetail.scrollTo(0, y);
				svApkDetail.smoothScrollTo(0, y);
				break;
			case REFERENSH_PROGRESS:
				// if (null != apkItem.appScreenshotUrl) {
				// for (int i = 0; i < apkItem.appScreenshotUrl.size(); i++) {
				// try {
				// FileService.getBitmap(apkItem.appScreenshotUrl
				// .get(i),
				// (ImageView) ((LinearLayout) mContentLayout
				// .getChildAt(0)).getChildAt(i),
				// defaultBitmap, 0);
				// } catch (OutOfMemoryError e) {
				// if (defaultBitmap != null
				// && !defaultBitmap.isRecycled()) {
				// defaultBitmap.recycle();
				// }
				// }
				// }
				// if (reflushFlag < 3) {
				// sendEmptyMessageDelayed(REFERENSH_PROGRESS, 3000);
				// }
				// reflushFlag++;
				// }
				break;
			}
		};
	};

	private void initHandler(int handlerflag) {
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		mDataHandler = new MyHandler(mHandlerThread.getLooper());
		Message msg = new Message();
		msg.what = EVENT_REQUEST_DETAIL_DATA;
		msg.arg1 = handlerflag;
		mDataHandler.sendMessage(msg);
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_DETAIL_DATA:
				requestDetailData(msg.arg1);
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg);
				break;
			case EVENT_GRADE:
				final float score = commitCommentRatingBar.getRating();
				int responseStatus;
				try {
					responseStatus = DataManager.newInstance().appGrade(
							apkItem.category + "", apkItem.appId + "",
							score + "");
					if (responseStatus == 1) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								llgrade_click.setVisibility(View.GONE);
								llgrade_noclick.setVisibility(View.VISIBLE);
								displayCommentRatingBar.setRating(score);
								displayCommentRatingBar.setClickable(false);
								displayCommentRatingBar.setIsIndicator(true);
								btnGrade.setEnabled(true);
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								btnGrade.setEnabled(true);
								AndroidUtils.showToast(ApkDetailActivity.this,
										R.string.grade_failed);
							}
						});
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			}
		}
	}

	private void setErrorMessage(final int rId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				}
			}
		});
	}

	private void requestDetailData(int openflag) {
		try {
			
			if (openflag == FLAG_OPENFROMOWN) {
				apkItem = DataManager.newInstance().getApkItemDetailByAppId(apkItem.category, apkItem.appId);
			} else {
				apkItem = DataManager.newInstance().getApkItemDetailByPackage(packageName, versionCode);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (null != apkItem) {
							Bundle bundle = new Bundle();
							bundle.putParcelable("apkItem", apkItem);
							titleUtil = new TitleUtil(ApkDetailActivity.this,
									mTopView, apkItem.appName, bundle,
									ApkDetailActivity.this);
						}
					}
				});
			}
			if (apkItem != null) {
				// apkItem=item;
				int status = apkItem.status;
				Bundle bundle = new Bundle();
				bundle.putParcelable("apkItem", apkItem);
				if (titleUtil != null) {
					titleUtil.setBundle(bundle);
				}
				System.out.println("============ before :" + status);
				apkItem.status = status;
				apkItem = setApkStatus(apkItem);
				System.out.println("============ after :" + apkItem.status);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mLoadingView.setVisibility(View.GONE);
						initViews();
					}
				});
			}
		} catch (IOException e) {
			if (!AndroidUtils.isNetworkAvailable(this)) {
				mDataHandler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
			} else {
				mDataHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
			}
		} catch (JSONException e) {
			mDataHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
		}
		if (apkItem != null) {
			db = new MarketDatabase(ApkDetailActivity.this);
			int typeid = apkItem.category;
			int appid = apkItem.appId;
			rating = db.selectRatingById(typeid, appid);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (rating == -1) {
						llgrade_click.setVisibility(View.VISIBLE);
						llgrade_noclick.setVisibility(View.GONE);
						btnGrade.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								int typeid = apkItem.category;
								int appid = apkItem.appId;
								rating = commitCommentRatingBar.getRating();
								db.addRatingApp(typeid, appid, rating);
								mDataHandler.sendEmptyMessage(EVENT_GRADE);
								btnGrade.setEnabled(false);
							}
						});
					} else {
						llgrade_click.setVisibility(View.GONE);
						llgrade_noclick.setVisibility(View.VISIBLE);
						displayCommentRatingBar.setRating(rating);
						displayCommentRatingBar.setClickable(false);
						displayCommentRatingBar.setIsIndicator(true);
					}
				}
			});
		} else {
			finish();
		}
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		System.out.println("=========onAppInstallOrUninstallDone");
		if (info.packageName.equals(apkItem.packageName)) {
			if (status == INSTALL_APP_DONE) {
				if (apkItem.versionCode == info.versionCode) {
					apkItem.status = AConstDefine.STATUS_APK_INSTALL_DONE;
					displayApkStatus(btnInstall, apkItem.status);
				}
			} else if (status == UNINSTALL_APP_DONE) {
				apkItem.status = AConstDefine.STATUS_APK_UNUPDATE;
				displayApkStatus(btnInstall, apkItem.status);
			}
		}
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName,
			int versionCode) {
		System.out.println("========= onAppStatusChange");
		if (packageName.equals(apkItem.packageName)
				&& versionCode == apkItem.versionCode) {
			if (isCancel && apkItem.status == STATUS_APK_INSTALL) {
				apkItem.status = STATUS_APK_UNINSTALL;
			} else if (isCancel && apkItem.status == STATUS_APK_UPDATE) {
				apkItem.status = STATUS_APK_UNUPDATE;
			} else if (!isCancel && apkItem.status == STATUS_APK_UNINSTALL) {
				apkItem.status = STATUS_APK_INSTALL;
			} else if (!isCancel && apkItem.status == STATUS_APK_UNUPDATE) {
				apkItem.status = STATUS_APK_UPDATE;
			}
			displayApkStatus(btnInstall, apkItem.status);
		} else {
			if (apkItem.historys != null && apkItem.historys.length > 0) {
				for (int i = 0; i < apkItem.historys.length; i++) {
					HistoryApkItem historyItem = apkItem.historys[i];
					if (packageName.equals(apkItem.packageName)
							&& versionCode == historyItem.versionCode) {
						System.out.println("==========" + historyItem.status);
						if (isCancel
								&& historyItem.status == STATUS_APK_INSTALL) {
							historyItem.status = STATUS_APK_UNINSTALL;
						} else if (isCancel
								&& historyItem.status == STATUS_APK_UPDATE) {
							historyItem.status = STATUS_APK_UNUPDATE;
						} else if (!isCancel
								&& historyItem.status == STATUS_APK_UNINSTALL) {
							historyItem.status = STATUS_APK_INSTALL;
						} else if (!isCancel
								&& historyItem.status == STATUS_APK_UNUPDATE) {
							historyItem.status = STATUS_APK_UPDATE;
						}
						displayApkStatus(btnInstall, apkItem.status);
						break;
					}
				}
			}
		}
	}

	@Override
	protected void onUpdateDataDone() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadingImage() {
	}

	@Override
	public void onClick() {
		if (svApkDetail != null) {
			// svApkDetail.scrollTo(0, 0);
			svApkDetail.smoothScrollTo(0, 0);
		}
	}
}