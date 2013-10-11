package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.cache.FileService;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;

/**
 * 专题列表页listview适配器
 * 
 * @author yvon
 * 
 */
public class ThemeListSingleTemplateAdapter extends ListBaseAdapter implements AConstDefine {
	private List<ApkItem> list;
	private Bitmap mDefaultBitmap;

	public ThemeListSingleTemplateAdapter(Context context, List<ApkItem> list, boolean isRemoteImage) {
		super(context);
		this.list = list;
		this.isDisplay = isRemoteImage;
		try {
			mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_theme_single_list, null);
			holder = new ViewHolder();
			holder.mAppIconImageView = (ImageView) convertView.findViewById(R.id.iconImageview);
			holder.mAppNameTextView = (TextView) convertView.findViewById(R.id.appnametextview);
			holder.mAppSizeTextView = (TextView) convertView.findViewById(R.id.appsizetextview);
			holder.mAppInstallNumTextView = (TextView) convertView.findViewById(R.id.appinstallnumtextview);
			holder.mInstallTextView = (Button) convertView.findViewById(R.id.installtextview);
			holder.mAuthorityImageview = (ImageView) convertView.findViewById(R.id.authorityimageview);
			holder.mAppReviewTextview = (TextView) convertView.findViewById(R.id.appreviewtextview);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		final ApkItem item = list.get(position);
		holder.mAppNameTextView.setText(item.appName);
		holder.mAppReviewTextview.setText(context.getString(R.string.app_review) + item.comment);
		holder.mAppSizeTextView.setText(context.getString(R.string.app_size) + item.apkSize);
		holder.mAppInstallNumTextView.setText(context.getString(R.string.detail_installCount2) + DJMarketUtils.convertionInstallNumber(context, item.downloadNum));
		displayApkStatus(holder.mInstallTextView, item.status);
		holder.mInstallTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (item.status == STATUS_APK_UNINSTALL || item.status == STATUS_APK_UNUPDATE) {
					DownloadUtils.checkDownload(context, item, holder.mInstallTextView);
				} else {
					Intent intent = new Intent(DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
					DownloadEntity entity = new DownloadEntity(item);
					Bundle bundle = new Bundle();
					bundle.putParcelable(DownloadConstDefine.DOWNLOAD_ENTITY, entity);
					intent.putExtras(bundle);
					context.sendBroadcast(intent);
					if (entity.downloadType == DownloadConstDefine.TYPE_OF_DOWNLOAD) {
						DownloadUtils.fillDownloadNotifycation(context, false);
					} else if (entity.downloadType == DownloadConstDefine.TYPE_OF_UPDATE) {
						DownloadUtils.fillUpdateAndUpdatingNotifycation(context, false);
					}
				}
			}
		});
		if (item.heavy > 0) {
			holder.mAuthorityImageview.setVisibility(View.VISIBLE);
		} else {
			holder.mAuthorityImageview.setVisibility(View.GONE);
		}
		try {
			FileService.getBitmap(item.appIconUrl, holder.mAppIconImageView, mDefaultBitmap, isDisplay);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView mAppIconImageView, mAuthorityImageview;
		TextView mAppNameTextView, mAppSizeTextView, mAppInstallNumTextView, mAppReviewTextview;
		Button mInstallTextView;
	}

	@Override
	public List<ApkItem> getItemList() {
		return list;
	}

	public void addList(List<ApkItem> list) {
		if (list != null && list.size() > 0) {
			this.list.addAll(list);
			notifyDataSetChanged();
		}
	}

	public void resetList() {
		if (list != null && list.size() > 0) {
			list.clear();
		}
	}

	public ApkItem getApkItemByPosition(int position) {
		if (list != null && list.size() > 0) {
			return list.get(position);
		}
		return null;
	}

}
