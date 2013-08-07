package com.dongji.market.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.PopupAdapter;
import com.dongji.market.application.AppMarket;
import com.dongji.market.database.MarketDatabase.Setting_Service;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.ADownloadService;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadService;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.ShareParams;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.pojo.NavigationInfo;
import com.dongji.market.pojo.SettingConf;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.protocol.DataUpdateService;
import com.dongji.market.widget.CustomIconAnimation;
import com.dongji.market.widget.CustomNoTitleDialog;
import com.dongji.market.widget.HorizontalScrollLayout;
import com.dongji.market.widget.HorizontalScrollLayout.OnPageChangedListener;
import com.dongji.market.widget.SlideMenu;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends ActivityGroup implements OnClickListener, OnPageChangedListener, OnItemClickListener, AConstDefine, OnToolBarBlankClickListener {

	private static final boolean DEBUG = true;

	private static final int EVENT_LOADING_PROGRESS = 0;
	private static final int EVENT_LOADDONE = 1;
	private static final int EVENT_CHANGE_EXIT_STATUS = 2;
	private static final int EVENT_LOADING_DATA = 3;
	private static final int EVENT_CHECK_APP_UPDATE = 4;

	private RadioButton mChoicenessButton;
	private HorizontalScrollLayout mMainLayout;

	private boolean isAnimRunning;

	private ImageView mSlideImageView; // 头部左右滑动控件
	private float slideLeft;

	private View mLeftBottomExpandView, mRightBottomExpandView;

	private static final int CHOICENESS_POSITION = 0, UPDATE_POSITION = 1, THEME_POSITION = 2, INSTALL_POSITION = 3, CHANNEL_POSITION = 4;

	private FrameLayout mContentLayout;
	private LinearLayout mBottomLeftPopup, mBottomRightPopup;
	private LinearLayout mTouchLayout;
	private RadioButton mAppBottomButton, mGameBottomButton;

	private LayoutInflater mInflater;

	private ProgressBar mProgressBar;

	private LinearLayout.LayoutParams mParams;

	private MyHandler mHandler;

	private TitleUtil titleUtil;

	private CustomNoTitleDialog mExitDialog;

	private SlideMenu mSlideMenu;

	private boolean isExit;

	private AppMarket mApp;

	private String[] activityIds;
	private RadioButton[] mTopButtons;

	private View mMaskView;

	private View mSoftView;
	private ImageView mTempIcon;
	private CustomIconAnimation iconAnim;

	private ArrayList<NavigationInfo> navigationInfos = null;
	private ListView mGameBottomListView;
	private ListView mAppBottomListView;

	private static final String APP_STRING = "应用";
	private static final String GAME_STRING = "游戏";

	private LinearLayout mTabLayout;
	private LinearLayout mMainBottomLayout;
	private boolean isScrollAnimRunning;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ADownloadService.isBackgroundRun = false;// 设置下载服务是否后台运行

		mApp = (AppMarket) getApplication();
		boolean flag = DJMarketUtils.isSaveFlow(this);// 是否开启流量模式
		mApp.setRemoteImage(flag);// 是否下载图片

		checkFirstLaunch();

		if (DEBUG)
			MobclickAgent.onError(this); // 友盟creash反馈

		initData();
		initViews();
		initHandler();
		registerPackageReceiver();

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
				mMaskView.setVisibility(View.GONE);
				return true;
			}
			if (event.getAction() == KeyEvent.ACTION_UP && Build.VERSION.SDK_INT > 11) { // google
																							// bug!
				getLocalActivityManager().removeAllActivities();
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		try {
			super.onResume();
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		MobclickAgent.onResume(this);
		if (titleUtil != null) {
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		try {
			super.onPause();
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		MobclickAgent.onPause(this);
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
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

	private void getDeleteData() {
		SharedPreferences sharedPreferences = getSharedPreferences(DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		long newTime = System.currentTimeMillis();
		long oldTime = sharedPreferences.getLong(SHARE_DELETEFILETIME, 0L);
		if ((newTime - oldTime) / (1000 * 60 * 60 * 24) > 7) {
			NetTool.deleteTempFile(MainActivity.this);
			Editor editor = sharedPreferences.edit();
			editor.putLong(SHARE_DELETEFILETIME, newTime);
			editor.commit();
		}
	}

	@Override
	public void onClick(View v) {
		BaseActivity mCurrentActivity = null;

		// if ( v.getId() == R.id.themeRecommendButton) {
		//
		// if (mMainBottomLayout.getVisibility() == View.VISIBLE) {
		// mMainBottomLayout.setVisibility(View.GONE);
		// }
		//
		// } else {
		//
		// if (mMainBottomLayout.getVisibility() == View.GONE) {
		// mMainBottomLayout.setVisibility(View.VISIBLE);
		// }
		// }

		switch (v.getId()) {
		case R.id.choicenessButton:
			initAnimation(v);
			// execute("choiceness", ChoicenessActivity.class, bundle);
			mMainLayout.snapToScreen(CHOICENESS_POSITION);
			break;
		case R.id.updateButton:
			initAnimation(v);
			// bundle.putInt("type", 1);
			// execute("update", UpdateActivity.class, bundle);
			mMainLayout.snapToScreen(UPDATE_POSITION);
			break;
		case R.id.installNecessaryButton:
			initAnimation(v);
			// bundle.putInt("type", 2);
			// execute("install", UpdateActivity.class, bundle);
			mMainLayout.snapToScreen(INSTALL_POSITION);
			break;
		case R.id.softChannelButton:
			initAnimation(v);
			// bundle.putInt("type", 3);
			// execute("channel", UpdateActivity.class, bundle);
			mMainLayout.snapToScreen(CHANNEL_POSITION);
			break;
		case R.id.themeRecommendButton:
			initAnimation(v);
			// bundle.putInt("type", 3);
			// execute("channel", UpdateActivity.class, bundle);
			mMainLayout.snapToScreen(THEME_POSITION);
			break;
		case R.id.appbutton:
			mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
			mCurrentActivity.onAppClick();
			break;
		case R.id.gamebutton:
			mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
			mCurrentActivity.onGameClick();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUpdateLoadedReceiver);
		titleUtil.unregisterMyReceiver(this);
		try {
			releaseRAM();
		} catch (SecurityException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
				mMaskView.setVisibility(View.GONE);
				return true;
			}

			if (mTouchLayout.getVisibility() == View.VISIBLE) {
				setVisibleForGone(mBottomLeftPopup);
				setVisibleForGone(mBottomRightPopup);
				mTouchLayout.setVisibility(View.GONE);
				return true;
			}
			if (isExit) {
				AndroidUtils.cancelToast();
				isExit = false;
				removeAllMessage();
				outLogin();
				if (DJMarketUtils.backgroundDownload(this)) {
					/*
					 * Intent serviceIntent = new Intent();
					 * serviceIntent.setClass(MainActivity.this,
					 * ADownloadService.class);
					 * serviceIntent.putExtra(FLAG_ISSTOPALLDWONLOAD, true);
					 * startService(serviceIntent);
					 */

					/*
					 * if (ADownloadService.getDownloadcountByStatus(
					 * STATUS_OF_PREPAREDOWNLOAD, STATUS_OF_DOWNLOADING) > 0 ||
					 * ADownloadService.getUpdateCountByStatus(
					 * STATUS_OF_PREPAREUPDATE, STATUS_OF_UPDATEING) != 0) {
					 * showExitAppDialog(); return true; }
					 */
					if (DownloadService.mDownloadService != null && DownloadService.mDownloadService.hasDownloading()) {
						showExitAppDialog();
						return true;
					} else {
						stopService(new Intent(this, ADownloadService.class));
						stopService(new Intent(this, DataUpdateService.class));
						stopService(new Intent(this, DownloadService.class));

						NetTool.cancelNotification(this, 4);
					}
				} else {
					// closeApp();
					stopService(new Intent(this, ADownloadService.class));
					stopService(new Intent(this, DataUpdateService.class));
					stopService(new Intent(this, DownloadService.class));

					NetTool.cancelNotification(this, 4);
				}
			} else {
				isExit = true;
				AndroidUtils.showToast(this, R.string.back_message);
				mHandler.sendEmptyMessageDelayed(EVENT_CHANGE_EXIT_STATUS, 2500);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 释放内存
	 */
	private void releaseRAM() {
		// 方法一,退出虚拟机
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
		System.exit(0);
		// 方法二,kill掉当前应用进程
		// android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 初始化首页框架视图
	 */
	private void initViews() {
		mInflater = LayoutInflater.from(this);
		mMainLayout = (HorizontalScrollLayout) findViewById(R.id.mainlayout);
		mMainLayout.setOnPageChangedListener(this);
		initHorizontalScrollLayout();

		mContentLayout = (FrameLayout) findViewById(R.id.contentlayout);

		View mTopView = findViewById(R.id.main_top);
		titleUtil = new TitleUtil(this, mTopView, "", getIntent().getExtras(), this);
		mSoftView = (View) findViewById(R.id.softmanagerbutton);

		mProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);

		initBottomExpandView();

		initTopButton();

		initBottomButton();

		initSlideImageView();

		initTouchLayout();

		mChoicenessButton.performClick();

	}

	/**
	 * 列表滚动时菜单滑入滑出的动画
	 * 
	 * @param mListView
	 */
	void setListViewSlide(ListView mListView) {
		if (mSlideMenu == null) {
			mSlideMenu = new SlideMenu();
			mSlideMenu.setMenuLayout(findViewById(R.id.menu_layout));
		}
		mSlideMenu.setListView(mListView);
	}

	/**
	 * 判断是否显示setting popupWindow
	 * 
	 * @return
	 */
	public boolean showSettingPop() {
		if (mSlideMenu != null) {
			return mSlideMenu.isShowSettingPop();
		}
		return false;
	}

	/**
	 * 显示菜单栏
	 */
	public void showMenuBar() {
		if (mSlideMenu != null) {
			mSlideMenu.showMenu();
		}
	}

	private void initBottomExpandView() {
		mLeftBottomExpandView = findViewById(R.id.leftBottomExpand);
		mRightBottomExpandView = findViewById(R.id.rightBottomExpand);
	}

	/**
	 * 注册手机应用安装卸载广播
	 */
	private void registerPackageReceiver() {
		// loginReceiver = new CommonReceiver();
		// registerReceiver(loginReceiver, new IntentFilter(
		// "com.dongji.market.loginReceiver"));

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_CHECK_DOWNLOAD);
		registerReceiver(mUpdateLoadedReceiver, intentFilter);

	}

	/**
	 * 软件分类底部分类按钮点击处理
	 * 
	 * @param isApp
	 */
	private void onChannelBottomClick(boolean isApp) {
		BaseActivity mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
		if (isApp) {
			if (mCurrentActivity.isAppClicked()) {
				if (mLeftBottomExpandView.getVisibility() == View.VISIBLE) {
					showBottomPop(true);
				}
			} else {
				mCurrentActivity.onAppClick();
			}
			setBottomExpandViewVisible(new boolean[] { true, true });
		} else {
			if (!mCurrentActivity.isAppClicked()) {
				if (mRightBottomExpandView.getVisibility() == View.VISIBLE) {
					showBottomPop(false);
				}
			} else {
				mCurrentActivity.onGameClick();
			}
			setBottomExpandViewVisible(new boolean[] { true, false });
		}
	}

	public void initBottomView(List<ChannelListInfo> list) {
		if (list != null && list.size() > 0) {
			String allString = getString(R.string.all_txt);
			List<ChannelListInfo> appListInfo = new ArrayList<ChannelListInfo>();
			List<ChannelListInfo> gameListInfo = new ArrayList<ChannelListInfo>();
			int appId = 0;
			int gameId = 0;
			for (int i = 0; i < list.size(); i++) {
				ChannelListInfo info = list.get(i);
				if (APP_STRING.equals(info.name)) {
					appId = info.id;
					info.name = allString;
					appListInfo.add(info);
				} else if (GAME_STRING.equals(info.name)) {
					gameId = info.id;
					info.name = allString;
					gameListInfo.add(info);
				}
			}
			for (int i = 0; i < list.size(); i++) {
				ChannelListInfo info = list.get(i);
				if (info.parentId == appId) {
					appListInfo.add(info);
				} else if (info.parentId == gameId) {
					gameListInfo.add(info);
				}
			}
			if (mBottomLeftPopup == null) {
				mBottomLeftPopup = initBottomPopup(true, appListInfo);
				mContentLayout.addView(mBottomLeftPopup);
			}
			if (mBottomRightPopup == null) {
				mBottomRightPopup = initBottomPopup(false, gameListInfo);
				mContentLayout.addView(mBottomRightPopup);
			}
		}
	}

	private void showBottomPop(boolean isLeft) {
		if (isLeft) {
			if (mBottomLeftPopup != null) {
				mBottomLeftPopup.startAnimation(AnimationUtils.loadAnimation(this, R.anim.up_fade_in));
				mBottomLeftPopup.setVisibility(View.VISIBLE);
				mTouchLayout.setVisibility(View.VISIBLE);
			}
		} else {
			if (mBottomRightPopup != null) {
				mBottomRightPopup.startAnimation(AnimationUtils.loadAnimation(this, R.anim.up_fade_in));
				mBottomRightPopup.setVisibility(View.VISIBLE);
				mTouchLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private void initSlideImageView() {
		mSlideImageView = (ImageView) findViewById(R.id.slideimageview);
		DisplayMetrics dm = AndroidUtils.getScreenSize(this);
		int num = AndroidUtils.dip2px(this, 2);
		float singleWidth = (dm.widthPixels - num * 3) / 5.0f;
		LayoutParams mParams = (LayoutParams) mSlideImageView.getLayoutParams();
		mParams.width = (int) singleWidth;
		mSlideImageView.setLayoutParams(mParams);
	}

	private void initAnimation(final View v) {
		if (!isAnimRunning && slideLeft != v.getLeft()) {
			isAnimRunning = true;
			TranslateAnimation mAnimation = new TranslateAnimation(slideLeft, v.getLeft(), 0, 0);
			mAnimation.setDuration(300L);
			mAnimation.setFillEnabled(true);
			mAnimation.setFillAfter(true);
			mAnimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					slideLeft = v.getLeft();
					isAnimRunning = false;
				}
			});
			mSlideImageView.startAnimation(mAnimation);
		}
	}

	private void initTopButton() {
		mTabLayout = (LinearLayout) findViewById(R.id.maintablayout);

		mChoicenessButton = (RadioButton) findViewById(R.id.choicenessButton);
		RadioButton mUpdateButton = (RadioButton) findViewById(R.id.updateButton);
		RadioButton mInstallNecessaryButton = (RadioButton) findViewById(R.id.installNecessaryButton);
		RadioButton mSoftChannelButton = (RadioButton) findViewById(R.id.softChannelButton);
		RadioButton mThemeRecommendButton = (RadioButton) findViewById(R.id.themeRecommendButton);
		mChoicenessButton.setOnClickListener(this);
		mUpdateButton.setOnClickListener(this);
		mInstallNecessaryButton.setOnClickListener(this);
		mSoftChannelButton.setOnClickListener(this);
		mThemeRecommendButton.setOnClickListener(this);

		mTopButtons = new RadioButton[] { mChoicenessButton, mUpdateButton, mThemeRecommendButton, mInstallNecessaryButton, mSoftChannelButton };
	}

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessageDelayed(EVENT_LOADING_DATA, 1500L);
		mHandler.sendEmptyMessage(EVENT_CHECK_APP_UPDATE);
	}

	/**
	 * 初始化水平滑动布局
	 */
	private void initHorizontalScrollLayout() {
		mMainLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
		mMainLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
		mMainLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
		mMainLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
		mMainLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
	}

	private LinearLayout initBottomPopup(boolean isLeft, List<ChannelListInfo> list) {
		LinearLayout view = (LinearLayout) mInflater.inflate(R.layout.layout_popup_bottom_left, null);
		LinearLayout mListViewBg = (LinearLayout) view.findViewById(R.id.listview_bg);
		TextView mTextView = (TextView) view.findViewById(R.id.shadowtextview);
		ListView mListView = (ListView) view.findViewById(R.id.poplistview);
		PopupAdapter mAdapter = new PopupAdapter(this, list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		DisplayMetrics dm = AndroidUtils.getScreenSize(this);
		int width = dm.widthPixels / 2 + AndroidUtils.dip2px(this, 3);
		int height = (int) (dm.heightPixels / 2.8f);
		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(width, height);
		FrameLayout.LayoutParams mTextViewParams = (FrameLayout.LayoutParams) mTextView.getLayoutParams();
		int padding = AndroidUtils.dip2px(this, 4.0f);
		if (isLeft) {
			mAppBottomListView = mListView;
			mParams.gravity = Gravity.BOTTOM;
			mTextViewParams.gravity = Gravity.RIGHT;
			mTextView.setBackgroundResource(R.drawable.pop_left_shadow);
			mTextViewParams.rightMargin = AndroidUtils.dip2px(this, 0);
			mListViewBg.setBackgroundResource(R.drawable.pop_left_top);
			mListViewBg.setPadding(0, 0, padding, 0);
		} else {
			mGameBottomListView = mListView;
			mParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
			mTextViewParams.gravity = Gravity.LEFT;
			mTextView.setBackgroundResource(R.drawable.pop_right_shadow);
			mTextViewParams.leftMargin = AndroidUtils.dip2px(this, 0);
			mListViewBg.setPadding(padding, 0, 0, 0);
			mListViewBg.setBackgroundResource(R.drawable.pop_right_top);
		}
		mTextView.setLayoutParams(mTextViewParams);
		view.setLayoutParams(mParams);
		view.setVisibility(View.GONE);
		return view;
	}

	private void initBottomButton() {
		mMainBottomLayout = (LinearLayout) findViewById(R.id.mainbottomlayout);

		mAppBottomButton = (RadioButton) findViewById(R.id.appbutton);
		mGameBottomButton = (RadioButton) findViewById(R.id.gamebutton);
		mAppBottomButton.setOnClickListener(this);
		mGameBottomButton.setOnClickListener(this);
	}

	private void initTouchLayout() {
		mTouchLayout = (LinearLayout) findViewById(R.id.touchlayout);
		mTouchLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				setVisibleForGone(mBottomLeftPopup);
				setVisibleForGone(mBottomRightPopup);
				mTouchLayout.setVisibility(View.GONE);
				return true;
			}
		});
	}

	private void setVisibleForGone(View v) {
		if (v != null && v.getVisibility() == View.VISIBLE) {
			v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.down_fade_out));
			v.setVisibility(View.GONE);
		}
	}

	private void execute(String id, Class<?> claxx, Bundle bundle) {
		Intent intent = new Intent(this, claxx);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		mMainLayout.removeAllViews();
		if (mParams == null) {
			mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		}
		if (getLocalActivityManager().getActivity(id) != null) {
			boolean isAppClicked = ((BaseActivity) getLocalActivityManager().getActivity(id)).isAppClicked();
			if (isAppClicked) {
				mAppBottomButton.setChecked(true);
			} else {
				mGameBottomButton.setChecked(true);
			}
			setBottomExpandViewVisible(new boolean[] { "channel".equals(id), isAppClicked });
		} else {
			if ("channel".equals(id)) {
				mLeftBottomExpandView.setVisibility(View.VISIBLE);
			} else {
				mLeftBottomExpandView.setVisibility(View.GONE);
				mRightBottomExpandView.setVisibility(View.GONE);
			}
			mAppBottomButton.setChecked(true);
		}
		mMainLayout.addView(getLocalActivityManager().startActivity(id, intent).getDecorView(), mParams);
	}

	/**
	 * 设置底部应用游戏箭头是否显示
	 * 
	 * @param flag
	 */
	private void setBottomExpandViewVisible(boolean... flag) {
		if (flag[0]) {
			if (flag[1]) {
				mLeftBottomExpandView.setVisibility(View.VISIBLE);
				mRightBottomExpandView.setVisibility(View.GONE);
			} else {
				mLeftBottomExpandView.setVisibility(View.GONE);
				mRightBottomExpandView.setVisibility(View.VISIBLE);
			}
		} else {
			mLeftBottomExpandView.setVisibility(View.GONE);
			mRightBottomExpandView.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化内容activityID
	 */
	private void initData() {
		activityIds = new String[5];
		activityIds[0] = "choiceness";
		activityIds[1] = "update";
		activityIds[2] = "theme";
		activityIds[3] = "install";
		activityIds[4] = "channel";
	}

	/**
	 * 检查是否是第一次使用该程序
	 * 
	 * @return
	 */
	private void checkFirstLaunch() {
		SharedPreferences mSharedPreferences = getSharedPreferences(this.getPackageName() + "_temp", Context.MODE_PRIVATE);
		boolean firstLaunch = mSharedPreferences.getBoolean(ShareParams.FIRST_LAUNCHER, true);
		if (firstLaunch) {
			boolean hasShortcut = checkExistsShortcut();
			if (!hasShortcut) {
				createShortcut();
			}
			changeFirseLaunch(mSharedPreferences);
			initSettingConfig();
			// initMaskView();
		}
	}

	private void initMaskView() {
		mMaskView = findViewById(R.id.main_mask_layout);
		mMaskView.setVisibility(View.VISIBLE);
		mMaskView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mMaskView.setVisibility(View.GONE);
				return false;
			}
		});
	}

	/**
	 * 初始化设置信息
	 */
	private void initSettingConfig() {
		Setting_Service settingDB = new Setting_Service(this);
		settingDB.add(new SettingConf("update_msg", 1));
		settingDB.add(new SettingConf("auto_del_pkg", 0));
		settingDB.add(new SettingConf("save_flow", 0));
		settingDB.add(new SettingConf("set_root", 0));
		settingDB.add(new SettingConf("auto_install", 0));
		settingDB.add(new SettingConf("only_wifi", 1));
		settingDB.add(new SettingConf("limit_flow", 50));
		settingDB.add(new SettingConf("download_bg", 1));
		settingDB.add(new SettingConf("auto_update", 0));
		settingDB.add(new SettingConf("sina_login", 0));
		settingDB.add(new SettingConf("tencent_login", 0));
		settingDB.add(new SettingConf("renren_login", 0));
	}

	private void changeFirseLaunch(SharedPreferences mSharedPreferences) {
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(ShareParams.FIRST_LAUNCHER, false);
		mEditor.commit();
	}

	/**
	 * 创建桌面快捷方式
	 */
	private void createShortcut() {
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		// 快捷方式的名称
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
		shortcut.putExtra("duplicate", false); // 不允许重复创建

		// 指定当前的Activity为快捷方式启动的对象: com.everest.video.VideoPlayer
		// 注意: ComponentName的第二个参数必须加上点号(.)，否则快捷方式无法启动相应程
		ComponentName comp = new ComponentName(this.getPackageName(), ShareParams.LAUNCHER_STR);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));

		// 快捷方式的图
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

		sendBroadcast(shortcut);
	}

	/***
	 * 检查桌面是否存在此快捷方式
	 */
	private boolean checkExistsShortcut() {
		boolean result = false;
		// 获取当前应用名称
		String title = null;
		try {
			final PackageManager pm = getPackageManager();
			title = pm.getApplicationLabel(pm.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)).toString();
		} catch (Exception e) {
		}

		final String uriStr;
		if (android.os.Build.VERSION.SDK_INT < 8) {
			uriStr = "content://com.android.launcher.settings/favorites?notify=true";
		} else {
			uriStr = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		final Uri CONTENT_URI = Uri.parse(uriStr);
		final Cursor c = getContentResolver().query(CONTENT_URI, null, "title=?", new String[] { title }, null);
		if (c != null && c.getCount() > 0) {
			result = true;
		}
		return result;
	}

	private void showExitAppDialog() {
		if (!isFinishing()) {
			if (mExitDialog == null) {
				mExitDialog = new CustomNoTitleDialog(this);
				mExitDialog.setMessage(R.string.background_download_msg);
				mExitDialog.setNegativeButton(getString(R.string.no), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mExitDialog.dismiss();
						stopService(new Intent(MainActivity.this, ADownloadService.class));
						stopService(new Intent(MainActivity.this, DataUpdateService.class));
						stopService(new Intent(MainActivity.this, DownloadService.class));

						NetTool.cancelNotification(MainActivity.this, 4);

						finish();
						// closeApp();
					}
				}).setNeutralButton(getString(R.string.ok), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mExitDialog.dismiss();
						// ADownloadService.isBackgroundRun = true;
						finish();
						// service 继续后台下载
						// closeApp();
					}
				});
			}
			if (mExitDialog != null) {
				mExitDialog.show();
			}
		}
	}

	public void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setProgress(0);
		mHandler.sendEmptyMessage(EVENT_LOADING_PROGRESS);
	}

	public void stopProgressBar() {
		if (mHandler.hasMessages(EVENT_LOADING_PROGRESS)) {
			mHandler.removeMessages(EVENT_LOADING_PROGRESS);
		}
		if (mHandler.hasMessages(EVENT_LOADDONE)) {
			mHandler.removeMessages(EVENT_LOADDONE);
		}
	}

	public void onProgressBarDone() {
		mHandler.sendEmptyMessage(EVENT_LOADDONE);
	}

	public void progressBarGone() {
		if (mHandler != null && mHandler.hasMessages(EVENT_LOADING_PROGRESS)) {
			mHandler.removeMessages(EVENT_LOADING_PROGRESS);
		}
		if (mHandler != null && mHandler.hasMessages(EVENT_LOADDONE)) {
			mHandler.removeMessages(EVENT_LOADDONE);
		}
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	/**
	 * 退出前清除所有消息
	 */
	private void removeAllMessage() {
		if (mHandler != null) {
			if (mHandler.hasMessages(EVENT_LOADING_PROGRESS)) {
				mHandler.removeMessages(EVENT_LOADING_PROGRESS);
			}
			if (mHandler.hasMessages(EVENT_LOADDONE)) {
				mHandler.removeMessages(EVENT_LOADDONE);
			}
			if (mHandler.hasMessages(EVENT_CHANGE_EXIT_STATUS)) {
				mHandler.removeMessages(EVENT_CHANGE_EXIT_STATUS);
			}
		}
	}

	/**
	 * 关闭当前应用
	 */
	private void closeApp() {
		stopService(new Intent(this, ADownloadService.class));
		outLogin();
		finish();
		Process.killProcess(Process.myPid());
		System.exit(0);
	}

	/**
	 * 退出应用时退出登录
	 */
	private void outLogin() {
		// SharedPreferences loginPref = getSharedPreferences(
		// AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		// Editor editor = loginPref.edit();
		// editor.putInt("loginState", 0);
		// editor.putString("sina_user_name", "");
		// editor.commit();
		LoginParams loginParams = ((AppMarket) getApplicationContext()).getLoginParams();
		loginParams.setSessionId(null);
		loginParams.setUserName(null);
		// loginParams.setLoginState(AConstDefine.LOGIN_OUT_FLAG);
		loginParams.setSinaUserName(null);
		loginParams.setTencentUserName(null);
	}

	@Override
	public void onPageChanged(int position) {
		BaseActivity mCurrentActivity = (BaseActivity) getLocalActivityManager().getActivity(activityIds[position]);
		Intent intent = null;
		Bundle bundle = null;
		progressBarGone();
		mTopButtons[position].setChecked(true);
		initAnimation(mTopButtons[position]);

		if (position == THEME_POSITION) {

			if (mMainBottomLayout.getVisibility() == View.VISIBLE) {
				mMainBottomLayout.setVisibility(View.GONE);
			}

		} else {

			if (mMainBottomLayout.getVisibility() == View.GONE) {
				mMainBottomLayout.setVisibility(View.VISIBLE);
			}

			if (mCurrentActivity != null) {
				boolean isAppClicked = mCurrentActivity.isAppClicked();
				if (isAppClicked) {
					mAppBottomButton.setChecked(true);
				} else {
					mGameBottomButton.setChecked(true);
				}
				// setBottomExpandViewVisible(new boolean[] {
				// "channel".equals(activityIds[position]), isAppClicked });
			} else {
				/*
				 * if ("channel".equals(activityIds[position])) {
				 * mLeftBottomExpandView.setVisibility(View.VISIBLE); } else {
				 */
				mLeftBottomExpandView.setVisibility(View.GONE);
				mRightBottomExpandView.setVisibility(View.GONE);
				// }
				mAppBottomButton.setChecked(true);
			}

		}

		switch (position) {
		case CHOICENESS_POSITION:
			intent = new Intent(this, ChoicenessActivity.class);
			bundle = new Bundle();
			// if (navigationInfos != null) {
			// NavigationInfo info = navigationInfos.get(CHOICENESS_POSITION);
			// bundle.putParcelable("navigation", info);
			bundle.putInt("type", CHOICENESS_POSITION);
			// }
			break;
		case UPDATE_POSITION:
			intent = new Intent(this, UpdateActivity.class);
			bundle = new Bundle();
			// if (navigationInfos != null) {
			// NavigationInfo info = navigationInfos.get(2);
			// bundle.putParcelable("navigation", info);
			// bundle.putInt("type", UPDATE_POSITION);
			intent.putExtra("type", UPDATE_POSITION);
			// }
			break;
		case THEME_POSITION:
			intent = new Intent(this, ThemeActivity.class);
			bundle = new Bundle();
			bundle.putInt("type", THEME_POSITION);
			break;
		case INSTALL_POSITION:
			intent = new Intent(this, UpdateActivity.class);
			bundle = new Bundle();
			// if (navigationInfos != null) {
			// NavigationInfo info = navigationInfos.get(1);
			// bundle.putParcelable("navigation", info);
			// bundle.putInt("type", INSTALL_POSITION);
			intent.putExtra("type", INSTALL_POSITION);
			// }
			break;
		case CHANNEL_POSITION:
			// intent = new Intent(this, UpdateActivity.class);
			intent = new Intent(this, ChannelActivity.class);
			bundle = new Bundle();
			bundle.putInt("type", CHANNEL_POSITION);
			break;
		}
		if (mCurrentActivity != null) {
			getLocalActivityManager().startActivity(activityIds[position], intent);
		} else {
			// intent.putExtras(bundle);
			mMainLayout.removeViewAt(position);
			mMainLayout.addView(getLocalActivityManager().startActivity(activityIds[position], intent).getDecorView(), position);
		}

	}

	public void setInterceptRange(View v) {
		mMainLayout.setInterceptTouchView(v, CHOICENESS_POSITION);
	}

	/**
	 * 是否隐藏顶部和底部
	 * 
	 * @param flag
	 */
	void scrollOperation(boolean flag) {
		if (flag) {
			if (!isScrollAnimRunning && mMainBottomLayout.getVisibility() == View.VISIBLE) {
				Animation mTopCollapseAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_tab_collapse);
				Animation mBottomCollapseAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bottom_collapse);
				mTopCollapseAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						isScrollAnimRunning = false;
						mTabLayout.setVisibility(View.GONE);
					}
				});
				mBottomCollapseAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						isScrollAnimRunning = false;
						mMainBottomLayout.setVisibility(View.GONE);
					}
				});
				isScrollAnimRunning = true;
				// mTabLayout.startAnimation(mTopCollapseAnimation);
				mMainBottomLayout.startAnimation(mBottomCollapseAnimation);
			}
		} else {
			if (mMainBottomLayout.getVisibility() == View.GONE) {
				// Animation
				// mTopExpandAnimation=AnimationUtils.loadAnimation(this,
				// R.anim.anim_tab_expand);
				Animation mBottomExpandAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bottom_expand);
				// mTabLayout.setVisibility(View.VISIBLE);
				mMainBottomLayout.setVisibility(View.VISIBLE);
				// mTabLayout.startAnimation(mTopExpandAnimation);
				mMainBottomLayout.startAnimation(mBottomExpandAnimation);
			}
		}
	}

	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_LOADING_PROGRESS:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = mProgressBar.getProgress();
						if (progress < 80) {
							mProgressBar.setProgress(progress + 10);
							sendEmptyMessageDelayed(EVENT_LOADING_PROGRESS, 300);
						}
					}
				});
				break;
			case EVENT_LOADDONE:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = mProgressBar.getProgress();
						if (progress == 100) {
							mProgressBar.setVisibility(View.GONE);
						} else {
							mProgressBar.setProgress(100);
							sendEmptyMessageDelayed(EVENT_LOADDONE, 500);
						}
					}
				});
				break;
			case EVENT_CHANGE_EXIT_STATUS:
				isExit = false;
				break;
			case EVENT_LOADING_DATA:
				Intent intent = new Intent(MainActivity.this, DataUpdateService.class);
				startService(intent);

				Intent downloadIntent = new Intent(MainActivity.this, DownloadService.class);
				startService(downloadIntent);
				break;
			case EVENT_CHECK_APP_UPDATE:
				String downloadUrl = DataManager.newInstance().checkAppUpdate(MainActivity.this);
				if (!TextUtils.isEmpty(downloadUrl)) {
					DJMarketUtils.appUpdate(MainActivity.this, downloadUrl);
				}
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.poplistview:
			BaseActivity mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
			if (mCurrentActivity.isAppClicked()) {
				setVisibleForGone(mBottomLeftPopup);
			} else {
				setVisibleForGone(mBottomRightPopup);
			}
			PopupAdapter mAdapter = (PopupAdapter) parent.getAdapter();
			mCurrentActivity.onItemClick((ChannelListInfo) mAdapter.getItem(position));
			mTouchLayout.setVisibility(View.GONE);
			break;
		}
	}

	public void setNavigationList(ArrayList<NavigationInfo> list) {
		if (list != null) {
			this.navigationInfos = list;
		}
	}

	public boolean performClickOnBottomButton(boolean isApp) {
		if (isApp) {
			if (mAppBottomListView != null) {
				mAppBottomListView.performItemClick(mAppBottomListView, 0, 0);
				return true;
			}
		} else {
			if (mGameBottomListView != null) {
				mGameBottomListView.performItemClick(mGameBottomListView, 0, 0);
				return true;
			}
		}
		return false;
	}

	public void onStartDownload(Map<String, Object> map) {
		int iconX = (Integer) map.get("X");
		int iconY = (Integer) map.get("Y") - mSoftView.getHeight() + AndroidUtils.getStatusBarInfo(this).top;
		Drawable icon = (Drawable) map.get("icon");
		if (mTempIcon == null) {
			mTempIcon = (ImageView) findViewById(R.id.tempIcon);
			iconAnim = new CustomIconAnimation(this);
		}
		iconAnim.startAnimation(iconX, iconY, icon, mTempIcon, mSoftView);
	}

	private BroadcastReceiver mUpdateLoadedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DownloadConstDefine.BROADCAST_ACTION_CHECK_DOWNLOAD.equals(intent.getAction())) {
				DownloadUtils.startAllDownload(context, mApp.isIs3GDownloadPrompt());
			}
		}
	};

	@Override
	public void onClick() {
		BaseActivity mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
		if (mCurrentActivity != null) {
			mCurrentActivity.OnToolBarClick();
		}
	}

	public void setBottomGone() {
		if (mMainBottomLayout.getVisibility() == View.VISIBLE) {
			mMainBottomLayout.setVisibility(View.GONE);
		}
	}

	public void setBottomVisible() {
		if (mMainBottomLayout.getVisibility() == View.GONE) {
			mMainBottomLayout.setVisibility(View.VISIBLE);
		}
	}
}