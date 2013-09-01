package com.dongji.market.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.activity.ADownloadActivity;
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.activity.BaseActivity;
import com.dongji.market.activity.MainActivity;
import com.dongji.market.activity.PublicActivity;
import com.dongji.market.activity.SoftwareManageActivity;
import com.dongji.market.activity.SoftwareMove_list_Activity;
import com.dongji.market.adapter.OnDownloadChangeStatusListener;
import com.dongji.market.application.AppMarket;
import com.dongji.market.database.MarketDatabase.Setting_Service;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.ADownloadApkDBHelper;
import com.dongji.market.download.ADownloadApkItem;
import com.dongji.market.download.ADownloadService;
import com.dongji.market.download.DownloadService;
import com.dongji.market.download.NetTool;
import com.dongji.market.listener.SinaOAuthDialogListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.InstalledAppInfo;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.widget.CustomNoTitleDialog;
import com.dongji.market.widget.TencentLoginDialog;
import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.demo.OAuthV2ImplicitGrant;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.weibo.net.Weibo;

/**
 * 用于放置关于动机应用市场相关的帮助方法
 * 
 * @author zhangkai
 * 
 */
public class DJMarketUtils implements AConstDefine {
	public static final int STATUS_CAN_DOWNLOAD = 0; // 可以下载
	public static final int STATUS_NOT_NETWORK = 1; // 没有网络
	public static final int STATUS_ONLY_MOBILE = 2; // 只有手机网络，无wifi
	public static final int STATUS_SDCARD_INSUFFICIENT = 3; // 下载空间不足

	public static final int STATUS_SETTING_FLOW_INSUFFICIENT = 4; // 设置流量不足以完成此次下载
	public static final int STATUS_NOT_SDCARD = 5; // 无SD卡
	public static final int STATUS_NOT_SETTING_MOBILE_DOWNLOAD = 6; // 未设置开启蜂窝下载

	// public static boolean IS_INSTALLING = false;

	private static NumberFormat numberFormat = new DecimalFormat("###,###");

	/**
	 * 是否默认安装
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isDefaultInstall(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("auto_install") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否只使用wifi下载
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isOnlyWifi(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("only_wifi") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取限制流量值
	 * 
	 * @param cxt
	 * @return
	 */
	public static int getMaxFlow(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		return service.select("limit_flow");
	}

	/**
	 * 是否使用后台下载
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean backgroundDownload(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("download_bg") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否自动更新
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isAutoUpdate(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("auto_update") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否启用应用更新通知
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isUpdatePrompt(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("update_msg") == 1) {
			return true;
		}
		return false;
	}

	public static final int FLAG_EXTERNAL_STORAGE = 1 << 18;
	public static final int INSTALL_LOCATION_PREFER_EXTERNAL = 2;

	public static final int MOVEAPPTYPE_MOVETOSDCARD = 1;
	public static final int MOVEAPPTYPE_MOVETOPHONE = 2;
	public static final int MOVEAPPTYPE_NONE = 3;

	public static List<InstalledAppInfo> getInstalledAppsByFlag(Context context, int flag) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
		if (flag == SoftwareMove_list_Activity.FLAG_PHONECARD) {
			for (PackageInfo pInfo : packages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 && !info.packageName.equals("com.dongji.market") && info.sourceDir.substring(0, 2).equals("/d")) {
					installedAppInfo.setAppInfo(info);
					// installedAppInfo.setIcon(info.loadIcon(pm));
					installedAppInfo.setName(info.loadLabel(pm) + "");
					installedAppInfo.setVersion(pInfo.versionName);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.moveType = getMoveType(pInfo, info);
					// map.put("uninstall", R.drawable.uninstall);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					if (installedAppInfo.moveType != MOVEAPPTYPE_NONE) {
						list.add(installedAppInfo);
					}
				}
			}
		} else if (flag == SoftwareMove_list_Activity.FLAG_SDCARD) {

			for (PackageInfo pInfo : packages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 && !info.packageName.equals("com.dongji.market") && info.sourceDir.substring(0, 2).equals("/m")) {
					installedAppInfo.setAppInfo(info);
					// installedAppInfo.setIcon(info.loadIcon(pm));
					installedAppInfo.setName(info.loadLabel(pm) + "");
					installedAppInfo.setVersion(pInfo.versionName);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.moveType = getMoveType(pInfo, info);
					// map.put("uninstall", R.drawable.uninstall);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					if (installedAppInfo.moveType != MOVEAPPTYPE_NONE) {
						list.add(installedAppInfo);
					}
				}
			}
		}
		return list;
	}

	public static int getMoveType(PackageInfo pInfo, ApplicationInfo info) {
		int moveType = MOVEAPPTYPE_NONE;
		if ((FLAG_EXTERNAL_STORAGE & info.flags) != 0) {
			moveType = MOVEAPPTYPE_MOVETOPHONE;
		} else if (pInfo != null) {
			int installLocation = 1;
			try {
				Field field = pInfo.getClass().getDeclaredField("installLocation");
				field.setAccessible(true);
				installLocation = field.getInt(pInfo);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (installLocation == 0) {
				moveType = MOVEAPPTYPE_MOVETOSDCARD;
				// System.out
				// .println("=======phone:" + info.loadLabel(pm));
			} else if (installLocation == -1 || installLocation == 1) {
				moveType = MOVEAPPTYPE_NONE;
				// System.out.println("=====not move:"
				// + info.loadLabel(pm));
			}
		}
		System.out.println("moveType..............." + moveType);
		return moveType;
	}

	/**
	 * 下载前的检查（点击界面上下载按钮开始下载）
	 * 
	 * @param context
	 * @param apkItem
	 * @param mTextView
	 * @param listener
	 */
	public static void checkDownload(Context context, ApkItem apkItem, TextView mTextView, OnDownloadChangeStatusListener listener, Map<String, Object> map) {
		if (NetTool.checkIsDownload(context, apkItem.packageName + "_" + apkItem.versionCode)) {
			NetTool.installApp(context, apkItem.packageName + "_" + apkItem.versionCode);
			return;
		}
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(context);
		if (aDownloadApkDBHelper.selectApkIsExist(apkItem.packageName, apkItem.versionCode) && AndroidUtils.checkFileExists(NetTool.DOWNLOADPATH + apkItem.appId)) {
			Toast.makeText(context, "此APK已存在", Toast.LENGTH_SHORT).show();
			return;
		}
		int status = DJMarketUtils.isCanDownload(context);
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK:
			AndroidUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			AndroidUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			AndroidUtils.showToast(context, R.string.download_size_insufficient);
			break;
		case DJMarketUtils.STATUS_ONLY_MOBILE:
			boolean isPromptUser = false;
			int flag = 0;
			try {
				ApkDetailActivity.class.cast(context);
			} catch (ClassCastException e) {
				isPromptUser = ((PublicActivity) context).is3GDownloadPromptUser();
				flag = 1;
			}
			if (flag == 0) {
				isPromptUser = ((ApkDetailActivity) context).is3GDownloadPromptUser();
			}
			if (isPromptUser) {
				if (ADownloadService.canUse3GDownload()) {
					prepareDownload(context, apkItem, mTextView, listener, map);
				} else {
					// showFlowSettingDialog(context);
					Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
					context.sendBroadcast(intent);
				}
			} else {
				showMobileDownloadDialog(context, apkItem, mTextView, listener, map);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			prepareDownload(context, apkItem, mTextView, listener, map);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	/**
	 * 取消列表下载
	 */
	public static void cancelListDownload(Context context, ApkItem item) {
		Intent intent = null;
		ADownloadApkDBHelper db = new ADownloadApkDBHelper(context);
		if (item.status == STATUS_APK_INSTALL) {
			intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
			intent.putExtra(BROADCAST_CANCELDOWNLOAD, item.packageName + "_" + item.versionCode);
		} else if (item.status == STATUS_APK_UPDATE) {
			intent = new Intent(BROADCAST_ACTION_UPDATE);
			intent.putExtra(BROADCAST_CANCELUPDATE, item.packageName + "_" + item.versionCode);
		}
		context.sendBroadcast(intent);
		db.deleteDownloadByPAndV(item.packageName, item.versionCode);
		NetTool.deleteFileByApkSaveName(item.packageName + "_" + item.versionCode);
	}

	private static void prepareDownload(Context context, ApkItem apkItem, TextView mTextView, OnDownloadChangeStatusListener listener, Map<String, Object> map) {
		int apkType = getApkType(apkItem.packageName, apkItem.versionCode);
		if (apkType == STATUS_APK_UNINSTALL) {
			NetTool.startServiceToDownload(context, new ADownloadApkItem(apkItem, STATUS_OF_PREPAREDOWNLOAD));
		} else if (apkType == STATUS_APK_UNUPDATE) {
			NetTool.startServiceToDownload(context, new ADownloadApkItem(apkItem, STATUS_OF_PREPAREUPDATE));
		}
		if (listener != null) {
			listener.onDownload(apkItem, mTextView, map);
		}
	}

	private static int getApkType(String apkPackageName, int apkVersionCode) {
		if (null != ADownloadService.updateAPKList) {
			for (int i = 0; i < ADownloadService.updateAPKList.apkList.size(); i++) {
				if (apkPackageName.equals(ADownloadService.updateAPKList.apkList.get(i).apkPackageName) && apkVersionCode == ADownloadService.updateAPKList.apkList.get(i).apkVersionCode) {
					return STATUS_APK_UNUPDATE;
				}
			}

		}
		return STATUS_APK_UNINSTALL;

	}

	/**
	 * 下载前的检查（列表下载）
	 * 
	 * @param context
	 * @param downloadType
	 * @param count
	 */
	public static void checkDownload(Context context, int downloadType) {
		int status = DJMarketUtils.isCanDownload(context);
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK:
			AndroidUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			AndroidUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			AndroidUtils.showToast(context, R.string.download_size_insufficient);
			break;
		case DJMarketUtils.STATUS_ONLY_MOBILE:
			boolean isPromptUser;
			if (downloadType == FLAG_ONEKEYUPDATEING) {
				isPromptUser = ((ADownloadActivity) context).is3GDownloadPromptUser();
			} else {
				isPromptUser = ((MainActivity) context).is3GDownloadPromptUser();
			}
			if (isPromptUser) {
				if (ADownloadService.canUse3GDownload()) {
					prepareDownload(context, downloadType);
				} else {
					// showFlowSettingDialog(context);
					Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
					context.sendBroadcast(intent);
				}
			} else {
				// TODO 这个地方不太懂
				showMobileDownloadDialog(context, downloadType);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			prepareDownload(context, downloadType);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	/**
	 * 下载前的检查（列表下载）
	 * 
	 * @param context
	 * @param downloadType
	 * @param count
	 */
	public static void checkDownload(Context context, int downloadType, ArrayList<ApkItem> apkItems) {
		int status = DJMarketUtils.isCanDownload(context);
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK:
			AndroidUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			AndroidUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			AndroidUtils.showToast(context, R.string.download_size_insufficient);
			break;
		case DJMarketUtils.STATUS_ONLY_MOBILE:
			boolean isPromptUser = ((SoftwareManageActivity) context).is3GDownloadPromptUser();
			if (isPromptUser) {
				if (ADownloadService.canUse3GDownload()) {
					prepareDownload(context, downloadType, apkItems);
				} else {
					// showFlowSettingDialog(context);
					Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
					context.sendBroadcast(intent);
				}
			} else {
				// TODO 这个地方不太懂
				showMobileDownloadDialog(context, downloadType, apkItems);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			prepareDownload(context, downloadType, apkItems);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	public static void prepareDownload(Context context, int downloadType) {
		Intent serviceIntent = new Intent();

		if (downloadType == FLAG_ONEKEYUPDATEING) {
			serviceIntent.putExtra(FLAG_ONEKEYUPDATE, true);
		} else if (downloadType == FLAG_LISTUNDOWNTASK) {
			serviceIntent.putExtra(FLAG_ISUNDONETASK, true);
		} else if (downloadType == INT_CONTINUEPAUSETASK) {
			serviceIntent.putExtra(FLAG_CONTINUEPAUSETASK, true);
		}

		serviceIntent.setClass(context, ADownloadService.class);
		context.startService(serviceIntent);
	}

	public static void prepareDownload(Context context, int downloadType, ArrayList<ApkItem> apkItems) {

		Intent serviceIntent = new Intent();

		if (downloadType == INT_CLOUDRESTORE) {
			serviceIntent.putExtra(FLAG_CLOUDRESTORE, true);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(FLAG_RESTORELIST, apkItems);
			serviceIntent.putExtras(bundle);
		}
		serviceIntent.setClass(context, ADownloadService.class);
		context.startService(serviceIntent);
	}

	/**
	 * 下载前的检查（下载管理界面上的下载，还有后台下载等……）
	 * 
	 * @param activityName
	 * @param context
	 * @param apkId
	 * @param aDownloadApkItem
	 */
	public static void checkDownload(Context context, ADownloadApkItem aDownloadApkItem) {
		if (NetTool.checkIsDownload(context, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode)) {
			switch (aDownloadApkItem.apkStatus) {
			case STATUS_OF_PAUSE:
			case STATUS_OF_PAUSE_BYHAND:
				aDownloadApkItem.apkStatus = STATUS_OF_DOWNLOADCOMPLETE;
				break;
			case STATUS_OF_PAUSEUPDATE:
			case STATUS_OF_PAUSEUPDATE_BYHAND:
				aDownloadApkItem.apkStatus = STATUS_OF_UPDATECOMPLETE;
				break;
			}

			NetTool.installApp(context, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
			return;
		}
		int status = DJMarketUtils.isCanDownload(context);
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK:
			AndroidUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			AndroidUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			AndroidUtils.showToast(context, R.string.download_size_insufficient);
			break;
		case DJMarketUtils.STATUS_ONLY_MOBILE:
			if (((ADownloadActivity) context).is3GDownloadPromptUser()) {
				if (ADownloadService.canUse3GDownload()) {
					prepareDownload(context, aDownloadApkItem);
				} else {
					// showFlowSettingDialog(context);
					Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
					context.sendBroadcast(intent);
				}
			} else {
				showMobileDownloadDialog(context, aDownloadApkItem);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			prepareDownload(context, aDownloadApkItem);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	private static void prepareDownload(Context context, ADownloadApkItem aDownloadApkItem) {
		System.out.println("状态——————" + aDownloadApkItem.apkStatus);
		if (aDownloadApkItem.apkStatus == STATUS_OF_PAUSE_BYHAND || aDownloadApkItem.apkStatus == STATUS_OF_PAUSE) {
			Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
			intent.putExtra(BROADCAST_CONTINUEDOWNLOAD, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
			context.sendBroadcast(intent);
		} else if (aDownloadApkItem.apkStatus == STATUS_OF_PAUSEUPDATE_BYHAND || aDownloadApkItem.apkStatus == STATUS_OF_PAUSEUPDATE) {
			Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
			intent.putExtra(BROADCAST_CONTINUEUPDATE, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
			context.sendBroadcast(intent);
		} else if (aDownloadApkItem.apkStatus == STATUS_OF_UPDATE) {
			aDownloadApkItem.apkStatus = STATUS_OF_PREPAREUPDATE;
			NetTool.startServiceToDownload(context, aDownloadApkItem);
		}
	}

	private static void showMobileDownloadDialog(final Context context, final ApkItem apkItem, final TextView mTextView, final OnDownloadChangeStatusListener listener, final Map<String, Object> map) {
		if (!((Activity) context).isFinishing()) {
			final CustomNoTitleDialog mDialog = new CustomNoTitleDialog(context);
			mDialog.setMessage(R.string.cellular_download_prompt_msg);
			mDialog.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ADownloadService.canUse3GDownload()) { // 判断当前用户使用3G下载有否流量限制
						prepareDownload(context, apkItem, mTextView, listener, map);
						// NetTool.startDownload(context,
						// new ADownloadApkItem(apkItem,
						// STATUS_OF_DOWNLOADING));
						// NetTool.onDownloadBtnClick(context, new
						// ADownloadApkItem(item,
						// STATUS_OF_DOWNLOADING));

						int flag = 0;
						try {
							ApkDetailActivity.class.cast(context);
						} catch (ClassCastException e) {
							((BaseActivity) context).set3GDownloadPromptUser();
							flag = 1;
						}
						if (flag == 0) {
							((ApkDetailActivity) context).set3GDownloadPromptUser();
						}
					} else {
						// showFlowSettingDialog(context);
						Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
					mDialog.dismiss();
				}
			});
			mDialog.setNegativeButton(context.getString(R.string.cancel), null);
			if (mDialog != null) {
				mDialog.show();
			}
		}
	}

	private static void showMobileDownloadDialog(final Context context, final ADownloadApkItem aDownloadApkItem) {
		if (!((Activity) context).isFinishing()) {
			final CustomNoTitleDialog mDialog = new CustomNoTitleDialog(context);

			mDialog.setMessage(R.string.cellular_download_prompt_msg);
			mDialog.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ADownloadService.canUse3GDownload()) { // 判断当前用户使用3G下载有否流量限制
						prepareDownload(context, aDownloadApkItem);
						// if (aDownloadApkItem == null) {
						// Intent intent = new Intent(
						// BROADCAST_ACTION_DOWNLOAD);
						// intent.putExtra(BROADCAST_CONTINUEDOWNLOAD,
						// apkId);
						// context.sendBroadcast(intent);
						// } else {
						// NetTool.setNotification(
						// context,
						// FLAG_NOTIFICATION_UPDATEING,
						// ADownloadService
						// .getUpdateCountByStatus(STATUS_OF_UPDATEING)
						// +
						// 1);
						// aDownloadApkItem.apkStatus =
						// STATUS_OF_UPDATEING;
						// NetTool.startDownload(context,
						// aDownloadApkItem);
						// Intent intent = new Intent(
						// BROADCAST_ACTION_3GDOWNLOAD);
						// context.sendBroadcast(intent);
						//
						// }
						((ADownloadActivity) context).set3GDownloadPromptUser();
					} else {
						// showFlowSettingDialog(context);
						Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
					mDialog.dismiss();
				}
			});
			mDialog.setNegativeButton(context.getString(R.string.cancel), null);

			if (mDialog != null) {
				mDialog.show();
			}
		}
	}

	private static void showMobileDownloadDialog(final Context context, final int downloadType) {
		if (!((Activity) context).isFinishing()) {
			final CustomNoTitleDialog mDialog = new CustomNoTitleDialog(context);

			mDialog.setMessage(R.string.cellular_download_prompt_msg);
			mDialog.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ADownloadService.canUse3GDownload()) { // 判断当前用户使用3G下载有否流量限制
						prepareDownload(context, downloadType);
						if (downloadType == FLAG_ONEKEYUPDATEING) {
							((ADownloadActivity) context).set3GDownloadPromptUser();
						} else {
							((MainActivity) context).set3GDownloadPromptUser();
						}

					} else {
						// showFlowSettingDialog(context);
						Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
					mDialog.dismiss();
				}
			});
			mDialog.setNegativeButton(context.getString(R.string.cancel), null);

			if (mDialog != null) {
				mDialog.show();
			}
		}
	}

	private static void showMobileDownloadDialog(final Context context, final int downloadType, final ArrayList<ApkItem> apkItems) {
		if (!((Activity) context).isFinishing()) {
			final CustomNoTitleDialog mDialog = new CustomNoTitleDialog(context);

			mDialog.setMessage(R.string.cellular_download_prompt_msg);
			mDialog.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ADownloadService.canUse3GDownload()) { // 判断当前用户使用3G下载有否流量限制
						prepareDownload(context, downloadType, apkItems);
						((SoftwareManageActivity) context).set3GDownloadPromptUser();

					} else {
						// showFlowSettingDialog(context);
						Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
					mDialog.dismiss();
				}
			});
			mDialog.setNegativeButton(context.getString(R.string.cancel), null);

			if (mDialog != null) {
				mDialog.show();
			}
		}
	}

	// private static void showFlowSettingDialog(Context cxt) {
	// final Context context = cxt;
	// final CustomNoTitleDialog mFlowSettingDialog = new CustomNoTitleDialog(
	// context);
	// mFlowSettingDialog.setMessage(R.string.continue_download_setting_msg);
	// mFlowSettingDialog.setNeutralButton(
	// context.getString(R.string.goto_setting),
	// new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// Intent intent = new Intent(context,
	// Setting_Activity.class);
	// context.startActivity(intent);
	// mFlowSettingDialog.dismiss();
	// }
	// });
	// mFlowSettingDialog.show();
	// }

	// public static void showNoFlowDialog(Activity activity) {
	// final Activity context = activity;
	//
	// final CustomNoTitleDialog mFlowSettingDialog = new CustomNoTitleDialog(
	// activity);
	// mFlowSettingDialog.setMessage(R.string.dialog_tip_noflow);
	// mFlowSettingDialog.setNeutralButton(
	// activity.getString(R.string.dialog_setting),
	// new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// mFlowSettingDialog.dismiss();
	// SettingFlowDialog settingFlowDialog = new SettingFlowDialog(
	// context);
	// settingFlowDialog.show();
	// }
	// });
	// mFlowSettingDialog.setNegativeButton(
	// activity.getString(R.string.dialog_pause),
	// new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// mFlowSettingDialog.dismiss();
	// }
	// });
	// mFlowSettingDialog.show();
	//
	//
	// }

	/**
	 * 是否超过7天
	 */
	public static boolean isExceedDate(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getPackageName() + "_temp", Context.MODE_PRIVATE);
		long lastClearDate = mSharedPreferences.getLong("last_clear_date", 0);
		long currentTimeMillis = System.currentTimeMillis();
		long num = currentTimeMillis - lastClearDate;
		long savenDay = 1000 * 60 * 60 * 24 * 7;
		return num >= savenDay;
	}

	/**
	 * 写入当前清除垃圾数据的时间
	 * 
	 * @param context
	 */
	public static void writeClearDate(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getPackageName() + "_temp", Context.MODE_PRIVATE);
		long currentTimeMillis = System.currentTimeMillis();
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putLong("last_clear_date", currentTimeMillis);
		mEditor.commit();
	}

	/**
	 * 安装后是否自动删除安装包
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isAutoDelPkg(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("auto_del_pkg") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否开启节省流量模式
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isSaveFlow(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("save_flow") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 判断当前网络状况及剩余可下载流量，是否可以下载
	 * 
	 * @param context
	 * @return
	 */
	public static int isCanDownload(Context context) {
		if (!AndroidUtils.isNetworkAvailable(context)) {
			return STATUS_NOT_NETWORK;
		} else if (!AndroidUtils.isSdcardExists()) {
			return STATUS_NOT_SDCARD;
		} else if (AndroidUtils.getSdcardAvalilaleSize() / 1024 / 1024 < 256) {
			return STATUS_SDCARD_INSUFFICIENT;
		} else if (!AndroidUtils.isWifiAvailable(context) && AndroidUtils.isMobileAvailable(context)) {
			// Setting_Service db = new Setting_Service(context);
			// if (db.select("only_wifi") != 1) {
			// return STATUS_ONLY_MOBILE;
			// } else {
			// return STATUS_NOT_SETTING_MOBILE_DOWNLOAD;
			// }
			if (!isOnlyWifi(context)) {
				return STATUS_ONLY_MOBILE;
			} else {
				return STATUS_NOT_SETTING_MOBILE_DOWNLOAD;
			}
		}
		return STATUS_CAN_DOWNLOAD;
	}

	/**
	 * 查询使用蜂窝下载所消耗的流量大小
	 * 
	 * @param context
	 * @return
	 */
	public static long queryUse3GDownloadSize(Context context) {
		/*
		 * if (ADownloadService.isSelfStart()) { return
		 * ADownloadService.get3GDownloadSize(); } else { SharedPreferences pref
		 * = context.getSharedPreferences( AConstDefine.DONGJI_SHAREPREFERENCES,
		 * Context.MODE_PRIVATE); long size =
		 * pref.getLong(AConstDefine.SHARE_DOWNLOADSIZE, 0); return size; }
		 */
		if (DownloadService.mDownloadService != null) {
			return DownloadService.mDownloadService.getAlreadyUseGprsTraffic();
		} else {
			return 0;
		}
	}

	/**
	 * 将安装量转换如字符
	 * 
	 * @param num
	 * @return
	 */
	public static String convertionInstallNumber(Context context, long num) {
		String value = String.valueOf(num);
		if (num < 10000) {
			return numberFormat.format(num);
		} else if (num >= 10000 && num < 100000000) {
			return value.substring(0, value.length() - 4) + context.getString(R.string.myriad);
		} else {
			return value.substring(0, value.length() - 8) + context.getString(R.string.calculate);
		}
	}

	/**
	 * 判断用户是否已登录
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isLogin(Context context) {
		LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
		return !TextUtils.isEmpty(loginParams.getUserName());
	}

	/**
	 * 检测新浪账号是否登录
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSinaLogin(Context context) {
		// SharedPreferences loginPref = context.getSharedPreferences(
		// AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
		// String sina_name = loginPref.getString("sina_user_name", "");
		LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
		String sina_name = loginParams.getSinaUserName();
		if (sina_name != null && sina_name.length() > 0) {
			return true;
		}
		return false;
	}

	private static final String PACKAGE_STR = "package:";

	public static String convertPackageName(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return "";
		}
		int num = packageName.indexOf(PACKAGE_STR);
		if (num != -1) {
			return packageName.substring(PACKAGE_STR.length() + num, packageName.length());
		}
		return packageName;
	}

	/**
	 * 根据包名获取软件信息
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static InstalledAppInfo getInstalledAppInfoByPackageName(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				installedAppInfo.setAppInfo(appInfo);
				installedAppInfo.setIcon(appInfo.loadIcon(pm));
				installedAppInfo.setName(appInfo.loadLabel(pm) + "");
				installedAppInfo.setVersion(packageInfo.versionName);
				installedAppInfo.setPkgName(appInfo.packageName);
				// map.put("uninstall", R.drawable.uninstall);
				// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
				// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
				String dir = appInfo.publicSourceDir;
				int size = Integer.valueOf((int) new File(dir).length());
				installedAppInfo.setSize(sizeFormat(size));
				return installedAppInfo;
			}
		} catch (NameNotFoundException e) {
			return null;
		}
		return null;
	}

	/**
	 * 获取第三方已安装软件列表
	 * 
	 * @param context
	 * @return
	 */
	public static List<InstalledAppInfo> getInstalledApps(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
		for (PackageInfo pInfo : packages) {
			InstalledAppInfo installedAppInfo = new InstalledAppInfo();
			ApplicationInfo info = pInfo.applicationInfo;

			// 显示用户安装应用，而不显示系统程序
			if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 && !info.packageName.equals("com.dongji.market")) {
				installedAppInfo.setAppInfo(info);
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
		}

		return list;
	}

	/**
	 * 根据包名获取软件版本号
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static int getInstalledAppVersionCodeByPackageName(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		for (PackageInfo pInfo : packages) {
			ApplicationInfo info = pInfo.applicationInfo;
			if (info.packageName.equals(packageName)) {
				return pInfo.versionCode;
			}
		}
		return -1;
	}

	public static List<InstalledAppInfo> getBackupItemList(Context context) {
		File directory = new File(NetTool.BACKUPPATH);
		List<InstalledAppInfo> backupItemInfos = new ArrayList<InstalledAppInfo>();
		if (directory.exists()) {
			int i;
			// String apkName;
			File[] files = directory.listFiles();
			for (i = 0; i < files.length; i++) {
				// String[] tempString = files[i].getName().split("_");
				InstalledAppInfo tempBackupItemInfo = getApkFileInfo(context, NetTool.BACKUPPATH + files[i].getName());
				if (null != tempBackupItemInfo) {
					backupItemInfos.add(tempBackupItemInfo);
				}
			}
		}
		return backupItemInfos;
	}

	/**
	 * 通过反射获取未安装apk信息
	 * 
	 * @param ctx
	 * @param apkPath
	 * @return
	 */
	public static InstalledAppInfo getApkFileInfo(Context context, String apkPath) {
		File apkFile = new File(apkPath);
		// if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
		// System.out.println("文件路径不正确");
		// return null;
		// }

		InstalledAppInfo backupItemInfo = new InstalledAppInfo();
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// 反射得到pkgParserCls对象并实例化，有参数
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			// 从pkgParserCls类得到parsePackage方法
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();// 显示相关，这里设为默认
			typeArgs = new Class<?>[] { File.class, String.class, DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			// 执行pkgParser_parsePackageMtd方法并返回
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

			// 从返回的对象得到名为"applicationInfo"的字段对象.当文件已损坏时，pkgParserPkg为null
			if (pkgParserPkg == null) {
				return null;
			}
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

			// 从对象"pkgParserPkg"得到字段"appInfoFld"的值
			if (appInfoFld.get(pkgParserPkg) == null) {
				return null;
			}
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

			// 反射得到assetMagCls对象并实例化，无参
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			// 从assetMagCls类得到addAssetPath方法
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			// 执行assetMag_addAssetPathMtd方法
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			// 得到Resources对象并实例化，有参数
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = resCt.newInstance(valueArgs);

			// 读取apk文件的信息
			// appInfoData = new AppInfoData();
			if (info != null) {
				if (info.icon != 0) {// 图片存在，则读取相关信息
					Drawable icon = res.getDrawable(info.icon);
					backupItemInfo.setIcon(icon);
				}
				if (info.labelRes != 0) {
					String name = (String) res.getText(info.labelRes);
					backupItemInfo.setName(name);
				} else {
					String apkName = apkFile.getName();
					backupItemInfo.setName(apkName.substring(0, apkName.lastIndexOf(".")));
				}
			} else {
				return null;
			}
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
			if (packageInfo != null) {
				backupItemInfo.setVersion(packageInfo.versionName + "");
				backupItemInfo.setPkgName(packageInfo.packageName);
				backupItemInfo.setVersionCode(packageInfo.versionCode);
			}
			backupItemInfo.setSize(ApkInfoFromSD.sizeFormat(apkFile.length()));
			// backupItemInfo.setSize(String.valueOf(apkFile.length());
			return backupItemInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 格式化数据大小
	 * 
	 * @param size
	 * @return
	 */
	public static String sizeFormat(int size) {
		if ((float) size / 1024 > 1024) {
			float size_mb = (float) size / 1024 / 1024;
			return String.format("%.2f", size_mb) + "M";
		}
		return size / 1024 + "K";
	}

	public static long sizeFromMToLong(String sizeString) {
		long size = 0;
		if (sizeString.endsWith("M")) {
			sizeString = sizeString.substring(0, sizeString.length() - 1);
			size = (long) ((Double.valueOf(sizeString)) * 1024 * 1024);
		} else if (sizeString.endsWith("K")) {
			sizeString = sizeString.substring(0, sizeString.length() - 1);
			size = (long) ((Double.valueOf(sizeString)) * 1024);
		}
		return size;
	}

	/**
	 * 新浪登录对话框
	 * 
	 * @param context
	 * @param handler
	 */
	public static void sinaLogin(Activity context, Handler handler) {
		final String CONSUMER_KEY = "1699956234";// 替换为开发者的appkey，例如"1646212960";
		final String CONSUMER_SECRET = "c01ed617178219c1344777e27623cade";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";

		if (AndroidUtils.isNetworkAvailable(context)) {
			Weibo weibo = Weibo.getInstance();
			weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);

			// Oauth2.0
			// 隐式授权认证方式
			weibo.setRedirectUrl("http://91dongji.com");// 此处回调页内容应该替换为与appkey对应的应用回调页
			// 对应的应用回调页可在开发者登陆新浪微博开发平台之后，
			// 进入我的应用--应用详情--应用信息--高级信息--授权设置--应用回调页进行设置和查看，
			// 应用回调页不可为空
			weibo.authorize(context, new SinaOAuthDialogListener(context, handler, CONSUMER_KEY, CONSUMER_SECRET));
		} else {
			AndroidUtils.showToast(context, R.string.net_error);
		}
	}

	public static String getCookieValue(Context context, String url, String key) {
		CookieSyncManager csm = CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		csm.sync();
		String cookieStr = cookieManager.getCookie(url);
		System.out.println("========cookied:" + cookieStr);
		String[] strs = cookieStr.split(";");
		String value = null;
		for (String string : strs) {
			if (string.trim().startsWith(key)) {
				value = string.substring(string.indexOf("=") + 1);
				break;
			}
		}
		return value;
	}

	public static String urlDecode(String urlCodeStr) {
		String str = null;
		try {
			str = URLDecoder.decode(urlCodeStr, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	public static void appUpdate(Context context, String url) {
		HttpURLConnection httpURLConnection = null;
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			URL mURL = new URL(url);
			httpURLConnection = (HttpURLConnection) mURL.openConnection();
			httpURLConnection.setConnectTimeout(10000);
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			is = httpURLConnection.getInputStream();
			String path = AndroidUtils.getSdcardFile() + "/" + context.getPackageName() + ".apk";
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);
			int i = 0;
			byte[] data = new byte[1024];
			while ((i = is.read(data)) != -1) {
				fos.write(data, 0, i);
			}
			fos.flush();
			if (context != null && !((Activity) context).isFinishing()) {
				AndroidUtils.showToast(context, R.string.app_download_done);
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
				context.startActivity(installIntent);
			}
		} catch (IOException e) {
			System.out.println("app update:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
			}
		}
		// getInstalledAppInfoByPackageName(context, packageName);

	}

	public static void tencentLogin(Activity context, Handler handler) {
		// !!!请根据您的实际情况修改!!! 认证成功后浏览器会被重定向到这个url中 必须与注册时填写的一致
		String redirectUri = "http://www.91dongji.com/";
		// !!!请根据您的实际情况修改!!! 换为您为自己的应用申请到的APP KEY
		String clientId = "801317350";
		// !!!请根据您的实际情况修改!!! 换为您为自己的应用申请到的APP SECRET
		String clientSecret = "0b46e85979a4d529cf44b864900406d2";

		OAuthV2 oAuth;

		if (AndroidUtils.isNetworkAvailable(context)) {
			oAuth = new OAuthV2(redirectUri);
			oAuth.setClientId(clientId);
			oAuth.setClientSecret(clientSecret);

			// 关闭OAuthV2Client中的默认开启的QHttpClient。
			OAuthV2Client.getQHttpClient().shutdownConnection();

			new TencentLoginDialog(context, oAuth, handler).show();
		} else {
			AndroidUtils.showToast(context, R.string.net_error);
		}
	}

	public static void getTencentUsrInfo(Context context, OAuthV2 oAuth, Handler handler) {

		String response;
		UserAPI userAPI;
		JSONObject jsonObject;

		if (oAuth.getStatus() != 0) {
			Toast.makeText(context, R.string.tencetn_oAuth_failed, Toast.LENGTH_SHORT).show();
		} else {
			userAPI = new UserAPI(OAuthConstants.OAUTH_VERSION_2_A);
			LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
			try {
				response = userAPI.info(oAuth, "json");// 调用QWeiboSDK获取用户信息
				jsonObject = new JSONObject(new JSONObject(response).getString("data"));
				loginParams.setTencentUserName(jsonObject.getString("nick"));
				loginParams.setTencent_oAuth(oAuth);
				// Message msg = new Message();
				// msg.what = OAuthV2ImplicitGrant.TENCENT_LOGIN_SUCCESS;
				// msg.obj = oAuth;
				// handler.sendMessage(msg);
				handler.sendEmptyMessage(OAuthV2ImplicitGrant.TENCENT_LOGIN_SUCCESS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			userAPI.shutdownConnection();
		}
		return;
	}

	public static boolean isTencentLogin(Context context) {
		LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
		String tencent_nick = loginParams.getTencentUserName();
		if (tencent_nick != null && tencent_nick.length() > 0) {
			return true;
		}
		return false;
	}

}
