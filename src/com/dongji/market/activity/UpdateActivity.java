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
public class UpdateActivity extends BaseActivity implements
		OnItemClickListener, OnScrollListener,
		ScrollListView.OnScrollTouchListener {
	private static final int EVENT_REQUEST_APPLIST_DATA = 1;
	private static final int EVENT_REQUEST_GAMELIST_DATA = 2;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private static final int EVENT_REQUEST_DOWNLOAD_DATA = 5;
	private static final int EVENT_REFRENSH_DATA = 6;
	private static final int EVENT_REQUEST_NAVIGATION_DATA = 7;

	private static final int SCROLL_DVALUE = 1;

	private static final int TYPE_UPDATE = 1, TYPE_INSTALL = 3,
			TYPE_CHANNEL = 4;

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
	private boolean isRequestDelay;

	private int currentAppPage;
	private int currentGamePage;

	private boolean isFirstResume = true;

	private NavigationInfo navigationInfo;
	private boolean hasAppData = true;
	private boolean hasGameData = true;
	private boolean isLoadChannelData;

	private List<ChannelListInfo> channelList;

	private ChannelListInfo currentAppChannel;
	private ChannelListInfo currentGameChannel;

	private boolean isClearDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_template);
		context = this;
		initData();
		initLoadingView();
		initHandler();
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
		// TODO Auto-generated method stub
		super.onPause();
		// setDefaultImage();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return getParent().onKeyDown(keyCode, event);
	}

	private void initData() {
		dataManager = DataManager.newInstance();
		// Bundle bundle=getIntent().getExtras();
		// if(bundle!=null) {
		// isSingleRow=bundle.getBoolean("isSingleRow");
		// currentType=bundle.getInt("type");
		// navigationInfo=bundle.getParcelable("navigation");
		// }
		currentType = getIntent().getIntExtra("type", 1);
		System.out.println("currenttype...didi..0719......." + currentType);
	}

	private void initAppListView() {
		mAppListView = (ScrollListView) findViewById(R.id.applistview);
		if (isSingleRow) {
			mAppSingleAdapter = new ListSingleTemplateAdapter(this, apps,
					isRemoteImage);
			mAppListView.setAdapter(mAppSingleAdapter);
		} else {
			mAppMultiAdapter = new ListMultiTemplateAdapter(this, apps);
			mAppListView.setAdapter(mAppMultiAdapter);
		}
		if (isSingleRow) {
			mAppListView.setOnItemClickListener(this);
		}
		// mAppListView.setOnScrollListener(this);
		mAppListView.setOnScrollTouchListener(this);
		if (isAppClicked) {
			mAppListView.setVisibility(View.VISIBLE);
			if (mGameListView != null) {
				mGameListView.setVisibility(View.GONE);
			}
		}
		// getParentActivity().setListViewSlide(mAppListView);
	}

	private void initGameListView() {
		mGameListView = (ScrollListView) findViewById(R.id.gamelistview);
		if (isSingleRow) {
			mGameSingleAdapter = new ListSingleTemplateAdapter(this, games,
					isRemoteImage);
			mGameListView.setAdapter(mGameSingleAdapter);
		} else {
			mGameMultiAdapter = new ListMultiTemplateAdapter(this, games);
			mGameListView.setAdapter(mGameMultiAdapter);
		}
		if (isSingleRow) {
			mGameListView.setOnItemClickListener(this);
		}
		// mGameListView.setOnScrollListener(this);
		mGameListView.setOnScrollTouchListener(this);
		if (!isAppClicked) {
			if (mAppListView != null) {
				mAppListView.setVisibility(View.GONE);
			}
			mGameListView.setVisibility(View.VISIBLE);
		}
		// getParentActivity().setListViewSlide(mGameListView);
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
					/*if (currentType != TYPE_CHANNEL) {
						// if(navigationInfo==null) {
						mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION_DATA);
						return false;
						// }
					}*/
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
	 * 数据获取异常处理
	 * 
	 * @param rId
	 */
	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getParentActivity().stopProgressBar();
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

	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
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

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("handlerThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_DOWNLOAD_DATA);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, ApkDetailActivity.class);
		Bundle bundle = new Bundle();
		switch (parent.getId()) {
		case R.id.applistview:
			bundle.putParcelable("apkItem",
					mAppSingleAdapter.getApkItemByPosition(position));
			break;
		case R.id.gamelistview:
			bundle.putParcelable("apkItem",
					mGameSingleAdapter.getApkItemByPosition(position));
			break;
		}
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private List<ApkItem> getApps() throws IOException, JSONException {
		if (currentType == TYPE_CHANNEL) {
			if (channelList == null || channelList.size() == 0) {
				channelList = dataManager.getChannelListData(this);
				if (channelList != null && channelList.size() > 0) {
					isLoadChannelData = true;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							getParentActivity().initBottomView(channelList);
							getParentActivity().performClickOnBottomButton(
									isAppClicked);
						}
					});
					if (isClearDate) {
						dataManager.clearRubbishCacheData(this, channelList);
						DJMarketUtils.writeClearDate(context);
					}
				}
			} else {
			}
		} else {
			// int position = isAppClicked ? 0 : 1;
			// StaticAddress
			// staticAddress=(StaticAddress)navigationInfo.staticAddress[position];
			if (isAppClicked) {
				hasAppData = false;
			} else {
				hasGameData = false;
			}
			return dataManager.getApps(this,
					currentType == TYPE_UPDATE ? DataManager.RECENT_UPDATA_ID
							: DataManager.ESSENTIAL_ID, isAppClicked);
		}
		return null;
	}

	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_APPLIST_DATA:
				try {
					apps = getApps();
					apps = setApkStatus(apps);
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
							if (currentAppPage == 0) {
								currentAppPage = 1;
								initAppListView();
								mLoadingView.setVisibility(View.GONE);
							} else {
								getParentActivity().onProgressBarDone();
								if (isAppClicked) {
									if (mAppListView.getVisibility() == View.GONE) {
										mAppListView
												.setVisibility(View.VISIBLE);
										mLoadingView.setVisibility(View.GONE);
									}
								}
								if (isRequestDelay) {
									addAdapterData();
								}
								currentAppPage++;
								isLoading = false;
							}
						}
					});
				} else {
					isLoading = false;
					if (currentType == TYPE_CHANNEL) {
						if (isLoadChannelData) {
							isLoadChannelData = false;
						} else {
							if (!AndroidUtils.isNetworkAvailable(context)) {
								sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
							} else {
								sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
							}
						}
					} else {
						if (!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						} else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
				}
				break;
			case EVENT_REQUEST_GAMELIST_DATA:
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
								getParentActivity().onProgressBarDone();
								if (!isAppClicked) {
									if (mGameListView.getVisibility() == View.GONE) {
										mGameListView
												.setVisibility(View.VISIBLE);
										mLoadingView.setVisibility(View.GONE);
									}
								}
								if (isRequestDelay) {
									addAdapterData();
									sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
									isRequestDelay = false;
								}
								currentGamePage++;
								isLoading = false;
							}
						}
					});
				} else {
					isLoading = false;
					if (currentType == TYPE_CHANNEL) {
						if (isLoadChannelData) {
							isLoadChannelData = false;
						} else {
							if (!AndroidUtils.isNetworkAvailable(context)) {
								sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
							} else {
								sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
							}
						}
					} else {
						if (!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						} else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
				}
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg,
						R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg,
						R.string.request_data_error_msg2);
				break;
			case EVENT_REQUEST_DOWNLOAD_DATA:
				isClearDate = DJMarketUtils.isExceedDate(context);

				initDownloadAndUpdateData();
				if (currentType == TYPE_CHANNEL) {
					sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
				} else {
					// if(navigationInfo!=null) {
					sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
					// }else {
					// sendEmptyMessage(EVENT_REQUEST_NAVIGATION_DATA);
					// }
				}
				break;
			case EVENT_REFRENSH_DATA:
				refreshData();
				break;
			case EVENT_REQUEST_NAVIGATION_DATA:
				requestNavigationData();
				if (navigationInfo == null) {
					if (!AndroidUtils.isNetworkAvailable(context)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				} else {
					if (isAppClicked) {
						sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
					} else {
						sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
					}
				}
				break;
			}
		}
	}

	@Override
	public boolean isAppClicked() {
		// TODO Auto-generated method stub
		return isAppClicked;
	}

	@Override
	public void onAppClick() {
		if (!isAppClicked) {
			isAppClicked = true;
			getParentActivity().progressBarGone();
			// if (currentType != TYPE_CHANNEL && navigationInfo==null) {
			// mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION_DATA);
			// return;
			// }
			if (currentAppPage == 0 && hasAppData) {
				displayLoading();
				if (currentType == TYPE_CHANNEL) {
					if (currentAppChannel == null) {
						boolean flag = getParentActivity()
								.performClickOnBottomButton(false);
						if (!flag) {
							mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
						}
					} else {
						mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
					}
				} else {
					mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
				}
			} else if (currentAppPage == 0 && !hasAppData) {
				if (mAppListView != null) {
					mAppListView.setVisibility(View.GONE);
				}
				if (mGameListView != null) {
					mGameListView.setVisibility(View.GONE);
				}
				mLoadingProgressBar.setVisibility(View.GONE);
				if (!AndroidUtils.isNetworkAvailable(context)) {
					mLoadingTextView.setText(R.string.no_network_refresh_msg);
				} else {
					mLoadingTextView.setText(R.string.request_data_error_msg);
				}
				mLoadingView.setVisibility(View.VISIBLE);
			} else {
				setDisplayVisible();
			}
		}
	}

	@Override
	public void onGameClick() {
		if (isAppClicked) {
			isAppClicked = false;
			getParentActivity().progressBarGone();
			// if (currentType != TYPE_CHANNEL && navigationInfo==null) {
			// mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION_DATA);
			// return;
			// }
			if (currentGamePage == 0 && hasGameData) {
				displayLoading();
				if (currentGameChannel == null) {
					System.out.println("currentGameChannel is null");
					if (currentType == TYPE_CHANNEL) {
						boolean flag = getParentActivity()
								.performClickOnBottomButton(false);
						if (!flag) {
							mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
						}
					} else {
						mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
					}
				}
			} else if (currentGamePage == 0 && !hasGameData) {
				if (mAppListView != null) {
					mAppListView.setVisibility(View.GONE);
				}
				if (mGameListView != null) {
					mGameListView.setVisibility(View.GONE);
				}
				mLoadingProgressBar.setVisibility(View.GONE);
				if (!AndroidUtils.isNetworkAvailable(context)) {
					mLoadingTextView.setText(R.string.no_network_refresh_msg);
				} else {
					mLoadingTextView.setText(R.string.request_data_error_msg);
				}
				mLoadingView.setVisibility(View.VISIBLE);
			} else {
				setDisplayVisible();
			}
		}
	}

	private void addAdapterRunUiThread(final ListBaseAdapter mAdapter,
			final List<ApkItem> items) {
		mAdapter.addList(items);
		int what = 0;
		if (isAppClicked) {
			what = EVENT_REQUEST_APPLIST_DATA;
		} else {
			what = EVENT_REQUEST_GAMELIST_DATA;
		}
		mHandler.sendEmptyMessage(what);
	}

	private void addAdapterData() {
		if (isAppClicked) {
			if (apps == null || apps.size() == 0) {
				isLoading = false;
				mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
				return;
			}
			apps = setApkStatus(apps);
			if (isSingleRow) {
				addAdapterRunUiThread(mAppSingleAdapter, apps);
			} else {
				addAdapterRunUiThread(mAppMultiAdapter, apps);
			}
			System.out.println("=========apps:" + (apps == null));
			if (apps != null) {
				apps.clear();
			}
		} else {
			if (games == null || games.size() == 0) {
				isLoading = false;
				mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
				return;
			}
			games = setApkStatus(games);
			if (isSingleRow) {
				addAdapterRunUiThread(mGameSingleAdapter, games);
			} else {
				addAdapterRunUiThread(mGameMultiAdapter, games);
			}
			if (games != null) {
				games.clear();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		// System.out.println("isLoding:"+isLoading);
		if (!isLoading
				&& firstVisibleItem + visibleItemCount >= totalItemCount
						- SCROLL_DVALUE) {
			if (isAppClicked) {
				if (!hasAppData) {
					getParentActivity().onProgressBarDone();
					return;
				}
			} else {
				if (!hasGameData) {
					getParentActivity().onProgressBarDone();
					return;
				}
			}
			isLoading = true;
			getParentActivity().showProgressBar();
			addAdapterData();
			getParentActivity().stopProgressBar();
		} else if (isLoading
				&& !isRequestDelay
				&& firstVisibleItem + visibleItemCount >= totalItemCount
						- SCROLL_DVALUE) {
			if (isAppClicked) {
				if (!hasAppData) {
					getParentActivity().onProgressBarDone();
					return;
				}
			} else {
				if (!hasGameData) {
					getParentActivity().onProgressBarDone();
					return;
				}
			}
			isRequestDelay = true;
			getParentActivity().showProgressBar();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onItemClick(ChannelListInfo info) {
		/*
		 * setLoadingVisible(); if(isAppClicked) { if(isSingleRow) {
		 * currentAppChannel=info; currentAppPage=0; // hasAppData =
		 * currentAppChannel.staticAddress.length > 0; //
		 * System.out.println("onitemclick!"
		 * +currentAppChannel.staticAddress.length+", hasAppData:"+hasAppData);
		 * if(mAppSingleAdapter!=null) { listViewFromTop(mAppListView);
		 * mAppSingleAdapter.resetList(); }
		 * mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA); }else {
		 * if(mAppMultiAdapter!=null) { listViewFromTop(mAppListView);
		 * mAppMultiAdapter.resetList();
		 * mHandler.sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA); } } }else {
		 * if(isSingleRow) { currentGameChannel=info; currentGamePage=0; //
		 * hasGameData = currentGameChannel.staticAddress.length > 0; // //
		 * System
		 * .out.println("game length:"+currentGameChannel.staticAddress.length
		 * +", hasGameData:"+hasGameData);
		 * 
		 * if(mGameSingleAdapter!=null) { listViewFromTop(mGameListView);
		 * mGameSingleAdapter.resetList(); }
		 * mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA); }else {
		 * if(mGameMultiAdapter!=null) { listViewFromTop(mGameListView);
		 * mGameMultiAdapter.resetList();
		 * mHandler.sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA); } } }
		 */
	}

	private void setLoadingVisible() {
		getParentActivity().stopProgressBar();
		isLoading = false;
		if (mLoadingView.getVisibility() == View.GONE) {
			setPreLoading();
		}
		if (mAppListView != null
				&& mAppListView.getVisibility() == View.VISIBLE) {
			mAppListView.setVisibility(View.GONE);
		}
		if (mGameListView != null
				&& mGameListView.getVisibility() == View.VISIBLE) {
			mGameListView.setVisibility(View.GONE);
		}
	}

	/**
	 * ListView 滑动到顶部
	 */
	private void listViewFromTop(ListView mListView) {
		if (mListView != null) {
			if (!mListView.isStackFromBottom()) {
				mListView.setStackFromBottom(true);
			}
			mListView.setStackFromBottom(false);
		}
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName,
			int versionCode) {
		if (mAppListView != null && mAppListView.getAdapter() != null) {
			if (isSingleRow) {
				mAppSingleAdapter.changeApkStatusByAppId(isCancel, packageName,
						versionCode);
			} else {
				mAppMultiAdapter.changeApkStatusByAppId(isCancel, packageName,
						versionCode);
			}
		}
		if (mGameListView != null && mGameListView.getAdapter() != null) {
			if (isSingleRow) {
				mGameSingleAdapter.changeApkStatusByAppId(isCancel,
						packageName, versionCode);
			} else {
				mGameMultiAdapter.changeApkStatusByAppId(isCancel, packageName,
						versionCode);
			}
		}
	}

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

	private void notifyListData(final ListBaseAdapter mAdapter) {
		initDownloadAndUpdateData();
		setApkStatus(mAdapter.getItemList());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				System.out.println("update isRemoteImage:" + isRemoteImage);
				mAdapter.setDisplayNotify(isRemoteImage);
			}
		});
	}

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

	private void requestNavigationData() {
		DataManager dataManager = DataManager.newInstance();
		try {
			ArrayList<NavigationInfo> navigationList = dataManager
					.getNavigationList();
			if (navigationList != null && navigationList.size() > 0) {
				getParentActivity().setNavigationList(navigationList);
				System.out.println("-----currentType:"+currentType);
				navigationInfo = navigationList.get(currentType);
			}
		} catch (JSONException e) {
			System.out.println("choiceness request navigation error!");
		}
	}

	@Override
	protected void onUpdateDataDone() {
		refreshData();
	}

	@Override
	public void onScrollTouch(int scrollState) {
		switch (scrollState) {
		case ScrollListView.OnScrollTouchListener.SCROLL_BOTTOM:
			// getParentActivity().scrollOperation(true);
			break;
		case ScrollListView.OnScrollTouchListener.SCROLL_TOP:
			// getParentActivity().scrollOperation(false);
			break;
		}
	}

	@Override
	protected void loadingImage() {
		// TODO Auto-generated method stub
		if (mAppSingleAdapter != null) {
			mAppSingleAdapter.setDisplayNotify(isRemoteImage);
		}
		if (mGameSingleAdapter != null) {
			mGameSingleAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	private int locStep;

	@Override
	public void OnToolBarClick() {
		if (isAppClicked()) {
			if (mAppListView != null) {
				// if (!mAppListView.isStackFromBottom()) {
				// mAppListView.setStackFromBottom(true);
				// }
				// mAppListView.setStackFromBottom(false);
				locStep = (int) Math.ceil(mAppListView
						.getFirstVisiblePosition()
						/ AConstDefine.AUTO_SCRLL_TIMES);
				mAppListView.post(appAutoScroll);
			}
		} else {
			if (mGameListView != null) {
				// if (!mGameListView.isStackFromBottom()) {
				// mGameListView.setStackFromBottom(true);
				// }
				// mGameListView.setStackFromBottom(false);
				locStep = (int) Math.ceil(mGameListView
						.getFirstVisiblePosition()
						/ AConstDefine.AUTO_SCRLL_TIMES);
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
					mAppListView
							.setSelection(Math.max(
									mAppListView.getFirstVisiblePosition()
											- locStep, 0));
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
					mGameListView.setSelection(Math.max(
							mGameListView.getFirstVisiblePosition() - locStep,
							0));
				}
				// mAppListView.postDelayed(this, 1);
				mGameListView.post(this);
			}
			return;
		}
	};
}
