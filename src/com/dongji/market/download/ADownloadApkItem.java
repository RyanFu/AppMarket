package com.dongji.market.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.dongji.market.pojo.ApkItem;

/**
 * 下载的APK的相关信息
 * 
 * @author quhm
 * 
 */
public class ADownloadApkItem implements Parcelable {
	/**
	 * 下载的APK序号
	 */
	public int apkId;
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
	 * APK已下载的长度
	 */
	public int apkDownloadSize;
	/**
	 * APK的总长度
	 */
	public int apkTotalSize;
	/**
	 * APK的状态，详见ConstDefine中的“STATUS_OF_”相关状态
	 */
	public int apkStatus;
	/**
	 * APK网络上的地址
	 */
	public String apkUrl;
	/**
	 * APK的包名
	 */
	public String apkPackageName;
	/**
	 * APK下载异常次数
	 */
	public int errCount=0;
	
	public int category;
	
	public ADownloadApkItem(){
		
	}
	
	public ADownloadApkItem(ApkItem apkItem,int apkStatus){
		this.apkDownloadSize=0;
		this.apkIconUrl=apkItem.appIconUrl;
		this.apkId=apkItem.appId;
		this.apkName=apkItem.appName;
		this.apkPackageName=apkItem.packageName;
		this.apkStatus=apkStatus;
		this.apkUrl=apkItem.apkUrl;
		this.apkVersion=apkItem.version;
		this.apkVersionCode=apkItem.versionCode;
		this.apkTotalSize = (int)apkItem.fileSize;
		this.category=apkItem.category;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(apkDownloadSize);
		dest.writeString(apkIconUrl);
		dest.writeInt(apkId);
		dest.writeString(apkName);
		dest.writeString(apkPackageName);
		dest.writeInt(apkStatus);
		dest.writeInt(apkTotalSize);
		dest.writeString(apkUrl);
		dest.writeString(apkVersion);
		dest.writeInt(apkVersionCode);
		dest.writeInt(category);
	}

	public static final Parcelable.Creator<ADownloadApkItem> CREATOR = new Creator<ADownloadApkItem>() {

		@Override
		public ADownloadApkItem[] newArray(int size) {
			return new ADownloadApkItem[size];
		}

		@Override
		public ADownloadApkItem createFromParcel(Parcel source) {
			ADownloadApkItem aDownloadApkItem = new ADownloadApkItem();
			aDownloadApkItem.apkDownloadSize=source.readInt();
			aDownloadApkItem.apkIconUrl=source.readString();
			aDownloadApkItem.apkId=source.readInt();
			aDownloadApkItem.apkName=source.readString();
			aDownloadApkItem.apkPackageName=source.readString();
			aDownloadApkItem.apkStatus=source.readInt();
			aDownloadApkItem.apkTotalSize=source.readInt();
			aDownloadApkItem.apkUrl=source.readString();
			aDownloadApkItem.apkVersion=source.readString();
			aDownloadApkItem.apkVersionCode=source.readInt();
			aDownloadApkItem.category=source.readInt();
			return aDownloadApkItem;
		}
	};
}
