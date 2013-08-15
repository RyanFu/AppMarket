package com.dongji.market.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.dongji.market.R;
import com.dongji.market.cache.FileService;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.ApkItem;

public class ImageGalleryAdapter extends BaseAdapter {
	private Context context;
	private List<ApkItem> urlList;
	private Bitmap defaultBitmap;
	private int halfWidth;
	private boolean isDisplay = true;
	
	public ImageGalleryAdapter(Context context, List<ApkItem> urlList) {
		this.context=context;
		this.urlList=urlList;
		initHalfWidth();
		try{
			defaultBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.banner_loding);
		}catch(OutOfMemoryError e) {
			if(defaultBitmap!=null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
	}
	
	private void initHalfWidth() {
		DisplayMetrics dm=AndroidUtils.getScreenSize((Activity)context);
		int num=AndroidUtils.dip2px(context, 5);
		halfWidth=dm.widthPixels-num;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return urlList==null||urlList.size()==0?0:Integer.MAX_VALUE;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position%urlList.size();
	}
	
	public void setDisplayNotify(boolean display) {
		isDisplay=display;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position<0) {
			position+=urlList.size();
		}
		position=position%urlList.size();
		ImageView mImageView=null;
		if(convertView==null) {
			mImageView=new ImageView(context);
			mImageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, //halfWidth
					Gallery.LayoutParams.FILL_PARENT));
			mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
		}else mImageView=(ImageView)convertView;
		mImageView.setTag(urlList.get(position).bannerUrl);
		try{
			FileService.getBitmap(urlList.get(position).bannerUrl, mImageView, defaultBitmap, isDisplay);
		}catch(OutOfMemoryError e) {
			if(defaultBitmap!=null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
		return mImageView;
		/*ViewHolder holder;
		if(convertView==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.item_choiceness_gallery, null);
			holder=new ViewHolder();
			holder.mImageView=(ImageView)convertView.findViewById(R.id.imageview);
			convertView.setTag(holder);
		}else holder=(ViewHolder)convertView.getTag();*/
		
		/*LinearLayout mLinearLayout=null;
		if(convertView==null) {
			mLinearLayout=new LinearLayout(context);
			mLinearLayout.setLayoutParams(new Gallery.LayoutParams(
					Gallery.LayoutParams.FILL_PARENT,
					Gallery.LayoutParams.FILL_PARENT));
			ImageView mImageView=new ImageView(context);
			LinearLayout.LayoutParams mParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
					LinearLayout.LayoutParams.FILL_PARENT);
			mParams.weight=1.0f;
			mParams.rightMargin=10;
			mImageView.setLayoutParams(mParams);
			mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
			ImageView mImageView2=new ImageView(context);
			mImageView2.setLayoutParams(new LinearLayout.LayoutParams(80, 
					LinearLayout.LayoutParams.FILL_PARENT));
			mImageView2.setBackgroundResource(R.drawable.bg_banner);
//			mImageView2.setScaleType(ImageView.ScaleType.FIT_XY);
			mLinearLayout.addView(mImageView, 0);
			mLinearLayout.addView(mImageView2, 1);
			convertView=mLinearLayout;
		}else mLinearLayout=(LinearLayout)convertView;
		
		return mLinearLayout;*/
	}

	/*private static class ViewHolder {
		ImageView mImageView;
	}*/
}
