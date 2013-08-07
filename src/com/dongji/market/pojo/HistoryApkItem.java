package com.dongji.market.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * app 历史版本
 * @author zhangkai
 *
 */
public class HistoryApkItem implements Parcelable {
	public int appId;
	public int category;
	public String appName;
	public String appIconUrl;
	public String updateDate;
	public long downloadNum;
	public String versionName;
	public int appType;
	public long appSize;
	public String url;
	public int versionCode;
	public int status;
	public int heavy;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(appId);
		dest.writeInt(category);
		dest.writeString(appName);
		dest.writeString(appIconUrl);
		dest.writeString(updateDate);
		dest.writeLong(downloadNum);
		dest.writeString(versionName);
		dest.writeInt(appType);
		dest.writeLong(appSize);
		dest.writeString(url);
		dest.writeInt(versionCode);
		dest.writeInt(status);
		dest.writeInt(heavy);
	}
	
	public static final Parcelable.Creator<HistoryApkItem> CREATOR=new Parcelable.Creator<HistoryApkItem>() {

		@Override
		public HistoryApkItem createFromParcel(Parcel source) {
			HistoryApkItem item=new HistoryApkItem();
			item.appId=source.readInt();
			item.category=source.readInt();
			item.appName=source.readString();
			item.appIconUrl=source.readString();
			item.updateDate=source.readString();
			item.downloadNum=source.readLong();
			item.versionName=source.readString();
			item.appType=source.readInt();
			item.appSize=source.readLong();
			item.url=source.readString();
			item.versionCode=source.readInt();
			item.status=source.readInt();
			item.heavy=source.readInt();
			return item;
		}

		@Override
		public HistoryApkItem[] newArray(int size) {
			// TODO Auto-generated method stub
			return new HistoryApkItem[size];
		}
		
	};
}
