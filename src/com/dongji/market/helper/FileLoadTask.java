package com.dongji.market.helper;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.dongji.market.R;
import com.dongji.market.activity.SoftwareMove_list_Activity;
import com.dongji.market.adapter.ChooseToBackupAdapter;
import com.dongji.market.adapter.SoftwareMoveAdapter;
import com.dongji.market.adapter.UninstallAdapter;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.pojo.InstalledAppInfo;

public class FileLoadTask extends AsyncTask<Void, InstalledAppInfo, Void> {

	public static final int EVENT_LOADED = 1;

	private Context context;
	private UninstallAdapter uninstallAdapter;
	private ChooseToBackupAdapter chooseToBackupAdapter;
	private SoftwareMoveAdapter softwareMoveAdapter;
	private ImageLoadTask task;
	private Drawable icon;
	private Handler handler;

	private int flag = 0;

	public FileLoadTask(Context context, UninstallAdapter adapter,
			Handler handler) {
		super();
		this.context = context;
		this.uninstallAdapter = adapter;
		this.handler = handler;
		icon = context.getResources().getDrawable(R.drawable.app_default_icon);
	}

	public FileLoadTask(Context context, ChooseToBackupAdapter adapter,
			Handler handler, int flag) {
		super();
		this.context = context;
		this.chooseToBackupAdapter = adapter;
		this.handler = handler;
		icon = context.getResources().getDrawable(R.drawable.app_default_icon);
		this.flag = flag;
	}

	public FileLoadTask(Context context, SoftwareMoveAdapter adapter,
			Handler handler, int flag) {
		super();
		this.context = context;
		this.softwareMoveAdapter = adapter;
		this.handler = handler;
		icon = context.getResources().getDrawable(R.drawable.app_default_icon);
		this.flag = flag;
	}

	@Override
	protected void onPreExecute() {// Runs on the UI thread before
									// doInBackground()
		Log.i("TAG", "onPreExecute");
		if (null != chooseToBackupAdapter) {
			chooseToBackupAdapter.clear();
		} else if (null != uninstallAdapter) {
			uninstallAdapter.clear();
		} else if (null != softwareMoveAdapter) {
			softwareMoveAdapter.clear();
		}

	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.i("TAG", "doInBackground");
		List<InstalledAppInfo> data;

		if (flag == AConstDefine.ACTIVITY_RESTORE) {
			data = DJMarketUtils.getBackupItemList(context);
		} else if (flag == SoftwareMove_list_Activity.FLAG_SDCARD
				|| flag == SoftwareMove_list_Activity.FLAG_PHONECARD) {
			data = DJMarketUtils.getInstalledAppsByFlag(context, flag);
		} else {
			data = DJMarketUtils.getInstalledApps(context);
		}
		for (InstalledAppInfo info : data) {
			if (isCancelled()) {
				return null;
			}
			publishProgress(info);// 获取一个软件信息后，通知任务更新ui
		}
		handler.sendEmptyMessage(EVENT_LOADED);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {// Runs on the UI thread after
												// doInBackground(). The
												// specified result is the value
												// returned by doInBackground
		Log.i("TAG", "onPostExecute");

		// 启动图片加载任务
		if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
			task.cancel(true);
		}
		if (null != uninstallAdapter) {
			task = new ImageLoadTask(context, uninstallAdapter);
		} else if (null != chooseToBackupAdapter) {
			task = new ImageLoadTask(context, chooseToBackupAdapter);
		} else if (null != softwareMoveAdapter) {
			task = new ImageLoadTask(context, softwareMoveAdapter);
		}
		task.execute();
	}

	@Override
	protected void onProgressUpdate(InstalledAppInfo... values) {
		Log.i("TAG", "onProgressUpdate");

		if (isCancelled()) {
			return;
		}
		InstalledAppInfo info = values[0];// 收到软件信息后，将图标更改为默认图标
		if (info != null && info.getIcon() == null) {
			info.setIcon(icon);
		}

		if (null != uninstallAdapter) {
			uninstallAdapter.addData(info);// 更新数据并显示
		} else if (null != chooseToBackupAdapter) {
			chooseToBackupAdapter.addData(info);// 更新数据并显示
		} else if (null != softwareMoveAdapter) {
			softwareMoveAdapter.setFlag(flag);
			softwareMoveAdapter.addData(info);
		}

	}

	/*
	 * private List<InstalledAppInfo> getInstalledApps() { PackageManager pm =
	 * context.getPackageManager(); List<PackageInfo> packages =
	 * pm.getInstalledPackages(0); List<InstalledAppInfo> list = new
	 * ArrayList<InstalledAppInfo>(); for (PackageInfo pInfo : packages) {
	 * InstalledAppInfo installedAppInfo = new InstalledAppInfo();
	 * ApplicationInfo info = pInfo.applicationInfo;
	 * 
	 * // 显示用户安装应用，而不显示系统程序 if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0
	 * && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 &&
	 * !info.packageName.equals("com.dongji.market")) {
	 * installedAppInfo.setAppInfo(info); //
	 * installedAppInfo.setIcon(info.loadIcon(pm));
	 * installedAppInfo.setName(info.loadLabel(pm) + "");
	 * installedAppInfo.setVersion(pInfo.versionName);
	 * installedAppInfo.setPkgName(info.packageName); // map.put("uninstall",
	 * R.drawable.uninstall); //
	 * 获取软件大小：通过PackageInfo的applicationInfo的publicSourceDir获得路径， //
	 * 再通过该路径创建一个文件new File(String dir)，得到该文件长度除以1024则取得该应用的大小 String dir =
	 * info.publicSourceDir; int size = Integer.valueOf((int) new
	 * File(dir).length()); installedAppInfo.setSize(sizeFormat(size));
	 * list.add(installedAppInfo); } }
	 * 
	 * return list; }
	 * 
	 * private String sizeFormat(int size) { if ((float) size / 1024 > 1024) {
	 * float size_mb = (float) size / 1024 / 1024; return String.format("%.2f",
	 * size_mb) + "M"; } return size / 1024 + "K"; }
	 */
}
