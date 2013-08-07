package com.dongji.market.adapter;

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
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.cache.FileService;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;

public class ListMultiTemplateAdapter extends ListBaseAdapter {
	private List<ApkItem> list;
	private Bitmap mDefaultBitmap;
	
	
	public ListMultiTemplateAdapter(Context context, List<ApkItem> list) {
		super(context);
		this.list=list;
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
			this.list.clear();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0 : list.size() == 0 ? 0
				: list.size() % 2 == 0 ? list.size() / 2 : list.size() / 2 + 1;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
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
			convertView=LayoutInflater.from(context).inflate(R.layout.item_multi_list, null);
			holder=new ViewHolder();
			holder.mLeftLayout=convertView.findViewById(R.id.leftlayout);
			holder.mLeftAppIconImageView=(ImageView)convertView.findViewById(R.id.leftIconImageview);
			holder.mLeftAppNameTextView=(TextView)convertView.findViewById(R.id.leftappnametextview);
			holder.mLeftAppVersionTextView=(TextView)convertView.findViewById(R.id.leftappversiontextview);
			holder.mLeftAppOwnerTextView=(TextView)convertView.findViewById(R.id.leftappownertextview);
			holder.mLeftAppSizeTextView=(TextView)convertView.findViewById(R.id.leftappsizetextview);
			holder.mLeftAppInstallNumTextView=(TextView)convertView.findViewById(R.id.leftappinstallnumtextview);
			holder.mLeftInstallTextView=(TextView)convertView.findViewById(R.id.leftinstalltextview);
			
			holder.mRightLayout=convertView.findViewById(R.id.rightlayout);
			holder.mRightAppIconImageView=(ImageView)convertView.findViewById(R.id.rightIconImageview);
			holder.mRightAppNameTextView=(TextView)convertView.findViewById(R.id.rightappnametextview);
			holder.mRightAppVersionTextView=(TextView)convertView.findViewById(R.id.rightappversiontextview);
			holder.mRightAppOwnerTextView=(TextView)convertView.findViewById(R.id.rightappownertextview);
			holder.mRightAppSizeTextView=(TextView)convertView.findViewById(R.id.rightappsizetextview);
			holder.mRightAppInstallNumTextView=(TextView)convertView.findViewById(R.id.rightappinstallnumtextview);
			holder.mRightInstallTextView=(TextView)convertView.findViewById(R.id.rightinstalltextview);
			convertView.setTag(holder);
		}else holder=(ViewHolder)convertView.getTag();
		int newposition=position*2;
		final ApkItem leftItem=list.get(newposition);
		final ApkItem rightItem=list.get(newposition+1);
		holder.mLeftLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(context, ApkDetailActivity.class);
				Bundle bundle=new Bundle();
				bundle.putParcelable("apkItem", leftItem);
				intent.putExtras(bundle);
				context.startActivity(intent);
			}
		});
		holder.mLeftAppNameTextView.setText(leftItem.appName);
		holder.mLeftAppVersionTextView.setText("v"+leftItem.version);
		holder.mLeftAppOwnerTextView.setText(leftItem.company);
		holder.mLeftAppSizeTextView.setText(context.getString(R.string.app_size)+leftItem.fileSize);
		holder.mLeftAppInstallNumTextView.setText(context.getString(R.string.detail_installCount2)+DJMarketUtils.convertionInstallNumber(context, leftItem.downloadNum));
		displayApkStatus(holder.mLeftInstallTextView, leftItem.status);
		holder.mLeftInstallTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				checkDownload(leftItem, holder.mLeftInstallTextView);
				DJMarketUtils.checkDownload(context, leftItem, holder.mLeftInstallTextView, ListMultiTemplateAdapter.this, null);
			}
		});
		holder.mRightLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(context, ApkDetailActivity.class);
				Bundle bundle=new Bundle();
				bundle.putParcelable("apkItem", rightItem);
				intent.putExtras(bundle);
				context.startActivity(intent);
			}
		});
		holder.mRightAppNameTextView.setText(rightItem.appName);
		holder.mRightAppVersionTextView.setText("v"+rightItem.version);
		holder.mRightAppOwnerTextView.setText(rightItem.company);
		holder.mRightAppSizeTextView.setText(context.getString(R.string.app_size)+rightItem.fileSize);
		holder.mRightAppInstallNumTextView.setText(context.getString(R.string.detail_installCount)+DJMarketUtils.convertionInstallNumber(context, rightItem.downloadNum));
		displayApkStatus(holder.mRightInstallTextView, rightItem.status);
		holder.mRightInstallTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				checkDownload(rightItem, holder.mRightInstallTextView);
				DJMarketUtils.checkDownload(context, rightItem, holder.mRightInstallTextView, ListMultiTemplateAdapter.this, null);
			}
		});
		try{
			FileService.getBitmap(leftItem.appIconUrl, holder.mLeftAppIconImageView, mDefaultBitmap, 0);
			FileService.getBitmap(rightItem.appIconUrl, holder.mRightAppIconImageView, mDefaultBitmap, 0);
		} catch (OutOfMemoryError e) {
			if(mDefaultBitmap!=null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		return convertView;
	}
	
	
	private static class ViewHolder {
		ImageView mLeftAppIconImageView, mRightAppIconImageView;
		TextView mLeftAppNameTextView, mLeftAppVersionTextView,
				mLeftAppLanguageTextView, mLeftAppOwnerTextView,
				mLeftAppSizeTextView, mLeftAppInstallNumTextView,
				mLeftInstallTextView, mRightAppNameTextView,
				mRightAppVersionTextView, mRightAppLanguageTextView,
				mRightAppOwnerTextView, mRightAppSizeTextView,
				mRightAppInstallNumTextView, mRightInstallTextView;
		View mLeftLayout, mRightLayout;
	}


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
	}
}
