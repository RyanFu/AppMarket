package com.dongji.market.download;

import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.InstalledAppInfo;

public class AIgnoreItem {
	/**
	 * 下载的APK序号
	 */
	// public int apkId;
	/**
	 * APK名字
	 */
	public String apkName;
	/**
	 * APK版本名称
	 */
	public String apkVersion;
	/**
	 * APK版本号
	 */
	public int apkVersionCode;
	/**
	 * APK显示的图标
	 */
	public String apkIconUrl;
	/**
	 * APK的总长度
	 */
	public int apkTotalSize;
	/**
	 * APK网络上的地址
	 */
	public String apkUrl;
	/**
	 * APK的包名
	 */
	public String apkPackageName;

	// /**
	// * APK可升级版本名称
	// */
	// public String apkUpdateVersion;
	// /**
	// * APK可升级版本长度
	// */
	// public String apkUpdateTotalSize;

	public AIgnoreItem() {

	}

	// public AIgnoreItem(ADownloadApkItem aDownloadApkItem){
	// this.apkIconUrl=aDownloadApkItem.apkIconUrl;
	// // this.apkId=aDownloadApkItem.apkId;
	// this.apkName=aDownloadApkItem.apkName;
	// this.apkPackageName=aDownloadApkItem.apkPackageName;
	// this.apkTotalSize=aDownloadApkItem.apkTotalSize;
	// this.apkUrl=aDownloadApkItem.apkUrl;
	// this.apkVersion=aDownloadApkItem.apkVersion;
	// this.apkVersionCode=aDownloadApkItem.apkVersionCode;
	// }
	//
	public AIgnoreItem(ADownloadApkItem aDownloadApkItem,
			InstalledAppInfo installedAppInfo) {
		this.apkIconUrl = aDownloadApkItem.apkIconUrl;
		this.apkName = installedAppInfo.getName();
		this.apkPackageName = installedAppInfo.getPkgName();
		this.apkTotalSize = (int) DJMarketUtils
				.sizeFromMToLong(installedAppInfo.getSize());
		this.apkUrl = aDownloadApkItem.apkUrl;
		this.apkVersion = installedAppInfo.getVersion();
		this.apkVersionCode = installedAppInfo.getVersionCode();
	}
}
