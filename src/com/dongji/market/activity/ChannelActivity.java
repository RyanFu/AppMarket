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
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.ScrollListView;

/**
 * 分类页
 * 
 * @author zhangkai
 */
public class ChannelActivity extends BaseActivity {
	private MyHandler mHandler;
	private final static int EVENT_REQUEST_DATA = 1;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private Context context;
	private boolean isAppClicked = true;
	private boolean isDataLoaded;
	private boolean isLoading;

	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;

	private ScrollListView mAppListView;
	private ScrollListView mGameListView;

	private ChannelAdapter mAppListAdapter;
	private ChannelAdapter mGameListAdapter;
	private int locStep;

	private static final String APP_STRING = "应用";
	private static final String GAME_STRING = "游戏";
	private static final String APP_STRING2 = "應用";
	private static final String GAME_STRING2 = "遊戲";

	private boolean isFirstInApp = true, isFirstInGame = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_template);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		context = this;
		initLoadingView();
		initHandler();
	}

	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mLoadingProgressBar = findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
				}
				return false;
			}
		});
	}

	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_DATA:// 请求分类数据
				try {
					isLoading = true;
					final List<ChannelListInfo> channelList = DataManager.newInstance().getChannelListData(context);
					if (channelList != null && channelList.size() > 0) {
						isDataLoaded = true;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								initViews(channelList);
								isLoading = false;
							}
						});
					}
				} catch (JSONException e) {
					isLoading = false;
					if (!DJMarketUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
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
	 * 因为请求到的分类数据是应用和游戏分在一起的，在这里需要将其分开
	 * 
	 * @param channelList
	 */
	private void initViews(List<ChannelListInfo> channelList) {
		String allString = getString(R.string.all_txt);
		List<ChannelListInfo> appListInfo = new ArrayList<ChannelListInfo>();
		List<ChannelListInfo> gameListInfo = new ArrayList<ChannelListInfo>();
		int appId = 0;
		int gameId = 0;
		if (DJMarketUtils.getLanguageType() == 1) {
			for (int i = 0; i < channelList.size(); i++) {
				ChannelListInfo info = channelList.get(i);
				if (APP_STRING2.equals(info.name)) {
					appId = info.id;
					info.name = allString + info.name;
					appListInfo.add(info);
				} else if (GAME_STRING2.equals(info.name)) {
					gameId = info.id;
					info.name = allString + info.name;
					gameListInfo.add(info);
				}
			}
		} else {
			for (int i = 0; i < channelList.size(); i++) {
				ChannelListInfo info = channelList.get(i);
				if (APP_STRING.equals(info.name)) {
					appId = info.id;
					info.name = allString + info.name;
					appListInfo.add(info);
				} else if (GAME_STRING.equals(info.name)) {
					gameId = info.id;
					info.name = allString + info.name;
					gameListInfo.add(info);
				}
			}
		}
		// 将分类信息分别添加到应用和游戏列表
		for (int i = 0; i < channelList.size(); i++) {
			ChannelListInfo info = channelList.get(i);
			if (info.parentId == appId) {
				appListInfo.add(info);
			} else if (info.parentId == gameId) {
				gameListInfo.add(info);
			}
		}
		mAppListView = (ScrollListView) findViewById(R.id.applistview);
		mGameListView = (ScrollListView) findViewById(R.id.gamelistview);
		mAppListAdapter = new ChannelAdapter(context, appListInfo, isRemoteImage);
		mAppListView.setAdapter(mAppListAdapter);
		mGameListAdapter = new ChannelAdapter(context, gameListInfo, isRemoteImage);
		mGameListView.setAdapter(mGameListAdapter);
		mLoadingView.setVisibility(View.GONE);
		LayoutAnimationController mLayoutAnimationController = getLayoutAnimationController();
		if (isAppClicked) {
			isFirstInApp = false;
			mAppListView.setVisibility(View.VISIBLE);
			mGameListView.setVisibility(View.GONE);
			mAppListView.setLayoutAnimation(mLayoutAnimationController);
		} else {
			isFirstInGame = false;
			mGameListView.setVisibility(View.VISIBLE);
			mAppListView.setVisibility(View.GONE);
			mGameListView.setLayoutAnimation(mLayoutAnimationController);
		}
	}

	/**
	 * 显示 ListView 下拉动画
	 * 
	 * @return
	 */
	private LayoutAnimationController getLayoutAnimationController() {
		AnimationSet localAnimationSet = new AnimationSet(true);
		AlphaAnimation localAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
		localAlphaAnimation.setDuration(50L);
		localAnimationSet.addAnimation(localAlphaAnimation);
		TranslateAnimation localTranslateAnimation = new TranslateAnimation(1, 0.0F, 1, 0.0F, 1, -1.0F, 1, 0.0F);
		localTranslateAnimation.setDuration(100L);
		localAnimationSet.addAnimation(localTranslateAnimation);
		return new LayoutAnimationController(localAnimationSet, 0.5F);
	}

	/**
	 * 记录应用按钮状态
	 */
	@Override
	public boolean isAppClicked() {
		return isAppClicked;
	}

	@Override
	public void onAppClick() {
		if (!isAppClicked) {
			isAppClicked = true;
			if (isDataLoaded) {
				setDisplayVisible();
			} else {
				if (!isLoading && !mHandler.hasMessages(EVENT_REQUEST_DATA)) {
					mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
				}
			}
		}
	}

	@Override
	public void onGameClick() {
		isAppClicked = false;
		if (isDataLoaded) {
			setDisplayVisible();
		} else {
			if (!isLoading && !mHandler.hasMessages(EVENT_REQUEST_DATA)) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
			}
		}
	}
	
	private void setDisplayVisible() {
		mLoadingView.setVisibility(View.GONE);
		if (isAppClicked) {
			if (mAppListView != null) {
				mAppListView.setVisibility(View.VISIBLE);
				if (isFirstInApp) {
					isFirstInApp = false;
					mAppListView.setLayoutAnimation(getLayoutAnimationController());
				}
			}
			if (mGameListView != null) {
				mGameListView.setVisibility(View.GONE);
			}
		} else {
			if (mGameListView != null) {
				mGameListView.setVisibility(View.VISIBLE);
				if (isFirstInGame) {
					isFirstInGame = false;
					mGameListView.setLayoutAnimation(getLayoutAnimationController());
				}
			}
			if (mAppListView != null) {
				mAppListView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 有应用安装或卸载时会回调此方法，但此处不需要做响应
	 */
	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
	}

	/**
	 * 应用状态改变时会回调此方法
	 */
	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
	}

	/**
	 * 数据获取异常处理
	 * 
	 * @param rId
	 */
	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					DJMarketUtils.showToast(context, rId2);
				}
			}
		});
	}

	/**
	 * 数据更新完毕回调
	 */
	@Override
	protected void onUpdateDataDone() {
	}

	/**
	 * 根据状态的改变，重新刷新图片
	 */
	@Override
	protected void loadingImage() {
		if (mAppListAdapter != null) {
			mAppListAdapter.setDisplayNotify(isRemoteImage);
		}
		if (mGameListAdapter != null) {
			mGameListAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	/**
	 * 点击顶部头回调
	 */
	@Override
	public void OnToolBarClick() {
		if (isAppClicked()) {
			if (mAppListView != null) {
				locStep = (int) Math.ceil(mAppListView.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
				mAppListView.post(appAutoScroll);
			}
		} else {
			if (mGameListView != null) {
				locStep = (int) Math.ceil(mGameListView.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
				mGameListView.post(gameAutoScroll);
			}
		}
	}

	Runnable appAutoScroll = new Runnable() {

		@Override
		public void run() {
			if (mAppListView.getFirstVisiblePosition() > 0) {
				if (mAppListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mAppListView.setSelection(mAppListView.getFirstVisiblePosition() - 1);
				} else {
					mAppListView.setSelection(Math.max(mAppListView.getFirstVisiblePosition() - locStep, 0));
				}
				mAppListView.post(this);
			}
			return;
		}
	};

	Runnable gameAutoScroll = new Runnable() {

		@Override
		public void run() {
			if (mGameListView.getFirstVisiblePosition() > 0) {
				if (mGameListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mGameListView.setSelection(mGameListView.getFirstVisiblePosition() - 1);
				} else {
					mGameListView.setSelection(Math.max(mGameListView.getFirstVisiblePosition() - locStep, 0));
				}
				mGameListView.post(this);
			}
			return;
		}
	};
}
