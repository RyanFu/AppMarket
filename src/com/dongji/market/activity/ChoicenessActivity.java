package com.dongji.market.activity;

import java.io.IOException;
import java.util.List;

import org.myjson.JSONException;

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
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.dongji.market.R;
import com.dongji.market.adapter.ImageGalleryAdapter;
import com.dongji.market.adapter.ListBaseAdapter;
import com.dongji.market.adapter.ListSingleTemplateAdapter;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
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
	private ImageGalleryAdapter mGalleryAdapter;
	private static final int EVENT_ROTATE = 1;
	private static final int EVENT_REQUEST_BANNER_DATA = 2;
	private static final int EVENT_REQUEST_APPLIST_DATA = 3;
	private static final int EVENT_REQUEST_GAMELIST_DATA = 4;
	private static final int EVENT_NO_NETWORK_ERROR = 5;
	private static final int EVENT_REQUEST_DATA_ERROR = 6;
	private static final int EVENT_REFRENSH_DATA = 8;
	private static final int EVENT_COLLECT_DEVICE_INFO = 12;

	private static final long ROTATE_TIME = 2000L;
	private Gallery mImageGallery;
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

	private int responseResult;

	private int locStep;

	/**
	 * 设置聚焦指示按钮
	 */
	private ImageView mSelectedSwitchButton;

	/**
	 * 指示按钮
	 */
	private LinearLayout mSwithBtnContainer;

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
		mHandler.sendEmptyMessage(EVENT_COLLECT_DEVICE_INFO);// 获取设备信息
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
			case EVENT_ROTATE:// gallery转动
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mImageGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);// 右滑
						sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);
					}
				});
				break;
			case EVENT_REQUEST_BANNER_DATA:// 获取banner信息
				if (requestBannerData()) {
					if (isAppClicked) {
						sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
					} else {
						sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
					}
				} else {
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				}
				break;
			case EVENT_REQUEST_APPLIST_DATA:// 获取应用列表信息
				try {
					apps = dataManager.getApps(context, DataManager.EDITOR_RECOMMEND_ID, true);
					System.out.println("apps ===> " + apps);
					apps = setApkStatus(apps);
				} catch (JSONException e) {
					System.out.println(e);
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					break;
				}
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
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				}
				break;
			case EVENT_REQUEST_GAMELIST_DATA:// 获取游戏应用列表
				try {
					games = dataManager.getApps(context, DataManager.EDITOR_RECOMMEND_ID, false);
					games = setApkStatus(games);
				} catch (JSONException e) {
					System.out.println(e);
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					break;
				}
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
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				}
				break;
			case EVENT_REFRENSH_DATA:// 刷新数据
				refreshData();
				if (responseResult != 1) {
					mHandler.sendEmptyMessage(EVENT_COLLECT_DEVICE_INFO);
				}
				break;
			case EVENT_COLLECT_DEVICE_INFO:
				try {
					if (AndroidUtils.isNetworkAvailable(context)) {
						responseResult = DataManager.newInstance().collectLocalData(context);
					} else {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					}
				} catch (IOException e) {
					e.printStackTrace();
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

	private void initView() {
		if (mHeaderView == null) {
			initHeaderView();
		}
		initAppListView();
	}

	private void initHeaderView() {
		mHeaderView = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.layout_choiceness_header, null);
		mImageGallery = (Gallery) mHeaderView.findViewById(R.id.choicenessgallery);
		mGalleryAdapter = new ImageGalleryAdapter(this, bannerList);
		mImageGallery.setAdapter(mGalleryAdapter);
		int count = bannerList.size();
		if (count > 0) {
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
		getParentActivity().setInterceptRange(mImageGallery);
	}

	/**
	 * 初始化海报指示标
	 * 
	 * @param count
	 */
	private void initSwitchIdResources(int count, View mHeaderView) {
		mSwithBtnContainer = (LinearLayout) mHeaderView.findViewById(R.id.switcherbtn_container);

		ImageView localImageView = null;
		int num = AndroidUtils.dip2px(this, 10.0f);

		SwitchBtnClickListener localSwitchBtnClickListener = new SwitchBtnClickListener();

		LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(num, num);
		localLayoutParams.leftMargin = 5;
		localLayoutParams.rightMargin = 5;

		for (int j = 0; j < count; j++) {
			localImageView = new ImageView(this);
			localImageView.setLayoutParams(localLayoutParams);
			localImageView.setBackgroundResource(R.drawable.selector_image_switcher);
			localImageView.setOnClickListener(localSwitchBtnClickListener);
			mSwithBtnContainer.addView(localImageView);
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

	private void setSelectedSwitchBtn(int paramInt) {
		if (this.mSelectedSwitchButton != null)
			this.mSelectedSwitchButton.setSelected(false);
		ImageView localImageView = (ImageView) this.mSwithBtnContainer.getChildAt(paramInt);
		this.mSelectedSwitchButton = localImageView;
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
					AndroidUtils.showToast(context, rId2);
				}
			}
		});
	}

	private void refreshData() {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			notifyListData(mAppSingleAdapter);
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			notifyListData(mGameSingleAdapter);
		}
	}

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
			System.out.println(item.appName + ", " + item.status);
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
