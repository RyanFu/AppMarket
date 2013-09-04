package com.dongji.market.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.dongji.market.R;
import com.dongji.market.activity.SoftwareManageActivity;
import com.dongji.market.activity.BackupOrRestoreActivity.OnProgressChangeListener;
import com.dongji.market.application.AppMarket;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.BackupItemInfo;
import com.dongji.market.pojo.InstalledAppInfo;

public class NetTool implements AConstDefine {
	public static String DOWNLOADPATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/.dongji/dongjiMarket/cache/apk/";
	public static String BACKUPPATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/.dongji/dongjiMarket/backup/";

	/**
	 * 检查网络连接情况
	 * 
	 * @param context
	 * @return 1: 无网络 2: Wifi 3: GPRS 4: 其他网络
	 */
	public static int getNetWorkType(Context context) {
		// showLog("getNetWorkType");
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (null == networkInfo || !networkInfo.isAvailable()) {
			return 1;
		}
		if (State.CONNECTED == connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState()) {
			return 2;
		}
		if (State.CONNECTED == connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState()) {
			return 3;
		}
		return 4;
	}

	public static void setSharedPreferences(Context context, String key,
			int value) {
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void setSharedPreferences(Context context, String key,
			String value) {
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void setSharedPreferences(Context context, String key,
			boolean value) {
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static int getSharedPreferences(Context context, String key,
			int value) {
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		return pref.getInt(key, value);
	}

	public static String getSharedPreferences(Context context, String key,
			String value) {
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		return pref.getString(key, value);
	}

	public static boolean getSharedPreferences(Context context, String key,
			boolean value) {
		SharedPreferences pref = context.getSharedPreferences(
				AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		return pref.getBoolean(key, value);
	}

	// public static int getFileTotalSize(String urlString) throws IOException {
	// URL url = new URL(urlString);
	// HttpURLConnection httpURLConnection = (HttpURLConnection) url
	// .openConnection();
	// httpURLConnection.setConnectTimeout(5000);
	// httpURLConnection.setRequestMethod("GET");
	// int size = httpURLConnection.getContentLength();
	// httpURLConnection.disconnect();
	// return size;
	//
	// }

	/**
	 * 
	 * @param rootPath
	 *            后面不要带"/"
	 * @param name
	 * @param suffix
	 * @return
	 */
	public static String getAbsolutePath(String name, String suffix) {
		if (createPath(DOWNLOADPATH)) {
			return DOWNLOADPATH + name + "." + suffix;
		}
		// return "";
		return DOWNLOADPATH + name + "." + suffix;
	}




	// private static void initUpdateNotification(Context context) {
	// int updatecount = ADownloadService
	// .getUpdateCountByStatus(STATUS_OF_UPDATE);
	// if (updatecount > 0) {
	// updateNotification(context);
	// }
	//
	// }

	public static boolean createPath(String path) {
		File newfolder = new File(path);
		if (!newfolder.exists()) {
			return newfolder.mkdirs();
		}
		return true;
		// String[] paths=path.split("/");
		// String tempPath;
		// File newfolder;
		// for(int i=0;i<paths.length;i++){
		// tempPath+="/"+paths[i];
		// newfolder=new File(tempPath);
		// if(!newfolder.exists()){
		// newfolder.m
		// }
		// }
		//
		// if(!(newfolder.exists())&&!(newfolder.isDirectory()))
		// {
		// boolean bool=newfolder.mkdirs();
		// if(bool)
		// BrowseFiles(currentDirectory);
		// }

	}

	// public static void fillPauseDataToView(){
	// ADownloadService
	// }

	public static void deleteLastSuffix(String filePath) {
		System.out.println("deleteSuffix");
		File file = new File(filePath);
		String newFilePath = filePath.substring(0, filePath.lastIndexOf("."));
		file.renameTo(new File(newFilePath));
	}

	public static String formatString(double value) {
		DecimalFormat df = new DecimalFormat("##0.00");
		return df.format(value);
	}

	public static void deleteFileByApkSaveName(String apkSaveName) {
		File file = new File(DOWNLOADPATH + apkSaveName + ".apk");
		file.delete();
	}

	public static void deleteFileByPackageName(Context context,
			String packageName) {
		File directory = new File(DOWNLOADPATH);
		int versionCode = DJMarketUtils
				.getInstalledAppVersionCodeByPackageName(context, packageName);
		if (versionCode != -1) {
			if (directory.exists()) {
				File[] files = directory.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().equals(
							packageName + "_" + versionCode + ".apk")) {
						files[i].delete();
						return;
					}
				}
			}
		}
	}

	public static void deleteTempFileByApkSaveName(String apkSaveName) {
		File file = new File(DOWNLOADPATH + apkSaveName + ".apk.temp");
		file.delete();
	}

	public static void deleteTempFile(Context context) {
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
				context);

		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".temp")) {
					fileName = fileName.substring(0, fileName.length() - 5);
					String[] tempString = fileName.split("_");
					if (tempString.length == 2) {
						try {
							if (!aDownloadApkDBHelper.selectApkIsExist(
									tempString[0],
									Integer.valueOf(tempString[1]))) {
								files[i].delete();
							}
						} catch (NumberFormatException e) {

						}
					}
				}
			}
		}
	}

	// /**
	// * 删除数据库中没有记录的APK
	// * @param context
	// */
	// public static void deleteNoSaveApk(Context context) {
	// ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
	// context);
	// File directory = new File(DOWNLOADPATH);
	// if (directory.exists()) {
	// File[] files = directory.listFiles();
	// int apkId = 0;
	// for (int i = 0; i < files.length; i++) {
	// String[] tempString = files[i].getName().split("\\.");
	// apkId = Integer.valueOf(tempString[0]);
	// if (tempString.length == 2) {
	// if (!aDownloadApkDBHelper.selectApkIdIsExist(apkId)) {
	// files[i].delete();
	// }
	// }
	// }
	// }
	// }

	public static boolean checkBackupApkIsExist(String apkName) {
		boolean isExist = false;
		File directory = new File(BACKUPPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				String tempName = files[i].getName();
				tempName = tempName.substring(0, tempName.length() - 4);
				if (tempName.equals(apkName)) {
					isExist = true;
					break;
				}
			}
		}
		return isExist;

	}

	public static void deleteNoBackupApk(List<String> apkNames) {
		boolean isExist;
		File directory = new File(BACKUPPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				isExist = false;
				for (int j = 0; j < apkNames.size(); j++) {
					String tempName = files[i].getName();
					tempName = tempName.substring(0, tempName.length() - 4);
					if (tempName.equals(apkNames.get(j))) {
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					files[i].delete();
				}

			}
		}
	}

	public static boolean checkApkIsExist(String apkSaveName) {
		boolean isExist = false;
		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".apk")) {
					fileName = fileName.substring(0, fileName.length() - 4);
					if (fileName.equals(apkSaveName)) {
						isExist = true;
						break;
					}
				}
			}
		}
		return isExist;

	}

	public static List<AErrorApk> checkErrorData(Context context) {
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
				context);
		List<AErrorApk> aErrorApks = aDownloadApkDBHelper
				.selectAllDownloadApkToError();

		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			int i, j;
			for (i = 0; i < aErrorApks.size(); i++) {
				File[] files = directory.listFiles();
				for (j = 0; j < files.length; j++) {
					String fileName = files[j].getName();
					if (fileName.endsWith(".apk")) {
						fileName = fileName.substring(0, fileName.length() - 4);
					} else if (fileName.endsWith(".temp")) {
						fileName = fileName.substring(0, fileName.length() - 9);
					}
					if (fileName.equals(aErrorApks.get(i).apkPackageName + "_"
							+ aErrorApks.get(i).apkVersionCode)) {
						aErrorApks.remove(i);
						i--;
						break;
					}
				}
			}
		} else {
			aErrorApks = new ArrayList<AErrorApk>();
			AErrorApk aErrorApk = new AErrorApk();
			aErrorApk.apkPackageName = "null";
			aErrorApks.add(aErrorApk);
		}
		return aErrorApks;

	}

	public static ADownloadApkList getWaitInstallList(Context context) {
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
				context);

		ADownloadApkList aDownloadApkList = aDownloadApkDBHelper
				.selectApkByStatus(new int[] { STATUS_OF_DOWNLOADCOMPLETE,
						STATUS_OF_UPDATECOMPLETE });
		if (aDownloadApkList.apkList.size() > 0) {
			for (int i = 0; i < aDownloadApkList.apkList.size(); i++) {
				File file = new File(DOWNLOADPATH
						+ aDownloadApkList.apkList.get(i).apkPackageName + "_"
						+ aDownloadApkList.apkList.get(i).apkVersionCode
						+ ".apk");
				if (!file.exists()) {
					aDownloadApkList.apkList.remove(i--);
				}
			}
		}
		/*
		 * File directory = new File(DOWNLOADPATH); if (directory.exists()) {
		 * File[] files = directory.listFiles(); int apkId = 0; for (int i = 0;
		 * i < files.length; i++) { String[] tempString =
		 * files[i].getName().split("\\."); apkId =
		 * Integer.valueOf(tempString[0]); if (tempString.length == 2) { if
		 * (aDownloadApkDBHelper.selectApkIdIsExist(apkId)) {
		 * aDownloadApkList.apkList.add(aDownloadApkDBHelper
		 * .selectApkByApkId(apkId)); } } } }
		 * System.out.println("=============install size:" +
		 * aDownloadApkList.apkList.size());
		 */
		return aDownloadApkList;
	}

	public static ADownloadApkList getIgnoreList(Context context) {
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
				context);
		ADownloadApkList ignoreList = aDownloadApkDBHelper.selectAllIgnoreApp();
		System.out.println("select size............"
				+ ignoreList.ignoreAppList.size());
		return ignoreList;
	}

	public static int getWaitInstallListCount(Context context) {
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
				context);
		int count = 0;
		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();

			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".apk")) {
					fileName = fileName.substring(0, fileName.length() - 4);
					String[] tempString = fileName.split("_");
					if (tempString.length == 2) {
						if (TextUtils.isDigitsOnly(tempString[1])) {
							if (aDownloadApkDBHelper.selectApkIsExist(
									tempString[0],
									Integer.valueOf(tempString[1]))) {
								count++;
							}
						}
					}
				}
			}
		}
		return count;
	}

	public static boolean checkIsDownload(Context context, String apkSaveName) {
		File directory = new File(DOWNLOADPATH);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(apkSaveName + ".apk")) {
					return true;
				}
			}
		}
		return false;

	}

	public static void installApp(Context context, String apkSaveName) {
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setDataAndType(
				Uri.fromFile(new File(NetTool.DOWNLOADPATH + apkSaveName
						+ ".apk")), "application/vnd.android.package-archive");
		context.startActivity(installIntent);
	}

	public static void installBackupApp(Context context, String apkName) {
		if (AndroidUtils.isRoot()) {
			AndroidUtils.rootInstallApp(NetTool.BACKUPPATH + apkName + ".apk");
		} else {
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			installIntent.setDataAndType(Uri.fromFile(new File(
					NetTool.BACKUPPATH + apkName + ".apk")),
					"application/vnd.android.package-archive");
			context.startActivity(installIntent);
		}
	}

	// private static long lastClickTime;

	public static boolean isFastDoubleClick(long lastClickTime) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 800) {
			return true;
		}
		// lastClickTime = time;
		return false;
	}

	public static void setNotification(Context context, int id, int count) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notice = new Notification();
		notice.icon = R.drawable.icon;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.layout_notification);
		int flag = 0;
		switch (id) {
		case FLAG_NOTIFICATION_DOWNLOAD:
			notice.flags = Notification.FLAG_AUTO_CANCEL;
			remoteViews.setTextViewText(R.id.tvNotificationTitle,
					context.getString(R.string.notification_tip_download));
			remoteViews
					.setTextViewText(
							R.id.tvNotificationText,
							count
									+ context
											.getString(R.string.notification_tip_clickdownloading));
			flag = 1;
			break;
		case FLAG_NOTIFICATION_UPDATE:
			if (DJMarketUtils.isUpdatePrompt(context)) {
				notice.flags = Notification.FLAG_AUTO_CANCEL;
				remoteViews.setTextViewText(R.id.tvNotificationTitle,
						context.getString(R.string.notification_tip_update));
				remoteViews
						.setTextViewText(
								R.id.tvNotificationText,
								count
										+ context
												.getString(R.string.notification_tip_clickupdate));
				flag = 1;
			}
			break;
		case FLAG_NOTIFICATION_UPDATEING:
			notice.flags = Notification.FLAG_AUTO_CANCEL;
			remoteViews.setTextViewText(R.id.tvNotificationTitle,
					context.getString(R.string.notification_tip_update));
			remoteViews
					.setTextViewText(
							R.id.tvNotificationText,
							count
									+ context
											.getString(R.string.notification_tip_clickupdateing));
			flag = 1;
			break;
		case FLAG_NOTIFICATION_WAITINGINSTALL:
			notice.flags = Notification.FLAG_AUTO_CANCEL;
			remoteViews.setTextViewText(R.id.tvNotificationTitle,
					context.getString(R.string.notification_tip_download));
			remoteViews
					.setTextViewText(
							R.id.tvNotificationText,
							count
									+ context
											.getString(R.string.notification_tip_clickdownloaded));
			flag = 1;
			break;
		}

		if (flag == 1) {
			notice.contentView = remoteViews;
			notice.when = System.currentTimeMillis();
			Intent intent = new Intent(context, SoftwareManageActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			PendingIntent pendingIntent = PendingIntent.getActivity(context,
					id, intent, 0);
			notice.contentIntent = pendingIntent;

			notificationManager.notify(id, notice);
		}
	}

	public static void fillWaitingInstallNotifitcation(Context context) {
		int count = NetTool.getWaitInstallListCount(context);
		if (count > 0) {
			NetTool.setNotification(context, FLAG_NOTIFICATION_WAITINGINSTALL,
					count);
		} else {
			NetTool.cancelNotification(context,
					FLAG_NOTIFICATION_WAITINGINSTALL);
		}
	}

	// public static void updateNotification(Context context) {
	// NotificationManager notificationManager = (NotificationManager) context
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	// Notification notice = new Notification();
	// notice.icon = R.drawable.icon;
	// RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
	// R.layout.layout_notification);
	//
	// if (ADownloadService.downloadingAPKList.apkList.size() > 0) {
	// notice.flags = Notification.FLAG_AUTO_CANCEL;
	// remoteViews.setTextViewText(R.id.tvNotificationTitle,
	// context.getString(R.string.notification_tip_download));
	// remoteViews
	// .setTextViewText(
	// R.id.tvNotificationText,
	// ADownloadService.downloadingAPKList.apkList.size()
	// + context
	// .getString(R.string.notification_tip_clickdownloading));
	// fillNotification(context, FLAG_NOTIFICATION_DOWNLOAD, notice,
	// remoteViews, notificationManager);
	// } else {
	// cancelNotification(context, FLAG_NOTIFICATION_DOWNLOAD);
	// }
	//
	// if (DJMarketUtils.isUpdatePrompt(context)
	// && ADownloadService.getUpdateCountByStatus(STATUS_OF_UPDATE) > 0) {
	// notice.flags = Notification.FLAG_AUTO_CANCEL;
	// remoteViews.setTextViewText(R.id.tvNotificationTitle,
	// context.getString(R.string.notification_tip_update));
	// remoteViews
	// .setTextViewText(
	// R.id.tvNotificationText,
	// ADownloadService
	// .getUpdateCountByStatus(STATUS_OF_UPDATE)
	// + context
	// .getString(R.string.notification_tip_clickupdate));
	// fillNotification(context, FLAG_NOTIFICATION_UPDATE, notice,
	// remoteViews, notificationManager);
	// } else {
	// cancelNotification(context, FLAG_NOTIFICATION_UPDATE);
	// }
	//
	// if (ADownloadService.getUpdateCountByStatus(STATUS_OF_PREPAREUPDATE,
	// STATUS_OF_UPDATEING, STATUS_OF_PAUSEUPDATE_BYHAND) > 0) {
	// notice.flags = Notification.FLAG_AUTO_CANCEL;
	// remoteViews.setTextViewText(R.id.tvNotificationTitle,
	// context.getString(R.string.notification_tip_update));
	// remoteViews
	// .setTextViewText(
	// R.id.tvNotificationText,
	// ADownloadService.getUpdateCountByStatus(
	// STATUS_OF_PREPAREUPDATE,
	// STATUS_OF_UPDATEING,
	// STATUS_OF_PAUSEUPDATE_BYHAND)
	// + context
	// .getString(R.string.notification_tip_clickupdateing));
	// fillNotification(context, FLAG_NOTIFICATION_UPDATEING, notice,
	// remoteViews, notificationManager);
	// } else {
	// cancelNotification(context, FLAG_NOTIFICATION_UPDATEING);
	// }
	//
	// if (getWaitInstallListCount(context) > 0) {
	// notice.flags = Notification.FLAG_AUTO_CANCEL;
	// remoteViews.setTextViewText(R.id.tvNotificationTitle,
	// context.getString(R.string.notification_tip_download));
	// remoteViews
	// .setTextViewText(
	// R.id.tvNotificationText,
	// getWaitInstallListCount(context)
	// + context
	// .getString(R.string.notification_tip_clickdownloaded));
	// fillNotification(context, FLAG_NOTIFICATION_WAITINGINSTALL, notice,
	// remoteViews, notificationManager);
	// } else {
	// cancelNotification(context, FLAG_NOTIFICATION_WAITINGINSTALL);
	// }
	//
	// }
	//
	// private static void fillNotification(Context context, int id,
	// Notification notice, RemoteViews remoteViews,
	// NotificationManager notificationManager) {
	// notice.contentView = remoteViews;
	// notice.when = System.currentTimeMillis();
	// Intent intent = new Intent(context, SoftwareManageActivity.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// PendingIntent pendingIntent = PendingIntent.getActivity(context, id,
	// intent, 0);
	// notice.contentIntent = pendingIntent;
	// notificationManager.notify(id, notice);
	// }

	public static void cancelNotification(Context context, int id) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (id == FLAG_NOTIFICATION_CANCELALL) {
			notificationManager.cancelAll();
		} else {
			notificationManager.cancel(id);
		}
	}

//	public static void startServiceToDownload(Context context,
//			ADownloadApkItem aDownloadApkItem) {
//		Intent serviceIntent = new Intent();
//		Bundle bundle = new Bundle();
//		bundle.putParcelable(APKDOWNLOADITEM, aDownloadApkItem);
//		serviceIntent.putExtra(APKDOWNLOADITEM, bundle);
//		serviceIntent.setClass(context, ADownloadService.class);
//		context.startService(serviceIntent);
//	}

	// public static void startServiceToCloudRestore(Context context,
	// ArrayList<ApkItem> apkItems) {
	//
	// Intent serviceIntent = new Intent();
	// Bundle bundle = new Bundle();
	// bundle.putInt(FLAG_CLOUDRESTORE, INT_CLOUDRESTORE);
	//
	// bundle.putParcelableArrayList(FLAG_RESTORELIST, apkItems);
	// serviceIntent.putExtras(bundle);
	// serviceIntent.setClass(context, ADownloadService.class);
	// context.startService(serviceIntent);
	// }

	public static int startToLocalBackup(Context context,
			List<BackupItemInfo> backupItemInfos,
			OnProgressChangeListener mListener) {
		File fromFile, toFile;
		String fromFileName;
		int count = 0;
		for (int i = 0; i < backupItemInfos.size(); i++) {
			fromFileName = backupItemInfos.get(i).appName;
			fromFile = new File(fromFileName);
			showLog(BACKUPPATH
					+ fromFileName.substring(10, fromFileName.length() - 6)
					+ "_" + backupItemInfos.get(i).appVerCode + ".apk");
			toFile = new File(BACKUPPATH
					+ fromFileName.substring(10, fromFileName.length() - 6)
					+ "_" + backupItemInfos.get(i).appVerCode + ".apk");
			if (copyfile(fromFile, toFile) == 1) {
				count++;
			}
			mListener.onProgressChange(fromFile.length());
		}
		return count;
	}

	// private static void showMyToast(Context context, int msgId) {
	// context.
	// Toast.makeText(context.getApplicationContext(), msgId,
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// private static void showMyToast(Context context, String msg) {
	// Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	// }

	// public static void startToLocalBackup(String[] fromFileNames){
	// File fromFile,toFile;
	// for(int i=0;i<fromFileNames.length;i++){
	// fromFile=new File(fromFileNames[i]);
	// toFile=new
	// File(BACKUPPATH+fromFileNames[i].substring(10,fromFileNames[i].length()-6)+"_"++".apk");
	// copyfile(fromFile, toFile);
	// }
	// }

	public static int copyfile(File fromFile, File toFile) {
		int i = 0;
		if (!fromFile.exists()) {
			return i;
		}
		if (!fromFile.isFile()) {
			return i;
		}
		if (!fromFile.canRead()) {
			return i;
		}
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists()) {
			toFile.delete();
		}
		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c); // 将内容写到新文件当中
			}
			fosfrom.close();
			fosto.close();
			i = 1;
		} catch (Exception ex) {
			showErrorLog(ex.toString());
		}
		return i;
	}

	// public static List<InstalledAppInfo> getInstallAppInfo(Context context) {
	//
	// PackageManager pm = context.getPackageManager();
	// List<PackageInfo> packages = pm.getInstalledPackages(0);
	// List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
	// for (PackageInfo pInfo : packages) {
	//
	// InstalledAppInfo installedAppInfo = new InstalledAppInfo();
	// ApplicationInfo info = pInfo.applicationInfo;
	// System.out.println("...install..."+info.loadLabel(pm));
	// for (int i = 0; i < ADownloadService.updateAPKList.apkList.size(); i++) {
	// if (info.packageName
	// .equals(ADownloadService.updateAPKList.apkList.get(i).apkPackageName)) {
	// installedAppInfo.setAppInfo(info);
	// installedAppInfo.setIcon(info.loadIcon(pm));
	// installedAppInfo.setName(info.loadLabel(pm) + "");
	// installedAppInfo.setVersion(pInfo.versionName);
	// installedAppInfo.setVersionCode(pInfo.versionCode);
	// installedAppInfo.setPkgName(info.packageName);
	// // map.put("uninstall", R.drawable.uninstall);
	// // 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
	// // 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
	// String dir = info.publicSourceDir;
	// int size = Integer.valueOf((int) new File(dir).length());
	// installedAppInfo.setSize(sizeFormat(size));
	// list.add(installedAppInfo);
	// }
	// }
	// }
	//
	// return list;
	// }

	public static List<InstalledAppInfo> getAllInstallAppInfo(Context context) {

		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
		for (PackageInfo pInfo : packages) {

			InstalledAppInfo installedAppInfo = new InstalledAppInfo();
			ApplicationInfo info = pInfo.applicationInfo;
			installedAppInfo.setAppInfo(info);
			installedAppInfo.setIcon(info.loadIcon(pm));
			installedAppInfo.setName(info.loadLabel(pm) + "");
			installedAppInfo.setVersion(pInfo.versionName);
			installedAppInfo.setVersionCode(pInfo.versionCode);
			installedAppInfo.setPkgName(info.packageName);
			// map.put("uninstall", R.drawable.uninstall);
			// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
			// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
			String dir = info.publicSourceDir;
			int size = Integer.valueOf((int) new File(dir).length());
			installedAppInfo.setSize(sizeFormat(size));
			list.add(installedAppInfo);

			// for (int i = 0; i <
			// ADownloadService.updateAPKList.apkList.size(); i++) {
			// if (info.packageName
			// .equals(ADownloadService.updateAPKList.apkList.get(i).apkPackageName))
			// {
			//
			// }
			// }
		}

		return list;
	}

	public static InstalledAppInfo getInstallAppInfoByPackage(Context context,
			List<InstalledAppInfo> installedAppInfos, String apkPackageName) {
		InstalledAppInfo installedAppInfo = new InstalledAppInfo();
		for (int i = 0; i < installedAppInfos.size(); i++) {
			if (null != installedAppInfos.get(i)) {
				if (installedAppInfos.get(i).getPkgName()
						.equals(apkPackageName)) {
					return installedAppInfos.get(i);
				}
			}
		}
		return installedAppInfo;
	}

	public static String sizeFormat(int size) {
		if ((float) size / 1024 > 1024) {
			float size_mb = (float) size / 1024 / 1024;
			return String.format("%.2f", size_mb) + "M";
		}
		return size / 1024 + "K";
	}

	public static String numberFormat(long number) {
		String numberString = String.valueOf(number);
		String returnString = "";
		if (numberString.length() > 4) {
			returnString = numberString.substring(0, numberString.length() - 4);
		} else if (numberString.length() == 4) {
			returnString = numberString.substring(0, 1) + ","
					+ numberString.substring(1, 4);
		} else {
			returnString = numberString;
		}
		return returnString;
	}

	// public static boolean checkIsDownload(Context context,
	// ADownloadApkItem aDownloadApkItem) {
	// int flag = 0;
	// int networkType = NetTool.getNetWorkType(context);
	// switch (networkType) {
	// // 无网络
	// case 1:
	// flag = 1;
	// break;
	// // Wifi状态
	// case 2:
	// flag = 2;
	// break;
	// case 3:
	// if (DJMarketUtils.isOnlyWifi(context)) {
	// flag = 3;
	// } else {
	// int size = context.getSharedPreferences(
	// DONGJI_SHAREPREFERENCES, 0).getInt(SHARE_DOWNLOADSIZE,
	// 0);
	// if ((DJMarketUtils.getMaxFlow(context) * 1024 - size) <
	// aDownloadApkItem.apkTotalSize) {
	// flag = 4;
	// } else {
	// flag = 2;
	// }
	//
	// }
	// break;
	// }
	// checkFlag(context, flag);
	// if (flag == 2) {
	// return true;
	// } else {
	// return false;
	// }
	// }
	//
	// private static void checkFlag(Context c, int flag) {
	// final Context context = c;
	// // Intent intent=new Intent(BROADCAST_ACTION_CHECKDOWNLOADSTATUS);
	// switch (flag) {
	// case 1:
	// // intent.putExtra(BROADCAST_DIALOG_NOCONNECT, true);
	// // context.sendBroadcast(intent);
	// // new AlertDialog.Builder(context).setTitle("提示")
	// // .setMessage("网络未连接，请连接后重新下载").setPositiveButton("确定", null)
	// // .show();
	// Intent activityIntent = new Intent();
	// activityIntent.setClass(context, MyDialogActivity.class);
	// context.startActivity(activityIntent);
	// break;
	// case 3:
	// // intent.putExtra(BROADCAST_DIALOG_SETTINGFLOW, true);
	// // context.sendBroadcast(intent);
	// new AlertDialog.Builder(context).setTitle("提示")
	// .setMessage("您设置了仅用WIFI下载，下载需要设置蜂窝流量，请问是否继续下载")
	// .setPositiveButton("取消", new OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// }).setNegativeButton("设置", new OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// Intent intent = new Intent();
	// intent.setClass(context, Setting_Activity.class);
	// context.startActivity(intent);
	// }
	// }).show();
	// break;
	// case 4:
	// // intent.putExtra(BROADCAST_DIALOG_REMAINFLOWLACK, true);
	// // context.sendBroadcast(intent);
	// new AlertDialog.Builder(context).setTitle("提示")
	// .setMessage("您的蜂窝流量不足，请问是否继续下载")
	// .setPositiveButton("取消", new OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// }).setNegativeButton("设置", new OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// Intent intent = new Intent();
	// intent.setClass(context, Setting_Activity.class);
	// context.startActivity(intent);
	// }
	// }).show();
	// break;
	// }
	// }

	private static void showLog(String msg) {
		Log.d("NetTool", msg);
	}

	private static void showErrorLog(String msg) {
		Log.e("NetTool", msg);
	}
}
