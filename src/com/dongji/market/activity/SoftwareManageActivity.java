package com.dongji.market.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.dongji.market.R;
import com.dongji.market.application.AppMarket;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.widget.HorizontalScrollLayout;
import com.dongji.market.widget.HorizontalScrollLayout.OnPageChangedListener;
import com.dongji.market.widget.LoginDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * 软件管理页面
 * @author yvon
 *
 */
public class SoftwareManageActivity extends ActivityGroup implements OnClickListener, OnPageChangedListener, OnToolBarBlankClickListener {

	private ImageView mSlideImageView;
	private RadioButton mUpdateInstallRB, mInstalledRB, mSoftwareMoveRB;
	private float slideLeft;
	private HorizontalScrollLayout horizontalScrollLayout;
	private boolean isRunning;
	private AppMarket mApp = null;

	private TitleUtil titleUtil;
	private MyBroadcastReceiver myBroadcastReceiver;

	private View mMaskView;
	private LayoutInflater mInflater;
	private String[] activityIds;
	private RadioButton[] mTopButtons;


	private static final int UPDATEINSTALL_POSITION = 0, INSTALLED_POSITION = 1, SOFTWAREMOVE_POSITION = 2;

	private boolean firstLauncherSoftMove;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_software_manage);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		mApp = (AppMarket) getApplication();
		initData();
		initView();
		registerReceiver();
		checkFirstLauncherSoftMove();
	}
	
	private void initData() {
		activityIds = new String[3];
		activityIds[0] = "updateandinstall";
		activityIds[1] = "alreadyinstall";
		activityIds[2] = "softwaremove";
	}

	private void initView() {
		mInflater = LayoutInflater.from(this);
		horizontalScrollLayout = (HorizontalScrollLayout) findViewById(R.id.horizontalScrollLayout);
		horizontalScrollLayout.setOnPageChangedListener(this);
		initHorizontalScrollLayout();
		View mTopView = findViewById(R.id.soft_manage_top);
		Bundle bundle = getIntent().getExtras();
		titleUtil = new TitleUtil(this, mTopView, R.string.software_manage, bundle, this);
		initTopButton();
		initSlideImageView();
		mUpdateInstallRB.performClick();
	}
	
	private void initHorizontalScrollLayout() {
		horizontalScrollLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
		horizontalScrollLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
		horizontalScrollLayout.addView(mInflater.inflate(R.layout.layout_loading, null));
	}
	
	private void initTopButton() {
		mUpdateInstallRB = (RadioButton) findViewById(R.id.update_install);
		mInstalledRB = (RadioButton) findViewById(R.id.installed_software);
		mSoftwareMoveRB = (RadioButton) findViewById(R.id.softwaremove);
		mUpdateInstallRB.setOnClickListener(this);
		mInstalledRB.setOnClickListener(this);
		mSoftwareMoveRB.setOnClickListener(this);
		mTopButtons = new RadioButton[] { mUpdateInstallRB, mInstalledRB, mSoftwareMoveRB };
	}
	
	private void initSlideImageView() {
		mSlideImageView = (ImageView) findViewById(R.id.slide_image);
		DisplayMetrics dm = DJMarketUtils.getScreenSize(this);
		int num = DJMarketUtils.dip2px(this, 2);
		float singleWidth = (dm.widthPixels - num * 2) / 3.0f;
		LayoutParams mParams = (LayoutParams) mSlideImageView.getLayoutParams();
		mParams.width = (int) singleWidth;
		mSlideImageView.setLayoutParams(mParams);
	}
	
	private void registerReceiver() {
		myBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST);
		intentFilter.addAction(AConstDefine.BROADCAST_ACTION_SHOWUNINSTALLLIST);
		registerReceiver(myBroadcastReceiver, intentFilter);
	}
	
	private void checkFirstLauncherSoftMove() {
		SharedPreferences mSharedPreferences = getSharedPreferences(this.getPackageName() + "_temp", Context.MODE_PRIVATE);
		firstLauncherSoftMove = mSharedPreferences.getBoolean(AConstDefine.FIRST_LAUNCHER_SOFT_MOVE, true);
	}

	public boolean is3GDownloadPromptUser() {
		return mApp.isIs3GDownloadPrompt();
	}

	/**
	 * 使用3G下载已提示用户
	 */
	public void set3GDownloadPromptUser() {
		mApp.setIs3GDownloadPrompt(true);
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
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.update_install:
			initAnimation(v);
			horizontalScrollLayout.snapToScreen(UPDATEINSTALL_POSITION);
			break;
		case R.id.installed_software:
			initAnimation(v);
			horizontalScrollLayout.snapToScreen(INSTALLED_POSITION);
			break;
		case R.id.softwaremove:
			initAnimation(v);
			horizontalScrollLayout.snapToScreen(SOFTWAREMOVE_POSITION);
			break;
		}
	}
	
	private void initAnimation(final View v) {
		if (!isRunning && slideLeft != v.getLeft()) {
			TranslateAnimation mAnimation = new TranslateAnimation(slideLeft, v.getLeft(), 0, 0);
			mAnimation.setDuration(300L);
			mAnimation.setFillEnabled(true);
			mAnimation.setFillAfter(true);
			isRunning = true;
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
					isRunning = false;
				}
			});
			mSlideImageView.startAnimation(mAnimation);
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(myBroadcastReceiver);
		titleUtil.unregisterMyReceiver(this);
		super.onDestroy();
	}

	@Override
	public void onPageChanged(int position) {
		Activity mCurrentActivity = (Activity) getLocalActivityManager().getActivity(activityIds[position]);
		Intent intent = null;
		mTopButtons[position].setChecked(true);
		initAnimation(mTopButtons[position]);
		switch (position) {
		case UPDATEINSTALL_POSITION:
			intent = new Intent(this, DownloadActivity.class);
			break;
		case INSTALLED_POSITION:
			intent = new Intent(this, Uninstall_list_Activity.class);
			break;
		case SOFTWAREMOVE_POSITION:
			intent = new Intent(this, SoftwareMove_list_Activity.class);
			if (firstLauncherSoftMove) {
				setMaskForSoftMove();
			}
			break;
		}
		if (mCurrentActivity != null) {
			getLocalActivityManager().startActivity(activityIds[position], intent);
		} else {
			horizontalScrollLayout.removeViewAt(position);
			horizontalScrollLayout.addView(getLocalActivityManager().startActivity(activityIds[position], intent).getDecorView(), position);
		}
	}
	
	private void setMaskForSoftMove() {
		SharedPreferences mSharedPreferences = getSharedPreferences(this.getPackageName() + "_temp", Context.MODE_PRIVATE);
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(AConstDefine.FIRST_LAUNCHER_SOFT_MOVE, false);
		boolean flag = mEditor.commit();
		if (flag)
			firstLauncherSoftMove = false;
		if (SoftwareMove_list_Activity.isCanMove(SoftwareManageActivity.this)) {
			mMaskView = findViewById(R.id.softmovemasklayout);
			mMaskView.setVisibility(View.VISIBLE);
			mMaskView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mMaskView.setVisibility(View.GONE);
					return false;
				}
			});
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		invokeFragmentManagerNoteStateNotSaved();
	}

	private void invokeFragmentManagerNoteStateNotSaved() {
		if (Build.VERSION.SDK_INT < 11) {
			return;
		}
		try {
			Class<?> cls = getClass();
			do {
				cls = cls.getSuperclass();
			} while (!"Activity".equals(cls.getSimpleName()));
			Field fragmentMgrField = cls.getDeclaredField("mFragments");
			fragmentMgrField.setAccessible(true);
			Object fragmentMgr = fragmentMgrField.get(this);
			cls = fragmentMgr.getClass();
			Method noteStateNotSavedMethod = cls.getDeclaredMethod("noteStateNotSaved", new Class[] {});
			noteStateNotSavedMethod.invoke(fragmentMgr, new Object[] {});
			Log.d("DLOutState", "Successful call for noteStateNotSaved!!!");
		} catch (Exception ex) {
			Log.e("DLOutState", "Exception on worka FM.noteStateNotSaved", ex);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
				mMaskView.setVisibility(View.GONE);
				return true;
			}
			if (event.getAction() == KeyEvent.ACTION_UP && getCurrentActivity().getClass().equals(BackupOrRestoreActivity.class)) {
				Intent tempintent = new Intent(SoftwareManageActivity.this, Uninstall_list_Activity.class);
				horizontalScrollLayout.removeViewAt(1);
				horizontalScrollLayout.addView(getLocalActivityManager().startActivity(activityIds[INSTALLED_POSITION], tempintent).getDecorView(), 1);
				return true;
			} else {
				if (event.getAction() == KeyEvent.ACTION_UP && Build.VERSION.SDK_INT > 11) { // google
																								// bug
					getLocalActivityManager().removeAllActivities();
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST)) {
				int flag = intent.getIntExtra(LoginDialog.FLAG_ACTIVITY_BANDR, -1);
				Intent tempintent = new Intent(SoftwareManageActivity.this, BackupOrRestoreActivity.class);
				tempintent.putExtra(LoginDialog.FLAG_ACTIVITY_BANDR, flag);
				horizontalScrollLayout.removeViewAt(1);
				switch (flag) {
				case LoginDialog.ACTIVITY_BACKUP:
					horizontalScrollLayout.addView(getLocalActivityManager().startActivity("bandr_backup", tempintent).getDecorView(), 1);
					break;

				case LoginDialog.ACTIVITY_CLOUD_BACKUP:
					horizontalScrollLayout.addView(getLocalActivityManager().startActivity("bandr_cloud_backup", tempintent).getDecorView(), 1);
					break;
				case LoginDialog.ACTIVITY_RESTORE:
					horizontalScrollLayout.addView(getLocalActivityManager().startActivity("bandr_restore", tempintent).getDecorView(), 1);
					break;
				case LoginDialog.ACTIVITY_CLOUD_RESTORE:
					horizontalScrollLayout.addView(getLocalActivityManager().startActivity("bandr_cloud_restore", tempintent).getDecorView(), 1);
					break;
				}
			} else if (intent.getAction().equals(AConstDefine.BROADCAST_ACTION_SHOWUNINSTALLLIST)) {

				Intent tempintent = new Intent(SoftwareManageActivity.this, Uninstall_list_Activity.class);
				horizontalScrollLayout.removeViewAt(1);
				horizontalScrollLayout.addView(getLocalActivityManager().startActivity(activityIds[INSTALLED_POSITION], tempintent).getDecorView(), 1);

			}
		}
	}

	@Override
	public void onClick() {
		Activity mCurrentActivity = getLocalActivityManager().getCurrentActivity();
		if (mCurrentActivity != null) {
			if (mCurrentActivity instanceof DownloadActivity) {
				((DownloadActivity) mCurrentActivity).onToolBarClick();
			} else if (mCurrentActivity instanceof Uninstall_list_Activity) {
				((Uninstall_list_Activity) mCurrentActivity).onToolBarClick();
			} else if (mCurrentActivity instanceof SoftwareMove_list_Activity) {
				((SoftwareMove_list_Activity) mCurrentActivity).onToolBarClick();
			} else if (mCurrentActivity instanceof BackupOrRestoreActivity) {
				((BackupOrRestoreActivity) mCurrentActivity).onToolBarClick();
			}
		}
	}
}
