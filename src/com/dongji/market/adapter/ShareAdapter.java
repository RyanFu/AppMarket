package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.pojo.ShareItem;

public class ShareAdapter extends BaseAdapter {

	private Context context;
	private List<ShareItem> data;
	
	public ShareAdapter() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ShareAdapter(Context context, List<ShareItem> data) {
		super();
		this.context = context;
		this.data = data;
	}
	
	public void setData(List<ShareItem> data) {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_share, null);
			holder.mIconImage = (ImageView) convertView.findViewById(R.id.share_image);
			holder.mTextView = (TextView) convertView.findViewById(R.id.share_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mIconImage.setImageDrawable(data.get(position).getShareIcon());
		holder.mTextView.setText(data.get(position).getShareName());
		return convertView;
	}
	
	private static class ViewHolder {
		ImageView mIconImage;
		TextView mTextView;
	}

}
