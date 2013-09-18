package com.dongji.market.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.dongji.market.helper.AndroidUtils;

/**
 * SD卡文件服务
 * 
 * @author yvon
 * 
 */
public class FileServiceBySDCard extends FileServiceAbstractDetails {

	private final static String TAG = "FileServiceBySDCard";
	private final long NEED_DELETED_TIME_INTERVAL = 48 * 3600000;// 2天（该变量用作判断2天前的图片会被删除）
	final static String EXTERNAL_STORAGE_DIRECTORY_PATH = AndroidUtils.getSdcardFile().getPath() + "/.dongji/dongjiMarket/cache/images/";
	static HashMap<String, String> urlMap;// key:URL hash code value:file
											// path，图片路径缓存

	@Override
	public void loadFileToMap() {
		if (urlMap == null) {
			urlMap = new HashMap<String, String>();
		} else {
			urlMap.clear();
		}
		File file = new File(EXTERNAL_STORAGE_DIRECTORY_PATH);
		if (!file.exists()) {
			file.mkdirs();
		} else if (file.isDirectory()) {
			getFileMap(file);
		}
	}

	private Bitmap getBitmapByCahche(String url, ImageView imageView, Bitmap defaultBitmap, Integer scrollState) {
		Bitmap bm = null;
		String filePath = null;
		try {
			String urlHashCode = getHashCode(url);
			if (urlMap == null) {
				urlMap = new HashMap<String, String>();
			}
			synchronized (urlMap) {
				filePath = urlMap.get(urlHashCode);
			}
			if (filePath != null) {// 本地data cache已经有图片
				File imageFile = new File(filePath);
				if (imageFile.exists()) {
					bm = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, mOptions);
					if (scrollState != SCROLL_STATUS_STOP) {// 处于滚动状态
						if (bm == null) {
							imageView.setImageBitmap(defaultBitmap);
							synchronized (urlMap) {
								urlMap.remove(urlHashCode);
							}
						} else {
							imageView.setImageBitmap(bm);
						}
						// return;
					}
				} else {
					// imageView.setImageBitmap(defaultBitmap);
					synchronized (urlMap) {
						urlMap.remove(urlHashCode);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("============" + filePath + ", " + e);
			// QQLiveLog.d(TAG,
			// "FileNotFoundException when get Bitmap from sd card.");
		} catch (OutOfMemoryError e) {
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
			}
			// QQLiveLog.d(TAG, "OutOfMemoryError:"+e.getMessage());
		}
		return bm;
	}

	@Override
	public void getBitmap(String url, ImageView imageView, Bitmap defaultBitmap, Integer scrollState) {

		Bitmap bm = getBitmapByCahche(url, imageView, defaultBitmap, scrollState);

		// 当滚动条处于非滚动状态时
		if (bm == null) {
			// SD Card还没有该URL所对应的图片， 接下来就从网络取
			forceDownload(url, imageView, defaultBitmap);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bm);
		}
	}

	@Override
	public void getBitmap(String url, ImageView imageView, Bitmap defaultBitmap, boolean isRemote) {
		Bitmap bm = getBitmapByCahche(url, imageView, defaultBitmap, 0);

		// 当滚动条处于非滚动状态时
		if (bm == null) {
			// 判断是否需要请求网络图片
			if (!isRemote) {
				imageView.setImageBitmap(defaultBitmap);
			} else {
				// SD Card还没有该URL所对应的图片， 接下来就从网络取
				forceDownload(url, imageView, defaultBitmap);
			}
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bm);
		}
	}

	// 递归获取每个文件名，并且把文件名放入Map里面
	private void getFileMap(File f) {
		if (f.listFiles() != null) {
			List<File> filesList = Arrays.asList(f.listFiles());
			long currentTime = System.currentTimeMillis();
			for (File singleFile : filesList) {
				if (singleFile.isDirectory()) {
					getFileMap(singleFile);
				} else {
					if ((currentTime - singleFile.lastModified()) > NEED_DELETED_TIME_INTERVAL) {
						singleFile.delete();// 删除2天之前的文件
						continue;
					}
					String fileName = singleFile.getName();
					int n = fileName.indexOf("_");
					if (n == -1 || (n + 1) == fileName.length())
						continue;
					String mapKey = fileName.substring(0, fileName.indexOf("_"));
					synchronized (urlMap) {
						if (!urlMap.containsKey(mapKey)) {
							urlMap.put(mapKey, f.getAbsolutePath() + "/" + fileName);
						}
					}
				}
			}
		}
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
		OutputStream outStream = null;
		try {
			String urlHashCode = String.valueOf(getHashCode(url));
			String postfixName = url.substring(url.lastIndexOf(".") + 1, url.length());// 文件后缀名
			File directory = new File(EXTERNAL_STORAGE_DIRECTORY_PATH);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			File file = new File(EXTERNAL_STORAGE_DIRECTORY_PATH + urlHashCode + "_" + postfixName);
			file.createNewFile();
			outStream = new FileOutputStream(file);
			if ("jpg".equalsIgnoreCase(postfixName)) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			} else if ("png".equalsIgnoreCase(postfixName)) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			}
			outStream.flush();

			synchronized (urlMap) {
				urlMap.put(urlHashCode, EXTERNAL_STORAGE_DIRECTORY_PATH + urlHashCode + "_" + postfixName);
			}
		} catch (FileNotFoundException e) {
			// QQLiveLog.d(TAG, "FileNotFoundException:" + e.getMessage());

			System.out.println("====save bitmap file notfound:" + e);
		} catch (IOException e) {
			System.out.println("====save bitmap io:" + e);
			// QQLiveLog.d(TAG, "IOException:" + e.getMessage());
		} catch (NullPointerException e) {
			// QQLiveLog.d(TAG, "NullPointerException:" + e.getMessage());
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public int getCurrentCacheBitmapNumbers() {
		if (urlMap != null) {
			return urlMap.size();
		}
		return 0;
	}

	@Override
	public boolean clearCacheBitmaps() {
		try {
			File file = new File(EXTERNAL_STORAGE_DIRECTORY_PATH);
			if (file.listFiles() != null) {
				List<File> filesList = Arrays.asList(file.listFiles());
				for (File singleFile : filesList) {
					if (!singleFile.isDirectory()) {
						singleFile.delete();
					}
				}
			}
			urlMap.clear();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}