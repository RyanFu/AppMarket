package com.dongji.market.helper;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

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

	/**
	 * 
	 * @param context上下文
	 * @return IWXAPI接口
	 */
	public static IWXAPI registWxApi(Context context) {
		if (api == null) {
			api = WXAPIFactory.createWXAPI(context, Constants.APP_ID, false);
			api.registerApp(Constants.APP_ID);
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

}
