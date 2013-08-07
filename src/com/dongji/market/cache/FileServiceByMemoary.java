package com.dongji.market.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

public class FileServiceByMemoary extends FileServiceAbstractDetails {
	
	@Override
	public void loadFileToMap() {
		// for memory, nothing to do.
	}
	
	private Bitmap getBitmapByCache(String url, ImageView imageView,
			Bitmap defaultBitmap, Integer scrollState) {
		resetPurgeTimer();
		// scrollState 0:停止状态，需要加载图片(cache -> network)
		// scrollState 1/2:滚动状态，此时就用cache图片，如果cache没有就用默认defaultBitmap
		Bitmap bitmap = getBitmapFromCache(url);
		if (scrollState != SCROLL_STATUS_STOP) {// 处于滚动状态
			if (bitmap == null) {
				imageView.setImageBitmap(defaultBitmap);
			} else {
				imageView.setImageBitmap(bitmap);
			}
//			return;
		}
		return bitmap;
	}

	@Override
	public void getBitmap(String url, ImageView imageView,
			Bitmap defaultBitmap, Integer scrollState) {

		Bitmap bitmap = getBitmapByCache(url, imageView, defaultBitmap, scrollState);
		
		if (bitmap == null) {
			forceDownload(url, imageView, defaultBitmap);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}
	

	@Override
	public void getBitmap(String url, ImageView imageView,
			Bitmap defaultBitmap, boolean isRemote) {
		// TODO Auto-generated method stub
		Bitmap bitmap = getBitmapByCache(url, imageView, defaultBitmap, 0);
		
		if (bitmap == null) {
			if(!isRemote) {
				imageView.setImageBitmap(defaultBitmap);
			}else {
				forceDownload(url, imageView, defaultBitmap);
			}
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	// public void setMode(Mode mode) {
	// this.mode = mode;
	// clearCache();
	// }

	private static final int HARD_CACHE_CAPACITY = 30;
	private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds
	private final static HashMap<String, Bitmap> mHardBitmapCache = new LinkedHashMap<String, Bitmap>(
			HARD_CACHE_CAPACITY, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(
				LinkedHashMap.Entry<String, Bitmap> eldest) {
			if (size() > HARD_CACHE_CAPACITY) {
				// Entries push-out of hard reference cache are transferred to
				// soft reference cache
				mSoftBitmapCache.put(eldest.getKey(),
						new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
		}
	};
	/*
	 * SoftReference 指到的对象，即使没有任何 Direct Reference，也不会被清除。 一直要到 JVM 内存不足时且 没有
	 * Direct Reference 时才会清除，SoftReference 是用来设计 object-cache 之用的。 不但可以把对象
	 * cache 起来，也不会造成内存不足的错误 （OutOfMemoryError）
	 */
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> mSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
			HARD_CACHE_CAPACITY);
	private final static Handler mPurgeHandler = new Handler();
	private final static Runnable mPurgerRunnable = new Runnable() {
		public void run() {
			clearCache();
		}
	};

	private static Bitmap getBitmapFromCache(String url) {
		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(url);
			if (bitmap != null) {
				mHardBitmapCache.remove(url);
				mHardBitmapCache.put(url, bitmap);
				return bitmap;
			}
		}

		if (mSoftBitmapCache != null) {
			SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(url);
			if (bitmapReference != null) {
				final Bitmap bitmap = bitmapReference.get();
				if (bitmap != null) {
					return bitmap;
				} else {
					mSoftBitmapCache.remove(url);
				}
			}
		}
		return null;
	}

	public static void clearCache() {
		mHardBitmapCache.clear();
		mSoftBitmapCache.clear();
	}

	private static void resetPurgeTimer() {
		mPurgeHandler.removeCallbacks(mPurgerRunnable);
		mPurgeHandler.postDelayed(mPurgerRunnable, DELAY_BEFORE_PURGE);
	}

	@Override
	public boolean getFile(String path, String targetPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean clearFile(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void saveBitmap(String url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (mHardBitmapCache) {
				mHardBitmapCache.put(url, bitmap);
			}
		}
	}

	@Override
	public int getCurrentCacheBitmapNumbers() {
		int numbers = 0;
		if (mHardBitmapCache != null) {
			numbers = mHardBitmapCache.size();
		}
		if (mSoftBitmapCache != null) {
			numbers = numbers + mSoftBitmapCache.size();
		}
		return numbers;
	}

	@Override
	public boolean clearCacheBitmaps() {
		try {
			mHardBitmapCache.clear();
			mSoftBitmapCache.clear();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}