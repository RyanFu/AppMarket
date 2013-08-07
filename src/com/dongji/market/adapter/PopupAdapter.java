package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.pojo.ChannelListInfo;

public class PopupAdapter extends BaseAdapter {
	private Context context;
	private List<ChannelListInfo> list;
	
	public PopupAdapter(Context context, List<ChannelListInfo> list) {
		this.context=context;
		this.list=list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list==null?0:this.list.size();
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
		TextView mTextView;
		if(convertView==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.item_popup_textview, null);
		}
		mTextView=(TextView)convertView;
		mTextView.setText(list.get(position).name);
		return mTextView;
	}
}
