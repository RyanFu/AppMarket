package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.pojo.ApkItem;

/**
 * 基类适配器
 * 
 * @author yvon
 * 
 */
public abstract class ListBaseAdapter extends BaseAdapter implements AConstDefine {
	protected Context context;
	protected boolean isDisplay = true;
	private static final int ENGLISH_LANGUAGE = 0, CHINESE_LANGUAGE = 1;

	public ListBaseAdapter(Context context) {
		this.context = context;
	}

	/**
	 * 根据AppId找到对应的应用修改其状态
	 * 
	 * @param appId
	 * @param status
	 * @return
	 */
	public final boolean changeApkStatusByAppId(boolean isCancel, String packageName, int versionCode) {
		List<ApkItem> list = getItemList();
		if (list != null && list.size() > 0) {
			int i = 0;
			for (; i < list.size(); i++) {
				ApkItem item = list.get(i);
				if (packageName.equals(item.packageName) && versionCode == item.versionCode) {
					if (isCancel && item.status == STATUS_APK_INSTALL) {
						item.status = STATUS_APK_UNINSTALL;
					} else if (isCancel && item.status == STATUS_APK_UPDATE) {
						item.status = STATUS_APK_UNUPDATE;
					} else if (!isCancel && item.status == STATUS_APK_UNINSTALL) {
						item.status = STATUS_APK_INSTALL;
					} else if (!isCancel && item.status == STATUS_APK_UNUPDATE) {
						item.status = STATUS_APK_UPDATE;
					}
					break;
				}
			}
			notifyDataSetChanged();
			return true;
		}
		return false;
	}

	/**
	 * 根据包信息修改应用状态
	 * 
	 * @param status
	 * @param info
	 * @return
	 */
	public boolean changeApkStatusByPackageInfo(int status, PackageInfo info) {
		List<ApkItem> list = getItemList();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				ApkItem item = list.get(i);
				if (status == AConstDefine.INSTALL_APP_DONE) {
					if (item.packageName.equals(info.packageName) && item.versionCode == info.versionCode) {
						list.get(i).status = STATUS_APK_INSTALL_DONE;
						break;
					}
				} else if (status == AConstDefine.UNINSTALL_APP_DONE) {
					if (item.packageName.equals(info.packageName)) {
						list.get(i).status = STATUS_APK_UNINSTALL;
						break;
					}
				}
			}
			notifyDataSetChanged();
			return true;
		}
		return false;
	}

	protected void setLanguageType(int language, ImageView mImageView, ImageView mMultiImageView) {
		if (language == ENGLISH_LANGUAGE) {
			mImageView.setBackgroundResource(R.drawable.language_english);
			mMultiImageView.setVisibility(View.GONE);
			mImageView.setVisibility(View.VISIBLE);
		} else if (language == CHINESE_LANGUAGE) {
			mImageView.setBackgroundResource(R.drawable.language_chinese);
			mMultiImageView.setVisibility(View.GONE);
			mImageView.setVisibility(View.VISIBLE);
		} else {
			mMultiImageView.setBackgroundResource(R.drawable.language_multinational);
			mMultiImageView.setVisibility(View.VISIBLE);
			mImageView.setVisibility(View.GONE);
		}
	}

	public void setDisplayNotify(boolean display) {
		isDisplay = display;
		notifyDataSetChanged();
	}

	/**
	 * 显示apk状态
	 * 
	 * @param mTextView
	 * @param status
	 */
	protected void displayApkStatus(TextView mTextView, int status) {
		switch (status) {
		case STATUS_APK_UNINSTALL:
			setvisibleInstallTextView(mTextView, true, R.string.install, R.drawable.button_has_border_selector, Color.BLACK);
			break;
		case STATUS_APK_INSTALL:
		case STATUS_APK_UPDATE:
			setvisibleInstallTextView(mTextView, true, R.string.cancel, R.drawable.cancel_selector, Color.parseColor("#7f5100"));
			break;
		case STATUS_APK_INSTALL_DONE:
			setvisibleInstallTextView(mTextView, false, R.string.has_installed, R.drawable.button_has_border_selector, Color.parseColor("#999999"));
			break;
		case STATUS_APK_UNUPDATE:
			setvisibleInstallTextView(mTextView, true, R.string.update, R.drawable.update_selector, Color.parseColor("#0e567d"));
			break;
		}
	}

	private void setvisibleInstallTextView(TextView mTextView, boolean enable, int rId, int resid, int textColor) {
		mTextView.setEnabled(enable);
		mTextView.setText(rId);
		mTextView.setBackgroundResource(resid);
		mTextView.setTextColor(textColor);
	}

	/******************* 抽象方法，子类实现 ****************/
	public abstract List<ApkItem> getItemList();// 获取应用列表

	public abstract void addList(List<ApkItem> list);// 添加应用列表
	/******************* 抽象方法，子类实现 ****************/

}
