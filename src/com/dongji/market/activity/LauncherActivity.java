package com.dongji.market.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.dongji.market.R;
import com.dongji.market.helper.DJMarketUtils;

/**
 * 启动界面
 * 
 * @author zhangkai
 * 
 */
public class LauncherActivity extends Activity implements AnimationListener {
	private MyHandler mHandler;
	private static final int EVENT_TO_MAIN = 1;
	private static final int EVENT_REQUEST_UPDATE = 2;
	private static final int EVENT_REQUEST_DATA_ERROR = 3;
	private static final int EVENT_START_ANIM = 4;
	private ImageView mTopImageView;
	private ImageView mBottomImageView;
	private int[] arr;
	private boolean isAnimEnd;
	private boolean isDataLoaded;
	private ImageView mAnimImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		initData();
		initHandler();
	}

	private void initData() {
		arr = new int[] { R.drawable.launcher_anim1, R.drawable.launcher_anim2, R.drawable.launcher_anim3, R.drawable.launcher_anim4, R.drawable.launcher_anim5 };
		mAnimImageView = (ImageView) findViewById(R.id.imageview);
		DisplayMetrics dm = DJMarketUtils.getScreenSize(this);
		int num = dm.heightPixels / 2;
		num -= DJMarketUtils.dip2px(this, 85);
		LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) mAnimImageView.getLayoutParams();
		mParams.topMargin = num;
		mAnimImageView.setLayoutParams(mParams);
	}

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_UPDATE);
		mHandler.sendEmptyMessage(EVENT_START_ANIM);
	}

	private class MyHandler extends Handler {
		private int currentId;

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_TO_MAIN:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case EVENT_REQUEST_UPDATE:
				isDataLoaded = true;
				if (isAnimEnd) {
					sendEmptyMessage(EVENT_TO_MAIN);
				}
				break;
			case EVENT_REQUEST_DATA_ERROR:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DJMarketUtils.showToast(LauncherActivity.this, R.string.datarequest_is_error);
						finish();
					}
				});
				break;
			case EVENT_START_ANIM:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (currentId < arr.length) {
							mAnimImageView.setBackgroundResource(arr[currentId++]);
							sendEmptyMessageDelayed(EVENT_START_ANIM, 300L);
						} else {
							isAnimEnd = true;
							if (isDataLoaded)
								sendEmptyMessage(EVENT_TO_MAIN);
						}
					}
				});
				break;
			}
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mTopImageView.setVisibility(View.GONE);
		mBottomImageView.setVisibility(View.GONE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}
}
