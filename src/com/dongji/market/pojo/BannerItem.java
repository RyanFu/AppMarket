package com.dongji.market.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 编辑精选画廊对象
 * @author zhangkai
 */
public class BannerItem implements Parcelable {
	public long appId;
	public String imageUrl;
	public long category;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(appId);
		dest.writeString(imageUrl);
		dest.writeLong(category);
	}

	public static final Parcelable.Creator<BannerItem> CREATOR=new Creator<BannerItem>() {
		
		@Override
		public BannerItem[] newArray(int size) {
			// TODO Auto-generated method stub
			return new BannerItem[size];
		}
		
		@Override
		public BannerItem createFromParcel(Parcel source) {
			BannerItem item=new BannerItem();
			item.appId=source.readLong();
			item.imageUrl=source.readString();
			item.category=source.readLong();
			return item;
		}
	};
}
