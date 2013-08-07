package com.dongji.market.activity;

import java.util.ArrayList;

import org.myjson.JSONException;

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
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.NavigationInfo;
import com.dongji.market.protocol.DataManager;

/**
 * 启动界面
 * 
 * @author zhangkai
 * 
 */
public class LauncherActivity extends Activity implements AnimationListener {
	private MyHandler mHandler;
	private static final int EVENT_TO_MAIN = 1;
	private static final long GOTO_TIME = 10L;
//	private boolean isSingleRow; // 是否为单列显示列表
	private static final int EVENT_REQUEST_UPDATE = 2;
	private static final int EVENT_REQUEST_DATA_ERROR = 3;
	private static final int EVENT_START_ANIM=4;
	
	private ImageView mTopImageView;
	private ImageView mBottomImageView;
	
	private ArrayList<NavigationInfo> list;
	
	private long begin;
	
	private int[] arr;
	private boolean isAnimEnd;
	private boolean isDataLoaded;
	private ImageView mAnimImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		initData();
		initHandler();
	}
	
	private void initData() {
		arr = new int[] { R.drawable.launcher_anim1, R.drawable.launcher_anim2,
				R.drawable.launcher_anim3, R.drawable.launcher_anim4,
				R.drawable.launcher_anim5 };
		mAnimImageView=(ImageView)findViewById(R.id.imageview);
		DisplayMetrics dm=AndroidUtils.getScreenSize(this);
		int num = dm.heightPixels / 2;
		num-=AndroidUtils.dip2px(this, 85);
		LinearLayout.LayoutParams mParams=(LinearLayout.LayoutParams)mAnimImageView.getLayoutParams();
		mParams.topMargin=num;
		mAnimImageView.setLayoutParams(mParams);
	}
	
	private void initViews() {
		/*mTopImageView =(ImageView)findViewById(R.id.topimageview);
		mBottomImageView=(ImageView)findViewById(R.id.bottomimageview);
		Animation mTopAnimation=AnimationUtils.loadAnimation(this, R.anim.anim_top);
		mTopAnimation.setAnimationListener(this);
		mTopImageView.startAnimation(mTopAnimation);
		mBottomImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_bottom));*/
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
			switch(msg.what) {
				case EVENT_TO_MAIN:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Intent intent=new Intent(LauncherActivity.this, MainActivity.class);
//							Bundle bundle=new Bundle();
//							bundle.putParcelableArrayList("navigationList", list);
//							intent.putExtras(bundle);
							startActivity(intent);
//							overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
							finish();
						}
					});
					break;
				case EVENT_REQUEST_UPDATE:
					begin=System.currentTimeMillis();
//					initService();
					
//					requestNavigationData();
					isDataLoaded=true;
					if(isAnimEnd) {
						sendEmptyMessage(EVENT_TO_MAIN);;
					}
					break;
				case EVENT_REQUEST_DATA_ERROR:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(LauncherActivity.this, R.string.datarequest_is_error);
							finish();
						}
					});
					break;
				case EVENT_START_ANIM:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							/*initViews();
							sendEmptyMessageDelayed(EVENT_TO_MAIN, 1000L);
							System.out.println("========="+(System.currentTimeMillis()-begin));*/
							if(currentId<arr.length) {
								mAnimImageView.setBackgroundResource(arr[currentId++]);
								sendEmptyMessageDelayed(EVENT_START_ANIM, 300L);
							}else {
								isAnimEnd=true;
								if(isDataLoaded)
									sendEmptyMessage(EVENT_TO_MAIN);
							}
						}
					});
					break;
			}
		}
	}
	
	private void requestNavigationData() {
		DataManager dataManager=DataManager.newInstance();
		try {
			list=dataManager.getNavigationList();
		/*} catch (IOException e) {
			System.out.println(e);
			if(!AndroidUtils.isNetworkAvailable(this)) {
				
			}else {
				
			}*/
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		mTopImageView.setVisibility(View.GONE);
		mBottomImageView.setVisibility(View.GONE);
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}
}
