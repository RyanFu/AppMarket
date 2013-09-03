package com.dongji.market.download;

import android.os.Environment;

/**
 * 处理下载的常量
 * @author zhangkai
 *
 */
public interface DownloadConstDefine {
	
	/*******************************************应用下载处于各种状态***************************************************/
	
	/**
	 * 下载初始化状态
	 */
	public static final int STATUS_OF_INITIAL = 0;
	
	/**
	 * 正在下载状态
	 */
	public static final int STATUS_OF_DOWNLOADING = 1;
	
	 /**
	  * 下载暂停状态
	  */
	public static final int STATUS_OF_PAUSE = 2;
	
	/**
	 * 下载完成状态
	 */
	public static final int STATUS_OF_COMPLETE = 3;
	
	/**
	 * 下载发生异常状态
	 */
	public static final int STATUS_OF_EXCEPTION = 4;
	
	/**
	 * 下载开始请求状态
	 */
	public static final int STATUS_OF_PREPARE = 5;
	
	/**
	 * 退出后台程序后暂停下载
	 */
	public static final int STATUS_OF_PAUSE_ON_EXIT_SYSTEM = 6;
	
	/**
	 * 因无网络状态暂停下载
	 */
	public static final int STATUS_OF_PAUSE_ON_NOT_NETWORK = 7;
	
	/**
	 * 忽略更新状态
	 */
	public static final int STATUS_OF_IGNORE = 8;
	
	/**
	 * 因达到流量限制暂停下载
	 */
	public static final int STATUS_OF_PAUSE_ON_TRAFFIC_LIMIT = 9;
	
	
	/*******************************************应用下载处于各种状态***************************************************/
	
	
	
	/*******************************************应用类型（下载、可更新、待安装、忽略）***************************************************/
	
	/**
	 * 下载类型
	 */
	public static final int TYPE_OF_DOWNLOAD = 1;

	/**
	 * 更新类型
	 */
	public static final int TYPE_OF_UPDATE = 2;
	
	/**
	 * 下载完成类型
	 */
	public static final int TYPE_OF_COMPLETE = 3;
	
	/**
	 * 更新忽略类型
	 */
	public static final int TYPE_OF_IGNORE = 4;
	
	/*******************************************应用类型（下载、可更新、待安装、忽略）***************************************************/
	
	
	/*******************************************广播****************************************/
	/**
	 * 添加下载广播
	 */
	public static final String BROADCAST_ACTION_ADD_DOWNLOAD = "com.dongji.market.ADD_DOWNLOAD";
	
	/**
	 * 删除下载广播
	 */
	public static final String BROADCAST_ACTION_REMOVE_DOWNLOAD = "com.dongji.market.REMOVE_DOWNLOAD";
	
	/**
	 * 暂停下载广播
	 */
	public static final String BROADCAST_ACTION_PAUSE_DOWNLOAD = "com.dongji.market.PAUSE_DOWNLOAD";
	
	/**
	 * 取消下载广播
	 */
	public static final String BROADCAST_ACTION_CANCEL_DOWNLOAD = "com.dongji.market.CANCEL_DOWNLOAD";
	
	/**
	 * 一键更新广播
	 */
	public static final String BROADCAST_ACTION_ONEKEY_UPDATE = "com.dongji.market.ONEKEY_UPDATE";
	
	/**
	 * 更新数据请求完成
	 */
	public static final String BROADCAST_ACTION_APP_UPDATE_DATADONE="com.dongji.market.APP_UPDATE_DATADONE";
	
	/**
	 * 更新数据合并完成
	 */
	public static final String BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE="com.dongji.market.UPDATE_DATA_MERGE_DONE";
	/**
	 * 更新Root安装失败后的状态
	 */
	public static final String BROADCAST_ACTION_UPDATE_ROOTSTATUS="com.dongji.market.UPDATE_ROOTSTATUS";
	
	/**
	 * 下载完成广播
	 */
	public static final String BROADCAST_ACTION_COMPLETE_DOWNLOAD = "com.dongji.market.COMPLETE_DOWNLOAD";
	
	/**
	 * 添加更新应用广播
	 */
	public static final String BROADCAST_ACTION_ADD_UPDATE = "com.dongji.market.ADD_UPDATE";
	
	/**
	 * 忽略更新广播
	 */
	public static final String BROADCAST_ACTION_IGNORE_UPDATE = "com.dongji.market.IGNORE_UPDATE";
	
	/**
	 * 取消忽略广播
	 */
	public static final String BROADCAST_ACTION_CANCEL_IGNORE = "com.dongji.market.CANCEL_IGNORE";
	
	/**
	 * 流量使用完广播
	 */
	public static final String BROADCAST_ACTION_TRAFFIC_OVER = "com.dongji.market.TRAFFIC_OVER";
	
	/**
	 * 流量设置改变广播
	 */
	public static final String BROADCAST_ACTION_GPRS_SETTING_CHANGE = "com.dongji.market.GPRS_SETTING_CHANGE";
	
	/**
	 * 程序退出后台继续下载广播
	 */
	public static final String BROADCAST_ACTION_BACKGROUND_DOWNLOAD = "com.dongji.market.BACKGROUND_DOWNLOAD";
	
	/**
	 * 请求单个应用的更新
	 */
	public static final String BROADCAST_ACTION_REQUEST_SINGLE_UPDATE = "com.dongji.market.REQUEST_SINGLE_UPDATE";
	
	/**
	 * 单个应用的更新数据已经请求到
	 */
	public static final String BROADCAST_ACTION_SINGLE_UPDATE_DONE = "com.dongji.market.SINGLE_UPDATE_DONE";
	
	/**
	 * 应用安装完成
	 */
	public static final String BROADCAST_ACTION_INSTALL_COMPLETE = "com.dongji.market.INSTALL_COMPLETE";
	
	/**
	 * 应用卸载完成
	 */
	public static final String BROADCAST_ACTION_REMOVE_COMPLETE = "com.dongji.market.REMOVE_COMPLETE";
	
	/**
	 * 检查所有下载
	 */
	public static final String BROADCAST_ACTION_CHECK_DOWNLOAD = "com.dongji.market.CHECK_DOWNLOAD";
	
	/**
	 * 开始所有下载
	 */
	public static final String BROADCAST_ACTION_START_ALL_DOWNLOAD = "com.dongji.market.START_ALL_DOWNLOAD";
	
	/**
	 * 云恢复广播
	 */
	public static final String BROADCAST_ACTION_CLOUD_RESTORE = "com.dongji.market.CLOUD_RESTORE";
	
	/**
	 * 添加到下载列表
	 */
	public static final String BROADCAST_ACTION_ADD_DOWNLOAD_LIST = "com.dongji.market.ADD_DOWNLOAD_LIST";
	
	/*******************************************广播****************************************/
	

	/**
	 * 下载存储根路径
	 */
	public static final String DOWNLOAD_ROOT_PATH = Environment
			.getExternalStorageDirectory().getPath()
			+ "/.dongji/dongjiMarket/cache/apk/";
	
	/**
	 * 下载完成的文件后缀
	 */
	public static final String DOWNLOAD_FILE_POST_SUFFIX = ".apk";
	
	/**
	 * 下载未完成的文件后缀
	 */
	public static final String DOWNLOAD_FILE_PREPARE_SUFFIX = ".apk.temp";
	
	/**
	 * 下载实体类
	 */
	public static final String DOWNLOAD_ENTITY="DownloadEntity";
	
	/**
	 * 包名
	 */
	public static final String DOWNLOAD_APKPACKAGENAME="ApkPackageName";
}
