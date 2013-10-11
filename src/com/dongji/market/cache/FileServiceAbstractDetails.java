package com.dongji.market.cache;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * 抽象文件服务信息
 * 
 * @author yvon
 * 
 */
public abstract class FileServiceAbstractDetails implements FileServiceInterface {

	private static BitmapDownloaderTask task;
	static final BitmapFactory.Options mOptions = new BitmapFactory.Options();// bitmap选项

	static {
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;// 16位位图
		mOptions.inPurgeable = true;// BitmapFactory创建的Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
		mOptions.inInputShareable = true;// 位图能够共享一个指向数据源的引用，或者是进行一份拷贝
	}

	/**
	 * 取消潜在的下载任务
	 * 
	 * @param url
	 * @param imageview
	 * @return
	 */
	boolean cancelPotentialDownload(String url, ImageView imageview) {

		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageview);
		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {//如果同一个imageView对应的url不同，则取消旧的url对应的下载任务
				bitmapDownloaderTask.cancel(true);
			} else {// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取图片下载任务
	 * 
	 * @param imageview
	 * @return
	 */
	private BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageview) {
		if (imageview != null) {
			Drawable drawable = imageview.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	/**
	 * 弱引用持有下载的Drawable
	 * 
	 * @author yvon
	 * 
	 */
	class DownloadedDrawable extends BitmapDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;// 图片下载弱引用

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, Bitmap defaultBitmap) {
			super(defaultBitmap);
			// 能随时取得某对象的信息，但又不想影响此对象的垃圾收集
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	/**
	 * 下载图片
	 * @param url
	 * @return
	 */
	private Bitmap downloadBitmap(String url) {
		Bitmap mBitmap = null;
		InputStream is = null;
		try {
			is = getBitmapInputStream(url);
			mBitmap = BitmapFactory.decodeStream(is, null, mOptions);
		} catch (OutOfMemoryError e) {
			System.out.println("downloadBitmap:" + e + ", " + url);
			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

		return mBitmap;
	}

	/**
	 * 网络下载请求
	 * @param url
	 * @return
	 */
	private InputStream getBitmapInputStream(String url) {
		URL newurl;
		try {
			newurl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) newurl.openConnection();
			conn.setDoInput(true);
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 8192);
			// InputStream inputStream=conn.getInputStream();
			return bis;
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("getBitmapInputStream:"+e);
		}
		return null;
	}

	/**
	 * 获取url的hashcode
	 * @param url
	 * @return
	 */
	String getHashCode(String url) {
		if (!TextUtils.isEmpty(url)) {
			return String.valueOf(url.hashCode());
		}
		return null;
	}

	/**
	 * 强制下载
	 * @param url
	 * @param imageView
	 * @param defaultBitmap
	 */
	void forceDownload(String url, ImageView imageView, Bitmap defaultBitmap) {
		if (url == null) {
			imageView.setImageBitmap(defaultBitmap);
			return;
		}

		if (cancelPotentialDownload(url, imageView)) {
			try {
				task = new BitmapDownloaderTask(imageView);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task, defaultBitmap);
				imageView.setImageDrawable(downloadedDrawable);
				task.execute(url);
			} catch (RejectedExecutionException re) {
				imageView.setImageBitmap(defaultBitmap);
			}
		}
	}

	/**
	 * 图片异步下载
	 * 
	 * @author yvon
	 * 
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		String url;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageview) {
			imageViewReference = new WeakReference<ImageView>(imageview);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return downloadBitmap(url);
		}

		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				if (isCancelled()) {
					bitmap = null;
				}
				saveBitmap(url, bitmap);
				if (imageViewReference != null) {
					ImageView imageview = imageViewReference.get();
					BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageview);
					if ((this == bitmapDownloaderTask) || (mode != Mode.CORRECT)) {
						imageview.setImageBitmap(bitmap);
					}
				}
			}
		}
	}
}