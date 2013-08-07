package com.dongji.market.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 静态页面地址，根据 md5 对比是否需要load新数据文件
 * @author zhangkai
 *
 */
public class StaticAddress implements Parcelable {
	public String url;
	public String md5Value;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeString(md5Value);
	}
	
	public static final Parcelable.Creator<StaticAddress> CREATOR = new Parcelable.Creator<StaticAddress>() {

		@Override
		public StaticAddress createFromParcel(Parcel source) {
			StaticAddress staticAddress=new StaticAddress();
			staticAddress.url=source.readString();
			staticAddress.md5Value=source.readString();
			return staticAddress;
		}

		@Override
		public StaticAddress[] newArray(int size) {
			// TODO Auto-generated method stub
			return new StaticAddress[size];
		}
		
	};
}
