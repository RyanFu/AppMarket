package com.dongji.market.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

/**
 * 微信工具类
 * 
 */
public class WxUtils {
	private static IWXAPI api;
	private static final String TAG = "WxUtil";
	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;
	private static final String EXTERNAL_STORAGE_DIRECTORY_PATH = AndroidUtils.getSdcardFile().getPath() + "/.dongji/dongjiMarket/cache/images/";
	private static final BitmapFactory.Options mOptions = new BitmapFactory.Options();

	static {
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;// 16位位图
		mOptions.inPurgeable = true;// BitmapFactory创建的Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
		mOptions.inInputShareable = true;// 位图能够共享一个指向数据源的引用，或者是进行一份拷贝
	}

	/**
	 * 
	 * @param context上下文
	 * @return IWXAPI接口
	 */
	public static IWXAPI registWxApi(Context context) {
		if (api == null) {
			api = WXAPIFactory.createWXAPI(context, WXConstants.APP_ID, false);
			api.registerApp(WXConstants.APP_ID);
		}
		return api;
	}

	/**
	 * 发布只带文本的微信
	 * 
	 * @param text
	 * @param scene
	 *            SendMessageToWX.Req.WXSceneTimeline 发送到朋友圈
	 *            SendMessageToWX.Req.WXSceneSession 发送到会话
	 * 
	 */
	public static void sendWx(String text, int scene) {
		WXTextObject wXTextObject = new WXTextObject(text);
		WXMediaMessage wXMediaMessage = new WXMediaMessage();
		wXMediaMessage.mediaObject = wXTextObject;
		wXMediaMessage.description = text;
		SendMessageToWX.Req request = new SendMessageToWX.Req();
		request.transaction = System.currentTimeMillis() + "";
		request.message = wXMediaMessage;
		api.sendReq(request);
	}

	/**
	 * 微信发布图片
	 * 
	 * @param imageData
	 *            图片的数组资源
	 * @param imagePath
	 *            图片的路径
	 * @param imageUrl
	 *            图片的URL
	 * @param width
	 *            显示图片的宽度
	 * @param heigth
	 *            显示图片的高度
	 * @param scene
	 *            SendMessageToWX.Req.WXSceneTimeline 发送到朋友圈
	 *            SendMessageToWX.Req.WXSceneSession 发送到会话
	 * 
	 *            注意:imageData imagePath imageUrl指定一个就行，不能同时为空
	 */
	public static void sendImageWx(byte[] imageData, String imagePath, String imageUrl, int width, int heigth, int scene) {
		WXImageObject imgObj = new WXImageObject();
		if (imageData != null) {
			imgObj.imageData = imageData;
		}
		if (imagePath != null) {
			imgObj.imagePath = imagePath;
		}
		if (imageUrl != null) {
			imgObj.imageUrl = imageUrl;
		}
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		try {
			Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
			byte[] bytes = getBitmapBytes(bmp, false, width, heigth);
			msg.thumbData = bytes;
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = System.currentTimeMillis() + "";
			req.message = msg;
			req.scene = scene;
			api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 微信发布图片
	 * 
	 * @param imageData
	 *            图片的数组资源
	 * @param imagePath
	 *            图片的路径
	 * @param imageUrl
	 *            图片的URL
	 * @param scene
	 *            SendMessageToWX.Req.WXSceneTimeline 发送到朋友圈
	 *            SendMessageToWX.Req.WXSceneSession 发送到会话
	 * 
	 *            注意:imageData imagePath imageUrl指定一个就行，不能同时为空
	 */
	public static void sendImageWx(byte[] imageData, String imagePath, String imageUrl, int scene) {
		WXImageObject imgObj = new WXImageObject();
		if (imageData != null) {
			imgObj.imageData = imageData;
		}
		if (imagePath != null) {
			imgObj.imagePath = imagePath;
		}
		if (imageUrl != null) {
			imgObj.imageUrl = imageUrl;
		}
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		try {
			Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
			byte[] bytes = bmpToByteArray(bmp, true);
			msg.thumbData = bytes;
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = System.currentTimeMillis() + "";
			req.message = msg;
			req.scene = scene;
			api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发布链接
	 * 
	 * @param url
	 *            链接的地址
	 * @param title
	 *            链接的标题
	 * @param text
	 *            链接的内容
	 * @param bitmap
	 *            链接的图片资源
	 * @param width
	 *            图片的宽度
	 * @param heigth
	 *            图片的高度
	 * @param scene
	 *            SendMessageToWX.Req.WXSceneTimeline 发送到朋友圈
	 *            SendMessageToWX.Req.WXSceneSession 发送到会话
	 */
	public static void sendWebPageWx(String url, String title, String text, Bitmap bitmap, int width, int heigth, int scene) {
		WXWebpageObject localWXWebpageObject = new WXWebpageObject();
		localWXWebpageObject.webpageUrl = url;
		WXMediaMessage localWXMediaMessage = new WXMediaMessage(localWXWebpageObject);
		localWXMediaMessage.title = title;
		localWXMediaMessage.description = text;
		if (bitmap != null) {
			localWXMediaMessage.thumbData = getBitmapBytes(bitmap, false, width, heigth);
		}
		SendMessageToWX.Req localReq = new SendMessageToWX.Req();
		localReq.transaction = System.currentTimeMillis() + "";
		localReq.message = localWXMediaMessage;
		localReq.scene = scene;
		api.sendReq(localReq);

	}

	/**
	 * 发布链接
	 * 
	 * @param url
	 *            链接的地址
	 * @param title
	 *            链接的标题
	 * @param text
	 *            链接的内容
	 * @param bitmap
	 *            链接的图片资源
	 * @param scene
	 *            SendMessageToWX.Req.WXSceneTimeline 发送到朋友圈
	 *            SendMessageToWX.Req.WXSceneSession 发送到会话
	 */
	public static void sendWebPageWx(String url, String title, String text, Bitmap bitmap, int scene) {
		WXWebpageObject localWXWebpageObject = new WXWebpageObject();
		localWXWebpageObject.webpageUrl = url;
		WXMediaMessage localWXMediaMessage = new WXMediaMessage(localWXWebpageObject);
		localWXMediaMessage.title = title;
		localWXMediaMessage.description = text;
		if (bitmap != null) {
			localWXMediaMessage.thumbData = bmpToByteArray(bitmap, true);
		}
		SendMessageToWX.Req localReq = new SendMessageToWX.Req();
		localReq.transaction = System.currentTimeMillis() + "";
		localReq.message = localWXMediaMessage;
		localReq.scene = scene;
		api.sendReq(localReq);

	}

	/**
	 * 发布链接
	 * 
	 * @param url
	 *            链接的地址
	 * @param title
	 *            链接的标题
	 * @param text
	 *            链接的内容
	 * @param bitmap
	 *            链接的图片资源
	 * @param scene
	 *            SendMessageToWX.Req.WXSceneTimeline 发送到朋友圈
	 *            SendMessageToWX.Req.WXSceneSession 发送到会话
	 */
	public static void sendWebPageWx(String url, String title, String text, byte[] bytes, int scene) {
		WXWebpageObject localWXWebpageObject = new WXWebpageObject();
		localWXWebpageObject.webpageUrl = url;
		WXMediaMessage localWXMediaMessage = new WXMediaMessage(localWXWebpageObject);
		localWXMediaMessage.title = title;
		localWXMediaMessage.description = text;
		if (bytes != null) {
			localWXMediaMessage.thumbData = bytes;
		}
		SendMessageToWX.Req localReq = new SendMessageToWX.Req();
		localReq.transaction = System.currentTimeMillis() + "";
		localReq.message = localWXMediaMessage;
		localReq.scene = scene;
		api.sendReq(localReq);

	}

	/**
	 * 图片处理类
	 * 
	 * @param bitmap
	 * @param paramBoolean
	 * @param width
	 * @param heigth
	 * @return
	 */
	private static byte[] getBitmapBytes(Bitmap bitmap, boolean paramBoolean, int width, int heigth) {
		Bitmap localBitmap = Bitmap.createBitmap(width, heigth, Bitmap.Config.RGB_565);
		Canvas localCanvas = new Canvas(localBitmap);
		int i;
		int j;
		if (bitmap.getHeight() > bitmap.getWidth()) {
			i = bitmap.getWidth();
			j = bitmap.getWidth();
		} else {
			i = bitmap.getHeight();
			j = bitmap.getHeight();
		}
		while (true) {
			localCanvas.drawBitmap(bitmap, new Rect(0, 0, i, j), new Rect(0, 0, width, heigth), null);
			if (paramBoolean)
				bitmap.recycle();
			ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
			localBitmap.compress(Bitmap.CompressFormat.JPEG, 100, localByteArrayOutputStream);
			localBitmap.recycle();
			byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
			try {
				localByteArrayOutputStream.close();
				return arrayOfByte;
			} catch (Exception e) {
				e.printStackTrace();
			}
			i = bitmap.getHeight();
			j = bitmap.getHeight();
		}
	}

	/**
	 * bitmap 转字节数组
	 * 
	 * @param bmp
	 * @param needRecycle
	 *            是否需要回收资源
	 * @return
	 */
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 输入流转字节数组
	 * 
	 * @param is
	 * @return
	 */
	public static byte[] inputStreamToByte(InputStream is) {
		try {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 获取缩略图
	 * 
	 * @param path
	 * @param height
	 * @param width
	 * @param crop
	 * @return
	 */
	public static Bitmap extractThumbNail(final String path, final int height, final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0 && width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height + ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = " + beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight + ", orig=" + options.outWidth + "x" + options.outHeight + ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG, "bitmap decoded size=" + bm.getWidth() + "x" + bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG, "bitmap croped size=" + bm.getWidth() + "x" + bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}

		return null;
	}

	/**
	 * 获取图片缓存
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getBitmapFromFile(String url) {
		String filePath = null;
		Bitmap bm = null;
		try {
			filePath = EXTERNAL_STORAGE_DIRECTORY_PATH + getHashCode(url) + "_" + url.substring(url.lastIndexOf(".") + 1, url.length());
			File imageFile = new File(filePath);
			if (imageFile.exists()) {
				bm = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, mOptions);
			}
		} catch (FileNotFoundException e) {
			System.out.println("============" + filePath + ", " + e);
		} catch (OutOfMemoryError e) {
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
			}
		}
		return bm;
	}

	/**
	 * 获取url的hashcode
	 * 
	 * @param url
	 * @return
	 */
	private static String getHashCode(String url) {
		if (!TextUtils.isEmpty(url)) {
			return String.valueOf(url.hashCode());
		}
		return null;
	}

}
