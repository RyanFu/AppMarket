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
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.cache.FileService;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DownloadUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.DownloadEntity;

/**
 * 搜索结果列表页listview适配器
 * 
 * @author yvon
 * 
 */
public class SearchResultAdapter extends ListBaseAdapter implements AConstDefine {

	private Context cxt;
	private List<ApkItem> data;
	private int descLen;
	private Bitmap mDefaultBitmap;
	private String updateTimeStr;

	public SearchResultAdapter(Context cxt, List<ApkItem> data, boolean isRemoteImage) {
		super(cxt);
		this.cxt = cxt;
		this.data = data;
		descLen = 80;
		updateTimeStr = cxt.getResources().getString(R.string.update_time_txt);
		this.isDisplay = isRemoteImage;
		try {
			mDefaultBitmap = BitmapFactory.decodeResource(cxt.getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(cxt);
			convertView = inflater.inflate(R.layout.item_search_result, null);
			holder = new ViewHolder();
			holder.mAppIcon = (ImageView) convertView.findViewById(R.id.result_icon);
			holder.ivdongji_searchresult = (ImageView) convertView.findViewById(R.id.ivdongji_searchresult);
			holder.mAppName = (TextView) convertView.findViewById(R.id.result_name);
			holder.mAppDate = (TextView) convertView.findViewById(R.id.result_date);
			holder.mAppInstall = (TextView) convertView.findViewById(R.id.result_install);
			holder.mAppDesc = (TextView) convertView.findViewById(R.id.result_description);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final ApkItem apkItem = data.get(position);
		if (apkItem.heavy > 0) {
			holder.ivdongji_searchresult.setVisibility(View.VISIBLE);
		} else {
			holder.ivdongji_searchresult.setVisibility(View.GONE);
		}

		holder.mAppName.setText(apkItem.appName);
		try {
			FileService.getBitmap(apkItem.appIconUrl, holder.mAppIcon, mDefaultBitmap, isDisplay);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		holder.mAppDate.setText(updateTimeStr + apkItem.updateDate);
		holder.mAppDesc.setText(limitDescLength(apkItem.discription));
		displayApkStatus(holder.mAppInstall, apkItem.status);
		holder.mAppInstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (apkItem.status == STATUS_APK_UNINSTALL || apkItem.status == STATUS_APK_UNUPDATE) {
					int[] location = new int[2];
					holder.mAppIcon.getLocationOnScreen(location);
					DownloadUtils.checkDownload(context, apkItem);
				} else {
					Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
					DownloadEntity entity = new DownloadEntity(apkItem);
					Bundle bundle = new Bundle();
					bundle.putParcelable(AConstDefine.DOWNLOAD_ENTITY, entity);
					intent.putExtras(bundle);
					context.sendBroadcast(intent);
				}
			}
		});
		return convertView;
	}

	/**
	 * 限制软件介绍内容的长度
	 * 
	 * @param description
	 * @return
	 */
	private String limitDescLength(String description) {
		if (description.length() > descLen) {
			return description.substring(0, descLen) + "...";
		}
		return description;
	}

	private static class ViewHolder {
		ImageView mAppIcon, ivdongji_searchresult;
		TextView mAppName, mAppDate, mAppDesc;
		TextView mAppInstall;
	}

	@Override
	public List<ApkItem> getItemList() {
		return data;
	}

	public int getDescLen() {
		return descLen;
	}

	public void setDescLen(int descLen) {
		this.descLen = descLen;
	}

	public void addList(List<ApkItem> list) {
		if (list != null && list.size() > 0) {
			data.addAll(list);
			notifyDataSetChanged();
		}
	}
}
