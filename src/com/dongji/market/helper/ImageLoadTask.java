package com.dongji.market.helper;

import com.dongji.market.adapter.ChooseToBackupAdapter;
import com.dongji.market.adapter.SoftwareMoveAdapter;
import com.dongji.market.adapter.UninstallAdapter;
import com.dongji.market.pojo.InstalledAppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class ImageLoadTask extends AsyncTask<Void, Void, Void> {

	private UninstallAdapter uninstallAdapter;
	private ChooseToBackupAdapter chooseToBackupAdapter;
	private SoftwareMoveAdapter softwareMoveAdapter;
	private Context context;

	public ImageLoadTask(Context context, UninstallAdapter adapter) {
		this.context = context;
		this.uninstallAdapter = adapter;
	}

	public ImageLoadTask(Context context, ChooseToBackupAdapter adapter) {
		this.context = context;
		this.chooseToBackupAdapter = adapter;
	}

	public ImageLoadTask(Context context, SoftwareMoveAdapter adapter) {
		this.context = context;
		this.softwareMoveAdapter = adapter;
	}

	@Override
	protected Void doInBackground(Void... params) {

		if (null != uninstallAdapter) {
			for (int i = 0; i < uninstallAdapter.getCount(); i++) {
				InstalledAppInfo info = (InstalledAppInfo) uninstallAdapter.getItem(i);
				Drawable icon = getIcon(info.getAppInfo());// 获取软件图标过程
				if (icon != null) {
					info.setIcon(icon);// 将取到的图标存入map
					publishProgress();// 通知更新ui
				}
			}
		} else if (null != chooseToBackupAdapter) {
			for (int i = 0; i < chooseToBackupAdapter.getCount(); i++) {
				InstalledAppInfo info = (InstalledAppInfo) chooseToBackupAdapter.getItem(i);
				Drawable icon = null;
				if (info.getAppInfo() != null) {
					icon = getIcon(info.getAppInfo());// 获取软件图标过程
				}
				if (icon != null) {
					info.setIcon(icon);// 将取到的图标存入map
					publishProgress();// 通知更新ui
				}
			}
		} else if (null != softwareMoveAdapter) {
			for (int i = 0; i < softwareMoveAdapter.getCount(); i++) {
				InstalledAppInfo info = (InstalledAppInfo) softwareMoveAdapter.getItem(i);
				Drawable icon = null;
				if (info.getAppInfo() != null) {
					icon = getIcon(info.getAppInfo());// 获取软件图标过程
				}
				if (icon != null) {
					info.setIcon(icon);// 将取到的图标存入map
					publishProgress();// 通知更新ui
				}
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		Log.i("TAG", "onProgressUpdate");
		if (isCancelled()) {
			return;
		}
		if (null != uninstallAdapter) {
			uninstallAdapter.notifyDataSetChanged();// 更新ui
		} else if (null != chooseToBackupAdapter) {
			chooseToBackupAdapter.notifyDataSetChanged();// 更新ui
		} else if (null != softwareMoveAdapter) {
			softwareMoveAdapter.notifyDataSetChanged();
		}
	}

	private Drawable getIcon(ApplicationInfo appInfo) {
		PackageManager pm = context.getPackageManager();
		return appInfo.loadIcon(pm);
	}

}
