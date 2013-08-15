package com.dongji.market.cache;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 文件服务接口
 * 
 * @author yvon
 * 
 */
public interface FileServiceInterface {

	public static final int SCROLL_STATUS_STOP = 0;//无滑动
	public static final boolean isRemoteImage = false;//是否运行下载图片

	// 文件服务模式 ，无同步任务 ，无下载图片 ，普通
	public enum Mode {
		NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT
	}

	public static Mode mode = Mode.CORRECT;

	/**
	 * 初始化cache文件夹，并读出今天的cache内文件放入HashMap<key: URL hash code, value: file full
	 * path> 该方法通常在应用的入口处调用，并且应用从打开到关闭，仅需执行一次（执行多次也是可以的，但是没有必要）。
	 */
	public void loadFileToMap();

	/**
	 * 通过URL获取图片，首先检查本地信息(SD card，内存，data目录)，如果没有则从网络获取
	 * 
	 * @param url
	 *            图片的URL
	 * @param imageView
	 *            需要set图片的ImageView
	 * @param defaultBitmap
	 *            如果不能通过网络或者本地cache获取图片，则使用这张Bitmap作为ImageView的背景
	 * @param scrollState
	 *            判断ListView的滚动状态，默认是不滚动
	 */
	public void getBitmap(String url, ImageView imageView, Bitmap defaultBitmap, Integer scrollState);

	/**
	 * 通过URL获取图片，图片可能保存在 memory或者sd card上，
	 * 注意:调用本方法之前，一定要先调用FileService.loadFileToMap();
	 * 
	 * @param url
	 *            图片的URL[类型:String]
	 * @param imageView
	 *            需要set图片的ImageView[类型:ImageView]
	 * @param defaultBitmap
	 *            如果不能通过网络或者本地cache获取图片，则使用这张Bitmap作为ImageView的背景[类型:Bitmap]
	 * @param isRemote
	 *            判断是否允许获取网络图片
	 * 
	 */
	public void getBitmap(String url, ImageView imageView, Bitmap defaultBitmap, boolean isRemote);

	/**
	 * 获取缓存在cache里面的图片总数
	 * 
	 * @return
	 */
	public int getCurrentCacheBitmapNumbers();

	/**
	 * 清除掉目前缓存中的所有图片，返回true表示清除成功，false表示清除失败
	 */
	public boolean clearCacheBitmaps();

	/**
	 * 保存bitmap
	 * 
	 * @param url
	 * @param bitmap
	 */
	public void saveBitmap(String url, Bitmap bitmap);

	/**
	 * 
	 * @param path
	 * @param targetPath
	 * @return
	 */
	public boolean getFile(String path, String targetPath);

	/**
	 * 
	 * @param path
	 * @return
	 */
	public boolean clearFile(String path);

}