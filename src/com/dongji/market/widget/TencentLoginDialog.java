package com.dongji.market.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tencent.weibo.R;
import com.tencent.weibo.demo.OAuthV2ImplicitGrant;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;

public class TencentLoginDialog extends Dialog {

	static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT);
	static final int MARGIN = 4;
	static final int PADDING = 2;

	// private final Weibo mWeibo;
	// private String mUrl;
	// private WeiboDialogListener mListener;
	private ProgressDialog mSpinner;
	private WebView mWebView;
	private RelativeLayout webViewContainer;
	private RelativeLayout mContent;

	private static final String TAG = "OAuthV2AuthorizeWebView";
	private OAuthV2 oAuth;
	private Handler mHandler;

	public TencentLoginDialog(Context context, OAuthV2 oAuth, Handler handler) {
//		super(context);
		super(context);
		this.oAuth = oAuth;
		mHandler = handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mSpinner = new ProgressDialog(getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContent = new RelativeLayout(getContext());

		setUpWebView();

		addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		setCanceledOnTouchOutside(true);
	}

	private void setUpWebView() {
		webViewContainer = new RelativeLayout(getContext());

		mWebView = new WebView(getContext());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);	//让加载的网页自适应宽度
		mWebView.requestFocus();
		String urlStr = OAuthV2Client.generateImplicitGrantUrl(oAuth);
		mWebView.loadUrl(urlStr);
		mWebView.setLayoutParams(FILL);
		mWebView.setVisibility(View.INVISIBLE);
		System.out.println(urlStr.toString());
		Log.i(TAG, "WebView Starting....");
		WebViewClient client = new WebViewClient() {
			/**
			 * 回调方法，当页面开始加载时执行
			 */
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.i(TAG, "WebView onPageStarted...");
				Log.i(TAG, "URL = " + url);
				if (url.indexOf("access_token=") != -1) {
					int start = url.indexOf("access_token=");
					String responseData = url.substring(start);
					OAuthV2Client
							.parseAccessTokenAndOpenId(responseData, oAuth);
					Message msg = new Message();
					msg.what = OAuthV2ImplicitGrant.GET_OATHV2;
					msg.obj = oAuth;
					mHandler.sendMessage(msg);
					view.destroyDrawingCache();
					view.destroy();
					dismiss();
					return;
				}
				super.onPageStarted(view, url, favicon);
				mSpinner.show();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);

				mSpinner.dismiss();

				mContent.setBackgroundColor(Color.TRANSPARENT);
				webViewContainer.setBackgroundResource(R.drawable.dialog_style);
				mWebView.setVisibility(View.VISIBLE);
			}

			/*
			 * TODO Android2.2及以上版本才能使用该方法
			 * 目前https://open.t.qq.com中存在http资源会引起sslerror，待网站修正后可去掉该方法
			 */
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				if ((null != view.getUrl())
						&& (view.getUrl().startsWith("https://open.t.qq.com"))) {
					handler.proceed();// 接受证书
				} else {
					handler.cancel(); // 默认的处理方式，WebView变成空白页
				}
				// handleMessage(Message msg); 其他处理
			}
		};
		mWebView.setWebViewClient(client);

		webViewContainer.addView(mWebView);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		Resources resources = getContext().getResources();
		lp.leftMargin = resources
				.getDimensionPixelSize(R.dimen.tencent_dialog_left_margin);
		lp.topMargin = resources
				.getDimensionPixelSize(R.dimen.tencent_dialog_top_margin);
		lp.rightMargin = resources
				.getDimensionPixelSize(R.dimen.tencent_dialog_right_margin);
		lp.bottomMargin = resources
				.getDimensionPixelSize(R.dimen.tencent_dialog_bottom_margin);
		mContent.addView(webViewContainer, lp);
	}

}
