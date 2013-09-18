package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.dongji.market.R;
import com.dongji.market.cache.FileService;
import com.dongji.market.pojo.ApkItem;

/**
 * Banner ImageGallery 适配器
 * 
 * @author yvon
 * 
 */
public class ImageGalleryAdapter extends BaseAdapter {
	private Context context;
	private List<ApkItem> urlList;
	private Bitmap defaultBitmap;
	private boolean isDisplay = true;

	public ImageGalleryAdapter(Context context, List<ApkItem> urlList) {
		this.context = context;
		this.urlList = urlList;
		try {
			defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner_loding);
		} catch (OutOfMemoryError e) {
			if (defaultBitmap != null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return urlList == null || urlList.size() == 0 ? 0 : Integer.MAX_VALUE;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position % urlList.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < 0) {
			position += urlList.size();
		}
		position = position % urlList.size();
		ImageView mImageView = null;
		if (convertView == null) {
			mImageView = new ImageView(context);
			mImageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, Gallery.LayoutParams.FILL_PARENT));
			mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
		} else
			mImageView = (ImageView) convertView;

		mImageView.setTag(urlList.get(position).bannerUrl);
		try {
			FileService.getBitmap(urlList.get(position).bannerUrl, mImageView, defaultBitmap, isDisplay);
		} catch (OutOfMemoryError e) {
			if (defaultBitmap != null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
		return mImageView;
	}

	public void setDisplayNotify(boolean display) {
		isDisplay = display;
		notifyDataSetChanged();
	}
}
