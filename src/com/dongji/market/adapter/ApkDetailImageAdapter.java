package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.dongji.market.R;
import com.dongji.market.cache.FileService;

public class ApkDetailImageAdapter extends BaseAdapter {

	private Context context;
	private List<String> data;
	private Bitmap defaultBitmap_gallery;

	public ApkDetailImageAdapter(Context context, List<String> data,
			Bitmap defaultBitmap_gallery) {
		super();
		this.context = context;
		this.data = data;
		this.defaultBitmap_gallery = defaultBitmap_gallery;
	}

	public void setData(List<String> data) {
		this.data = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_apkdetailimage_gridview, null);
			holder.mIconImage = (ImageView) convertView
					.findViewById(R.id.app_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
		// AndroidUtils.dip2px(context, 155), AndroidUtils.dip2px(context,
		// 235));
		// holder.mIconImage.setLayoutParams(ivParams);
		holder.mIconImage.setScaleType(ScaleType.FIT_XY);
		// try {
		// defaultBitmap_gallery = BitmapFactory.decodeResource(
		// context.getResources(), R.drawable.gallery_default);
		//
		// } catch (OutOfMemoryError e) {
		// if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
		// mDefaultBitmap.recycle();
		// }
		// }
		FileService.getBitmap(data.get(position), holder.mIconImage,
				defaultBitmap_gallery, 0);
		return convertView;
	}

	private static class ViewHolder {
		ImageView mIconImage;
	}

}
