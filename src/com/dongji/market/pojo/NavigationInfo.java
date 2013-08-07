package com.dongji.market.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 导航数据(编辑精选、最近更新、装机必备)
 * @author zhangkai
 *
 */
public class NavigationInfo implements Parcelable {
	public int id;
	public String name;
	public Parcelable[] staticAddress;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeParcelableArray(staticAddress, 0);
	}

	public static final Parcelable.Creator<NavigationInfo> CREATOR=new Parcelable.Creator<NavigationInfo>() {

		@Override
		public NavigationInfo createFromParcel(Parcel source) {
			NavigationInfo info=new NavigationInfo();
			info.id=source.readInt();
			info.name=source.readString();
			info.staticAddress=source.readParcelableArray(StaticAddress.class.getClassLoader());
			return info;
		}

		@Override
		public NavigationInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new NavigationInfo[size];
		}
		
	};
}
