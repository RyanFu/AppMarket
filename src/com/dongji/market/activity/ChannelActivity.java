package com.dongji.market.activity;

import java.util.ArrayList;
import java.util.List;

import org.myjson.JSONException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.ChannelAdapter;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.ScrollListView;

/**
 * 
 * @author zhangkai
 */
public class ChannelActivity extends BaseActivity implements ScrollListView.OnScrollTouchListener {
	private MyHandler mHandler;
	private final static int EVENT_REQUEST_DATA = 1;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private Context context;
	private boolean isAppClicked=true;
	private boolean isDataLoaded;
	private boolean isLoading;
	
	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	
	private ScrollListView mAppListView;
	private ScrollListView mGameListView;
	
	private ChannelAdapter mAppListAdapter;
	private ChannelAdapter mGameListAdapter;
	
	private static final String APP_STRING = "应用";
	private static final String GAME_STRING = "游戏";
	private static final String APP_STRING2 = "應用";
	private static final String GAME_STRING2 = "遊戲";
	
	private boolean isFirstInApp=true, isFirstInGame=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_template);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		context=this;
		initLoadingView();
		initHandler();
	}
	
	private void initHandler() {
		HandlerThread mHandlerThread=new HandlerThread("HandlerThread");
		mHandlerThread.start();
		mHandler=new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
	}
	
	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}
	
	private void initLoadingView() {
		mLoadingView=findViewById(R.id.loadinglayout);
		mLoadingProgressBar=findViewById(R.id.loading_progressbar);
		mLoadingTextView=(TextView)findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mLoadingProgressBar.getVisibility()==View.GONE) {
					setPreLoading();
					mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
				}
				return false;
			}
		});
	}
	
	/**
	 * 因为请求到的分类数据是应用和游戏分在一起的，在这里需要将其分开
	 * @param channelList
	 */
	private void initViews(List<ChannelListInfo> channelList) {
		System.out.println("....0719..."+channelList.get(2).name);
		String allString = getString(R.string.all_txt);
		List<ChannelListInfo> appListInfo = new ArrayList<ChannelListInfo>();
		List<ChannelListInfo> gameListInfo = new ArrayList<ChannelListInfo>();
		int appId=0;
		int gameId=0;
		if(AndroidUtils.getLanguageType()==1){
			for(int i=0;i<channelList.size();i++) {
				ChannelListInfo info = channelList.get(i);
				if (APP_STRING2.equals(info.name)) {
					appId=info.id;
					info.name = allString+info.name;
					appListInfo.add(info);
				}else if(GAME_STRING2.equals(info.name)) {
					gameId=info.id;
					info.name = allString+info.name;
					gameListInfo.add(info);
				}
			}
		}else{
			for(int i=0;i<channelList.size();i++) {
				ChannelListInfo info = channelList.get(i);
				if (APP_STRING.equals(info.name)) {
					appId=info.id;
					info.name = allString+info.name;
					appListInfo.add(info);
				}else if(GAME_STRING.equals(info.name)) {
					gameId=info.id;
					info.name = allString+info.name;
					gameListInfo.add(info);
				}
			}
		}
		
		for (int i = 0; i < channelList.size(); i++) {
			ChannelListInfo info = channelList.get(i);
			if (info.parentId == appId) {
				appListInfo.add(info);
			} else if (info.parentId == gameId) {
				gameListInfo.add(info);
			}
		}
		mAppListView=(ScrollListView)findViewById(R.id.applistview);
		mGameListView=(ScrollListView)findViewById(R.id.gamelistview);
		mAppListAdapter=new ChannelAdapter(context, appListInfo, isRemoteImage);
		mAppListView.setAdapter(mAppListAdapter);
		mGameListAdapter=new ChannelAdapter(context, gameListInfo, isRemoteImage);
		mGameListView.setAdapter(mGameListAdapter);
		mAppListView.setOnScrollTouchListener(this);
		mGameListView.setOnScrollTouchListener(this);
		mLoadingView.setVisibility(View.GONE);
		LayoutAnimationController mLayoutAnimationController=getLayoutAnimationController();
		if(isAppClicked) {
			isFirstInApp=false;
			mAppListView.setVisibility(View.VISIBLE);
			mGameListView.setVisibility(View.GONE);
			mAppListView.setLayoutAnimation(mLayoutAnimationController);
//			getParentActivity().setListViewSlide(mAppListView);
		}else {
			isFirstInGame=false;
			mGameListView.setVisibility(View.VISIBLE);
			mAppListView.setVisibility(View.GONE);
			mGameListView.setLayoutAnimation(mLayoutAnimationController);
//			getParentActivity().setListViewSlide(mGameListView);
		}
	}
	
	/**
	 * 显示 ListView 下拉动画
	 * @return
	 */
	private LayoutAnimationController getLayoutAnimationController() {
		AnimationSet localAnimationSet = new AnimationSet(true);
		AlphaAnimation localAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
		localAlphaAnimation.setDuration(50L);
		localAnimationSet.addAnimation(localAlphaAnimation);
		TranslateAnimation localTranslateAnimation = new TranslateAnimation(1,
				0.0F, 1, 0.0F, 1, -1.0F, 1, 0.0F);
		localTranslateAnimation.setDuration(100L);
		localAnimationSet.addAnimation(localTranslateAnimation);
		return new LayoutAnimationController(localAnimationSet, 0.5F);
	}
	
	@Override
	public boolean isAppClicked() {
		// TODO Auto-generated method stub
		return isAppClicked;
	}

	@Override
	public void onAppClick() {
		// TODO Auto-generated method stub
		if(!isAppClicked) {
			isAppClicked=true;
			getParentActivity().progressBarGone();
			if(isDataLoaded) {
				setDisplayVisible();
			}else {
				if(!isLoading && !mHandler.hasMessages(EVENT_REQUEST_DATA)) {
					mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
				}
			}
		}
	}
	
	private void setDisplayVisible() {
		mLoadingView.setVisibility(View.GONE);
		if(isAppClicked) {
			if(mAppListView!=null) {
				mAppListView.setVisibility(View.VISIBLE);
				if(isFirstInApp) {
					isFirstInApp=false;
					mAppListView.setLayoutAnimation(getLayoutAnimationController());
				}
			}
			if(mGameListView!=null) {
				mGameListView.setVisibility(View.GONE);
			}
		}else {
			if(mGameListView!=null) {
				mGameListView.setVisibility(View.VISIBLE);
				if(isFirstInGame) {
					isFirstInGame=false;
					mGameListView.setLayoutAnimation(getLayoutAnimationController());
				}
			}
			if(mAppListView!=null) {
				mAppListView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onGameClick() {
		// TODO Auto-generated method stub
		isAppClicked=false;
		getParentActivity().progressBarGone();
		if(isDataLoaded) {
			setDisplayVisible();
		}else {
			if(!isLoading && !mHandler.hasMessages(EVENT_REQUEST_DATA)) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
			}
		}
	}

	@Override
	public void onItemClick(ChannelListInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		// TODO Auto-generated method stub
		
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
				case EVENT_REQUEST_DATA:
					try{
						isLoading=true;
						final List<ChannelListInfo> channelList=DataManager.newInstance().getChannelListData(context);
						if(channelList!=null && channelList.size()>0) {
							isDataLoaded=true;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									initViews(channelList);
									isLoading=false;
								}
							});
						}
					}catch(JSONException e) {
						isLoading=false;
						if(!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
					break;
				case EVENT_NO_NETWORK_ERROR:
					setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
					break;
				case EVENT_REQUEST_DATA_ERROR:
					setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
					break;
			}
		}
	}
	
	/**
	 * 数据获取异常处理
	 * @param rId
	 */
	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getParentActivity().stopProgressBar();
				if(mLoadingView.getVisibility()==View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				}else {
					AndroidUtils.showToast(context, rId2);
				}
			}
		});
	}

	@Override
	protected void onUpdateDataDone() {
		
	}

	@Override
	public void onScrollTouch(int scrollState) {
		// TODO Auto-generated method stub
		switch (scrollState) {
		case ScrollListView.OnScrollTouchListener.SCROLL_BOTTOM:
//			getParentActivity().scrollOperation(true);
			break;
		case ScrollListView.OnScrollTouchListener.SCROLL_TOP:
//			getParentActivity().scrollOperation(false);
			break;
		}
	}

	@Override
	protected void loadingImage() {
		if(mAppListAdapter!=null) {
			mAppListAdapter.setDisplayNotify(isRemoteImage);
		}
		if(mGameListAdapter!=null) {
			mGameListAdapter.setDisplayNotify(isRemoteImage);
		}
	}
	
	private int locStep;
	@Override
	public void OnToolBarClick() {
		if(isAppClicked()) {
			if(mAppListView!=null) {
//				if (!mAppListView.isStackFromBottom()) {
//					mAppListView.setStackFromBottom(true);
//				}
//				mAppListView.setStackFromBottom(false);
				locStep = (int) Math.ceil(mAppListView.getFirstVisiblePosition()/AConstDefine.AUTO_SCRLL_TIMES);
				mAppListView.post(appAutoScroll);
			}
		}else {
			if(mGameListView!=null) {
//				if (!mGameListView.isStackFromBottom()) {
//					mGameListView.setStackFromBottom(true);
//				}
//				mGameListView.setStackFromBottom(false);
				locStep = (int) Math.ceil(mGameListView.getFirstVisiblePosition()/AConstDefine.AUTO_SCRLL_TIMES);
				mGameListView.post(gameAutoScroll);
			}
		}
	}
	
	Runnable appAutoScroll = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mAppListView.getFirstVisiblePosition() > 0) {
				if (mAppListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mAppListView.setSelection(mAppListView
							.getFirstVisiblePosition() - 1);
				} else {
					mAppListView.setSelection(Math.max(mAppListView.getFirstVisiblePosition() - locStep, 0));
				}
				// mAppListView.postDelayed(this, 1);
				mAppListView.post(this);
			}
			return;
		}
	};
	
	Runnable gameAutoScroll = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mGameListView.getFirstVisiblePosition() > 0) {
				if (mGameListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mGameListView.setSelection(mGameListView
							.getFirstVisiblePosition() - 1);
				} else {
					mGameListView.setSelection(Math.max(mGameListView.getFirstVisiblePosition() - locStep, 0));
				}
				// mAppListView.postDelayed(this, 1);
				mGameListView.post(this);
			}
			return;
		}
	};
}
