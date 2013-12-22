package com.dongji.market.activity;

import java.util.ArrayList;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.ListBaseAdapter;
import com.dongji.market.adapter.ThemeListSingleTemplateAdapter;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.helper.TitleUtil.OnToolBarBlankClickListener;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.SubjectInfo;
import com.dongji.market.pojo.SubjectItem;
import com.dongji.market.protocol.DataManager;
import com.umeng.analytics.MobclickAgent;

/**
 * 专题列表页
 * 
 * @author yvon
 * 
 */
public class ThemeListActivity extends PublicActivity implements OnItemClickListener, OnToolBarBlankClickListener {

	private SubjectInfo info;
	private MyHandler mHandler;
	private final static int EVENT_REQUEST_DATA = 1;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;

	private static final int EVENT_REFRENSH_DATA = 7;

	private Context context;

	private boolean isFirstResume;

	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	private ListView mListView;
	private TextView mDetails;

	private TitleUtil titleUtil;

	private List<ApkItem> apps;

	private View mContentView;

	private SubjectInfo subjectInfo = null;

	private DataManager dataManager = null;

	private FrameLayout mHeaderView;

	private ThemeListSingleTemplateAdapter mThemeListSingleTemplateAdapter;

	private int locStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_item_list);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		context = this;
		dataManager = DataManager.newInstance();

		initData();
		initLoadingView();
		initHandler();
	}

	private void initData() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			info = (SubjectInfo) bundle.getSerializable("subjectInfo");
			if (info != null) {
				View mTopView = findViewById(R.id.search_result_top);
				titleUtil = new TitleUtil(this, mTopView, info.title, null, this);
				findViewById(R.id.result_count).setVisibility(View.GONE);
				findViewById(R.id.shawview).setVisibility(View.GONE);
			}
		} else {
			DJMarketUtils.showToast(this, R.string.request_channel_data_error);
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
		if (info != null) {
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
				try {
					subjectInfo = dataManager.getSubjectApk(info.subjectId);
					if (subjectInfo.subjectItems != null && subjectInfo.subjectItems.size() > 0) {
						List<SubjectItem> subjectItem = subjectInfo.subjectItems;
						List<ApkItem> apkItem = new ArrayList<ApkItem>();

						for (int i = 0; i < subjectItem.size(); i++) {
							SubjectItem item = subjectItem.get(i);
							ApkItem apk = new ApkItem();
							apk.appId = item.appId;
							apk.category = item.catpId;
							apk.appName = item.title;
							apk.updateDate = item.inputTime;
							apk.apkUrl = item.downUrl;
							apk.appIconUrl = item.iconUrl;
							apk.apkSize = item.apkSize;
							apk.version = item.apkVerion;
							apk.downloadNum = item.downloadNum;
							apk.comment = item.comment;
							apk.language = item.language;
							apk.packageName = item.packageName;
							apk.versionCode = item.versionCode;

							apkItem.add(apk);
						}

						apps = apkItem;

						if (apps != null && apps.size() > 0) {
							apps = setApkStatus(apps);
							ThemeListActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (mListView == null) {
										initViews();
										mDetails.setText(subjectInfo.contents);
									} else {
										apps = setApkStatus(apps);
										ThemeListSingleTemplateAdapter mAdapter = (ThemeListSingleTemplateAdapter) mListView.getAdapter();
										mAdapter.addList(apps);
										if (apps != null) {
											apps.clear();
										}
										mListView.setVisibility(View.VISIBLE);
										mContentView.setVisibility(View.VISIBLE);
										mLoadingView.setVisibility(View.GONE);
									}
								}
							});
						}

					} else {
						if (!DJMarketUtils.isNetworkAvailable(context)) {
							System.out.println(" network  error ");
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						} else {
							System.out.println("  data error   ");
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
				} catch (JSONException e) {
					System.out.println("jsonexception:" + e);
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
			case EVENT_REFRENSH_DATA:
				refreshData();
				break;
			}
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
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					DJMarketUtils.showToast(context, rId2);
				}
			}
		});
	}

	private void initViews() {
		if (mHeaderView == null) {
			initHeaderView();
		}
		mListView = (ListView) findViewById(R.id.result_list);
		mListView.addHeaderView(mHeaderView, null, false);
		mThemeListSingleTemplateAdapter = new ThemeListSingleTemplateAdapter(context, apps, isRemoteImage);
		mListView.setAdapter(mThemeListSingleTemplateAdapter);
		mListView.setOnItemClickListener(this);
		mLoadingView.setVisibility(View.GONE);
		mContentView = findViewById(R.id.show_search_result);
		mContentView.setVisibility(View.VISIBLE);
	}

	private void initHeaderView() {
		mHeaderView = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.activity_theme_list_head, null);
		mDetails = (TextView) mHeaderView.findViewById(R.id.details);
	}

	private void refreshData() {
		if (mListView != null && mListView.getAdapter() != null) {
			notifyListData(mThemeListSingleTemplateAdapter);
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
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
		isFirstResume = true;
		if (titleUtil != null) {
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
			mThemeListSingleTemplateAdapter.changeApkStatusByPackageInfo(status, info);
		}
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		if (mListView != null && mListView.getAdapter() != null) {
			mThemeListSingleTemplateAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
		}
	}

	@Override
	protected void onUpdateDataDone() {
		if (mListView != null && mListView.getAdapter() != null) {
			setApkStatus(mThemeListSingleTemplateAdapter.getItemList());
			mThemeListSingleTemplateAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	protected void loadingImage() {
		if (mListView != null && mListView.getAdapter() != null) {
			mThemeListSingleTemplateAdapter.setDisplayNotify(isRemoteImage);
		}
	}

	@Override
	public void onClick() {
		listViewFromTop(mListView);
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
