package com.dongji.market.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.dongji.market.R;
import com.dongji.market.application.AppMarket;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.ADownloadApkItem;
import com.dongji.market.download.ADownloadExpandAdapter;
import com.dongji.market.download.ADownloadService;
import com.dongji.market.download.AIgnoreItem;
import com.dongji.market.download.NetTool;

/**
 * 下载界面
 * 
 * @author quhm
 * 
 */
public class ADownloadActivity extends Activity implements AConstDefine {
	private ExpandableListView exlvDownload;
	private ADownloadExpandAdapter mAdapter;
	private AppMarket mApp;
	private View mLoadingView;
	private MyHandler myHandler;

	private static final int EVENT_START_INIT = 1;
	private static final int EVENT_UPDATE_DATA_LOADED = 3;

	// private int flag_isStart=0;

	// private FlowBroadcastReceiver flowBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		mApp = (AppMarket) getApplication();
		exlvDownload = (ExpandableListView) findViewById(R.id.exlvDownload);
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		myHandler = new MyHandler(mHandlerThread.getLooper());
		registerAllReceiver();
		initLoadingView();

		// flowBroadcastReceiver=new
		// FlowBroadcastReceiver(ADownloadActivity.this);
		// flowBroadcastReceiver.registerMyReceiver();
		
//		((SoftwareManageActivity)getParent()).setMenuSlide(exlvDownload);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	/**
	 * 注册广播
	 */
	private void registerAllReceiver() {
		IntentFilter intentFilter = new IntentFilter(
				BROADCAST_UPDATE_DATA_REFRESH);
		registerReceiver(mUpdateDataReceiver, intentFilter);
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_START_INIT:
				// ADownloadExpandAdapter.installedAppInfos = NetTool
				// .getInstallAppInfo(ADownloadActivity.this);
				ADownloadExpandAdapter.installedAppInfos = NetTool
						.getAllInstallAppInfo(ADownloadActivity.this);
				final List<List<Object>> childData = getChildData();
				final List<String> groupData = getGroupData();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mLoadingView.setVisibility(View.VISIBLE);
						exlvDownload.setVisibility(View.GONE);

						exlvDownload.setChildDivider(getResources()
								.getDrawable(R.drawable.list_divider));

						exlvDownload.setGroupIndicator(null);

						mAdapter = new ADownloadExpandAdapter(
								ADownloadActivity.this, groupData, childData);
						exlvDownload.setAdapter(mAdapter);

						for (int i = 0; i < groupData.size(); i++) {
							exlvDownload.expandGroup(i);
						}

						exlvDownload
								.setOnGroupClickListener(new OnGroupClickListener() {

									@Override
									public boolean onGroupClick(
											ExpandableListView parent, View v,
											int groupPosition, long id) {
										if (mAdapter.getGroupCount() == 4
												&& groupPosition == 0) {
											return false;
										}
										return true;
									}
								});
						mLoadingView.setVisibility(View.GONE);
						exlvDownload.setVisibility(View.VISIBLE);
					}
				});
				break;
			case 2:
				/********* 注释 *********/
				/*
				 * ADownloadExpandAdapter.installedAppInfos=NetTool.
				 * getAllInstallAppInfo(ADownloadActivity.this); final
				 * List<List<Object>> childData2 = getChildData(); final
				 * List<String> groupData2 = getGroupData(); runOnUiThread(new
				 * Runnable() {
				 * 
				 * @Override public void run() { if(mAdapter!=null) {
				 * mAdapter.addData(groupData2, childData2); } for (int i = 0; i
				 * < groupData2.size(); i++) { exlvDownload.expandGroup(i); } }
				 * });
				 */
				mAdapter.checkHasErrorData();
				break;
			case EVENT_UPDATE_DATA_LOADED:
				final List<List<Object>> childData2 = getChildData();
				final List<String> groupData2 = getGroupData();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mAdapter != null) {
							mAdapter.addData(groupData2, childData2);
							for (int i = 0; i < groupData2.size(); i++) {
								exlvDownload.expandGroup(i);
							}
						}
					}
				});
				break;
			}
		}

	}

	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
		myHandler.sendEmptyMessage(EVENT_START_INIT);
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

	private List<String> getGroupData() {
		List<String> groupData = new ArrayList<String>();
		if (null != ADownloadService.downloadingAPKList.apkList
				&& ADownloadService.downloadingAPKList.apkList.size() > 0) {
			groupData.add(getString(R.string.transferapk));
		}
		groupData.add(getString(R.string.updateapk));
		groupData.add(getString(R.string.waitinstallapk));
		groupData.add(getString(R.string.ignoreapk));
		return groupData;

	}

	private List<List<Object>> getChildData() {
		List<List<Object>> childData = new ArrayList<List<Object>>();

		if (null != ADownloadService.downloadingAPKList.apkList
				&& ADownloadService.downloadingAPKList.apkList.size() > 0) {
			List<Object> sub1 = new ArrayList<Object>();
			sub1.addAll(ADownloadService.downloadingAPKList.apkList);
			childData.add(sub1);
		}

		List<Object> sub2 = new ArrayList<Object>();

		sub2.addAll(ADownloadService.updateAPKList.apkList);
		childData.add(sub2);

		List<Object> sub3 = new ArrayList<Object>();
		List<ADownloadApkItem> uninstallList = NetTool
				.getWaitInstallList(ADownloadActivity.this).apkList;
		sub3.addAll(uninstallList);
		childData.add(sub3);

		List<Object> sub4 = new ArrayList<Object>();
		List<AIgnoreItem> ignoreList = NetTool
				.getIgnoreList(ADownloadActivity.this).ignoreAppList;
		System.out.println("adownloadactivity......ignore......."
				+ ignoreList.size());
		sub4.addAll(ignoreList);
		childData.add(sub4);

		return childData;
	}

	@Override
	protected void onResume() {
		if (null != mAdapter) {
			// myHandler.sendEmptyMessage(2);
			/**** kai.zhang **/
			if (!myHandler.hasMessages(2)) {
				myHandler.sendEmptyMessage(2);
			}
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.removeMessage();
			mAdapter.unregisterInstallReceiver();
			mAdapter.unregisterSettingReceiver();
		}
		unregisterReceiver(mUpdateDataReceiver);
		// if(null!=flowBroadcastReceiver){
		// flowBroadcastReceiver.unregisterMyReceiver();
		// }
		showLog("onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return getParent().onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return getParent().onMenuOpened(featureId, menu);
	}

	private void showLog(String msg) {
		Log.i("ADownloadActivity", msg);
	}

	private BroadcastReceiver mUpdateDataReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BROADCAST_UPDATE_DATA_REFRESH.equals(intent.getAction())) {
				if (myHandler != null) {
					myHandler.sendEmptyMessage(EVENT_UPDATE_DATA_LOADED);
				}
			}
		}
	};
}
