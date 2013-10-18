package com.dongji.market.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.application.AppMarket;
import com.dongji.market.database.MarketDatabase;
import com.dongji.market.database.MarketDatabase.SearchHistory;
import com.dongji.market.database.MarketDatabase.Setting_Service;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.helper.TitleUtil.SaveSettingListener;
import com.dongji.market.widget.CustomDialog;
import com.dongji.market.widget.CustomNoTitleDialog;
import com.dongji.market.widget.SlipSwitch;
import com.dongji.market.widget.SlipSwitch.OnSwitchListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 设置界面
 * 
 * @author yvon
 * 
 */
public class Setting_Activity extends Activity implements OnToolBarBlankClickListener {

	// Body
	private SlipSwitch mUpdate_msg, mAuto_del_pkg, mSave_flow, mSet_root, mAuto_install, mOnly_wifi, mAuto_download_bg, mAuto_update;
	private TextView mAuto_install_text1;
	private TextView mAuto_install_text2;
	private EditText mLimit_flow;
	private TextView mLast_flow;
	private TextView mClear_search_history;
	private TextView mDel_pkg;
	private TextView mAbout;
	private TextView mFeedback;
	private TextView mLimit_text;
	private TextView mM_text;
	private RelativeLayout mUpdate_msg_layout, mAuto_del_pkg_layout, mSave_flow_layout, mSet_root_layout, mAuto_install_layout, mOnly_wifi_layout, mAuto_download_bg_layout, mAuto_update_layout;

	private Setting_Service service;
	private SearchHistory searchHistory;
	private Handler mHandler;

	private TitleUtil titleUtil;

	private static final String APKPath = Environment.getExternalStorageDirectory().getPath() + "/.dongji/dongjiMarket/cache/apk/";
	public static final int EXIT_SETTINGPAGE = 0;
	public static final int LEAVE_SETTINGPAGE = 3;
	private static final int CLEAR_SEARCH_HISTORY = 1;
	private static final int DEL_DOWNLOADED_APK = 2;
	private static final int CHECK_ROOT = 4;

	public static final String SAVE_FLOW_STATUS = "save_flow_status";

	private boolean isLimitFlowChange;
	private int limitFlow;
	private CustomNoTitleDialog mSettingDialog;
	private boolean wifiSettingChange = false;

	private long startTime = 0;

	private View mMaskView;
	private boolean fromBackKey = false;

	private ScrollView mScrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

		startTime = System.currentTimeMillis();

		checkFirstLauncherSetting();

		initView();

		initDBService();

		initRootHandler();

		initSetting();

	}

	private void checkFirstLauncherSetting() {
		SharedPreferences mSharedPreferences = getSharedPreferences(this.getPackageName() + "_temp", Context.MODE_PRIVATE);
		boolean firstLaunch = mSharedPreferences.getBoolean(AConstDefine.FIRST_LAUNCHER_SETTING2, true);
		if (firstLaunch) {
			SharedPreferences.Editor mEditor = mSharedPreferences.edit();
			mEditor.putBoolean(AConstDefine.FIRST_LAUNCHER_SETTING2, false);
			mEditor.commit();
			mMaskView = findViewById(R.id.settingmasklayout);
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

	private void initView() {
		View mTopView = findViewById(R.id.setting_top);
		titleUtil = new TitleUtil(this, mTopView, R.string.setting, new SaveListener(), null, this);

		mUpdate_msg_layout = (RelativeLayout) findViewById(R.id.update_msg_layout);
		mAuto_del_pkg_layout = (RelativeLayout) findViewById(R.id.auto_del_pkg_layout);
		mSave_flow_layout = (RelativeLayout) findViewById(R.id.save_flow_layout);
		mSet_root_layout = (RelativeLayout) findViewById(R.id.set_root_layout);
		mAuto_install_layout = (RelativeLayout) findViewById(R.id.auto_install_layout);
		mOnly_wifi_layout = (RelativeLayout) findViewById(R.id.only_wifi_layout);
		mAuto_download_bg_layout = (RelativeLayout) findViewById(R.id.download_background_layout);
		mAuto_update_layout = (RelativeLayout) findViewById(R.id.auto_update_layout);

		mUpdate_msg = (SlipSwitch) findViewById(R.id.update_msg);
		mAuto_del_pkg = (SlipSwitch) findViewById(R.id.auto_del_pkg);
		mSave_flow = (SlipSwitch) findViewById(R.id.save_flow);
		mSet_root = (SlipSwitch) findViewById(R.id.set_root);
		mAuto_install_text1 = (TextView) findViewById(R.id.auto_install_text1);
		mAuto_install_text2 = (TextView) findViewById(R.id.auto_install_text2);
		mAuto_install = (SlipSwitch) findViewById(R.id.auto_install);
		mOnly_wifi = (SlipSwitch) findViewById(R.id.only_wifi);
		mLimit_flow = (EditText) findViewById(R.id.limet_flow);
		mLast_flow = (TextView) findViewById(R.id.last_flow); // 暂未用
		mAuto_download_bg = (SlipSwitch) findViewById(R.id.auto_download_background);
		mAuto_update = (SlipSwitch) findViewById(R.id.auto_update);
		mClear_search_history = (TextView) findViewById(R.id.clear_search_history);
		mDel_pkg = (TextView) findViewById(R.id.del_pkg);
		mAbout = (TextView) findViewById(R.id.about);
		mFeedback = (TextView) findViewById(R.id.feedback);

		mLimit_text = (TextView) findViewById(R.id.limit_text);
		mM_text = (TextView) findViewById(R.id.M_text);

		mUpdate_msg_layout.setOnClickListener(listener);
		mAuto_del_pkg_layout.setOnClickListener(listener);
		mSave_flow_layout.setOnClickListener(listener);
		mSet_root_layout.setOnClickListener(listener);
		mAuto_install_layout.setOnClickListener(listener);
		mOnly_wifi_layout.setOnClickListener(listener);
		mAuto_download_bg_layout.setOnClickListener(listener);
		mAuto_update_layout.setOnClickListener(listener);
		mClear_search_history.setOnClickListener(listener);
		mDel_pkg.setOnClickListener(listener);
		mAbout.setOnClickListener(listener);
		mFeedback.setOnClickListener(listener);

		mScrollView = (ScrollView) findViewById(R.id.scrollview);

		mSet_root.setOnSwitchListener(new OnSwitchListener() {

			@Override
			public void onSwitched(boolean switchState) {
				if (switchState) {
					mHandler.sendEmptyMessage(CHECK_ROOT);
				} else {
					mSet_root.setSwitchState(false);
					mAuto_install.updateSwitchState(false);
					mAuto_install_layout.setEnabled(false);
					mAuto_install.setEnabled(false);
					mAuto_install_text1.setTextColor(Color.rgb(136, 136, 136));
					mAuto_install_text2.setTextColor(Color.rgb(136, 136, 136));
				}
			}
		});

		mOnly_wifi.setOnSwitchListener(new OnSwitchListener() {

			public void onSwitched(boolean switchState) {
				if (switchState) {
					mLimit_flow.setEnabled(false);
					mLimit_text.setTextColor(Color.rgb(136, 136, 136));// 0x888888
					mM_text.setTextColor(Color.rgb(136, 136, 136));
				} else {
					mLimit_flow.setEnabled(true);
					mLimit_text.setTextColor(Color.rgb(59, 59, 59));// 0x3b3b3b
					mM_text.setTextColor(Color.rgb(59, 59, 59));// 0x3b3b3b
				}
			}
		});
	}

	private void initDBService() {
		if (service == null) {
			service = new Setting_Service(this);
		}
		if (searchHistory == null) {
			searchHistory = new SearchHistory(this);
		}
	}

	private void initRootHandler() {
		HandlerThread handlerThread = new HandlerThread("handler");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHECK_ROOT:
				if (DJMarketUtils.isRoot()) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mSet_root.updateSwitchState(true);
							mAuto_install_layout.setEnabled(true);
							mAuto_install.setEnabled(true);
							mAuto_install_text1.setTextColor(Color.rgb(59, 59, 59));
							mAuto_install_text2.setTextColor(Color.rgb(59, 59, 59));
						}
					});

				} else {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mSet_root.updateSwitchState(false);
							mAuto_install.updateSwitchState(false);
							mAuto_install_layout.setEnabled(false);
							mAuto_install.setEnabled(false);
							mAuto_install_text1.setTextColor(Color.rgb(136, 136, 136));
							mAuto_install_text2.setTextColor(Color.rgb(136, 136, 136));
							DJMarketUtils.showToast(getApplicationContext(), R.string.get_root_failed);
						}
					});
				}
				break;
			}
			super.handleMessage(msg);
		}

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLEAR_SEARCH_HISTORY: // 清除搜索历史
				DJMarketUtils.showToast(Setting_Activity.this, R.string.has_clear_record);
				mClear_search_history.setEnabled(false);
				mClear_search_history.setTextColor(Color.rgb(136, 136, 136));
				break;
			case DEL_DOWNLOADED_APK: // 删除已下载安装包
				DJMarketUtils.showToast(Setting_Activity.this, R.string.has_clear_pkgs);
				mDel_pkg.setEnabled(false);
				mDel_pkg.setTextColor(Color.rgb(136, 136, 136));
				break;
			}
		}

	};

	/**
	 * 打开设置界面初始化为历史设置
	 */
	private void initSetting() {
		if (service.select("update_msg") == 1) {
			mUpdate_msg.setSwitchState(true);// 开启应用更新通知
		} else {
			mUpdate_msg.setSwitchState(false);
		}
		if (service.select("auto_del_pkg") == 1) {
			mAuto_del_pkg.setSwitchState(true);// 开启安装后自动删除安装包
		} else {
			mAuto_del_pkg.setSwitchState(false);
		}
		if (service.select("save_flow") == 1) {
			mSave_flow.setSwitchState(true);// 开启节省流量模式（应用列表不加载图片)
		} else {
			mSave_flow.setSwitchState(false);
		}
		if (service.select("set_root") == 1) {
			mSet_root.setSwitchState(true);// 开启root权限

			mAuto_install_layout.setEnabled(true);
			mAuto_install.setEnabled(true);
			mAuto_install_text1.setTextColor(Color.rgb(59, 59, 59));
			mAuto_install_text2.setTextColor(Color.rgb(59, 59, 59));
			if (service.select("auto_install") == 1) {
				mAuto_install.setSwitchState(true);// 开启自动安装
			} else {
				mAuto_install.setSwitchState(false);
			}
		} else {
			mSet_root.setSwitchState(false);

			mAuto_install.setSwitchState(false);
			mAuto_install_layout.setEnabled(false);
			mAuto_install.setEnabled(false);
			mAuto_install_text1.setTextColor(Color.rgb(136, 136, 136));
			mAuto_install_text2.setTextColor(Color.rgb(136, 136, 136));
		}
		limitFlow = service.select("limit_flow");// 限制可用于下载应用的流量数
		if (service.select("only_wifi") == 1) {// 仅使用wifi下载
			mOnly_wifi.setSwitchState(true);
			mLimit_flow.setText(String.valueOf(limitFlow));
			mLimit_flow.setEnabled(false);
			mLimit_text.setTextColor(Color.rgb(136, 136, 136));
			mM_text.setTextColor(Color.rgb(136, 136, 136));
		} else {
			mOnly_wifi.setSwitchState(false);
			mLimit_flow.setEnabled(true);
			mLimit_text.setTextColor(Color.rgb(59, 59, 59));// 0x3b3b3b
			mM_text.setTextColor(Color.rgb(59, 59, 59));// 0x3b3b3b
			mLimit_flow.setText(String.valueOf(limitFlow));
		}
		mLast_flow.setText(getLastFlow());// 设置剩余流量数

		if (service.select("download_bg") == 1) {// 开启退出程序后，自动下载未完成的应用
			mAuto_download_bg.setSwitchState(true);
		} else {
			mAuto_download_bg.setSwitchState(false);
		}
		if (service.select("auto_update") == 1) {// 开启自动更新应用
			mAuto_update.setSwitchState(true);
		} else {
			mAuto_update.setSwitchState(false);
		}
		// initDBService();
		if (searchHistory.getCount() > 0) {
			mClear_search_history.setEnabled(true);// 查询搜索记录，如果大于0条，则可点击
			mClear_search_history.setTextColor(Color.rgb(59, 59, 59));
		} else {
			mClear_search_history.setEnabled(false);
			mClear_search_history.setTextColor(Color.rgb(136, 136, 136));
		}
		if (isEmptyDir()) {
			mDel_pkg.setEnabled(false);// 如果有可删除的安装包，则可点击
			mDel_pkg.setTextColor(Color.rgb(136, 136, 136));
		} else {
			mDel_pkg.setEnabled(true);
			mDel_pkg.setTextColor(Color.rgb(59, 59, 59));
		}
	}

	/**
	 * 判断文件夹中是否存在安装包
	 * 
	 * @return
	 */
	private boolean isEmptyDir() {
		File[] list = new File(APKPath).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".apk");
			}
		});
		if (list != null && list.length > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 每次打开设置界面更新一次剩余蜂窝下载流量
	 * 
	 * @return
	 */
	private String getLastFlow() {
		float temp = limitFlow - getUsedFlow();
		DecimalFormat decimal = new DecimalFormat("#.##");
		return decimal.format(temp < 0 ? 0 : temp);
	}

	/**
	 * 获取已使用流量
	 * 
	 * @return
	 */
	private float getUsedFlow() {
		long num = queryUse3GSize();
		float used_flow = b2mb(num < 0 ? 0 : num);
		return used_flow;
	}

	/**
	 * 查询当前使用3G下载所消耗的流量大小
	 * 
	 * @return
	 */
	private long queryUse3GSize() {
		return DJMarketUtils.queryUse3GDownloadSize(this);
	}

	/**
	 * 将byte转换成Mb
	 * 
	 * @param size
	 * @return
	 */
	private float b2mb(long size) {
		float size_mb = (float) size / 1024 / 1024;
		return size_mb;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("打开设置页面到显示耗时为:" + (System.currentTimeMillis() - startTime));
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (mMaskView != null && mMaskView.getVisibility() == View.VISIBLE) {
				mMaskView.setVisibility(View.GONE);
				return true;
			}

			int value = 0;
			if (TextUtils.isEmpty(mLimit_flow.getText())) {
				isLimitFlowChange = true;
				value = -1;
			} else {
				value = Integer.valueOf(mLimit_flow.getText().toString());
				if (value != limitFlow) {
					isLimitFlowChange = true;
				}
			}
			if (isLimitFlowChange) {
				fromBackKey = true;
				showSettingChangedDialog(true, -1);
				return true;
			} else {
				save2db();
			}
		}
		return super.onKeyDown(keyCode, event);
	};

	/**
	 * 当流量限制发生改变时，跳转页面弹出提示
	 * 
	 * @param isFinish
	 *            　值为true时，返回并关闭当前activity；值为false时，跳转但不关闭activity
	 * @param pageFlag
	 *            　标记所要到达的页面
	 */
	private void showSettingChangedDialog(final boolean isFinish, final int pageFlag) {
		if (!isFinishing()) {
			if (mSettingDialog == null) {
				mSettingDialog = new CustomNoTitleDialog(this);
				mSettingDialog.setMessage(R.string.setting_for_flow_changed);
				mSettingDialog.setNeutralButton(getString(R.string.confirm), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int num = 0;
						if (!TextUtils.isEmpty(mLimit_flow.getText())) {
							num = Integer.parseInt(mLimit_flow.getText().toString());
						}
						if (num > 2048) {
							DJMarketUtils.showToast(Setting_Activity.this, getResources().getString(R.string.limit_flow_too_large));
							mLimit_flow.requestFocus();
						} else {
							save2db();
							if (isFinish) {
								if (fromBackKey) {
									finish();
								} else {
									sendBroadcast(new Intent(AConstDefine.GO_HOME_BROADCAST));
								}
							} else {
								titleUtil.toOtherPage(pageFlag);
							}
						}
						mSettingDialog.dismiss();
					}
				});
				mSettingDialog.setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mSettingDialog.dismiss();
					}
				});
			}
			if (mSettingDialog != null) {
				mSettingDialog.show();
			}
		}
	}

	/**
	 * 退出页面时保存到数据库
	 */
	private void save2db() {
		if (checkSaveFlowTypeChanged()) {// 流量模式是否发生改变，是则发送广播，通知是否可下载图片
			Intent intent = new Intent(AConstDefine.SAVE_FLOW_BROADCAST);
			intent.putExtra(SAVE_FLOW_STATUS, mSave_flow.getSwitchState());
			sendBroadcast(intent);
		}
		if (mUpdate_msg.getSwitchState()) {
			service.update("update_msg", 1);
		} else {
			service.update("update_msg", 0);
		}
		if (mAuto_del_pkg.getSwitchState()) {
			service.update("auto_del_pkg", 1);
		} else {
			service.update("auto_del_pkg", 0);
		}
		if (mSave_flow.getSwitchState()) {
			((AppMarket) getApplication()).setRemoteImage(false);
			service.update("save_flow", 1);
		} else {
			((AppMarket) getApplication()).setRemoteImage(true);
			service.update("save_flow", 0);
		}
		if (mSet_root.getSwitchState()) {
			service.update("set_root", 1);
		} else {
			service.update("set_root", 0);
		}
		if (mAuto_install.getSwitchState()) {
			service.update("auto_install", 1);
		} else {
			service.update("auto_install", 0);
		}
		if (mOnly_wifi.getSwitchState()) {
			service.update("only_wifi", 1);
			System.out.println("662:" + wifiSettingChange + ", " + isLimitFlowChange);
			int value = -1;
			Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_GPRS_SETTING_CHANGE);
			Bundle bundle = new Bundle();
			if (isLimitFlowChange) {
				if (TextUtils.isEmpty(mLimit_flow.getText())) {
					value = 50;
				} else {
					value = Integer.parseInt(mLimit_flow.getText().toString());
				}
			}
			bundle.putLong("limitFlow", value);
			bundle.putBoolean("isOnlyWifi", true);
			intent.putExtras(bundle);
			sendBroadcast(intent);
		} else {
			service.update("only_wifi", 0);
			int value = 0;
			if (TextUtils.isEmpty(mLimit_flow.getText())) {
				value = 50;
			} else {
				value = Integer.parseInt(mLimit_flow.getText().toString());
			}
			System.out.println("686:" + wifiSettingChange + ", " + isLimitFlowChange);
			Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_GPRS_SETTING_CHANGE);
			Bundle bundle = new Bundle();
			if (isLimitFlowChange) {
				service.update("limit_flow", value);
				// 每次修改限制流量必须清零已使用流量
				clearUsedFlow();

				bundle.putLong("limitFlow", value);
			} else {
				bundle.putLong("limitFlow", -1);
			}
			bundle.putBoolean("isOnlyWifi", false);
			intent.putExtras(bundle);
			sendBroadcast(intent);
		}
		if (mAuto_download_bg.getSwitchState()) {
			service.update("download_bg", 1);
		} else {
			service.update("download_bg", 0);
		}
		if (mAuto_update.getSwitchState()) {
			service.update("auto_update", 1);
		} else {
			service.update("auto_update", 0);
		}
	}

	/**
	 * 检测节省流量模式状态是否改变
	 * 
	 * @return
	 */
	private boolean checkSaveFlowTypeChanged() {
		int currentType = mSave_flow.getSwitchState() ? 1 : 0;
		if (service.select("save_flow") != currentType) {
			return true;
		}
		return false;
	}

	/**
	 * 每次修改限制流量值后都需要对已用流量清零
	 */
	private void clearUsedFlow() {
		SharedPreferences pref = getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putLong(AConstDefine.SHARE_DOWNLOADSIZE, 0);
		editor.commit();
	}

	@Override
	protected void onRestart() {
		initSetting();
		isLimitFlowChange = false;
		super.onRestart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.update_msg_layout:
				if (mUpdate_msg.getSwitchState()) {
					mUpdate_msg.updateSwitchState(false);
				} else {
					mUpdate_msg.updateSwitchState(true);
				}
				break;
			case R.id.auto_del_pkg_layout:
				if (mAuto_del_pkg.getSwitchState()) {
					mAuto_del_pkg.updateSwitchState(false);
				} else {
					mAuto_del_pkg.updateSwitchState(true);
				}
				break;
			case R.id.save_flow_layout:
				if (mSave_flow.getSwitchState()) {
					mSave_flow.updateSwitchState(false);
				} else {
					mSave_flow.updateSwitchState(true);
				}
				break;
			case R.id.set_root_layout:
				if (mSet_root.getSwitchState()) {
					mSet_root.updateSwitchState(false);

					mAuto_install.updateSwitchState(false);
					mAuto_install_layout.setEnabled(false);
					mAuto_install.setEnabled(false);
					mAuto_install_text1.setTextColor(Color.rgb(136, 136, 136));
					mAuto_install_text2.setTextColor(Color.rgb(136, 136, 136));
				} else {
					mSet_root.updateSwitchState(true);
				}
				break;
			case R.id.auto_install_layout:
				if (mSet_root.getSwitchState()) {
					if (mAuto_install.getSwitchState()) {
						mAuto_install.updateSwitchState(false);
					} else {
						mAuto_install.updateSwitchState(true);
					}

				}
				break;
			case R.id.only_wifi_layout:
				if (mOnly_wifi.getSwitchState()) {
					mOnly_wifi.updateSwitchState(false);
				} else {
					mOnly_wifi.updateSwitchState(true);
				}
				wifiSettingChange = true;
				break;
			case R.id.download_background_layout:
				if (mAuto_download_bg.getSwitchState()) {
					mAuto_download_bg.updateSwitchState(false);
				} else {
					mAuto_download_bg.updateSwitchState(true);
				}
				break;
			case R.id.auto_update_layout:
				if (mAuto_update.getSwitchState()) {
					mAuto_update.updateSwitchState(false);
				} else {
					mAuto_update.updateSwitchState(true);
				}
				break;
			case R.id.clear_search_history:
				if (!isFinishing()) {
					final CustomDialog clearSearchDialog = new CustomDialog(Setting_Activity.this).setIcon(R.drawable.icon);
					clearSearchDialog.setTitle(R.string.clear_record);

					clearSearchDialog.setMessage(R.string.confirm_clear_record).setPositiveButton(R.string.confirm, new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (searchHistory == null) {
								searchHistory = new MarketDatabase.SearchHistory(Setting_Activity.this);
							}
							searchHistory.delAll();
							clearSearchDialog.dismiss();
							handler.sendEmptyMessage(CLEAR_SEARCH_HISTORY);
						}
					}).setNegativeButton(R.string.cancel, new OnClickListener() {

						@Override
						public void onClick(View v) {
							clearSearchDialog.dismiss();
						}
					});

					if (clearSearchDialog != null) {
						clearSearchDialog.show();
					}
				}
				break;
			case R.id.del_pkg:
				if (!isFinishing()) {
					final CustomDialog clearPkgsDialog = new CustomDialog(Setting_Activity.this).setIcon(R.drawable.icon);
					clearPkgsDialog.setTitle(R.string.del_pkg2);
					clearPkgsDialog.setMessage(R.string.confirm_clear_pkgs).setPositiveButton(R.string.confirm, new OnClickListener() {

						@Override
						public void onClick(View v) {

							del_packages();
							clearPkgsDialog.dismiss();
							sendBroadcast(new Intent(AConstDefine.BROADCAST_DEL_DOWNLOADED_APK));
							handler.sendEmptyMessage(DEL_DOWNLOADED_APK);
						}
					}).setNegativeButton(R.string.cancel, new OnClickListener() {

						@Override
						public void onClick(View v) {
							clearPkgsDialog.dismiss();
						}
					});
					if (clearPkgsDialog != null) {
						clearPkgsDialog.show();
					}
				}
				break;
			case R.id.about:

				if (!isFinishing()) {
					Intent intent = new Intent(Setting_Activity.this, AboutUsActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.feedback:
				Intent intent = new Intent(Setting_Activity.this, FeedbackActivity.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 删除已下载的安装包
	 */
	private void del_packages() {
		File[] list = new File(APKPath).listFiles(new FilenameFilter() {

			public boolean accept(File dir, String filename) {
				return filename.endsWith(".apk");
			}
		});
		if (list != null) {
			for (File file : list) {
				file.delete();
			}
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
			return true;
		}
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}

	@Override
	protected void onDestroy() {
		titleUtil.unregisterMyReceiver(this);
		super.onDestroy();
	}

	@Override
	public void onClick() {
		if (mScrollView != null) {
			mScrollView.smoothScrollTo(0, 0);
		}
	}

	class SaveListener implements SaveSettingListener {

		@Override
		public void exitVerify(boolean isFinish, int pageFlag) {
			int value = 0;
			if (TextUtils.isEmpty(mLimit_flow.getText())) {
				isLimitFlowChange = true;
				value = -1;
			} else {
				value = Integer.valueOf(mLimit_flow.getText().toString());
				if (value != limitFlow) {
					isLimitFlowChange = true;
				}
			}
			if (isLimitFlowChange) {
				fromBackKey = false;
				showSettingChangedDialog(isFinish, pageFlag);
				return;
			} else {
				save2db();
				if (isFinish) {
					sendBroadcast(new Intent(AConstDefine.GO_HOME_BROADCAST));
				} else {
					titleUtil.toOtherPage(pageFlag);
				}
			}
		}
	}

}
