package com.dongji.market.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.myjson.JSONException;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.GuessLikeAdapter;
import com.dongji.market.adapter.SearchResultAdapter;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.CustomIconAnimation;
import com.dongji.market.widget.ScrollListView;

public class Search_Result_Activity extends PublicActivity implements
		OnScrollListener, ScrollListView.OnScrollTouchListener, OnToolBarBlankClickListener {

	private static final int EVENT_REQUEST_SEARCH_LIST = 2;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private static final int EVENT_LOADING = 5;
	private static final int EVENT_LOADED = 6;

	private static final int EVENT_REFRENSH_DATA = 8;
	private static final int EVENT_REQUEST_GUESS_LIST = 9;

	private static final int SCROLL_DVALUE = 1;

	private int currentPage;
	private int sumCount;

	private boolean isLoading, continueLoad;

	private List<ApkItem> data;

	private TitleUtil titleUtil;
	private SearchResultAdapter adapter;
	private MyHandler handler;
	private CustomIconAnimation iconAnim;

	private View mLoadingProgressBar, mLoadingView;
	private ProgressBar mProgressBar;
	private TextView mLoadingTextView;
	private TextView mResultCount;
	private ScrollListView mResultList;
	private FrameLayout mShowResult;

	private ImageView mTempIcon;

	private ImageView mSoftwareBtn;

	private boolean isFirstResume = true;
	private boolean hasData = true;

	private LinearLayout ll_top;

	private LinearLayout llhasdata, llnodata;

	private Bitmap defaultBitmap_icon;

	// private static List<ApkItem> apkItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

		initTitle();

		iconAnim = new CustomIconAnimation(this);
		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		mTempIcon = (ImageView) findViewById(R.id.tempIcon);
		mSoftwareBtn = (ImageView) findViewById(R.id.softmanagerbutton);
		mResultCount = (TextView) findViewById(R.id.result_count);
		mProgressBar = (ProgressBar) findViewById(R.id.loading_progress);

		mShowResult = (FrameLayout) findViewById(R.id.show_search_result);
		mResultList = (ScrollListView) findViewById(R.id.result_list);
		mResultList.setOnScrollTouchListener(this);

		llhasdata = (LinearLayout) findViewById(R.id.llhasdata);
		llnodata = (LinearLayout) findViewById(R.id.llnodata);

		try {
			InputStream is = getResources().openRawResource(
					R.drawable.app_default_icon);
			defaultBitmap_icon = BitmapFactory.decodeStream(is);
			// mDefaultBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.app_default_icon);

		} catch (OutOfMemoryError e) {
			if (defaultBitmap_icon != null && !defaultBitmap_icon.isRecycled()) {
				defaultBitmap_icon.recycle();
			}
		}

		initHandler();
		initLoadingView();

		// handler.sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);

//		new SlideMenu(ll_top, mResultList);
	}

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (IllegalStateException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
//		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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
		if(titleUtil!=null) {
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(titleUtil!=null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
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

	private void initGuessLoading() {
		mGuessLoadingLayout.setVisibility(View.VISIBLE);
		mGuessLoadingProgressBar.setVisibility(View.VISIBLE);
		mGuessLoadingTextView.setText(R.string.loading_txt);
	}

	private static List<ApkItem> guessList;

	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_SEARCH_LIST:
				try {
					data = DataManager.newInstance().getSearchResult(
							getIntent().getStringExtra("search_keyword"));
					data = setApkStatus(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (!AndroidUtils
							.isNetworkAvailable(Search_Result_Activity.this)) {
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
					// sumCount += data.size();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							llhasdata.setVisibility(View.VISIBLE);
							llnodata.setVisibility(View.GONE);
							mResultCount.setText(getResources().getString(
									R.string.text1)
									+ data.size()
									+ getResources().getString(R.string.text2));
							if (currentPage == 0) {
								currentPage = 1;
								initListData();
								mLoadingView.setVisibility(View.GONE);
								sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);
								isLoading = true;
							} else {
								sendEmptyMessage(EVENT_LOADED);
								if (continueLoad && data != null
										&& data.size() > 0) {
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
					// if (sumCount == 0) {
					//
					// }
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
				// try {
				// guessList =
				// DataManager.newInstance().getSearchResult("围棋");
				try {
					String top50time = NetTool.getSharedPreferences(
							Search_Result_Activity.this,
							AConstDefine.SHARE_GETTOP50TIME, "");
					Calendar cal = Calendar.getInstance();
					String dateString = "" + cal.get(Calendar.YEAR)
							+ (cal.get(Calendar.MONTH) + 1)
							+ cal.get(Calendar.DATE);
					if (null == Search_Activity2.apkItems
							|| !top50time.equals(dateString)) {
						Search_Activity2.apkItems = DataManager.newInstance()
								.getTop50();
						NetTool.setSharedPreferences(
								Search_Result_Activity.this,
								AConstDefine.SHARE_GETTOP50TIME, dateString);
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (!AndroidUtils
							.isNetworkAvailable(getApplicationContext())) {
						handler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						handler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				}
				if (null != Search_Activity2.apkItems) {
					guessList = new ArrayList<ApkItem>();
					Random random = new Random();
					int[] ranInt = new int[8];
					ranInt[0] = random
							.nextInt(Search_Activity2.apkItems.size());
					guessList.add(Search_Activity2.apkItems.get(ranInt[0]));
					System.out.println("guesslist........" + ranInt[0]);
					for (int i = 1; i < 8; i++) {
						int tempRandom = random
								.nextInt(Search_Activity2.apkItems.size());
						for (int j = 0; j < i; j++) {
							while (tempRandom == ranInt[j]) {
								tempRandom = random
										.nextInt(Search_Activity2.apkItems
												.size());
							}
						}
						ranInt[i] = tempRandom;
						guessList.add(Search_Activity2.apkItems.get(ranInt[i]));
						System.out.println("guesslist........" + ranInt[i]);
					}
				}
				// } catch (IOException e) {
				// e.printStackTrace();
				// if (!AndroidUtils
				// .isNetworkAvailable(getApplicationContext())) {
				// sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
				// } else {
				// sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				// }
				// break;
				// } catch (JSONException e) {
				// e.printStackTrace();
				// sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
				// break;
				// }
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mGuessLoadingLayout.setVisibility(View.GONE);
						showGuessLike();
					}
				});
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg,
						R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg,
						R.string.request_data_error_msg2);
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
	};

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

	private GridView mGuessLikeGrid;
	private LinearLayout mGuessLikeLayout;

	private void showGuessLike() {
		// guessList = getGuessLikeList();
		final List<ApkItem> list = getGuessLikeList(guessList);
		mGuessLikeGrid = new GridView(this);
		mGuessLikeLayout = (LinearLayout) findViewById(R.id.show_gridview);
		// mGuessLoadingLayout = findViewById(R.id.guess_loading_layout);
		// mGuessLoadingProgressBar = (ProgressBar)
		// mGuessLoadingLayout.findViewById(R.id.loading_progressbar);
		// mGuessLoadingTextView = (TextView)
		// mGuessLoadingLayout.findViewById(R.id.loading_textview);
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
		int columnWidth = AndroidUtils.dip2px(this, 48);
		int horizontalSpacing = AndroidUtils.dip2px(this, 10);
		// int padding = AndroidUtils.dip2px(this, 5);
		// mGuessLikeGrid = (GridView) findViewById(R.id.guess_gridview);
		// LayoutParams params = new LayoutParams(list.size()
		// * (columnWidth + horizontalSpacing + padding),
		// LayoutParams.FILL_PARENT);
		LayoutParams params = new LayoutParams(list.size() * columnWidth
				+ list.size() * horizontalSpacing, columnWidth
				+ horizontalSpacing * 4);
		mGuessLikeGrid.setLayoutParams(params);
		mGuessLikeGrid.setColumnWidth(columnWidth);
		mGuessLikeGrid.setHorizontalSpacing(horizontalSpacing);
		mGuessLikeGrid.setNumColumns(list.size());
		// mGuessLikeGrid.setPadding(padding, padding, padding, padding);
		mGuessLikeGrid.setStretchMode(GridView.NO_STRETCH);
		mGuessLikeGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String keyword = list.get(position).appName;
				titleUtil.history.add(keyword);
				Intent intent = new Intent();
				intent.putExtra("search_keyword", keyword);
				intent.setClass(Search_Result_Activity.this,
						Search_Result_Activity.class);
				startActivity(intent);
			}
		});
		GuessLikeAdapter adapter = new GuessLikeAdapter(this, list,
				defaultBitmap_icon);
		mGuessLikeGrid.setAdapter(adapter);
		mGuessLikeLayout.addView(mGuessLikeGrid);

	}

	/**
	 * 下载时动画设置
	 * 
	 * @param map
	 */
	public void onStartDownload(Map<String, Object> map) {
		int iconX = (Integer) map.get("X");
		int iconY = (Integer) map.get("Y")
				- mSoftwareBtn.getHeight()
				+ AndroidUtils.getStatusBarInfo(Search_Result_Activity.this).top;
		Drawable icon = (Drawable) map.get("icon");
		iconAnim.startAnimation(iconX, iconY, icon, mTempIcon, mSoftwareBtn);
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

	private View mGuessLoadingLayout;
	private ProgressBar mGuessLoadingProgressBar;
	private TextView mGuessLoadingTextView;

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
		mGuessLoadingProgressBar = (ProgressBar) mGuessLoadingLayout
				.findViewById(R.id.guessloading_progressbar);
		mGuessLoadingTextView = (TextView) mGuessLoadingLayout
				.findViewById(R.id.guessloading_textview);
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

	/**
	 * 初始化数据列表
	 */
	private void initListData() {
		// FrameLayout mShowResult = (FrameLayout)
		// findViewById(R.id.show_search_result);
		// mResultList = (ScrollListView) findViewById(R.id.result_list);
		// mResultList.setOnScrollTouchListener(this);
		adapter = new SearchResultAdapter(Search_Result_Activity.this, data,
				isRemoteImage);
		mResultList.setAdapter(adapter);
		mResultList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelable("apkItem",
						(ApkItem) adapter.getItem(position));
				intent.putExtras(bundle);
				intent.setClass(Search_Result_Activity.this,
						ApkDetailActivity.class);
				startActivity(intent);
			}
		});
		// mResultList.setOnScrollListener(this);
		mShowResult.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("test");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
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
					AndroidUtils.showToast(Search_Result_Activity.this, rId2);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		titleUtil.unregisterMyReceiver(this);
		super.onDestroy();
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
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			// isLoading = false;
			// AndroidUtils.showToast(this, R.string.last_item);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// System.out.println("firstVisibleItem-------------->" +
		// firstVisibleItem);
		// System.out.println("visibleItemCount==============>" +
		// visibleItemCount);
		// System.out.println("totalItemCount==============>" + totalItemCount);
		// System.out.println("当前请求数据量-------------->" + data.size());
		// System.out.println("请求总数据量sumCount-------------->" + sumCount);
		// System.out.println("是否正在加载＝＝＝＝＝＝＝＝＝＝＝＝＝>" + isLoading);
		if (!isLoading
				&& firstVisibleItem + visibleItemCount > totalItemCount
						- SCROLL_DVALUE) {
			// System.out.println("adapter count+++++++++++++>" +
			// adapter.getCount());
			// adapter.notifyDataSetChanged();
			adapter.addList(data);
			isLoading = true;
			showProgressBar();
			handler.sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);
		} else if (isLoading
				&& firstVisibleItem + visibleItemCount > totalItemCount
						- SCROLL_DVALUE) {
			handler.sendEmptyMessageDelayed(EVENT_LOADING, 300);
			continueLoad = true;
		}
		// if (sumCount == maxCount) {
		// adapter.setCount(maxCount);
		// }
		// if (!isLoading
		// && firstVisibleItem + visibleItemCount > totalItemCount
		// - SCROLL_DVALUE) {
		// isLoading = true;
		// showProgressBar();
		// handler.sendEmptyMessage(EVENT_REQUEST_SEARCH_LIST);
		// }
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		if (mResultList != null && adapter != null) {
			adapter.changeApkStatusByPackageInfo(status, info);
		}
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName,
			int versionCode) {
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
	public void onScrollTouch(int scrollState) {
		switch (scrollState) {
		case ScrollListView.OnScrollTouchListener.SCROLL_BOTTOM:
			// scrollOperation(true);
			break;
		case ScrollListView.OnScrollTouchListener.SCROLL_TOP:
			// scrollOperation(false);
			break;
		}
	}

	private boolean isScrollAnimRunning;

	void scrollOperation(boolean flag) {
		if (flag) {
			if (!isScrollAnimRunning && ll_top.getVisibility() == View.VISIBLE) {
				Animation mTopCollapseAnimation = AnimationUtils.loadAnimation(
						this, R.anim.anim_tab_collapse);
				Animation mBottomCollapseAnimation = AnimationUtils
						.loadAnimation(this, R.anim.anim_bottom_collapse);
				mTopCollapseAnimation
						.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// TODO Auto-generated method stub
								isScrollAnimRunning = false;
								ll_top.setVisibility(View.GONE);
							}
						});
				mBottomCollapseAnimation
						.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// mMainBottomLayout.setVisibility(View.GONE);
							}
						});
				isScrollAnimRunning = true;
				ll_top.startAnimation(mTopCollapseAnimation);
				// mMainBottomLayout.startAnimation(mBottomCollapseAnimation);
			}
		} else {
			if (ll_top.getVisibility() == View.GONE) {
				Animation mTopExpandAnimation = AnimationUtils.loadAnimation(
						this, R.anim.anim_tab_expand);
				Animation mBottomExpandAnimation = AnimationUtils
						.loadAnimation(this, R.anim.anim_bottom_expand);
				ll_top.setVisibility(View.VISIBLE);
				// mMainBottomLayout.setVisibility(View.VISIBLE);
				System.out.println("onanimationstart.......................");
				ll_top.startAnimation(mTopExpandAnimation);
				// mMainBottomLayout.startAnimation(mBottomExpandAnimation);
			}
		}
	}

	@Override
	protected void loadingImage() {
		// TODO Auto-generated method stub
		if(adapter!=null) {
			adapter.setDisplayNotify(isRemoteImage);
		}
	}

	private int locStep;
	@Override
	public void onClick() {
		if(mResultList!=null) {
//			if (!mResultList.isStackFromBottom()) {
//				mResultList.setStackFromBottom(true);
//			}
//			mResultList.setStackFromBottom(false);
			locStep = (int) Math.ceil(mResultList.getFirstVisiblePosition()/AConstDefine.AUTO_SCRLL_TIMES);
			mResultList.post(scrollToTop);
		}
	}
	
	Runnable scrollToTop = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mResultList.getFirstVisiblePosition() > 0) {
				if (mResultList.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mResultList.setSelection(mResultList
							.getFirstVisiblePosition() - 1);
				} else {
					mResultList.setSelection(Math.max(mResultList.getFirstVisiblePosition() - locStep, 0));
				}
				// mAppListView.postDelayed(this, 1);
				mResultList.post(this);
			}
			return;
		}
	};
}
