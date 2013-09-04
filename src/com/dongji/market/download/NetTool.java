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
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.dongji.market.R;
import com.dongji.market.activity.SoftwareManageActivity;
import com.dongji.market.activity.BackupOrRestoreActivity.OnProgressChangeListener;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.BackupItemInfo;
import com.dongji.market.pojo.InstalledAppInfo;

public class NetTool implements AConstDefine {
	public static String DOWNLOADPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.dongji/dongjiMarket/cache/apk/";
	public static String BACKUPPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.dongji/dongjiMarket/backup/";

	/**
	 * 检查网络连接情况
	 * 
	 * @param context
	 * @return 1: 无网络 2: Wifi 3: GPRS 4: 其他网络
	 */
	public static int getNetWorkType(Context context) {
		// showLog("getNetWorkType");
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (null == networkInfo || !networkInfo.isAvailable()) {
			return 1;
		}
		if (State.CONNECTED == connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()) {
			return 2;
		}
		if (State.CONNECTED == connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()) {
			return 3;
		}
		return 4;
	}

	public static void setSharedPreferences(Context context, String key, int value) {
		SharedPreferences pref = context.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void setSharedPreferences(Context context, String key, String value) {
		SharedPreferences pref = context.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void setSharedPreferences(Context context, String key, boolean value) {
		SharedPreferences pref = context.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static int getSharedPreferences(Context context, String key, int value) {
		SharedPreferences pref = context.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		return pref.getInt(key, value);
	}

	public static String getSharedPreferences(Context context, String key, String value) {
		SharedPreferences pref = context.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		return pref.getString(key, value);
	}

	public static boolean getSharedPreferences(Context context, String key, boolean value) {
		SharedPreferences pref = context.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		return pref.getBoolean(key, value);
	}

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
		return DOWNLOADPATH + name + "." + suffix;
	}

	public static boolean createPath(String path) {
		File newfolder = new File(path);
		if (!newfolder.exists()) {
			return newfolder.mkdirs();
		}
		return true;
	}

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

	public static void deleteFileByPackageName(Context context, String packageName) {
		File directory = new File(DOWNLOADPATH);
		int versionCode = DJMarketUtils.getInstalledAppVersionCodeByPackageName(context, packageName);
		if (versionCode != -1) {
			if (directory.exists()) {
				File[] files = directory.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().equals(packageName + "_" + versionCode + ".apk")) {
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
		installIntent.setDataAndType(Uri.fromFile(new File(NetTool.DOWNLOADPATH + apkSaveName + ".apk")), "application/vnd.android.package-archive");
		context.startActivity(installIntent);
	}

	public static void installBackupApp(Context context, String apkName) {
		if (AndroidUtils.isRoot()) {
			AndroidUtils.rootInstallApp(NetTool.BACKUPPATH + apkName + ".apk");
		} else {
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			installIntent.setDataAndType(Uri.fromFile(new File(NetTool.BACKUPPATH + apkName + ".apk")), "application/vnd.android.package-archive");
			context.startActivity(installIntent);
		}
	}

	public static boolean isFastDoubleClick(long lastClickTime) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 800) {
			return true;
		}
		return false;
	}

	public static void setNotification(Context context, int id, int count) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notice = new Notification();
		notice.icon = R.drawable.icon;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
		int flag = 0;
		switch (id) {
		case FLAG_NOTIFICATION_DOWNLOAD:
			notice.flags = Notification.FLAG_AUTO_CANCEL;
			remoteViews.setTextViewText(R.id.tvNotificationTitle, context.getString(R.string.notification_tip_download));
			remoteViews.setTextViewText(R.id.tvNotificationText, count + context.getString(R.string.notification_tip_clickdownloading));
			flag = 1;
			break;
		case FLAG_NOTIFICATION_UPDATE:
			if (DJMarketUtils.isUpdatePrompt(context)) {
				notice.flags = Notification.FLAG_AUTO_CANCEL;
				remoteViews.setTextViewText(R.id.tvNotificationTitle, context.getString(R.string.notification_tip_update));
				remoteViews.setTextViewText(R.id.tvNotificationText, count + context.getString(R.string.notification_tip_clickupdate));
				flag = 1;
			}
			break;
		case FLAG_NOTIFICATION_UPDATEING:
			notice.flags = Notification.FLAG_AUTO_CANCEL;
			remoteViews.setTextViewText(R.id.tvNotificationTitle, context.getString(R.string.notification_tip_update));
			remoteViews.setTextViewText(R.id.tvNotificationText, count + context.getString(R.string.notification_tip_clickupdateing));
			flag = 1;
			break;
		case FLAG_NOTIFICATION_WAITINGINSTALL:
			notice.flags = Notification.FLAG_AUTO_CANCEL;
			remoteViews.setTextViewText(R.id.tvNotificationTitle, context.getString(R.string.notification_tip_download));
			remoteViews.setTextViewText(R.id.tvNotificationText, count + context.getString(R.string.notification_tip_clickdownloaded));
			flag = 1;
			break;
		}

		if (flag == 1) {
			notice.contentView = remoteViews;
			notice.when = System.currentTimeMillis();
			Intent intent = new Intent(context, SoftwareManageActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 0);
			notice.contentIntent = pendingIntent;
			notificationManager.notify(id, notice);
		}
	}


	public static void cancelNotification(Context context, int id) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (id == FLAG_NOTIFICATION_CANCELALL) {
			notificationManager.cancelAll();
		} else {
			notificationManager.cancel(id);
		}
	}

	public static int startToLocalBackup(Context context, List<BackupItemInfo> backupItemInfos, OnProgressChangeListener mListener) {
		File fromFile, toFile;
		String fromFileName;
		int count = 0;
		for (int i = 0; i < backupItemInfos.size(); i++) {
			fromFileName = backupItemInfos.get(i).appName;
			fromFile = new File(fromFileName);
			showLog(BACKUPPATH + fromFileName.substring(10, fromFileName.length() - 6) + "_" + backupItemInfos.get(i).appVerCode + ".apk");
			toFile = new File(BACKUPPATH + fromFileName.substring(10, fromFileName.length() - 6) + "_" + backupItemInfos.get(i).appVerCode + ".apk");
			if (copyfile(fromFile, toFile) == 1) {
				count++;
			}
			mListener.onProgressChange(fromFile.length());
		}
		return count;
	}


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

		}

		return list;
	}

	public static InstalledAppInfo getInstallAppInfoByPackage(Context context, List<InstalledAppInfo> installedAppInfos, String apkPackageName) {
		InstalledAppInfo installedAppInfo = new InstalledAppInfo();
		for (int i = 0; i < installedAppInfos.size(); i++) {
			if (null != installedAppInfos.get(i)) {
				if (installedAppInfos.get(i).getPkgName().equals(apkPackageName)) {
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
			returnString = numberString.substring(0, 1) + "," + numberString.substring(1, 4);
		} else {
			returnString = numberString;
		}
		return returnString;
	}

	private static void showLog(String msg) {
		Log.d("NetTool", msg);
	}

	private static void showErrorLog(String msg) {
		Log.e("NetTool", msg);
	}
}
