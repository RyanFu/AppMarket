package com.dongji.market.helper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

import com.dongji.market.R;
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.activity.DownloadActivity;
import com.dongji.market.activity.PublicActivity;
import com.dongji.market.activity.SoftwareManageActivity;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.DownloadEntity;
import com.dongji.market.service.DownloadService;
import com.dongji.market.widget.CustomNoTitleDialog;

public class DownloadUtils implements AConstDefine {
	public static final int STATUS_CAN_DOWNLOAD = 0; // 可以下载
	public static final int STATUS_NOT_NETWORK = 1; // 没有网络
	public static final int STATUS_ONLY_MOBILE = 2; // 只有手机网络，无wifi
	public static final int STATUS_SDCARD_INSUFFICIENT = 3; // 下载空间不足
	public static final int STATUS_SETTING_FLOW_INSUFFICIENT = 4; // 设置流量不足以完成此次下载
	public static final int STATUS_NOT_SDCARD = 5; // 无SD卡
	public static final int STATUS_NOT_SETTING_MOBILE_DOWNLOAD = 6; // 未设置开启蜂窝下载

	private static CustomNoTitleDialog mUseGprsDialog;
	private static CustomNoTitleDialog mUseGprsDialog2;

	
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
	
	/**
	 * 检查下载
	 * @param context
	 * @param entity
	 * @param mTextView 
	 */
	public static void checkDownload(Context context, DownloadEntity entity) {
		int status = DJMarketUtils.isCanDownload(context);//获取下载前设备或者网络的状态
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK://无网络
			DJMarketUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD://无SD卡
			DJMarketUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT://下载空间不足
			DJMarketUtils.showToast(context, R.string.download_size_insufficient);
			break;
		case DJMarketUtils.STATUS_ONLY_MOBILE://只有手机网络，无wifi
			boolean isPromptUser = false;
			if (context instanceof ApkDetailActivity) {
				isPromptUser = ((ApkDetailActivity) context).is3GDownloadPromptUser();//是否已经进行3g下载提示
			} else if (context instanceof DownloadActivity) {
				isPromptUser = ((DownloadActivity) context).is3GDownloadPromptUser();
			} else {
				isPromptUser = ((PublicActivity) context).is3GDownloadPromptUser();
			}
			if (isPromptUser) {//已经提示
				if (DownloadService.mDownloadService != null) {
					if (DownloadService.mDownloadService.canUseGprsDownload()) {//是否可以使用gprs下载
						sendDownloadBroadcast(context, entity);//发送下载广播
					} else {
						Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_NOFLOW);//发送无流量广播
						context.sendBroadcast(intent);
					}
				}
			} else {
				showUseMobileGprsPromptDialog(context, entity);
			}
			break;
		case DJMarketUtils.STATUS_CAN_DOWNLOAD://可以下载
			sendDownloadBroadcast(context, entity);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD://未设置开启蜂窝下载
			DJMarketUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}
	
	/**
	 * 发送下载广播
	 * @param context
	 * @param entity
	 */
	private static void sendDownloadBroadcast(Context context, DownloadEntity entity) {
		if (entity.getStatus() == STATUS_OF_INITIAL) {//处于下载初始化状态
			if (entity.downloadType == TYPE_OF_DOWNLOAD) {//类型为下载类型
				fillDownloadNotifycation(context, true);//通知栏更新下载数
			} else if (entity.downloadType == TYPE_OF_UPDATE) {
				fillUpdateAndUpdatingNotifycation(context, true);//通知栏更新更新数
			}
		}
		entity.setStatus(STATUS_OF_PREPARE);//设置状态
		Intent intent = new Intent(BROADCAST_ACTION_ADD_DOWNLOAD);//发送添加下载广播
		Bundle bundle = new Bundle();
		bundle.putParcelable(DOWNLOAD_ENTITY, entity);
		intent.putExtras(bundle);
		context.sendBroadcast(intent);
	}
	
	
	/**
	 * 显示标题栏正在下载的数目
	 * 
	 * @param context
	 */
	public static void fillDownloadNotifycation(Context context, boolean isAdd) {
		int count = 0;
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();//获取下载列表
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_DOWNLOAD) {//类型为下载类型
					count++;
				}
			}
			if (isAdd) {//新添下载
				count++;
			} else {//下载完成
				count--;
			}
			if (count > 0) {//通知栏提示
				NetTool.setNotification(context, 1, count);
			} else {//取消提示
				NetTool.cancelNotification(context, 1);
			}
		}
	}

	/**
	 * 检查下载
	 * @param context
	 * @param apkItem
	 * @param mTextView
	 */
	public static void checkDownload(Context context, ApkItem apkItem) {
		DownloadEntity entity = new DownloadEntity(apkItem);//将apkItem转成下载实体，它们之间主要是一个状态和下载类型的对应关系，但是它们的都有一个状态值，不过含义不同，downloadentity状态指的是下载中各个状态，而apkitem的状态是指已安装、未安装，已更新、未更新
		if (null != DownloadService.mDownloadService) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();//获取下载列表
			int i = 0;
			for (; i < downloadList.size(); i++) {
				DownloadEntity d = downloadList.get(i);
				if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
					if (d.downloadType == TYPE_OF_COMPLETE) {//下载类型为已完成类型
						String path = DOWNLOAD_ROOT_PATH + d.hashCode() + DOWNLOAD_FILE_POST_SUFFIX;
						File file = new File(path);
						if (file.exists()) {//apk文件是否存在
							installApk(context, path);//安装apk
						} else {
							Intent removeIntent = new Intent(BROADCAST_ACTION_REMOVE_DOWNLOAD);//发送删除广播
							Bundle removeBundle = new Bundle();
							removeBundle.putParcelable(DOWNLOAD_ENTITY, entity);
							removeIntent.putExtras(removeBundle);
							context.sendBroadcast(removeIntent);
							checkDownload(context, entity);
						}
					} else {
						checkDownload(context, entity);
					}
					break;
				}
			}
			if (i == downloadList.size()) {
				checkDownload(context, entity);
			}
		} else {
			checkDownload(context, entity);
		}
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
	 * 检查一键更新下载
	 * @param context
	 * @param entity
	 */
	public static void checkOneKeyDownload(Context context, DownloadEntity entity) {
		int status = DJMarketUtils.isCanDownload(context);
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK:
			DJMarketUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			DJMarketUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			DJMarketUtils.showToast(context, R.string.download_size_insufficient);
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
			context.sendBroadcast(new Intent(BROADCAST_ACTION_ONEKEY_UPDATE));
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			DJMarketUtils.showToast(context, R.string.setting_for_cellular_close);
			break;
		}
	}

	/**
	 * 显示用户手机gprs提示框
	 * @param context
	 * @param entity
	 */
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
		if (!DJMarketUtils.isNetworkAvailable(context)) {
			return STATUS_NOT_NETWORK;
		} else if (!DJMarketUtils.isSdcardExists()) {
			return STATUS_NOT_SDCARD;
		} else if (DJMarketUtils.getSdcardAvalilaleSize() / 1024 / 1024 < 256) {
			return STATUS_SDCARD_INSUFFICIENT;
		} else if (!DJMarketUtils.isWifiAvailable(context) && DJMarketUtils.isMobileAvailable(context)) {
			if (!DJMarketUtils.isOnlyWifi(context)) {
				return STATUS_ONLY_MOBILE;
			} else {
				return STATUS_NOT_SETTING_MOBILE_DOWNLOAD;
			}
		}
		return STATUS_CAN_DOWNLOAD;
	}

	/**
	 * 开始下载所有
	 * @param context
	 * @param isPromptUser
	 */
	public static void startAllDownload(Context context, boolean isPromptUser) {
		int status = DJMarketUtils.isCanDownload(context);
		switch (status) {
		case DJMarketUtils.STATUS_NOT_NETWORK:
			DJMarketUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			DJMarketUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			DJMarketUtils.showToast(context, R.string.download_size_insufficient);
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
			DJMarketUtils.showToast(context, R.string.setting_for_cellular_close);
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
			DJMarketUtils.showToast(context, R.string.no_network_msg1);
			break;
		case DJMarketUtils.STATUS_NOT_SDCARD:
			DJMarketUtils.showToast(context, R.string.no_sdcard_msg);
			break;
		case DJMarketUtils.STATUS_SDCARD_INSUFFICIENT:
			DJMarketUtils.showToast(context, R.string.download_size_insufficient);
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
			Intent intent = new Intent(BROADCAST_ACTION_CLOUD_RESTORE);//发送云恢复广播
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("cloudList", items);
			intent.putExtras(bundle);
			context.sendBroadcast(intent);
			break;
		case DJMarketUtils.STATUS_NOT_SETTING_MOBILE_DOWNLOAD:
			DJMarketUtils.showToast(context, R.string.setting_for_cellular_close);
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
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
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
