package com.dongji.market.pojo;

import java.util.ArrayList;

import com.dongji.market.download.ADownloadApkItem;

import android.os.Parcel;
import android.os.Parcelable;

public class ApkItem implements Parcelable {
	public int appId;   // 应用Id
	public String appName;   // 应用名称
	public int category;   // 应用分类
	public String apkUrl;   // 应用下载地址
	public String packageName;   // 包名
	public String firmwareVersion;
	public String version;
	public int classx;
	public int language;
	public String company;
	public String appIconUrl;
	public ArrayList<String> appScreenshotUrl=new ArrayList<String>();
	public String discription;
	public String updateDate;
	public long fileSize;
	public long downloadNum;
	public int versionCode;
	public int status; // 用以区分当前apk状态
	public HistoryApkItem[] historys;
	public ArrayList<String> permisions=new ArrayList<String>();
	public int minSdkVersion;
	public String bannerUrl;
	public double score;
	public ArrayList<ApkItem> likeList=new ArrayList<ApkItem>();
	public int heavy; //=1

	public String apkSize;
	public String comment;
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public ApkItem(){
		
	}
	
	public ApkItem(ADownloadApkItem aDownloadApkItem){
		this.appId=aDownloadApkItem.apkId;
		this.appName=aDownloadApkItem.apkName;
		this.category=aDownloadApkItem.category;
		this.apkUrl=aDownloadApkItem.apkUrl;
		this.packageName=aDownloadApkItem.apkPackageName;
		this.version=aDownloadApkItem.apkVersion;
		this.appIconUrl=aDownloadApkItem.apkIconUrl;
		this.fileSize=aDownloadApkItem.apkTotalSize;
		this.versionCode=aDownloadApkItem.apkVersionCode;
		this.status=aDownloadApkItem.apkStatus;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(appId);
		dest.writeString(appName);
		dest.writeInt(category);
		dest.writeString(apkUrl);
		dest.writeString(packageName);
		dest.writeString(firmwareVersion);
		dest.writeString(version);
		dest.writeInt(classx);
		dest.writeInt(language);
		dest.writeString(company);
		dest.writeString(appIconUrl);
		dest.writeList(appScreenshotUrl);
		dest.writeString(discription);
		dest.writeString(updateDate);
		dest.writeLong(fileSize);
		dest.writeLong(downloadNum);
		dest.writeInt(versionCode);
		dest.writeInt(status);
//		dest.writeArray(historys);
		dest.writeInt(historys==null?0:historys.length);
		dest.writeTypedArray(historys, 1);
		dest.writeList(permisions);
		dest.writeInt(minSdkVersion);
		dest.writeString(bannerUrl);
		dest.writeDouble(score);
		dest.writeInt(heavy);
	}
	
	public static final Parcelable.Creator<ApkItem> CREATOR=new Creator<ApkItem>() {
		
		@Override
		public ApkItem[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ApkItem[size];
		}
		
		@Override
		public ApkItem createFromParcel(Parcel source) {
			ApkItem item=new ApkItem();
			item.appId=source.readInt();
			item.appName=source.readString();
			item.category=source.readInt();
			item.apkUrl=source.readString();
			item.packageName=source.readString();
			item.firmwareVersion=source.readString();
			item.version=source.readString();
			item.classx=source.readInt();
			item.language=source.readInt();
			item.company=source.readString();
			item.appIconUrl=source.readString();
			source.readList(item.appScreenshotUrl, String.class.getClassLoader());
			item.discription=source.readString();
			item.updateDate=source.readString();
			item.fileSize=source.readLong();
			item.downloadNum=source.readLong();
			item.versionCode=source.readInt();
			item.status=source.readInt();
			int length=source.readInt();
			if(length>0) {
				item.historys=new HistoryApkItem[length];
				source.readTypedArray(item.historys, HistoryApkItem.CREATOR);
			}
//			item.historys=(HistoryApkItem[])source.readArray(item.getClass().getClassLoader());  //HistoryApkItem.class.getClassLoader()
			source.readList(item.permisions, String.class.getClassLoader());
			item.minSdkVersion=source.readInt();
			item.bannerUrl=source.readString();
			item.score=source.readDouble();
			item.heavy=source.readInt();
			return item;
		}
	};
}
