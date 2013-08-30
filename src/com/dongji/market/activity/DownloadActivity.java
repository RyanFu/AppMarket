package com.dongji.market.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;

import com.dongji.market.R;
import com.dongji.market.adapter.DownloadAdapter;
import com.dongji.market.application.AppMarket;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadService;
import com.dongji.market.download.DownloadUtils;
import com.umeng.analytics.MobclickAgent;

public class DownloadActivity extends Activity implements DownloadConstDefine {
	private static final int EVENT_REQUEST_DATA = 1; // 请求与下载有关的数据
	private static final int EVENT_REFRESH_DATA = 2; // 刷新显示数据

	private ExpandableListView mExpandableListView;
	private View mLoadingView;
	private DownloadAdapter mAdapter;

	private MyHandler mHandler;

	private AppMarket mApp;

	private static final String PACKAGE_STR = "package:";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		mApp = (AppMarket) getApplication();
		initLoadingView();
		initHandler();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeMessages(EVENT_REFRESH_DATA);
		}
		if (mAdapter != null) {
			mAdapter.unregisterAllReceiver();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_REQUEST_DATA);
	}

	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
	}

	private void initViews(List<String> groupList, List<List<DownloadEntity>> childList) {
		mExpandableListView = (ExpandableListView) findViewById(R.id.exlvDownload);
		mExpandableListView.setChildDivider(getResources().getDrawable(R.drawable.list_divider));
		mExpandableListView.setGroupIndicator(null);
		mAdapter = new DownloadAdapter(this, childList, groupList, mHandler);
		mExpandableListView.setAdapter(mAdapter);
		int count = groupList.size() - 1;
		for (int i = 0; i < count; i++) {
			mExpandableListView.expandGroup(i);
		}
		mExpandableListView.collapseGroup(count);
		mLoadingView.setVisibility(View.GONE);
		mExpandableListView.setVisibility(View.VISIBLE);
		mHandler.sendEmptyMessageDelayed(EVENT_REFRESH_DATA, 1000L);
	}

	private List<List<DownloadEntity>> initData() {
		List<List<DownloadEntity>> adapterData = new ArrayList<List<DownloadEntity>>();
		List<DownloadEntity> installList = new ArrayList<DownloadEntity>();
		List<DownloadEntity> downloadingList = new ArrayList<DownloadEntity>();
		List<DownloadEntity> updatingList = new ArrayList<DownloadEntity>();
		List<DownloadEntity> ignoreList = new ArrayList<DownloadEntity>();
		if (DownloadService.mDownloadService != null) {
			List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
			for (int i = 0; i < downloadList.size(); i++) {
				DownloadEntity entity = downloadList.get(i);
				if (entity.downloadType == TYPE_OF_COMPLETE) {
					installList.add(entity);
				} else if (entity.downloadType == TYPE_OF_DOWNLOAD) {
					downloadingList.add(entity);
				} else if (entity.downloadType == TYPE_OF_UPDATE) {
					DownloadUtils.setInstallDownloadEntity(this, entity);
					updatingList.add(entity);
				} else if (entity.downloadType == TYPE_OF_IGNORE) {
					DownloadUtils.setInstallDownloadEntity(this, entity);
					ignoreList.add(entity);
				}
			}
			if (downloadingList.size() > 0) {
				adapterData.add(downloadingList);
			}
			System.out.println("download size:" + downloadingList.size() + ", " + updatingList.size() + ", " + installList.size());
		}
		adapterData.add(updatingList);
		adapterData.add(installList);
		adapterData.add(ignoreList);
		return adapterData;
	}

	private List<String> initGroupData(boolean hasDownloadData) {
		List<String> list = new ArrayList<String>();
		if (hasDownloadData) {
			list.add(getString(R.string.transferapk));
		}
		list.add(getString(R.string.updateapk));
		list.add(getString(R.string.waitinstallapk));
		list.add(getString(R.string.ignoreapk));
		return list;
	}

	public class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_DATA:
				final List<List<DownloadEntity>> childList = initData();
				boolean hasDownloadData = childList != null ? childList.size() > 3 : false;
				final List<String> groupList = initGroupData(hasDownloadData);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initViews(groupList, childList);
					}
				});
				break;
			case EVENT_REFRESH_DATA:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mAdapter.refreshAdapter();
						mHandler.sendEmptyMessageDelayed(EVENT_REFRESH_DATA, 1000L);
					}
				});
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getParent() == null) {
			return super.onCreateOptionsMenu(menu);
		} else {
			return getParent().onCreateOptionsMenu(menu);
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (getParent() == null) {
			return super.onMenuOpened(featureId, menu);
		} else {
			return getParent().onMenuOpened(featureId, menu);
		}
	}

	/**
	 * 使用3G下载是否提示过用户
	 * 
	 * @return
	 */
	public boolean is3GDownloadPromptUser() {
		return mApp.isIs3GDownloadPrompt();
	}

	/**
	 * 使用3G下载已提示用户
	 */
	public void set3GDownloadPromptUser() {
		mApp.setIs3GDownloadPrompt(true);
	}

	private int locStep;

	void onToolBarClick() {
		if (mExpandableListView != null) {
			locStep = (int) Math.ceil(mExpandableListView.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
			mExpandableListView.post(scrollToTop);
		}
	}

	Runnable scrollToTop = new Runnable() {

		@Override
		public void run() {
			if (mExpandableListView.getFirstVisiblePosition() > 0) {
				if (mExpandableListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mExpandableListView.setSelection(mExpandableListView.getFirstVisiblePosition() - 1);
				} else {
					mExpandableListView.setSelection(Math.max(mExpandableListView.getFirstVisiblePosition() - locStep, 0));
				}
				mExpandableListView.post(this);
			}
			return;
		}
	};
}
