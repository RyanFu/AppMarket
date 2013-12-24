package com.dongji.market.activity;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.ImageGalleryAdapter;
import com.dongji.market.adapter.ListBaseAdapter;
import com.dongji.market.adapter.ListSingleTemplateAdapter;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.ScrollListView;

/**
 * 编辑推荐
 * 
 * @author zhangkai
 * 
 */
public class ChoicenessActivity extends BaseActivity implements OnItemClickListener {
	private static final int EVENT_ROTATE = 1;
	private static final int EVENT_REQUEST_BANNER_DATA = 2;
	private static final int EVENT_REQUEST_APPLIST_DATA = 3;
	private static final int EVENT_REQUEST_GAMELIST_DATA = 4;
	private static final int EVENT_NO_NETWORK_ERROR = 5;
	private static final int EVENT_REQUEST_DATA_ERROR = 6;
	private static final int EVENT_REFRENSH_DATA = 7;
	private static final long ROTATE_TIME = 2000L;
	private ImageGalleryAdapter mGalleryAdapter;// banner adapter
	private Gallery mImageGallery;// banner gallery
	private MyHandler mHandler;
	private List<ApkItem> bannerList;
	private FrameLayout mHeaderView;
	private ScrollListView mAppListView;
	private ScrollListView mGameListView;
	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	private List<ApkItem> apps;
	private List<ApkItem> games;
	private boolean isAppClicked = true;
	private ListSingleTemplateAdapter mAppSingleAdapter;
	private ListSingleTemplateAdapter mGameSingleAdapter;
	private int currentAppPage;
	private int currentGamePage;
	private DataManager dataManager;
	private Context context;
	private boolean isFirstResume = true;
	private boolean hasAppData = true;
	private boolean hasGameData = true;
	private int locStep;
	private ImageView mSelectedSwitchButton;// 设置聚焦指示按钮
	private LinearLayout mSwithBtnContainer;// 指示按钮

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_template);
		context = this;
		initLoadingView();
		initHandler();
		initData();
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
					if (isAppClicked && hasAppData) {
						mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
					} else if (!isAppClicked && hasGameData) {
						mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
					}
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
		HandlerThread mHandlerThread = new HandlerThread("handler");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
	}

	private void initData() {
		dataManager = DataManager.newInstance();
		mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);// 获取banner信息
	}

	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_BANNER_DATA:// 获取banner信息
				fetchBannerData();
				break;
			case EVENT_REQUEST_APPLIST_DATA:// 获取应用列表信息
				try {
					fetchAppsData();
				} catch (JSONException e) {
					System.out.println(e);
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					break;
				}
				handleAppsData();
				break;
			case EVENT_ROTATE:// gallery转动
				galleryRotate();
				break;
			case EVENT_REQUEST_GAMELIST_DATA:// 获取游戏应用列表
				try {
					fetchGamesData();
				} catch (JSONException e) {
					e.printStackTrace();
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					break;
				}
				handleGamesData();
				break;
			case EVENT_REFRENSH_DATA:// 刷新数据
				refreshData();
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
	 * gallery轮滑
	 */
	private void galleryRotate() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mImageGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);// 右滑
				mHandler.sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);// 定时右滑
			}
		});
	}

	/**
	 * 处理游戏数据
	 */
	private void handleGamesData() {
		if (games != null && games.size() > 0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (currentGamePage == 0) {
						currentGamePage = 1;
						initGameListView();
						mLoadingView.setVisibility(View.GONE);
						if (!mHandler.hasMessages(EVENT_ROTATE)) {
							ChoicenessActivity.this.sendMessage();
						}
					} else {
						currentGamePage++;
					}
				}
			});
		} else {
			if (!DJMarketUtils.isNetworkAvailable(context)) {
				mHandler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
			} else {
				mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
			}
		}
	}

	/**
	 * 获取游戏数据
	 * 
	 * @throws JSONException
	 */
	private void fetchGamesData() throws JSONException {
		games = dataManager.getApps(context, DataManager.EDITOR_RECOMMEND_ID, false);
		games = setApkStatus(games);// 重设游戏状态
	}

	/**
	 * 处理应用数据
	 */
	private void handleAppsData() {
		if (apps != null && apps.size() > 0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (currentAppPage == 0) {
						currentAppPage = 1;
						initView();
						mLoadingView.setVisibility(View.GONE);
						if (!mHandler.hasMessages(EVENT_ROTATE)) {
							ChoicenessActivity.this.sendMessage();
						}
					} else {
						currentAppPage++;
					}
				}
			});
		} else {
			if (!DJMarketUtils.isNetworkAvailable(context)) {
				mHandler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
			} else {
				mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
			}
		}
	}

	/**
	 * 获取应用数据
	 * 
	 * @throws JSONException
	 */
	private void fetchAppsData() throws JSONException {
		apps = dataManager.getApps(context, DataManager.EDITOR_RECOMMEND_ID, true);
		System.out.println("apps ===> " + apps);
		apps = setApkStatus(apps);// 重设应用状态
	}

	/**
	 * 获取banner数据
	 */
	private void fetchBannerData() {
		if (requestBannerData()) {// 请求banner数据
			if (isAppClicked) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);// 请求应用列表
			} else {
				mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);// 请求游戏列表
			}
		} else {
			if (!DJMarketUtils.isNetworkAvailable(context)) {
				mHandler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
			} else {
				mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
			}
		}
	}

	/**
	 * 请求banner数据
	 * 
	 * @return
	 */
	private boolean requestBannerData() {
		try {
			bannerList = dataManager.getBanners();
			if (bannerList != null && bannerList.size() > 0)
				return true;
		} catch (JSONException e) {
			System.out.println(e);
			mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
		}
		return false;
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		if (mHeaderView == null) {
			initHeaderView();
		}
		initAppListView();
	}

	/**
	 * 初始化gallery
	 */
	private void initHeaderView() {
		mHeaderView = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.layout_choiceness_header, null);
		mImageGallery = (Gallery) mHeaderView.findViewById(R.id.choicenessgallery);
		mGalleryAdapter = new ImageGalleryAdapter(this, bannerList);
		mImageGallery.setAdapter(mGalleryAdapter);
		int count = bannerList.size();
		if (count > 0) {// 取中间位置
			int i = (Integer.MAX_VALUE / 2);
			int num = i % count;
			if (num != 0) {
				i -= num;
			}
			mImageGallery.setSelection(i);
		}
		initSwitchIdResources(count, mHeaderView);// 初始化海报指示标切换器
		mImageGallery.setOnItemSelectedListener(new ItemSelectedListener(count));
		mImageGallery.setOnItemClickListener(this);
		mImageGallery.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mHandler.sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);
					break;
				default:
					mHandler.removeMessages(EVENT_ROTATE);
					break;
				}
				return false;
			}
		});
		getParentActivity().setInterceptRange(mImageGallery);// 设置水平滑动拦截view
	}

	/**
	 * 初始化海报指示标
	 * 
	 * @param count
	 */
	private void initSwitchIdResources(int count, View mHeaderView) {
		mSwithBtnContainer = (LinearLayout) mHeaderView.findViewById(R.id.switcherbtn_container);

		ImageView imageView = null;
		int num = DJMarketUtils.dip2px(this, 10.0f);

		SwitchBtnClickListener switchBtnClickListener = new SwitchBtnClickListener();

		LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(num, num);
		localLayoutParams.leftMargin = 5;
		localLayoutParams.rightMargin = 5;

		for (int j = 0; j < count; j++) {
			imageView = new ImageView(this);
			imageView.setLayoutParams(localLayoutParams);
			imageView.setBackgroundResource(R.drawable.selector_image_switcher);
			imageView.setOnClickListener(switchBtnClickListener);
			mSwithBtnContainer.addView(imageView);
		}
	}

	/**
	 * 切换按钮的点击事件响应
	 * 
	 * @author Administrator
	 * 
	 */
	public class SwitchBtnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int position = mSwithBtnContainer.indexOfChild(v);
			setSelectedSwitchBtn(position);
			setSelectedGalleryImg(position);
		}
	}

	/**
	 * 设置切换按钮
	 * 
	 * @param paramInt
	 */
	private void setSelectedSwitchBtn(int paramInt) {
		if (this.mSelectedSwitchButton != null)
			this.mSelectedSwitchButton.setSelected(false);
		ImageView iamgeView = (ImageView) this.mSwithBtnContainer.getChildAt(paramInt);
		this.mSelectedSwitchButton = iamgeView;
		this.mSelectedSwitchButton.setSelected(true);
	}

	/**
	 * 设置聚焦画廊海报图片
	 * 
	 * @param position
	 */
	private void setSelectedGalleryImg(int position) {
		if (mImageGallery != null) {
			if (position == Integer.MAX_VALUE - 1) {
				int count = bannerList == null ? 0 : bannerList.size();
				if (count > 0) {
					int i = (Integer.MAX_VALUE / 2);
					int num = i % count;
					if (num != 0) {
						i -= num;
					}
					mImageGallery.setSelection(i);
				}
			} else {
				mImageGallery.setSelection(position);
			}
		}
	}

	/**
	 * 画廊点击联动指示按钮
	 * 
	 * @author
	 * 
	 */
	public class ItemSelectedListener implements OnItemSelectedListener {

		int mCount;

		public ItemSelectedListener(int count) {
			super();
			mCount = count;
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			setSelectedSwitchBtn(position % mCount);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	}

	/**
	 * 初始化应用列表
	 */
	private void initAppListView() {
		mAppListView = (ScrollListView) findViewById(R.id.applistview);
		mAppListView.addHeaderView(mHeaderView, null, false);
		mAppSingleAdapter = new ListSingleTemplateAdapter(this, apps, isRemoteImage);
		mAppListView.setAdapter(mAppSingleAdapter);
		mAppListView.setOnItemClickListener(this);
		mAppListView.setVisibility(View.VISIBLE);
	}

	private void initGameListView() {
		mGameListView = (ScrollListView) findViewById(R.id.gamelistview);
		if (mHeaderView == null) {
			initHeaderView();
		}
		mGameListView.addHeaderView(mHeaderView, null, false);
		mGameSingleAdapter = new ListSingleTemplateAdapter(this, games, isRemoteImage);
		mGameListView.setAdapter(mGameSingleAdapter);
		mGameListView.setOnItemClickListener(this);
		if (mAppListView != null) {
			mAppListView.setVisibility(View.GONE);
		}
		mGameListView.setVisibility(View.VISIBLE);
	}

	/**
	 * 数据请求错误处理
	 * 
	 * @param rId
	 * @param rId2
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
	 * 刷新数据
	 */
	private void refreshData() {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			notifyListData(mAppSingleAdapter);
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			notifyListData(mGameSingleAdapter);
		}
	}

	/**
	 * 刷新列表数据
	 * 
	 * @param mAdapter
	 */
	private void notifyListData(final ListBaseAdapter mAdapter) {
		setApkStatus(mAdapter.getItemList());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mGalleryAdapter != null) {
					mGalleryAdapter.setDisplayNotify(isRemoteImage);
				}
				mAdapter.setDisplayNotify(isRemoteImage);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		sendMessage();
		if (!isFirstResume) {
			if (mHandler != null) {
				if (mHandler.hasMessages(EVENT_REFRENSH_DATA)) {
					mHandler.removeMessages(EVENT_REFRENSH_DATA);
				}
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
		isFirstResume = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		removeMessage();
	}

	private void sendMessage() {
		removeMessage();
		if (mHandler != null && mGalleryAdapter != null) {
			mHandler.sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);
		}
	}

	private void removeMessage() {
		if (mHandler != null && mHandler.hasMessages(EVENT_ROTATE)) {
			mHandler.removeMessages(EVENT_ROTATE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(context, ApkDetailActivity.class);
		Bundle bundle = new Bundle();
		switch (parent.getId()) {
		case R.id.applistview:
			ApkItem item = mAppSingleAdapter.getApkItemByPosition(position - 1);
			bundle.putParcelable("apkItem", item);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
			break;
		case R.id.gamelistview:
			bundle.putParcelable("apkItem", mGameSingleAdapter.getApkItemByPosition(position - 1));
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
			break;
		case R.id.choicenessgallery:
			intent = new Intent(this, ApkDetailActivity.class);
			position = position % bannerList.size();
			bundle.putParcelable("apkItem", bannerList.get(position));
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onAppClick() {
		if (!isAppClicked) {
			isAppClicked = true;
			if (currentAppPage == 0) {
				displayLoading();
				mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
			} else {
				setDisplayVisible();
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
	}

	@Override
	public void onGameClick() {
		if (isAppClicked) {
			isAppClicked = false;
			if (currentGamePage == 0) {
				displayLoading();
				mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
			} else {
				setDisplayVisible();
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
	}

	private void setDisplayVisible() {
		mLoadingView.setVisibility(View.GONE);
		if (isAppClicked) {
			if (mAppListView != null) {
				mAppListView.setVisibility(View.VISIBLE);
			}
			if (mGameListView != null) {
				mGameListView.setVisibility(View.GONE);
			}
		} else {
			if (mGameListView != null) {
				mGameListView.setVisibility(View.VISIBLE);
			}
			if (mAppListView != null) {
				mAppListView.setVisibility(View.GONE);
			}
		}
	}

	private void displayLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		setPreLoading();
		if (mAppListView != null) {
			mAppListView.setVisibility(View.GONE);
		}
		if (mGameListView != null) {
			mGameListView.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean isAppClicked() {
		return isAppClicked;
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			mAppSingleAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			mGameSingleAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
		}
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			mAppSingleAdapter.changeApkStatusByPackageInfo(status, info);
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			mGameSingleAdapter.changeApkStatusByPackageInfo(status, info);
		}
	}

	@Override
	protected void onUpdateDataDone() {
		refreshData();
	}

	@Override
	protected void loadingImage() {
		if (mGalleryAdapter != null) {
			mGalleryAdapter.setDisplayNotify(isRemoteImage);
		}
		if (mAppSingleAdapter != null) {
			mAppSingleAdapter.setDisplayNotify(isRemoteImage);
		}
		if (mGameSingleAdapter != null) {
			mGameSingleAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	public void OnToolBarClick() {
		listViewFromTop();
	}

	private void listViewFromTop() {
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
