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

/**
 * GalleryDailog适配器
 * 
 * @author yvon
 * 
 */
public class GalleryDetailAdapter extends BaseAdapter {
	private Context context;
	private List<String> urlArr;
	private Bitmap defaultBitmap;

	public GalleryDetailAdapter(Context context, List<String> urlArr) {
		this.context = context;
		this.urlArr = urlArr;
		try {
			defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.gallery_default);
		} catch (OutOfMemoryError e) {
			if (defaultBitmap != null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return urlArr == null ? 0 : urlArr.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView mImageView = null;
		if (convertView == null) {
			mImageView = new ImageView(context);
			mImageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, Gallery.LayoutParams.FILL_PARENT));
			mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
		} else
			mImageView = (ImageView) convertView;
		try {
			FileService.getBitmap(urlArr.get(position), mImageView, defaultBitmap, 0);
		} catch (OutOfMemoryError e) {
			if (defaultBitmap != null && !defaultBitmap.isRecycled()) {
				defaultBitmap.recycle();
			}
		}
		return mImageView;
	}

}
