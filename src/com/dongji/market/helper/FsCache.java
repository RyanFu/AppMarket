package com.dongji.market.helper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FsCache {
	private static String DATA_CACHE_DIRECTORY;
	private static final String updateFileName = "softUpdate";

	static {
		DATA_CACHE_DIRECTORY = DJMarketUtils.cachePath + "data/";
	}

	/**
	 * 缓存软件更新数据
	 * 
	 * @param result
	 */
	public static void cacheSofeUpdateData(String result) {
		OutputStream os = null;
		try {
			mkdirs(DATA_CACHE_DIRECTORY);
			os = new FileOutputStream(DATA_CACHE_DIRECTORY + updateFileName);
			byte[] data = result.getBytes();
			os.write(data);
			os.flush();
		} catch (IOException e) {
			System.out.println("cache soft update data!" + e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					System.out.println("cache soft update data!" + e);
				}

			}
		}
	}
	
	private static void mkdirs(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 获取缓存软件更新数据
	 * @return
	 */
	public static String getCacheSoftUpdateData() {
		File file = new File(DATA_CACHE_DIRECTORY + updateFileName);
		if (file.exists()) {
			InputStream is = null;
			ByteArrayOutputStream bos = null;
			try {
				is = new FileInputStream(file);
				bos = new ByteArrayOutputStream(2048);
				byte[] data = new byte[2048];
				int num = 0;
				while ((num = is.read(data)) != -1) {
					bos.write(data, 0, num);
				}
				String result = bos.toString();
				bos.flush();
				return result;
			} catch (IOException e) {
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * 缓存json数据
	 * 
	 * @param result
	 * @param md5Value
	 */
	public static void cacheFileByMd5(String result, String md5Value) {
		BufferedWriter bw = null;
		try {
			mkdirs(DATA_CACHE_DIRECTORY);
			File file = new File(DATA_CACHE_DIRECTORY + md5Value);
			if (file.exists())
				file.delete();
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(result);
			bw.flush();
		} catch (IOException e) {
			System.out.println("cacheFile error:" + e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("cacheFile close error:" + e);
				}
			}
		}
	}

	/**
	 * 获取缓存json数据
	 * 
	 * @param md5Value
	 * @return
	 */
	public static String getCacheString(String md5Value) {
		File file = new File(DATA_CACHE_DIRECTORY + md5Value);
		if (file.exists()) {
			InputStream is = null;
			ByteArrayOutputStream bos = null;
			try {
				is = new FileInputStream(file);
				bos = new ByteArrayOutputStream(1024);
				byte[] data = new byte[1024];
				int num = 0;
				while ((num = is.read(data)) != -1) {
					bos.write(data, 0, num);
				}
				bos.flush();
				String result = bos.toString();
				return result;
			} catch (IOException e) {
			} finally {
				try {
					if (is != null) {
						is.close();
					}
					if (bos != null) {
						bos.close();
					}
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	/**
	 * 删除缓存json数据
	 * @param md5Value
	 */
	public static void deleteCacheFileByMd5Value(String md5Value) {
		File file = new File(DATA_CACHE_DIRECTORY + md5Value);
		System.out.println("deleteCacheFileByMd5Value");
		if (file.exists()) {
			file.delete();
		}
	}
}
