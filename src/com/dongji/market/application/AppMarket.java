package com.dongji.market.application;

import java.util.ArrayList;

import com.dongji.market.cache.FileService;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.LoginParams;

import android.app.Application;

/**
 * 全局配置文件
 * 
 * @author yvon
 * 
 */
public class AppMarket extends Application {
	private boolean is3GDownloadPrompt; // 蜂窝下载有无提示过
	private ArrayList<ApkItem> updateList; // 更新列表
	private LoginParams loginParams; // 保存登录产生的状态值
	private boolean isRemoteImage = true; // 是否请求网络图片

	public boolean isIs3GDownloadPrompt() {
		return is3GDownloadPrompt;
	}

	public void onCreate() {
		super.onCreate();
		initPicturesResources();
	};

	/**
	 * 初始化图片资源
	 */
	private void initPicturesResources() {
		FileService.loadFileToMap();
	}

	public void setIs3GDownloadPrompt(boolean is3gDownloadPrompt) {
		is3GDownloadPrompt = is3gDownloadPrompt;
	}

	public ArrayList<ApkItem> getUpdateList() {
		return updateList;
	}

	public void addUpdate(ApkItem item) {
		if (this.updateList == null) {
			updateList = new ArrayList<ApkItem>();
		}
		updateList.add(item);
	}

	public void setUpdateList(ArrayList<ApkItem> updateList) {
		this.updateList = updateList;
	}

	public LoginParams getLoginParams() {
		if (loginParams == null) {
			loginParams = new LoginParams();
		}
		return loginParams;
	}

	public boolean isRemoteImage() {
		return isRemoteImage;
	}

	public void setRemoteImage(boolean isRemoteImage) {
		this.isRemoteImage = isRemoteImage;
	}
}
