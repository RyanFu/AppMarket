package com.dongji.market.activity;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.dongji.market.application.AppMarket;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.ADownloadApkDBHelper;
import com.dongji.market.download.ADownloadApkItem;
import com.dongji.market.download.ADownloadApkList;
import com.dongji.market.download.ADownloadService;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadService;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.HistoryApkItem;
import com.umeng.analytics.MobclickAgent;

public abstract class PublicActivity extends Activity {
	protected final static int INSTALL_APP_DONE = 1;
	protected final static int UNINSTALL_APP_DONE = 2;
	protected List<ADownloadApkItem> downloadList;

	private ApkStatusReceiver mReceiver;
	private static final String PACKAGE_STR = "package:";
	private boolean isEconomizeTraffic; // 当前是否为浏览图片省流量模式

	protected AppMarket mApp;

	protected boolean isRemoteImage = true; // 是否允许请求网络图片

	/**
	 * 软件安装或卸载完成
	 * 
	 * @param info
	 */
	public abstract void onAppInstallOrUninstallDone(int status, PackageInfo info);

	/**
	 * 当软件取消下载或更新的时候
	 * 
	 * @param appId
	 */
	public abstract void onAppStatusChange(boolean isCancel, String packageName, int versionCode);

	protected abstract void onUpdateDataDone();

	protected abstract void loadingImage();

	private void registerAllReceiver() {
		mReceiver = new ApkStatusReceiver();
		IntentFilter intentFilter = new IntentFilter();
		// intentFilter.addAction(AConstDefine.BROADCAST_ACTION_DOWNLOAD); //
		// 正在下载广播
		// intentFilter.addAction(AConstDefine.BROADCAST_ACTION_UPDATE); //
		// 正在更新广播
		// intentFilter.addAction(AConstDefine.BROADCAST_UPDATE_DATA_REFRESH);
		// // 软件更新数据获取后将发送此广播
		// intentFilter.addAction(AConstDefine.SAVE_FLOW_BROADCAST); //
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_ADD_DOWNLOAD);
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_COMPLETE_DOWNLOAD);
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE);
		intentFilter.addAction(AConstDefine.SAVE_FLOW_BROADCAST);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		IntentFilter packageFilter = new IntentFilter();
		packageFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL);
		packageFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE);
		packageFilter.addDataScheme("package");
		registerReceiver(mReceiver, intentFilter);
		registerReceiver(mPackageStatusReceiver, packageFilter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (getParent() == null) {
			MobclickAgent.onResume(this);
		}

		isEconomizeTraffic = mApp.isRemoteImage();

		System.out.println("===========isEconomizeTraffic" + isEconomizeTraffic);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (getParent() == null) {
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		if (mPackageStatusReceiver != null) {
			unregisterReceiver(mPackageStatusReceiver);
		}
	}

	/**
	 * 查询下载或更新数据
	 */
	protected void initDownloadAndUpdateData() {
		ADownloadApkDBHelper db = new ADownloadApkDBHelper(this);
		ADownloadApkList list = db.queryAllApkStatus();
		if (list != null) {
			downloadList = list.apkList;
		}
	}

	/**
	 * 每次数据请求都需要做以下判断: 1.如果处于正在下载或正在更新的状态，则需要判断其.temp文件是否存在
	 * 2.如果处在下载完成或更新完成的状态，则需判断其.apk文件是否存在 3.需要和手机上已安装文件做比较 4.需要和更新列表的文件做比较
	 * 
	 * @param items
	 * @return
	 */
	/*
	 * protected List<ApkItem> setApkStatus(List<ApkItem> items) { if (items !=
	 * null && items.size() > 0) { ADownloadApkDBHelper db=new
	 * ADownloadApkDBHelper(this); // 获取手机上已安装的应用 List<PackageInfo>
	 * infos=AndroidUtils.getInstalledPackages(this); // 获取安装完毕的应用列表
	 * ADownloadApkList list=db.selectApkByStatus(new
	 * int[]{AConstDefine.STATUS_OF_DOWNLOADCOMPLETE,
	 * AConstDefine.STATUS_OF_UPDATECOMPLETE}); ArrayList<ApkItem>
	 * updateList=mApp.getUpdateList(); // 获取当前下载列表 List<ADownloadApkItem>
	 * downloadingList=ADownloadService.downloadingAPKList.apkList; // 获取当前更新列表
	 * List<ADownloadApkItem>
	 * updatingList=ADownloadService.updateAPKList.apkList; for(int
	 * i=0;i<items.size();i++) { if(downloadingList.size()>0) { for (int j = 0;
	 * j < downloadingList.size(); j++) { ADownloadApkItem downloadItem =
	 * downloadingList.get(j); if (items.get(i).packageName
	 * .equals(downloadItem.apkPackageName) &&
	 * items.get(i).versionCode==downloadItem.apkVersionCode) { //
	 * 当此应用处于下载中或下载被动暂停的状态下时，则显示为下载状态 if (downloadItem.apkStatus ==
	 * AConstDefine.STATUS_OF_DOWNLOADING || downloadItem.apkStatus ==
	 * AConstDefine.STATUS_OF_PAUSE) { boolean flag = AndroidUtils
	 * .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * downloadItem.apkPackageName + "_" + downloadItem.apkVersionCode +
	 * ".apk.temp"); if (flag) { items.get(i).status =
	 * AConstDefine.STATUS_APK_INSTALL; } else { items.get(i).status =
	 * AConstDefine.STATUS_APK_UNINSTALL; db.deleteDownloadByPAndV(
	 * items.get(i).packageName, items.get(i).versionCode); //
	 * ADownloadService.downloadingAPKList.apkList.remove(j--); } break; } else
	 * if (downloadItem.apkStatus == AConstDefine.STATUS_OF_DOWNLOADCOMPLETE) {
	 * // 当此应用为下载完成的状态，但本地文件并不存在，则可能是用户将此文件手动删除了，那么此应用为未安装状态 boolean flag =
	 * AndroidUtils .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * downloadItem.apkPackageName + "_" + downloadItem.apkVersionCode +
	 * ".apk"); if (!flag) { db.deleteDownloadByPAndV( items.get(i).packageName,
	 * items.get(i).versionCode); } items.get(i).status =
	 * AConstDefine.STATUS_APK_UNINSTALL; break; } else
	 * if(downloadItem.apkStatus==AConstDefine.STATUS_OF_PREPAREDOWNLOAD) { //
	 * 当此应用为等待中状态时，需显示为下载状态 items.get(i).status =
	 * AConstDefine.STATUS_APK_INSTALL; } else if(downloadItem.apkStatus ==
	 * AConstDefine.STATUS_OF_PAUSE_BYHAND) { //
	 * 当此应用为手动暂停状态时，需判断其是否已经开始下载了，如果已经下载则为下载状态，如果没有下载，则为为下载状态
	 * if(downloadItem.apkDownloadSize>0) { boolean flag = AndroidUtils
	 * .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * downloadItem.apkPackageName + "_" + downloadItem.apkVersionCode +
	 * ".apk.temp"); if (flag) { items.get(i).status =
	 * AConstDefine.STATUS_APK_INSTALL; } else { items.get(i).status =
	 * AConstDefine.STATUS_APK_UNINSTALL; db.deleteDownloadByPAndV(
	 * items.get(i).packageName, items.get(i).versionCode); //
	 * ADownloadService.downloadingAPKList.apkList.remove(j--); } break; } } } }
	 * }else { if(items.get(i).status==AConstDefine.STATUS_APK_INSTALL) {
	 * items.get(i).status=AConstDefine.STATUS_APK_UNINSTALL; } } if
	 * (updatingList.size() > 0) { for (int y = 0; y < updatingList.size(); y++)
	 * { ADownloadApkItem updateItem = updatingList.get(y); if
	 * (items.get(i).packageName .equals(updateItem.apkPackageName) &&
	 * items.get(i).versionCode==updateItem.apkVersionCode) { if
	 * (updateItem.apkStatus == AConstDefine.STATUS_OF_UPDATEING ||
	 * updateItem.apkStatus == AConstDefine.STATUS_OF_PAUSEUPDATE) { boolean
	 * flag = AndroidUtils .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * updateItem.apkPackageName + "_" + updateItem.apkVersionCode +
	 * ".apk.temp"); if (flag) { items.get(i).status =
	 * AConstDefine.STATUS_APK_UPDATE; } else { items.get(i).status =
	 * AConstDefine.STATUS_APK_UNUPDATE; db.deleteDownloadByPAndV(
	 * items.get(i).packageName, items.get(i).versionCode); //
	 * ADownloadService.updateAPKList.apkList.remove(y--); } break; } else if
	 * (updateItem.apkStatus == AConstDefine.STATUS_OF_UPDATECOMPLETE) { boolean
	 * flag = AndroidUtils .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * updateItem.apkPackageName + "_" + updateItem.apkVersionCode + ".apk");
	 * if(!flag) { db.deleteDownloadByPAndV( items.get(i).packageName,
	 * items.get(i).versionCode); } items.get(i).status =
	 * AConstDefine.STATUS_APK_UNUPDATE; break; } else if(updateItem.apkStatus
	 * == AConstDefine.STATUS_OF_PAUSEUPDATE_BYHAND) {
	 * if(updateItem.apkDownloadSize>0) { boolean flag = AndroidUtils
	 * .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * updateItem.apkPackageName + "_" + updateItem.apkVersionCode +
	 * ".apk.temp"); if (flag) { items.get(i).status =
	 * AConstDefine.STATUS_APK_UPDATE; } else { items.get(i).status =
	 * AConstDefine.STATUS_APK_UNUPDATE; db.deleteDownloadByPAndV(
	 * items.get(i).packageName, items.get(i).versionCode); } break; } }else
	 * if(updateItem.apkStatus==AConstDefine.STATUS_OF_PREPAREUPDATE) { //
	 * 当此应用为等待中状态时，需显示为下载状态 items.get(i).status =
	 * AConstDefine.STATUS_APK_UPDATE; }else
	 * if(updateItem.apkStatus==AConstDefine.STATUS_OF_UPDATE) {
	 * items.get(i).status = AConstDefine.STATUS_APK_UNUPDATE; break; } } } }
	 * if(list.apkList!=null && list.apkList.size()>0) { for(int
	 * z=0;z<list.apkList.size();z++) { // 查看下载完成列表中有无此应用 if
	 * (items.get(i).packageName .equals(list.apkList.get(z).apkPackageName) &&
	 * items.get(i).versionCode == list.apkList .get(z).apkVersionCode) {
	 * boolean flag = AndroidUtils .checkFileExists(AndroidUtils.cachePath +
	 * "apk/" + list.apkList.get(z).apkPackageName + "_" +
	 * list.apkList.get(z).apkVersionCode + ".apk");
	 * if(list.apkList.get(z).apkStatus
	 * ==AConstDefine.STATUS_OF_DOWNLOADCOMPLETE) { if(!flag) {
	 * db.deleteDownloadByPAndV( items.get(i).packageName,
	 * items.get(i).versionCode); } items.get(i).status =
	 * AConstDefine.STATUS_APK_UNINSTALL; }else { if(!flag) {
	 * db.deleteDownloadByPAndV( items.get(i).packageName,
	 * items.get(i).versionCode); } items.get(i).status =
	 * AConstDefine.STATUS_APK_UNUPDATE; } } } } // 查看手机已安装应用中有无此应用
	 * if(infos!=null && infos.size()>0) { for(int k=0;k<infos.size();k++) {
	 * PackageInfo info=infos.get(k); if
	 * (info.packageName.equals(items.get(i).packageName) && info.versionCode >=
	 * items.get(i).versionCode) { items.get(i).status =
	 * AConstDefine.STATUS_APK_INSTALL_DONE; break; } } } } } return items; }
	 */

	protected List<ApkItem> setApkStatus(List<ApkItem> items) {
		if (items != null && items.size() > 0) {
			// 获取手机上已安装的应用
			List<PackageInfo> infos = AndroidUtils.getInstalledPackages(this);
			for (int i = 0; i < items.size(); i++) {
				ApkItem item = items.get(i);
				if (DownloadService.mDownloadService != null) {
					List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
					for (int j = 0; j < downloadList.size(); j++) {
						DownloadEntity entity = downloadList.get(j);
						if (item.packageName.equals(entity.packageName) && item.versionCode == entity.versionCode) {
							switch (entity.downloadType) {
							case DownloadConstDefine.TYPE_OF_DOWNLOAD:
								items.get(i).status = AConstDefine.STATUS_APK_INSTALL;
								break;
							case DownloadConstDefine.TYPE_OF_UPDATE:
								if (entity.getStatus() != DownloadConstDefine.STATUS_OF_INITIAL && entity.getStatus() != DownloadConstDefine.STATUS_OF_IGNORE) {
									items.get(i).status = AConstDefine.STATUS_APK_UPDATE;
								} else {
									items.get(i).status = AConstDefine.STATUS_APK_UNUPDATE;
								}
								break;
							case DownloadConstDefine.TYPE_OF_COMPLETE:
								if (item.status == AConstDefine.STATUS_APK_INSTALL) {
									items.get(i).status = AConstDefine.STATUS_APK_UNINSTALL;
								} else if (item.status == AConstDefine.STATUS_APK_UPDATE) {
									items.get(i).status = AConstDefine.STATUS_APK_UNUPDATE;
								}
								break;
							}
						}
					}
				}
				// 查看手机已安装应用中有无此应用
				if (infos != null && infos.size() > 0) {
					for (int k = 0; k < infos.size(); k++) {
						PackageInfo info = infos.get(k);
						if (info.packageName.equals(items.get(i).packageName) && info.versionCode >= items.get(i).versionCode) {
							items.get(i).status = AConstDefine.STATUS_APK_INSTALL_DONE;
							break;
						}
					}
				}
			}
		}
		return items;
	}

	/*
	 * protected ApkItem setApkStatus(ApkItem item) { if (item != null) {
	 * ADownloadApkDBHelper db = new ADownloadApkDBHelper(this);
	 * List<PackageInfo> infos = AndroidUtils.getInstalledPackages(this);
	 * ADownloadApkList list = db.selectApkByStatus(new int[] {
	 * AConstDefine.STATUS_OF_DOWNLOADCOMPLETE,
	 * AConstDefine.STATUS_OF_UPDATECOMPLETE }); ArrayList<ApkItem> updateList =
	 * mApp.getUpdateList(); List<ADownloadApkItem> downloadingList =
	 * ADownloadService.downloadingAPKList.apkList; List<ADownloadApkItem>
	 * updatingList = ADownloadService.updateAPKList.apkList; if
	 * (downloadingList.size() > 0) { for (int j = 0; j <
	 * downloadingList.size(); j++) { ADownloadApkItem downloadItem =
	 * downloadingList.get(j); if
	 * (item.packageName.equals(downloadItem.apkPackageName) &&
	 * item.versionCode==downloadItem.apkVersionCode) { if
	 * (downloadItem.apkStatus == AConstDefine.STATUS_OF_DOWNLOADING ||
	 * downloadItem.apkStatus == AConstDefine.STATUS_OF_PAUSE) { boolean flag =
	 * AndroidUtils .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * downloadItem.apkPackageName + "_" + downloadItem.apkVersionCode +
	 * ".apk.temp");
	 * 
	 * if (!flag) { db.deleteDownloadByPAndV( downloadItem.apkPackageName,
	 * downloadItem.apkVersionCode); item.status =
	 * AConstDefine.STATUS_APK_UNINSTALL; }else { item.status =
	 * AConstDefine.STATUS_APK_INSTALL; } break; } else if
	 * (downloadItem.apkStatus == AConstDefine.STATUS_OF_DOWNLOADCOMPLETE) {
	 * boolean flag = AndroidUtils .checkFileExists(AndroidUtils.cachePath +
	 * "apk/" +downloadItem.apkPackageName+"_"+downloadItem.apkVersionCode +
	 * ".apk"); if (!flag) { db.deleteDownloadByPAndV(
	 * downloadItem.apkPackageName, downloadItem.apkVersionCode); item.status =
	 * AConstDefine.STATUS_APK_UNINSTALL; } break; } else if
	 * (downloadItem.apkStatus == AConstDefine.STATUS_OF_PREPAREDOWNLOAD) {
	 * item.status = AConstDefine.STATUS_APK_INSTALL; break; } else
	 * if(downloadItem.apkStatus == AConstDefine.STATUS_OF_PAUSE_BYHAND) { //
	 * 当此应用为手动暂停状态时，需判断其是否已经开始下载了，如果已经下载则为下载状态，如果没有下载，则为为下载状态
	 * if(downloadItem.apkDownloadSize>0) { boolean flag = AndroidUtils
	 * .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * downloadItem.apkPackageName + "_" + downloadItem.apkVersionCode +
	 * ".apk.temp"); if (flag) { item.status = AConstDefine.STATUS_APK_INSTALL;
	 * } else { item.status = AConstDefine.STATUS_APK_UNINSTALL;
	 * db.deleteDownloadByPAndV( item.packageName, item.versionCode); //
	 * ADownloadService.downloadingAPKList.apkList.remove(j--); } break; } } } }
	 * } else { if (item.status == AConstDefine.STATUS_APK_INSTALL) {
	 * item.status = AConstDefine.STATUS_APK_UNINSTALL; } } if
	 * (updatingList.size() > 0) { for (int y = 0; y < updatingList.size(); y++)
	 * { ADownloadApkItem updateItem = updatingList.get(y); if
	 * (item.packageName.equals(updateItem.apkPackageName) &&
	 * item.versionCode==updateItem.apkVersionCode) { if (updateItem.apkStatus
	 * == AConstDefine.STATUS_OF_UPDATEING || updateItem.apkStatus ==
	 * AConstDefine.STATUS_OF_PAUSEUPDATE) { boolean flag = AndroidUtils
	 * .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * updateItem.apkPackageName+"_"+updateItem.apkVersionCode + ".apk.temp");
	 * if (flag) { item.status = AConstDefine.STATUS_APK_UPDATE; } else {
	 * item.status = AConstDefine.STATUS_APK_UNUPDATE; db.deleteDownloadByPAndV(
	 * updateItem.apkPackageName, updateItem.apkVersionCode);
	 * ADownloadService.updateAPKList.apkList .remove(y--); } break; } else if
	 * (updateItem.apkStatus == AConstDefine.STATUS_OF_UPDATECOMPLETE) { boolean
	 * flag = AndroidUtils .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * updateItem.apkPackageName+"_"+updateItem.apkVersionCode + ".apk"); if
	 * (!flag) { db.deleteDownloadByPAndV( updateItem.apkPackageName,
	 * updateItem.apkVersionCode); ADownloadService.updateAPKList.apkList
	 * .remove(y--); } item.status = AConstDefine.STATUS_APK_UNUPDATE; break; }
	 * else if(updateItem.apkStatus ==
	 * AConstDefine.STATUS_OF_PAUSEUPDATE_BYHAND) {
	 * if(updateItem.apkDownloadSize>0) { boolean flag = AndroidUtils
	 * .checkFileExists(AndroidUtils.cachePath + "apk/" +
	 * updateItem.apkPackageName + "_" + updateItem.apkVersionCode +
	 * ".apk.temp"); if (flag) { item.status = AConstDefine.STATUS_APK_UPDATE; }
	 * else { item.status = AConstDefine.STATUS_APK_UNUPDATE;
	 * db.deleteDownloadByPAndV(item.packageName, item.versionCode); } break; }
	 * } else if (updateItem.apkStatus == AConstDefine.STATUS_OF_PREPAREUPDATE)
	 * { item.status = AConstDefine.STATUS_APK_UPDATE; break; } else
	 * if(updateItem.apkStatus==AConstDefine.STATUS_OF_UPDATE) { item.status =
	 * AConstDefine.STATUS_APK_UNUPDATE; break; } } } } if(list.apkList!=null &&
	 * list.apkList.size()>0) { for(int z=0;z<list.apkList.size();z++) {
	 * if(item.packageName .equals(list.apkList.get(z).apkPackageName) &&
	 * item.versionCode==list.apkList.get(z).apkVersionCode) { boolean flag =
	 * AndroidUtils .checkFileExists(AndroidUtils.cachePath + "apk/"
	 * +list.apkList
	 * .get(z).apkPackageName+"_"+list.apkList.get(z).apkVersionCode + ".apk");
	 * if
	 * (list.apkList.get(z).apkStatus==AConstDefine.STATUS_OF_DOWNLOADCOMPLETE)
	 * { if(!flag) { db.deleteDownloadByPAndV( item.packageName,
	 * item.versionCode); } item.status = AConstDefine.STATUS_APK_UNINSTALL;
	 * }else { if(!flag) { db.deleteDownloadByPAndV(item.packageName,
	 * item.versionCode); } item.status = AConstDefine.STATUS_APK_UNUPDATE; } }
	 * } } if (infos != null && infos.size() > 0) { for (int k = 0; k <
	 * infos.size(); k++) { PackageInfo info = infos.get(k); if
	 * (info.packageName.equals(item.packageName) && info.versionCode >=
	 * item.versionCode) { item.status = AConstDefine.STATUS_APK_INSTALL_DONE;
	 * break; } } } } return item; }
	 */

	protected ApkItem setApkStatus(ApkItem item) {
		if (item != null) {
			// 获取手机上已安装的应用
			List<PackageInfo> infos = AndroidUtils.getInstalledPackages(this);
			if (DownloadService.mDownloadService != null) {
				List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
				for (int j = 0; j < downloadList.size(); j++) {
					DownloadEntity entity = downloadList.get(j);
					if (null != entity && null != item && null != item.packageName && null != entity.packageName) {
						if (item.packageName.equals(entity.packageName) && item.versionCode == entity.versionCode) {
							switch (entity.downloadType) {
							case DownloadConstDefine.TYPE_OF_DOWNLOAD:
								item.status = AConstDefine.STATUS_APK_INSTALL;
								break;
							case DownloadConstDefine.TYPE_OF_UPDATE:
								if (entity.getStatus() != DownloadConstDefine.STATUS_OF_INITIAL && entity.getStatus() != DownloadConstDefine.STATUS_OF_IGNORE) {
									item.status = AConstDefine.STATUS_APK_UPDATE;
								} else {
									item.status = AConstDefine.STATUS_APK_UNUPDATE;
								}
								break;
							case DownloadConstDefine.TYPE_OF_COMPLETE:
								if (item.status == AConstDefine.STATUS_APK_INSTALL) {
									item.status = AConstDefine.STATUS_APK_UNINSTALL;
								} else if (item.status == AConstDefine.STATUS_APK_UPDATE) {
									item.status = AConstDefine.STATUS_APK_UNUPDATE;
								}
								break;
							}
							break;
						}
					}
				}
				if (item.historys != null && item.historys.length > 0) {
					for (int i = 0; i < item.historys.length; i++) {
						HistoryApkItem historyItem = item.historys[i];
						for (int j = 0; j < downloadList.size(); j++) {
							DownloadEntity entity = downloadList.get(j);
							PackageInfo info = AndroidUtils.getPackageInfo(this, item.packageName);
							if (null != entity && null != entity.packageName) {
								if (item.packageName.equals(entity.packageName)) {
									switch (entity.downloadType) {
									case DownloadConstDefine.TYPE_OF_DOWNLOAD:
										if (historyItem.versionCode == entity.versionCode) {
											if (info != null) {
												if (info.versionCode < historyItem.versionCode) {
													if (entity.getStatus() != DownloadConstDefine.STATUS_OF_INITIAL && entity.getStatus() != DownloadConstDefine.STATUS_OF_IGNORE) {
														historyItem.status = AConstDefine.STATUS_APK_UPDATE;
													} else {
														historyItem.status = AConstDefine.STATUS_APK_UNUPDATE;
													}
												} else {
													System.out.println("=============== TYPE_OF_DOWNLOAD");
													historyItem.status = AConstDefine.STATUS_APK_INSTALL_DONE;
												}
											}
										}
										break;
									case DownloadConstDefine.TYPE_OF_UPDATE:
										if (info != null) {
											if (info.versionCode < historyItem.versionCode) {
												if (entity.getStatus() != DownloadConstDefine.STATUS_OF_INITIAL && entity.getStatus() != DownloadConstDefine.STATUS_OF_IGNORE) {
													historyItem.status = AConstDefine.STATUS_APK_UPDATE;
												} else {
													historyItem.status = AConstDefine.STATUS_APK_UNUPDATE;
												}
											} else {
												System.out.println("============= TYPE_OF_UPDATE, " + historyItem.appName + ", " + historyItem.versionName + ", " + historyItem.versionCode + ", info_versionCode" + info.versionCode + ", " + historyItem.url);
												historyItem.status = AConstDefine.STATUS_APK_INSTALL_DONE;
											}
										}
										break;
									case DownloadConstDefine.TYPE_OF_COMPLETE:
										if (info != null) {
											if (info.versionCode == historyItem.versionCode) {
												if (historyItem.status == AConstDefine.STATUS_APK_INSTALL) {
													historyItem.status = AConstDefine.STATUS_APK_UNINSTALL;
												} else if (historyItem.status == AConstDefine.STATUS_APK_UPDATE) {
													historyItem.status = AConstDefine.STATUS_APK_UNUPDATE;
												}
											}
										}

										break;
									}
									// break;
								}
							}
						}
					}
				}
			}
			// 查看手机已安装应用中有无此应用
			if (infos != null && infos.size() > 0) {
				for (int k = 0; k < infos.size(); k++) {
					PackageInfo info = infos.get(k);
					if (info.packageName.equals(item.packageName) && info.versionCode >= item.versionCode) {
						item.status = AConstDefine.STATUS_APK_INSTALL_DONE;
						break;
					}
				}
			}
		}
		return item;
	}

	private class ApkStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * if (intent.getAction().equals(
			 * AConstDefine.BROADCAST_ACTION_DOWNLOAD)) { // int appId = 0;
			 * String apkSaveName; apkSaveName = intent.getStringExtra(
			 * AConstDefine.BROADCAST_CANCELDOWNLOAD); if (null!=apkSaveName) {
			 * initDownloadAndUpdateData(); onAppStatusChange(true,
			 * apkSaveName); } apkSaveName = intent.getStringExtra(
			 * AConstDefine.BROADCAST_STARTDOWNLOAD); if (null!=apkSaveName) {
			 * initDownloadAndUpdateData(); onAppStatusChange(false,
			 * apkSaveName); } } else if (intent.getAction().equals(
			 * AConstDefine.BROADCAST_ACTION_UPDATE)) { String apkSaveName =
			 * intent.getStringExtra( AConstDefine.BROADCAST_CANCELUPDATE); if
			 * (null!=apkSaveName) { initDownloadAndUpdateData();
			 * onAppStatusChange(true,apkSaveName); } apkSaveName =
			 * intent.getStringExtra(AConstDefine.BROADCAST_STARTUPDATE); if
			 * (null!=apkSaveName) { initDownloadAndUpdateData();
			 * onAppStatusChange(false, apkSaveName); } } else if
			 * (AConstDefine.BROADCAST_UPDATE_DATA_REFRESH.equals(intent
			 * .getAction())) { onUpdateDataDone(); } else
			 * if(AConstDefine.SAVE_FLOW_BROADCAST.equals(intent.getAction())) {
			 * isRemoteImage=!(intent.getBooleanExtra("save_flow_status",
			 * true)); }
			 */
			if (DownloadConstDefine.BROADCAST_ACTION_ADD_DOWNLOAD.equals(intent.getAction())) {//下载广播
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DownloadConstDefine.DOWNLOAD_ENTITY);
					if (entity != null) {
						onAppStatusChange(false, entity.packageName, entity.versionCode);
					}
				}
			} else if (DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD.equals(intent.getAction())) {//取消广播
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DownloadConstDefine.DOWNLOAD_ENTITY);
					if (entity != null) {
						onAppStatusChange(true, entity.packageName, entity.versionCode);//取消当前下载
					}
				}
			} else if (DownloadConstDefine.BROADCAST_ACTION_COMPLETE_DOWNLOAD.equals(intent.getAction())) {//下载完成广播
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DownloadConstDefine.DOWNLOAD_ENTITY);
					if (entity != null) {
						onAppStatusChange(true, entity.packageName, entity.versionCode);
					}
				}
			} else if (DownloadConstDefine.BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE.equals(intent.getAction())) {//更新数据合并广播
				onUpdateDataDone();//更新数据
			} else if (AConstDefine.SAVE_FLOW_BROADCAST.equals(intent.getAction())) {//节省流量广播
				isRemoteImage = !(intent.getBooleanExtra("save_flow_status", true));//是否开启流量
				System.out.println("isrempte:" + isRemoteImage);
			} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {//网络连接广播
				ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (wifiNetworkInfo.isAvailable() && wifiNetworkInfo.isConnected()) {//WIFI网络可用
					System.out.println("==========wifi isEconomizeTraffic:" + isEconomizeTraffic + ", isRemoteImage:" + isRemoteImage);
					if (isEconomizeTraffic) {
						isRemoteImage = true;
						loadingImage();
					}
				} else if (mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnected()) {//移动网络可用
					System.out.println("==========mobile isEconomizeTraffic:" + isEconomizeTraffic + ", isRemoteImage:" + isRemoteImage);
					if (isEconomizeTraffic) {//开启蜂窝数据，禁止下载图片
						isRemoteImage = false;
						loadingImage();
					} else {
						isRemoteImage = true;
						loadingImage();
					}
				}
			}
		}
	}

	private BroadcastReceiver mPackageStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {//接收安装卸载成功广播
			String action = intent.getAction();
			if (action.equals(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL) || action.equals(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE)) {
				String packageName = intent.getDataString();
				if (!TextUtils.isEmpty(packageName)) {
					packageName = packageName.substring(packageName.indexOf(PACKAGE_STR) + PACKAGE_STR.length());
				}
				PackageInfo info = null;
				if (action.equals(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL)) {//安装操作
					try {
						info = getPackageManager().getPackageInfo(packageName, 0);
					} catch (NameNotFoundException e) {
						System.out.println("name not found:" + e);
					}
					if (info != null) {//安装成功回调
						onAppInstallOrUninstallDone(INSTALL_APP_DONE, info);
					}
				} else {//卸载操作
					info = new PackageInfo();
					info.packageName = packageName;
					List<ADownloadApkItem> updatingList = ADownloadService.updateAPKList.apkList;//更新的APK列表
					if (updatingList != null) {
						for (int i = 0; i < updatingList.size(); i++) {
							ADownloadApkItem updateItem = updatingList.get(i);
							if (updateItem.apkPackageName.equals(packageName)) {//从更新列表中去掉此APK
								info.versionCode = updateItem.apkVersionCode;
								ADownloadService.updateAPKList.apkList.remove(updateItem);
								break;
							}
						}
					}
					onAppInstallOrUninstallDone(UNINSTALL_APP_DONE, info);//卸载成功回调
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (AppMarket) getApplication();
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetworkInfo != null && mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnected()) {
			if (mApp.isRemoteImage()) {
				isRemoteImage = false;
			}
		}
		registerAllReceiver();
	}

	/**
	 * 使用3G下载是否提示过用户
	 * 
	 * @return
	 */
	public boolean is3GDownloadPromptUser() {
		return mApp.isIs3GDownloadPrompt();
	}

	/**
	 * 使用3G下载已提示用户
	 */
	public void set3GDownloadPromptUser() {
		mApp.setIs3GDownloadPrompt(true);
	}
}
