package com.dongji.market.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.myjson.JSONException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.dongji.market.application.AppMarket;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.DownloadEntity;

/**
 * 数据后台更新的service
 * 
 * @author zhangkai
 */
public class DataUpdateService extends Service {
	private static long lastUpdateTime = 0L; // 最后更新次的时间
	private boolean isMobileUpdate = true; // 是否开启蜂窝网络下的数据更新
	private NetWodkStatusReceiver mNetWorkReceiver; // 网络状态监听广播
	private static final int EVENT_REQUEST_UPDATE = 1; // 向服务器请求更新数据
	private static final int EVENT_REQUEST_INSTALL_UPDATE = 3; // 向服务器请求更新刚安装的程序更新
	private DataManager dataManager;
	private MyHandler mHandler;
	private AppMarket mApp;
	private Context context;
	private static final int MAX_REQUEST_UPDATE_RETRY_NUM = 3;
	private int currentRetry;
	private boolean isNetwork;
	private static final String PACKAGE_STR = "package:";

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = (AppMarket) getApplication();
		context = this;
		initData();
		registerAllReceiver();
		dataManager = DataManager.newInstance();
		initHandler();
		System.out.println("data service oncreate");
	}

	private void initData() {
		initLastUpdateTime();
	}

	/**
	 * 初始化最后一次更新数据时间
	 */
	private void initLastUpdateTime() {
		if (lastUpdateTime == 0) {
			SharedPreferences mSharedPreferences = getSharedPreferences(AConstDefine.SHARE_FILE_NAME, Context.MODE_PRIVATE);
			lastUpdateTime = mSharedPreferences.getLong(AConstDefine.LAST_UPDATE_TIME, 0);
		}
	}

	/**
	 * 注册广播监听器
	 */
	private void registerAllReceiver() {
		mNetWorkReceiver = new NetWodkStatusReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // 注册网络监听广播
		intentFilter.addAction(AConstDefine.BROADCAST_ACTION_REQUEST_SINGLE_UPDATE); // 请求单个应用的更新
		registerReceiver(mNetWorkReceiver, intentFilter);

		IntentFilter packageIntentFilter = new IntentFilter();
		packageIntentFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL); // 注册安装广播
		packageIntentFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE); // 注册卸载广播
		packageIntentFilter.addDataScheme("package");
		registerReceiver(mPackageStatusReceiver, packageIntentFilter);
	}

	/**
	 * 初始化Handler
	 */
	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("UpdateServiceHandler");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_UPDATE:// 请求更新数据
				requestUpdateData();
				break;
			case EVENT_REQUEST_INSTALL_UPDATE:// 请求安装更新
				Bundle bundle = msg.getData();
				if (bundle != null) {
					String[] data = bundle.getStringArray("updateData");
					requestInstallUpdate(data);
				}
				break;
			}
		}
	}

	/**
	 * 向服务器请求更新数据
	 */
	private void requestUpdateData() {
		try {
			requestData();// 请求软件更新数据
			writeLastUpdateTime();// 写入最后更新时间
			currentRetry = 0;
		} catch (IOException e) {
			System.out.println("request update data error!" + e);
			if (DJMarketUtils.isNetworkAvailable(this)) {
				if (currentRetry < MAX_REQUEST_UPDATE_RETRY_NUM) {
					currentRetry++;
					requestUpdateData();
				}
			} else {
				isNetwork = true;
			}
		} catch (JSONException e) {
			System.out.println("parse data error:" + e);
		}
	}

	/**
	 * 请求数据
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private void requestData() throws IOException, JSONException {
		ArrayList<ApkItem> updateList = dataManager.getUpdateList(this);
		if (updateList != null && updateList.size() > 0) {
			List<PackageInfo> infos = DJMarketUtils.getInstalledPackages(this);// 安装应用总数
			mApp.setUpdateList(updateList);
			int count = (infos == null ? 0 : infos.size());
			int k = 0;// 非系统应用数
			for (int i = 0; i < count; i++) {
				if ((infos.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (infos.get(i).applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
					k++;
				}
			}
			int num = mApp.getUpdateList().size();// 可更新应用数（包括系统应用）
			int n = num - k;
			if (n > 0) {
				System.out.println("count:" + count + ", num:" + num);
				for (int i = 0; i < n; i++) {
					if (i < mApp.getUpdateList().size()) {
						mApp.getUpdateList().remove(0);
					}
				}
			}
			Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_APP_UPDATE_DATADONE);
			sendBroadcast(intent);
		}
	}

	/**
	 * 写入数据更新后的时间
	 */
	private void writeLastUpdateTime() {
		long updateTime = System.currentTimeMillis();
		lastUpdateTime = updateTime;
		SharedPreferences mSharedPreferences = getSharedPreferences(AConstDefine.SHARE_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putLong(AConstDefine.LAST_UPDATE_TIME, updateTime);
		mEditor.commit();
	}

	/**
	 * 处理从第三方安装后的应用请求更新
	 * 
	 * @param data
	 */
	private void requestInstallUpdate(String[] data) {
		try {
			ApkItem item = dataManager.getUpdateBySingle(context, data);
			if (item != null) {
				mApp.addUpdate(item);
				System.out.println("broadcast single update!");
				Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_SINGLE_UPDATE_DONE);
				DownloadEntity entity = new DownloadEntity(item);
				Bundle bundle = new Bundle();
				bundle.putParcelable(AConstDefine.DOWNLOAD_ENTITY, entity);
				intent.putExtras(bundle);
				sendBroadcast(intent);
			}
		} catch (IOException e) {

		} catch (JSONException e) {

		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		isNetwork = false;
		mHandler.sendEmptyMessage(EVENT_REQUEST_UPDATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("data service ondestroy!");
		unregisterAllReceiver();
		removeAllMessage();
	}

	private void unregisterAllReceiver() {
		unregisterReceiver(mNetWorkReceiver);
		unregisterReceiver(mPackageStatusReceiver);
	}

	private void removeAllMessage() {
		if (mHandler != null) {
			if (mHandler.hasMessages(EVENT_REQUEST_UPDATE)) {
				mHandler.removeMessages(EVENT_REQUEST_UPDATE);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 网络状态广播、单个更新请求广播接收者
	 * 
	 * @author yvon
	 * 
	 */
	private class NetWodkStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {// 网络连接广播
				onNetworkStatusReceiver(context);
			} else if (AConstDefine.BROADCAST_ACTION_REQUEST_SINGLE_UPDATE.equals(intent.getAction())) {// 单个更新请求广播
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(AConstDefine.DOWNLOAD_ENTITY);
					Bundle requestBundle = new Bundle();
					String[] data = new String[] { String.valueOf(entity.installedVersionCode), entity.packageName };
					requestBundle.putStringArray("updateData", data);
					Message msg = mHandler.obtainMessage();
					msg.what = EVENT_REQUEST_INSTALL_UPDATE;
					msg.setData(requestBundle);
					mHandler.sendMessage(msg);
				}
			}
		}
	}

	/**
	 * 如果进入程序后没有可用网络，则需要在有可用网络的情况下进行本机软件更新的请求
	 * 
	 * @param context
	 */
	private void onNetworkStatusReceiver(Context context) {
		if (mApp.getUpdateList() == null && currentRetry < MAX_REQUEST_UPDATE_RETRY_NUM && isNetwork) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (wifiNetworkInfo.isAvailable() && wifiNetworkInfo.isConnected()) {
				currentRetry++;
				System.out.println("wifi request update!");
				mHandler.sendEmptyMessage(EVENT_REQUEST_UPDATE);
			} else if (isMobileUpdate && mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnected()) {
				currentRetry++;
				System.out.println("mobile request update!");
				mHandler.sendEmptyMessage(EVENT_REQUEST_UPDATE);
			}
		}
	}

	/**
	 * 包安装状态接收者
	 */
	private BroadcastReceiver mPackageStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = null;
			if (AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL.equals(intent.getAction())) {
				packageName = intent.getDataString();
				packageName = packageName.substring(packageName.indexOf(PACKAGE_STR) + PACKAGE_STR.length());
				onAppInstallReceiver(context, packageName);
			} else if (AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE.equals(intent.getAction())) {
				packageName = intent.getDataString();
				packageName = packageName.substring(packageName.indexOf(PACKAGE_STR) + PACKAGE_STR.length());
				onAppRemoveReceiver(context, packageName);
			}
		}
	};

	/**
	 * 应用安装广播接收
	 * 
	 * @param context
	 * @param packageName
	 */
	private void onAppInstallReceiver(Context context, String packageName) {
		int versionCode = DJMarketUtils.getInstalledAppVersionCodeByPackageName(context, packageName);
		if (versionCode != -1) {
			Bundle bundle = new Bundle();
			String[] data = new String[] { String.valueOf(versionCode), packageName };
			bundle.putStringArray("updateData", data);
			Message msg = mHandler.obtainMessage();
			msg.what = EVENT_REQUEST_INSTALL_UPDATE;
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
	}

	private void onAppRemoveReceiver(Context context, String packageName) {
		// 未做处理
	}

}
