package com.dongji.market.activity;

import java.util.ArrayList;

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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.dongji.market.R;
import com.dongji.market.adapter.UninstallAdapter;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.FileLoadTask;
import com.dongji.market.pojo.InstalledAppInfo;
import com.dongji.market.widget.CustomDialog;
import com.dongji.market.widget.LoginDialog;
import com.dongji.market.widget.ScrollListView;
/**
 * 卸载列表页
 * @author yvon
 *
 */
public class Uninstall_list_Activity extends Activity {

	public static final int FLAG_RESTORE = 1;
	public static final int FLAG_BACKUP = 2;

	private static final int EVENT_REQUEST_SOFTWARE_LIST = 0;
	private static final int EVENT_LOADED = 1;

	private UninstallAdapter adapter;
	private MyHandler mHandler;
	private LoginDialog dialog;
	private CustomDialog restoreDialog;

	private ScrollListView mListView;

	private FileLoadTask task;
	private View mLoadingView;
	private int locStep;

	public static boolean cloudBackupOngoing = false;
	public static boolean cloudRestoreOngoing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		initView();
		initHandler();
		registerAllReceiver();
		startLoad();
	}

	private void initView() {
		mListView = (ScrollListView) findViewById(R.id.list);
		Button mCloudRestore = (Button) findViewById(R.id.cloud_restore);
		Button mCloudBackup = (Button) findViewById(R.id.cloud_backup);
		mCloudRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!DJMarketUtils.isLogin(Uninstall_list_Activity.this)) {
					if (!isFinishing()) {
						showLoginDialog(FLAG_RESTORE);
					}
				} else {
					if (cloudBackupOngoing) {
						DJMarketUtils.showToast(Uninstall_list_Activity.this, R.string.backup_running_prompt);
					} else if (cloudRestoreOngoing) {
						DJMarketUtils.showToast(Uninstall_list_Activity.this, R.string.restore_running_prompt);
					} else {
						if (!isFinishing()) {
							if (restoreDialog == null) {
								restoreDialog = new CustomDialog(Uninstall_list_Activity.this).setIcon(R.drawable.icon).setMessage(R.string.cloud_restore_prompt);
								restoreDialog.setPositiveButton(R.string.local_restore, new OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST);
										intent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, AConstDefine.ACTIVITY_RESTORE);
										sendBroadcast(intent);
										restoreDialog.dismiss();
									}
								}).setNegativeButton(R.string.cloud_restore, new OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST);
										intent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, AConstDefine.ACTIVITY_CLOUD_RESTORE);
										sendBroadcast(intent);
										restoreDialog.dismiss();
									}
								});
								restoreDialog.setTitle(R.string.chooserestoretype);
							}
							if (restoreDialog != null) {
								restoreDialog.show();
							}
						}
					}
				}
			}
		});
		mCloudBackup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!DJMarketUtils.isLogin(Uninstall_list_Activity.this)) {
					if (!isFinishing()) {
						showLoginDialog(FLAG_BACKUP);
					}
				} else {
					if (cloudRestoreOngoing) {
						DJMarketUtils.showToast(Uninstall_list_Activity.this, R.string.restore_running_prompt);
					} else if (cloudBackupOngoing) {
						DJMarketUtils.showToast(Uninstall_list_Activity.this, R.string.backup_running_prompt);
					} else {
						if (!isFinishing()) {
							final CustomDialog backupDialog = new CustomDialog(Uninstall_list_Activity.this).setIcon(R.drawable.icon);
							backupDialog.setPositiveButton(R.string.local_backup, new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST);
									intent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, AConstDefine.ACTIVITY_BACKUP);
									sendBroadcast(intent);
									backupDialog.dismiss();
								}
							}).setNegativeButton(R.string.cloud_backup, new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST);
									intent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, AConstDefine.ACTIVITY_CLOUD_BACKUP);
									sendBroadcast(intent);
									backupDialog.dismiss();
								}
							});
							backupDialog.setTitle(R.string.choosebackuptype);
							if (backupDialog != null) {
								backupDialog.show();
							}
						}
					}
				}
			}
		});
	}

	private void showLoginDialog(int flag) {
		if (dialog == null) {
			dialog = new LoginDialog(this, flag);
		} else if (flag != dialog.getFlag()) {
			dialog.setFlag(flag);
			dialog.refreshContent();
		}
		if (dialog != null) {
			dialog.show();
		}
	}

	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("handler");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_SOFTWARE_LIST:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (adapter == null) {
							adapter = new UninstallAdapter(Uninstall_list_Activity.this, new ArrayList<InstalledAppInfo>());
							mListView.setAdapter(adapter);
							mListView.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									DJMarketUtils.showInstalledAppDetails(Uninstall_list_Activity.this, ((InstalledAppInfo) adapter.getItem(position)).getPkgName());
								}
							});
						}
						task = new FileLoadTask(Uninstall_list_Activity.this, adapter, mHandler);// 本地图片异步加载
						task.execute();
					}
				});
				break;
			case EVENT_LOADED:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mLoadingView.setVisibility(View.GONE);
						mListView.setVisibility(View.VISIBLE);// 显示列表
					}

				});
				break;
			default:
				break;
			}
		}
	}

	private void registerAllReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL);
		intentFilter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE);
		intentFilter.addDataScheme("package");
		registerReceiver(mReceiver, intentFilter);
	}

	private void startLoad() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == FLAG_BACKUP) {
				Intent intent = new Intent(LoginDialog.BROADCAST_ACTION_SHOWBANDRLIST);
				intent.putExtra(LoginDialog.FLAG_ACTIVITY_BANDR, LoginDialog.ACTIVITY_CLOUD_BACKUP);
				sendBroadcast(intent);
			} else if (requestCode == FLAG_RESTORE) {
				Intent intent = new Intent(LoginDialog.BROADCAST_ACTION_SHOWBANDRLIST);
				intent.putExtra(LoginDialog.FLAG_ACTIVITY_BANDR, LoginDialog.ACTIVITY_CLOUD_RESTORE);
				sendBroadcast(intent);
			}
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return getParent().onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return getParent().onMenuOpened(featureId, menu);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	/**
	 * 接收应用安装卸载广播
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName;
			if (AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL.equals(intent.getAction())) {
				if (adapter != null) {
					packageName = DJMarketUtils.convertPackageName(intent.getDataString());
					InstalledAppInfo info = DJMarketUtils.getInstalledAppInfoByPackageName(context, packageName);
					adapter.addAppData(info);
				}
			} else if (AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE.equals(intent.getAction())) {
				if (adapter != null) {
					packageName = DJMarketUtils.convertPackageName(intent.getDataString());
					adapter.removeAppDataByPackageName(packageName);
				}
			}
		}
	};

	void onToolBarClick() {
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
