package com.dongji.market.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.application.AppMarket;
import com.dongji.market.cache.FileService;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.InstalledAppInfo;

public class ADownloadExpandAdapter extends BaseExpandableListAdapter implements AConstDefine {

	private Context context;
	private List<String> groupData;
	private List<List<Object>> childData;
	private MyInstallReceiver myInstallReceiver;
	private MyDownloadReceiver myDownloadReceiver;
	private Bitmap mDefaultBitmap;

	public static List<InstalledAppInfo> installedAppInfos;

	public ADownloadExpandAdapter(Context context, List<String> groupData, List<List<Object>> childData) {
		this.context = context;
		this.groupData = groupData;
		this.childData = childData;

		/*
		 * int count = 0; if (childData.size() == 3) { count =
		 * childData.get(1).size() - installedAppInfos.size(); for (int i = 0; i
		 * < count; i++) { childData.get(0).remove(i);
		 * ADownloadService.updateAPKList.apkList.remove(i); i--; count--; } }
		 * else { count = childData.get(0).size() - installedAppInfos.size();
		 * for (int i = 0; i < count; i++) { childData.get(0).remove(i);
		 * ADownloadService.updateAPKList.apkList.remove(i); i--; count--; } }
		 */

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_SYS_ACTION_APPINSTALL);
		intentFilter.addAction(BROADCAST_SYS_ACTION_APPREMOVE);
		intentFilter.addDataScheme("package");
		myInstallReceiver = new MyInstallReceiver();
		context.registerReceiver(myInstallReceiver, intentFilter);

		intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_DEL_DOWNLOADED_APK);
		intentFilter.addAction(BROADCAST_ACTION_UPDATE);
		intentFilter.addAction(BROADCAST_SYS_ACTION_CONNECTCHANGE);
		intentFilter.addAction(BROADCAST_ACTION_DOWNLOAD);
		intentFilter.addAction(BROADCAST_ACTION_DIALOG_LIMITFLOWCHANGE);
		intentFilter.addAction(BROADCAST_ACTION_CLOUDRESTORE);
		intentFilter.addAction(BROADCAST_ACTION_DOWNLOADADAPTER);
		myDownloadReceiver = new MyDownloadReceiver();
		context.registerReceiver(myDownloadReceiver, intentFilter);

		try {
			mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}

		getChildrenCount(0);

		HandlerThread mHandlerThread = new HandlerThread("");
		mHandlerThread.start();
		classHandler = new MyHandler(mHandlerThread.getLooper());
		sendRefreshMessage(1);
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFERENSH_SCREEN:
				Bundle bundle = msg.getData();
				String apkSaveName = bundle.getString(HANDLER_CLEARPACKAGE);

				/******* kai.zhang **********/

				if (null != apkSaveName) {
					String[] tempString = apkSaveName.split("_");

					NetTool.deleteFileByApkSaveName(apkSaveName);
					if (tempString.length == 2) {
						ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(context);
						aDownloadApkDBHelper.deleteDownloadByPAndV(tempString[0], Integer.valueOf(tempString[1]));
					}

					if (tempString.length == 2) {
						List<ApkItem> apkItems = ((AppMarket) context.getApplicationContext()).getUpdateList();
						if (null != apkItems) {
							for (int i = 0; i < apkItems.size(); i++) {
								if (apkItems.get(i).packageName.equals(tempString[0])) {
									int j = 0, size = ADownloadService.updateAPKList.apkList.size();
									for (; j < size; j++) {
										// 判断删除的应用是否存在于更新队列中
										if (ADownloadService.updateAPKList.apkList.get(j).apkPackageName.equals(tempString[0])) {
											break;
										}
									}
									if (j == size) {
										ADownloadApkItem aDownloadApkItem = new ADownloadApkItem(apkItems.get(i), STATUS_OF_UPDATE);
										ADownloadService.updateAPKList.apkList.add(aDownloadApkItem);
										if (groupData.size() == 3) {
											if (childData.get(0).get(0) instanceof ADownloadApkItem) {
												childData.get(0).add(aDownloadApkItem);
											} else {
												childData.get(0).remove(0);
												childData.get(0).add(aDownloadApkItem);
											}
										} else {
											if (childData.get(1).get(0) instanceof ADownloadApkItem) {
												childData.get(1).add(aDownloadApkItem);
											} else {
												childData.get(1).remove(0);
												childData.get(1).add(aDownloadApkItem);
											}
										}
										context.sendBroadcast(new Intent(BROADCAST_ACTION_UPDATECOUNT));
										break;
									}
								}
							}
						}
					}
				}

				if (groupData.size() == 4) {
					for (int i = 0; i < childData.get(2).size(); i++) {
						if (childData.get(2).get(i) instanceof ADownloadApkItem) {
							ADownloadApkItem item = (ADownloadApkItem) childData.get(2).get(i);
							if (apkSaveName.equals(item.apkPackageName + "_" + item.apkVersionCode)) {
								childData.get(2).remove(i);
								break;
							}
						}
					}
				} else {
					for (int i = 0; i < childData.get(1).size(); i++) {
						if (childData.get(1).get(i) instanceof ADownloadApkItem) {
							ADownloadApkItem item = (ADownloadApkItem) childData.get(1).get(i);
							if (apkSaveName.equals(item.apkPackageName + "_" + item.apkVersionCode)) {
								childData.get(1).remove(item);
								break;
							}
						}
					}
				}
				sendRefreshMessage(1);
				// notifyDataSetChanged();
				break;
			case REFERENSH_PROGRESS:
				boolean temp1 = false;
				boolean temp2 = false;
				if (groupData.size() == 4) {
					int i;
					for (i = 0; i < childData.get(0).size(); i++) {
						ADownloadApkItem item = (ADownloadApkItem) childData.get(0).get(i);
						if (item.apkStatus == STATUS_OF_DOWNLOADCOMPLETE || item.apkStatus == STATUS_OF_UPDATECOMPLETE || item.apkStatus == STATUS_OF_CANCEL) {
							boolean flag = childData.get(0).remove(item);
							i--;
							if (flag && childData.size() > 2 && (item.apkStatus == STATUS_OF_DOWNLOADCOMPLETE || item.apkStatus == STATUS_OF_UPDATECOMPLETE)) {
								childData.get(2).add(item);
							}
							if (childData.get(0).size() == 0) {
								groupData.remove(0);
								childData.remove(0);
								break;
							}
						} else if (item.apkStatus == STATUS_OF_DOWNLOADING || item.apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
							temp1 = true;
						}
					}
					int position = childData.size() > 2 ? 1 : 0;
					int position2 = position == 1 ? 2 : 1;
					if (childData.get(position).get(0) instanceof ADownloadApkItem && childData.get(position).size() > 0) {
						int j = 0;
						for (j = 0; j < childData.get(position).size(); j++) {
							ADownloadApkItem item = (ADownloadApkItem) childData.get(position).get(j);
							if (item.apkStatus == STATUS_OF_DOWNLOADCOMPLETE || item.apkStatus == STATUS_OF_UPDATECOMPLETE) {
								boolean flag = childData.get(position).remove(item);
								j--;
								if (flag) {
									System.out.println("---------------------add install:" + item.apkName);
									childData.get(position2).add(item);
								}
							} else if (item.apkStatus == STATUS_OF_UPDATEING || item.apkStatus == STATUS_OF_PREPAREUPDATE) {
								temp2 = true;
							}
						}
					}
				} else {
					if (childData.get(0).size() > 0 && childData.get(0).size() > 0) {
						if (childData.get(0).get(0) instanceof ADownloadApkItem && childData.get(0).size() > 0) {
							for (int i = 0; i < childData.get(0).size(); i++) {
								ADownloadApkItem item = (ADownloadApkItem) childData.get(0).get(i);
								if (item.apkStatus == STATUS_OF_UPDATECOMPLETE) {
									boolean flag = childData.get(0).remove(item);
									if (flag) {
										i--;
										System.out.println("================> add install:" + item.apkName);
										childData.get(1).add(item);
									}
								} else if (item.apkStatus == STATUS_OF_UPDATEING || item.apkStatus == STATUS_OF_PREPAREUPDATE) {
									temp2 = true;
								}
							}
						}
					}
				}
				if (temp1 || temp2) {
					// System.out.println("handle refresh!");
					sendEmptyMessageDelayed(REFERENSH_PROGRESS, 800);
				}
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
				break;
			}
		};
	}

	private MyHandler classHandler = null;

	private class MyInstallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = intent.getDataString();
			packageName = DJMarketUtils.convertPackageName(packageName);
			if (intent.getAction().equals(BROADCAST_SYS_ACTION_APPINSTALL)) {
				InstalledAppInfo installInfo = DJMarketUtils.getInstalledAppInfoByPackageName(context, packageName);
				if (installInfo != null) {
					installedAppInfos.add(installInfo);
				}
				installedAppInfos.add(installInfo);
				ADownloadApkItem aDownloadApkItem;
				ADownloadApkDBHelper db = new ADownloadApkDBHelper(context);
				for (int i = 0; i < groupData.size(); i++) {
					if (groupData.get(i).equals(context.getString(R.string.waitinstallapk)) || groupData.get(i).equals(context.getString(R.string.updateapk))) {
						for (int j = 0; j < childData.get(i).size(); j++) {
							if (childData.get(i).get(j) instanceof ADownloadApkItem) {
								aDownloadApkItem = (ADownloadApkItem) childData.get(i).get(j);
								if (packageName.equals(aDownloadApkItem.apkPackageName)) {
									db.deleteDownloadByPAndV(aDownloadApkItem.apkPackageName, aDownloadApkItem.apkVersionCode);
									for (int k = 0; k < ADownloadService.updateAPKList.apkList.size(); k++) {
										if (ADownloadService.updateAPKList.apkList.get(k).apkPackageName.equals(aDownloadApkItem.apkPackageName) && ADownloadService.updateAPKList.apkList.get(k).apkVersionCode == aDownloadApkItem.apkVersionCode) {
											ADownloadService.updateAPKList.apkList.remove(k);
											break;
										}
									}
									childData.get(i).remove(aDownloadApkItem);
									// ADownloadService.
									if (j < 0) {
										j--;
									}
								}
							}
						}
					}
				}
				sendRefreshMessage(1);
			} else if (intent.getAction().equals(BROADCAST_SYS_ACTION_APPREMOVE)) {
				if (installedAppInfos != null) {
					for (int i = 0; i < installedAppInfos.size(); i++) {
						installedAppInfos.remove(i);
					}
				}
				ADownloadApkItem aDownloadApkItem;
				if (groupData.size() == 3) {
					if (childData.get(0).get(0) instanceof ADownloadApkItem) {
						for (int i = 0; i < childData.get(0).size(); i++) {
							aDownloadApkItem = (ADownloadApkItem) childData.get(0).get(i);
							if (aDownloadApkItem.apkPackageName.equals(packageName)) {
								System.out.println("Adownloadexpandadapter.....移除了。。。。。");
								ADownloadService.updateAPKList.apkList.remove(aDownloadApkItem);
								childData.get(0).remove(aDownloadApkItem);
							}
						}
					}
				} else {
					if (childData.get(1).get(0) instanceof ADownloadApkItem) {
						for (int i = 0; i < childData.get(1).size(); i++) {
							aDownloadApkItem = (ADownloadApkItem) childData.get(1).get(i);
							if (aDownloadApkItem.apkPackageName.equals(packageName)) {
								ADownloadService.updateAPKList.apkList.remove(aDownloadApkItem);
								childData.get(1).remove(aDownloadApkItem);
							}
						}
					}
				}
				sendRefreshMessage(1);
			}
		}
	}

	private class MyDownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_DEL_DOWNLOADED_APK)) {
				if (groupData.size() == 4) {
					childData.get(2).clear();
					childData.get(2).add(CHILDISNULL);
				} else {
					childData.get(1).clear();
					childData.get(1).add(CHILDISNULL);
				}
				NetTool.cancelNotification(context, FLAG_NOTIFICATION_WAITINGINSTALL);
				sendRefreshMessage(1);
			} else if (intent.getAction().equals(BROADCAST_ACTION_UPDATE)) {
				sendRefreshMessage(1);
			} else if (intent.getAction().equals(BROADCAST_SYS_ACTION_CONNECTCHANGE)) {
				sendRefreshMessage(1);
			} else if (intent.getAction().equals(BROADCAST_ACTION_DIALOG_LIMITFLOWCHANGE)) {
				sendRefreshMessage(2);
			} else if (intent.getAction().equals(BROADCAST_ACTION_CLOUDRESTORE)) {
				if (groupData.size() == 3) {
					groupData.add(0, context.getString(R.string.transferapk));
					List<Object> sub1 = new ArrayList<Object>();
					sub1.addAll(ADownloadService.downloadingAPKList.apkList);
					childData.add(0, sub1);
				} else {
					childData.get(0).clear();
					childData.get(0).addAll(ADownloadService.downloadingAPKList.apkList);
				}

				sendRefreshMessage(1);
			} else if (intent.getAction().equals(BROADCAST_ACTION_DOWNLOADADAPTER)) {
				Bundle bundle = intent.getExtras();
				if (null != bundle) {
					ADownloadApkItem aDownloadApkItem = bundle.getParcelable(APKDOWNLOADITEM);
					onReceiveAddUpdate(context, aDownloadApkItem);
				}
			} else if (intent.getAction().equals(BROADCAST_ACTION_DOWNLOAD)) {
				// String apkSaveName = intent
				// .getStringExtra(BROADCAST_CANCELINSTALL);
				/****** 注释 *********/
				/*
				 * if (null != apkSaveName) { String[] tempString =
				 * apkSaveName.split("_"); if (tempString.length == 2) {
				 * List<ApkItem> apkItems = ((AppMarket) context
				 * .getApplicationContext()).getUpdateList(); if (null !=
				 * apkItems) { for (int i = 0; i < apkItems.size(); i++) { if
				 * (apkItems.get(i).packageName .equals(tempString[0])) { int j
				 * = 0, size = ADownloadService.updateAPKList.apkList .size();
				 * for (; j < size; j++) { if
				 * (ADownloadService.updateAPKList.apkList
				 * .get(j).apkPackageName .equals(tempString[0])) { break; } }
				 * if (j == size) { ADownloadApkItem aDownloadApkItem = new
				 * ADownloadApkItem( apkItems.get(i), STATUS_OF_UPDATE);
				 * ADownloadService.updateAPKList.apkList
				 * .add(aDownloadApkItem); if (groupData.size() == 2) { if
				 * (childData.get(0).get(0) instanceof ADownloadApkItem) {
				 * childData.get(0).add( aDownloadApkItem); } else {
				 * childData.get(0).remove(0); childData.get(0).add(
				 * aDownloadApkItem); } } else { if (childData.get(1).get(0)
				 * instanceof ADownloadApkItem) { childData.get(1).add(
				 * aDownloadApkItem); } else { childData.get(1).remove(0);
				 * childData.get(1).add( aDownloadApkItem); } }
				 * context.sendBroadcast(new Intent(
				 * BROADCAST_ACTION_UPDATECOUNT)); NetTool.setNotification(
				 * context, FLAG_NOTIFICATION_UPDATE, ADownloadService
				 * .getUpdateCountByStatus(STATUS_OF_UPDATE));
				 * sendRefreshMessage(1); break; } } } } } }
				 */
			}
		}
	}

	private void onReceiveAddUpdate(Context context, ADownloadApkItem aDownloadApkItem) {
		if (groupData.size() == 3) {
			if (childData.get(0).get(0) instanceof ADownloadApkItem) {
				ADownloadApkItem tempADownloadApkItem;
				int i = 0;
				for (; i < childData.get(0).size(); i++) {
					tempADownloadApkItem = (ADownloadApkItem) childData.get(0).get(i);
					if (tempADownloadApkItem.apkPackageName.equals(aDownloadApkItem.apkPackageName)) {
						break;
					}
				}
				if (i == childData.get(0).size()) {
					childData.get(0).add(aDownloadApkItem);
				}
			}
		} else {
			if (childData.get(1).get(0) instanceof ADownloadApkItem) {
				ADownloadApkItem tempADownloadApkItem;
				int i = 0;
				for (; i < childData.get(1).size(); i++) {
					tempADownloadApkItem = (ADownloadApkItem) childData.get(1).get(i);
					if (tempADownloadApkItem.apkPackageName.equals(aDownloadApkItem.apkPackageName)) {
						break;
					}
				}
				if (i == childData.get(1).size()) {
					childData.get(1).add(aDownloadApkItem);
				}
			}
		}
	}

	public void unregisterInstallReceiver() {
		if (myInstallReceiver != null) {
			context.unregisterReceiver(myInstallReceiver);
		}
	}

	public void unregisterSettingReceiver() {
		if (myDownloadReceiver != null) {
			context.unregisterReceiver(myDownloadReceiver);
		}
	}

	public void checkHasErrorData() {
		List<AErrorApk> aErrorApks = NetTool.checkErrorData(context);
		if (aErrorApks.size() > 0 && null != aErrorApks.get(0) && aErrorApks.get(0).apkPackageName.equals("null")) {
			deleteAllErrorData();
		} else {
			int tempStatus;
			String tempApkPackageName;
			int tempApkVersionCode;
			for (int i = 0; i < aErrorApks.size(); i++) {
				tempApkPackageName = aErrorApks.get(i).apkPackageName;
				tempApkVersionCode = aErrorApks.get(i).apkVersionCode;
				tempStatus = aErrorApks.get(i).apkStatus;

				if (tempStatus == STATUS_OF_DOWNLOADING || tempStatus == STATUS_OF_PAUSE || tempStatus == STATUS_OF_PAUSE_BYHAND) {
					deleteErrorData(0, tempApkPackageName, tempApkVersionCode);
				} else if (tempStatus == STATUS_OF_UPDATEING || tempStatus == STATUS_OF_PAUSEUPDATE || tempStatus == STATUS_OF_PAUSEUPDATE_BYHAND) {
					if (groupData.size() == 4) {
						deleteErrorData(1, tempApkPackageName, tempApkVersionCode);
					} else {
						deleteErrorData(0, tempApkPackageName, tempApkVersionCode);
					}
				} else if (tempStatus == STATUS_OF_DOWNLOADCOMPLETE || tempStatus == STATUS_OF_UPDATECOMPLETE) {
					if (groupData.size() == 4) {
						deleteErrorData(2, tempApkPackageName, tempApkVersionCode);
					} else {
						deleteErrorData(1, tempApkPackageName, tempApkVersionCode);
					}
				}
			}
		}

		if (groupData.size() == 4 && childData.get(0).size() == 0) {
			groupData.remove(0);
			childData.remove(0);
		}

		sendRefreshMessage(1);
	}

	/**
	 * 
	 * @param flag
	 * @param apkPackageName
	 * @param apkVersionCode
	 */
	private void deleteErrorData(int flag, String apkPackageName, int apkVersionCode) {
		ADownloadApkItem aDownloadApkItem;
		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(context);

		for (int i = 0; i < childData.get(flag).size(); i++) {
			if (childData.get(flag).get(i) instanceof ADownloadApkItem) {
				aDownloadApkItem = (ADownloadApkItem) childData.get(flag).get(i);
				if (apkPackageName.equals(aDownloadApkItem.apkPackageName) && apkVersionCode == aDownloadApkItem.apkVersionCode && aDownloadApkItem.apkDownloadSize > 0) {
					childData.get(flag).remove(aDownloadApkItem);
					aDownloadApkDBHelper.deleteDownloadByPAndV(apkPackageName, apkVersionCode);
					// // TODO 暂时先都移除，以后再做判断
					ADownloadService.downloadingAPKList.apkList.remove(aDownloadApkItem);
					ADownloadService.updateAPKList.apkList.remove(aDownloadApkItem);
					Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
					intent.putExtra(BROADCAST_APKISDELETE, apkPackageName + "_" + apkVersionCode);
					context.sendBroadcast(intent);
					i--;
					break;
				}
			}
		}
	}

	private void deleteAllErrorData() {

		ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(context);
		ADownloadApkItem aDownloadApkItem;
		int flag = 0;
		for (int i = 0; i < groupData.size(); i++) {
			for (int j = 0; j < childData.get(i).size(); j++) {
				if (childData.get(i).get(j) instanceof ADownloadApkItem) {
					aDownloadApkItem = (ADownloadApkItem) childData.get(i).get(j);
					if (aDownloadApkItem.apkDownloadSize != 0) {
						childData.get(i).remove(aDownloadApkItem);
						// TODO 暂时先都移除，以后再做判断
						ADownloadService.downloadingAPKList.apkList.remove(aDownloadApkItem);
						ADownloadService.updateAPKList.apkList.remove(aDownloadApkItem);
						j--;
						aDownloadApkDBHelper.deleteDownloadByPAndV(aDownloadApkItem.apkPackageName, aDownloadApkItem.apkVersionCode);
						flag = 1;
					}
				}
			}
		}
		if (flag == 1) {
			Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
			intent.putExtra(BROADCAST_APKLISTISNULL, true);
			context.sendBroadcast(intent);
		}

	}

	public synchronized void addData(List<String> groupData, List<List<Object>> childData) {
		this.groupData = groupData;
		this.childData = childData;
		getChildrenCount(0);
		System.out.println("===========child size:" + childData.get(0).size());
		sendRefreshMessage(1);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childData.get(groupPosition).get(childPosition);
		// return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		// installedAppInfos = NetTool.getInstallAppInfo(context);
		ChildViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_list_download, null);
			holder = new ChildViewHolder();
			holder.mImageView = (ImageView) convertView.findViewById(R.id.iconImageview);
			holder.mAppNameTextView = (TextView) convertView.findViewById(R.id.appnametextview);
			holder.mAppVersionTextView = (TextView) convertView.findViewById(R.id.appversiontextview);
			holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_horizontal);
			holder.mCenterTextView = (TextView) convertView.findViewById(R.id.centertextview);
			holder.mBottomTextView = (TextView) convertView.findViewById(R.id.bottomtextview);
			holder.mFirstButton = (Button) convertView.findViewById(R.id.firstButton);
			holder.mSecondButton = (Button) convertView.findViewById(R.id.secondButton);
			holder.mLongButton = (Button) convertView.findViewById(R.id.longButton);
			holder.mEmptyTextView = (TextView) convertView.findViewById(R.id.emptytextview);
			holder.mContentLayout = convertView.findViewById(R.id.contentlayout);
			convertView.setTag(holder);
		} else
			holder = (ChildViewHolder) convertView.getTag();

		if (groupData.size() == 4) {
			if (groupPosition == 0) {
				fillTransferChildView(holder, groupPosition, childPosition);
			} else if (groupPosition == 1) {
				fillUpdateChildView(holder, groupPosition, childPosition);
			} else if (groupPosition == 2) {
				fillInstallChildView(holder, groupPosition, childPosition);
			} else if (groupPosition == 3) {
				fillIgnoreChildView(holder, groupPosition, childPosition);
			}
		} else if (groupData.size() == 3) {
			if (groupPosition == 0) {
				fillUpdateChildView(holder, groupPosition, childPosition);
			} else if (groupPosition == 1) {
				fillInstallChildView(holder, groupPosition, childPosition);
			} else if (groupPosition == 2) {
				fillIgnoreChildView(holder, groupPosition, childPosition);
			}
		}
		if (groupPosition < childData.size() && childPosition < childData.get(groupPosition).size()) {
			convertView.setBackgroundResource(R.drawable.android_listselector);
			if (childData.get(groupPosition).get(childPosition) instanceof ADownloadApkItem) {
				ApkItem apkItem = new ApkItem((ADownloadApkItem) childData.get(groupPosition).get(childPosition));
				convertView.setOnClickListener(new onMyItemClickListener(apkItem));
			} else {
				convertView.setOnClickListener(null);
			}
		}
		return convertView;
	}

	private class onMyItemClickListener implements OnClickListener {
		private ApkItem apkItem;

		public onMyItemClickListener(ApkItem apkItem) {
			this.apkItem = apkItem;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, ApkDetailActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable("apkItem", apkItem);
			intent.putExtras(bundle);
			context.startActivity(intent);
		}
	}

	private static final class ChildViewHolder {
		ImageView mImageView;
		TextView mAppNameTextView, mAppVersionTextView, mCenterTextView, mBottomTextView, mEmptyTextView;
		ProgressBar mProgressBar;
		Button mFirstButton, mSecondButton, mLongButton;
		View mContentLayout;
	}

	private void fillTransferChildView(ChildViewHolder holder, int groupPosition, int childPosition) {

		if (childPosition >= childData.get(groupPosition).size()) {
			return;
		}

		ADownloadApkItem aDownloadApkItem = (ADownloadApkItem) childData.get(groupPosition).get(childPosition);
		holder.mAppNameTextView.setText(aDownloadApkItem.apkName);
		holder.mEmptyTextView.setVisibility(View.GONE);
		holder.mContentLayout.setVisibility(View.VISIBLE);
		holder.mAppVersionTextView.setVisibility(View.VISIBLE);
		holder.mCenterTextView.setVisibility(View.GONE);
		holder.mLongButton.setVisibility(View.GONE);
		holder.mAppVersionTextView.setText("V" + aDownloadApkItem.apkVersion);
		holder.mCenterTextView.setText(context.getString(R.string.paused));

		holder.mProgressBar.setMax(100);

		int downloadSize = aDownloadApkItem.apkDownloadSize;
		int totalSize = aDownloadApkItem.apkTotalSize;

		String currentDownloadSize = (NetTool.formatString(downloadSize / 1048576.0) + "M");
		String currentTotalSize = NetTool.formatString(totalSize / 1048576.0) + "M";
		holder.mBottomTextView.setText(currentDownloadSize + "/" + currentTotalSize);

		holder.mFirstButton.setText(getButtonText(holder.mFirstButton, aDownloadApkItem.apkStatus, 1));
		holder.mFirstButton.setOnClickListener(new DownloadButtonClick(childPosition));
		holder.mSecondButton.setText(getButtonText(holder.mSecondButton, aDownloadApkItem.apkStatus, 2));
		holder.mSecondButton.setOnClickListener(new DownloadButtonClick(childPosition));
		holder.mFirstButton.setVisibility(View.VISIBLE);
		if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREDOWNLOAD) {
			holder.mProgressBar.setProgress(0);
			holder.mProgressBar.setVisibility(View.VISIBLE);
			holder.mCenterTextView.setVisibility(View.GONE);
			holder.mSecondButton.setVisibility(View.GONE);
		} else if (aDownloadApkItem.apkStatus == STATUS_OF_DOWNLOADING) {
			if (totalSize > 0 && downloadSize > 0) {
				double progress = downloadSize * 100.0 / totalSize;
				holder.mProgressBar.setProgress((int) progress);
			} /*
			 * else { holder.mProgressBar.setVisibility(View.GONE); }
			 */
			holder.mProgressBar.setVisibility(View.VISIBLE);
			holder.mCenterTextView.setVisibility(View.GONE);
			holder.mSecondButton.setVisibility(View.GONE);
		} else if (aDownloadApkItem.apkStatus == STATUS_OF_PAUSE_BYHAND || aDownloadApkItem.apkStatus == STATUS_OF_PAUSE) {
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mCenterTextView.setVisibility(View.VISIBLE);
			holder.mSecondButton.setVisibility(View.VISIBLE);
			holder.mFirstButton.setVisibility(View.VISIBLE);
		}

		try {
			FileService.getBitmap(aDownloadApkItem.apkIconUrl, holder.mImageView, mDefaultBitmap, 0);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}

	private void fillUpdateChildView(ChildViewHolder holder, int groupPosition, int childPosition) {
		if (childPosition < childData.get(groupPosition).size()) {
			if (childData.get(groupPosition).get(childPosition).equals(CHILDISNULL)) {
				holder.mEmptyTextView.setText(R.string.current_not_update_app);
				holder.mEmptyTextView.setVisibility(View.VISIBLE);
				holder.mContentLayout.setVisibility(View.GONE);
			} else {
				ADownloadApkItem aDownloadApkItem = (ADownloadApkItem) childData.get(groupPosition).get(childPosition);
				InstalledAppInfo installedAppInfo = NetTool.getInstallAppInfoByPackage(context, installedAppInfos, aDownloadApkItem.apkPackageName);

				if (null != installedAppInfo) {
					holder.mContentLayout.setVisibility(View.VISIBLE);

					holder.mAppNameTextView.setText(aDownloadApkItem.apkName);
					holder.mEmptyTextView.setVisibility(View.GONE);
					holder.mCenterTextView.setText(context.getString(R.string.paused));
					// InstalledAppInfo installedAppInfo = installedAppInfos
					// .get(childPosition);

					holder.mAppVersionTextView.setVisibility(View.VISIBLE);
					holder.mProgressBar.setVisibility(View.GONE);

					holder.mProgressBar.setMax(100);

					holder.mAppVersionTextView.setText(installedAppInfo.getVersion() + "/" + installedAppInfo.getSize());
					holder.mBottomTextView.setText(aDownloadApkItem.apkVersion + "/" + NetTool.sizeFormat((int) aDownloadApkItem.apkTotalSize));
					holder.mFirstButton.setText(getButtonText(holder.mFirstButton, aDownloadApkItem.apkStatus, 1));
					holder.mFirstButton.setOnClickListener(new UpdateButtonClick(childPosition));
					holder.mSecondButton.setText(getButtonText(holder.mSecondButton, aDownloadApkItem.apkStatus, 2));
					holder.mSecondButton.setOnClickListener(new UpdateButtonClick(childPosition));
					holder.mLongButton.setText(context.getString(R.string.cancle_ignore));
					holder.mLongButton.setBackgroundResource(R.drawable.cancel_selector);
					holder.mLongButton.setTextColor(Color.parseColor("#7f5100"));
					holder.mLongButton.setOnClickListener(new UpdateButtonClick(childPosition));

					if (aDownloadApkItem.apkStatus == STATUS_OF_UPDATE) {
						holder.mProgressBar.setVisibility(View.GONE);
						holder.mFirstButton.setVisibility(View.VISIBLE);
						holder.mSecondButton.setVisibility(View.VISIBLE);
						holder.mLongButton.setVisibility(View.GONE);
						holder.mCenterTextView.setVisibility(View.GONE);
					} else if (aDownloadApkItem.apkStatus == STATUS_OF_PREPAREUPDATE || aDownloadApkItem.apkStatus == STATUS_OF_UPDATEING) {
						int downloadSize = aDownloadApkItem.apkDownloadSize;
						int totalSize = aDownloadApkItem.apkTotalSize;
						double progress = 0;
						if (totalSize > 0 && downloadSize > 0) {
							progress = downloadSize * 100.0 / totalSize;
						}
						holder.mProgressBar.setVisibility(View.VISIBLE);
						holder.mProgressBar.setProgress((int) progress);
						// holder.mProgressBar.setVisibility(View.VISIBLE);
						holder.mFirstButton.setVisibility(View.VISIBLE);
						holder.mSecondButton.setVisibility(View.VISIBLE);
						holder.mLongButton.setVisibility(View.GONE);
						holder.mCenterTextView.setVisibility(View.GONE);
					} else if (aDownloadApkItem.apkStatus == STATUS_OF_PAUSEUPDATE_BYHAND || aDownloadApkItem.apkStatus == STATUS_OF_PAUSEUPDATE) {
						holder.mProgressBar.setVisibility(View.GONE);
						holder.mFirstButton.setVisibility(View.VISIBLE);
						holder.mSecondButton.setVisibility(View.VISIBLE);
						holder.mLongButton.setVisibility(View.GONE);
						holder.mCenterTextView.setVisibility(View.VISIBLE);
					}
					// else if (aDownloadApkItem.apkStatus ==
					// STATUS_OF_IGNOREUPDATE) {
					// holder.mProgressBar.setVisibility(View.GONE);
					// holder.mFirstButton.setVisibility(View.GONE);
					// holder.mSecondButton.setVisibility(View.GONE);
					// holder.mLongButton.setVisibility(View.VISIBLE);
					// holder.mCenterTextView.setVisibility(View.GONE);
					// }
					try {
						FileService.getBitmap(aDownloadApkItem.apkIconUrl, holder.mImageView, mDefaultBitmap, 0);
					} catch (OutOfMemoryError e) {
						if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
							mDefaultBitmap.recycle();
						}
					}
				}

			}
		}
	}

	private void fillInstallChildView(ChildViewHolder holder, int groupPosition, int childPosition) {

		// /////*********************//
		if (groupPosition < childData.size() && childPosition < childData.get(groupPosition).size()) {
			if (childData.get(groupPosition).get(childPosition).equals(CHILDISNULL)) {
				holder.mEmptyTextView.setText(R.string.current_not_install_app);
				holder.mEmptyTextView.setVisibility(View.VISIBLE);
				holder.mContentLayout.setVisibility(View.GONE);
			} else {
				ADownloadApkItem aDownloadApkItem = (ADownloadApkItem) childData.get(groupPosition).get(childPosition);
				holder.mAppNameTextView.setText(aDownloadApkItem.apkName);
				holder.mEmptyTextView.setVisibility(View.GONE);
				holder.mContentLayout.setVisibility(View.VISIBLE);
				holder.mCenterTextView.setVisibility(View.GONE);

				holder.mAppVersionTextView.setVisibility(View.GONE);

				int totalSize = aDownloadApkItem.apkTotalSize;
				holder.mBottomTextView.setText(aDownloadApkItem.apkVersion + "/" + NetTool.formatString(totalSize / 1048576.0) + "M");
				holder.mProgressBar.setVisibility(View.GONE);
				holder.mFirstButton.setVisibility(View.VISIBLE);
				holder.mSecondButton.setVisibility(View.VISIBLE);
				holder.mLongButton.setVisibility(View.GONE);
				holder.mFirstButton.setText(getButtonText(holder.mFirstButton, aDownloadApkItem.apkStatus, 1));
				holder.mFirstButton.setOnClickListener(new InstallButtonClick(aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode));
				holder.mSecondButton.setText(getButtonText(holder.mSecondButton, aDownloadApkItem.apkStatus, 2));
				holder.mSecondButton.setOnClickListener(new CancelButtonClick(aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode));
				try {
					FileService.getBitmap(aDownloadApkItem.apkIconUrl, holder.mImageView, mDefaultBitmap, 0);
				} catch (OutOfMemoryError e) {
					if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
						mDefaultBitmap.recycle();
					}
				}
			}
		}
	}

	private void fillIgnoreChildView(ChildViewHolder holder, int groupPosition, int childPosition) {
		if (childPosition < childData.get(groupPosition).size()) {
			if (childData.get(groupPosition).get(childPosition).equals(CHILDISNULL)) {
				holder.mEmptyTextView.setText(R.string.current_not_ignore_app);
				holder.mEmptyTextView.setVisibility(View.VISIBLE);
				holder.mContentLayout.setVisibility(View.GONE);
			} else if (childData.get(groupPosition).get(childPosition) instanceof AIgnoreItem) {
				AIgnoreItem aIgnoreItem = (AIgnoreItem) childData.get(groupPosition).get(childPosition);
				holder.mAppNameTextView.setText(aIgnoreItem.apkName);
				holder.mEmptyTextView.setVisibility(View.GONE);
				holder.mContentLayout.setVisibility(View.VISIBLE);
				holder.mCenterTextView.setVisibility(View.GONE);

				holder.mAppVersionTextView.setVisibility(View.GONE);

				int totalSize = aIgnoreItem.apkTotalSize;
				holder.mBottomTextView.setText(aIgnoreItem.apkVersion + "/" + NetTool.formatString(totalSize / 1048576.0) + "M");
				holder.mProgressBar.setVisibility(View.GONE);
				holder.mLongButton.setText(context.getString(R.string.cancle_ignore));
				holder.mLongButton.setBackgroundResource(R.drawable.cancel_selector);
				holder.mLongButton.setTextColor(Color.parseColor("#7f5100"));
				holder.mLongButton.setOnClickListener(new IgnoreButtonClick(childPosition));

				holder.mProgressBar.setVisibility(View.GONE);
				holder.mFirstButton.setVisibility(View.GONE);
				holder.mSecondButton.setVisibility(View.GONE);
				holder.mLongButton.setVisibility(View.VISIBLE);
				holder.mCenterTextView.setVisibility(View.GONE);

				try {
					FileService.getBitmap(aIgnoreItem.apkIconUrl, holder.mImageView, mDefaultBitmap, 0);
				} catch (OutOfMemoryError e) {
					if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
						mDefaultBitmap.recycle();
					}
				}
			}
		}
	}

	private String getButtonText(TextView textView, int apkStatus, int btnFlag) {
		String buttonText = "";
		if (btnFlag == 1) {
			switch (apkStatus) {
			case STATUS_OF_PREPAREDOWNLOAD:
			case STATUS_OF_DOWNLOADING:
			case STATUS_OF_PREPAREUPDATE:
			case STATUS_OF_UPDATEING:
				textView.setBackgroundResource(R.drawable.update_selector);
				textView.setTextColor(Color.parseColor("#0e567d"));
				buttonText = context.getString(R.string.button_status_pause);
				break;
			case STATUS_OF_PAUSE:
			case STATUS_OF_PAUSE_BYHAND:
			case STATUS_OF_PAUSEUPDATE:
			case STATUS_OF_PAUSEUPDATE_BYHAND:
				textView.setBackgroundResource(R.drawable.update_selector);
				textView.setTextColor(Color.parseColor("#0e567d"));
				buttonText = context.getString(R.string.button_status_continue);
				break;
			case STATUS_OF_UPDATE:
				textView.setBackgroundResource(R.drawable.cancel_selector);
				textView.setTextColor(Color.parseColor("#7f5100"));
				buttonText = context.getString(R.string.ignore);
				break;
			case STATUS_OF_DOWNLOADCOMPLETE:
			case STATUS_OF_UPDATECOMPLETE:
				textView.setBackgroundResource(R.drawable.install_button_selector);
				textView.setTextColor(Color.BLACK);
				buttonText = context.getString(R.string.button_status_install);
				break;
			}

		} else if (btnFlag == 2) {
			switch (apkStatus) {
			case STATUS_OF_PAUSE:
			case STATUS_OF_PAUSE_BYHAND:
			case STATUS_OF_PAUSEUPDATE:
			case STATUS_OF_PAUSEUPDATE_BYHAND:
			case STATUS_OF_PREPAREUPDATE:
			case STATUS_OF_UPDATEING:

				textView.setBackgroundResource(R.drawable.cancel_selector);
				textView.setTextColor(Color.parseColor("#7f5100"));
				buttonText = context.getString(R.string.cancel);
				break;
			case STATUS_OF_UPDATE:
				textView.setBackgroundResource(R.drawable.update_selector);
				textView.setTextColor(Color.parseColor("#0e567d"));
				buttonText = context.getString(R.string.update);
				break;
			case STATUS_OF_DOWNLOADCOMPLETE:
			case STATUS_OF_UPDATECOMPLETE:
				textView.setBackgroundResource(R.drawable.cancel_selector);
				textView.setTextColor(Color.parseColor("#7f5100"));
				buttonText = context.getString(R.string.app_delete);
				break;
			// textView.setBackgroundResource(R.drawable.install_button_selector);
			// textView.setTextColor(Color.BLACK);
			// buttonText = context.getString(R.string.button_status_cancel);
			// break;
			}

		}

		return buttonText;
	}

	private class DownloadButtonClick implements OnClickListener {
		private int childPosition;

		public DownloadButtonClick(int childPosition) {
			this.childPosition = childPosition;
		}

		@Override
		public void onClick(View v) {
			if (!NetTool.isFastDoubleClick((Long) (v.getTag() == null ? 0L : v.getTag()))) {
				long currentTime = System.currentTimeMillis();
				v.setTag(currentTime);
				String btnText = ((Button) v).getText().toString();

				/*** kai.zhang ***/
				if (childData.get(0).size() <= childPosition) {
					return;
				}
				if (childData.get(0).get(childPosition) instanceof ADownloadApkItem) {
					ADownloadApkItem aDownloadApkItem = (ADownloadApkItem) childData.get(0).get(childPosition);

					/*** kai.zhang ***/
					if (aDownloadApkItem.apkStatus == STATUS_OF_DOWNLOADCOMPLETE) {
						sendRefreshMessage(1);
						return;
					}

					if (aDownloadApkItem.apkDownloadSize > 0 && aDownloadApkItem.apkDownloadSize == aDownloadApkItem.apkTotalSize) {
						aDownloadApkItem.apkStatus = STATUS_OF_DOWNLOADCOMPLETE;
						NetTool.deleteLastSuffix(NetTool.getAbsolutePath(aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode, "apk.temp"));
						Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
						intent.putExtra(BROADCAST_COMPLETEDOWNLOAD, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
						Bundle bundle = new Bundle();
						bundle.putInt("status", aDownloadApkItem.apkStatus);
						intent.putExtras(bundle);
						context.sendBroadcast(intent);
						return;
					}

					Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
					if (btnText.equals(context.getString(R.string.button_status_pause))) {
						aDownloadApkItem.apkStatus = STATUS_OF_PAUSE_BYHAND;
						intent.putExtra(BROADCAST_PAUSEDOWNLOAD, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
						context.sendBroadcast(intent);
						sendRefreshMessage(1);
						return;
					}
					if (btnText.equals(context.getString(R.string.button_status_continue))) {
						DJMarketUtils.checkDownload(context, aDownloadApkItem);
						sendRefreshMessage(2);
						return;
					}
					if (btnText.equals(context.getString(R.string.button_status_cancel))) {
						intent.putExtra(BROADCAST_CANCELDOWNLOAD, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);

						// childData.get(0).remove(childPosition);
						/*
						 * if (childData.get(0).size() == 0) {
						 * groupData.remove(0); childData.remove(0); }
						 */
						context.sendBroadcast(intent);
						((ADownloadApkItem) childData.get(0).get(childPosition)).apkDownloadSize = 0;
						((ADownloadApkItem) childData.get(0).get(childPosition)).apkStatus = STATUS_OF_CANCEL;
						sendRefreshMessage(1);
						return;
					}
				}
			} else {
				showLog("不要点太快哦");
			}
		}

	}

	private class UpdateButtonClick implements OnClickListener {
		// private int apkId;
		private int childPosition;

		public UpdateButtonClick(int childPosition) {
			// this.apkId = apkId;
			this.childPosition = childPosition;
		}

		@Override
		public void onClick(View v) {
			if (!NetTool.isFastDoubleClick((Long) (v.getTag() == null ? 0L : v.getTag()))) {
				long currentTime = System.currentTimeMillis();
				v.setTag(currentTime);
				String btnText = ((Button) v).getText().toString();
				ADownloadApkItem aDownloadApkItem = null;
				if (groupData.size() == 3) {
					/***
					 * kai.zhang if (childData.get(0).size() <= childPosition) {
					 * return ; }
					 ***/

					if (childData.get(0).get(childPosition) instanceof ADownloadApkItem) {
						aDownloadApkItem = (ADownloadApkItem) childData.get(0).get(childPosition);
					}
				} else {
					if (childData.get(1).get(childPosition) instanceof ADownloadApkItem) {
						aDownloadApkItem = (ADownloadApkItem) childData.get(1).get(childPosition);
					}
				}
				if (null != aDownloadApkItem) {
					if (aDownloadApkItem.apkStatus == STATUS_OF_UPDATECOMPLETE) {
						sendRefreshMessage(1);
						return;
					}

					Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
					if (btnText.equals(context.getString(R.string.ignore))) {
						// TODO 忽略应用
						// aDownloadApkItem.apkStatus = STATUS_OF_IGNOREUPDATE;
						ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(context);
						InstalledAppInfo installedAppInfo = NetTool.getInstallAppInfoByPackage(context, installedAppInfos, aDownloadApkItem.apkPackageName);
						if (null != installedAppInfo) {
							AIgnoreItem aIgnoreItem = new AIgnoreItem(aDownloadApkItem, installedAppInfo);
							aDownloadApkDBHelper.insertToIgnore(aIgnoreItem);
							if (groupData.size() == 3) {
								if (childData.get(0).size() > 1) {
									childData.get(0).remove(aDownloadApkItem);
								} else if (childData.get(0).get(0) instanceof ADownloadApkItem) {
									childData.get(0).remove(aDownloadApkItem);
									childData.get(0).add(CHILDISNULL);
								}
								childData.get(2).add(aIgnoreItem);
							} else {
								if (childData.get(1).size() > 1) {
									childData.get(1).remove(aDownloadApkItem);
								} else if (childData.get(1).get(0) instanceof ADownloadApkItem) {
									childData.get(1).remove(aDownloadApkItem);
									childData.get(1).add(CHILDISNULL);
								}
								childData.get(3).add(aIgnoreItem);
							}
							ADownloadService.updateAPKList.apkList.remove(aDownloadApkItem);
							ADownloadService.updateAPKList.ignoreAppList.add(aIgnoreItem);
						}
						intent.putExtra(BROADCAST_IGNOREUPDATE, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
						context.sendBroadcast(intent);
						sendRefreshMessage(1);
					} else if (btnText.equals(context.getString(R.string.update))) {
						System.out.println("=============update ");
						DJMarketUtils.checkDownload(context, aDownloadApkItem);
						// DJMarketUtils.checkDownload(context, , mTextView,
						// listener)
						sendRefreshMessage(2);
					} else if (btnText.equals(context.getString(R.string.button_status_pause))) {
						aDownloadApkItem.apkStatus = STATUS_OF_PAUSEUPDATE_BYHAND;
						intent.putExtra(BROADCAST_PAUSEUPDATE, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
						context.sendBroadcast(intent);
						sendRefreshMessage(1);
					} else if (btnText.equals(context.getString(R.string.button_status_continue))) {
						System.out.println("============continue ");
						DJMarketUtils.checkDownload(context, aDownloadApkItem);
						sendRefreshMessage(2);
					} else if (btnText.equals(context.getString(R.string.cancel))) {
						aDownloadApkItem.apkStatus = STATUS_OF_UPDATE;
						aDownloadApkItem.apkDownloadSize = 0;
						intent.putExtra(BROADCAST_CANCELUPDATE, aDownloadApkItem.apkPackageName + "_" + aDownloadApkItem.apkVersionCode);
						context.sendBroadcast(intent);
						sendRefreshMessage(1);
					}
					// else if (btnText.equals(context
					// .getString(R.string.cancle_ignore))) {
					// aDownloadApkItem.apkStatus = STATUS_OF_UPDATE;
					// intent.putExtra(BROADCAST_CANCELIGNORE,
					// aDownloadApkItem.apkPackageName + "_"
					// + aDownloadApkItem.apkVersionCode);
					// ADownloadApkDBHelper aDownloadApkDBHelper = new
					// ADownloadApkDBHelper(
					// context);
					// aDownloadApkDBHelper
					// .deleteIgnoreByPackageName(aDownloadApkItem.apkPackageName);
					// context.sendBroadcast(intent);
					// sendRefreshMessage(1);
					// }
				}

			} else {
				showLog("不要点太快哦");
			}
		}
	}

	private class IgnoreButtonClick implements OnClickListener {
		private int childPosition;

		public IgnoreButtonClick(int childPosition) {
			this.childPosition = childPosition;
		}

		@Override
		public void onClick(View v) {
			if (!NetTool.isFastDoubleClick((Long) (v.getTag() == null ? 0L : v.getTag()))) {
				long currentTime = System.currentTimeMillis();
				v.setTag(currentTime);
				String btnText = ((Button) v).getText().toString();
				AIgnoreItem aIgnoreItem = null;
				if (groupData.size() == 3) {
					if (childData.get(2).get(childPosition) instanceof AIgnoreItem) {
						aIgnoreItem = (AIgnoreItem) childData.get(2).get(childPosition);
					}
				} else {
					if (childData.get(3).get(childPosition) instanceof AIgnoreItem) {
						aIgnoreItem = (AIgnoreItem) childData.get(3).get(childPosition);
					}
				}

				if (null != aIgnoreItem) {
					if (btnText.equals(context.getString(R.string.cancle_ignore))) {
						if (groupData.size() == 3) {
							if (childData.get(2).size() > 1) {
								childData.get(2).remove(aIgnoreItem);
								List<ApkItem> apkItems = ((AppMarket) context.getApplicationContext()).getUpdateList();
								ApkItem apkItem;
								for (int i = 0; i < apkItems.size(); i++) {
									apkItem = apkItems.get(i);
									if (apkItem.packageName.equals(aIgnoreItem.apkPackageName)) {
										ADownloadApkItem aDownloadApkItem = new ADownloadApkItem(apkItem, STATUS_OF_UPDATE);
										childData.get(0).add(aDownloadApkItem);
										ADownloadService.updateAPKList.apkList.add(aDownloadApkItem);
									}
								}
							} else {
								childData.get(2).remove(aIgnoreItem);
								childData.get(2).add(CHILDISNULL);
								List<ApkItem> apkItems = ((AppMarket) context.getApplicationContext()).getUpdateList();
								ApkItem apkItem;
								for (int i = 0; i < apkItems.size(); i++) {
									apkItem = apkItems.get(i);
									if (apkItem.packageName.equals(aIgnoreItem.apkPackageName)) {
										ADownloadApkItem aDownloadApkItem = new ADownloadApkItem(apkItem, STATUS_OF_UPDATE);
										childData.get(0).add(aDownloadApkItem);
										ADownloadService.updateAPKList.apkList.add(aDownloadApkItem);
									}
								}
							}
						} else {
							if (childData.get(3).size() > 1) {
								childData.get(3).remove(aIgnoreItem);
								List<ApkItem> apkItems = ((AppMarket) context.getApplicationContext()).getUpdateList();
								ApkItem apkItem;
								for (int i = 0; i < apkItems.size(); i++) {
									apkItem = apkItems.get(i);
									if (apkItem.packageName.equals(aIgnoreItem.apkPackageName)) {
										ADownloadApkItem aDownloadApkItem = new ADownloadApkItem(apkItem, STATUS_OF_UPDATE);
										childData.get(1).add(aDownloadApkItem);
										ADownloadService.updateAPKList.apkList.add(aDownloadApkItem);
									}
								}
							} else {
								childData.get(3).remove(aIgnoreItem);
								childData.get(3).add(CHILDISNULL);
								List<ApkItem> apkItems = ((AppMarket) context.getApplicationContext()).getUpdateList();
								ApkItem apkItem;
								for (int i = 0; i < apkItems.size(); i++) {
									apkItem = apkItems.get(i);
									if (apkItem.packageName.equals(aIgnoreItem.apkPackageName)) {
										ADownloadApkItem aDownloadApkItem = new ADownloadApkItem(apkItem, STATUS_OF_UPDATE);
										childData.get(1).add(aDownloadApkItem);
										ADownloadService.updateAPKList.apkList.add(aDownloadApkItem);
									}
								}
							}
						}
						ADownloadService.updateAPKList.ignoreAppList.remove(aIgnoreItem);
						Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
						intent.putExtra(BROADCAST_CANCELIGNORE, aIgnoreItem.apkPackageName + "_" + aIgnoreItem.apkVersionCode);
						context.sendBroadcast(intent);
						ADownloadApkDBHelper aDownloadApkDBHelper = new ADownloadApkDBHelper(context);
						aDownloadApkDBHelper.deleteIgnoreByPackageName(aIgnoreItem.apkPackageName);

						sendRefreshMessage(1);
					}
				}

			} else {
				showLog("不要点太快哦");
			}
		}
	}

	private class InstallButtonClick implements OnClickListener {
		// private int apkId;
		private String apkSaveName;

		public InstallButtonClick(String apkSaveName) {
			// this.apkId = apkId;
			this.apkSaveName = apkSaveName;
		}

		@Override
		public void onClick(View v) {
			if (!NetTool.isFastDoubleClick((Long) (v.getTag() == null ? 0L : v.getTag()))) {
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(Uri.fromFile(new File(NetTool.DOWNLOADPATH + apkSaveName + ".apk")), "application/vnd.android.package-archive");
				context.startActivity(installIntent);
				return;
			} else {
				showLog("不要点太快哦");
			}
		}

	}

	private class CancelButtonClick implements OnClickListener {
		private String apkSaveName;

		public CancelButtonClick(String apkSaveName) {
			this.apkSaveName = apkSaveName;
		}

		@Override
		public void onClick(View v) {
			if (!NetTool.isFastDoubleClick((Long) (v.getTag() == null ? 0L : v.getTag()))) {
				/*** kai.zhang 注释 ***/
				/*
				 * Intent intent = new Intent(BROADCAST_ACTION_DOWNLOAD);
				 * intent.putExtra(BROADCAST_CANCELINSTALL, apkSaveName);
				 * context.sendBroadcast(intent);
				 */

				NetTool.fillWaitingInstallNotifitcation(context);

				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString(HANDLER_CLEARPACKAGE, apkSaveName);
				msg.setData(bundle);
				msg.what = REFERENSH_SCREEN;
				classHandler.sendMessage(msg);
				return;
			} else {
				showLog("不要点太快哦");
			}
		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupData.size() == 4) {
			if (childData.get(1).size() == 0) {
				childData.get(1).add(CHILDISNULL);
			} else if (childData.get(1).size() > 1 && childData.get(1).get(0).equals(CHILDISNULL)) {
				childData.get(1).remove(CHILDISNULL);
			}
			if (childData.get(2).size() == 0) {
				childData.get(2).add(CHILDISNULL);
			} else if (childData.get(2).size() > 1 && childData.get(2).get(0).equals(CHILDISNULL)) {
				childData.get(2).remove(CHILDISNULL);
			}
			if (childData.get(3).size() == 0) {
				childData.get(3).add(CHILDISNULL);
			} else if (childData.get(3).size() > 1 && childData.get(3).get(0).equals(CHILDISNULL)) {
				childData.get(3).remove(CHILDISNULL);
			}
		} else if (groupData.size() == 3) {
			if (childData.get(0).size() == 0) {
				childData.get(0).add(CHILDISNULL);
			} else if (childData.get(0).size() > 1 && childData.get(0).get(0).equals(CHILDISNULL)) {
				childData.get(0).remove(CHILDISNULL);
			}
			if (childData.get(1).size() == 0) {
				childData.get(1).add(CHILDISNULL);
			} else if (childData.get(1).size() > 1 && childData.get(1).get(0).equals(CHILDISNULL)) {
				childData.get(1).remove(CHILDISNULL);
			}
			if (childData.get(2).size() == 0) {
				childData.get(2).add(CHILDISNULL);
			} else if (childData.get(2).size() > 1 && childData.get(2).get(0).equals(CHILDISNULL)) {
				childData.get(2).remove(CHILDISNULL);
			}
		}

		return childData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.adownloadexpandgroup, null);
			holder = new GroupViewHolder();
			holder.mTextView = (TextView) convertView.findViewById(R.id.tvExpandGroupTitle);
			holder.mButton = (Button) convertView.findViewById(R.id.btnOneKeyUpdate);
			holder.mImageView = (ImageView) convertView.findViewById(R.id.ivExpandGroupPic);
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		holder.mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DJMarketUtils.checkDownload(context, FLAG_ONEKEYUPDATEING);
			}
		});
		if (groupPosition < groupData.size()) {
			holder.mTextView.setText(groupData.get(groupPosition));
			String title = groupData.get(groupPosition);
			if (title.equals(context.getString(R.string.transferapk))) {
				holder.mButton.setVisibility(View.GONE);
				holder.mImageView.setVisibility(View.VISIBLE);
				if (isExpanded) {
					holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.pic_down));
				} else {
					holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.pic_up));
				}
			} else if (title.equals(context.getString(R.string.updateapk))) {
				holder.mButton.setVisibility(View.VISIBLE);
				holder.mImageView.setVisibility(View.GONE);

			} else if (title.equals(context.getString(R.string.waitinstallapk))) {
				holder.mButton.setVisibility(View.GONE);
				holder.mImageView.setVisibility(View.GONE);
			} else if (title.equals(context.getString(R.string.ignoreapk))) {
				holder.mButton.setVisibility(View.GONE);
				holder.mImageView.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	private static class GroupViewHolder {
		TextView mTextView;
		Button mButton;
		ImageView mImageView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private void showLog(String msg) {
		Log.i("ADownloadExpandAdapter", msg);
	}

	public void removeMessage() {
		if (classHandler != null && classHandler.hasMessages(REFERENSH_PROGRESS)) {
			classHandler.removeMessages(REFERENSH_PROGRESS);
		}
	}

	/**
	 * 
	 * @param flag
	 *            =1,立即刷新 =2 延迟刷新
	 */
	public void sendRefreshMessage(int flag) {
		removeMessage();
		if (flag == 1) {
			classHandler.sendEmptyMessage(REFERENSH_PROGRESS);
		} else {
			classHandler.sendEmptyMessageDelayed(REFERENSH_PROGRESS, 800);
		}
	}
}
