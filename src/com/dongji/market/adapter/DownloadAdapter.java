package com.dongji.market.adapter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.activity.DownloadActivity;
import com.dongji.market.activity.DownloadActivity.MyHandler;
import com.dongji.market.cache.FileService;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadService;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.ApkItem;
import com.umeng.common.net.r;

public class DownloadAdapter extends BaseExpandableListAdapter implements DownloadConstDefine, DownloadService.DownloadStatusListener {
	private Context context;
	private List<List<DownloadEntity>> childList;
	private List<String> groupList;
	private String downloadingString, updatingString;
	private Bitmap mDefaultBitmap;
	private String continueString, cancelString, pauseString, installString, ignoreString, cancelIgnoreString, deleteString, updateString;

	private static final int EVENT_REFRESH_DATA = 2;

	private boolean locked;

	private MyHandler mHandler;

	private DecimalFormat df = null;

	// public static int isRootInstalling = 0;
	public static List<String> rootApkList = new ArrayList<String>();

	public DownloadAdapter(Context context, List<List<DownloadEntity>> list, List<String> groupList, DownloadActivity.MyHandler mHandler) {
		this.context = context;
		this.childList = list;
		this.groupList = groupList;

		this.mHandler = mHandler;
		/*
		 * if(DownloadService.mDownloadService!=null) {
		 * DownloadService.mDownloadService.setDownloadStatusListener(this); }
		 */
		DownloadService.setDownloadStatusListener(this);
		initString();
		df = new DecimalFormat("##0.00");
		try {
			mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		registerAllReceiver();
	}

	private void registerAllReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_ACTION_COMPLETE_DOWNLOAD);
		intentFilter.addAction(BROADCAST_ACTION_ADD_UPDATE);
		intentFilter.addAction(BROADCAST_ACTION_INSTALL_COMPLETE);
		intentFilter.addAction(BROADCAST_ACTION_REMOVE_COMPLETE);
		intentFilter.addAction(BROADCAST_ACTION_ADD_DOWNLOAD_LIST);
		intentFilter.addAction(BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE);
		intentFilter.addAction(BROADCAST_ACTION_UPDATE_ROOTSTATUS);
		context.registerReceiver(mDownloadReceiver, intentFilter);
	}

	public void unregisterAllReceiver() {
		context.unregisterReceiver(mDownloadReceiver);
	}

	private void initString() {
		downloadingString = context.getString(R.string.transferapk);
		updatingString = context.getString(R.string.updateapk);
		continueString = context.getString(R.string.button_status_continue);
		cancelString = context.getString(R.string.button_status_cancel);
		pauseString = context.getString(R.string.button_status_pause);
		installString = context.getString(R.string.button_status_install);
		ignoreString = context.getString(R.string.ignore);
		cancelIgnoreString = context.getString(R.string.cancle_ignore);
		deleteString = context.getString(R.string.app_delete);
		updateString = context.getString(R.string.update);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
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
			holder.mAuthorityImageview = (ImageView) convertView.findViewById(R.id.authorityimageview);

			convertView.setTag(holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag();
			holder.mProgressBar.setProgress(0);
		}

		DownloadEntity entity = null;
		// System.out.println(childList.size()+", "+groupPosition+", "+childPosition);
		if (childList.size() > groupPosition && childList.get(groupPosition).size() > childPosition) {
			entity = childList.get(groupPosition).get(childPosition);
			// System.out.println("groupPosition:"+groupPosition+", "+childPosition+", "+entity.downloadType+", "+entity.iconUrl);
			switch (entity.downloadType) {
			case DownloadConstDefine.TYPE_OF_DOWNLOAD:
				fillDownloadChildView(entity, holder);
				if (!TextUtils.isEmpty(entity.iconUrl)) {
					try {
						FileService.getBitmap(entity.iconUrl, holder.mImageView, mDefaultBitmap, 0);
					} catch (OutOfMemoryError e) {
						if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
							mDefaultBitmap.recycle();
						}
					}
				}
				break;
			case DownloadConstDefine.TYPE_OF_UPDATE:
				fillUpdateChildView(entity, holder);
				if (entity != null && entity.installedIcon != null) {
					holder.mImageView.setImageBitmap(((BitmapDrawable) entity.installedIcon).getBitmap());
				} else {
					holder.mImageView.setImageBitmap(mDefaultBitmap);
				}
				break;
			case DownloadConstDefine.TYPE_OF_COMPLETE:
				fillWaitInstallChildView(entity, holder);
				if (!TextUtils.isEmpty(entity.iconUrl)) {
					try {
						FileService.getBitmap(entity.iconUrl, holder.mImageView, mDefaultBitmap, 0);
					} catch (OutOfMemoryError e) {
						if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
							mDefaultBitmap.recycle();
						}
					}
				}
				break;
			case TYPE_OF_IGNORE:
				fillIgnoreChildView(entity, holder);
				if (entity != null && entity.installedIcon != null) {
					holder.mImageView.setImageBitmap(((BitmapDrawable) entity.installedIcon).getBitmap());
				} else {
					holder.mImageView.setImageBitmap(mDefaultBitmap);
				}
				break;
			}
		} else {
			if (childList.size() == 4) {
				if (groupPosition == 0) {
					if (childList.get(groupPosition).size() > childPosition) {
						entity = childList.get(groupPosition).get(childPosition);
						fillDownloadChildView(entity, holder);
						if (!TextUtils.isEmpty(entity.iconUrl)) {
							try {
								FileService.getBitmap(entity.iconUrl, holder.mImageView, mDefaultBitmap, 0);
							} catch (OutOfMemoryError e) {
								if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
									mDefaultBitmap.recycle();
								}
							}
						}
					}
				} else if (groupPosition == 1) {
					if (childList.get(groupPosition).size() > childPosition) {
						entity = childList.get(groupPosition).get(childPosition);
						if (entity != null && entity.installedIcon != null) {
							holder.mImageView.setImageBitmap(((BitmapDrawable) entity.installedIcon).getBitmap());
						} else {
							holder.mImageView.setImageBitmap(mDefaultBitmap);
						}
					}
					fillUpdateChildView(entity, holder);
				} else if (groupPosition == 2) {
					if (childList.get(groupPosition).size() > childPosition) {
						if (entity != null && !TextUtils.isEmpty(entity.iconUrl)) {
							try {
								FileService.getBitmap(entity.iconUrl, holder.mImageView, mDefaultBitmap, 0);
							} catch (OutOfMemoryError e) {
								if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
									mDefaultBitmap.recycle();
								}
							}
						}
					}
					fillWaitInstallChildView(entity, holder);
				} else if (groupPosition == 3) {
					if (childList.get(groupPosition).size() > childPosition) {
						entity = childList.get(groupPosition).get(childPosition);
						if (entity != null && entity.installedIcon != null) {
							holder.mImageView.setImageBitmap(((BitmapDrawable) entity.installedIcon).getBitmap());
						} else {
							holder.mImageView.setImageBitmap(mDefaultBitmap);
						}
					}
					fillIgnoreChildView(entity, holder);
				}
			} else {
				if (groupPosition == 0) {
					if (childList.get(0).size() > childPosition) {
						entity = childList.get(groupPosition).get(childPosition);
						if (entity != null && entity.installedIcon != null) {
							holder.mImageView.setImageBitmap(((BitmapDrawable) entity.installedIcon).getBitmap());
						} else {
							holder.mImageView.setImageBitmap(mDefaultBitmap);
						}
					}
					fillUpdateChildView(entity, holder);
				} else if (groupPosition == 1) {
					if (childList.get(groupPosition).size() > childPosition) {
						entity = childList.get(groupPosition).get(childPosition);
						if (entity != null && !TextUtils.isEmpty(entity.iconUrl)) {
							try {
								FileService.getBitmap(entity.iconUrl, holder.mImageView, mDefaultBitmap, 0);
							} catch (OutOfMemoryError e) {
								if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
									mDefaultBitmap.recycle();
								}
							}
						}
					}
					fillWaitInstallChildView(entity, holder);
				} else if (groupPosition == 2) {
					if (childList.get(groupPosition).size() > childPosition) {
						entity = childList.get(groupPosition).get(childPosition);
						if (entity != null && entity.installedIcon != null) {
							holder.mImageView.setImageBitmap(((BitmapDrawable) entity.installedIcon).getBitmap());
						} else {
							holder.mImageView.setImageBitmap(mDefaultBitmap);
						}
					}
					fillIgnoreChildView(entity, holder);
				}
			}

		}
		return convertView;
	}

	/**
	 * 下载状态的条目显示
	 * 
	 * @param entity
	 * @param holder
	 */
	private void fillDownloadChildView(DownloadEntity entity, ChildViewHolder holder) {
		holder.mEmptyTextView.setVisibility(View.GONE);
		holder.mContentLayout.setVisibility(View.VISIBLE);

		holder.mAppNameTextView.setText(entity.appName);
		holder.mAppVersionTextView.setVisibility(View.VISIBLE);
		holder.mAppVersionTextView.setText(entity.versionName);
		String currentDownloadSize = df.format(entity.currentPosition / 1048576.0);
		String currentTotalSize = df.format(entity.fileLength / 1048576.0);
		holder.mBottomTextView.setText(currentDownloadSize + "M/" + currentTotalSize + "M");

		setButtonStatus(entity, holder);

		holder.mContentLayout.setOnClickListener(new DetailOnClickListener(entity));

		if (entity.heavy > 0) {
			holder.mAuthorityImageview.setVisibility(View.VISIBLE);
		} else {
			holder.mAuthorityImageview.setVisibility(View.GONE);
		}

		switch (entity.getStatus()) {
		case STATUS_OF_DOWNLOADING:
		case STATUS_OF_COMPLETE:
		case STATUS_OF_PREPARE:
			holder.mCenterTextView.setVisibility(View.GONE);
			holder.mProgressBar.setVisibility(View.VISIBLE);
			if (entity.fileLength > 0 && entity.currentPosition > 0) {
				double progress = entity.currentPosition * 100.0 / entity.fileLength;
				holder.mProgressBar.setProgress((int) progress);
			}
			break;
		/*
		 * holder.mCenterTextView.setVisibility(View.VISIBLE);
		 * holder.mProgressBar.setVisibility(View.GONE);
		 * holder.mCenterTextView.setText("正在获取连接"); break;
		 */
		case STATUS_OF_EXCEPTION:
			holder.mCenterTextView.setVisibility(View.VISIBLE);
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mCenterTextView.setText(R.string.download_error_msg1);
			break;
		case STATUS_OF_PAUSE:
		case STATUS_OF_PAUSE_ON_TRAFFIC_LIMIT:
		case STATUS_OF_PAUSE_ON_EXIT_SYSTEM:
			holder.mCenterTextView.setVisibility(View.VISIBLE);
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mCenterTextView.setText(R.string.paused);
			break;
		}
	}

	/**
	 * 更新状态的条目显示
	 * 
	 * @param entity
	 * @param holder
	 */
	private void fillUpdateChildView(DownloadEntity entity, ChildViewHolder holder) {
		if (entity == null) {
			holder.mEmptyTextView.setVisibility(View.VISIBLE);
			holder.mContentLayout.setVisibility(View.GONE);
			holder.mEmptyTextView.setText(R.string.current_not_update_app);
			return;
		}

		holder.mEmptyTextView.setVisibility(View.GONE);
		holder.mContentLayout.setVisibility(View.VISIBLE);
		holder.mAppNameTextView.setText(entity.appName);
		holder.mAppVersionTextView.setVisibility(View.VISIBLE);
		holder.mAppVersionTextView.setText("V" + entity.installedVersionName + "/" + NetTool.sizeFormat((int) entity.installedFileLength));
		holder.mBottomTextView.setVisibility(View.VISIBLE);

		holder.mBottomTextView.setText("V" + entity.versionName + "/" + NetTool.sizeFormat((int) entity.fileLength));

		setButtonStatus(entity, holder);

		if (entity.heavy > 0) {
			holder.mAuthorityImageview.setVisibility(View.VISIBLE);
		} else {
			holder.mAuthorityImageview.setVisibility(View.GONE);
		}

		holder.mContentLayout.setOnClickListener(new DetailOnClickListener(entity));
		switch (entity.getStatus()) {
		case STATUS_OF_DOWNLOADING:
		case STATUS_OF_COMPLETE:
		case STATUS_OF_PREPARE:
			holder.mCenterTextView.setVisibility(View.GONE);
			holder.mProgressBar.setVisibility(View.VISIBLE);
			if (entity.fileLength > 0 && entity.currentPosition >= 0) {
				double progress = entity.currentPosition * 100.0 / entity.fileLength;
				holder.mProgressBar.setProgress((int) progress);
			}
			break;
		case STATUS_OF_INITIAL:
			holder.mCenterTextView.setVisibility(View.GONE);
			holder.mProgressBar.setVisibility(View.GONE);

			break;
		case STATUS_OF_EXCEPTION:
			holder.mCenterTextView.setVisibility(View.VISIBLE);
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mCenterTextView.setText(R.string.update_error_msg1);
			break;
		default:
			holder.mCenterTextView.setVisibility(View.VISIBLE);
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mCenterTextView.setText(R.string.paused);
			break;
		}
	}

	/**
	 * 待安装条目显示
	 * 
	 * @param entity
	 * @param holder
	 */
	private void fillWaitInstallChildView(DownloadEntity entity, ChildViewHolder holder) {
		if (entity == null) {
			holder.mEmptyTextView.setVisibility(View.VISIBLE);
			holder.mContentLayout.setVisibility(View.GONE);
			holder.mEmptyTextView.setText(R.string.current_not_install_app);
			return;
		}
		System.out.println("..........test0522.....fillWaitInstallChildView..." + entity.appName + "," + entity.downloadType);
		holder.mContentLayout.setOnClickListener(new DetailOnClickListener(entity));
		holder.mEmptyTextView.setVisibility(View.GONE);
		holder.mContentLayout.setVisibility(View.VISIBLE);
		holder.mAppNameTextView.setText(entity.appName);
		holder.mAppVersionTextView.setVisibility(View.GONE);
		holder.mCenterTextView.setVisibility(View.GONE);
		holder.mProgressBar.setVisibility(View.GONE);
		holder.mBottomTextView.setVisibility(View.VISIBLE);
		holder.mBottomTextView.setText("V" + entity.versionName + "/" + NetTool.sizeFormat((int) entity.fileLength));

		setButtonStatus(entity, holder);

		if (entity.heavy > 0) {
			holder.mAuthorityImageview.setVisibility(View.VISIBLE);
		} else {
			holder.mAuthorityImageview.setVisibility(View.GONE);
		}
	}

	private void fillIgnoreChildView(DownloadEntity entity, ChildViewHolder holder) {
		if (entity == null) {
			holder.mEmptyTextView.setVisibility(View.VISIBLE);
			holder.mContentLayout.setVisibility(View.GONE);
			holder.mEmptyTextView.setText(R.string.current_not_ignore_app);
			return;
		}

		holder.mAuthorityImageview.setVisibility(View.GONE);
		holder.mAppNameTextView.setText(entity.appName);
		holder.mEmptyTextView.setVisibility(View.GONE);
		holder.mContentLayout.setVisibility(View.VISIBLE);
		holder.mCenterTextView.setVisibility(View.GONE);

		holder.mAppVersionTextView.setVisibility(View.GONE);

		holder.mBottomTextView.setText("V" + entity.installedVersionName + "/" + NetTool.formatString(entity.installedFileLength / 1048576.0) + "M");
		holder.mProgressBar.setVisibility(View.GONE);

		setButtonStatus(entity, holder);

		holder.mContentLayout.setOnClickListener(new DetailOnClickListener(entity));

	}

	/**
	 * 设置 button 状态
	 * 
	 * @param entity
	 * @param holder
	 */
	private void setButtonStatus(DownloadEntity entity, ChildViewHolder holder) {
		if (entity.downloadType == TYPE_OF_DOWNLOAD) {
			holder.mLongButton.setVisibility(View.GONE);
			switch (entity.getStatus()) {
			case STATUS_OF_PREPARE:
			case STATUS_OF_DOWNLOADING:
				holder.mFirstButton.setVisibility(View.GONE);

				setFirstButtonStyle(holder.mSecondButton, pauseString);
				holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				break;
			case STATUS_OF_EXCEPTION:
			case STATUS_OF_PAUSE:
			case STATUS_OF_PAUSE_ON_EXIT_SYSTEM:
				setSecondButtonStyle(holder.mFirstButton, continueString);
				setFirstButtonStyle(holder.mSecondButton, cancelString);
				holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				holder.mFirstButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				break;
			case STATUS_OF_COMPLETE:
				holder.mFirstButton.setVisibility(View.GONE);

				setFirstButtonStyle(holder.mSecondButton, pauseString);
				holder.mSecondButton.setEnabled(false);
				break;
			default:

				setFirstButtonStyle(holder.mFirstButton, continueString);

				setSecondButtonStyle(holder.mSecondButton, cancelString);
				break;
			}
		} else if (entity.downloadType == TYPE_OF_UPDATE) {
			switch (entity.getStatus()) {
			case STATUS_OF_IGNORE:
				holder.mLongButton.setVisibility(View.VISIBLE);

				holder.mLongButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				break;
			case STATUS_OF_INITIAL:
				holder.mLongButton.setVisibility(View.GONE);

				setFirstButtonStyle(holder.mSecondButton, updateString);

				setSecondButtonStyle(holder.mFirstButton, ignoreString);

				holder.mFirstButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));

				break;
			case STATUS_OF_DOWNLOADING:
			case STATUS_OF_PREPARE:
				holder.mLongButton.setVisibility(View.GONE);

				setSecondButtonStyle(holder.mFirstButton, pauseString);
				setFirstButtonStyle(holder.mSecondButton, cancelString);
				holder.mFirstButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				break;
			case STATUS_OF_EXCEPTION:
			case STATUS_OF_PAUSE:
				holder.mLongButton.setVisibility(View.GONE);

				setSecondButtonStyle(holder.mFirstButton, continueString);
				setFirstButtonStyle(holder.mSecondButton, cancelString);
				holder.mFirstButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
				break;
			case STATUS_OF_COMPLETE:
				holder.mLongButton.setVisibility(View.GONE);
				setSecondButtonStyle(holder.mFirstButton, pauseString);
				setFirstButtonStyle(holder.mSecondButton, cancelString);
				holder.mFirstButton.setEnabled(false);
				holder.mSecondButton.setEnabled(false);
				break;
			}
		} else if (entity.downloadType == TYPE_OF_COMPLETE) {
			holder.mLongButton.setVisibility(View.GONE);
			holder.mSecondButton.setVisibility(View.VISIBLE);
			holder.mFirstButton.setVisibility(View.VISIBLE);

			setCommonButtonStyle(holder.mFirstButton, installString);

			setSecondButtonStyle(holder.mSecondButton, deleteString);

			holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
			holder.mFirstButton.setOnClickListener(new OnDownloadClickListener(entity, holder));

			for (int i = 0; i < rootApkList.size(); i++) {
				if (null != entity.packageName && rootApkList.get(i).equals(entity.packageName)) {
					holder.mFirstButton.setFocusable(false);
					holder.mFirstButton.setFocusableInTouchMode(false);
					holder.mFirstButton.setEnabled(false);
					holder.mFirstButton.setClickable(false);
					holder.mSecondButton.setFocusable(false);
					holder.mSecondButton.setFocusableInTouchMode(false);
					holder.mSecondButton.setEnabled(false);
					holder.mSecondButton.setClickable(false);
					holder.mFirstButton.setText(R.string.installing);
				}
			}

		} else if (entity.downloadType == TYPE_OF_IGNORE) {
			// holder.mLongButton.setText(context
			// .getString(R.string.cancle_ignore));

			setSecondButtonStyle(holder.mLongButton, context.getString(R.string.cancle_ignore));

			holder.mFirstButton.setVisibility(View.GONE);
			holder.mSecondButton.setVisibility(View.GONE);
			holder.mLongButton.setVisibility(View.VISIBLE);
			holder.mLongButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
		}
	}

	/**
	 * 设置 button 样式(黄色)
	 * 
	 * @param mButton
	 * @param text
	 */
	private void setFirstButtonStyle(Button mButton, String text) {
		mButton.setEnabled(true);
		mButton.setVisibility(View.VISIBLE);
		mButton.setBackgroundResource(R.drawable.update_selector);
		mButton.setTextColor(Color.parseColor("#0e567d"));
		mButton.setText(text);
	}

	/**
	 * 设置 button 样式(蓝色)
	 * 
	 * @param mButton
	 * @param text
	 */
	private void setSecondButtonStyle(Button mButton, String text) {
		mButton.setEnabled(true);
		mButton.setVisibility(View.VISIBLE);
		mButton.setBackgroundResource(R.drawable.cancel_selector);
		mButton.setTextColor(Color.parseColor("#7f5100"));
		mButton.setText(text);
	}

	/**
	 * 设置 button 样式(白色)
	 * 
	 * @param mButton
	 * @param text
	 */
	private void setCommonButtonStyle(Button mButton, String text) {
		mButton.setEnabled(true);
		mButton.setVisibility(View.VISIBLE);
		mButton.setText(text);
		mButton.setBackgroundResource(R.drawable.install_button_selector);
		mButton.setTextColor(Color.BLACK);
	}

	private class OnDownloadClickListener implements View.OnClickListener {
		private DownloadEntity entity;
		private ChildViewHolder holder;

		OnDownloadClickListener(DownloadEntity entity, ChildViewHolder holder) {
			this.entity = entity;
			this.holder = holder;
		}

		@Override
		public void onClick(View v) {
			String text = ((TextView) v).getText().toString();
			if (entity.downloadType == TYPE_OF_DOWNLOAD) {
				if (pauseString.equals(text)) {
					// if(entity.canPause()) {
					setFirstButtonStyle(holder.mSecondButton, cancelString);

					setSecondButtonStyle(holder.mFirstButton, continueString);

					holder.mFirstButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
					holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));

					holder.mProgressBar.setVisibility(View.GONE);
					holder.mCenterTextView.setText(R.string.paused);

					entity.setStatus(STATUS_OF_PAUSE);

				} else if (continueString.equals(text)) {
					if (entity.canDownload()) {
						holder.mFirstButton.setVisibility(View.GONE);

						setFirstButtonStyle(holder.mSecondButton, pauseString);
						holder.mSecondButton.setOnClickListener(new OnDownloadClickListener(entity, holder));
						holder.mCenterTextView.setVisibility(View.GONE);
						holder.mProgressBar.setVisibility(View.VISIBLE);
						if (entity.fileLength > 0 && entity.currentPosition > 0) {
							double progress = entity.currentPosition * 100.0 / entity.fileLength;
							holder.mProgressBar.setProgress((int) progress);
						} else {
							holder.mProgressBar.setProgress(0);
						}
						String currentDownloadSize = df.format(entity.currentPosition / 1048576.0);
						String currentTotalSize = df.format(entity.fileLength / 1048576.0);
						holder.mBottomTextView.setText(currentDownloadSize + "M/" + currentTotalSize + "M");

						DownloadUtils.checkDownload(context, entity);
					}
				} else if (cancelString.equals(text)) {
					// entity.setStatus(status_of)
					cancelDownloadEntity(entity);
					DownloadUtils.fillDownloadNotifycation(context, false);

					Bundle bundle = new Bundle();
					bundle.putParcelable(DOWNLOAD_ENTITY, entity);
					sendServiceBroadcastByAction(BROADCAST_ACTION_CANCEL_DOWNLOAD, bundle);

					System.out.println("on download cancel!");
				}
			} else if (entity.downloadType == TYPE_OF_UPDATE) {
				// 当按钮为更新时，点击需要下载此应用更新
				if (updateString.equals(text)) {
					setSecondButtonStyle(holder.mFirstButton, pauseString);
					setFirstButtonStyle(holder.mSecondButton, cancelString);
					holder.mProgressBar.setVisibility(View.VISIBLE);

					if (entity.fileLength > 0 && entity.currentPosition > 0) {
						double progress = entity.currentPosition * 100.0 / entity.fileLength;
						holder.mProgressBar.setProgress((int) progress);
					} else {
						holder.mProgressBar.setProgress(0);
					}

					DownloadUtils.checkDownload(context, entity);
				} else if (ignoreString.equals(text)) { // 当按钮为忽略时
					// entity.downloadType = TYPE_OF_IGNORE;
					// Bundle bundle=new Bundle();
					// bundle.putParcelable(DOWNLOAD_ENTITY, entity);
					// sendServiceBroadcastByAction(BROADCAST_ACTION_IGNORE_UPDATE,
					// bundle);

					updateToIgnore(entity);
				} else if (pauseString.equals(text)) { // 当按钮为暂停状态时，点击需暂停此下载
					setSecondButtonStyle(holder.mFirstButton, continueString);
					holder.mProgressBar.setVisibility(View.GONE);
					holder.mCenterTextView.setText(R.string.paused);

					entity.setStatus(STATUS_OF_PAUSE);

					// Bundle bundle=new Bundle();
					// bundle.putParcelable(DOWNLOAD_ENTITY, entity);
					// sendServiceBroadcastByAction(BROADCAST_ACTION_PAUSE_DOWNLOAD,
					// bundle);

				} else if (cancelString.equals(text)) { // 当按钮为取消状态时，点击需要取消此次更新操作，其它相关状态还原
					setFirstButtonStyle(holder.mSecondButton, updateString);
					setSecondButtonStyle(holder.mFirstButton, ignoreString);
					holder.mProgressBar.setVisibility(View.GONE);

					DownloadUtils.fillUpdateAndUpdatingNotifycation(context, false);

					// entity.reset();
					Bundle bundle = new Bundle();
					bundle.putParcelable(DOWNLOAD_ENTITY, entity);
					sendServiceBroadcastByAction(BROADCAST_ACTION_CANCEL_DOWNLOAD, bundle);
				} else if (continueString.equals(text)) { // 当按钮为继续状态时
					setFirstButtonStyle(holder.mSecondButton, cancelString);
					setSecondButtonStyle(holder.mFirstButton, pauseString);
					holder.mCenterTextView.setVisibility(View.GONE);
					holder.mProgressBar.setVisibility(View.VISIBLE);
					if (entity.fileLength > 0 && entity.currentPosition > 0) {
						double progress = entity.currentPosition * 100.0 / entity.fileLength;
						holder.mProgressBar.setProgress((int) progress);
					}

					DownloadUtils.checkDownload(context, entity);
				}
			} else if (entity.downloadType == TYPE_OF_COMPLETE) {
				if (installString.equals(text)) {
					String path = DOWNLOAD_ROOT_PATH + entity.hashCode() + DOWNLOAD_FILE_POST_SUFFIX;
					File file = new File(path);
					if (file.exists()) {
						DownloadUtils.installApk(context, path);
					} else {
						Toast.makeText(context, R.string.download_file_delete_prompt, Toast.LENGTH_SHORT).show();
						deleteWaitInstallApk(entity);
						Bundle bundle = new Bundle();
						bundle.putParcelable(DOWNLOAD_ENTITY, entity);
						sendServiceBroadcastByAction(BROADCAST_ACTION_REMOVE_DOWNLOAD, bundle);
					}
				} else if (deleteString.equals(text)) {
					String path = DOWNLOAD_ROOT_PATH + entity.hashCode() + DOWNLOAD_FILE_POST_SUFFIX;
					boolean flag = DownloadUtils.deleteDownloadFile(path);
					if (flag) {
						deleteWaitInstallApk(entity);
						System.out.println(entity.appName + ", " + entity.downloadType + ", " + entity.getStatus());
						Bundle bundle = new Bundle();
						bundle.putParcelable(DOWNLOAD_ENTITY, entity);
						sendServiceBroadcastByAction(BROADCAST_ACTION_REMOVE_DOWNLOAD, bundle);
					}
				}
			} else if (entity.downloadType == TYPE_OF_IGNORE) {
				deleteIgnore(entity);
				Bundle bundle = new Bundle();
				bundle.putParcelable(DOWNLOAD_ENTITY, entity);
				sendServiceBroadcastByAction(BROADCAST_ACTION_CANCEL_IGNORE, bundle);
			}
		}
	}

	private class DetailOnClickListener implements OnClickListener {
		private DownloadEntity entity;

		DetailOnClickListener(DownloadEntity entity) {
			this.entity = entity;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (entity != null) {
				if (entity.downloadType != TYPE_OF_IGNORE) {
					Intent intent = new Intent(context, ApkDetailActivity.class);
					Bundle bundle = new Bundle();
					ApkItem item = new ApkItem();
					item.appId = entity.appId;
					item.category = entity.category;
					item.appName = entity.appName;
					item.apkUrl = entity.url;
					item.appIconUrl = entity.iconUrl;
					bundle.putParcelable("apkItem", item);
					intent.putExtras(bundle);
					context.startActivity(intent);
				}
			} else {
				AndroidUtils.showToast(context, R.string.not_found_soft_detail);
			}
		}

	}

	/**
	 * 删除刷新消息
	 */
	private void removeMessage() {
		if (mHandler != null && mHandler.hasMessages(EVENT_REFRESH_DATA)) {
			mHandler.removeMessages(EVENT_REFRESH_DATA);
		}
	}

	/**
	 * 发送刷新消息
	 */
	private void sendMessage() {
		if (mHandler != null && !mHandler.hasMessages(EVENT_REFRESH_DATA)) {
			mHandler.sendEmptyMessage(EVENT_REFRESH_DATA);
		}
	}

	/**
	 * 移除下载完成的应用至待安装
	 * 
	 * @param entity
	 */
	private void removeCompleteToInstall(DownloadEntity entity) {
		System.out.println(".........test0522....removeCompleteToInstall..." + entity.downloadType);
		boolean removed = false;
		removeMessage();
		for (int i = 0; i < childList.size(); i++) {
			int j = 0;
			for (; j < childList.get(i).size(); j++) {
				DownloadEntity d = childList.get(i).get(j);
				if (d.appId == entity.appId && d.category == entity.category) {
					System.out.println("removeCompleteToInstall:" + entity.appName + ", " + mHandler.hasMessages(2));

					childList.get(i).remove(j);
					if (childList.size() > 3 && i == 0 && childList.get(i).size() == 0) {
						groupList.remove(i);
						childList.remove(i);
					}
					entity.downloadType = TYPE_OF_COMPLETE;
					childList.get(childList.size() - 2).add(entity);

					removed = true;
					break;
				}
			}
			if (removed) {
				System.out.println("removed");
				break;
			}
		}
		sendMessage();
	}

	/**
	 * 将更新列表应用移至忽略
	 */
	private void updateToIgnore(DownloadEntity entity) {
		int position = 0;

		removeMessage();

		if (childList.size() > 3) {
			position = 1;
		}
		for (int i = 0; i < childList.get(position).size(); i++) {
			DownloadEntity d = childList.get(position).get(i);
			if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
				if (d.downloadType == TYPE_OF_UPDATE) {
					childList.get(position).remove(i);
					d.downloadType = TYPE_OF_IGNORE;
					childList.get(childList.size() - 1).add(d);
					DownloadUtils.fillUpdateNotifycation(context);
				}
				break;
			}
		}
		sendMessage();
	}

	/**
	 * 移除掉已忽略应用
	 * 
	 * @param entity
	 */
	private void deleteIgnore(DownloadEntity entity) {
		removeMessage();
		int position = childList.size() - 1;
		for (int i = 0; i < childList.get(position).size(); i++) {
			DownloadEntity d = childList.get(position).get(i);
			if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
				childList.get(position).remove(i);
				break;
			}
		}
		sendMessage();
	}

	/**
	 * 从待安装的列表中删除应用
	 * 
	 * @param entity
	 */
	private void deleteWaitInstallApk(DownloadEntity entity) {
		removeMessage();
		List<DownloadEntity> list = childList.get(childList.size() - 2);
		for (int i = 0; i < list.size(); i++) {
			DownloadEntity mDownloadEntity = list.get(i);
			if (mDownloadEntity.packageName.equals(entity.packageName) && mDownloadEntity.versionCode == entity.versionCode) {
				System.out.println("remove dasdasdasdasd");
				list.remove(i);
				break;
			}
		}
		sendMessage();

		// DownloadUtils.fillWaitInstallNotifycation(context);
	}

	/**
	 * 取消下载
	 * 
	 * @param entity
	 * @return
	 */
	private boolean cancelDownloadEntity(DownloadEntity entity) {
		locked = true;
		removeMessage();
		for (int i = 0; i < childList.get(0).size(); i++) {
			DownloadEntity d = childList.get(0).get(i);
			if (d.appId == entity.appId && d.category == entity.category) {
				childList.get(0).remove(i);
				if (childList.get(0).size() == 0) {
					childList.remove(0);
					groupList.remove(0);
				}
				break;
			}
		}
		locked = false;
		sendMessage();
		return false;
	}

	/**
	 * 发送广播
	 * 
	 * @param action
	 * @param bundle
	 */
	private void sendServiceBroadcastByAction(String action, Bundle bundle) {
		Intent intent = new Intent(action);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.sendBroadcast(intent);
	}

	public void refreshAdapter() {
		notifyDataSetChanged();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return childList == null ? 0 : childList.get(groupPosition).size() == 0 ? 1 : childList.get(groupPosition).size();
		// return childList==null?0:childList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return groupList == null ? 0 : groupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
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

			holder.mButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// sendServiceBroadcastByAction(
					// BROADCAST_ACTION_ONEKEY_UPDATE, null);
					DownloadUtils.checkOneKeyDownload(context, null);
				}
			});

			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		String str = null;
		if (groupList.size() > groupPosition) {
			str = groupList.get(groupPosition);
			holder.mTextView.setText(str);
		}
		if (isExpanded) {
			holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.pic_down));
		} else {
			holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.pic_up));
		}
		holder.mImageView.setVisibility(View.VISIBLE);
		if (downloadingString.equals(str)) {
			holder.mButton.setVisibility(View.GONE);
			holder.mImageView.setVisibility(View.VISIBLE);
		} else if (updatingString.equals(str)) {
			holder.mButton.setVisibility(View.VISIBLE);
			holder.mImageView.setVisibility(View.GONE);
		} else {
			holder.mButton.setVisibility(View.GONE);
			holder.mImageView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	/**
	 * 是否能点击伸缩第一个条目
	 * 
	 * @param groupPostion
	 * @return
	 */
	public boolean canClickGroup(int groupPosition) {
		if (!locked && groupList.size() > 3 && downloadingString.equals(groupList.get(groupPosition))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private static final class ChildViewHolder {
		ImageView mImageView; // 应用icon
		TextView mAppNameTextView; // 应用名称
		TextView mAppVersionTextView; // 应用版本名称
		TextView mCenterTextView; // 下载指示(暂停等)
		TextView mBottomTextView; // 底部文字
		TextView mEmptyTextView; // 当没有可更新应用和安装应用时显示此
		ProgressBar mProgressBar; // 下载进度条
		Button mFirstButton, mSecondButton, mLongButton;
		View mContentLayout;
		ImageView mAuthorityImageview;
	}

	private static final class GroupViewHolder {
		TextView mTextView;
		Button mButton;
		ImageView mImageView;
	}

	@Override
	public void onDownloadChanged(DownloadEntity entity) {
		locked = true;
		removeCompleteToInstall(entity);
		locked = false;
	}

	@Override
	public void onUpdateListDone(List<DownloadEntity> list) {
		removeMessage();
		int position = 0;
		if (childList.size() > 3) {
			position = 1;
		}
		childList.get(position).addAll(list);
		sendMessage();
	}

	/**
	 * 将新更新添加至列表中
	 * 
	 * @param entity
	 */
	private void addUpdate(DownloadEntity entity) {
		removeMessage();

		List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
		for (int i = 0; i < downloadList.size(); i++) {
			DownloadEntity d = downloadList.get(i);
			if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
				int position = 0;
				if (childList.size() > 3) {
					position = 1;
				}
				childList.get(position).add(d);

				DownloadUtils.fillUpdateNotifycation(context);
				break;
			}
		}
		sendMessage();
	}

	/**
	 * 卸载后需将可更新应用移除
	 */
	private void deleteUpdateDownloadEntity(DownloadEntity entity) {
		removeMessage();
		int position = 0;
		if (childList.size() == 4) {
			position = 1;
		}
		for (int i = 0; i < childList.get(position).size(); i++) {
			DownloadEntity d = childList.get(position).get(i);
			if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
				childList.get(position).remove(i);
				break;
			}
		}
		sendMessage();
	}

	private void updateInstallFailStatus(String apkPackageName) {
		removeMessage();
		System.out.println("...........test0522...0....." + apkPackageName);
		System.out.println("...........test0522....0.0.1......." + childList.size());
		if (childList.size() == 3) {
			System.out.println("...........test0522...0..3......" + childList.get(1).size());
			for (int i = 0; i < childList.get(1).size(); i++) {
				DownloadEntity downloadEntity = childList.get(1).get(i);
				System.out.println(".............test0522........0.1.." + downloadEntity.packageName);
				if (apkPackageName.equals(downloadEntity.packageName)) {
					downloadEntity.downloadType = TYPE_OF_COMPLETE;
					System.out.println("...........test0522........");
					break;
				}
			}
		} else {
			for (int i = 0; i < childList.get(2).size(); i++) {
				DownloadEntity downloadEntity = childList.get(2).get(i);
				if (apkPackageName.equals(downloadEntity.packageName)) {
					downloadEntity.downloadType = TYPE_OF_COMPLETE;
					System.out.println("...........test0522.......1.");
					break;
				}
			}
		}
		sendMessage();
	}

	/**
	 * 添加至正在下载列表
	 * 
	 * @param entity
	 */
	private void addDownloadList(DownloadEntity entity) {
		removeMessage();
		List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
		for (int i = 0; i < downloadList.size(); i++) {
			DownloadEntity d = downloadList.get(i);
			if (d.packageName.equals(entity.packageName) && d.versionCode == entity.versionCode) {
				System.out.println("==========" + groupList.size());
				if (groupList.size() > 3) {
					childList.get(0).add(d);
					System.out.println("=========" + childList.get(childList.size() - 1).size());
				} else {
					groupList.add(0, context.getString(R.string.transferapk));
					List<DownloadEntity> downloadingList = new ArrayList<DownloadEntity>();
					downloadingList.add(d);
					childList.add(0, downloadingList);
				}
				break;
			}
		}
		sendMessage();
	}

	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BROADCAST_ACTION_COMPLETE_DOWNLOAD.equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DOWNLOAD_ENTITY);
					locked = true;
					removeCompleteToInstall(entity);
					locked = false;
				}
			} else if (BROADCAST_ACTION_ADD_UPDATE.equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DOWNLOAD_ENTITY);
					addUpdate(entity);
				}
			} else if (BROADCAST_ACTION_INSTALL_COMPLETE.equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DOWNLOAD_ENTITY);
					deleteWaitInstallApk(entity);
					for (int i = 0; i < rootApkList.size(); i++) {
						if (rootApkList.get(i).equals(entity.packageName)) {
							rootApkList.remove(entity.packageName);
						}
					}
				}
			} else if (BROADCAST_ACTION_REMOVE_COMPLETE.equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DOWNLOAD_ENTITY);
					deleteUpdateDownloadEntity(entity);
				}
			} else if (BROADCAST_ACTION_ADD_DOWNLOAD_LIST.equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					DownloadEntity entity = bundle.getParcelable(DOWNLOAD_ENTITY);
					addDownloadList(entity);
				}
			} else if (BROADCAST_ACTION_UPDATE_DATA_MERGE_DONE.equals(intent.getAction())) {

			} else if (BROADCAST_ACTION_UPDATE_ROOTSTATUS.equals(intent.getAction())) {
				updateInstallFailStatus(intent.getStringExtra(DOWNLOAD_APKPACKAGENAME));
			}
		}
	};

	@Override
	public void onRemoveDownload(DownloadEntity entity) {
		removeMessage();
		/*
		 * if(childList.size()>3) { int count=childList.get(0).size(); for(int
		 * i=0;i<count;i++) { DownloadEntity d=childList.get(0).get(i); if(d.) }
		 * }
		 */
		cancelDownloadEntity(entity);
		sendMessage();
	}
}
