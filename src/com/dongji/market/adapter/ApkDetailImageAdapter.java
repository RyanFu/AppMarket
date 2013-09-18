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

/**
 * 应用详情页应用截图gridview适配器
 * @author yvon
 * 
 */
public class ApkDetailImageAdapter extends BaseAdapter {

	private Context context;
	private List<String> data;
	private Bitmap defaultBitmap_gallery;

	public ApkDetailImageAdapter(Context context, List<String> data, Bitmap defaultBitmap_gallery) {
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
		return data.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.item_apkdetailimage_gridview, null);
			holder.mIconImage = (ImageView) convertView.findViewById(R.id.app_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mIconImage.setScaleType(ScaleType.FIT_XY);
		FileService.getBitmap(data.get(position), holder.mIconImage, defaultBitmap_gallery, 0);
		return convertView;
	}

	private static class ViewHolder {
		ImageView mIconImage;
	}

}
