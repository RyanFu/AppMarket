package com.dongji.market.pojo;

import android.graphics.drawable.Drawable;

public class ShareItem {
	
	private Drawable shareIcon;
	private String shareName;
	public ShareItem() {}
	public ShareItem(Drawable shareIcon, String shareName) {
		this.shareIcon = shareIcon;
		this.shareName = shareName;
	}
	public Drawable getShareIcon() {
		return shareIcon;
	}
	public void setShareIcon(Drawable shareIcon) {
		this.shareIcon = shareIcon;
	}
	public String getShareName() {
		return shareName;
	}
	public void setShareName(String shareName) {
		this.shareName = shareName;
	}
	@Override
	public String toString() {
		return "ShareItem [mShareIcon=" + shareIcon + ", mShareName="
				+ shareName + "]";
	}

}
