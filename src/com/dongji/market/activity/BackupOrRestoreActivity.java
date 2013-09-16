package com.dongji.market.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.myjson.JSONException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dongji.market.R;
import com.dongji.market.adapter.ChooseToBackupAdapter;
import com.dongji.market.adapter.ChooseToCloudRestoreAdapter;
import com.dongji.market.application.AppMarket;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.FileLoadTask;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.BackupItemInfo;
import com.dongji.market.pojo.InstalledAppInfo;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.protocol.DataManager;

/**
 * 应用备份和恢复
 * @author yvon
 *
 */
public class BackupOrRestoreActivity extends Activity implements AConstDefine, OnCheckedChangeListener {
	private static final int EVENT_REQUEST_SOFTWARE_LIST = 0;
	private static final int EVENT_LOADED = 1;
	private static final int EVENT_LOCAL_BACKUP = 2;
	private static final int EVENT_CLOUND_BACKUP = 3;
	private static final int EVENT_CLOUND_RESTORE = 4;
	private static final int EVENT_LOCAL_BACKUPRESULT = 5;
	private static final int EVENT_LOCAL_RESTORE = 6;
	public static final int EVENT_CHECKCHANGE = 7;

	private NotificationManager mNotificationManager;
	private Notification mNotification;

	private ChooseToBackupAdapter chooseToBackupAdapter;
	private ChooseToCloudRestoreAdapter chooseToCloudRestoreAdapter;
	private FileLoadTask task;

	private TextView tvTitleBAndR;
	private Button btnBAndR;
	private ListView lvBackupList;
	private TextView tvNoAppTips;

	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	private MyHandler mHandler;
	private int flag;

	private List<InstalledAppInfo> installInfos = null;

	private MyBroadcastReceiver myBroadcastReceiver;
	private long totalSize;

	private AppMarket mApp = null;
	private List<BackupItemInfo> backupItemInfos = new ArrayList<BackupItemInfo>();

	private boolean btnCheck = false;
	private CheckBox mCheckBox;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backuplist);
		mApp = (AppMarket) getApplication();
		
		initHandler();
		initBroadcastReceiver();
		initDataAndView();
		startLoad();
	}
	
	/**
	 * 初始化handler
	 */
	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("handler");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
	}
	
	/**
	 * 注册应用安装、卸载广播
	 */
	private void initBroadcastReceiver() {
		myBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE);
		filter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL);
		filter.addDataScheme("package");
		registerReceiver(myBroadcastReceiver, filter);
	}

	/**
	 * 初始化视图和数据
	 */
	private void initDataAndView() {
		mCheckBox = (CheckBox) findViewById(R.id.allcheckbox);
		mCheckBox.setOnCheckedChangeListener(this);
		mCheckBox.setOnClickListener(new OnClickListener() {//checkbox按钮监听

			@Override
			public void onClick(View v) {
				btnCheck = true;
				if (mCheckBox.isChecked()) {
					onCheckedChanged(mCheckBox, true);
				} else {
					onCheckedChanged(mCheckBox, false);
				}
			}
		});
		tvTitleBAndR = (TextView) findViewById(R.id.tvTitleBAndR);
		btnBAndR = (Button) findViewById(R.id.btnBAndR);
		lvBackupList = (ListView) findViewById(R.id.lvBackupList);
		tvNoAppTips = (TextView) findViewById(R.id.tvNoAppTips);
		flag = getIntent().getIntExtra(FLAG_ACTIVITY_BANDR, 0);
		if (flag == AConstDefine.ACTIVITY_RESTORE || flag == AConstDefine.ACTIVITY_CLOUD_RESTORE) {//如果是本地恢复或者是云恢复
			tvTitleBAndR.setText(R.string.title_choosetorestore);
			btnBAndR.setText(R.string.backgroup_restore);
			tvNoAppTips.setText(R.string.noRestoreApp);
		}
		btnBAndR.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (flag) {
				case ACTIVITY_BACKUP:
					mHandler.sendEmptyMessage(EVENT_LOCAL_BACKUP);//本地备份
					break;
				case ACTIVITY_RESTORE:
					mHandler.sendEmptyMessage(EVENT_LOCAL_RESTORE);//本地恢复
					break;
				case ACTIVITY_CLOUD_BACKUP://云备份
					onClickCloudBackup();
					break;
				case ACTIVITY_CLOUD_RESTORE://云恢复
					onClickCloudRestore();
					break;
				}
			}
		});
	}
	
	/**
	 * 开始加载数据
	 */
	private void startLoad() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mLoadingProgressBar = findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) findViewById(R.id.loading_textview);
		lvBackupList.setVisibility(View.GONE);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					mHandler.sendEmptyMessage(EVENT_CLOUND_RESTORE);//云恢复消息
				}
				return false;
			}
		});
		if (flag == ACTIVITY_CLOUD_RESTORE) {//如果是云恢复则发送云恢复消息
			mHandler.sendEmptyMessage(EVENT_CLOUND_RESTORE);
		} else {//其它则先获取软件列表
			mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
		}
	}
	
	/**
	 * 消息处理
	 * @author luonian
	 *
	 */
	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_SOFTWARE_LIST:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (chooseToBackupAdapter == null) {//初始化选择adapter
							chooseToBackupAdapter = new ChooseToBackupAdapter(BackupOrRestoreActivity.this, new ArrayList<InstalledAppInfo>(), mHandler);
							lvBackupList.setAdapter(chooseToBackupAdapter);
						}
						lvBackupList.setSelection(0);
						task = new FileLoadTask(BackupOrRestoreActivity.this, chooseToBackupAdapter, mHandler, flag);// 本地图片异步加载
						task.execute();
					}
				});
				break;
			case EVENT_LOADED:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mLoadingView.setVisibility(View.GONE);
						if (null == lvBackupList || lvBackupList.getCount() == 0) {
							tvNoAppTips.setVisibility(View.VISIBLE);
							lvBackupList.setVisibility(View.GONE);
						} else {
							tvNoAppTips.setVisibility(View.GONE);
							lvBackupList.setVisibility(View.VISIBLE);
						}
					}

				});
				break;
			case EVENT_LOCAL_BACKUP://本地备份
				onClickBackup();
				break;
			case EVENT_CLOUND_BACKUP://云备份
				break;
			case EVENT_CLOUND_RESTORE://云恢复
				DataManager dataManager = DataManager.newInstance();
				ArrayList<ApkItem> apkItems = null;
				try {
					System.out.println("=========username:" + mApp.getLoginParams().getUserName());
					apkItems = dataManager.getCloudRecoverList(mApp.getLoginParams().getUserName());
				} catch (IOException e) {
					setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
					System.out.println("ioexcepiton............backupacitivty");
					break;
				} catch (JSONException e) {
					setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
					System.out.println("JSONException............backupacitivty");
					break;
				}
				final ArrayList<ApkItem> items = apkItems;
				if (apkItems != null && apkItems.size() > 0) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							chooseToCloudRestoreAdapter = new ChooseToCloudRestoreAdapter(BackupOrRestoreActivity.this, items, mHandler);
							lvBackupList.setAdapter(chooseToCloudRestoreAdapter);
							sendEmptyMessage(EVENT_LOADED);
						}
					});
				} else {
					sendEmptyMessage(EVENT_LOADED);
				}
				break;
			case EVENT_LOCAL_BACKUPRESULT://本地备份结果
				PackageManager pm = getPackageManager();
				ApplicationInfo info;
				for (int i = 0; i < backupItemInfos.size(); i++) {
					try {
						info = pm.getApplicationInfo(backupItemInfos.get(i).appName, PackageManager.GET_ACTIVITIES);
						backupItemInfos.get(i).appName = info.sourceDir;
						long size = (new File(info.publicSourceDir).length());
						totalSize += size;
						backupItemInfos.get(i).appSize = size;
					} catch (NameNotFoundException e) {
						showLog(e.toString());
					}
				}
				runOnUiThread(new Runnable() {
					public void run() {
						showMyToast(R.string.toast_backuping);
					}
				});
				showNotification(R.drawable.icon, getResources().getString(R.string.local_backup), getResources().getString(R.string.local_backuping));
				NetTool.startToLocalBackup(BackupOrRestoreActivity.this, backupItemInfos, mOnProgressChangeListener);

				runOnUiThread(new Runnable() {
					public void run() {
						mNotificationManager.cancel(0);
						showMyToast(R.string.backup_success);
					}
				});

				break;
			case EVENT_LOCAL_RESTORE://本地恢复
				onClickRestore();
				break;
			case EVENT_CHECKCHANGE://检查改变
				if (null == chooseToBackupAdapter) {
					btnCheck = false;
					List<Integer> checkdList = chooseToCloudRestoreAdapter.getCheckdList();
					int checkCount = 0;
					if (null != checkdList && checkdList.size() > 0) {
						final boolean isAll;
						for (int i = 0; i < checkdList.size(); i++) {
							if (checkdList.get(i) == 1) {
								checkCount++;
							}
						}
						if (checkCount == checkdList.size()) {
							isAll = true;
						} else {
							isAll = false;
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mCheckBox.setChecked(isAll);
							}
						});
					}
				} else {
					btnCheck = false;
					List<Integer> checkdList = chooseToBackupAdapter.getCheckdList();
					int checkCount = 0;
					if (null != checkdList && checkdList.size() > 0) {
						final boolean isAll;
						for (int i = 0; i < checkdList.size(); i++) {
							if (checkdList.get(i) == 1) {
								checkCount++;
							}
						}
						if (checkCount == checkdList.size()) {
							isAll = true;
						} else {
							isAll = false;
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mCheckBox.setChecked(isAll);
							}
						});
					}
				}

				break;
			}
		}
	}

	/**
	 * 本地备份
	 */
	private void onClickBackup() {
		if (null != chooseToBackupAdapter) {
			List<Integer> checkFlag = chooseToBackupAdapter.getCheckdList();//获取需备份的应用列表
			showLog(null == checkFlag ? "null" : ("not null" + checkFlag.size()));
			if (null == checkFlag || checkFlag.size() == 0) {
				showMyToast(R.string.not_backup);
				return;
			}
			Uninstall_list_Activity.cloudBackupOngoing = true;
			List<String> nameList = new ArrayList<String>();
			String apkName;
			BackupItemInfo backupItemInfo;
			long totalBackupSize = 0;
			boolean isSelected = false, isBakcup = false;
			for (int i = 0; i < checkFlag.size(); i++) {
				if (checkFlag.get(i) == 1) {
					if (!isSelected) {
						isSelected = true;
					}
					InstalledAppInfo installedAppInfo = (InstalledAppInfo) chooseToBackupAdapter.getItem(i);

					apkName = installedAppInfo.getPkgName() + "_" + installedAppInfo.getVersion();
					totalBackupSize += DJMarketUtils.sizeFromMToLong(installedAppInfo.getSize());
					nameList.add(apkName);
					if (!NetTool.checkBackupApkIsExist(apkName)) {
						isBakcup = true;
						backupItemInfo = new BackupItemInfo();
						backupItemInfo.appName = installedAppInfo.getPkgName();
						backupItemInfo.appVerCode = installedAppInfo.getVersion();
						backupItemInfos.add(backupItemInfo);
					}
				}
			}
			if (!isSelected) {
				showMyToast(R.string.not_select_backup);
				return;
			}
			NetTool.deleteNoBackupApk(nameList);
			if (isBakcup) {
				if (!AndroidUtils.isSdcardExists()) {
					showMyToast(R.string.nosdcardtobackup);
				} else if (AndroidUtils.getSdcardAvalilaleSize() < totalBackupSize) {
					showMyToast(R.string.sdcardnospacetobackup);
				} else {
					mHandler.sendEmptyMessage(EVENT_LOCAL_BACKUPRESULT);
				}
			} else {
				showMyToast(R.string.apkalreadybackup);
			}
			Uninstall_list_Activity.cloudBackupOngoing = false;
			sendBroadcast(new Intent(BROADCAST_ACTION_SHOWUNINSTALLLIST));
		}
	}

	/**
	 * 本地恢复
	 */
	private void onClickRestore() {
		if (null != chooseToBackupAdapter) {
			List<Integer> checkFlag = chooseToBackupAdapter.getCheckdList();
			if (null == checkFlag || checkFlag.size() == 0) {
				showMyToast(R.string.not_recovery);
				return;
			}
			Uninstall_list_Activity.cloudRestoreOngoing = true;
			boolean isSelected = false;
			boolean isRootInstall = DJMarketUtils.isDefaultInstall(this);
			if (isRootInstall) {
				isRootInstall = AndroidUtils.isRoot();
			}
			for (int i = 0; i < checkFlag.size(); i++) {
				if (checkFlag.get(i) == 1) {
					if (!isSelected) {
						isSelected = true;
						showMyToast(R.string.cloud_restoring);
					}
					InstalledAppInfo installedAppInfo = (InstalledAppInfo) chooseToBackupAdapter.getItem(i);
					int versionCode = DJMarketUtils.getInstalledAppVersionCodeByPackageName(BackupOrRestoreActivity.this, installedAppInfo.getPkgName());
					if (installedAppInfo.getVersionCode() != versionCode) {
						String apkPath = installedAppInfo.getPkgName() + "_" + installedAppInfo.getVersion();
						Intent installIntent = new Intent(Intent.ACTION_VIEW);
						installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						installIntent.setDataAndType(Uri.fromFile(new File(NetTool.BACKUPPATH + apkPath + ".apk")), "application/vnd.android.package-archive");
						startActivity(installIntent);
					}
				}
			}
			if (!isSelected) {
				showMyToast(R.string.not_select_recovery);
				return;
			}
			Intent intent = new Intent(BROADCAST_ACTION_SHOWUNINSTALLLIST);
			Uninstall_list_Activity.cloudRestoreOngoing = false;
			sendBroadcast(intent);
		}
	}

	/**
	 * 云备份
	 */
	private void onClickCloudBackup() {
		if (null != chooseToBackupAdapter) {
			List<Integer> checkFlag = chooseToBackupAdapter.getCheckdList();

			if (checkFlag == null) {
				showMyToast(R.string.not_backup);
				return;
			}
			Uninstall_list_Activity.cloudBackupOngoing = true;
			// String apkName;
			boolean isSelected = false;
			LoginParams loginParams = ((AppMarket) getApplicationContext()).getLoginParams();
			String sessionId = loginParams.getUserName();
			List<String[]> list = new ArrayList<String[]>();
			String[] listItem;
			for (int i = 0; i < checkFlag.size(); i++) {
				if (checkFlag.get(i) == 1) {
					if (!isSelected) {
						isSelected = true;
						showMyToast(R.string.toast_cloud_backuping);
					}

					InstalledAppInfo installedAppInfo = (InstalledAppInfo) chooseToBackupAdapter.getItem(i);
					listItem = new String[2];
					listItem[0] = String.valueOf(installedAppInfo.getVersionCode());
					listItem[1] = installedAppInfo.getPkgName();
					list.add(listItem);
				}
			}
			if (!isSelected) {
				showMyToast(R.string.not_select_backup);
				return;
			}
			boolean isSuccess = false;
			try {
				System.out.println("=========username:" + mApp.getLoginParams().getUserName());
				isSuccess = DataManager.newInstance().cloudBackup(list, sessionId, BackupOrRestoreActivity.this);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (isSuccess) {
				showMyToast(R.string.cloud_backup_completed);
			} else {
				showMyToast(R.string.cloud_backup_failed);
			}

			Uninstall_list_Activity.cloudBackupOngoing = false;
			sendBroadcast(new Intent(BROADCAST_ACTION_SHOWUNINSTALLLIST));
		}
	}

	/**
	 * 云恢复
	 */
	private void onClickCloudRestore() {
		if (null != chooseToCloudRestoreAdapter) {
			List<Integer> checkFlag = chooseToCloudRestoreAdapter.getCheckdList();
			ArrayList<ApkItem> apkItems = new ArrayList<ApkItem>();
			boolean isSelected = false;
			for (int i = 0; i < checkFlag.size(); i++) {
				if (checkFlag.get(i) == 1) {
					if (!isSelected) {
						isSelected = true;
						showMyToast(R.string.toast_cloud_restoreing);
					}
					ApkItem apkItem = (ApkItem) chooseToCloudRestoreAdapter.getItem(i);
					if (apkItem.versionCode != DJMarketUtils.getInstalledAppVersionCodeByPackageName(BackupOrRestoreActivity.this, apkItem.packageName)) {
						apkItems.add(apkItem);
					}
				}
			}
			if (!isSelected) {
				showMyToast(R.string.not_select_backup);
				return;
			}
			if (apkItems.size() == 0) {
				showMyToast(R.string.not_cloudrestore);
			} else {
				DownloadUtils.checkCloudRestore(getParent(), apkItems);
			}
			sendBroadcast(new Intent(BROADCAST_ACTION_SHOWUNINSTALLLIST));
		}
	}

	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				stopProgressBar();
				mLoadingView.setVisibility(View.VISIBLE);
				mLoadingProgressBar.setVisibility(View.GONE);
				mLoadingTextView.setVisibility(View.VISIBLE);
				mLoadingTextView.setText(rId);
			}
		});
	}

	public void stopProgressBar() {
		if (mHandler.hasMessages(EVENT_REQUEST_SOFTWARE_LIST)) {
			mHandler.removeMessages(EVENT_REQUEST_SOFTWARE_LIST);
		}
		if (mHandler.hasMessages(EVENT_LOADED)) {
			mHandler.removeMessages(EVENT_LOADED);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		System.out.println("===================== destroy");
		if (mNotificationManager != null) {
			mNotificationManager.cancel(0);
		}
		unregisterReceiver(myBroadcastReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println("子类中onkeydown...");
		return true;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		System.out.println("dispatchKeyEvent.................");
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
			BackupOrRestoreActivity.this.finish();
		}
		return super.dispatchKeyEvent(event);
	}
	
	/**
	 * 结束当前activity
	 */
	@Override
	public void onBackPressed() {
		System.out.println("onbackpressed");
		BackupOrRestoreActivity.this.finish();
	}

	
	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	

	/**
	 * 应用安装卸载广播接收器
	 * @author luonian
	 *
	 */
	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = DJMarketUtils.convertPackageName(intent.getDataString());
			if (AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE.equals(intent.getAction())) {
				if (flag == ACTIVITY_CLOUD_RESTORE) {
					if (chooseToBackupAdapter != null) {
						for (int i = 0; i < chooseToBackupAdapter.getCount(); i++) {
							InstalledAppInfo info = (InstalledAppInfo) chooseToBackupAdapter.getItem(i);
							if (packageName.equals(info.getPkgName())) {
								chooseToBackupAdapter.removeItemByPosition(i);
								break;
							}
						}
					}
				} else {
					if (chooseToCloudRestoreAdapter != null) {
						for (int i = 0; i < chooseToCloudRestoreAdapter.getCount(); i++) {
							ApkItem item = (ApkItem) chooseToCloudRestoreAdapter.getItem(i);
							if (packageName.equals(item.packageName)) {
								chooseToCloudRestoreAdapter.removeItemByPosition(i);
								break;
							}
						}
					}
				}
			} else if (AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL.equals(intent.getAction())) {
				System.out.println("clound " + packageName + ", " + installInfos);
				if (installInfos != null) {
					int i = 0;
					for (; i < installInfos.size(); i++) {
						if (packageName.equals(installInfos.get(i).getPkgName())) {
							mOnProgressChangeListener.onProgressChange(i + 1);
							installInfos.remove(i--);
							break;
						}
					}

				}
			}
		}
	}

	private void showMyToast(int msgId) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	private void showLog(String msg) {
		Log.d("BackupOrRestoreActivity", msg);
	}
	

	public interface OnProgressChangeListener {
		void onProgressChange(long progress);
	}

	private OnProgressChangeListener mOnProgressChangeListener = new OnProgressChangeListener() {
		private long currentSize;
		private java.text.DecimalFormat df = new java.text.DecimalFormat("00");

		@Override
		public void onProgressChange(final long progress) {
			runOnUiThread(new Runnable() {
				public void run() {
					currentSize += progress;
					if (currentSize < totalSize) {
						int d = (int) (currentSize * 100 / totalSize);
						int num = Integer.valueOf(df.format(d));
						mNotification.contentView.setProgressBar(R.id.cloud_opt_progress, 100, num, false);
						mNotification.contentView.setTextViewText(R.id.cloud_progress_rate, num + "%");
						mNotificationManager.notify(0, mNotification);
					}
				}
			});
		}
	};

	/**
	 * checkbox变更
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (btnCheck) {
			if (chooseToBackupAdapter != null) {
				chooseToBackupAdapter.setAllChecked(isChecked);
			}
			if (chooseToCloudRestoreAdapter != null) {
				chooseToCloudRestoreAdapter.setAllChecked(isChecked);
			}
		}
	}

	
	/**
	 * 通知栏通知备份
	 * @param icon
	 * @param tickerText
	 * @param title
	 */
	private void showNotification(int icon, String tickerText, String title) {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotification = new Notification();
		mNotification.icon = icon;
		mNotification.tickerText = tickerText;
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		mNotification.contentIntent = pendingIntent;
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_cloud_progress);
		remoteViews.setTextViewText(R.id.cloud_title, title);
		remoteViews.setTextViewText(R.id.cloud_progress_rate, "0%");
		remoteViews.setProgressBar(R.id.cloud_opt_progress, 100, 0, false);
		mNotification.contentView = remoteViews;
		mNotificationManager.notify(0, mNotification);
	}

	private int locStep;

	void onToolBarClick() {
		if (lvBackupList != null) {
			locStep = (int) Math.ceil(lvBackupList.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
			lvBackupList.post(scrollToTop);
		}
	}

	Runnable scrollToTop = new Runnable() {

		@Override
		public void run() {
			if (lvBackupList.getFirstVisiblePosition() > 0) {
				if (lvBackupList.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					lvBackupList.setSelection(lvBackupList.getFirstVisiblePosition() - 1);
				} else {
					lvBackupList.setSelection(Math.max(lvBackupList.getFirstVisiblePosition() - locStep, 0));
				}
				lvBackupList.post(this);
			}
			return;
		}
	};
}
