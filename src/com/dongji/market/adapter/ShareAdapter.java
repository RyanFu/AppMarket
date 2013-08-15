package com.dongji.market.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;

public class ShareAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<HashMap<String, Object>> shareItems;

	public ShareAdapter(Context context, ArrayList<HashMap<String, Object>> tempArrayList) {
		this.mInflater = LayoutInflater.from(context);
		this.shareItems = tempArrayList;
	}

	@Override
	public int getCount() {
		return shareItems.size();
	}

	@Override
	public Object getItem(int position) {
		return shareItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.share_girdview_item, null);
			viewHolder.iv = (ImageView) convertView.findViewById(R.id.itemImage);
			viewHolder.tv = (TextView) convertView.findViewById(R.id.itemText);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tv.setText(shareItems.get(position).get("name").toString());
		viewHolder.iv.setImageDrawable((Drawable) shareItems.get(position).get("icon"));
		return convertView;
	}

	private static class ViewHolder {
		ImageView iv = null;
		TextView tv = null;
	}
}
