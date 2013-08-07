package com.dongji.market.adapter;

import com.dongji.market.R;
import com.dongji.market.cache.FileService;
import com.dongji.market.helper.AndroidUtils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryImageAdapter extends BaseAdapter {
	private Context context;
	private String[] mImage;
	private Bitmap defaultBitmap;

	public GalleryImageAdapter(Context context,String[] mImage) {
		this.context = context;
		this.mImage=mImage;
		try{
			defaultBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.gallery_default);
		}catch(OutOfMemoryError e) {
			if(defaultBitmap!=null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return mImage.length;
	}

	@Override
	public Object getItem(int position) {
		return mImage[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView=null;
		if(convertView==null) {
			imageView = new ImageView(context);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			int imageWidth=AndroidUtils.dip2px(context, 120.0f);
			int imageHeight=AndroidUtils.dip2px(context, 210.0f);
			imageView.setLayoutParams(new Gallery.LayoutParams(imageWidth,
					imageHeight));
		}else imageView=(ImageView)convertView;
		try{
			FileService.getBitmap(mImage[position], imageView, defaultBitmap, 0);
		}catch(OutOfMemoryError e) {
			if(defaultBitmap!=null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
		return imageView;
	}
}