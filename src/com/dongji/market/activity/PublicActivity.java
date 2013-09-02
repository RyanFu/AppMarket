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
import com.dongji.market.download.ADownloadApkItem;
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
	private ApkStatusReceiver mApkStatusReceiver;
	private PackageStatusReceiver mPackageStatusReceiver;
	private static final String PACKAGE_STR = "package:";
	private boolean isEconomizeTraffic; // 当前是否为浏览图片省流量模式
	protected AppMarket mApp;
	protected boolean isRemoteImage = true; // 是否允许请求网络图片
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (AppMarket) getApplication();
		//是否有手机网络
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetworkInfo != null && mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnected()) {//是否下载图片
			isRemoteImage = mApp.isRemoteImage();
		}
		registerAllReceiver();
	}
	
	
	/**
	 * 注册广播接收器
	 */
	private void registerAllReceiver() {
		mApkStatusReceiver = new ApkStatusReceiver();//apk状态接收者
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_ADD_DOWNLOAD);//下载
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);//取消
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_COMPLETE_DOWNLOAD);//下载完成
		intentFilter.addAction(DownloadConstDefine.BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE);//更新数据合并
		intentFilter.addAction(AConstDefine.SAVE_FLOW_BROADCAST);//节省流量
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//网络连接
		
		mPackageStatusReceiver =new PackageStatusReceiver();//包状态接收者
		IntentFilter packageFilter = new IntentFilter();
		packageFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL);//APP安装成功
		packageFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE);//APP卸载成功
		packageFilter.addDataScheme("package");
		
		registerReceiver(mApkStatusReceiver, intentFilter);
		registerReceiver(mPackageStatusReceiver, packageFilter);
	}
	
	/**
	 * 每次数据请求完成后都要重新设置apk状态
	 * infos：为本地应用安装信息
	 * downloadlist：为后台应用下载信息
	 * items：为从服务端获取的应用信息
	 * @param items
	 * @return
	 */
	protected List<ApkItem> setApkStatus(List<ApkItem> items) {
		if (items != null && items.size() > 0) {
			// 获取手机上已安装的应用
			List<PackageInfo> infos = AndroidUtils.getInstalledPackages(this);
			for (int i = 0; i < items.size(); i++) {
				ApkItem item = items.get(i);
				//与后台下载应用信息作比较
				if (DownloadService.mDownloadService != null) {//有下载服务在运行
					List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
					for (int j = 0; j < downloadList.size(); j++) {//有下载列表
						DownloadEntity entity = downloadList.get(j);
						if (item.packageName.equals(entity.packageName) && item.versionCode == entity.versionCode) {
							switch (entity.downloadType) {//重新设置apk的状态
							case DownloadConstDefine.TYPE_OF_DOWNLOAD://为正在下载类型的应用
								items.get(i).status = AConstDefine.STATUS_APK_INSTALL;//设置应用为安装状态
								break;
							case DownloadConstDefine.TYPE_OF_UPDATE://为可更新类型的应用
								if (entity.getStatus() != DownloadConstDefine.STATUS_OF_INITIAL && entity.getStatus() != DownloadConstDefine.STATUS_OF_IGNORE) {//此类型应用是否处于下载初始化或忽略状态
									items.get(i).status = AConstDefine.STATUS_APK_UPDATE;//设置应用为更新状态
								} else {
									items.get(i).status = AConstDefine.STATUS_APK_UNUPDATE;//设置应用为未更新状态
								}
								break;
							case DownloadConstDefine.TYPE_OF_COMPLETE://为可安装类型的应用
								if (item.status == AConstDefine.STATUS_APK_INSTALL) {//判断应用状态是否是已安装，
									items.get(i).status = AConstDefine.STATUS_APK_UNINSTALL;//设置应用为未安装
								} else if (item.status == AConstDefine.STATUS_APK_UPDATE) {//判断应用状态是否是更新
									items.get(i).status = AConstDefine.STATUS_APK_UNUPDATE;//设置应用为未更新
								}
								break;
							}
						}
					}
				}
				// 与手机已安装应用信息作比较
				if (infos != null && infos.size() > 0) {
					for (int k = 0; k < infos.size(); k++) {
						PackageInfo info = infos.get(k);
						if (info.packageName.equals(items.get(i).packageName) && info.versionCode >= items.get(i).versionCode) {//手机已安装此应用
							items.get(i).status = AConstDefine.STATUS_APK_INSTALL_DONE;//设置应用为已安装
							break;
						}
					}
				}
			}
		}
		return items;
	}


	/**
	 * 设置应用详情页应用状态
	 * @param item
	 * @return
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
				//设置历史版本应用状态
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
												if (info.versionCode < historyItem.versionCode) {//判断获取应用的版本是否小于历史版本
													if (entity.getStatus() != DownloadConstDefine.STATUS_OF_INITIAL && entity.getStatus() != DownloadConstDefine.STATUS_OF_IGNORE) {//是否处于初始化或忽略状态
														historyItem.status = AConstDefine.STATUS_APK_UPDATE;//设置应用为更新
													} else {
														historyItem.status = AConstDefine.STATUS_APK_UNUPDATE;//设置应用为未更新
													}
												} else {
													System.out.println("=============== TYPE_OF_DOWNLOAD");
													historyItem.status = AConstDefine.STATUS_APK_INSTALL_DONE;//设置应用为已安装
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

	private class PackageStatusReceiver  extends BroadcastReceiver{

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

	
	/***************************************抽象类定义，子类实现**********************************/
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
	
	/**
	 * 数据更新
	 */
	protected abstract void onUpdateDataDone();
	
	/**
	 * 加载图片
	 */
	protected abstract void loadingImage();
	/***************************************抽象类定义**********************************/

	
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
	


	@Override
	protected void onResume() {
		super.onResume();
		if (getParent() == null) {
			MobclickAgent.onResume(this);
		}
		isEconomizeTraffic =!mApp.isRemoteImage();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (getParent() == null) {
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mApkStatusReceiver != null) {
			unregisterReceiver(mApkStatusReceiver);
		}
		if (mPackageStatusReceiver != null) {
			unregisterReceiver(mPackageStatusReceiver);
		}
	}


	
}
