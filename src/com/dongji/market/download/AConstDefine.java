package com.dongji.market.download;


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

	// ———————————————————————————————————————批量下载与更新—————————————————————————————————
	/**
	 * 标志：有未完成的下载与更新
	 */
	public static final int FLAG_LISTUNDOWNTASK = 1;
	/**
	 * 标志：一键更新
	 */
	public static final int FLAG_ONEKEYUPDATEING = 2;
	/**
	 * 标志：继续因流量不足而暂停的任务
	 */
	public static final int INT_CONTINUEPAUSETASK = 3;
	/**
	 * 标志：云恢复
	 */
	public static final int INT_CLOUDRESTORE = 4;

	/**
	 * 标志：是否有未完成的任务
	 */
	public static final String FLAG_ISUNDONETASK = "flag_isUndoneTask";
	/**
	 * 标志：一键更新
	 */
	public static final String FLAG_ONEKEYUPDATE = "flag_onekeyupdate";
	/**
	 * 标志：继续因流量不足而暂停的任务
	 */
	public static final String FLAG_CONTINUEPAUSETASK = "flag_continuepausetask";
	/**
	 * 标志：继续因流量不足而暂停的任务
	 */
	public static final String FLAG_BUNDLECONTINUEPAUSETASK = "flag_bundlecontinuepausetask";
	/**
	 * 标志：是否停止所有下载
	 */
	public static final String FLAG_ISSTOPALLDWONLOAD = "flag_isStopAllDownload";
	/**
	 * 标志：云恢复
	 */
	public static final String FLAG_CLOUDRESTORE = "flag_cloudrestore";

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
	 * sharepreferences--定时删除垃圾文件
	 */
	public static final String SHARE_DELETEFILETIME = "share_deletefiletime";
	/**
	 * sharepreferences--记录TOP50获取时间
	 */
	public static final String SHARE_GETTOP50TIME = "share_gettop50time";

	// ———————————————————————————————————————APK状态—————————————————————————————————
	/**
	 * 准备下载
	 */
	public static final int STATUS_OF_PREPAREDOWNLOAD = 1;
	/**
	 * 正在下载
	 */
	public static final int STATUS_OF_DOWNLOADING = 2;
	/**
	 * 被动暂停下载
	 */
	public static final int STATUS_OF_PAUSE = 3;
	/**
	 * 手动暂停下载
	 */
	public static final int STATUS_OF_PAUSE_BYHAND = 4;
	/**
	 * 下载完成
	 */
	public static final int STATUS_OF_DOWNLOADCOMPLETE = 5;
	/**
	 * 下载异常
	 */
	public static final int STATUS_OF_DOWNLOADEXCEPTION = 6;
	/**
	 * 更新
	 */
	public static final int STATUS_OF_UPDATE = 7;
	/**
	 * 准备更新
	 */
	public static final int STATUS_OF_PREPAREUPDATE = 8;
	/**
	 * 更新中
	 */
	public static final int STATUS_OF_UPDATEING = 9;
	/**
	 * 被动暂停更新
	 */
	public static final int STATUS_OF_PAUSEUPDATE = 10;
	/**
	 * 手动暂停更新
	 */
	public static final int STATUS_OF_PAUSEUPDATE_BYHAND = 11;
	// /**
	// * 忽略更新
	// */
	// public static final int STATUS_OF_IGNOREUPDATE = 12;
	/**
	 * 更新完成
	 */
	public static final int STATUS_OF_UPDATECOMPLETE = 13;
	/**
	 * 更新异常
	 */
	public static final int STATUS_OF_UPDATEEXCEPTION = 14;
	/**
	 * 取消
	 */
	public static final int STATUS_OF_CANCEL = 15;

	// ———————————————————————————————————————广播—————————————————————————————————
	/**
	 * 系统广播_网络状态改变
	 */
	public static final String BROADCAST_SYS_ACTION_CONNECTCHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	/**
	 * 系统广播_app安装成功
	 */
	public static final String BROADCAST_SYS_ACTION_APPINSTALL = "android.intent.action.PACKAGE_ADDED";
	/**
	 * 系统广播_App卸载成功
	 */
	public static final String BROADCAST_SYS_ACTION_APPREMOVE = "android.intent.action.PACKAGE_REMOVED";
	/**
	 * 广播ACTION_下载
	 */
	public static final String BROADCAST_ACTION_DOWNLOAD = "com.dongji.market.action.receiver.DOWNLOAD";
	/**
	 * 广播ACTION_更新
	 */
	public static final String BROADCAST_ACTION_UPDATE = "com.dongji.market.action.receiver.UPDATE";
	/**
	 * 广播ACTION_云恢复
	 */
	public static final String BROADCAST_ACTION_CLOUDRESTORE = "com.dongji.market.action.receiver.CLOUDRESTORE";
	/**
	 * 广播ACTION_更新TitleNum
	 */
	public static final String BROADCAST_ACTION_UPDATECOUNT = "com.dongji.market.action.receiver.title.updatecount";
	/**
	 * 广播ACTION_更新DownloadAdapter
	 */
	public static final String BROADCAST_ACTION_DOWNLOADADAPTER = "com.dongji.market.action.receiver.downloadapdater";
	/**
	 * 广播ACTION_下载
	 */
	public static final String BROADCAST_ACTION_TITLERECEIVER = "com.dongji.market.action.receiver.title.receiver";
	// /**
	// * 广播ACTION_下载
	// */
	// public static final String BROADCAST_ACTION_DOWNLOAD =
	// "com.dongji.market.action.receiver.DOWNLOAD";
	/**
	 * 设置界面修改了流量限制
	 */
	public static final String BROADCAST_ACTION_LIMITFLOWCHANGE = "com.dongji.market.limitFlowChange";
	/**
	 * 对话框修改了流量限制
	 */
	public static final String BROADCAST_ACTION_DIALOG_LIMITFLOWCHANGE = "com.dongji.market.dialogFlowChange";
	/**
	 * 设置流量已用完
	 */
	public static final String BROADCAST_ACTION_NOFLOW = "com.dongji.market.action.noflow";

	/**
	 * 广播_批量任务
	 */
	public static final String BROADCAST_LISTTASK = "broadcast_listtask";
	/**
	 * 广播_开始下载
	 */
	public static final String BROADCAST_STARTDOWNLOAD = "broadcast_startdownload";
	/**
	 * 广播_暂停下载
	 */
	public static final String BROADCAST_PAUSEDOWNLOAD = "broadcast_pausedownload";
	/**
	 * 广播_继续下载
	 */
	public static final String BROADCAST_CONTINUEDOWNLOAD = "broadcast_continuedownload";
	/**
	 * 广播_取消下载
	 */
	public static final String BROADCAST_CANCELDOWNLOAD = "broadcast_canceldownload";
	/**
	 * 广播_下载完成
	 */
	public static final String BROADCAST_COMPLETEDOWNLOAD = "broadcast_completedownload";
	/**
	 * 广播_取消安装文件
	 */
	public static final String BROADCAST_CANCELINSTALL = "broadcast_cancelInstall";
	/**
	 * 广播_更新
	 */
	public static final String BROADCAST_UPDATEAPP = "broadcast_updateapp";
	/**
	 * 广播_一键更新
	 */
	public static final String BROADCAST_ONEKEYUPDATE = "broadcast_onekeyupdate";
	/**
	 * 广播_开始更新
	 */
	public static final String BROADCAST_STARTUPDATE = "broadcast_startupdate";
	/**
	 * 广播_继续更新
	 */
	public static final String BROADCAST_CONTINUEUPDATE = "broadcast_continueupdate";
	/**
	 * 广播_暂停更新
	 */
	public static final String BROADCAST_PAUSEUPDATE = "broadcast_pauseupdate";
	/**
	 * 广播_取消更新
	 */
	public static final String BROADCAST_CANCELUPDATE = "broadcast_cancelupdate";
	/**
	 * 广播_忽略更新
	 */
	public static final String BROADCAST_IGNOREUPDATE = "broadcast_ignoreupdate";
	public static final String BROADCAST_IGNOREUPDATE_VERSION = "broadcast_ignoreupdate_name";
	public static final String BROADCAST_IGNOREUPDATE_PACKAGE = "broadcast_ignoreupdate_package";
	/**
	 * 广播_取消忽略更新
	 */
	public static final String BROADCAST_CANCELIGNORE = "broadcast_cancelignore";
	public static final String BROADCAST_CANCELIGNORE_PACKAGE = "broadcast_cancelignore_package";
	/**
	 * 广播_apk被删除
	 */
	public static final String BROADCAST_APKISDELETE = "broadcast_apkisdelete";
	// /**
	// * 广播_临时apk被删除
	// */
	// public static final String BROADCAST_TEMPAPKISDELETE =
	// "broadcast_tempapkisdelete";
	/**
	 * 广播_APK下载目录不存在
	 */
	public static final String BROADCAST_APKLISTISNULL = "broadcast_apklistisnull";

	public static final String BROADCAST_UPDATE_DATA_REFRESH = "com.dongji.market.update_data_refresh";

	// ———————————————————————————————————————其它—————————————————————————————————
	/**
	 * 刷新进程
	 */
	public static final int REFERENSH_PROGRESS = 1;
	/**
	 * 刷新屏幕
	 */
	public static final int REFERENSH_SCREEN = 2;

	/**
	 * 标志：异常信息
	 */
	public static final String FLAG_EXCEPTION_STATUS = "flag_exception_status";
	/**
	 * 标志：异常APKID
	 */
	public static final String FLAG_EXCEPTION_APKSAVENAME = "flag_exception_apksavename";
	/**
	 * 广播_异常处理
	 */
	public static final String BROADCAST_DOWNLOAD_ERROR = "broadcast_download_error";
	/**
	 * 广播删除
	 */
	public static final String BROADCAST_DELETE_APK_FILE = "broadcast_delete_apkfile";

	/**
	 * 广播_删除已下载安装包
	 */
	public static final String BROADCAST_DEL_DOWNLOADED_APK = "com.dongji.market.del_all_apk";

	public static final String BROADCAST_REQUEST_UPDATE_ACTION = "com.dongji.market.updateReceiver";

	public static final String CHILDISNULL = "childisnull";
	public static final String APKDOWNLOADITEM = "apkDownloadItem";
	public static final String APKDOWNLOADITEM_APKID = "apkDownloadItem_apkid";
	public static final String APPUPDATELIST = "updateList";
	public static final String SETTINGFLOWDATA = "settingflowdata";
	public static final String CANCELNOFLOWDIALOG = "cancelnoflowdialog";
	public static final String CANCELFLOWCHANGEDIALOG = "cancelchangedialog";
	public static final String HANDLER_CLEARPACKAGE = "handler_clearPackage";

	/*************** apk状态值 **********************/
	public static final int STATUS_APK_UNINSTALL = 0; // 未安装
	public static final int STATUS_APK_INSTALL = 1; //正在安装
	public static final int STATUS_APK_INSTALL_DONE = 2; //已安装
	public static final int STATUS_APK_UPDATE = 3; //正在更新
	public static final int STATUS_APK_UNUPDATE = 4; //未更新
	/*************** apk状态值 **********************/

	public static final String DIALOG_LOGIN = "dialog_login";
	public static final String BROADCAST_DIALOG_LOGIN = "broadcast_dialog_login";
	public static final String FLAG_ACTIVITY_BANDR = "flag_activity_bandr";
	public static final int ACTIVITY_RESTORE = 1;
	public static final int ACTIVITY_BACKUP = 2;
	public static final int ACTIVITY_CLOUD_BACKUP = 3;
	public static final int ACTIVITY_CLOUD_RESTORE = 4;

	public static final String FLAG_RESTORELIST = "flag_restorelist";
	public static final String BROADCAST_ACTION_SHOWBANDRLIST = "broadcast_action_showbandrlist";
	public static final String BROADCAST_ACTION_SHOWUNINSTALLLIST = "broadcast_action_showuninstalllist";

	// ——————————————————————————————登录相关——————————————————————————————————————
	public static final String LOGIN_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=mlogin";
	public static final String DIALOG_LOGIN_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=login";
	// public static final String LOGIN_SUCCESS_URL =
	// "http://192.168.0.101/wuxiuwu/index.php?g=Api&m=UserApi&a=loginsuc&message=%E7%99%BB%E9%99%86%E6%88%90%E5%8A%9F";
	public static final String REGISTER_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=register";
	public static final String REGISTER_LOGIN_SUCCESS_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=loginsuc";
	public static final String RETAKE_PWD_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=findpwd";
	public static final String CHANGE_PWD_URL = "http://www.91dongji.com/index.php?g=Api&m=UserApi&a=changepwd";

	public static final int LOGIN_SUCCESS_FLAG = 1; // 登录成功标识
	public static final int LOGIN_ONGOING_FLAG = 0; // 正在登录标识
	public static final int LOGIN_OUT_FLAG = -1; // 登出标识
	public static final String LOGIN_SOURCE = "com.dongji.market.fromDialog";
	public static final String LOGIN_SERVICE_ACTION = "com.dongji.market.loginService";
	public static final String LOGIN_STATUS_BROADCAST = "login_status_broadcast";
	public static final String LOGIN_STATUS = "loginStatus";
	

	public static final String FEEDBACK_URL = "http://bbs.91dongji.com/forum-57-1.html";

	// ———————————————————————————————回到首页广播————————————————————————————————————
	public static final String GO_HOME_BROADCAST = "com.dongji.market.goHome_broadcast";

	// ———————————————————————————————节省流量模式改变广播————————————————————————————————————
	public static final String SAVE_FLOW_BROADCAST = "com.dongji.market.saveFlow_changed_broadcast";

	public static final int THRESHOLD = Integer.MAX_VALUE;
	
	public static final int AUTO_SCRLL_TIMES = 5;
}
