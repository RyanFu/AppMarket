package com.dongji.market.adapter;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.BaseActivity;
import com.dongji.market.cache.FileService;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.download.DownloadEntity;
import com.dongji.market.download.DownloadUtils;
import com.dongji.market.download.NetTool;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;

public class ThemeListSingleTemplateAdapter extends ListBaseAdapter implements AConstDefine {
	private List<ApkItem> list;
	private Bitmap mDefaultBitmap;
	
	public ThemeListSingleTemplateAdapter(Context context, List<ApkItem> list, boolean isRemoteImage) {
		super(context);
		this.list=list;
		this.isDisplay=isRemoteImage;
		try{
			mDefaultBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if(mDefaultBitmap!=null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}
	
	public void addList(List<ApkItem> list) {
		if(list!=null && list.size()>0) {
			this.list.addAll(list);
			notifyDataSetChanged();
		}
	}
	
	public void resetList() {
		if(list!=null && list.size()>0) {
			list.clear();
		}
	}
	
	public ApkItem getApkItemByPosition(int position) {
		if(list!=null && list.size()>0) {
			return list.get(position);
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list==null?0:list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.item_theme_single_list, null);
			holder=new ViewHolder();
			holder.mAppIconImageView=(ImageView)convertView.findViewById(R.id.iconImageview);
			holder.mAppNameTextView=(TextView)convertView.findViewById(R.id.appnametextview);
			holder.mAppVersionTextView=(TextView)convertView.findViewById(R.id.appversiontextview);
			holder.mAppLanguageImageView=(ImageView)convertView.findViewById(R.id.languageimageview);
//			holder.mAppOwnerTextView=(TextView)convertView.findViewById(R.id.appownertextview);
			holder.mAppSizeTextView=(TextView)convertView.findViewById(R.id.appsizetextview);
			holder.mAppInstallNumTextView=(TextView)convertView.findViewById(R.id.appinstallnumtextview);
			holder.mInstallTextView=(Button)convertView.findViewById(R.id.installtextview);
			holder.mAppLanguageMultiImageView=(ImageView)convertView.findViewById(R.id.languagemultiimageview);
			holder.mAuthorityImageview=(ImageView)convertView.findViewById(R.id.authorityimageview);
			holder.mAppReviewTextview=(TextView)convertView.findViewById(R.id.appreviewtextview);
			convertView.setTag(holder);
		}else holder=(ViewHolder)convertView.getTag();
		final ApkItem item=list.get(position);
		holder.mAppNameTextView.setText(item.appName);
//		holder.mAppVersionTextView.setText("V"+item.version);
//		holder.mAppOwnerTextView.setText(item.company);
		holder.mAppReviewTextview.setText(context.getString(R.string.app_review)+item.comment);
		holder.mAppSizeTextView.setText(context.getString(R.string.app_size)+item.apkSize);
		holder.mAppInstallNumTextView.setText(context.getString(R.string.detail_installCount2)+DJMarketUtils.convertionInstallNumber(context, item.downloadNum));
//		setLanguageType(item.language, holder.mAppLanguageImageView, holder.mAppLanguageMultiImageView);
		displayApkStatus(holder.mInstallTextView, item.status);
		holder.mInstallTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				checkDownload(item, holder.mInstallTextView);
				if(item.status==STATUS_APK_UNINSTALL || item.status==STATUS_APK_UNUPDATE) {
					int[] location = new int[2];
					holder.mAppIconImageView.getLocationOnScreen(location);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("X", location[0]);
					map.put("Y", location[1]);
					map.put("icon", holder.mAppIconImageView.getDrawable());
					
					DownloadUtils.checkDownload(context, item, holder.mInstallTextView, ThemeListSingleTemplateAdapter.this, map);
//					DJMarketUtils.checkDownload(context, item, holder.mInstallTextView, ListSingleTemplateAdapter.this, map);
				}else {
//					DJMarketUtils.cancelListDownload(context, item);
					Intent intent=new Intent(DownloadConstDefine.BROADCAST_ACTION_CANCEL_DOWNLOAD);
					DownloadEntity entity=new DownloadEntity(item);
					Bundle bundle=new Bundle();
					bundle.putParcelable(DownloadConstDefine.DOWNLOAD_ENTITY, entity);
					intent.putExtras(bundle);
					context.sendBroadcast(intent);
					if(entity.downloadType==DownloadConstDefine.TYPE_OF_DOWNLOAD) {
						DownloadUtils.fillDownloadNotifycation(context, false);
					}else if(entity.downloadType==DownloadConstDefine.TYPE_OF_UPDATE) {
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
		try{
			FileService.getBitmap(item.appIconUrl, holder.mAppIconImageView, mDefaultBitmap, isDisplay);
		} catch (OutOfMemoryError e) {
			if(mDefaultBitmap!=null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView mAppIconImageView, mAppLanguageImageView, mAppLanguageMultiImageView, mAuthorityImageview;
		TextView mAppNameTextView, mAppVersionTextView,
				mAppSizeTextView, mAppInstallNumTextView,mAppReviewTextview;
		Button mInstallTextView;
	}

//	@Override
//	public void download(ApkItem item, TextView mTextView) {
//		NetTool.onDownloadBtnClick(context, new ADownloadApkItem(item, STATUS_OF_DOWNLOADING));
//		if(item.status==STATUS_APK_UNINSTALL) {
//			item.status=STATUS_APK_INSTALL;
//		}else if(item.status==STATUS_APK_UNUPDATE) {
//			item.status=STATUS_APK_UPDATE;
//		}
//		displayApkStatus(mTextView, item);
//	}

	@Override
	public List<ApkItem> getItemList() {
		// TODO Auto-generated method stub
		return list;
	}

	@Override
	public void onDownload(ApkItem item, TextView mTextView, Map<String, Object> map) {
		if(item.status==STATUS_APK_UNINSTALL) {
			item.status=STATUS_APK_INSTALL;
		}else if(item.status==STATUS_APK_UNUPDATE) {
			item.status=STATUS_APK_UPDATE;
		}
		displayApkStatus(mTextView, item.status);
		((BaseActivity)context).onStartDownload(map);
	}
	
}
