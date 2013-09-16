package com.dongji.market.activity;

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
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.dongji.market.R;
import com.dongji.market.adapter.ListBaseAdapter;
import com.dongji.market.adapter.ListSingleTemplateAdapter;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnSortChangeListener;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.protocol.DataManager;
import com.umeng.analytics.MobclickAgent;

/**
 * 分类列表页面
 * 
 * @author zhangkai
 */
public class ChannelListActivity extends PublicActivity implements OnItemClickListener, OnScrollListener, OnSortChangeListener, OnToolBarBlankClickListener {
	private ChannelListInfo info;
	private MyHandler mHandler;
	private final static int EVENT_REQUEST_DATA = 1;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;

	private static final int EVENT_LOADING_PROGRESS = 5;
	private static final int EVENT_LOADDONE = 6;

	private static final int EVENT_REFRENSH_DATA = 7;

	private static final int SCROLL_DVALUE = 1;

	private Context context;
	private boolean hasData = true;
	private boolean isLoading = false;
	private boolean isRequestDelay;

	private boolean isFirstResume;

	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	private ListView mListView;
	private ProgressBar mBottomProgressBar;

	private TitleUtil titleUtil;

	private List<ApkItem> apps;

	private View mContentView;
	private int currentSort = 1;
	private int locStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		context = this;
		initData();
		initLoadingView();
		initHandler();
	}

	private void initData() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			info = bundle.getParcelable("channelListInfo");
			if (info != null) {
				View mTopView = findViewById(R.id.search_result_top);
				titleUtil = new TitleUtil(this, mTopView, info.name, null, this, this);
				findViewById(R.id.result_count).setVisibility(View.GONE);
				findViewById(R.id.shawview).setVisibility(View.GONE);
				mBottomProgressBar = (ProgressBar) findViewById(R.id.loading_progress);
				info.currentPage = 1;
			}
		} else {
			AndroidUtils.showToast(this, R.string.request_channel_data_error);
			finish();
		}
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
		if (info != null && info.pageCount > 0) {
			isLoading = true;
			mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
		} else {
			mLoadingProgressBar.setVisibility(View.GONE);
			mLoadingTextView.setText(R.string.current_channel_not_data);
		}
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_DATA:
				if (info.currentPage <= info.pageCount) {
					try {
						DataManager dataManager = DataManager.newInstance();
						apps = dataManager.getApps(info, context, currentSort);
						if (apps != null && apps.size() > 0) {
							apps = setApkStatus(apps);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (info.currentPage == 1) {
										if (mListView == null) {
											initViews();
										} else {
											apps = setApkStatus(apps);
											ListSingleTemplateAdapter mAdapter = (ListSingleTemplateAdapter) mListView.getAdapter();
											mAdapter.addList(apps);
											if (apps != null) {
												apps.clear();
											}
											mListView.setVisibility(View.VISIBLE);
											mContentView.setVisibility(View.VISIBLE);
											mLoadingView.setVisibility(View.GONE);
										}
										info.currentPage++;
										if (info.pageCount >= info.currentPage) {
											sendEmptyMessage(EVENT_REQUEST_DATA);
											isLoading = true;
										} else {
											mBottomProgressBar.setVisibility(View.GONE);
											hasData = false;
											apps = null;
										}
									} else {
										onProgressBarDone();
										if (mListView.getVisibility() == View.GONE) {
											mListView.setVisibility(View.VISIBLE);
											mContentView.setVisibility(View.VISIBLE);
											mLoadingView.setVisibility(View.GONE);
										}
										if (isRequestDelay) {
											addAdapterData();
											if (info.pageCount >= info.currentPage) {
												sendEmptyMessage(EVENT_REQUEST_DATA);
												isRequestDelay = false;
											} else {
												hasData = false;
												isRequestDelay = false;
											}
										}
										info.currentPage++;
										isLoading = false;
									}
								}
							});
						} else {
							if (hasData) {
								isLoading = false;
								isRequestDelay = false;
							}
							if (!AndroidUtils.isNetworkAvailable(context)) {
								sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
							} else {
								sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
							}
						}
					} catch (JSONException e) {
						System.out.println("jsonexception:" + e);
						if (!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						} else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
				}
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
				break;
			case EVENT_LOADING_PROGRESS:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = mBottomProgressBar.getProgress();
						if (progress < 80) {
							mBottomProgressBar.setProgress(progress + 10);
							sendEmptyMessageDelayed(EVENT_LOADING_PROGRESS, 300);
						}
					}
				});
				break;
			case EVENT_LOADDONE:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = mBottomProgressBar.getProgress();
						if (progress == 100) {
							mBottomProgressBar.setVisibility(View.GONE);
						} else {
							mBottomProgressBar.setProgress(100);
							sendEmptyMessageDelayed(EVENT_LOADDONE, 500);
						}
					}
				});
				break;
			case EVENT_REFRENSH_DATA:
				refreshData();
				break;
			}
		}
	}

	private void initViews() {
		mListView = (ListView) findViewById(R.id.result_list);
		mListView.setAdapter(new ListSingleTemplateAdapter(context, apps, isRemoteImage));
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);
		mLoadingView.setVisibility(View.GONE);
		mContentView = findViewById(R.id.show_search_result);
		mContentView.setVisibility(View.VISIBLE);
	}

	private void refreshData() {
		if (mListView != null && mListView.getAdapter() != null) {
			ListBaseAdapter mAdapter = (ListBaseAdapter) mListView.getAdapter();
			notifyListData(mAdapter);
		}
	}

	private void notifyListData(final ListBaseAdapter mAdapter) {
		setApkStatus(mAdapter.getItemList());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.setDisplayNotify(isRemoteImage);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, ApkDetailActivity.class);
		Bundle bundle = new Bundle();
		ApkItem apkItem = (ApkItem) mListView.getAdapter().getItem(position);
		bundle.putParcelable("apkItem", apkItem);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (isFirstResume) {
			if (mHandler != null) {
				if (mHandler.hasMessages(EVENT_REFRENSH_DATA)) {
					mHandler.removeMessages(EVENT_REFRENSH_DATA);
				}
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);// 刷新数据
			}
		}
		isFirstResume = true;
		if (titleUtil != null) {// 刷新头部
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (titleUtil != null) {
			titleUtil.unregisterMyReceiver(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (titleUtil != null) {
			titleUtil.showOrDismissSettingPopupWindow();
		}
		return false;
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		if (mListView != null && mListView.getAdapter() != null) {
			ListBaseAdapter mAdapter = (ListBaseAdapter) mListView.getAdapter();
			mAdapter.changeApkStatusByPackageInfo(status, info);
		}
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		if (mListView != null && mListView.getAdapter() != null) {
			ListBaseAdapter mAdapter = (ListBaseAdapter) mListView.getAdapter();
			mAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (hasData && !isLoading && firstVisibleItem + visibleItemCount >= totalItemCount - SCROLL_DVALUE) {
			isLoading = true;
			showProgressBar();
			addAdapterData();
			stopProgressBar();
		} else if (hasData && isLoading && !isRequestDelay && firstVisibleItem + visibleItemCount >= totalItemCount - SCROLL_DVALUE) {
			isRequestDelay = true;
			showProgressBar();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	protected void onUpdateDataDone() {
		if (mListView != null && mListView.getAdapter() != null) {
			ListBaseAdapter mAdapter = (ListBaseAdapter) mListView.getAdapter();
			setApkStatus(mAdapter.getItemList());
			mAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	public void onSortChanged(int sort) {
		if (info != null) {
			currentSort = sort;
			info.currentPage = 1;
			setPreLoading();
			if (mListView != null) {
				((ListSingleTemplateAdapter) mListView.getAdapter()).resetList();
				mListView.setVisibility(View.GONE);
				mContentView.setVisibility(View.GONE);
				listViewFromTop(mListView);
				hasData = info.currentPage <= info.pageCount;
			}
			mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
		}
	}

	@Override
	protected void loadingImage() {
		if (mListView != null && mListView.getAdapter() != null) {
			ListBaseAdapter mAdapter = (ListBaseAdapter) mListView.getAdapter();
			mAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	public void onClick() {
		listViewFromTop(mListView);
	}

	/**
	 * 往适配器里添加数据
	 */
	private void addAdapterData() {
		if (apps == null || apps.size() == 0) {
			isLoading = false;
			mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
			return;
		}
		apps = setApkStatus(apps);
		ListSingleTemplateAdapter mAdapter = (ListSingleTemplateAdapter) mListView.getAdapter();
		addAdapterRunUiThread(mAdapter, apps);
		if (apps != null) {
			apps.clear();
		}
	}

	private void addAdapterRunUiThread(final ListBaseAdapter mAdapter, final List<ApkItem> items) {
		mAdapter.addList(items);
		int what = 0;
		what = EVENT_REQUEST_DATA;
		if (info.pageCount < info.currentPage) {
			hasData = false;
			progressBarGone();
			return;
		}
		mHandler.sendEmptyMessage(what);
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
				stopProgressBar();
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					AndroidUtils.showToast(context, rId2);
				}
			}
		});
	}

	private void showProgressBar() {
		mBottomProgressBar.setVisibility(View.VISIBLE);
		mBottomProgressBar.setProgress(0);
		mHandler.sendEmptyMessage(EVENT_LOADING_PROGRESS);
	}

	private void stopProgressBar() {
		if (mHandler.hasMessages(EVENT_LOADING_PROGRESS)) {
			mHandler.removeMessages(EVENT_LOADING_PROGRESS);
		}
		if (mHandler.hasMessages(EVENT_LOADDONE)) {
			mHandler.removeMessages(EVENT_LOADDONE);
		}
	}

	private void progressBarGone() {
		if (mHandler != null && mHandler.hasMessages(EVENT_LOADING_PROGRESS)) {
			mHandler.removeMessages(EVENT_LOADING_PROGRESS);
		}
		if (mHandler != null && mHandler.hasMessages(EVENT_LOADDONE)) {
			mHandler.removeMessages(EVENT_LOADDONE);
		}
		if (mBottomProgressBar != null) {
			mBottomProgressBar.setVisibility(View.GONE);
		}
	}

	private void onProgressBarDone() {
		mHandler.sendEmptyMessage(EVENT_LOADDONE);
	}

	/**
	 * ListView 滑动到顶部
	 */
	private void listViewFromTop(ListView mListView) {
		if (mListView != null) {
			locStep = (int) Math.ceil(mListView.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
			mListView.post(scrollToTop);
		}
	}

	Runnable scrollToTop = new Runnable() {

		@Override
		public void run() {
			if (mListView.getFirstVisiblePosition() > 0) {
				if (mListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mListView.setSelection(mListView.getFirstVisiblePosition() - 1);
				} else {
					mListView.setSelection(Math.max(mListView.getFirstVisiblePosition() - locStep, 0));
				}
				mListView.post(this);
			}
			return;
		}
	};
}
