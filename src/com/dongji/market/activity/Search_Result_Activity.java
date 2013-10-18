package com.dongji.market.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.myjson.JSONException;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.GuessLikeAdapter;
import com.dongji.market.adapter.SearchResultAdapter;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.ScrollListView;

/**
 * 搜索结果页
 * 
 * @author yvon
 * 
 */
public class Search_Result_Activity extends PublicActivity implements OnScrollListener, OnToolBarBlankClickListener {

	private static final int EVENT_REQUEST_SEARCH_LIST = 2;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private static final int EVENT_LOADING = 5;
	private static final int EVENT_LOADED = 6;

	private static final int EVENT_REFRENSH_DATA = 8;
	private static final int EVENT_REQUEST_GUESS_LIST = 9;

	private static final int SCROLL_DVALUE = 1;

	private int currentPage;
	private boolean isLoading, continueLoad;

	private List<ApkItem> data;

	private TitleUtil titleUtil;
	private SearchResultAdapter adapter;
	private MyHandler handler;
	private View mLoadingProgressBar, mLoadingView;
	private ProgressBar mProgressBar;
	private TextView mLoadingTextView;
	private TextView mResultCount;
	private ScrollListView mResultList;
	private FrameLayout mShowResult;
	private boolean isFirstResume = true;
	private boolean hasData = true;

	private LinearLayout llhasdata, llnodata;

	private Bitmap defaultBitmap_icon;

	private static List<ApkItem> guessList;

	private GridView mGuessLikeGrid;
	private LinearLayout mGuessLikeLayout;

	private View mGuessLoadingLayout;
	private ProgressBar mGuessLoadingProgressBar;
	private TextView mGuessLoadingTextView;
	private int locStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

		initTitle();

		mResultCount = (TextView) findViewById(R.id.result_count);
		mProgressBar = (ProgressBar) findViewById(R.id.loading_progress);

		mShowResult = (FrameLayout) findViewById(R.id.show_search_result);
		mResultList = (ScrollListView) findViewById(R.id.result_list);

		llhasdata = (LinearLayout) findViewById(R.id.llhasdata);
		llnodata = (LinearLayout) findViewById(R.id.llnodata);

		try {
			InputStream is = getResources().openRawResource(R.drawable.app_default_icon);
			defaultBitmap_icon = BitmapFactory.decodeStream(is);
		} catch (OutOfMemoryError e) {
			if (defaultBitmap_icon != null && !defaultBitmap_icon.isRecycled()) {
				defaultBitmap_icon.recycle();
			}
		}

		initHandler();
		initLoadingView();
	}

	/**
	 * 初始化标题栏
	 */
	private void initTitle() {
		String titleName = getIntent().getStringExtra("search_keyword");
		View mTopView = findViewById(R.id.search_result_top);
		Bundle bundle = getIntent().getExtras();
		titleUtil = new TitleUtil(this, mTopView, titleName, bundle, this);
	}

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("handler");
		mHandlerThread.start();
		handler = new MyHandler(mHandlerThread.getLooper());
		handler.sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);
	}

	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_SEARCH_LIST:
				try {
					data = DataManager.newInstance().getSearchResult(getIntent().getStringExtra("search_keyword"));
					data = setApkStatus(data);
				} catch (IOException e) {
					e.printStackTrace();
					if (!DJMarketUtils.isNetworkAvailable(Search_Result_Activity.this)) {
						handler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						handler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
					break;
				} catch (JSONException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					break;
				}
				if (data != null && data.size() > 0) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							llhasdata.setVisibility(View.VISIBLE);
							llnodata.setVisibility(View.GONE);
							mResultCount.setText(getResources().getString(R.string.text1) + data.size() + getResources().getString(R.string.text2));
							if (currentPage == 0) {
								currentPage = 1;
								initListData();
								mLoadingView.setVisibility(View.GONE);
								isLoading = true;
							} else {
								sendEmptyMessage(EVENT_LOADED);
								if (continueLoad && data != null && data.size() > 0) {
									adapter.addList(data);
									continueLoad = false;
								}
								currentPage++;
								isLoading = false;
							}
						}
					});
				} else {
					hasData = false;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							llhasdata.setVisibility(View.GONE);
							llnodata.setVisibility(View.VISIBLE);
							handler.sendEmptyMessage(EVENT_REQUEST_GUESS_LIST);
						}
					});

				}
				break;
			case EVENT_REQUEST_GUESS_LIST:
				try {
					String top50time = DJMarketUtils.getSharedPreferences(Search_Result_Activity.this, AConstDefine.SHARE_GETTOP50TIME, "");
					Calendar cal = Calendar.getInstance();
					String dateString = "" + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DATE);
					if (null == Search_Activity.apkItems || !top50time.equals(dateString)) {
						Search_Activity.apkItems = DataManager.newInstance().getTop50();
						DJMarketUtils.setSharedPreferences(Search_Result_Activity.this, AConstDefine.SHARE_GETTOP50TIME, dateString);
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (!DJMarketUtils.isNetworkAvailable(getApplicationContext())) {
						handler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						handler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				}
				if (null != Search_Activity.apkItems) {
					guessList = new ArrayList<ApkItem>();
					Random random = new Random();
					int[] ranInt = new int[8];
					ranInt[0] = random.nextInt(Search_Activity.apkItems.size());
					guessList.add(Search_Activity.apkItems.get(ranInt[0]));
					System.out.println("guesslist........" + ranInt[0]);
					for (int i = 1; i < 8; i++) {
						int tempRandom = random.nextInt(Search_Activity.apkItems.size());
						for (int j = 0; j < i; j++) {
							while (tempRandom == ranInt[j]) {
								tempRandom = random.nextInt(Search_Activity.apkItems.size());
							}
						}
						ranInt[i] = tempRandom;
						guessList.add(Search_Activity.apkItems.get(ranInt[i]));
						System.out.println("guesslist........" + ranInt[i]);
					}
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mGuessLoadingLayout.setVisibility(View.GONE);
						showGuessLike();
					}
				});
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
				break;
			case EVENT_LOADING:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = mProgressBar.getProgress();
						if (progress < 80) {
							mProgressBar.setProgress(progress + 10);
							sendEmptyMessageDelayed(EVENT_LOADING, 300);
						}
					}
				});
				break;
			case EVENT_LOADED:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = mProgressBar.getProgress();
						if (progress == 100) {
							mProgressBar.setVisibility(View.GONE);
						} else {
							mProgressBar.setProgress(100);
							sendEmptyMessageDelayed(EVENT_LOADED, 500);
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

	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				isLoading = false;
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					DJMarketUtils.showToast(Search_Result_Activity.this, rId2);
				}
			}
		});
	}

	/**
	 * 初始化数据列表
	 */
	private void initListData() {
		adapter = new SearchResultAdapter(Search_Result_Activity.this, data, isRemoteImage);
		mResultList.setAdapter(adapter);
		mResultList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelable("apkItem", (ApkItem) adapter.getItem(position));
				intent.putExtras(bundle);
				intent.setClass(Search_Result_Activity.this, ApkDetailActivity.class);
				startActivity(intent);
			}
		});
		mShowResult.setVisibility(View.VISIBLE);
	}

	private void showGuessLike() {
		final List<ApkItem> list = getGuessLikeList(guessList);
		mGuessLikeGrid = new GridView(this);
		mGuessLikeLayout = (LinearLayout) findViewById(R.id.show_gridview);
		if (list.size() == 0) {
			mGuessLikeGrid.setVisibility(View.GONE);
			mGuessLoadingLayout.setVisibility(View.VISIBLE);
			mGuessLoadingProgressBar.setVisibility(View.GONE);
			mGuessLoadingTextView.setText(R.string.no_data);
			return;
		} else {
			mGuessLoadingLayout.setVisibility(View.GONE);
			mGuessLikeGrid.setVisibility(View.VISIBLE);
		}
		int columnWidth = DJMarketUtils.dip2px(this, 48);
		int horizontalSpacing = DJMarketUtils.dip2px(this, 10);
		LayoutParams params = new LayoutParams(list.size() * columnWidth + list.size() * horizontalSpacing, columnWidth + horizontalSpacing * 4);
		mGuessLikeGrid.setLayoutParams(params);
		mGuessLikeGrid.setColumnWidth(columnWidth);
		mGuessLikeGrid.setHorizontalSpacing(horizontalSpacing);
		mGuessLikeGrid.setNumColumns(list.size());
		mGuessLikeGrid.setStretchMode(GridView.NO_STRETCH);
		mGuessLikeGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String keyword = list.get(position).appName;
				titleUtil.history.add(keyword);
				Intent intent = new Intent();
				intent.putExtra("search_keyword", keyword);
				intent.setClass(Search_Result_Activity.this, Search_Result_Activity.class);
				startActivity(intent);
			}
		});
		GuessLikeAdapter adapter = new GuessLikeAdapter(this, list, defaultBitmap_icon);
		mGuessLikeGrid.setAdapter(adapter);
		mGuessLikeLayout.addView(mGuessLikeGrid);

	}

	private List<ApkItem> getGuessLikeList(List<ApkItem> data) {
		List<ApkItem> list = new ArrayList<ApkItem>();
		if (data != null) {
			if (data.size() <= 8) {
				list = data;
			} else {
				for (int i = 0; i < 8; i++) {
					list.add(data.get(i));
				}
			}
		}
		return list;
	}

	/**
	 * 刷新数据
	 */
	private void refreshData() {
		setApkStatus(adapter.getItemList());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.setDisplayNotify(isRemoteImage);
			}
		});
	}

	/**
	 * 初始化加载环形进度条
	 */
	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mLoadingProgressBar = findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (hasData && mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					handler.sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);
				}
				return false;
			}
		});
		mGuessLoadingLayout = findViewById(R.id.guessLodinglayout);
		mGuessLoadingProgressBar = (ProgressBar) mGuessLoadingLayout.findViewById(R.id.guessloading_progressbar);
		mGuessLoadingTextView = (TextView) mGuessLoadingLayout.findViewById(R.id.guessloading_textview);
		mGuessLoadingLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mGuessLoadingProgressBar.getVisibility() == View.GONE) {
					initGuessLoading();
					handler.sendEmptyMessage(EVENT_REQUEST_GUESS_LIST);
				}
				return false;
			}
		});
	}

	/**
	 * 设置加载环形进度条为可见状态
	 */
	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	private void initGuessLoading() {
		mGuessLoadingLayout.setVisibility(View.VISIBLE);
		mGuessLoadingProgressBar.setVisibility(View.VISIBLE);
		mGuessLoadingTextView.setText(R.string.loading_txt);
	}

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isFirstResume) {
			if (handler != null && adapter != null) {
				if (handler.hasMessages(EVENT_REFRENSH_DATA)) {
					handler.removeMessages(EVENT_REFRENSH_DATA);
				}
				handler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
		isFirstResume = false;
		if (titleUtil != null) {
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}

	@Override
	protected void onDestroy() {
		titleUtil.unregisterMyReceiver(this);
		super.onDestroy();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (!isLoading && firstVisibleItem + visibleItemCount > totalItemCount - SCROLL_DVALUE) {
			adapter.addList(data);
			isLoading = true;
			showProgressBar();
			handler.sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);
		} else if (isLoading && firstVisibleItem + visibleItemCount > totalItemCount - SCROLL_DVALUE) {
			handler.sendEmptyMessageDelayed(EVENT_LOADING, 300);
			continueLoad = true;
		}
	}

	/**
	 * 显示同步加载数据时条形进度条进度
	 */
	public void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setProgress(0);
		handler.sendEmptyMessage(EVENT_LOADING);
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		if (mResultList != null && adapter != null) {
			adapter.changeApkStatusByPackageInfo(status, info);
		}
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		if (mResultList != null && adapter != null) {
			adapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
		}
	}

	@Override
	protected void onUpdateDataDone() {
		if (mResultList != null && mResultList.getAdapter() != null) {
			setApkStatus(adapter.getItemList());
			adapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	protected void loadingImage() {
		if (adapter != null) {
			adapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	public void onClick() {
		if (mResultList != null) {
			locStep = (int) Math.ceil(mResultList.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
			mResultList.post(scrollToTop);
		}
	}

	Runnable scrollToTop = new Runnable() {

		@Override
		public void run() {
			if (mResultList.getFirstVisiblePosition() > 0) {
				if (mResultList.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mResultList.setSelection(mResultList.getFirstVisiblePosition() - 1);
				} else {
					mResultList.setSelection(Math.max(mResultList.getFirstVisiblePosition() - locStep, 0));
				}
				mResultList.post(this);
			}
			return;
		}
	};
}
