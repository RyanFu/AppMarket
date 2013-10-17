package com.dongji.market.activity;

import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.dongji.market.R;
import com.dongji.market.application.AppMarket;
import com.dongji.market.database.MarketDatabase.Setting_Service;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DownloadUtils;
import com.dongji.market.helper.NetTool;
import com.dongji.market.helper.ShareParams;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.pojo.SettingConf;
import com.dongji.market.protocol.DataUpdateService;
import com.dongji.market.service.DownloadService;
import com.dongji.market.widget.CustomNoTitleDialog;
import com.dongji.market.widget.HorizontalScrollLayout;
import com.dongji.market.widget.HorizontalScrollLayout.OnPageChangedListener;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends ActivityGroup implements OnClickListener, OnPageChangedListener, AConstDefine, OnToolBarBlankClickListener {

	private static final boolean DEBUG = true;
	private static final int EVENT_CHANGE_EXIT_STATUS = 2;// 改变退出状态
	private static final int EVENT_LOADING_DATA = 3;// 加载数据
	private static final int EVENT_CHECK_APP_UPDATE = 4;// 检查更新

	private RadioButton mChoicenessButton;// 推荐
	private HorizontalScrollLayout mMainLayout;// 水平滑动布局

	private boolean isAnimRunning;// 动画运行中

	private ImageView mSlideImageView; // 头部左右滑动控件
	private float slideLeft;// 滑动线距左边距离

	private static final int CHOICENESS_POSITION = 0, UPDATE_POSITION = 1, THEME_POSITION = 2, INSTALL_POSITION = 3, CHANNEL_POSITION = 4;// 推荐、新品、专题、必备、分类

	private RadioButton mAppBottomButton, mGameBottomButton;

	private LayoutInflater mInflater;

	private MyHandler mHandler;

	private TitleUtil titleUtil;

	private CustomNoTitleDialog mExitDialog;

	private boolean isExit;

	private AppMarket mApp;

	private String[] activityIds;
	private RadioButton[] mTopButtons;
	private LinearLayout mMainBottomLayout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mApp = (AppMarket) getApplication();
		boolean flag = DJMarketUtils.isSaveFlow(this);// 是否开启流量模式
		mApp.setRemoteImage(!flag);// 是否下载图片
		mApp.setIsPhone(DJMarketUtils.isPhone(this));//判断是不是手机

		checkFirstLaunch();
		if (DEBUG)
			MobclickAgent.onError(this); // 友盟creash反馈
		initData();
		initViews();
		initHandler();
		registerPackageReceiver();
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
			changeFirstLaunch(mSharedPreferences);
			initSettingConfig();
		}
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

	/**
	 * 修改配置信息，首次运行
	 * 
	 * @param mSharedPreferences
	 */
	private void changeFirstLaunch(SharedPreferences mSharedPreferences) {
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(ShareParams.FIRST_LAUNCHER, false);
		mEditor.commit();
	}

	/**
	 * 初始化设置信息，向数据库中写入
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
	 * 初始化handler，并发送数据加载、检查app更新消息
	 */
	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessageDelayed(EVENT_LOADING_DATA, 1500L);
		mHandler.sendEmptyMessage(EVENT_CHECK_APP_UPDATE);
	}

	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_CHANGE_EXIT_STATUS:// 改变退出状态
				isExit = false;
				break;
			case EVENT_LOADING_DATA:
				Intent intent = new Intent(MainActivity.this, DataUpdateService.class);// 开启数据更新服务
				startService(intent);

				Intent downloadIntent = new Intent(MainActivity.this, DownloadService.class);// 开启下载服务
				startService(downloadIntent);
				break;
			case EVENT_CHECK_APP_UPDATE:// 检查更新服务
				// String downloadUrl =
				// DataManager.newInstance().checkAppUpdate(MainActivity.this);
				// if (!TextUtils.isEmpty(downloadUrl)) {
				// DJMarketUtils.appUpdate(MainActivity.this, downloadUrl);
				// }
				break;
			}
		}
	}

	/**
	 * 初始化首页框架视图
	 */
	private void initViews() {
		mInflater = LayoutInflater.from(this);
		mMainLayout = (HorizontalScrollLayout) findViewById(R.id.mainlayout);// 水平滑动框架
		mMainLayout.setOnPageChangedListener(this);
		initHorizontalScrollLayout();
		View mTopView = findViewById(R.id.main_top);// actionbar
		titleUtil = new TitleUtil(this, mTopView, "", getIntent().getExtras(), this);// 初始化actionbar

		initTopButton();

		initBottomButton();

		initSlideImageView();

		mChoicenessButton.performClick();
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

	/**
	 * 初始化标签按钮
	 */
	private void initTopButton() {
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

	/**
	 * 初始化游戏、应用切换按钮
	 */
	private void initBottomButton() {
		mMainBottomLayout = (LinearLayout) findViewById(R.id.mainbottomlayout);
		mAppBottomButton = (RadioButton) findViewById(R.id.appbutton);
		mGameBottomButton = (RadioButton) findViewById(R.id.gamebutton);
		mAppBottomButton.setOnClickListener(this);
		mGameBottomButton.setOnClickListener(this);
	}

	/**
	 * 初始化标签页滑动线
	 */
	private void initSlideImageView() {
		mSlideImageView = (ImageView) findViewById(R.id.slideimageview);
		DisplayMetrics dm = DJMarketUtils.getScreenSize(this);
		int num = DJMarketUtils.dip2px(this, 2);
		float singleWidth = (dm.widthPixels - num * 3) / 5.0f;
		LayoutParams mParams = (LayoutParams) mSlideImageView.getLayoutParams();
		mParams.width = (int) singleWidth;
		mSlideImageView.setLayoutParams(mParams);
	}

	/**
	 * 注册检查所有下载广播接收器
	 */
	private void registerPackageReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AConstDefine.BROADCAST_ACTION_CHECK_DOWNLOAD);
		registerReceiver(mUpdateLoadedReceiver, intentFilter);
	}

	private BroadcastReceiver mUpdateLoadedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (AConstDefine.BROADCAST_ACTION_CHECK_DOWNLOAD.equals(intent.getAction())) {
				DownloadUtils.startAllDownload(context, mApp.isIs3GDownloadPrompt());
			}
		}
	};

	@Override
	public void onClick(View v) {
		BaseActivity mCurrentActivity = null;
		switch (v.getId()) {
		case R.id.choicenessButton:// 推荐按钮
			initAnimation(v);
			mMainLayout.snapToScreen(CHOICENESS_POSITION);
			break;
		case R.id.updateButton:// 新品按钮
			initAnimation(v);
			mMainLayout.snapToScreen(UPDATE_POSITION);
			break;
		case R.id.installNecessaryButton:// 必备按钮
			initAnimation(v);
			mMainLayout.snapToScreen(INSTALL_POSITION);
			break;
		case R.id.softChannelButton:// 分类按钮
			initAnimation(v);
			mMainLayout.snapToScreen(CHANNEL_POSITION);
			break;
		case R.id.themeRecommendButton:// 专题按钮
			initAnimation(v);
			mMainLayout.snapToScreen(THEME_POSITION);
			break;
		case R.id.appbutton:// 底部应用按钮
			mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
			mCurrentActivity.onAppClick();
			break;
		case R.id.gamebutton:// 底部游戏按钮
			mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
			mCurrentActivity.onGameClick();
			break;
		}
	}

	/**
	 * 初始化滑动线动画
	 * 
	 * @param v
	 */
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

	@Override
	protected void onResume() {
		try {
			super.onResume();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		MobclickAgent.onResume(this);
		if (titleUtil != null) {
			titleUtil.sendRefreshHandler();// 刷新actionbar
		}
	}

	@Override
	protected void onPause() {
		try {
			super.onPause();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		MobclickAgent.onPause(this);
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();// 停止刷新actionbar
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUpdateLoadedReceiver);// 注销检查所有下载广播接收器(接收检查所有下载广播)
		titleUtil.unregisterMyReceiver(this);// 注销流量用完广播接收器
		try {
			releaseRAM();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 显示设置项PopupWindow
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (event.getAction() == KeyEvent.ACTION_UP && Build.VERSION.SDK_INT > 11) { // google
				getLocalActivityManager().removeAllActivities();
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {// 按一次回退键
			if (isExit) {// 进行一些退出操作
				isExit = false;
				removeAllMessage();// 清除messagequeue里面所有消息
				outLogin();// 退出登陆
				if (DJMarketUtils.backgroundDownload(this)) {// 是否使用后台下载
					if (DownloadService.mDownloadService != null && DownloadService.mDownloadService.hasDownloading()) {// 下载服务是否开启且是否正在下载
						showExitAppDialog();// 是否退出下载
						return true;
					} else {
						stopService(new Intent(this, DataUpdateService.class));// 停止数据更新服务
						stopService(new Intent(this, DownloadService.class));// 停止下载服务
						NetTool.cancelNotification(this, 4);// 取消通知
					}
				} else {
					stopService(new Intent(this, DataUpdateService.class));
					stopService(new Intent(this, DownloadService.class));
					NetTool.cancelNotification(this, 4);
				}
			} else {
				isExit = true;
				DJMarketUtils.showToast(this, R.string.back_message);
				mHandler.sendEmptyMessageDelayed(EVENT_CHANGE_EXIT_STATUS, 2500);// 延时2.5秒发送退出广播，此处很妙，如果在2.5秒内再按一次，则可以退出
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 如果后台有应用正在下载，展示退出应用下载dialog
	 */
	private void showExitAppDialog() {
		if (!isFinishing()) {
			if (mExitDialog == null) {
				mExitDialog = new CustomNoTitleDialog(this);
				mExitDialog.setMessage(R.string.background_download_msg);
				mExitDialog.setNegativeButton(getString(R.string.no), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mExitDialog.dismiss();
						stopService(new Intent(MainActivity.this, DataUpdateService.class));
						stopService(new Intent(MainActivity.this, DownloadService.class));
						NetTool.cancelNotification(MainActivity.this, 4);
						finish();
					}
				}).setNeutralButton(getString(R.string.ok), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mExitDialog.dismiss();
						finish();
					}
				});
			}
			if (mExitDialog != null) {
				mExitDialog.show();
			}
		}
	}

	/**
	 * 退出前清除所有消息
	 */
	private void removeAllMessage() {
		if (mHandler != null) {
			if (mHandler.hasMessages(EVENT_CHANGE_EXIT_STATUS)) {
				mHandler.removeMessages(EVENT_CHANGE_EXIT_STATUS);
			}
		}
	}

	/**
	 * 退出应用时退出登录
	 */
	private void outLogin() {
		LoginParams loginParams = ((AppMarket) getApplicationContext()).getLoginParams();
		loginParams.setSessionId(null);
		loginParams.setUserName(null);
		loginParams.setSinaUserName(null);
		loginParams.setTencentUserName(null);
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

	/**
	 * 水平滑动
	 */
	@Override
	public void onPageChanged(int position) {
		BaseActivity mCurrentActivity = (BaseActivity) getLocalActivityManager().getActivity(activityIds[position]);
		Intent intent = null;
		Bundle bundle = null;
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
			} else {
				mAppBottomButton.setChecked(true);
			}

		}
		switch (position) {
		case CHOICENESS_POSITION:
			intent = new Intent(this, ChoicenessActivity.class);
			bundle = new Bundle();
			bundle.putInt("type", CHOICENESS_POSITION);
			break;
		case UPDATE_POSITION:
			intent = new Intent(this, UpdateActivity.class);
			bundle = new Bundle();
			intent.putExtra("type", UPDATE_POSITION);
			break;
		case THEME_POSITION:
			intent = new Intent(this, ThemeActivity.class);
			bundle = new Bundle();
			bundle.putInt("type", THEME_POSITION);
			break;
		case INSTALL_POSITION:
			intent = new Intent(this, UpdateActivity.class);
			bundle = new Bundle();
			intent.putExtra("type", INSTALL_POSITION);
			break;
		case CHANNEL_POSITION:
			intent = new Intent(this, ChannelActivity.class);
			bundle = new Bundle();
			bundle.putInt("type", CHANNEL_POSITION);
			break;
		}
		if (mCurrentActivity != null) {
			getLocalActivityManager().startActivity(activityIds[position], intent);
		} else {
			mMainLayout.removeViewAt(position);
			mMainLayout.addView(getLocalActivityManager().startActivity(activityIds[position], intent).getDecorView(), position);
		}

	}

	/**
	 * 内部控件水平滑动
	 * 
	 * @param v
	 */
	public void setInterceptRange(View v) {
		mMainLayout.setInterceptTouchView(v, CHOICENESS_POSITION);
	}

	@Override
	public void onClick() {
		BaseActivity mCurrentActivity = (BaseActivity) getLocalActivityManager().getCurrentActivity();
		if (mCurrentActivity != null) {
			mCurrentActivity.OnToolBarClick();
		}
	}

}