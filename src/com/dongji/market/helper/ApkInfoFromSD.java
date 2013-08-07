package com.dongji.market.helper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import com.dongji.market.R;
import com.dongji.market.pojo.AppInfoData;

public class ApkInfoFromSD {

	Context ctx;
	AppInfoData appInfo;
	List<HashMap<String, Object>> list;

	public ApkInfoFromSD(Context context) {
		super();
		this.ctx = context;
		list = new ArrayList<HashMap<String, Object>>();
	}

	/**
	 * 遍历sdCard中的apk文件，以集合返回
	 * 
	 * @param file
	 * @return
	 */
	public List<HashMap<String, Object>> traverseApp(File file, Handler mHandler) {
		if (file.isFile()) {
			if (file.getName().toLowerCase().endsWith(".apk")) {

				appInfo = getApkFileInfo(file.getAbsolutePath());
				if (appInfo != null) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("icon", appInfo.getAppIcon());
					map.put("name", appInfo.getAppName());
					map.put("version", appInfo.getAppVerName());
					map.put("date", appInfo.getAppLastModified());
					map.put("size", appInfo.getAppSize());
					map.put("path", appInfo.getAppPath());
					if (appInfo.isInstalled()) {
						map.put("isInstalled",
								ctx.getResources().getString(
										R.string.has_installed));
					} else {
						map.put("isInstalled",
								ctx.getResources().getString(
										R.string.no_installed));
					}

					Message msg = new Message();
					msg.obj = map;
					msg.what = 1;
					mHandler.sendMessage(msg);
					list.add(map);

					list.add(map);
				}
			}

		} else {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File mfile : files) {
					traverseApp(mfile, mHandler);
				}
			}
		}
		return list;
	}

	/**
	 * 通过反射获取未安装apk信息
	 * 
	 * @param ctx
	 * @param apkPath
	 * @return
	 */
	public AppInfoData getApkFileInfo(String apkPath) {
		File apkFile = new File(apkPath);
		// if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
		// System.out.println("文件路径不正确");
		// return null;
		// }

		AppInfoData appInfoData = new AppInfoData();
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
			typeArgs = new Class<?>[] { File.class, String.class,
					DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			// 执行pkgParser_parsePackageMtd方法并返回
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);

			// 从返回的对象得到名为"applicationInfo"的字段对象.当文件已损坏时，pkgParserPkg为null
			if (pkgParserPkg == null) {
				String apkName = apkFile.getName();
				appInfoData.setAppName(apkName.substring(0,
						apkName.lastIndexOf(".")));
				appInfoData.setAppIcon(ctx.getResources().getDrawable(
						R.drawable.icon));
				appInfoData.setAppVerCode("");
				appInfoData.setAppVerName(ctx.getResources().getString(
						R.string.app_destroyed));
				appInfoData.setInstalled(false);
				appInfoData.setAppLastModified(dateFormat(apkFile
						.lastModified()));
				appInfoData.setAppSize(sizeFormat(apkFile.length()));
				appInfoData.setAppPath(apkPath);
				return appInfoData;
			}
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");

			// 从对象"pkgParserPkg"得到字段"appInfoFld"的值
			if (appInfoFld.get(pkgParserPkg) == null) {
				return null;
			}
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);

			// 反射得到assetMagCls对象并实例化，无参
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			// 从assetMagCls类得到addAssetPath方法
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			// 执行assetMag_addAssetPathMtd方法
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			// 得到Resources对象并实例化，有参数
			Resources res = ctx.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class
					.getConstructor(typeArgs);
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
					appInfoData.setAppIcon(icon);
				}
				if (info.labelRes != 0) {
					String name = (String) res.getText(info.labelRes);
					appInfoData.setAppName(name);
				} else {
					String apkName = apkFile.getName();
					appInfoData.setAppName(apkName.substring(0,
							apkName.lastIndexOf(".")));
				}
				String pkgName = info.packageName;
				appInfoData.setAppPackageName(pkgName);
			} else {
				return null;
			}
			PackageManager pm = ctx.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			if (packageInfo != null) {
				appInfoData.setAppVerCode(packageInfo.versionCode + "");
				appInfoData.setAppVerName(packageInfo.versionName);
				appInfoData.setInstalled(isInstalled(pm, info.packageName));
			}
			appInfoData.setAppLastModified(dateFormat(apkFile.lastModified()));
			appInfoData.setAppSize(sizeFormat(apkFile.length()));
			appInfoData.setAppPath(apkPath);
			return appInfoData;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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

	/**
	 * 格式化文件修改日期
	 * 
	 * @param milliseconds
	 * @return
	 */
	private static String dateFormat(long milliseconds) {
		Date date = new Date();
		date.setTime(milliseconds);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
		return dateFormat.format(date);
	}

	/**
	 * getPackgeInfo(packageName, flags)方法会根据给定包名检索是否安装过此软件
	 * 
	 * @param pm
	 * @param packageName
	 * @return
	 */
	private static boolean isInstalled(PackageManager pm, String packageName) {
		try {
			pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return false;
		}
		// List<PackageInfo> pInfoList = pm.getInstalledPackages(0);
		// for (PackageInfo packageInfo : pInfoList) {
		// if (packageName.equals(packageInfo.packageName)) {
		// return true;
		// }
		// }
		// return false;
		return true;
	}
}
