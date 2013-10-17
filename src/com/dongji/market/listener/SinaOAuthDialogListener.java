package com.dongji.market.listener;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.dongji.market.R;
import com.dongji.market.activity.Login_Activity;
import com.dongji.market.application.AppMarket;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.pojo.LoginParams;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.DialogError;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

public class SinaOAuthDialogListener implements WeiboDialogListener {

	private Context context;
	private String consumer_key;
	private String consumer_secret;
	private Handler handler;

	// sina login
	private static final String URL_ACTIVITY_CALLBACK = "weiboandroidsdk://TimeLineActivity";
	private static final String FROM = "xweibo";

	// 设置appkey及appsecret，如何获取新浪微博appkey和appsecret请另外查询相关信息，此处不作介绍
//	private static final String CONSUMER_KEY = "155814752";// 替换为开发者的appkey，例如"1646212960";
//	private static final String CONSUMER_SECRET = "3b52ce17701c19e61250ad7bf493d985";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";

	private String username = "";
	private String password = "";

	public SinaOAuthDialogListener(Context context, Handler handler, String consumer_key, String consumer_secret) {
		super();
		this.context = context;
		this.consumer_key = consumer_key;
		this.consumer_secret = consumer_secret;
		this.handler = handler;
	}

	@Override
	public void onComplete(Bundle values) {
		String token = values.getString("access_token");
		String expires_in = values.getString("expires_in");

		AccessToken accessToken = new AccessToken(token, consumer_secret);
		accessToken.setExpiresIn(expires_in);
		Weibo.getInstance().setAccessToken(accessToken);

		String uid = values.getString("uid");
		WeiboParameters mParameters = new WeiboParameters();
		mParameters.add("access_token", token);
		mParameters.add("uid", uid);
		AsyncWeiboRunner mRunner = new AsyncWeiboRunner(Weibo.getInstance());
		mRunner.request(context, Weibo.SERVER + "users/show.json", mParameters,
				Utility.HTTPMETHOD_GET, mRequestListener);
	}

	@Override
	public void onWeiboException(WeiboException e) {
		DJMarketUtils.showToast(context, "Auth exception : " + e.getMessage());
	}

	@Override
	public void onError(DialogError e) {
		DJMarketUtils.showToast(context, "Auth error : " + e.getMessage());
	}

	@Override
	public void onCancel() {
		DJMarketUtils.showToast(context, context.getResources().getString(R.string.auth_cancel));
	}

	private AsyncWeiboRunner.RequestListener mRequestListener = new RequestListener() {

		@Override
		public void onIOException(IOException e) {
			System.out.println("request exception:" + e);
		}

		@Override
		public void onError(WeiboException e) {
			System.out.println("request error:" + e);
		}

		@Override
		public void onComplete(String response) {
			saveSinaLoginState(response);
		}
	};

	private void saveSinaLoginState(String response) {
		LoginParams loginParams = ((AppMarket)context.getApplicationContext()).getLoginParams();
		try {
			JSONObject jsonObject = new JSONObject(response);
			loginParams.setSinaUserName(jsonObject.getString("screen_name"));
			handler.sendEmptyMessage(TitleUtil.SINA_SHARE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
