package com.dongji.market.pojo;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class InstalledAppInfo {

	Drawable icon;
	String name;
	String version;
	int versionCode;
	String pkgName;
	String size;
	ApplicationInfo appInfo;
	public int moveType;

	public InstalledAppInfo() {}

	public InstalledAppInfo(Drawable icon, String name, String version,
			String pkgName, String size, ApplicationInfo appInfo) {
		super();
		this.icon = icon;
		this.name = name;
		this.version = version;
		this.pkgName = pkgName;
		this.size = size;
		this.appInfo = appInfo;
	}
	
	public InstalledAppInfo(Drawable icon, String name, String version,
			String pkgName, String size, int versionCode,ApplicationInfo appInfo) {
		super();
		this.icon = icon;
		this.name = name;
		this.version = version;
		this.versionCode=versionCode;
		this.pkgName = pkgName;
		this.size = size;
		this.appInfo = appInfo;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getVersionCode() {
		return versionCode;
	}
	
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	
	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public ApplicationInfo getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(ApplicationInfo appInfo) {
		this.appInfo = appInfo;
	}

	@Override
	public String toString() {
		return "SoftwareInfo [icon=" + icon + ", name=" + name + ", version="
				+ version + ", pkgName=" + pkgName + ", size=" + size
				+ ", appInfo=" + appInfo + "]";
	}

}
