package com.dongji.market.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhangkai
 */
public class AndroidUtils {
	private static TextView toast_txt;
	public static String cachePath;
	private static DisplayMetrics mDisplayMetrics;

	static {
		cachePath = Environment.getExternalStorageDirectory().getPath() + "/.dongji/dongjiMarket/cache/";
	}

	private static ZipInputStream getZipInputStream(Context context, int rawId) throws IOException {
		ZipInputStream zis = new ZipInputStream(context.getResources().openRawResource(rawId));
		ZipEntry zipEntry = zis.getNextEntry();
		if (zipEntry != null) {
			return zis;
		}
		return null;
	}

	public static void copyFile(Context context, int rawId, String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		ZipInputStream zis = getZipInputStream(context, rawId);
		byte[] b = new byte[2048];
		int num = 0;
		while ((num = zis.read(b)) != -1) {
			fos.write(b, 0, num);
		}
		zis.close();
		fos.flush();
		fos.close();
	}

	public static void copyFile(Context context, String... str) throws IOException {
		FileOutputStream fos = new FileOutputStream(str[0]);
		ZipInputStream zis = getZipInputStream(context, str[1]);
		byte[] b = new byte[2048];
		int num = 0;
		while ((num = zis.read(b)) != -1) {
			fos.write(b, 0, num);
		}
		zis.close();
		fos.flush();
		fos.close();
	}

	private static ZipInputStream getZipInputStream(Context context, String filePath) throws IOException {
		ZipInputStream zis = new ZipInputStream(context.getAssets().open(filePath));
		ZipEntry zipEntry = zis.getNextEntry();
		if (zipEntry != null) {
			return zis;
		}
		return null;
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
	 * 复制 drawable 到指定的目录
	 * 
	 * @param context
	 * @param rid
	 * @param savePath
	 * @return
	 */
	private static boolean copyImage(Context context, int rid, String savePath) {
		Bitmap bitmap = null;
		try {
			if (new File(savePath).exists()) {
				return true;
			}
			bitmap = BitmapFactory.decodeResource(context.getResources(), rid);
			if (bitmap != null) {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(savePath);
					boolean flag = bitmap.compress(CompressFormat.JPEG, 100, fos);
					fos.flush();
					return flag;
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (OutOfMemoryError e) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
		return false;
	}

	/**
	 * 检查文件夹是否存在，如不存在则直接创建
	 * 
	 * @param path
	 */
	private static void checkFileAndmkdirs(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
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
	 * 获取版本大小
	 * 
	 * @param cntext
	 * @return
	 */
	public static String getAppVersionName(Context cntext) {
		try {

			PackageManager pm = cntext.getPackageManager();
			String pkgName = cntext.getPackageName();
			PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
			String ver = pkgInfo.versionName;
			return ver;
		} catch (NameNotFoundException e) {
			return "0";
		}
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
				}
			}
			/*
			 * if (process != null) { process.destroy(); }
			 */
		}
		return false;
	}

	/**
	 * 判断设备有无 SIM 卡
	 * 
	 * @return
	 */
	public static boolean checkSIM(Context context) {
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int simState = tManager.getSimState();
		if (simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN) {
			return false;
		}
		return true;
	}

	/**
	 * root 权限静默安装 apk
	 * 
	 * @param apkPath
	 */
	public static boolean rootInstallApp(String apkPath) {

		// boolean result = false;
		// Process process = null;
		// OutputStream out = null;
		// try {
		// process = Runtime.getRuntime().exec("su");
		// out = process.getOutputStream();
		// DataOutputStream dataOutputStream = new DataOutputStream(out);
		// dataOutputStream.writeBytes("chmod 777 " + apkPath + "\n");
		// dataOutputStream
		// .writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
		// + apkPath);
		// // 提交命令
		// dataOutputStream.flush();
		// // 关闭流操作
		// dataOutputStream.close();
		// out.close();
		// int value = process.waitFor();
		//
		// // 代表成功
		// if (value == 0) {
		// result = true;
		// } else if (value == 1) { // 失败
		// result = false;
		// } else { // 未知情况
		// result = false;
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// return result;

		// Process process = null;
		// OutputStream out = null;
		// try {
		// process = Runtime.getRuntime().exec("su"); // 得到root 权限
		// out = process.getOutputStream();
		// out.write(("pm install -r " + apkPath + "\n").getBytes());// 调用安装
		// out.flush();
		// return true;
		// } catch (IOException e) {
		// System.out.println("root install:" + e);
		// } finally {
		// if (out != null) {
		// try {
		// out.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// /*
		// * if(process!=null) { process.destroy(); }
		// */
		// }
		// return false;

		// DJMarketUtils.IS_INSTALLING = true;
		// DownloadAdapter.isRootInstalling++;

		Process process = null;
		OutputStream out = null;
		InputStream in = null;
		String state = null;
		boolean result = false;
		// String[] tempString = apkPath.split("/");
		// String resultString = "";
		// resultString = tempString[tempString.length - 1];
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
					// DownloadAdapter.isRootInstalling--;
					break;
				}
			}

		} catch (IOException e) {
			System.out.println("root install:" + e);
			// DJMarketUtils.IS_INSTALLING = false;
			// DownloadAdapter.isRootInstalling--;
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
				// DJMarketUtils.IS_INSTALLING = false;
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * root 权限静默卸载应用
	 * 
	 * @param packageName
	 */
	public static void rootUninstallApp(String packageName) {
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su"); // 得到root 权限
			out = process.getOutputStream();
			out.write(("pm uninstall " + packageName + "\n").getBytes());// 调用安装
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/*
			 * if (process != null) { process.destroy(); }
			 */
		}
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
	 * 根据应用名称判断该应用是否创建桌面快捷方式
	 * 
	 * @param context
	 * @param appName
	 * @return
	 */
	public static boolean hasShortCut(Context context, String appName) {
		int systemVersion = Build.VERSION.SDK_INT;
		boolean flag = false;
		String queryUrl = "";
		if (systemVersion < 8) {
			queryUrl = "content://com.android.launcher.settings/favorites?notify=true";
		} else {
			queryUrl = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(Uri.parse(queryUrl), null, "title=?", new String[] { appName }, null);
		if (cursor != null && cursor.moveToFirst()) {
			flag = true;
		}
		if (cursor != null) {
			cursor.close();
		}
		return flag;
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
		/*
		 * for(PackageInfo info : infos) { ApplicationInfo
		 * appInfo=info.applicationInfo; int n=ApplicationInfo.FLAG_SYSTEM;
		 * System
		 * .out.println(appInfo.loadLabel(pm)+", "+info.versionCode+", "+appInfo
		 * .flags); }
		 */
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
		// String app_detail_class_name = "com.android.settings.SubSettings";

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
	 * 获取应用签名信息
	 * 
	 * @param cxt
	 * @param pkgName
	 * @return
	 */
	public static byte[] getSignInfo(Context cxt, String pkgName) {
		PackageManager pm = cxt.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
			Signature[] signs = pi.signatures;
			return signs[0].toByteArray();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断是否是平板
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	
	/**
	 * 获取物理尺寸
	 * @param context
	 * @return
	 */
	public static double getPhysicalSize(Activity context){
		DisplayMetrics dm=getScreenSize(context);
		double diagonalPixels=Math.sqrt(Math.pow(dm.widthPixels, 2)+Math.pow(dm.heightPixels, 2));
		return diagonalPixels/(160*dm.density);
	}


	/**
	 * 解析密钥字节数组
	 * 
	 * @param signature
	 */
	public static void parseSignature(byte[] signature) {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
			String pubKey = cert.getPublicKey().toString();
			String signNumber = cert.getSerialNumber().toString();
			System.out.println("signName:" + cert.getSigAlgName());
			System.out.println("pubKey:" + pubKey);
			System.out.println("signNumber:" + signNumber);
			System.out.println("subjectDN:" + cert.getSubjectDN().toString());
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取设备相关信息
	 * 
	 * @param cxt
	 */
	public static Map<String, String> getDeviceInfo(Context cxt) {
		TelephonyManager tm = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId(); // GSM手机的 IMEI 和 CDMA手机的 MEID
		String imeiSV = tm.getDeviceSoftwareVersion(); // IMEI版本
		String manufacturer = getManufacturer(); // 手机制造商
		String deviceModel = Build.MODEL; // 设备型号
		String sdkVersion = Build.VERSION.SDK; // 设备SDK版本
		String sysVersion = Build.VERSION.RELEASE; // 系统版本
		String simOperator = getSimOperator(tm); // 运营商名称
		String networkType = getPhoneNetworkType(tm); // 移动网络制式
		String phoneNum = tm.getLine1Number() == null ? "Unknown" : tm.getLine1Number(); // 手机号码
		String simNum = tm.getSimSerialNumber(); // SIM卡唯一编号ID
		String usrId = tm.getSubscriberId(); // 获取客户id，在gsm中是imsi号
		// CellLocation cellLoc = tm.getCellLocation();//电话方位
		String basehandVersion = getBasehandVersion(); // 基带版本
		String kernelVersion = getKernelVersion(); // 内核版本号
		String internelVersion = Build.DISPLAY; // 内部版本号
		String versionIncremental = Build.VERSION.INCREMENTAL; // 版本增量
		String versionCodeName = Build.VERSION.CODENAME; // 版本代号
		String cellBrand = Build.BRAND; // 如：google
		String cellProduct = Build.PRODUCT; // 如：yakju
		String cellDevice = Build.DEVICE; // 如：maguro

		String signalType = tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE ? "无信号" : (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM ? "GSM信号" : "CDMA信号"); // 信号类型
		// String simOperatorName = tm.getSimState() ==
		// TelephonyManager.SIM_STATE_READY ? tm.getSimOperatorName() :
		// "SIM state error!"; //服务商名称
		boolean isRoaming = tm.isNetworkRoaming(); // 是否漫游
		String roamingState = isRoaming ? "漫游" : "非漫游";
		String cpuInfo = getCPUInfo(); // cpu信息

		// System.out.println("IMEI: " + imei + "\n" + "sdkVer: " + sdkVersion
		// + "\n" + "deviceModel: " + deviceModel + "\n" + "sysVer: "
		// + sysVersion + "\n" + "IMEI SV: " + imeiSV + "\n"
		// + "phoneNum: " + phoneNum + "\n" + "simNum: " + simNum + "\n"
		// + "usrId: " + usrId + "\n" + "basehandVer: " + basehandVersion
		// + "\n" + "kernelVer: " + kernelVersion + "\n" + "internelVer: "
		// + internelVersion + "\n" + "verIncremental: "
		// + versionIncremental + "\n" + "verCodename: " + versionCodeName
		// + "\n" + "cellBrand: " + cellBrand + "\n" + "cellDevice: "
		// + cellDevice + "\n" + "cellProduct: " + cellProduct + "\n"
		// + "signalType: " + signalType + "\n" + "simOperName: "
		// + simOperator + "\n" + "roamingState: " + roamingState + "\n"
		// + "manufacturer: " + manufacturer + "\n" + "networkType: "
		// + networkType + "\n" + "cpu info: " + cpuInfo);

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

		// Set<Entry<String, String>> set = map.entrySet();
		//
		// for (Entry<String, String> entry : set) {
		// System.out.println(entry.getKey() + ": " + entry.getValue());
		// }

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

			Class cl = Class.forName("android.os.SystemProperties");

			Object invoker = cl.newInstance();

			Method m = cl.getMethod("get", new Class[] { String.class, String.class });

			Object result = m.invoke(invoker, new Object[] { "gsm.version.baseband", "no message" });

			basehandVer = (String) result;

			// System.out.println("基带版本: " +basehandVer);

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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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

			// int version = 3;
			Class<android.os.Build.VERSION> build_version_class = android.os.Build.VERSION.class;
			// 取得 android 版本
			// java.lang.reflect.Field field =
			// build_version_class.getField("SDK_INT");
			// version = (Integer) field.get(new android.os.Build.VERSION());

			Class<android.os.Build> build_class = android.os.Build.class;
			// 取得牌子
			java.lang.reflect.Field manu_field = build_class.getField("MANUFACTURER");
			manufacturer = (String) manu_field.get(new android.os.Build());
			// 取得型號
			// java.lang.reflect.Field field2 = build_class.getField("MODEL");
			// String model = (String) field2.get(new android.os.Build());
			// 模組號碼
			// java.lang.reflect.Field device_field =
			// build_class.getField("DEVICE");
			// String device = (String) device_field.get(new
			// android.os.Build());

			// System.out.println("cellInfo : " + "牌子:" + manufacturer + " 型號:"
			// + model + " SDK版本:"
			// + version + " 模組號碼:" + device);
		} catch (Exception e) {
			// TODO: handle exception
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
			return array[1];
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
}
