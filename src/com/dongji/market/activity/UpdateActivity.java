package com.dongji.market.activity;

import java.io.IOException;
import java.util.ArrayList;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.dongji.market.R;
import com.dongji.market.adapter.ListBaseAdapter;
import com.dongji.market.adapter.ListMultiTemplateAdapter;
import com.dongji.market.adapter.ListSingleTemplateAdapter;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.pojo.NavigationInfo;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.ScrollListView;

/**
 * 最近更新、装机必备
 * 
 * @author zhangkai
 * 
 */
public class UpdateActivity extends BaseActivity implements OnItemClickListener{
	private static final int EVENT_REQUEST_APPLIST_DATA = 1;
	private static final int EVENT_REQUEST_GAMELIST_DATA = 2;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private static final int EVENT_REFRENSH_DATA = 6;

	private static final int SCROLL_DVALUE = 1;

	private static final int TYPE_UPDATE = 1, TYPE_INSTALL = 3;

	private int currentType;

	private Context context;
	private MyHandler mHandler;

	private List<ApkItem> apps;
	private List<ApkItem> games;

	private ScrollListView mAppListView;
	private ScrollListView mGameListView;

	private ListMultiTemplateAdapter mAppMultiAdapter;
	private ListSingleTemplateAdapter mAppSingleAdapter;

	private ListMultiTemplateAdapter mGameMultiAdapter;
	private ListSingleTemplateAdapter mGameSingleAdapter;

	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;

	private DataManager dataManager;

	private boolean isSingleRow = true;
	private boolean isLoading;
	private boolean isAppClicked = true;

	private int currentAppPage;
	private int currentGamePage;

	private boolean isFirstResume = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_template);
		context = this;
		initData();
		initLoadingView();
		initHandler();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		dataManager = DataManager.newInstance();
		currentType = getIntent().getIntExtra("type", 1);
	}

	/**
	 * 初始化加载视图
	 */
	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mLoadingProgressBar = findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					if (isAppClicked) {
						mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
					} else {
						mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
					}
				}
				return false;
			}
		});
	}

	/**
	 * 初始化handler
	 */
	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("handlerThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);;
	}

	@Override
	protected void onResume() {
		super.onResume();
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
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return getParent().onKeyDown(keyCode, event);
	}

	/**
	 * 初始化应用列表视图,单行还是双行显示
	 */
	private void initAppListView() {
		mAppListView = (ScrollListView) findViewById(R.id.applistview);
		if (isSingleRow) {
			mAppSingleAdapter = new ListSingleTemplateAdapter(this, apps, isRemoteImage);
			mAppListView.setAdapter(mAppSingleAdapter);
		} else {
			mAppMultiAdapter = new ListMultiTemplateAdapter(this, apps);
			mAppListView.setAdapter(mAppMultiAdapter);
		}
		if (isSingleRow) {
			mAppListView.setOnItemClickListener(this);
		}
		if (isAppClicked) {
			mAppListView.setVisibility(View.VISIBLE);
			if (mGameListView != null) {
				mGameListView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 初始化游戏列表视图，单行还是双行显示
	 */
	private void initGameListView() {
		mGameListView = (ScrollListView) findViewById(R.id.gamelistview);
		if (isSingleRow) {
			mGameSingleAdapter = new ListSingleTemplateAdapter(this, games, isRemoteImage);
			mGameListView.setAdapter(mGameSingleAdapter);
		} else {
			mGameMultiAdapter = new ListMultiTemplateAdapter(this, games);
			mGameListView.setAdapter(mGameMultiAdapter);
		}
		if (isSingleRow) {
			mGameListView.setOnItemClickListener(this);
		}
		if (!isAppClicked) {
			if (mAppListView != null) {
				mAppListView.setVisibility(View.GONE);
			}
			mGameListView.setVisibility(View.VISIBLE);
		}
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
				isLoading = false;
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					AndroidUtils.showToast(context, rId2);
				}
			}
		});
	}

	/**
	 * 设置预加载
	 */
	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	/**
	 * 显示加载
	 */
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

	/**
	 * 设置listView显示
	 */
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

	/**
	 * 获取应用数据（应用、游戏）
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private List<ApkItem> getApps() throws IOException, JSONException {
		return dataManager.getApps(this, currentType == TYPE_UPDATE ? DataManager.RECENT_UPDATA_ID : DataManager.ESSENTIAL_ID, isAppClicked);
	}

	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_APPLIST_DATA:// 请求应用信息
				try {
					apps = getApps();
					apps = setApkStatus(apps);// 设置应用状态
				} catch (IOException e) {
					System.out.println("ioexception:" + e);
				} catch (JSONException e) {
					System.out.println("jsonexception:" + e);
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				}
				if (apps != null && apps.size() > 0) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (currentAppPage == 0) {// 初始化
								currentAppPage = 1;
								initAppListView();
								mLoadingView.setVisibility(View.GONE);
							} else {// 显示列表，隐藏加载
								if (isAppClicked) {
									if (mAppListView.getVisibility() == View.GONE) {
										mAppListView.setVisibility(View.VISIBLE);
										mLoadingView.setVisibility(View.GONE);
									}
								}
								currentAppPage++;
								isLoading = false;
							}
						}
					});
				} else {
					isLoading = false;
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				}
				break;
			case EVENT_REQUEST_GAMELIST_DATA:// 请求游戏信息
				try {
					games = getApps();
					games = setApkStatus(games);
				} catch (IOException e) {
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				} catch (JSONException e) {
					sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				}
				if (games != null && games.size() > 0) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (currentGamePage == 0) {
								currentGamePage = 1;
								initGameListView();
								mLoadingView.setVisibility(View.GONE);
							} else {
								if (!isAppClicked) {
									if (mGameListView.getVisibility() == View.GONE) {
										mGameListView.setVisibility(View.VISIBLE);
										mLoadingView.setVisibility(View.GONE);
									}
								}
								currentGamePage++;
								isLoading = false;
							}
						}
					});
				} else {
					isLoading = false;
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				}
				break;
			case EVENT_NO_NETWORK_ERROR:// 网络错误
				setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:// 请求数据失败
				setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
				break;
			case EVENT_REFRENSH_DATA:// 请求刷新数据
				refreshData();
				break;
			}
		}
	}

	@Override
	public boolean isAppClicked() {
		return isAppClicked;
	}

	/**
	 * 点击应用按钮
	 */
	@Override
	public void onAppClick() {
		if (!isAppClicked) {
			isAppClicked = true;
			if (currentAppPage == 0) {
				displayLoading();
				mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
			} else {
				setDisplayVisible();
			}
		}
	}

	/**
	 * 点击游戏按钮
	 */
	@Override
	public void onGameClick() {
		if (isAppClicked) {
			isAppClicked = false;
			if (currentGamePage == 0) {
				displayLoading();
				mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
			} else {
				setDisplayVisible();
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
	}


	/**
	 * 当app取消下载或更新的时候回调,更改应用状态
	 */
	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			if (isSingleRow) {
				mAppSingleAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			} else {
				mAppMultiAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			}
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			if (isSingleRow) {
				mGameSingleAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			} else {
				mGameMultiAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			}
		}
	}

	/**
	 * 当app安装或卸载的时候回调，更改应用状态
	 */
	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			if (isSingleRow) {
				mAppSingleAdapter.changeApkStatusByPackageInfo(status, info);
			} else {
				mAppMultiAdapter.changeApkStatusByPackageInfo(status, info);
			}
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			if (isSingleRow) {
				mGameSingleAdapter.changeApkStatusByPackageInfo(status, info);
			} else {
				mGameMultiAdapter.changeApkStatusByPackageInfo(status, info);
			}
		}
	}
	
	/**
	 * 刷新数据
	 */
	private void refreshData() {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			if (isSingleRow) {
				notifyListData(mAppSingleAdapter);
			} else {
				notifyListData(mAppMultiAdapter);
			}
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			if (isSingleRow) {
				notifyListData(mGameSingleAdapter);
			} else {
				notifyListData(mGameMultiAdapter);
			}
		}
	}
	
	/**
	 * 通知数据更新
	 * @param mAdapter
	 */
	private void notifyListData(final ListBaseAdapter mAdapter) {
		setApkStatus(mAdapter.getItemList());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				System.out.println("update isRemoteImage:" + isRemoteImage);
				mAdapter.setDisplayNotify(isRemoteImage);
			}
		});
	}

	@Override
	protected void onUpdateDataDone() {
		refreshData();
	}


	@Override
	protected void loadingImage() {
		if (mAppSingleAdapter != null) {
			mAppSingleAdapter.setDisplayNotify(isRemoteImage);
		}
		if (mGameSingleAdapter != null) {
			mGameSingleAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	private int locStep;

	/**
	 * 重载baseactivity方法
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
			// TODO Auto-generated method stub
			if (mAppListView.getFirstVisiblePosition() > 0) {
				if (mAppListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mAppListView.setSelection(mAppListView.getFirstVisiblePosition() - 1);
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
					mGameListView.setSelection(mGameListView.getFirstVisiblePosition() - 1);
				} else {
					mGameListView.setSelection(Math.max(mGameListView.getFirstVisiblePosition() - locStep, 0));
				}
				// mAppListView.postDelayed(this, 1);
				mGameListView.post(this);
			}
			return;
		}
	};

	@Override
	public void onItemClick(ChannelListInfo info) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, ApkDetailActivity.class);
		Bundle bundle = new Bundle();
		switch (parent.getId()) {
		case R.id.applistview:
			bundle.putParcelable("apkItem", mAppSingleAdapter.getApkItemByPosition(position));
			break;
		case R.id.gamelistview:
			bundle.putParcelable("apkItem", mGameSingleAdapter.getApkItemByPosition(position));
			break;
		}
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
