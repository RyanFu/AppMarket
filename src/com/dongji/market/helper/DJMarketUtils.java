package com.dongji.market.helper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.activity.SoftwareMove_list_Activity;
import com.dongji.market.application.AppMarket;
import com.dongji.market.database.MarketDatabase.Setting_Service;
import com.dongji.market.listener.SinaOAuthDialogListener;
import com.dongji.market.pojo.InstalledAppInfo;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.service.DownloadService;
import com.dongji.market.widget.TencentLoginDialog;
import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.demo.OAuthV2ImplicitGrant;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.weibo.net.Weibo;

/**
 * 用于放置关于动机应用市场相关的帮助方法
 * 
 * @author zhangkai
 * 
 */
public class DJMarketUtils implements AConstDefine {
	public static final int STATUS_CAN_DOWNLOAD = 0; // 可以下载
	public static final int STATUS_NOT_NETWORK = 1; // 没有网络
	public static final int STATUS_ONLY_MOBILE = 2; // 只有手机网络，无wifi
	public static final int STATUS_SDCARD_INSUFFICIENT = 3; // 下载空间不足
	public static final int STATUS_SETTING_FLOW_INSUFFICIENT = 4; // 设置流量不足以完成此次下载
	public static final int STATUS_NOT_SDCARD = 5; // 无SD卡
	public static final int STATUS_NOT_SETTING_MOBILE_DOWNLOAD = 6; // 未设置开启蜂窝下载
	public static final int FLAG_EXTERNAL_STORAGE = 1 << 18;
	public static final int INSTALL_LOCATION_PREFER_EXTERNAL = 2;
	public static final int MOVEAPPTYPE_MOVETOSDCARD = 1;
	public static final int MOVEAPPTYPE_MOVETOPHONE = 2;
	public static final int MOVEAPPTYPE_NONE = 3;
	private static NumberFormat numberFormat = new DecimalFormat("###,###");
	
	public static String cachePath;
	private static DisplayMetrics mDisplayMetrics;

	static {
		cachePath = Environment.getExternalStorageDirectory().getPath() + "/.dongji/dongjiMarket/cache/";
	}


	/**
	 * 是否默认安装
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isDefaultInstall(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("auto_install") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否只使用wifi下载
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isOnlyWifi(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("only_wifi") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取限制流量值
	 * 
	 * @param cxt
	 * @return
	 */
	public static int getMaxFlow(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		return service.select("limit_flow");
	}

	/**
	 * 是否使用后台下载
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean backgroundDownload(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("download_bg") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否自动更新
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isAutoUpdate(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("auto_update") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否启用应用更新通知
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isUpdatePrompt(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("update_msg") == 1) {
			return true;
		}
		return false;
	}

	public static List<InstalledAppInfo> getInstalledAppsByFlag(Context context, int flag) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
		if (flag == SoftwareMove_list_Activity.FLAG_PHONECARD) {
			for (PackageInfo pInfo : packages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 && !info.packageName.equals("com.dongji.market") && info.sourceDir.substring(0, 2).equals("/d")) {
					installedAppInfo.setAppInfo(info);
					installedAppInfo.setName(info.loadLabel(pm) + "");
					installedAppInfo.setVersion(pInfo.versionName);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.moveType = getMoveType(pInfo, info);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					if (installedAppInfo.moveType != MOVEAPPTYPE_NONE) {
						list.add(installedAppInfo);
					}
				}
			}
		} else if (flag == SoftwareMove_list_Activity.FLAG_SDCARD) {

			for (PackageInfo pInfo : packages) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				ApplicationInfo info = pInfo.applicationInfo;
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 && !info.packageName.equals("com.dongji.market") && info.sourceDir.substring(0, 2).equals("/m")) {
					installedAppInfo.setAppInfo(info);
					installedAppInfo.setName(info.loadLabel(pm) + "");
					installedAppInfo.setVersion(pInfo.versionName);
					installedAppInfo.setPkgName(info.packageName);
					installedAppInfo.moveType = getMoveType(pInfo, info);
					// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
					// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
					String dir = info.publicSourceDir;
					int size = Integer.valueOf((int) new File(dir).length());
					installedAppInfo.setSize(sizeFormat(size));
					if (installedAppInfo.moveType != MOVEAPPTYPE_NONE) {
						list.add(installedAppInfo);
					}
				}
			}
		}
		return list;
	}

	public static int getMoveType(PackageInfo pInfo, ApplicationInfo info) {
		int moveType = MOVEAPPTYPE_NONE;
		if ((FLAG_EXTERNAL_STORAGE & info.flags) != 0) {
			moveType = MOVEAPPTYPE_MOVETOPHONE;
		} else if (pInfo != null) {
			int installLocation = 1;
			try {
				Field field = pInfo.getClass().getDeclaredField("installLocation");
				field.setAccessible(true);
				installLocation = field.getInt(pInfo);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (installLocation == 0) {
				moveType = MOVEAPPTYPE_MOVETOSDCARD;
				// System.out
				// .println("=======phone:" + info.loadLabel(pm));
			} else if (installLocation == -1 || installLocation == 1) {
				moveType = MOVEAPPTYPE_NONE;
				// System.out.println("=====not move:"
				// + info.loadLabel(pm));
			}
		}
		System.out.println("moveType..............." + moveType);
		return moveType;
	}

	/**
	 * 安装后是否自动删除安装包
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isAutoDelPkg(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("auto_del_pkg") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 是否开启节省流量模式
	 * 
	 * @param cxt
	 * @return
	 */
	public static boolean isSaveFlow(Context cxt) {
		Setting_Service service = new Setting_Service(cxt);
		if (service.select("save_flow") == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 判断当前网络状况及剩余可下载流量，是否可以下载
	 * 
	 * @param context
	 * @return
	 */
	public static int isCanDownload(Context context) {
		if (!isNetworkAvailable(context)) {
			return STATUS_NOT_NETWORK;
		} else if (!isSdcardExists()) {
			return STATUS_NOT_SDCARD;
		} else if (getSdcardAvalilaleSize() / 1024 / 1024 < 256) {
			return STATUS_SDCARD_INSUFFICIENT;
		} else if (!isWifiAvailable(context) && isMobileAvailable(context)) {
			if (!isOnlyWifi(context)) {
				return STATUS_ONLY_MOBILE;
			} else {
				return STATUS_NOT_SETTING_MOBILE_DOWNLOAD;
			}
		}
		return STATUS_CAN_DOWNLOAD;
	}

	/**
	 * 查询使用蜂窝下载所消耗的流量大小
	 * 
	 * @param context
	 * @return
	 */
	public static long queryUse3GDownloadSize(Context context) {
		if (DownloadService.mDownloadService != null) {
			return DownloadService.mDownloadService.getAlreadyUseGprsTraffic();
		} else {
			return 0;
		}
	}

	/**
	 * 将安装量转换如字符
	 * 
	 * @param num
	 * @return
	 */
	public static String convertionInstallNumber(Context context, long num) {
		String value = String.valueOf(num);
		if (num < 10000) {
			return numberFormat.format(num);
		} else if (num >= 10000 && num < 100000000) {
			return value.substring(0, value.length() - 4) + context.getString(R.string.myriad);
		} else {
			return value.substring(0, value.length() - 8) + context.getString(R.string.calculate);
		}
	}

	/**
	 * 判断用户是否已登录
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isLogin(Context context) {
		LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
		return !TextUtils.isEmpty(loginParams.getUserName());
	}

	/**
	 * 检测新浪账号是否登录
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSinaLogin(Context context) {
		LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
		String sina_name = loginParams.getSinaUserName();
		if (sina_name != null && sina_name.length() > 0) {
			return true;
		}
		return false;
	}

	private static final String PACKAGE_STR = "package:";

	public static String convertPackageName(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return "";
		}
		int num = packageName.indexOf(PACKAGE_STR);
		if (num != -1) {
			return packageName.substring(PACKAGE_STR.length() + num, packageName.length());
		}
		return packageName;
	}

	/**
	 * 根据包名获取软件信息
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static InstalledAppInfo getInstalledAppInfoByPackageName(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
				InstalledAppInfo installedAppInfo = new InstalledAppInfo();
				installedAppInfo.setAppInfo(appInfo);
				installedAppInfo.setIcon(appInfo.loadIcon(pm));
				installedAppInfo.setName(appInfo.loadLabel(pm) + "");
				installedAppInfo.setVersion(packageInfo.versionName);
				installedAppInfo.setPkgName(appInfo.packageName);
				// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
				// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
				String dir = appInfo.publicSourceDir;
				int size = Integer.valueOf((int) new File(dir).length());
				installedAppInfo.setSize(sizeFormat(size));
				return installedAppInfo;
			}
		} catch (NameNotFoundException e) {
			return null;
		}
		return null;
	}

	/**
	 * 获取第三方已安装软件列表
	 * 
	 * @param context
	 * @return
	 */
	public static List<InstalledAppInfo> getInstalledApps(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
		for (PackageInfo pInfo : packages) {
			InstalledAppInfo installedAppInfo = new InstalledAppInfo();
			ApplicationInfo info = pInfo.applicationInfo;

			// 显示用户安装应用，而不显示系统程序
			if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 && !info.packageName.equals("com.dongji.market")) {
				installedAppInfo.setAppInfo(info);
				installedAppInfo.setName(info.loadLabel(pm) + "");
				installedAppInfo.setVersion(pInfo.versionName);
				installedAppInfo.setVersionCode(pInfo.versionCode);
				installedAppInfo.setPkgName(info.packageName);
				// 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径，
				// 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小
				String dir = info.publicSourceDir;
				int size = Integer.valueOf((int) new File(dir).length());
				installedAppInfo.setSize(sizeFormat(size));
				list.add(installedAppInfo);
			}
		}

		return list;
	}

	/**
	 * 根据包名获取软件版本号
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static int getInstalledAppVersionCodeByPackageName(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		for (PackageInfo pInfo : packages) {
			ApplicationInfo info = pInfo.applicationInfo;
			if (info.packageName.equals(packageName)) {
				return pInfo.versionCode;
			}
		}
		return -1;
	}

	public static List<InstalledAppInfo> getBackupItemList(Context context) {
		File directory = new File(NetTool.BACKUPPATH);
		List<InstalledAppInfo> backupItemInfos = new ArrayList<InstalledAppInfo>();
		if (directory.exists()) {
			int i;
			File[] files = directory.listFiles();
			for (i = 0; i < files.length; i++) {
				InstalledAppInfo tempBackupItemInfo = getApkFileInfo(context, NetTool.BACKUPPATH + files[i].getName());
				if (null != tempBackupItemInfo) {
					backupItemInfos.add(tempBackupItemInfo);
				}
			}
		}
		return backupItemInfos;
	}

	/**
	 * 通过反射获取未安装apk信息
	 * 
	 * @param ctx
	 * @param apkPath
	 * @return
	 */
	public static InstalledAppInfo getApkFileInfo(Context context, String apkPath) {
		File apkFile = new File(apkPath);
		InstalledAppInfo backupItemInfo = new InstalledAppInfo();
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// 反射得到pkgParserCls对象并实例化，有参数
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			// 从pkgParserCls类得到parsePackage方法
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();// 显示相关，这里设为默认
			typeArgs = new Class<?>[] { File.class, String.class, DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			// 执行pkgParser_parsePackageMtd方法并返回
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

			// 从返回的对象得到名为"applicationInfo"的字段对象.当文件已损坏时，pkgParserPkg为null
			if (pkgParserPkg == null) {
				return null;
			}
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

			// 从对象"pkgParserPkg"得到字段"appInfoFld"的值
			if (appInfoFld.get(pkgParserPkg) == null) {
				return null;
			}
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

			// 反射得到assetMagCls对象并实例化，无参
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			// 从assetMagCls类得到addAssetPath方法
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			// 执行assetMag_addAssetPathMtd方法
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			// 得到Resources对象并实例化，有参数
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = resCt.newInstance(valueArgs);

			// 读取apk文件的信息
			// appInfoData = new AppInfoData();
			if (info != null) {
				if (info.icon != 0) {// 图片存在，则读取相关信息
					Drawable icon = res.getDrawable(info.icon);
					backupItemInfo.setIcon(icon);
				}
				if (info.labelRes != 0) {
					String name = (String) res.getText(info.labelRes);
					backupItemInfo.setName(name);
				} else {
					String apkName = apkFile.getName();
					backupItemInfo.setName(apkName.substring(0, apkName.lastIndexOf(".")));
				}
			} else {
				return null;
			}
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
			if (packageInfo != null) {
				backupItemInfo.setVersion(packageInfo.versionName + "");
				backupItemInfo.setPkgName(packageInfo.packageName);
				backupItemInfo.setVersionCode(packageInfo.versionCode);
			}
			backupItemInfo.setSize(sizeFormat(apkFile.length()));
			return backupItemInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 格式化数据大小
	 * 
	 * @param size
	 * @return
	 */
	public static String sizeFormat(int size) {
		if ((float) size / 1024 > 1024) {
			float size_mb = (float) size / 1024 / 1024;
			return String.format("%.2f", size_mb) + "M";
		}
		return size / 1024 + "K";
	}

	public static long sizeFromMToLong(String sizeString) {
		long size = 0;
		if (sizeString.endsWith("M")) {
			sizeString = sizeString.substring(0, sizeString.length() - 1);
			size = (long) ((Double.valueOf(sizeString)) * 1024 * 1024);
		} else if (sizeString.endsWith("K")) {
			sizeString = sizeString.substring(0, sizeString.length() - 1);
			size = (long) ((Double.valueOf(sizeString)) * 1024);
		}
		return size;
	}

	/**
	 * 新浪登录对话框
	 * 
	 * @param context
	 * @param handler
	 */
	public static void sinaLogin(Activity context, Handler handler) {
		final String CONSUMER_KEY = "1699956234";// 替换为开发者的appkey，例如"1646212960";
		final String CONSUMER_SECRET = "c01ed617178219c1344777e27623cade";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";

		if (isNetworkAvailable(context)) {
			Weibo weibo = Weibo.getInstance();
			weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);

			// Oauth2.0
			// 隐式授权认证方式
			weibo.setRedirectUrl("http://91dongji.com");// 此处回调页内容应该替换为与appkey对应的应用回调页
			// 对应的应用回调页可在开发者登陆新浪微博开发平台之后，
			// 进入我的应用--应用详情--应用信息--高级信息--授权设置--应用回调页进行设置和查看，
			// 应用回调页不可为空
			weibo.authorize(context, new SinaOAuthDialogListener(context, handler, CONSUMER_KEY, CONSUMER_SECRET));
		} else {
			showToast(context, R.string.net_error);
		}
	}

	public static String getCookieValue(Context context, String url, String key) {
		CookieSyncManager csm = CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		csm.sync();
		String cookieStr = cookieManager.getCookie(url);
		System.out.println("========cookied:" + cookieStr);
		String[] strs = cookieStr.split(";");
		String value = null;
		for (String string : strs) {
			if (string.trim().startsWith(key)) {
				value = string.substring(string.indexOf("=") + 1);
				break;
			}
		}
		return value;
	}

	public static String urlDecode(String urlCodeStr) {
		String str = null;
		try {
			str = URLDecoder.decode(urlCodeStr, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static void appUpdate(Context context, String url) {
		HttpURLConnection httpURLConnection = null;
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			URL mURL = new URL(url);
			httpURLConnection = (HttpURLConnection) mURL.openConnection();
			httpURLConnection.setConnectTimeout(10000);
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			is = httpURLConnection.getInputStream();
			String path = getSdcardFile() + "/" + context.getPackageName() + ".apk";
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);
			int i = 0;
			byte[] data = new byte[1024];
			while ((i = is.read(data)) != -1) {
				fos.write(data, 0, i);
			}
			fos.flush();
			if (context != null && !((Activity) context).isFinishing()) {
				showToast(context, R.string.app_download_done);
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
				context.startActivity(installIntent);
			}
		} catch (IOException e) {
			System.out.println("app update:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static void tencentLogin(Activity context, Handler handler) {
		// !!!请根据您的实际情况修改!!! 认证成功后浏览器会被重定向到这个url中 必须与注册时填写的一致
		String redirectUri = "http://www.91dongji.com/";
		// !!!请根据您的实际情况修改!!! 换为您为自己的应用申请到的APP KEY
		String clientId = "801317350";
		// !!!请根据您的实际情况修改!!! 换为您为自己的应用申请到的APP SECRET
		String clientSecret = "0b46e85979a4d529cf44b864900406d2";

		OAuthV2 oAuth;

		if (isNetworkAvailable(context)) {
			oAuth = new OAuthV2(redirectUri);
			oAuth.setClientId(clientId);
			oAuth.setClientSecret(clientSecret);

			// 关闭OAuthV2Client中的默认开启的QHttpClient。
			OAuthV2Client.getQHttpClient().shutdownConnection();

			new TencentLoginDialog(context, oAuth, handler).show();
		} else {
			showToast(context, R.string.net_error);
		}
	}

	public static void getTencentUsrInfo(Context context, OAuthV2 oAuth, Handler handler) {

		String response;
		UserAPI userAPI;
		JSONObject jsonObject;

		if (oAuth.getStatus() != 0) {
			Toast.makeText(context, R.string.tencetn_oAuth_failed, Toast.LENGTH_SHORT).show();
		} else {
			userAPI = new UserAPI(OAuthConstants.OAUTH_VERSION_2_A);
			LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
			try {
				response = userAPI.info(oAuth, "json");// 调用QWeiboSDK获取用户信息
				jsonObject = new JSONObject(new JSONObject(response).getString("data"));
				loginParams.setTencentUserName(jsonObject.getString("nick"));
				loginParams.setTencent_oAuth(oAuth);
				handler.sendEmptyMessage(OAuthV2ImplicitGrant.TENCENT_LOGIN_SUCCESS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			userAPI.shutdownConnection();
		}
		return;
	}

	public static boolean isTencentLogin(Context context) {
		LoginParams loginParams = ((AppMarket) context.getApplicationContext()).getLoginParams();
		String tencent_nick = loginParams.getTencentUserName();
		if (tencent_nick != null && tencent_nick.length() > 0) {
			return true;
		}
		return false;
	}

	
	

	
	/**
	 * 自定义Toast
	 */
	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 自定义Toast
	 */
	public static void showToast(Context context, int id) {
		showToast(context, context.getResources().getString(id));
	}

	/**
	 * 获取 SD 卡
	 * 
	 * @return
	 */
	public static File getSdcardFile() {
		return Environment.getExternalStorageDirectory();
	}

	/**
	 * 检查文件是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkFileExists(String path) {
		File file = new File(path);
		return file.exists();
	}

	/**
	 * 判断 SD 卡是否存在
	 * 
	 * @return
	 */
	public static boolean isSdcardExists() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取 SD 卡剩余大小
	 * 
	 * @return
	 */
	public static long getSdcardAvalilaleSize() {
		File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
		if (path != null && path.exists()) {
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}
		return 0;
	}

	/**
	 * 根据路径删除此路径下所有文件
	 * 
	 * @param filePath
	 */
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteFile(files[i].getPath());
					} else {
						files[i].delete();
					}
				}
			}
			file.delete();
		}
	}

	/**
	 * 判断网络是否有效
	 * 
	 * @param context
	 *            上下文对象
	 * @return boolean -- TRUE 有效 -- FALSE 无效
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (context != null) {
			ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (manager == null) {
				return false;
			}
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			if (networkinfo == null || !networkinfo.isAvailable()) {
				return false;
			}

			if (networkinfo.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 验证当前wifi状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiAvailable(Context context) {
		int type = ConnectivityManager.TYPE_WIFI;
		return isAvailableByType(context, type);
	}

	/**
	 * 验证当前mobile状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileAvailable(Context context) {
		int type = ConnectivityManager.TYPE_MOBILE;
		return isAvailableByType(context, type);
	}

	/**
	 * 根据状态验证网络
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	private static boolean isAvailableByType(Context context, int type) {
		if (context != null) {
			ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (manager != null) {
				NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
				for (int i = 0; i < networkInfos.length; i++) {
					if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
						if (networkInfos[i].getType() == type) {
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	/**
	 * 获取屏幕尺寸
	 * 
	 * @param context
	 * @return
	 */
	public static DisplayMetrics getScreenSize(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm;
	}

	/**
	 * 验证邮箱格式是否正确
	 * 
	 * @param emailStr
	 * @return
	 */
	public static boolean isEmail(String emailStr) {
		String patternStr = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(emailStr);
		return m.matches();
	}

	/**
	 * 验证密码长度是否在6－18位之间
	 * 
	 * @param passwordStr
	 * @return
	 */
	public static boolean passwdFormat(String passwordStr) {
		int len = passwordStr.length();
		if (len >= 6 && len <= 18) {
			return true;
		}
		return false;
	}

	/**
	 * 获取安装包信息
	 * 
	 * @param packageName
	 * @return
	 */
	public static PackageInfo getPackageInfo(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			return pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断手机是否root
	 * 
	 * @return
	 */
	public static boolean isRoot() {
		Process process = null;
		DataOutputStream dos = null;
		try {
			process = Runtime.getRuntime().exec("su");
			dos = new DataOutputStream(process.getOutputStream());
			dos.writeBytes("exit\n");
			dos.flush();
			int exitValue = process.waitFor();
			if (exitValue == 0) {
				return true;
			}
		} catch (IOException e) {

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

	/**
	 * root 权限静默安装 apk
	 * 
	 * @param apkPath
	 */
	public static boolean rootInstallApp(String apkPath) {
		Process process = null;
		OutputStream out = null;
		InputStream in = null;
		String state = null;
		boolean result = false;
		try {
			process = Runtime.getRuntime().exec("su"); // 得到root 权限
			out = process.getOutputStream();
			out.write(("pm install -r " + apkPath + "\n").getBytes());
			// 调用安装，将文件写入到process里面
			// in = process.getInputStream();//拿到输出流，开始安装操作
			in = process.getErrorStream();
			int len = 0;
			byte[] bs = new byte[256];
			while ((len = in.read(bs)) != -1) {
				// System.out.println("install len ===> " + len);
				state = new String(bs, 0, len);
				System.out.println("install info -----> " + state);
				if (state.equals("Success\n")) {
					result = true;
					break;
				} else if (state.indexOf("Failure") != -1) {
					break;
				}
			}

		} catch (IOException e) {
			System.out.println("root install:" + e);
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (dpValue * mDisplayMetrics.density + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (pxValue / mDisplayMetrics.density + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (pxValue / mDisplayMetrics.scaledDensity);
	}

	/**
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int sp2px(Context context, float pxValue) {
		if (mDisplayMetrics == null) {
			mDisplayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (pxValue * mDisplayMetrics.scaledDensity);
	}

	/**
	 * 获取已安装软件列表
	 * 
	 * @param context
	 * @return
	 */
	public static List<PackageInfo> getInstalledPackages(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> infos = pm.getInstalledPackages(0);
		return infos;
	}

	/**
	 * 获取状态栏信息
	 * 
	 * @param context
	 * @return
	 */
	public static Rect getStatusBarInfo(Activity context) {
		Rect frame = new Rect();
		context.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return frame;
	}

	/**
	 * 获取控件在屏幕上的坐标值,x、y保存在int[2]中
	 * 
	 * @param view
	 * @return
	 */
	public static int[] getViewLocation(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location;
	}

	/**
	 * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息 对于Android 2.3（Api Level
	 * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void showInstalledAppDetails(Context context, String packageName) {
		String scheme = "package";
		String app_pkg_name_21 = "com.android.settings.ApplicationPkgName"; // 调用系统InstalledAppDetails界面所需的Extra名称(用于Android
																			// 2.1及之前版本)
		String app_pkg_name_22 = "pkg"; // 调用系统InstalledAppDetails界面所需的Extra名称(用于Android
										// 2.2)
		String app_detail_pkg_name = "com.android.settings"; // InstalledAppDetails所在包名
		String app_detail_class_name = "com.android.settings.InstalledAppDetails"; // InstalledAppDetails类名

		Intent intent = new Intent();
		int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3以上版本，直接调用接口
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			Uri uri = Uri.fromParts(scheme, packageName, null);
			intent.setData(uri);
		} else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）,2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			String appPkgName = apiLevel == 8 ? app_pkg_name_22 : app_pkg_name_21;
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(app_detail_pkg_name, app_detail_class_name);
			intent.putExtra(appPkgName, packageName);
		}
		context.startActivity(intent);
	}

	/**
	 * 获取物理尺寸
	 * 
	 * @param context
	 * @return
	 */
	public static double getPhysicalSize(Activity context) {
		DisplayMetrics dm = getScreenSize(context);
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		return diagonalPixels / (160 * dm.density);
	}

	/**
	 * 获取设备相关信息
	 * 
	 * @param cxt
	 */
	public static Map<String, String> getDeviceInfo(Context cxt) {
		TelephonyManager tm = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId(); // GSM手机的 IMEI 和 CDMA手机的 MEID
		String manufacturer = getManufacturer(); // 手机制造商
		String deviceModel = Build.MODEL; // 设备型号
		String sdkVersion = Build.VERSION.SDK; // 设备SDK版本
		String sysVersion = Build.VERSION.RELEASE; // 系统版本
		String simOperator = getSimOperator(tm); // 运营商名称
		String networkType = getPhoneNetworkType(tm); // 移动网络制式
		String phoneNum = tm.getLine1Number() == null ? "Unknown" : tm.getLine1Number(); // 手机号码
		String simNum = tm.getSimSerialNumber(); // SIM卡唯一编号ID
		String usrId = tm.getSubscriberId(); // 获取客户id，在gsm中是imsi号
		String basehandVersion = getBasehandVersion(); // 基带版本
		String kernelVersion = getKernelVersion(); // 内核版本号
		String internelVersion = Build.DISPLAY; // 内部版本号

		String signalType = tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE ? "无信号" : (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM ? "GSM信号" : "CDMA信号"); // 信号类型
		boolean isRoaming = tm.isNetworkRoaming(); // 是否漫游
		String roamingState = isRoaming ? "漫游" : "非漫游";
		String cpuInfo = getCPUInfo(); // cpu信息
		Map<String, String> map = new HashMap<String, String>();
		map.put("imei_num", imei);
		map.put("manufacturer", manufacturer);
		map.put("device_model", deviceModel);
		map.put("sdk_version", sdkVersion);
		map.put("sys_version", sysVersion);
		map.put("sim_operator", simOperator);
		map.put("network_type", networkType);
		map.put("phone_num", phoneNum);
		map.put("sim_num", simNum);
		map.put("usr_id", usrId);
		map.put("basehand_version", basehandVersion);
		map.put("kernel_version", kernelVersion);
		map.put("internel_version", internelVersion);
		map.put("signal_type", signalType);
		map.put("roaming_state", roamingState);
		map.put("cpu_info", cpuInfo);
		return map;
	}

	/**
	 * 获取基带版本号
	 * 
	 * @return
	 */
	public static String getBasehandVersion() {

		String basehandVer = null;
		// 获取基带版本
		try {

			Class<?> cl = Class.forName("android.os.SystemProperties");

			Object invoker = cl.newInstance();

			Method m = cl.getMethod("get", new Class[] { String.class, String.class });

			Object result = m.invoke(invoker, new Object[] { "gsm.version.baseband", "no message" });

			basehandVer = (String) result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return basehandVer;
	}

	/**
	 * 获取内核版本号
	 * 
	 * @return
	 */
	public static String getKernelVersion() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr, 8 * 1024);
		String result = "";
		String line;
		try {
			while ((line = br.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result != "") {
			String keyword = "version ";
			int index = result.indexOf(keyword);
			line = result.substring(index + keyword.length());
			index = line.indexOf(" ");
			return line.substring(0, index);
		}
		return "Unknown";
	}

	/**
	 * 移动网络制式
	 * 
	 * @param tm
	 * @return
	 */
	public static String getPhoneNetworkType(TelephonyManager tm) {
		String networkType = "Unknown";
		switch (tm.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			networkType = "1xRTT:";
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			networkType = "CDMA:";
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			networkType = "EDGE:";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			networkType = "EVDO_0:";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			networkType = "EVDO_A:";
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			networkType = "GPRS:";
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			networkType = "HSDPA:";
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			networkType = "HSPA:";
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			networkType = "HSUPA:";
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			networkType = "UMTS:";
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			networkType = "UNKNOWN:";
			break;
		default:
			break;
		}
		return networkType + tm.getNetworkType();
	}

	/**
	 * 获取运营商
	 * 
	 * @param tm
	 * @return
	 */
	public static String getSimOperator(TelephonyManager tm) {
		// Returns the MCC+MNC (mobile country code + mobile network code) of
		// the provider of the SIM. 5 or 6 decimal digits.
		// 获取SIM卡提供的移动国家码和移动网络码.5或6位的十进制数字.
		String operator = tm.getSimState() == TelephonyManager.SIM_STATE_READY ? tm.getSimOperator() : null;
		if (operator != null) {
			// 460是国家代码，后面两位是运营商代码
			if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
				return "中国移动";
			} else if (operator.equals("46001")) {
				return "中国联通";
			} else if (operator.equals("46003")) {
				return "中国电信";
			}
		}
		return null;
	}

	/**
	 * 获取手机制造商
	 * 
	 * @return
	 */
	public static String getManufacturer() {
		String manufacturer = "Unknown";
		try {
			Class<android.os.Build> build_class = android.os.Build.class;
			// 取得牌子
			java.lang.reflect.Field manu_field = build_class.getField("MANUFACTURER");
			manufacturer = (String) manu_field.get(new android.os.Build());
		} catch (Exception e) {
		}
		return manufacturer;

	}

	/**
	 * 获取CPU信息
	 * 
	 * @return
	 */
	public static String getCPUInfo() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			br.close();
			return array[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getLanguageType() {
		String language = Locale.getDefault().getLanguage();
		String country = Locale.getDefault().getCountry();
		if (language.equals("zh")) {
			if (country.equals("TW") || country.equals("HK")) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 判断是否是手机
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isPhone(Activity context) {
		return ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) || getPhysicalSize(context) < 6;
	}
	
	/**
	 * 格式化文件大小
	 * 
	 * @param size
	 * @return
	 */
	public static String sizeFormat(long size) {
		if ((float) size / 1024 > 1024) {
			float size_mb = (float) size / 1024 / 1024;
			return String.format("%.2f", size_mb) + "MB";
		}
		return size / 1024 + "KB";
	}


}
