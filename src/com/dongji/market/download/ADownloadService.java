package com.dongji.market.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;

/**
 * 下载service
 * 
 * @author 131
 * 
 */
public class ADownloadService extends Service implements AConstDefine {

	private ExecutorService executorService;
	public static ADownloadApkList downloadingAPKList = new ADownloadApkList();
	public static ADownloadApkList updateAPKList = new ADownloadApkList();
//	public static ADownloadApkList ignoreAPKList = new ADownloadApkList();
	public static boolean isBackgroundRun = false;
	private ADownloadApkDBHelper aDownloadApkDBHelper;
	private ADownloadThread aDownloadThread = null;
	private MyDownloadReceiver myDownloadReceiver;
	private MyInstallReceiver myInstallReceiver;
	private Context context;
	private static boolean selfIsStart = false;
	private static boolean isOnlyWifi;

	@Override
	public void onCreate() {
		showLog("------------------------------onCreate");
		selfIsStart = true;
		context = getApplicationContext();

		executorService = Executors.newFixedThreadPool(3);
		aDownloadApkDBHelper = new ADownloadApkDBHelper(context);

		IntentFilter intentFilter1 = new IntentFilter();
		intentFilter1.addAction(BROADCAST_ACTION_DOWNLOAD);
		intentFilter1.addAction(BROADCAST_ACTION_UPDATE);
		intentFilter1.addAction(BROADCAST_SYS_ACTION_CONNECTCHANGE);
		intentFilter1.addAction(BROADCAST_ACTION_LIMITFLOWCHANGE);
		intentFilter1.addAction(BROADCAST_DOWNLOAD_ERROR);
		intentFilter1.addAction(BROADCAST_DEL_DOWNLOADED_APK);
		myDownloadReceiver = new MyDownloadReceiver();
		registerReceiver(myDownloadReceiver, intentFilter1);

		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction(BROADCAST_SYS_ACTION_APPINSTALL);
		intentFilter2.addDataScheme("package");
		myInstallReceiver = new MyInstallReceiver();
		registerReceiver(myInstallReceiver, intentFilter2);

		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {

		if (null == intent) {
			return;
		}

		if (intent.getBooleanExtra(FLAG_ISSTOPALLDWONLOAD, false)) {
			stopSelf();
		}
		Bundle apkItemBundle = intent.getBundleExtra(APKDOWNLOADITEM);
		if (null != apkItemBundle) {
			final ADownloadApkItem aDownloadApkItem = (ADownloadApkItem) apkItemBundle
					.getParcelable(APKDOWNLOADITEM);

			if (null != aDownloadApkItem.apkUrl) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
							// 这行一定要写在downloading方法前，因为downloading方法在列表里添加了数据，如果再加1数据就错了
							NetTool.setNotification(ADownloadService.this,
									FLAG_NOTIFICATION_DOWNLOAD,
									downloadingAPKList.apkList.size() + 1);
							downloading(aDownloadApkItem);

							Intent intent = new Intent(
									BROADCAST_ACTION_DOWNLOAD);
							intent.putExtra(BROADCAST_STARTDOWNLOAD,
									aDownloadApkItem.apkPackageName + "_"
											+ aDownloadApkItem.apkVersionCode);
							sendBroadcast(intent);
							showToast(aDownloadApkItem.apkName + "开始下载");
						} else if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREUPDATE) {
							NetTool.setNotification(
									context,
									FLAG_NOTIFICATION_UPDATEING,
									getUpdateCountByStatus(
											STATUS_OF_PREPAREUPDATE,
											STATUS_OF_UPDATEING,
											STATUS_OF_PAUSEUPDATE_BYHAND));
							NetTool.fillUpdateNotification(context);
							int tempFlag = 0;
							for (int i = 0; i < updateAPKList.apkList.size(); i++) {
								if (updateAPKList.apkList.get(i).apkPackageName
										.equals(aDownloadApkItem.apkPackageName)
										&& updateAPKList.apkList.get(i).apkVersionCode == aDownloadApkItem.apkVersionCode) {
									tempFlag = i;
									break;
								}
							}
							updateAPKList.apkList.get(tempFlag).apkStatus = STATUS_OF_PREPAREUPDATE;
							downloading(updateAPKList.apkList.get(tempFlag));
							Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
							intent.putExtra(BROADCAST_STARTUPDATE,
									aDownloadApkItem.apkPackageName + "_"
											+ aDownloadApkItem.apkVersionCode);
							sendBroadcast(intent);
							showToast(aDownloadApkItem.apkName + "开始更新");
						}
					}
				}).start();
			}
		} else if (intent.getBooleanExtra(FLAG_ISUNDONETASK, false)) {
			Intent broadIntent = new Intent(BROADCAST_ACTION_DOWNLOAD);
			broadIntent.putExtra(BROADCAST_LISTTASK, true);
			sendBroadcast(broadIntent);
			NetTool.fillDownloadingList(context);

			int apkStatus = -1;
			int downloadListSize = downloadingAPKList.apkList.size();

			for (int position = 0; position < downloadListSize; position++) {
				apkStatus = downloadingAPKList.apkList.get(position).apkStatus;
				if (apkStatus == STATUS_OF_PAUSE
						|| apkStatus == STATUS_OF_DOWNLOADING) {
					File file = new File(
							NetTool.DOWNLOADPATH
									+ downloadingAPKList.apkList.get(position).apkPackageName
									+ "_"
									+ downloadingAPKList.apkList.get(position).apkVersionCode
									+ ".apk.temp");
					if (!file.exists()
							&& downloadingAPKList.apkList.get(position).apkDownloadSize > 0) {
						downloadingAPKList.apkList.get(position).apkDownloadSize = 0;
					}
					downloadingAPKList.apkList.get(position).apkStatus = STATUS_OF_PREPAREDOWNLOAD;
					initThread(position, 1);
				} else if (apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
					initThread(position, 1);
				}
			}
			if (downloadListSize > 0) {
				NetTool.setNotification(context, FLAG_NOTIFICATION_DOWNLOAD,
						downloadListSize);
			}

			NetTool.fillUpdateingList(context);

			int updateListSize = updateAPKList.apkList.size();
			System.out.println("service size" + updateListSize);
			for (int position = 0; position < updateListSize; position++) {
				apkStatus = updateAPKList.apkList.get(position).apkStatus;
				System.out.println("adownloadservice......." + apkStatus);
				if (apkStatus == STATUS_OF_PAUSEUPDATE
						|| apkStatus == STATUS_OF_UPDATEING) {
					File file = new File(
							NetTool.DOWNLOADPATH
									+ updateAPKList.apkList.get(position).apkPackageName
									+ "_"
									+ updateAPKList.apkList.get(position).apkVersionCode
									+ ".apk.temp");
					if (!file.exists()
							&& updateAPKList.apkList.get(position).apkDownloadSize > 0) {
						updateAPKList.apkList.get(position).apkDownloadSize = 0;
					}
					updateAPKList.apkList.get(position).apkStatus = STATUS_OF_PREPAREUPDATE;
					initThread(position, 2);
				} else if (apkStatus == STATUS_OF_PREPAREUPDATE) {
					initThread(position, 2);
				}
			}
			NetTool.fillotherUpdate(context);

			NetTool.fillUpdateingNotifitcation(context);
		} else if (intent.getBooleanExtra(FLAG_ONEKEYUPDATE, false)) {
			Intent broadIntent = new Intent(BROADCAST_ACTION_UPDATE);
			broadIntent.putExtra(BROADCAST_ONEKEYUPDATE, true);
			sendBroadcast(broadIntent);
			int updateListSize = updateAPKList.apkList.size();
			for (int position = 0; position < updateListSize; position++) {
				if (updateAPKList.apkList.get(position).apkStatus == STATUS_OF_UPDATE) {
					updateAPKList.apkList.get(position).apkStatus = STATUS_OF_PREPAREUPDATE;
					downloading(updateAPKList.apkList.get(position));
				}
			}
			int count = getUpdateCountByStatus(STATUS_OF_PREPAREUPDATE,
					STATUS_OF_UPDATEING, STATUS_OF_PAUSEUPDATE_BYHAND);
			if (count > 0) {
				NetTool.setNotification(context, FLAG_NOTIFICATION_UPDATEING,
						count);
				NetTool.cancelNotification(context, FLAG_NOTIFICATION_UPDATE);
			}
		} else if (intent.getBooleanExtra(FLAG_CONTINUEPAUSETASK, false)) {
			int apkStatus = -1;
			int downloadListSize = downloadingAPKList.apkList.size();

			for (int position = 0; position < downloadListSize; position++) {
				apkStatus = downloadingAPKList.apkList.get(position).apkStatus;
				if (apkStatus == STATUS_OF_PAUSE) {
					downloadingAPKList.apkList.get(position).apkStatus = STATUS_OF_PREPAREDOWNLOAD;
					initThread(position, 1);
				} else if (apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
					initThread(position, 1);
				}
			}
			NetTool.fillDownloadingList(context);
			int updateListSize = updateAPKList.apkList.size();
			for (int position = 0; position < updateListSize; position++) {
				apkStatus = updateAPKList.apkList.get(position).apkStatus;
				if (apkStatus == STATUS_OF_PAUSEUPDATE) {
					updateAPKList.apkList.get(position).apkStatus = STATUS_OF_PREPAREUPDATE;
					initThread(position, 2);
				} else if (apkStatus == STATUS_OF_PREPAREUPDATE) {
					initThread(position, 2);
				}
			}
			NetTool.fillUpdateingNotifitcation(context);
		} else if (intent.getBooleanExtra(FLAG_CLOUDRESTORE, false)) {
			sendBroadcast(new Intent(BROADCAST_ACTION_CLOUDRESTORE));
			ArrayList<ApkItem> apkItems = intent
					.getParcelableArrayListExtra(FLAG_RESTORELIST);

			ADownloadApkItem aDownloadApkItem;
			for (int i = 0; i < apkItems.size(); i++) {
				int j = 0;
				for (; j < updateAPKList.apkList.size(); j++) {
					aDownloadApkItem = updateAPKList.apkList.get(j);
					if (aDownloadApkItem.apkPackageName
							.equals(apkItems.get(i).packageName)
							&& aDownloadApkItem.apkVersionCode == apkItems
									.get(i).versionCode) {
						aDownloadApkItem.apkStatus = STATUS_OF_PREPAREUPDATE;
						downloading(aDownloadApkItem);
						break;
					}
				}
				if (j == updateAPKList.apkList.size()) {
					downloading(new ADownloadApkItem(apkItems.get(i),
							STATUS_OF_PREPAREDOWNLOAD));
				}

			}
		}
		super.onStart(intent, startId);
	}

	private void initThread(int position, int type) {
		if (type == 1) {
			aDownloadThread = new ADownloadThread(
					downloadingAPKList.apkList.get(position), context);
		} else if (type == 2) {
			aDownloadThread = new ADownloadThread(
					updateAPKList.apkList.get(position), context);
		}
		executorService.execute(aDownloadThread);
	}

	private void initThread(ADownloadApkItem aDownloadApkItem) {
		aDownloadThread = new ADownloadThread(aDownloadApkItem, context);
		executorService.execute(aDownloadThread);
	}

	private void downloading(ADownloadApkItem aDownloadApkItem) {
		aDownloadApkDBHelper.insertToDownload(aDownloadApkItem);
		if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
			downloadingAPKList.apkList.add(aDownloadApkItem);
		}
		initThread(aDownloadApkItem);
	}

	@Override
	public void onDestroy() {
		showLog("onDestroy");
		selfIsStart = false;
		long _3gDownloadSize = 0;
		int downloadListSize = downloadingAPKList.apkList.size();
		int updateListSize = updateAPKList.apkList.size();

		for (int i = 0; i < downloadListSize; i++) {
			ADownloadApkItem item = downloadingAPKList.apkList.get(i);
			if (item.apkStatus == STATUS_OF_DOWNLOADING
					|| item.apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
				item.apkStatus = STATUS_OF_PAUSE;
			}
		}
		for (int i = 0; i < updateListSize; i++) {
			ADownloadApkItem item = updateAPKList.apkList.get(i);

			if (item.apkStatus == STATUS_OF_UPDATEING
					|| item.apkStatus == STATUS_OF_PREPAREUPDATE) {
				item.apkStatus = STATUS_OF_PAUSEUPDATE;
			}
		}
		_3gDownloadSize = get3GDownloadSize();
		writeToSharePreference(_3gDownloadSize);

		_3GDownloadSize = 0;
		showLog("service destroy saveFlow:" + saveFlow + ", maxFlow:" + maxFlow);

		aDownloadApkDBHelper.updateDownloadList(downloadingAPKList);

		aDownloadApkDBHelper.updateDownloadList(updateAPKList);

		if (myDownloadReceiver != null) {
			unregisterReceiver(myDownloadReceiver);
		}
		if (myInstallReceiver != null) {
			unregisterReceiver(myInstallReceiver);
		}
		NetTool.cancelNotification(ADownloadService.this,
				FLAG_NOTIFICATION_CANCELALL);

		List<Runnable> aa = executorService.shutdownNow();
		showErrorLog("===================================================  service exit "
				+ aa.size());
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		showLog("onBind");
		return null;
	}

	private class MyDownloadReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_ACTION_DOWNLOAD)) {
				int size = downloadingAPKList.apkList.size();
				String apkSaveName = intent
						.getStringExtra(BROADCAST_COMPLETEDOWNLOAD);
				if (null != apkSaveName) {
					Bundle bundle = intent.getExtras();
					int apkStatus = bundle.getInt("status", 0);
					onReceiveCompleteDownload(apkSaveName, apkStatus);
					if (apkStatus == STATUS_OF_DOWNLOADCOMPLETE) {
						NetTool.fillDownloadingNotification(context);
						NetTool.setNotification(context,
								FLAG_NOTIFICATION_WAITINGINSTALL,
								NetTool.getWaitInstallListCount(context));
					} else if (apkStatus == STATUS_OF_UPDATECOMPLETE) {
						NetTool.fillUpdateingNotifitcation(context);
						NetTool.setNotification(context,
								FLAG_NOTIFICATION_WAITINGINSTALL,
								NetTool.getWaitInstallListCount(context));
					}
					return;
				}
				apkSaveName = intent.getStringExtra(BROADCAST_PAUSEDOWNLOAD);
				if (null != apkSaveName) {
					onReceivePauseDownload(apkSaveName, size);
					return;
				}
				apkSaveName = intent.getStringExtra(BROADCAST_CONTINUEDOWNLOAD);
				if (null != apkSaveName) {
					onReceiveContinueDownload(apkSaveName, size);
					return;
				}
				apkSaveName = intent.getStringExtra(BROADCAST_CANCELDOWNLOAD);
				if (null != apkSaveName) {
					onReceiveCancelDownload(apkSaveName, size);
					return;
				}
				apkSaveName = intent.getStringExtra(BROADCAST_APKISDELETE);
				if (null != apkSaveName) {
					onDeleteErrorData(apkSaveName);
					return;
				}
				if (intent.getBooleanExtra(BROADCAST_APKLISTISNULL, false)) {
					onDeleteAllErrorData();
					return;
				}
			} else if (intent.getAction().equals(BROADCAST_ACTION_UPDATE)) {
				int size = updateAPKList.apkList.size();
				String apkSaveName = intent
						.getStringExtra(BROADCAST_PAUSEUPDATE);
				if (null != apkSaveName) {
					onReceivePauseUpdate(apkSaveName, size);
					return;
				}
				apkSaveName = intent.getStringExtra(BROADCAST_CONTINUEUPDATE);
				if (null != apkSaveName) {
					onReceiveContinueUpdate(apkSaveName, size);
					return;
				}
				apkSaveName = intent.getStringExtra(BROADCAST_CANCELUPDATE);
				if (null != apkSaveName) {
					onReceiveCancelUpdate(apkSaveName, size);
					return;
				}
				// apkSaveName = intent.getStringExtra(BROADCAST_IGNOREUPDATE);
				// if (null != apkSaveName) {
				// onReceiveIgnoreUpdate();
				// return;
				// }
				// apkSaveName = intent.getStringExtra(BROADCAST_CANCELIGNORE);
				// if (null != apkSaveName) {
				// onReceiveCancelIgnoreUpdate();
				// return;
				// }
			} else if (intent.getAction().equals(
					BROADCAST_SYS_ACTION_CONNECTCHANGE)) {
				ADownloadApkItem aDownloadApkItem;
				if (AndroidUtils.isWifiAvailable(context)) {
					for (int i = 0; i < downloadingAPKList.apkList.size(); i++) {
						aDownloadApkItem = downloadingAPKList.apkList.get(i);
						if (aDownloadApkItem.errCount > 0) {
							if (aDownloadApkItem.apkDownloadSize == 0) {
								aDownloadApkItem.apkStatus = STATUS_OF_PREPAREDOWNLOAD;
							} else {
								aDownloadApkItem.apkStatus = STATUS_OF_DOWNLOADING;
							}
							initThread(aDownloadApkItem);
							aDownloadApkItem.errCount = 0;
						}
					}
					for (int i = 0; i < updateAPKList.apkList.size(); i++) {
						aDownloadApkItem = updateAPKList.apkList.get(i);
						if (aDownloadApkItem.errCount > 0) {
							if (aDownloadApkItem.apkDownloadSize == 0) {
								aDownloadApkItem.apkStatus = STATUS_OF_PREPAREUPDATE;
							} else {
								aDownloadApkItem.apkStatus = STATUS_OF_UPDATEING;
							}
							initThread(aDownloadApkItem);
							aDownloadApkItem.errCount = 0;
						}
					}
				}
			} else if (BROADCAST_ACTION_LIMITFLOWCHANGE.equals(intent
					.getAction())) {
				Bundle bundle = intent.getExtras();
				long temp = bundle.getLong("limitFlow", -1);
				if (temp > -1) {
					maxFlow = temp;
					if (maxFlow > 0) {
						maxFlow *= (1024 * 1024);
					}
					saveFlow = 0;
					_3GDownloadSize = 0;
				}
				isOnlyWifi = bundle.getBoolean("isOnlyWifi");
				if (bundle.getBoolean(FLAG_BUNDLECONTINUEPAUSETASK)) {
					DJMarketUtils.prepareDownload(context,
							INT_CONTINUEPAUSETASK);
				}
			} else if (BROADCAST_DOWNLOAD_ERROR.equals(intent.getAction())) {
				String apkSaveName = intent
						.getStringExtra(FLAG_EXCEPTION_APKSAVENAME);
				int apkStatus = intent.getIntExtra(FLAG_EXCEPTION_STATUS, -1);
				onReceiveException(apkSaveName, apkStatus);
			} else if (intent.getAction().equals(BROADCAST_DEL_DOWNLOADED_APK)) {
				aDownloadApkDBHelper
						.deleteDownloadByApkStatus(AConstDefine.STATUS_OF_DOWNLOADCOMPLETE);
				aDownloadApkDBHelper
						.deleteDownloadByApkStatus(AConstDefine.STATUS_OF_UPDATECOMPLETE);
			}
		}
	}

	private void onReceiveException(String apkSaveName, int apkStatus) {
		ADownloadApkItem aDownloadApkItem;
		if (apkStatus == STATUS_OF_PREPAREDOWNLOAD
				|| apkStatus == STATUS_OF_DOWNLOADING) {
			for (int i = 0; i < downloadingAPKList.apkList.size(); i++) {
				aDownloadApkItem = downloadingAPKList.apkList.get(i);
				if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
						+ aDownloadApkItem.apkVersionCode)) {
					if (aDownloadApkItem.errCount < 3) {
						aDownloadApkItem.errCount += 1;
						initThread(aDownloadApkItem);
					} else {
						aDownloadApkItem.apkStatus = STATUS_OF_PAUSE_BYHAND;
						aDownloadApkDBHelper
								.updateADownloadApkItem(aDownloadApkItem);
						String msg = aDownloadApkItem.apkName
								+ context
										.getString(R.string.download_error_pause_msg);
						if (!TextUtils.isEmpty(aDownloadApkItem.apkName)) {
							AndroidUtils.showToast(context, msg);
						}
					}
					break;
				}
			}
		} else {
			for (int i = 0; i < updateAPKList.apkList.size(); i++) {
				aDownloadApkItem = updateAPKList.apkList.get(i);
				if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
						+ aDownloadApkItem.apkVersionCode)) {
					if (aDownloadApkItem.errCount < 3) {
						aDownloadApkItem.errCount += 1;
						initThread(aDownloadApkItem);
					} else {
						aDownloadApkItem.apkStatus = STATUS_OF_PAUSEUPDATE_BYHAND;
						aDownloadApkDBHelper
								.updateADownloadApkItem(aDownloadApkItem);
						String msg = aDownloadApkItem.apkName
								+ context
										.getString(R.string.update_error_pause_msg);
						if (!TextUtils.isEmpty(aDownloadApkItem.apkName)) {
							AndroidUtils.showToast(context, msg);
						}
					}
					break;
				}
			}
		}
	}

	private void onReceivePauseDownload(String apkSaveName, int size) {
		ADownloadApkItem aDownloadApkItem;
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = downloadingAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				showToast(aDownloadApkItem.apkName + "暂停下载");
				aDownloadApkDBHelper.updateADownloadApkItem(aDownloadApkItem);
				break;
			}
		}
	}

	private void onReceiveContinueDownload(String apkSaveName, int size) {
		System.out.println("onReceiveContinueDownload");
		ADownloadApkItem aDownloadApkItem;
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = downloadingAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				showToast(aDownloadApkItem.apkName + "继续下载");
				if (aDownloadApkItem.apkDownloadSize == 0) {
					aDownloadApkItem.apkStatus = STATUS_OF_PREPAREDOWNLOAD;
				} else {
					aDownloadApkItem.apkStatus = STATUS_OF_DOWNLOADING;
				}
				initThread(aDownloadApkItem);
				break;
			}
		}
	}

	private void onReceiveCancelDownload(String apkSaveName, int size) {
		ADownloadApkItem aDownloadApkItem;
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = downloadingAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				aDownloadApkItem.apkStatus = STATUS_OF_CANCEL;
				sendBroadcast(new Intent(BROADCAST_ACTION_TITLERECEIVER));
				showToast(aDownloadApkItem.apkName + "取消下载");
				// if (size - 1 > 0) {
				// NetTool.setNotification(ADownloadService.this,
				// FLAG_NOTIFICATION_DOWNLOAD, size - 1);
				// } else {
				// NetTool.cancelNotification(ADownloadService.this,
				// FLAG_NOTIFICATION_DOWNLOAD);
				// }
				downloadingAPKList.apkList.remove(i);
				aDownloadApkDBHelper.deleteDownloadByPAndV(
						aDownloadApkItem.apkPackageName,
						aDownloadApkItem.apkVersionCode);
				NetTool.deleteTempFileByApkSaveName(aDownloadApkItem.apkPackageName
						+ "_" + aDownloadApkItem.apkVersionCode);
				break;
			}
		}
	}

	private void onDeleteErrorData(String apkSaveName) {
		int size = 0;
		ADownloadApkItem aDownloadApkItem;
		int flag = 0;

		size = downloadingAPKList.apkList.size();
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = downloadingAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				flag = 1;
				showToast(aDownloadApkItem.apkName + "文件在它处被删除");
				downloadingAPKList.apkList.remove(aDownloadApkItem);
				if (size < 1) {
					if (isBackgroundRun) {
						stopSelf();
					}
				}
				break;
			}
		}

		size = updateAPKList.apkList.size();
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = updateAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				flag = 1;
				showToast(aDownloadApkItem.apkName + "文件在它处被删除");
				updateAPKList.apkList.remove(aDownloadApkItem);
				int notificationCout = ADownloadService.getUpdateCountByStatus(
						STATUS_OF_PREPAREUPDATE, STATUS_OF_UPDATEING,
						STATUS_OF_PAUSEUPDATE_BYHAND);
				if (notificationCout < 1) {
					if (isBackgroundRun) {
						stopSelf();
					}
				}
				break;
			}
		}

		if (flag == 0) {
			NetTool.fillWaitingInstallNotifitcation(context);
		}
	}

	private void onDeleteAllErrorData() {
		int size = 0;
		// ADownloadApkItem aDownloadApkItem;
		// for (int i = 0; i < downloadingAPKList.apkList.size(); i++) {
		// aDownloadApkItem = downloadingAPKList.apkList.get(i);
		// if (aDownloadApkItem.apkDownloadSize > 0) {
		// showToast(aDownloadApkItem.apkName + "文件在它处被删除");
		// downloadingAPKList.apkList.remove(aDownloadApkItem);
		// i--;
		// }
		// }
		size = downloadingAPKList.apkList.size();
		if (size > 0) {
			// NetTool.setNotification(ADownloadService.this,
			// FLAG_NOTIFICATION_DOWNLOAD, size);
		} else {
			// NetTool.cancelNotification(ADownloadService.this,
			// FLAG_NOTIFICATION_DOWNLOAD);
			if (isBackgroundRun) {
				stopSelf();
			}
		}

		// for (int i = 0; i < updateAPKList.apkList.size(); i++) {
		// aDownloadApkItem = updateAPKList.apkList.get(i);
		// if (aDownloadApkItem.apkDownloadSize > 0) {
		// System.out.println(aDownloadApkItem.apkName + "文件在它处被删除");
		// showToast(aDownloadApkItem.apkName + "文件在它处被删除");
		// updateAPKList.apkList.remove(aDownloadApkItem);
		// i--;
		// }
		// }
		int notificationCout = ADownloadService.getUpdateCountByStatus(
				STATUS_OF_PREPAREUPDATE, STATUS_OF_UPDATEING,
				STATUS_OF_PAUSEUPDATE_BYHAND);
		if (notificationCout > 0) {
			// NetTool.setNotification(ADownloadService.this,
			// FLAG_NOTIFICATION_UPDATEING, notificationCout);
		} else {
			// NetTool.cancelNotification(ADownloadService.this,
			// FLAG_NOTIFICATION_UPDATEING);
			if (isBackgroundRun) {
				stopSelf();
			}
		}
	}

	private void onReceivePauseUpdate(String apkSaveName, int size) {
		ADownloadApkItem aDownloadApkItem;
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = updateAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				showToast(aDownloadApkItem.apkName + "暂停更新");
				aDownloadApkDBHelper.updateADownloadApkItem(aDownloadApkItem);
				break;
			}
		}
	}

	private void onReceiveContinueUpdate(String apkSaveName, int size) {
		ADownloadApkItem aDownloadApkItem;
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = updateAPKList.apkList.get(i);
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				showToast(aDownloadApkItem.apkName + "继续更新");
				if (aDownloadApkItem.apkDownloadSize == 0) {
					aDownloadApkItem.apkStatus = STATUS_OF_PREPAREUPDATE;
				} else {
					aDownloadApkItem.apkStatus = STATUS_OF_UPDATEING;
				}
				initThread(aDownloadApkItem);
				break;
			}
		}
	}

	private void onReceiveCancelUpdate(String apkSaveName, int size) {
		if (null == apkSaveName || apkSaveName.trim().equals("")) {
			return;
		}
		ADownloadApkItem aDownloadApkItem;
		for (int i = 0; i < size; i++) {
			aDownloadApkItem = updateAPKList.apkList.get(i);
			// aDownloadApkItem.apkStatus = STATUS_OF_UPDATE;
			if (apkSaveName.equals(aDownloadApkItem.apkPackageName + "_"
					+ aDownloadApkItem.apkVersionCode)) {
				aDownloadApkItem.apkStatus = STATUS_OF_UPDATE;
				showToast(aDownloadApkItem.apkName + "取消更新");
				NetTool.fillUpdateingNotifitcation(context);
				NetTool.setNotification(context, FLAG_NOTIFICATION_UPDATE,
						ADownloadService
								.getUpdateCountByStatus(STATUS_OF_UPDATE));

				NetTool.deleteTempFileByApkSaveName(apkSaveName);
				NetTool.deleteFileByApkSaveName(apkSaveName);
				String[] tempString = apkSaveName.split("_");
				if (tempString.length == 2) {
					aDownloadApkDBHelper.deleteDownloadByPAndV(tempString[0],
							Integer.valueOf(tempString[1]));
				}
				break;
			}
		}
	}

	private void onReceiveCompleteDownload(final String apkSaveName, int status) {

		if (null == apkSaveName || apkSaveName.trim().equals("")) {
			return;
		}
		String[] tempString = apkSaveName.split("_");
		if (tempString.length == 2) {
			ADownloadApkItem aDownloadApkItem;

			if (status == STATUS_OF_DOWNLOADCOMPLETE) {
				int downloadSize = downloadingAPKList.apkList.size();
				for (int i = 0; i < downloadSize; i++) {
					aDownloadApkItem = downloadingAPKList.apkList.get(i);
					if (aDownloadApkItem.apkPackageName.equals(tempString[0])
							&& aDownloadApkItem.apkVersionCode == Integer
									.valueOf(tempString[1])) {
						aDownloadApkDBHelper.updateWhileDownloadComplete(
								tempString[0], Integer.valueOf(tempString[1]),
								aDownloadApkItem.apkDownloadSize,
								STATUS_OF_DOWNLOADCOMPLETE);

						if (DJMarketUtils.isDefaultInstall(context)) {
							if (AndroidUtils.isRoot()) {
								AndroidUtils
										.rootInstallApp(NetTool.DOWNLOADPATH
												+ apkSaveName + ".apk");
								showToast(aDownloadApkItem.apkName + "下载完成");
							} else {
								new Thread(new Runnable() {

									@Override
									public void run() {
										NetTool.installApp(context, apkSaveName);
									}
								}).start();
							}

						} else if (!isBackgroundRun) {
							System.out.println("名字" + aDownloadApkItem.apkName);
							new Thread(new Runnable() {

								@Override
								public void run() {
									NetTool.installApp(context, apkSaveName);
								}
							}).start();

						}
						downloadingAPKList.apkList.remove(i);
						break;
					}
				}
			} else if (status == STATUS_OF_UPDATECOMPLETE) {
				int updateSize = updateAPKList.apkList.size();
				for (int i = 0; i < updateSize; i++) {
					aDownloadApkItem = updateAPKList.apkList.get(i);
					if (apkSaveName.equals(aDownloadApkItem.apkPackageName
							+ "_" + aDownloadApkItem.apkVersionCode)) {
						aDownloadApkDBHelper.updateWhileDownloadComplete(
								tempString[0], Integer.valueOf(tempString[1]),
								aDownloadApkItem.apkDownloadSize,
								STATUS_OF_UPDATECOMPLETE);

						if (DJMarketUtils.isDefaultInstall(context)) {
							if (AndroidUtils.isRoot()) {
								AndroidUtils
										.rootInstallApp(NetTool.DOWNLOADPATH
												+ apkSaveName + ".apk");
								showToast(aDownloadApkItem.apkName + "下载完成");
							} else {
								new Thread(new Runnable() {

									@Override
									public void run() {
										NetTool.installApp(context, apkSaveName);
									}
								}).start();
							}

						} else if (!isBackgroundRun) {
							System.out.println("名字" + aDownloadApkItem.apkName);
							new Thread(new Runnable() {

								@Override
								public void run() {
									NetTool.installApp(context, apkSaveName);
								}
							}).start();

						}

						updateAPKList.apkList.remove(i);

						break;
					}
				}
			}
			int notificationCout = ADownloadService.getUpdateCountByStatus(
					STATUS_OF_PREPAREUPDATE, STATUS_OF_UPDATEING,
					STATUS_OF_PAUSEUPDATE_BYHAND);
			if ((ADownloadService.downloadingAPKList.apkList.size() < 1)
					&& (notificationCout < 1)) {
				if (isBackgroundRun) {
					stopSelf();
				}
			}
		}
	}

	private void writeToSharePreference(long size) {
		SharedPreferences mSharedPreferences = getSharedPreferences(
				DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putLong(SHARE_DOWNLOADSIZE, size);
		mEditor.commit();
	}

	public static int getDownloadcountByStatus(int status) {
		int tempCount = 0;
		for (int i = 0; i < downloadingAPKList.apkList.size(); i++) {
			if (downloadingAPKList.apkList.get(i).apkStatus == status) {
				tempCount++;
			}
		}
		return tempCount;
	}

	public static int getDownloadcountByStatus(int status1, int status2) {
		int tempCount = 0;
		int tempStatus;
		for (int i = 0; i < downloadingAPKList.apkList.size(); i++) {
			tempStatus = downloadingAPKList.apkList.get(i).apkStatus;
			if (tempStatus == status1 || tempStatus == status2) {
				tempCount++;
			}
		}
		return tempCount;
	}

	public static int getDownloadcountByStatus(int status1, int status2,
			int status3) {
		int tempCount = 0;
		int tempStatus;
		for (int i = 0; i < downloadingAPKList.apkList.size(); i++) {
			tempStatus = downloadingAPKList.apkList.get(i).apkStatus;
			if (tempStatus == status1 || tempStatus == status2
					|| tempStatus == status3) {
				tempCount++;
			}
		}
		return tempCount;
	}

	public static int getUpdateCountByStatus(int status) {
		int tempCount = 0;
		for (int i = 0; i < updateAPKList.apkList.size(); i++) {
			if (updateAPKList.apkList.get(i).apkStatus == status) {
				tempCount++;
			}
		}
		return tempCount;
	}

	public static int getUpdateCountByStatus(int status1, int status2) {
		int tempCount = 0;
		int tempStatus;
		for (int i = 0; i < updateAPKList.apkList.size(); i++) {
			tempStatus = updateAPKList.apkList.get(i).apkStatus;
			if (tempStatus == status1 || tempStatus == status2) {
				tempCount++;
			}
		}
		return tempCount;
	}

	public static int getUpdateCountByStatus(int status1, int status2,
			int status3) {
		int tempCount = 0;
		int tempStatus;
		for (int i = 0; i < updateAPKList.apkList.size(); i++) {
			tempStatus = updateAPKList.apkList.get(i).apkStatus;
			if (tempStatus == status1 || tempStatus == status2
					|| tempStatus == status3) {
				tempCount++;
			}
		}
		return tempCount;
	}

	private class MyInstallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_SYS_ACTION_APPINSTALL)) {
				String packageName = intent.getDataString();
				packageName = DJMarketUtils.convertPackageName(packageName);
				if (!isBackgroundRun) {
					String appName = aDownloadApkDBHelper
							.selectApkNameByPackageName(packageName);
					if (!TextUtils.isEmpty(appName)) {
						AndroidUtils.showToast(context, appName
								+ getString(R.string.install_success_msg));
					}
				}
				if (DJMarketUtils.isAutoDelPkg(context)) {
					NetTool.deleteFileByPackageName(context, packageName);
				}
				aDownloadApkDBHelper
						.deleteDownloadByApkPackageName(packageName);
			}
		}
	}

	private static void showLog(String msg) {
		Log.i("ADownloadService", msg);
	}

	private void showErrorLog(String msg) {
		Log.e("ADownloadService", msg);
	}

	private void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private static long _3GDownloadSize;
	private static long maxFlow;
	private static long saveFlow;

	public synchronized static boolean set3GDownloadSize(int value) {
		if (value > 0) {
			_3GDownloadSize += value;
		}
		System.out.println("_3GDownloadSize:" + _3GDownloadSize + ", saveFlow:"
				+ saveFlow + ", maxFlow:" + maxFlow);
		return canUse3GDownload();
	}

	/**
	 * 是否还有剩余3G限制流量
	 * 
	 * @return
	 */
	public static boolean canUse3GDownload() {
		if (!isOnlyWifi && _3GDownloadSize + saveFlow >= maxFlow) {
			System.out.println("_3GDownloadSize:" + _3GDownloadSize
					+ ", saveFlow:" + saveFlow + ", maxFlow:" + maxFlow);
			return false;
		}
		return true;
	}

	/**
	 * 获取使用蜂窝下载所消耗的总流量
	 * 
	 * @return
	 */
	public static long get3GDownloadSize() {
		return _3GDownloadSize + saveFlow;
	}

	/**
	 * 判断当前 service 是否启动
	 * 
	 * @return
	 */
	public static boolean isSelfStart() {
		return selfIsStart;
	}

	/**
	 * 改变设置修改中的各项参数
	 */
	public static void changeDownloadParameter(long maxFlow,
			boolean isOnlyWifi, boolean downloadChange) {
		if (maxFlow > 0) {
			maxFlow *= (1024 * 1024);
		}
		ADownloadService.maxFlow = maxFlow;
		ADownloadService.isOnlyWifi = isOnlyWifi;
		if (downloadChange) {
			saveFlow = 0;
			_3GDownloadSize = 0;
		}
	}

	/**
	 * 当 service 启动时需要初始化蜂窝下载的各项参数
	 */
	public static void init3GDownloadParams(Context context) {
		maxFlow = DJMarketUtils.getMaxFlow(context);

		isOnlyWifi = DJMarketUtils.isOnlyWifi(context);
		if (maxFlow > 0) {
			maxFlow *= (1024 * 1024);
		}
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		saveFlow = pref.getLong(AConstDefine.SHARE_DOWNLOADSIZE, 0);
		_3GDownloadSize = 0;

		showLog("init maxFlow:" + maxFlow + ", saveFlow:" + saveFlow
				+ ", isOnlyWifi:" + isOnlyWifi);
	}

}
