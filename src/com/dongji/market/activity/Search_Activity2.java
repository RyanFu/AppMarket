package com.dongji.market.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.myjson.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

import com.dongji.market.R;
import com.dongji.market.adapter.GuessLikeAdapter;
import com.dongji.market.adapter.OnDownloadChangeStatusListener;
import com.dongji.market.cache.FileService;
import com.dongji.market.database.MarketDatabase;
import com.dongji.market.database.MarketDatabase.HotwordsService;
import com.dongji.market.database.MarketDatabase.SearchHistory;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.ShareParams;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.listener.ShakeListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.receiver.CommonReceiver;
import com.dongji.market.widget.CustomIconAnimation;
import com.umeng.analytics.MobclickAgent;

public class Search_Activity2 extends BaseActivity implements OnDownloadChangeStatusListener {
	private static final int EVENT_REQUEST_HOTWORDS_LIST = 2;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private static final int EVENT_REQUEST_GUESS_LIST = 5;
	private static final int SENSOR_SHAKE = 6;
	private static final int EVENT_REQUEST_SHAKE_LIST = 7;
	private static final int CHANGE_SHAKE_IMAGE = 8;
	private static final int HIDE_ANIM_STATUS = 9;
	private static final int START_OPEN_ANIM = 10;
	private static final int UPDATE_SHAKE_STATUS = 11;
	private static final int EVENT_REQUEST_DATA = 12;
	private static final int UPDATE_SHAKE_SOUND = 13;

	private long lastUpdateTime = 0; // 上次更新热词时间
	private long currUpdateTime = 0; // 当前更新热词时间

	private long lastShakeTime, currentShakeTime, soundStartTime;

	private int position;
	private String keyword = null;

	private List<String> data;
	private List<ApkItem> guessList, shakeData;
	private View mLoadingProgressBar, mLoadingView, mShakeLoadingLayout, mGuessLoadingLayout;
	private TextView mLoadingTextView, mShakeLoadingTextView, mGuessLoadingTextView;
	// private GridView mHotwordsGrid;
	private LinearLayout mHotwordsLayout, mGuessLikeLayout, mShakeOpenLayout;
	private GridView mGuessLikeGrid;

	private TitleUtil titleUtil;
	private HotwordsService hotwordsService;
	private SearchHistory searchHistory;
	private Handler mHandler;

	private SensorManager sm;
	// private Vibrator vibrator;
	private SoundPool sp;
	private HashMap<Integer, Integer> spMap;
	private ImageView mShake_image, mShake_first_image, mShake_second_image;
	// private ListView mShake_list;
	private LinearLayout mShake_layout;
	private Bitmap mDefaultBitmap;
	private View mShake_item;
	// private ListSingleTemplateAdapter mShake_adapter;
	private CustomIconAnimation iconAnim;
	private ImageView mSoftwareBtn, mTempIcon;
	private ProgressBar mShakeLoadingProgressBar, mGuessLoadingProgressBar;
	private boolean defImage = true;

	public static List<ApkItem> apkItems;

	private View mMaskView;

	private Bitmap defaultBitmap_icon;
	private ShakeListener mShaker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search2);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		View mTopView = findViewById(R.id.search_top);
		titleUtil = new TitleUtil(this, mTopView, "", null, null);

		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		// vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mShakeLoadingLayout = findViewById(R.id.shake_loading_layout);
		mShakeOpenLayout = (LinearLayout) findViewById(R.id.shake_open_layout);
		mShakeLoadingProgressBar = (ProgressBar) mShakeLoadingLayout.findViewById(R.id.loading_progressbar);
		mShakeLoadingTextView = (TextView) mShakeLoadingLayout.findViewById(R.id.loading_textview);
		mShake_image = (ImageView) findViewById(R.id.shake_image);
		mShake_first_image = (ImageView) findViewById(R.id.shake_first_half);
		mShake_second_image = (ImageView) findViewById(R.id.shake_second_half);
		mShake_layout = (LinearLayout) findViewById(R.id.shake_layout);

		try {
			InputStream is = getResources().openRawResource(R.drawable.app_default_icon);
			defaultBitmap_icon = BitmapFactory.decodeStream(is);
			// mDefaultBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.app_default_icon);

		} catch (OutOfMemoryError e) {
			if (defaultBitmap_icon != null && !defaultBitmap_icon.isRecycled()) {
				defaultBitmap_icon.recycle();
			}
		}

		// mGuessLoadingLayout = findViewById(R.id.guess_loading_layout);
		// mGuessLoadingProgressBar = (ProgressBar)
		// mGuessLoadingLayout.findViewById(R.id.loading_progressbar);
		// mGuessLoadingTextView = (TextView)
		// mGuessLoadingLayout.findViewById(R.id.loading_textview);

		// mShake_list = (ListView) findViewById(R.id.shake_listview);
		// mShake_list.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position,
		// long id) {
		// Intent intent = new Intent();
		// Bundle bundle = new Bundle();
		// bundle.putParcelable("apkItem", (ApkItem)
		// mShake_adapter.getItem(position));
		// intent.putExtras(bundle);
		// intent.setClass(Search_Activity2.this,
		// ApkDetailActivity.class);
		// startActivity(intent);
		// }
		// });

		initHandler();
		initLoadingView();
		initSoundPool();

		checkFirstInSearch();

		mShaker = new ShakeListener(this);
		mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
					mMaskView.setVisibility(View.GONE);
				}
				mHandler.sendEmptyMessage(EVENT_REQUEST_SHAKE_LIST);
				mShakeLoadingLayout.setVisibility(View.VISIBLE);

				mShake_image.setVisibility(View.VISIBLE);
				// mShake_list.setVisibility(View.GONE);
				mHandler.sendEmptyMessageDelayed(CHANGE_SHAKE_IMAGE, 500);
				mHandler.removeMessages(START_OPEN_ANIM);
				mHandler.sendEmptyMessageDelayed(START_OPEN_ANIM, 700);
				mHandler.sendEmptyMessage(UPDATE_SHAKE_SOUND);
				// playSounds(1, 0);
				mHandler.sendEmptyMessage(SENSOR_SHAKE);
			}
		});

		iconAnim = new CustomIconAnimation(this);
		mTempIcon = (ImageView) findViewById(R.id.tempIcon);
		mSoftwareBtn = (ImageView) findViewById(R.id.softmanagerbutton);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("test");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (titleUtil != null) {
			titleUtil.dismissSearchPop();
		}

		/*
		 * if (sm != null) { sm.registerListener(sensorEventListener,
		 * sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		 * SensorManager.SENSOR_DELAY_NORMAL); }
		 */

		if (mShaker != null) {
			mShaker.resume();
		}
		MobclickAgent.onResume(this);
		if (titleUtil != null) {
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		/*
		 * if (sm != null) { sm.unregisterListener(sensorEventListener); }
		 */
		if (mShaker != null) {
			mShaker.pause();
		}
		MobclickAgent.onPause(this);
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// if (sm != null) {
		// sm.unregisterListener(sensorEventListener);
		// }
	}

	@Override
	protected void onRestart() {
		System.out.println("search onRestart =========== ");
		if (searchHistory == null) {
			searchHistory = new MarketDatabase.SearchHistory(this);
		}
		// if (TextUtils.isEmpty(titleUtil.mSearchEdit.getText())) {
		// titleUtil.historyAdapter.updateData(searchHistory.getAll());
		// }
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		titleUtil.mSearchEdit.dismissDropDown();
		// unregisterReceiver(commonReceiver);
		titleUtil.unregisterMyReceiver(this);
		super.onDestroy();
	}

	private SensorEventListener sensorEventListener = new SensorEventListener() {

		// long lastShakeTime, currentShakeTime;
		private float[] lastValue = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event) {
			int sensorType = event.sensor.getType();

			float[] values = event.values; // 传感器信息改变时执行该方法
			float x = values[0]; // x轴方向的重力加速度，向右为正
			float y = values[1]; // y轴方向的重力加速度，向前为正
			float z = values[2]; // z轴方向的重力加速度，向上为正

			if (sensorType == Sensor.TYPE_ACCELEROMETER) {
				int midumValue = 8; // 一般在这三个方向的重力加速度达到14就达到了摇晃手机的状态
				x = Math.abs(x);
				y = Math.abs(y);
				z = Math.abs(z);

				System.out.println("============" + x + ", " + y + ", " + z + ", " + lastValue[0] + ", " + lastValue[1] + ", " + lastValue[2]);

				if (x > midumValue || y > midumValue) { // || z > midumValue

					if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
						mMaskView.setVisibility(View.GONE);
					}

					currentShakeTime = System.currentTimeMillis();
					// System.out.println("current======>" + currentShakeTime +
					// ",interval =========>" + (currentShakeTime -
					// lastShakeTime));
					if (currentShakeTime - lastShakeTime > 1000) {
						mHandler.sendEmptyMessage(EVENT_REQUEST_SHAKE_LIST);
						mShakeLoadingLayout.setVisibility(View.VISIBLE);
						System.out.println("update shake data=========>");
						// mShake_image.setVisibility(View.GONE);
						// initShakeAnim();
						// return;
					}
					lastShakeTime = currentShakeTime;

					// vibrator.vibrate(200);
					mShake_image.setVisibility(View.VISIBLE);
					// mShake_list.setVisibility(View.GONE);
					mHandler.sendEmptyMessageDelayed(CHANGE_SHAKE_IMAGE, 500);
					mHandler.removeMessages(START_OPEN_ANIM);
					mHandler.sendEmptyMessageDelayed(START_OPEN_ANIM, 700);
					mHandler.sendEmptyMessage(UPDATE_SHAKE_SOUND);
					// playSounds(1, 0);
					mHandler.sendEmptyMessage(SENSOR_SHAKE);
				}
				lastValue[0] = x;
				lastValue[1] = y;
				lastValue[2] = z;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
				mMaskView.setVisibility(View.GONE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	};

	private void checkFirstInSearch() {
		SharedPreferences mSharedPreferences = getSharedPreferences(this.getPackageName() + "_temp", Context.MODE_PRIVATE);
		boolean isFirst = mSharedPreferences.getBoolean(ShareParams.FIRST_LAUNCHER_SEARCH, true);
		if (isFirst) {
			mMaskView = findViewById(R.id.searchmasklayout);
			mMaskView.setVisibility(View.VISIBLE);
			mMaskView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mMaskView.setVisibility(View.GONE);
					return false;
				}
			});
			SharedPreferences.Editor mEditor = mSharedPreferences.edit();
			mEditor.putBoolean(ShareParams.FIRST_LAUNCHER_SEARCH, false);
			mEditor.commit();
		}
	}

	/**
	 * 初始化摇一摇声音池
	 */
	private void initSoundPool() {
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		spMap = new HashMap<Integer, Integer>();
		spMap.put(1, sp.load(this, R.raw.shake_sound_start, 1));
		spMap.put(2, sp.load(this, R.raw.shake_sound_end, 1));
	}

	/**
	 * 播放声音
	 * 
	 * @param sound
	 * @param number
	 */
	private void playSounds(int sound, int number) {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		float volumeRatio = audioCurrentVolume / audioMaxVolume;

		sp.play(spMap.get(sound), volumeRatio, volumeRatio, 1, number, 1);
	}

	/**
	 * 获取摇一摇数据中的前两个
	 * 
	 * @return
	 */
	private List<ApkItem> getShakeData(List<ApkItem> data) {
		List<ApkItem> list = new ArrayList<ApkItem>();
		if (data != null) {
			if (data.size() > 2) {
				list.add(data.get(0));
				list.add(data.get(1));
			} else {
				list = data;
			}
			list = setApkStatus(list);
		}
		return list;
	}

	/**
	 * 显示摇一摇数据
	 */
	private void showShakeData() {
		List<ApkItem> list = getShakeData(shakeData);
		if (list != null && list.size() > 0) {
			mShakeLoadingLayout.setVisibility(View.GONE);
			// mShake_list.setVisibility(View.VISIBLE);
			// if (mShake_adapter == null) {
			// mShake_adapter = new ListSingleTemplateAdapter(this, list, true);
			// mShake_list.setAdapter(mShake_adapter);
			// }
			// mShake_adapter.resetList();
			// mShake_adapter.addList(getShakeData(shakeData));
			mShake_layout.setVisibility(View.VISIBLE);
			mShake_layout.removeAllViews();
			for (int i = 0; i < list.size(); i++) {
				addShakeItem(list.get(i));
			}
		} else {
			// mShake_list.setVisibility(View.GONE);
			mShake_layout.setVisibility(View.GONE);
			mShakeLoadingLayout.setVisibility(View.VISIBLE);
			mShakeLoadingProgressBar.setVisibility(View.GONE);
			mShakeLoadingTextView.setText(R.string.no_data);
		}
	}

	// ==================================摇一摇=================================<<<<<<<<<

	private void addShakeItem(ApkItem apkItem) {
		final ApkItem item = apkItem;
		// System.out.println("item info ====>" + item.appName);
		mShake_item = LayoutInflater.from(this).inflate(R.layout.item_single_list, null);
		final ImageView mAppIconImageView = (ImageView) mShake_item.findViewById(R.id.iconImageview);
		ImageView mAppLanguageImageView = (ImageView) mShake_item.findViewById(R.id.languageimageview);
		ImageView authorityimageview = (ImageView) mShake_item.findViewById(R.id.authorityimageview);
		ImageView mAppLanguageMultiImageView = (ImageView) mShake_item.findViewById(R.id.languagemultiimageview);
		TextView mAppNameTextView = (TextView) mShake_item.findViewById(R.id.appnametextview);
		TextView mAppVersionTextView = (TextView) mShake_item.findViewById(R.id.appversiontextview);
		TextView mAppOwnerTextView = (TextView) mShake_item.findViewById(R.id.appownertextview);
		TextView mAppSizeTextView = (TextView) mShake_item.findViewById(R.id.appsizetextview);
		TextView mAppInstallNumTextView = (TextView) mShake_item.findViewById(R.id.appinstallnumtextview);
		final Button mInstallTextView = (Button) mShake_item.findViewById(R.id.installtextview);

		if (apkItem.heavy > 0) {
			authorityimageview.setVisibility(View.VISIBLE);
		} else {
			authorityimageview.setVisibility(View.GONE);
		}

		try {
			mDefaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}

		mShake_item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelable("apkItem", item);
				intent.putExtras(bundle);
				intent.setClass(Search_Activity2.this, ApkDetailActivity.class);
				startActivity(intent);
			}
		});

		mAppNameTextView.setText(item.appName);
		mAppVersionTextView.setText(" V" + item.version);
		mAppOwnerTextView.setText(item.company);
		mAppSizeTextView.setText(getString(R.string.app_size) + NetTool.sizeFormat((int) item.fileSize));
		mAppInstallNumTextView.setText(getString(R.string.detail_installCount2) + DJMarketUtils.convertionInstallNumber(this, item.downloadNum));
		setLanguageType(item.language, mAppLanguageImageView, mAppLanguageMultiImageView);
		displayApkStatus(mInstallTextView, item.status);
		mInstallTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// checkDownload(item, holder.mInstallTextView);
				if (item.status == AConstDefine.STATUS_APK_UNINSTALL || item.status == AConstDefine.STATUS_APK_UNUPDATE) {
					int[] location = new int[2];
					mAppIconImageView.getLocationOnScreen(location);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("X", location[0]);
					map.put("Y", location[1]);
					map.put("icon", mAppIconImageView.getDrawable());

					// DJMarketUtils.checkDownload(Search_Activity2.this, item,
					// mInstallTextView, Search_Activity2.this, map);
					DownloadUtils.checkDownload(Search_Activity2.this, item, mInstallTextView, Search_Activity2.this, map);
				} else {
					// DJMarketUtils.cancelListDownload(Search_Activity2.this,
					// item);

					Intent intent = new Intent(DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
					DownloadEntity entity = new DownloadEntity(item);
					Bundle bundle = new Bundle();
					bundle.putParcelable(DownloadConstDefine.DOWNLOAD_ENTITY, entity);
					intent.putExtras(bundle);
					sendBroadcast(intent);
					if (entity.downloadType == DownloadConstDefine.TYPE_OF_DOWNLOAD) {
						DownloadUtils.fillDownloadNotifycation(Search_Activity2.this, false);
					} else if (entity.downloadType == DownloadConstDefine.TYPE_OF_UPDATE) {
						DownloadUtils.fillUpdateAndUpdatingNotifycation(Search_Activity2.this, false);
					}
				}
			}
		});
		try {
			FileService.getBitmap(item.appIconUrl, mAppIconImageView, mDefaultBitmap, true);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		mShake_layout.addView(mShake_item);

		View mDividerView = new View(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, AndroidUtils.px2dip(this, 1));
		mDividerView.setLayoutParams(params);
		mDividerView.setBackgroundColor(0xffd3d3d3);
		mShake_layout.addView(mDividerView);
	}

	private static final int ENGLISH_LANGUAGE = 0, CHINESE_LANGUAGE = 1, MULTINATIONAL_LANGUAGE = 2;

	protected void setLanguageType(int language, ImageView mImageView, ImageView mMultiImageView) {
		if (language == ENGLISH_LANGUAGE) {
			mImageView.setBackgroundResource(R.drawable.language_english);
			mMultiImageView.setVisibility(View.GONE);
			mImageView.setVisibility(View.VISIBLE);
		} else if (language == CHINESE_LANGUAGE) {
			mImageView.setBackgroundResource(R.drawable.language_chinese);
			mMultiImageView.setVisibility(View.GONE);
			mImageView.setVisibility(View.VISIBLE);
		} else {
			mMultiImageView.setBackgroundResource(R.drawable.language_multinational);
			mMultiImageView.setVisibility(View.VISIBLE);
			mImageView.setVisibility(View.GONE);
		}
	}

	protected void displayApkStatus(TextView mTextView, int status) {
		switch (status) {
		case AConstDefine.STATUS_APK_UNINSTALL:
			setvisibleInstallTextView(mTextView, true, R.string.install, R.drawable.button_has_border_selector, Color.BLACK);
			break;
		case AConstDefine.STATUS_APK_INSTALL:
			// setvisibleInstallTextView(mTextView, false,
			// R.string.installing_msg);
			// break;
		case AConstDefine.STATUS_APK_UPDATE:
			// setvisibleInstallTextView(mTextView, false,
			// R.string.updating_msg);
			setvisibleInstallTextView(mTextView, true, R.string.cancel, R.drawable.cancel_selector, Color.parseColor("#7f5100"));
			break;
		case AConstDefine.STATUS_APK_INSTALL_DONE:
			setvisibleInstallTextView(mTextView, false, R.string.has_installed, R.drawable.button_has_border_selector, Color.parseColor("#999999"));
			break;
		case AConstDefine.STATUS_APK_UNUPDATE:
			setvisibleInstallTextView(mTextView, true, R.string.update, R.drawable.update_selector, Color.parseColor("#0e567d"));
			break;
		}
	}

	private void setvisibleInstallTextView(TextView mTextView, boolean enable, int rId, int resid, int textColor) {
		mTextView.setEnabled(enable);
		mTextView.setText(rId);
		mTextView.setBackgroundResource(resid);
		mTextView.setTextColor(textColor);
	}

	@Override
	public void onDownload(ApkItem item, TextView mTextView, Map<String, Object> map) {
		if (item.status == AConstDefine.STATUS_APK_UNINSTALL) {
			item.status = AConstDefine.STATUS_APK_INSTALL;
		} else if (item.status == AConstDefine.STATUS_APK_UNUPDATE) {
			item.status = AConstDefine.STATUS_APK_UPDATE;
		}
		displayApkStatus(mTextView, item.status);
		onStartDownload(map);
	}

	/**
	 * 根据AppId找到对应的应用修改其状态
	 * 
	 * @param appId
	 * @param status
	 * @return
	 */
	public final boolean changeApkStatusByAppId(boolean isCancel, String packageName, int versionCode, List<ApkItem> list) {
		// List<ApkItem> list=getItemList();
		if (list != null && list.size() > 0) {
			int i = 0;
			for (; i < list.size(); i++) {
				ApkItem item = list.get(i);
				if (packageName.equals(item.packageName) && versionCode == item.versionCode) {
					if (isCancel && item.status == AConstDefine.STATUS_APK_INSTALL) {
						item.status = AConstDefine.STATUS_APK_UNINSTALL;
					} else if (isCancel && item.status == AConstDefine.STATUS_APK_UPDATE) {
						item.status = AConstDefine.STATUS_APK_UNUPDATE;
					} else if (!isCancel && item.status == AConstDefine.STATUS_APK_UNINSTALL) {
						item.status = AConstDefine.STATUS_APK_INSTALL;
					} else if (!isCancel && item.status == AConstDefine.STATUS_APK_UNUPDATE) {
						item.status = AConstDefine.STATUS_APK_UPDATE;
					}
				}
			}
			// notifyDataSetChanged();
			mHandler.sendEmptyMessage(UPDATE_SHAKE_STATUS);
			return true;
		}
		return false;
	}

	public boolean changeApkStatusByPackageInfo(int status, PackageInfo info, List<ApkItem> list) {
		// List<ApkItem> list = getItemList();
		if (list != null && list.size() > 0) {
			int i = 0;
			for (; i < list.size(); i++) {
				ApkItem item = list.get(i);
				if (status == 1) {
					if (item.packageName.equals(info.packageName) && item.versionCode == info.versionCode) {
						list.get(i).status = AConstDefine.STATUS_APK_INSTALL_DONE;
					}
				} else if (status == 2) {
					if (item.packageName.equals(info.packageName)) {
						list.get(i).status = AConstDefine.STATUS_APK_UNINSTALL;
					}
				}
			}
			// notifyDataSetChanged();
			mHandler.sendEmptyMessage(UPDATE_SHAKE_STATUS);
			return true;
		}
		return false;
	}

	// ========================================================================================================>>>>>>>>>>

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("handler");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_HOTWORDS_LIST);
		mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
	}

	/**
	 * 显示初始化进度条，同时发送消息请求数据
	 */
	private void initLoadingView() {

		mGuessLoadingLayout = findViewById(R.id.guess_loading_layout);
		mGuessLoadingProgressBar = (ProgressBar) mGuessLoadingLayout.findViewById(R.id.loading_progressbar);
		mGuessLoadingTextView = (TextView) mGuessLoadingLayout.findViewById(R.id.loading_textview);
		mGuessLoadingLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mGuessLoadingProgressBar.getVisibility() == View.GONE) {
					initGuessLoading();
					mHandler.sendEmptyMessage(EVENT_REQUEST_GUESS_LIST);
				}
				return false;
			}
		});

		mLoadingView = findViewById(R.id.hotwordLoadingLayout);
		mLoadingProgressBar = (ProgressBar) mLoadingView.findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) mLoadingView.findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					mHandler.sendEmptyMessage(EVENT_REQUEST_HOTWORDS_LIST);
				}
				return false;
			}
		});
	}

	/**
	 * 设置进度条为可见
	 */
	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	private void initGuessLoading() {
		mGuessLoadingLayout.setVisibility(View.VISIBLE);
		mGuessLoadingProgressBar.setVisibility(View.VISIBLE);
		mGuessLoadingTextView.setText(R.string.loading_txt);
	}

	private void initHotWordsData() {
		/*
		 * mHotwordsGrid = (GridView) findViewById(R.id.hotwords_gridview);
		 * mHotwordsGrid.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { String keyword = data.get(position);
		 * titleUtil.history.add(keyword); Intent intent = new Intent();
		 * intent.putExtra("search_keyword", keyword);
		 * intent.setClass(Search_Activity2.this, Search_Result_Activity.class);
		 * startActivity(intent); } }); ArrayAdapter<String> arrayAdapter = new
		 * ArrayAdapter<String>( Search_Activity2.this,
		 * R.layout.item_gridview_hotwords, R.id.hotword_view, data);
		 * mHotwordsGrid.setAdapter(arrayAdapter);
		 */

		mHotwordsLayout = (LinearLayout) findViewById(R.id.hotwords_layout);
		if (data == null || data.size() == 0) {
			TextView tvTips = new TextView(this);
			tvTips.setGravity(Gravity.CENTER);
			tvTips.setTextColor(getResources().getColor(android.R.color.black));
			int padding = AndroidUtils.dip2px(this, 5.0f);
			tvTips.setPadding(0, padding, 0, padding);
			tvTips.setTextSize(15);
			tvTips.setText(R.string.not_hotkeyword);
			mHotwordsLayout.addView(tvTips);
			return;
		}
		position = 0;
		LayoutInflater inflater = LayoutInflater.from(this);
		DisplayMetrics dm = AndroidUtils.getScreenSize(this);
		int maxWidth = dm.widthPixels - AndroidUtils.dip2px(this, 10f);
		float spaceWidth = AndroidUtils.dip2px(this, 68);
		while (position < data.size()) {
			LinearLayout mLinearLayout = getLinearLayout();
			LinearLayout mChildLayout = (LinearLayout) inflater.inflate(R.layout.item_view_hotwords, null);
			Button mButton = (Button) mChildLayout.findViewById(R.id.hotword_view);
			keyword = data.get(position);
			mButton.setText(keyword);
			final String tempString = keyword;
			mButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// String keyword = data.get(position);
					titleUtil.history.add(tempString);
					Intent intent = new Intent();
					intent.putExtra("search_keyword", tempString);
					intent.setClass(Search_Activity2.this, Search_Result_Activity.class);
					startActivity(intent);
				}
			});
			float childWidth = mButton.getPaint().measureText(keyword);
			// System.out.println("==="+data.get(position));
			position++;
			mLinearLayout.addView(mChildLayout);
			float tempWidth = childWidth;
			tempWidth += spaceWidth;
			while (tempWidth < maxWidth && position < data.size()) {
				LinearLayout mChildLayout2 = (LinearLayout) inflater.inflate(R.layout.item_view_hotwords, null);
				Button mButton2 = (Button) mChildLayout2.findViewById(R.id.hotword_view);
				keyword = data.get(position);
				childWidth = mButton2.getPaint().measureText(keyword);
				tempWidth += childWidth;
				if (tempWidth <= maxWidth) {
					mButton2.setText(keyword);
					final String tempString2 = keyword;
					mButton2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// String keyword = data.get(position);
							titleUtil.history.add(tempString2);
							Intent intent = new Intent();
							intent.putExtra("search_keyword", tempString2);
							intent.setClass(Search_Activity2.this, Search_Result_Activity.class);
							startActivity(intent);
						}
					});
					// System.out.println(data.get(position));
					position++;
					mLinearLayout.addView(mChildLayout2);
					tempWidth += spaceWidth;
				} else {
					tempWidth = 0;
					break;
				}
			}
			mHotwordsLayout.addView(mLinearLayout);
		}

		/*
		 * LineWrapViewGroup viewGroup = new LineWrapViewGroup(this);
		 * 
		 * for (String str : data) { View view =
		 * getLayoutInflater().inflate(R.layout.item_view_hotwords, null);
		 * Button itemBtn = (Button) view.findViewById(R.id.hotword_view);
		 * itemBtn.setText(str); viewGroup.addView(view); }
		 * mHotwordsLayout.addView(viewGroup);
		 */
	}

	private LinearLayout getLinearLayout() {
		LinearLayout mLinearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLinearLayout.setLayoutParams(mParams);
		return mLinearLayout;
	}

	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					AndroidUtils.showToast(Search_Activity2.this, rId2);
				}
				if (mShakeLoadingLayout.getVisibility() == View.VISIBLE) {
					mShakeLoadingProgressBar.setVisibility(View.GONE);
					mShakeLoadingTextView.setText(R.string.request_data_error_msg2);
				} else {
					AndroidUtils.showToast(Search_Activity2.this, rId2);
				}
				if (mGuessLoadingLayout.getVisibility() == View.VISIBLE) {
					mGuessLoadingProgressBar.setVisibility(View.GONE);
					mGuessLoadingTextView.setText(R.string.request_data_error_msg2);
				} else {
					AndroidUtils.showToast(Search_Activity2.this, rId2);
				}
			}
		});
	}

	/**
	 * 热词更新，每六小时更新一次
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private void updateHotwords() throws IOException, JSONException {
		SharedPreferences pref = getSharedPreferences(ShareParams.SHARE_FILE_NAME, Context.MODE_PRIVATE);
		lastUpdateTime = pref.getLong("updateHotwordsTime", 0);
		currUpdateTime = System.currentTimeMillis() / (3600 * 1000);// 取当前日期到1970年1月1日的小时数
		hotwordsService = new MarketDatabase.HotwordsService(this);
		if (currUpdateTime >= lastUpdateTime + 6) {
			List<String> hotwords = DataManager.newInstance().getKeywords();
			hotwordsService.update(hotwords);
			Editor editor = pref.edit();
			editor.putLong("updateHotwordsTime", currUpdateTime);
			editor.commit();
		}
	}

	/**
	 * 获取“猜你喜欢”列表数据
	 * 
	 * @return
	 */
	private List<ApkItem> getGuessLikeList(List<ApkItem> data) {
		List<ApkItem> list = new ArrayList<ApkItem>();
		if (data != null) {
			if (data.size() <= 8) {
				list = data;
			} else {
				for (int i = 0; i < 8; i++) {
					list.add(data.get(i));
				}
			}
		}
		return list;
	}

	/**
	 * 初始化“猜你喜欢”列表
	 */
	private void showGuessLike() {
		// guessList = getGuessLikeList();
		final List<ApkItem> list = getGuessLikeList(guessList);
		mGuessLikeGrid = new GridView(this);
		mGuessLikeLayout = (LinearLayout) findViewById(R.id.show_gridview);
		// mGuessLoadingLayout = findViewById(R.id.guess_loading_layout);
		// mGuessLoadingProgressBar = (ProgressBar)
		// mGuessLoadingLayout.findViewById(R.id.loading_progressbar);
		// mGuessLoadingTextView = (TextView)
		// mGuessLoadingLayout.findViewById(R.id.loading_textview);
		if (list.size() == 0) {
			mGuessLikeGrid.setVisibility(View.GONE);
			mGuessLoadingLayout.setVisibility(View.VISIBLE);
			mGuessLoadingProgressBar.setVisibility(View.GONE);
			mGuessLoadingTextView.setText(R.string.no_data);
			return;
		} else {
			mGuessLoadingLayout.setVisibility(View.GONE);
			mGuessLikeGrid.setVisibility(View.VISIBLE);
		}
		int columnWidth = AndroidUtils.dip2px(this, 48);
		int horizontalSpacing = AndroidUtils.dip2px(this, 10);
		// int padding = AndroidUtils.dip2px(this, 5);
		// mGuessLikeGrid = (GridView) findViewById(R.id.guess_gridview);
		// LayoutParams params = new LayoutParams(list.size()
		// * (columnWidth + horizontalSpacing + padding),
		// LayoutParams.FILL_PARENT);
		LayoutParams params = new LayoutParams(list.size() * columnWidth + list.size() * horizontalSpacing, columnWidth + horizontalSpacing * 4);
		mGuessLikeGrid.setLayoutParams(params);
		mGuessLikeGrid.setColumnWidth(columnWidth);
		mGuessLikeGrid.setHorizontalSpacing(horizontalSpacing);
		mGuessLikeGrid.setNumColumns(list.size());
		// mGuessLikeGrid.setPadding(padding, padding, padding, padding);
		mGuessLikeGrid.setStretchMode(GridView.NO_STRETCH);
		mGuessLikeGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String keyword = list.get(position).appName;
				titleUtil.history.add(keyword);
				Intent intent = new Intent();
				intent.putExtra("search_keyword", keyword);
				intent.setClass(Search_Activity2.this, Search_Result_Activity.class);
				startActivity(intent);
			}
		});
		GuessLikeAdapter adapter = new GuessLikeAdapter(this, list, defaultBitmap_icon);
		mGuessLikeGrid.setAdapter(adapter);
		mGuessLikeLayout.addView(mGuessLikeGrid);

	}

	private void initShakeAnim() {
		// if (mShakeOpenLayout == null) {
		// mShakeOpenLayout = (LinearLayout)
		// findViewById(R.id.shake_open_layout);
		// }
		playSounds(2, 0);
		mShake_image.setVisibility(View.GONE);
		mShakeOpenLayout.setVisibility(View.VISIBLE);
		Animation first_half = AnimationUtils.loadAnimation(this, R.anim.open_first_half);
		Animation second_half = AnimationUtils.loadAnimation(this, R.anim.open_second_half);
		mShake_first_image.setAnimation(first_half);
		mShake_second_image.setAnimation(second_half);
		first_half.start();
		second_half.start();
		mHandler.removeMessages(HIDE_ANIM_STATUS);
		mHandler.sendEmptyMessageDelayed(HIDE_ANIM_STATUS, 1500);
	}

	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_DATA:
				try {
					String top50time = NetTool.getSharedPreferences(Search_Activity2.this, AConstDefine.SHARE_GETTOP50TIME, "");
					Calendar cal = Calendar.getInstance();
					String dateString = "" + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DATE);
					if (null == apkItems || !top50time.equals(dateString)) {
						apkItems = DataManager.newInstance().getTop50();
						NetTool.setSharedPreferences(Search_Activity2.this, AConstDefine.SHARE_GETTOP50TIME, dateString);
					}
					mHandler.sendEmptyMessage(EVENT_REQUEST_GUESS_LIST);
				} catch (IOException e) {
					e.printStackTrace();
					if (!AndroidUtils.isNetworkAvailable(getApplicationContext())) {
						mHandler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				}
				break;
			case EVENT_REQUEST_HOTWORDS_LIST:
				try {
					updateHotwords();
					data = hotwordsService.getAll();
				} catch (IOException e) {
					e.printStackTrace();
					if (!AndroidUtils.isNetworkAvailable(getApplicationContext())) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
					break;
				} catch (JSONException e) {
					e.printStackTrace();
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					break;
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						initHotWordsData();
						mLoadingView.setVisibility(View.GONE);
						mHotwordsLayout.setVisibility(View.VISIBLE);
					}
				});
				break;
			case EVENT_REQUEST_GUESS_LIST:
				// try {
				// guessList =
				// DataManager.newInstance().getSearchResult("围棋");

				if (null != apkItems) {
					guessList = new ArrayList<ApkItem>();
					Random random = new Random();
					int[] ranInt = new int[8];
					ranInt[0] = random.nextInt(apkItems.size());
					guessList.add(apkItems.get(ranInt[0]));
					System.out.println("guesslist........" + ranInt[0]);
					for (int i = 1; i < 8; i++) {
						int tempRandom = random.nextInt(apkItems.size());
						for (int j = 0; j < i; j++) {
							while (tempRandom == ranInt[j]) {
								tempRandom = random.nextInt(apkItems.size());
							}
						}
						ranInt[i] = tempRandom;
						guessList.add(apkItems.get(ranInt[i]));
						System.out.println("guesslist........" + ranInt[i]);
					}
				}
				// } catch (IOException e) {
				// e.printStackTrace();
				// if (!AndroidUtils
				// .isNetworkAvailable(getApplicationContext())) {
				// sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
				// } else {
				// sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				// }
				// break;
				// } catch (JSONException e) {
				// e.printStackTrace();
				// sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				// break;
				// }
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mGuessLoadingLayout.setVisibility(View.GONE);
						showGuessLike();
					}
				});
				break;
			case EVENT_REQUEST_SHAKE_LIST:
				// try {
				// shakeData = getShakeData();
				// shakeData =
				// DataManager.newInstance().getSearchResult("百度");
				if (null != apkItems && apkItems.size() > 1) {
					Random random = new Random();
					int result1 = random.nextInt(apkItems.size());
					int result2;
					if (result1 == 0) {
						result2 = 1;
					} else {
						result2 = random.nextInt(result1);
					}
					shakeData = new ArrayList<ApkItem>();
					shakeData.add(apkItems.get(result2));
					shakeData.add(apkItems.get(result1));
				}

				System.out.println("time length ====> " + (System.currentTimeMillis() - currentShakeTime));
				// shakeData = setApkStatus(shakeData);
				// } catch (IOException e) {
				// e.printStackTrace();
				// if (!AndroidUtils
				// .isNetworkAvailable(getApplicationContext())) {
				// sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
				// } else {
				// sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				// }
				// runOnUiThread(new Runnable() {
				// public void run() {
				// mShake_layout.setVisibility(View.GONE);
				// }
				// });
				// break;
				// } catch (JSONException e) {
				// e.printStackTrace();
				// sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				// break;
				// }
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mShakeLoadingLayout.setVisibility(View.GONE);
						showShakeData();
					}
				});
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
				break;
			case SENSOR_SHAKE:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// AndroidUtils.showToast(getApplicationContext(),
						// "摇一摇");
					}
				});
				break;
			case CHANGE_SHAKE_IMAGE:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						if (defImage) {
							mShake_image.setImageResource(R.drawable.wave_running);
							mShake_image.setPadding(0, AndroidUtils.dip2px(Search_Activity2.this, 30), 0, AndroidUtils.dip2px(Search_Activity2.this, 30));
							defImage = false;
						} else {
							mShake_image.setImageResource(R.drawable.wave_default);
							mShake_image.setPadding(0, AndroidUtils.dip2px(Search_Activity2.this, 30), 0, AndroidUtils.dip2px(Search_Activity2.this, 30));
							defImage = true;
						}
					}
				});
				break;
			case START_OPEN_ANIM:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// playSounds(2, 0);
						initShakeAnim();
						System.out.println("open animation!");
					}
				});
				break;
			case HIDE_ANIM_STATUS:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mShakeOpenLayout.setVisibility(View.GONE);
					}
				});
				break;
			case UPDATE_SHAKE_STATUS:
				runOnUiThread(new Runnable() {
					public void run() {
						showShakeData();
					}
				});
				break;
			case UPDATE_SHAKE_SOUND:
				if (currentShakeTime - soundStartTime >= 1000) {
					playSounds(1, 0);
					soundStartTime = currentShakeTime;
				}
				break;
			default:
				break;
			}
		}

	}

	@Override
	public void onAppClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGameClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAppClicked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemClick(ChannelListInfo info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		// ListBaseAdapter mAdapter=(ListBaseAdapter)mShake_list.getAdapter();
		// mAdapter.changeApkStatusByPackageInfo(status, info);
		changeApkStatusByPackageInfo(status, info, getShakeData(shakeData));
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		// ListBaseAdapter mAdapter=(ListBaseAdapter)mShake_list.getAdapter();
		// mAdapter.changeApkStatusByAppId(isCancel, apkSaveName);
		changeApkStatusByAppId(isCancel, packageName, versionCode, getShakeData(shakeData));
	}

	@Override
	protected void onUpdateDataDone() {
		// if(mShake_list!=null && mShake_list.getAdapter()!=null) {
		// ListBaseAdapter mAdapter=(ListBaseAdapter)mShake_list.getAdapter();
		// setApkStatus(mAdapter.getItemList());
		// mAdapter.setDisplayNotify(isRemoteImage);
		// }
		setApkStatus(getShakeData(shakeData));
		mHandler.sendEmptyMessage(UPDATE_SHAKE_STATUS);
	}

	@Override
	public void onStartDownload(Map<String, Object> map) {
		int iconX = (Integer) map.get("X");
		int iconY = (Integer) map.get("Y") - mSoftwareBtn.getHeight() + AndroidUtils.getStatusBarInfo(this).top;
		Drawable icon = (Drawable) map.get("icon");
		iconAnim.startAnimation(iconX, iconY, icon, mTempIcon, mSoftwareBtn);
	}

	@Override
	protected void loadingImage() {

	}
}
