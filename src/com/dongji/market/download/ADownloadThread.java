package com.dongji.market.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dongji.market.helper.AndroidUtils;

public class ADownloadThread implements Runnable, AConstDefine {

	private ADownloadApkItem aDownloadApkItem;
	private Context context;

	public ADownloadThread(ADownloadApkItem aDownloadApkItem, Context context) {
		this.aDownloadApkItem = aDownloadApkItem;
		this.context = context;
	}

	@Override
	public void run() {
		showLog("------------------getInputStream-----------------");
		HttpURLConnection conn = null;
		RandomAccessFile raf = null;
		InputStream is = null;
		URL apkUrl;
		try {
			apkUrl = new URL(aDownloadApkItem.apkUrl);

			if (apkUrl != null) {

				conn = (HttpURLConnection) apkUrl.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Range", "bytes="
						+ aDownloadApkItem.apkDownloadSize + "-");

				conn.connect();

				aDownloadApkItem.apkTotalSize = conn.getContentLength()
						+ aDownloadApkItem.apkDownloadSize;
				ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(
						context);
				aDownloadApkDBHelper.updateTotalSize(
						aDownloadApkItem.apkPackageName,
						aDownloadApkItem.apkVersionCode,
						aDownloadApkItem.apkTotalSize);

				is = conn.getInputStream();
				showLog("apkName" + aDownloadApkItem.apkName);

				showLog("------------------startDownload-----------------"
						+ aDownloadApkItem.apkTotalSize);
				if (is == null) {
					throw new IOException("inputStream is null!");
				}
				String filePath = NetTool.getAbsolutePath(
						aDownloadApkItem.apkPackageName + "_"
								+ aDownloadApkItem.apkVersionCode, "apk.temp");
				raf = new RandomAccessFile(filePath, "rw");
				raf.seek(aDownloadApkItem.apkDownloadSize);
				byte buf[] = new byte[4 * 1024];
				int readByte = 0;
				if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
					aDownloadApkItem.apkStatus = STATUS_OF_DOWNLOADING;
				} else if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREUPDATE) {
					aDownloadApkItem.apkStatus = STATUS_OF_UPDATEING;
				}
//				boolean honeycombNetwork = NetTool.getNetWorkType(context) == 3;
				while ((aDownloadApkItem.apkStatus == STATUS_OF_DOWNLOADING || aDownloadApkItem.apkStatus == STATUS_OF_UPDATEING)
						&& (readByte = is.read(buf)) != -1
						&& aDownloadApkItem.apkTotalSize > aDownloadApkItem.apkDownloadSize) {
					raf.write(buf, 0, readByte);
					aDownloadApkItem.apkDownloadSize += readByte;
					switch (NetTool.getNetWorkType(context)) {
					case 3:
						if (!ADownloadService.set3GDownloadSize(readByte)) { // 当设置限制流量用完，则停止下载
							if (aDownloadApkItem.apkStatus == STATUS_OF_DOWNLOADING) {
								aDownloadApkItem.apkStatus = STATUS_OF_PAUSE;
							} else if (aDownloadApkItem.apkStatus == STATUS_OF_UPDATEING) {
								aDownloadApkItem.apkStatus = STATUS_OF_PAUSEUPDATE;
							}
							Intent intent = new Intent(BROADCAST_ACTION_NOFLOW);
							context.sendBroadcast(intent);
							break;
						}
						break;
					case 1:
						errorOperation();
						break;
					}

				}
				if (aDownloadApkItem.apkStatus == STATUS_OF_UPDATE) {
					aDownloadApkItem.apkDownloadSize = 0;
				}
				if (aDownloadApkItem.apkTotalSize < 10240) {
					errorOperation();
					return;
				}
				if (aDownloadApkItem.apkDownloadSize == aDownloadApkItem.apkTotalSize) {
					NetTool.deleteLastSuffix(NetTool.getAbsolutePath(
							aDownloadApkItem.apkPackageName + "_"
									+ aDownloadApkItem.apkVersionCode,
							"apk.temp"));
					showLog("重命名完成");
					if (aDownloadApkItem.apkStatus == STATUS_OF_DOWNLOADING) {
						aDownloadApkItem.apkStatus = STATUS_OF_DOWNLOADCOMPLETE;
					} else {
						aDownloadApkItem.apkStatus = STATUS_OF_UPDATECOMPLETE;
					}
					Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
					intent.putExtra(BROADCAST_COMPLETEDOWNLOAD,
							aDownloadApkItem.apkPackageName + "_"
									+ aDownloadApkItem.apkVersionCode);
					Bundle bundle = new Bundle();
					bundle.putInt("status", aDownloadApkItem.apkStatus);
					intent.putExtras(bundle);
					context.sendBroadcast(intent);
				} else if (aDownloadApkItem.apkDownloadSize > aDownloadApkItem.apkTotalSize) {
					errorOperation();
				}
			}
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException:" + e);
			errorOperation();
		} catch (IOException e) {
			Log.e("important------", e.toString() + ", "
					+ aDownloadApkItem.apkDownloadSize + ", "
					+ aDownloadApkItem.apkTotalSize);
			errorOperation();
		} catch (Exception e) {
			System.out.println("Exception:" + e);
			errorOperation();
		} finally {
			try {
				if (null != raf) {
					raf.close();
				}
				if (null != is) {
					is.close();
				}

			} catch (IOException e) {
			}
			if (null != conn) {
				conn.disconnect();
			}
		}
	}

	private void errorOperation() {
		System.out.println("status:" + aDownloadApkItem.apkStatus);
		if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREDOWNLOAD
				|| aDownloadApkItem.apkStatus == STATUS_OF_PREPAREUPDATE
				|| aDownloadApkItem.apkStatus == STATUS_OF_DOWNLOADING
				|| aDownloadApkItem.apkStatus == STATUS_OF_UPDATEING) {

			System.out.println("network status:"
					+ AndroidUtils.isNetworkAvailable(context) + ", "
					+ aDownloadApkItem.apkStatus);
			Intent intent = new Intent(BROADCAST_DOWNLOAD_ERROR);
			intent.putExtra(FLAG_EXCEPTION_APKSAVENAME,
					aDownloadApkItem.apkPackageName + "_"
							+ aDownloadApkItem.apkVersionCode);
			intent.putExtra(FLAG_EXCEPTION_STATUS, aDownloadApkItem.apkStatus);

			context.sendBroadcast(intent);

		}
	}

	private void showLog(String msg) {
		Log.i("ADownloadThread", msg);
	}
}
