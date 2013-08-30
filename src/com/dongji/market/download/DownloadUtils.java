package com.dongji.market.download;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.activity.BaseActivity;
import com.dongji.market.activity.DownloadActivity;
import com.dongji.market.activity.PublicActivity;
import com.dongji.market.activity.SoftwareManageActivity;
import com.dongji.market.adapter.OnDownloadChangeStatusListener;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.widget.CustomNoTitleDialog;

public class DownloadUtils implements DownloadConstDefine {
	public static final int STATUS_CAN_DOWNLOAD = 0; // 可以下载
	public static final int STATUS_NOT_NETWORK = 1; // 没有网络
	public static final int STATUS_ONLY_MOBILE = 2; // 只有手机网络，无wifi
	public static final int STATUS_SDCARD_INSUFFICIENT = 3; // 下载空间不足
	public static final int STATUS_SETTING_FLOW_INSUFFICIENT = 4; // 设置流量不足以完成此次下载
	public static final int STATUS_NOT_SDCARD = 5; // 无SD卡
	public static final int STATUS_NOT_SETTING_MOBILE_DOWNLOAD = 6; // 未设置开启蜂窝下载

	private static CustomNoTitleDialog mUseGprsDialog;
	private static CustomNoTitleDialog mUseGprsDialog2;
	private static CustomNoTitleDialog mUseGprsDialog3;

	/**
	 * 删除下载文件
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteDownloadFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return file.delete();
		}
		return true;
	}

	public static void checkDownload(Context context, DownloadEntity entity) {
		checkDownload(context, entity, null, null);
	}

	public static void checkDownload(Context context, ApkItem apkItem, TextView mTextView, OnDownloadChangeStatusListener listener, Map<String, Object> map) {
		DownloadEntity entity = new DownloadEntity(apkItem);
		if (null != DownloadService.mDownloadService) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			int i = 0;
			for (; i < downloadList.size(); i++) {
				DownloadEntity d = downloadList.get(i);
				if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
					if (d.downloadType == TYPE_OF_COMPLETE) {
						String path = DOWNLOAD_ROOT_PATH + d.hashCode() + DOWNLOAD_FILE_POST_SUFFIX;
						File file = new File(path);
						if (file.exists()) {
							installApk(context, path);
						} else {
							Intent removeIntent = new Intent(BROADCAST_ACTION_REMOVE_DOWNLOAD);
							Bundle removeBundle = new Bundle();
							removeBundle.putParcelable(DOWNLOAD_ENTITY, entity);
							removeIntent.putExtras(removeBundle);
							context.sendBroadcast(removeIntent);

							checkDownload(context, entity, mTextView, listener);
						}
					} else {
						checkDownload(context, entity, mTextView, listener);
					}
					break;
				}
			}
			if (i == downloadList.size()) {
				checkDownload(context, entity, mTextView, listener);
			}
		} else {
			checkDownload(context, entity, mTextView, listener);
		}
	}

	private static void checkDownload(Context context, DownloadEntity entity, TextView mTextView, OnDownloadChangeStatusListener listener) {
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
			if (context instanceof ApkDetailActivity) {
				isPromptUser = ((ApkDetailActivity) context).is3GDownloadPromptUser();
			} else if (context instanceof DownloadActivity) {
				isPromptUser = ((DownloadActivity) context).is3GDownloadPromptUser();
			} else {
				isPromptUser = ((PublicActivity) context).is3GDownloadPromptUser();
			}
			if (isPromptUser) {
				if (DownloadService.mDownloadService != null) {
					if (DownloadService.mDownloadService.canUseGprsDownload()) {
						sendDownloadBroadcast(context, entity);
					} else {
						Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
				}
			} else {
				showUseMobileGprsPromptDialog(context, entity);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			sendDownloadBroadcast(context, entity);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	public static void checkOneKeyDownload(Context context, DownloadEntity entity) {
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
			if (context instanceof ApkDetailActivity) {
				isPromptUser = ((ApkDetailActivity) context).is3GDownloadPromptUser();
			} else if (context instanceof DownloadActivity) {
				isPromptUser = ((DownloadActivity) context).is3GDownloadPromptUser();
			} else {
				isPromptUser = ((PublicActivity) context).is3GDownloadPromptUser();
			}
			if (isPromptUser) {
				if (DownloadService.mDownloadService != null) {
					if (DownloadService.mDownloadService.canUseGprsDownload()) {
						context.sendBroadcast(new Intent(BROADCAST_ACTION_ONEKEY_UPDATE));
					} else {
						Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
				}
			} else {
				showUseMobileGprsPromptDialog(context, null);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			// sendDownloadBroadcast(context, entity);
			context.sendBroadcast(new Intent(BROADCAST_ACTION_ONEKEY_UPDATE));
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	private static void sendDownloadBroadcast(Context context, DownloadEntity entity) {
		if (entity.getStatus() == STATUS_OF_INITIAL) {
			if (entity.downloadType == TYPE_OF_DOWNLOAD) {
				fillDownloadNotifycation(context, true);
			} else if (entity.downloadType == TYPE_OF_UPDATE) {
				fillUpdateAndUpdatingNotifycation(context, true);
			}
		}
		entity.setStatus(STATUS_OF_PREPARE);
		Intent intent = new Intent(BROADCAST_ACTION_ADD_DOWNLOAD);
		Bundle bundle = new Bundle();
		bundle.putParcelable(DOWNLOAD_ENTITY, entity);
		intent.putExtras(bundle);
		context.sendBroadcast(intent);
	}

	private static void showUseMobileGprsPromptDialog(final Context context, final DownloadEntity entity) {
		if (mUseGprsDialog == null) {
			mUseGprsDialog = new CustomNoTitleDialog(context);
			mUseGprsDialog.setMessage(R.string.cellular_download_prompt_msg);
			mUseGprsDialog.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mUseGprsDialog.dismiss();
					if (DownloadService.mDownloadService != null) {
						if (DownloadService.mDownloadService.canUseGprsDownload()) { // 能否继续使用
																						// Gprs
																						// 流量下载
							if (entity != null) {
								sendDownloadBroadcast(context, entity);
							} else {
								checkOneKeyDownload(context, null);
							}
						} else {
							Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
							context.sendBroadcast(intent);
						}
					}
				}
			});
			mUseGprsDialog.setNegativeButton(context.getString(R.string.cancel), null);
			if (context instanceof ApkDetailActivity) {
				((ApkDetailActivity) context).set3GDownloadPromptUser();
			} else if (context instanceof DownloadActivity) {
				((DownloadActivity) context).set3GDownloadPromptUser();
			} else {
				((PublicActivity) context).set3GDownloadPromptUser();
			}
		}
		if (!((Activity) context).isFinishing() && !mUseGprsDialog.isShowing()) {
			mUseGprsDialog.show();
		}
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
			if (!DJMarketUtils.isOnlyWifi(context)) {
				return STATUS_ONLY_MOBILE;
			} else {
				return STATUS_NOT_SETTING_MOBILE_DOWNLOAD;
			}
		}
		return STATUS_CAN_DOWNLOAD;
	}

	public static void startAllDownload(Context context, boolean isPromptUser) {
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
			if (isPromptUser) {
				if (DownloadService.mDownloadService != null) {
					if (DownloadService.mDownloadService.canUseGprsDownload()) {
						Intent intent = new Intent(BROADCAST_ACTION_START_ALL_DOWNLOAD);
						context.sendBroadcast(intent);
					} else {
						Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
				}
			} else {
				showUseMobileGprsPromptDialog2(context);

			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			Intent intent = new Intent(BROADCAST_ACTION_START_ALL_DOWNLOAD);
			context.sendBroadcast(intent);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	private static void showUseMobileGprsPromptDialog2(final Context context) {
		if (mUseGprsDialog2 == null) {
			mUseGprsDialog2 = new CustomNoTitleDialog(context);
			mUseGprsDialog2.setMessage(R.string.cellular_download_prompt_msg);
			mUseGprsDialog2.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mUseGprsDialog2.dismiss();
					if (DownloadService.mDownloadService != null) {
						if (DownloadService.mDownloadService.canUseGprsDownload()) { // 能否继续使用
																						// Gprs
																						// 流量下载
							Intent intent = new Intent(BROADCAST_ACTION_START_ALL_DOWNLOAD);
							context.sendBroadcast(intent);
						} else {
							Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
							context.sendBroadcast(intent);
						}
					}
				}
			});
			mUseGprsDialog2.setNegativeButton(context.getString(R.string.cancel), null);
		}
		if (!((Activity) context).isFinishing() && !mUseGprsDialog2.isShowing()) {
			mUseGprsDialog2.show();
		}
	}

	public static void checkCloudRestore(Context context, ArrayList<ApkItem> items) {
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
				if (DownloadService.mDownloadService != null) {
					if (DownloadService.mDownloadService.canUseGprsDownload()) {
						Intent intent = new Intent(BROADCAST_ACTION_CLOUD_RESTORE);
						Bundle bundle = new Bundle();
						bundle.putParcelableArrayList("cloudList", items);
						intent.putExtras(bundle);
						context.sendBroadcast(intent);
					} else {
						Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
						context.sendBroadcast(intent);
					}
				}
			} else {
				showUseMobileGprsPromptDialog3(context, items);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD:
			Intent intent = new Intent(BROADCAST_ACTION_CLOUD_RESTORE);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("cloudList", items);
			intent.putExtras(bundle);
			context.sendBroadcast(intent);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			AndroidUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	private static void showUseMobileGprsPromptDialog3(final Context context, final ArrayList<ApkItem> items) {
		if (mUseGprsDialog == null) {
			mUseGprsDialog = new CustomNoTitleDialog(context);
			mUseGprsDialog.setMessage(R.string.cellular_download_prompt_msg);
			mUseGprsDialog.setNeutralButton(context.getString(R.string.prompt_download), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mUseGprsDialog.dismiss();
					if (DownloadService.mDownloadService != null) {
						if (DownloadService.mDownloadService.canUseGprsDownload()) { // 能否继续使用
																						// Gprs
																						// 流量下载
							Intent intent = new Intent(BROADCAST_ACTION_CLOUD_RESTORE);
							Bundle bundle = new Bundle();
							bundle.putParcelableArrayList("cloudList", items);
							intent.putExtras(bundle);
							context.sendBroadcast(intent);
						} else {
							Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);
							context.sendBroadcast(intent);
						}
					}
				}
			});
			mUseGprsDialog.setNegativeButton(context.getString(R.string.cancel), null);
			((SoftwareManageActivity) context).set3GDownloadPromptUser();
		}
		if (!((Activity) context).isFinishing() && !mUseGprsDialog.isShowing()) {
			mUseGprsDialog.show();
		}
	}

	/**
	 * 验证下载的 apk 文件是否正确
	 * 
	 * @param apkPath
	 * @return
	 */
	public static boolean checkApkFile(String apkPath) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
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
			return pkgParserPkg != null;
		} catch (ClassNotFoundException e) {

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 解析包名
	 * 
	 * @param str
	 * @return
	 */
	public static String parsePackageName(String str) {
		final String packageStr = "package:";
		int i = str.indexOf(packageStr);
		if (i < str.length()) {
			return str.substring(i + packageStr.length(), str.length());
		}
		return null;
	}

	/**
	 * 安装 apk 文件
	 * 
	 * @param context
	 * @param path
	 */
	public static void installApk(Context context, String path) {
		File file = new File(path);
		if (file.exists()) {
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			context.startActivity(installIntent);
		}
	}

	/**
	 * 显示标题栏正在下载的数目
	 * 
	 * @param context
	 */
	public static void fillDownloadNotifycation(Context context, boolean isAdd) {
		int count = 0;
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_DOWNLOAD) {
					count++;
				}
			}
			if (isAdd) {
				count++;
			} else {
				count--;
			}
			if (count > 0) {
				NetTool.setNotification(context, 1, count);
			} else {
				NetTool.cancelNotification(context, 1);
			}
		}
	}

	public static void fillUpdateNotifycation(Context context) {
		int count = 0;
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_UPDATE) {
					count++;
				}
			}
			if (count > 0) {
				NetTool.setNotification(context, 2, count);
			} else {
				NetTool.cancelNotification(context, 2);
			}
		}
	}

	/**
	 * 显示标题栏可更新数目
	 * 
	 * @param context
	 * @param downloadList
	 */
	public static void fillUpdateNotifycation(Context context, List<DownloadEntity> downloadList) {
		int count = 0;
		for (int i = 0; i < downloadList.size(); i++) {
			DownloadEntity entity = downloadList.get(i);
			if (entity.downloadType == TYPE_OF_UPDATE && entity.getStatus() == STATUS_OF_INITIAL) {
				count++;
			}
		}
		if (count > 0) {
			NetTool.setNotification(context, 2, count);
		} else {
			NetTool.cancelNotification(context, 2);
		}
	}

	/**
	 * 
	 * @param context
	 * @param downloadList
	 */
	public static void fillUpdateAndUpdatingNotifycation(Context context, boolean isAdd) {
		int updateCount = 0;
		int updatingCount = 0;
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_UPDATE) {
					switch (entity.getStatus()) {
					case STATUS_OF_INITIAL:
						updateCount++;
						break;
					default:
						updatingCount++;
						break;
					}
				}
			}
			if (isAdd) {
				updateCount--;
				updatingCount++;
			} else {
				updateCount++;
				updatingCount--;
			}
			if (updateCount > 0) {
				NetTool.setNotification(context, 2, updateCount);
			} else {
				NetTool.cancelNotification(context, 2);
			}
			if (updatingCount > 0) {
				NetTool.setNotification(context, 3, updatingCount);
			} else {
				NetTool.cancelNotification(context, 3);
			}
		}
	}

	public static void fillAll(Context context) {
		int updateCount = 0;
		int updatingCount = 0;
		int downloadCount = 0;
		int completeCount = 0;
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_UPDATE) {
					switch (entity.getStatus()) {
					case STATUS_OF_INITIAL:
						updateCount++;
						break;
					default:
						updatingCount++;
						break;
					}
				} else if (entity.downloadType == TYPE_OF_DOWNLOAD) {
					downloadCount++;
				} else if (entity.downloadType == TYPE_OF_COMPLETE) {
					completeCount++;
				}
			}
			if (downloadCount > 0) {
				NetTool.setNotification(context, 1, downloadCount);
			} else {
				NetTool.cancelNotification(context, 1);
			}
			if (updateCount > 0) {
				NetTool.setNotification(context, 2, updateCount);
			} else {
				NetTool.cancelNotification(context, 2);
			}
			if (updatingCount > 0) {
				NetTool.setNotification(context, 3, updatingCount);
			} else {
				NetTool.cancelNotification(context, 3);
			}
			if (completeCount > 0) {
				NetTool.setNotification(context, 5, completeCount);
			} else {
				NetTool.cancelNotification(context, 5);
			}
		}
	}

	public static void fillWaitInstallNotifycation(Context context) {
		int count = 0;
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_COMPLETE) {
					count++;
				}
			}
			count--;
			if (count > 0) {
				NetTool.setNotification(context, 5, count);
			} else {
				NetTool.cancelNotification(context, 5);
			}
		}
	}

	/**
	 * 
	 * @param entity
	 */
	public static void setInstallDownloadEntity(Context context, DownloadEntity entity) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packageInfo = pm.getPackageInfo(entity.packageName, PackageManager.GET_ACTIVITIES);
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			entity.installedIcon = appInfo.loadIcon(pm);
			entity.installedVersionName = packageInfo.versionName;
			String dir = appInfo.publicSourceDir;
			entity.installedFileLength = (new File(dir).length());
		} catch (NameNotFoundException e) {
		}
	}
}
