package com.dongji.market.helper;

import android.os.Environment;

public interface AConstDefine {
	// ————————————————————————————————————NOTIFICATION————————————————————————————————————
	/**
	 * 标志：Notification下载提示
	 */
	public static final int FLAG_NOTIFICATION_DOWNLOAD = 1;
	/**
	 * 标志：Notification升级提示
	 */
	public static final int FLAG_NOTIFICATION_UPDATE = 2;
	/**
	 * 标志：Notification正在升级提示
	 */
	public static final int FLAG_NOTIFICATION_UPDATEING = 3;
	/**
	 * 标志：取消所有Notification
	 */
	public static final int FLAG_NOTIFICATION_CANCELALL = 4;
	/**
	 * 标志：下载完成，等待安装
	 */
	public static final int FLAG_NOTIFICATION_WAITINGINSTALL = 5;

	// ———————————————————————————————————————sharepreferences—————————————————————————————————
	/**
	 * sharepreferences名字
	 */
	public static final String DONGJI_SHAREPREFERENCES = "dongji_sharepreferences";
	/**
	 * sharepreferences--已用蜂窝下载的值
	 */
	public static final String SHARE_DOWNLOADSIZE = "share_downloadsize";
	/**
	 * sharepreferences--记录TOP50获取时间
	 */
	public static final String SHARE_GETTOP50TIME = "share_gettop50time";
	
	// ———————————————————————————————————————广播—————————————————————————————————
	/**
	 * 系统广播_app安装成功
	 */
	public static final String BROADCAST_SYS_ACTION_APPINSTALL = "android.intent.action.PACKAGE_ADDED";
	/**
	 * 系统广播_App卸载成功
	 */
	public static final String BROADCAST_SYS_ACTION_APPREMOVE = "android.intent.action.PACKAGE_REMOVED";
	/**
	 * 对话框修改了流量限制
	 */
	public static final String BROADCAST_ACTION_DIALOG_LIMITFLOWCHANGE = "com.dongji.market.dialogFlowChange";
	/**
	 * 设置流量已用完
	 */
	public static final String BROADCAST_ACTION_NOFLOW = "com.dongji.market.action.noflow";
	
	/**
	 * 广播_删除已下载安装包
	 */
	public static final String BROADCAST_DEL_DOWNLOADED_APK = "com.dongji.market.del_all_apk";
	
	public static final String BROADCAST_DIALOG_LOGIN = "broadcast_dialog_login";
	
	public static final String BROADCAST_ACTION_SHOWBANDRLIST = "broadcast_action_showbandrlist";
	
	public static final String BROADCAST_ACTION_SHOWUNINSTALLLIST = "broadcast_action_showuninstalllist";
	
	public static final String GO_HOME_BROADCAST = "com.dongji.market.goHome_broadcast";
	
	public static final String SAVE_FLOW_BROADCAST = "com.dongji.market.saveFlow_changed_broadcast";

	// ———————————————————————————————————————其它—————————————————————————————————
	public static final String CANCELNOFLOWDIALOG = "cancelnoflowdialog";

	/*************** apk状态值 **********************/
	public static final int STATUS_APK_UNINSTALL = 0; // 未安装
	public static final int STATUS_APK_INSTALL = 1; // 正在安装
	public static final int STATUS_APK_INSTALL_DONE = 2; // 已安装
	public static final int STATUS_APK_UPDATE = 3; // 正在更新
	public static final int STATUS_APK_UNUPDATE = 4; // 未更新
	/*************** apk状态值 **********************/
	public static final String FLAG_ACTIVITY_BANDR = "flag_activity_bandr";
	public static final int ACTIVITY_RESTORE = 1;
	public static final int ACTIVITY_BACKUP = 2;
	public static final int ACTIVITY_CLOUD_BACKUP = 3;
	public static final int ACTIVITY_CLOUD_RESTORE = 4;
	
	public static final int THRESHOLD = Integer.MAX_VALUE;

	public static final int AUTO_SCRLL_TIMES = 5;

	// ——————————————————————————————登录相关——————————————————————————————————————
	public static final String DIALOG_LOGIN_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=login";
	public static final String REGISTER_LOGIN_SUCCESS_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=loginsuc";
	public static final String LOGIN_SOURCE = "com.dongji.market.fromDialog";
	public static final String LOGIN_STATUS_BROADCAST = "login_status_broadcast";
	public static final String LOGIN_STATUS = "loginStatus";
	


	/******************************************* 应用下载处于各种状态 ***************************************************/

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

	/******************************************* 应用下载处于各种状态 ***************************************************/
	
	

	/******************************************* 应用类型（下载、可更新、待安装、忽略） ***************************************************/

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

	/******************************************* 应用类型（下载、可更新、待安装、忽略） ***************************************************/
	
	

	/******************************************* 广播 ****************************************/
	/**
	 * 添加下载广播
	 */
	public static final String BROADCAST_ACTION_ADD_DOWNLOAD = "com.dongji.market.ADD_DOWNLOAD";

	/**
	 * 删除下载广播
	 */
	public static final String BROADCAST_ACTION_REMOVE_DOWNLOAD = "com.dongji.market.REMOVE_DOWNLOAD";


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
	public static final String BROADCAST_ACTION_APP_UPDATE_DATADONE = "com.dongji.market.APP_UPDATE_DATADONE";

	/**
	 * 更新数据合并完成
	 */
	public static final String BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE = "com.dongji.market.UPDATE_DATA_MERGE_DONE";
	/**
	 * 更新Root安装失败后的状态
	 */
	public static final String BROADCAST_ACTION_UPDATE_ROOTSTATUS = "com.dongji.market.UPDATE_ROOTSTATUS";

	/**
	 * 下载完成广播
	 */
	public static final String BROADCAST_ACTION_COMPLETE_DOWNLOAD = "com.dongji.market.COMPLETE_DOWNLOAD";

	/**
	 * 添加更新应用广播
	 */
	public static final String BROADCAST_ACTION_ADD_UPDATE = "com.dongji.market.ADD_UPDATE";

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

	/******************************************* 广播 ****************************************/
	
	

	/**
	 * 下载存储根路径
	 */
	public static final String DOWNLOAD_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/.dongji/dongjiMarket/cache/apk/";

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
	public static final String DOWNLOAD_ENTITY = "DownloadEntity";

	/**
	 * 包名
	 */
	public static final String DOWNLOAD_APKPACKAGENAME = "ApkPackageName";
	
	
	
	
	final static String FIRST_LAUNCHER="first_launcher";
	
	final static String FIRST_LAUNCHER_DETAIL="first_launcher_detail";
	
	final static String FIRST_LAUNCHER_UNINSTALL="first_launcher_uninstall";
	
	final static String FIRST_LAUNCHER_SETTING="first_launcher_setting";
	final static String FIRST_LAUNCHER_SETTING2="first_launcher_setting2";
	
	final static String LAUNCHER_STR="com.dongji.market.activity.LauncherActivity";
	
	final static String SHARE_FILE_NAME="com.dongji.market_temp";
	
	final static String LAST_UPDATE_TIME = "last_update_time";
	
	final static String FIRST_LAUNCHER_SEARCH = "first_launcher_search";
	
	final static String FIRST_LAUNCHER_SOFT_MOVE = "first_launcher_soft_move";
	
	
	
	
	public static final String IMGTYPE = "image/*";// mimeType 图片类型
	public static final String TXTTYPE = "text/plain";// mimeType 文本类型
	public static final String ALLTYPE = "*/*";// 所有类型
	public static final int THUMB_SIZE = 100;// 缩略图大小
	public static final String FRIENDACTNAME = "com.tencent.mm.ui.tools.ShareImgUI";// 微信好友分享activity名
	public static final String TIMELINEACTNAME = "com.tencent.mm.ui.tools.ShareToTimeLineUI";// 微信朋友圈分享activity名
	public static final String TIMELINELABLE = "朋友圈";
	public static final String WXPKGNAME = "com.tencent.mm";// 微信包名
	public static final String APP_ID = "wx3ea8689414314421";// APP_ID为你的应用从官方网站申请到的合法appId
	public static final String DOMAIN_NAME = "http://www.91dongji.com/";
	public static final long IMAGE_SIZE_LIMIT =18*1024;

}
