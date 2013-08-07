package com.dongji.market.pojo;

import android.graphics.drawable.Drawable;

public class AppInfoData {

	private Drawable appIcon; // 图标
	private String appName; // 名称
	private String appVerCode; // 版本号
	private String appVerName; // 版本码
	private String appPackageName; // 包名
	private String appLastModified; // 最后更改时间
	private String appSize; // 大小
	private String appPath; // 路径
	private boolean isInstalled; // 是否已安装

	public AppInfoData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AppInfoData(Drawable appIcon, String appName, String appVerCode,
			String appVerName, String appPackageName, String appLastModified,
			String appSize, String appPath, boolean isInstalled) {
		super();
		this.appIcon = appIcon;
		this.appName = appName;
		this.appVerCode = appVerCode;
		this.appVerName = appVerName;
		this.appPackageName = appPackageName;
		this.appLastModified = appLastModified;
		this.appSize = appSize;
		this.appPath = appPath;
		this.isInstalled = isInstalled;
	}

	public Drawable getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVerCode() {
		return appVerCode;
	}

	public void setAppVerCode(String appVerCode) {
		this.appVerCode = appVerCode;
	}

	public String getAppVerName() {
		return appVerName;
	}

	public void setAppVerName(String appVerName) {
		this.appVerName = appVerName;
	}

	public String getAppPackageName() {
		return appPackageName;
	}

	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}

	public String getAppLastModified() {
		return appLastModified;
	}

	public void setAppLastModified(String appLastModified) {
		this.appLastModified = appLastModified;
	}

	public String getAppSize() {
		return appSize;
	}

	public void setAppSize(String appSize) {
		this.appSize = appSize;
	}

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public boolean isInstalled() {
		return isInstalled;
	}

	public void setInstalled(boolean isInstalled) {
		this.isInstalled = isInstalled;
	}

}
