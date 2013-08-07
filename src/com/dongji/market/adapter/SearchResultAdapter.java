package com.dongji.market.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dongji.market.activity.Search_Result_Activity;
import com.dongji.market.cache.FileService;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;

public class SearchResultAdapter extends ListBaseAdapter implements
		AConstDefine {

	private Context cxt;
	private List<ApkItem> data;
	private int descLen;
	private int loadCount;
	private Bitmap mDefaultBitmap;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private String updateTimeStr;

	public SearchResultAdapter(Context cxt, List<ApkItem> data,
			boolean isRemoteImage) {
		super(cxt);
		this.cxt = cxt;
		this.data = data;
		descLen = 80;
		updateTimeStr=cxt.getResources().getString(R.string.update_time_txt);
		this.isDisplay = isRemoteImage;
		try {
			mDefaultBitmap = BitmapFactory.decodeResource(cxt.getResources(),
					R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}

	public int getDescLen() {
		return descLen;
	}

	public void setDescLen(int descLen) {
		this.descLen = descLen;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		// return data == null ? 0 : data.size() == 0 ? 0
		// : data.size() % 2 == 0 ? data.size() / 2 : data.size() / 2 + 1;
		return data == null ? 0 : data.size();
		// return loadCount;
	}

	public void setCount(int count) {
		loadCount = count;
	}

	public void addList(List<ApkItem> list) {
		if (list != null && list.size() > 0) {
			loadCount = data.size();
			data.addAll(list);
			notifyDataSetChanged();
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(cxt);
			convertView = inflater.inflate(R.layout.item_search_result, null);
			holder = new ViewHolder();
			holder.mAppIcon = (ImageView) convertView
					.findViewById(R.id.result_icon);
			holder.ivdongji_searchresult = (ImageView) convertView
			.findViewById(R.id.ivdongji_searchresult);
			holder.mAppName = (TextView) convertView
					.findViewById(R.id.result_name);
			holder.mAppDate = (TextView) convertView
					.findViewById(R.id.result_date);
			holder.mAppInstall = (TextView) convertView
					.findViewById(R.id.result_install);
			holder.mAppDesc = (TextView) convertView
					.findViewById(R.id.result_description);
			holder.mLanguageImageView = (ImageView) convertView
					.findViewById(R.id.languageimageview);
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
		
		
		// holder.mAppIcon
		// .setImageDrawable(getIcon(apkItem.appIconUrl));
		holder.mAppName.setText(apkItem.appName);
		try {
			FileService.getBitmap(apkItem.appIconUrl, holder.mAppIcon,
					mDefaultBitmap, isDisplay);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		holder.mAppDate.setText(updateTimeStr+apkItem.updateDate);
		holder.mAppDesc.setText(limitDescLength(apkItem.discription));
		displayApkStatus(holder.mAppInstall, apkItem.status);
//		setLanguageType(apkItem.language, holder.mAppType,
//				holder.mLanguageImageView);
		holder.mAppInstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (apkItem.status == STATUS_APK_UNINSTALL
						|| apkItem.status == STATUS_APK_UNUPDATE) {
					int[] location = new int[2];
					holder.mAppIcon.getLocationOnScreen(location);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("X", location[0]);
					map.put("Y", location[1]);
					map.put("icon", holder.mAppIcon.getDrawable());
					// DJMarketUtils.checkDownload(context, apkItem,
					// holder.mAppInstall, SearchResultAdapter.this, map);
					DownloadUtils.checkDownload(context, apkItem,
							holder.mAppInstall, SearchResultAdapter.this, map);
				} else {
					// DJMarketUtils.cancelListDownload(context, apkItem);
					Intent intent = new Intent(
							DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
					DownloadEntity entity = new DownloadEntity(apkItem);
					Bundle bundle = new Bundle();
					bundle.putParcelable(DownloadConstDefine.DOWNLOAD_ENTITY,
							entity);
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

	/*
	 * private Drawable getIcon(String iconUrl) { ByteArrayOutputStream
	 * outputStream = new ByteArrayOutputStream(); Drawable drawable =
	 * cxt.getResources().getDrawable(R.drawable.icon); try { URL url = new
	 * URL(iconUrl); HttpURLConnection conn = (HttpURLConnection)
	 * url.openConnection(); conn.setRequestMethod("GET");
	 * conn.setConnectTimeout(5000); InputStream inputStream =
	 * conn.getInputStream(); int len = 0; byte[] buffer = new byte[1024]; while
	 * ((len = inputStream.read(buffer)) != -1) { outputStream.write(buffer, 0,
	 * len); } outputStream.close(); inputStream.close(); byte[] byteData =
	 * outputStream.toByteArray(); drawable = new
	 * BitmapDrawable(BitmapFactory.decodeByteArray( byteData, 0,
	 * byteData.length)); } catch (Exception e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } return drawable; }
	 */

	private static class ViewHolder {
		ImageView mAppIcon,ivdongji_searchresult, mAppType, mLanguageImageView;
		TextView mAppName, mAppVersion, mAppDate, mAppCorp, mAppDesc;
		TextView mAppInstall;
	}

	// @Override
	// public void download(ApkItem item, TextView mTextView) {
	// // TODO Auto-generated method stub
	// NetTool.onDownloadBtnClick(cxt,
	// new ADownloadApkItem(item, STATUS_OF_DOWNLOADING));
	// displayApkStatus(mTextView, item);
	// }

	@Override
	public List<ApkItem> getItemList() {
		// TODO Auto-generated method stub
		return data;
	}

	@Override
	public void onDownload(ApkItem item, TextView mTextView,
			Map<String, Object> map) {
		if (item.status == STATUS_APK_UNINSTALL) {
			item.status = STATUS_APK_INSTALL;
		} else if (item.status == STATUS_APK_UNUPDATE) {
			item.status = STATUS_APK_UPDATE;
		}
		displayApkStatus(mTextView, item.status);
		// ((Search_Result_Activity)cxt).onStartDownload(map);
	}

}
