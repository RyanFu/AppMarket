package com.dongji.market.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 分类列表
 * @author zhangkai
 */
public class ChannelListInfo implements Parcelable {
	public int id;
	public String name;
	public int parentId;
	public String iconUrl;
	public int pageCount;
	public int currentPage;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeInt(parentId);
		dest.writeString(iconUrl);
		dest.writeInt(pageCount);
		dest.writeInt(currentPage);
	}

	public static final Parcelable.Creator<ChannelListInfo> CREATOR = new Creator<ChannelListInfo>() {
		
		@Override
		public ChannelListInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ChannelListInfo[size];
		}
		
		@Override
		public ChannelListInfo createFromParcel(Parcel source) {
			ChannelListInfo info=new ChannelListInfo();
			info.id=source.readInt();
			info.name=source.readString();
			info.parentId=source.readInt();
			info.iconUrl=source.readString();
			info.pageCount=source.readInt();
			info.currentPage=source.readInt();
			return info;
		}
	};
	
	@Override
	public int hashCode() {
		return id+name.hashCode()+parentId;
	};
	
	@Override
	public boolean equals(Object o) {
		if(o!=null && o instanceof ChannelListInfo) {
			if(o.hashCode()==hashCode()) {
				return true;
			}
		}
		return false;
	};
}
