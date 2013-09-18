package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.ChannelListActivity;
import com.dongji.market.cache.FileService;
import com.dongji.market.pojo.ChannelListInfo;

/**
 * 分类列表页listview适配器
 * 
 * @author yvon
 * 
 */
public class ChannelAdapter extends BaseAdapter {
	private List<ChannelListInfo> list;
	private Bitmap mDefaultBitmap;
	private Context context;
	private boolean isDisplay = true;

	public ChannelAdapter(Context context, List<ChannelListInfo> list, boolean isDisplay) {
		this.context = context;
		this.list = list;
		this.isDisplay = isDisplay;// 是否能下载图片
		try {
			mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.channel_default);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size() == 0 ? 0 : list.size() % 2 == 0 ? list.size() / 2 : list.size() / 2 + 1;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_channel_list, null);
			holder = new ViewHolder();
			holder.mLeftLayout = convertView.findViewById(R.id.leftlayout);
			holder.mRightLayout = convertView.findViewById(R.id.rightlayout);
			holder.mLeftImageView = (ImageView) convertView.findViewById(R.id.leftImageView);
			holder.mRightImageView = (ImageView) convertView.findViewById(R.id.rightImageView);
			holder.mLeftTextView = (TextView) convertView.findViewById(R.id.leftTextView);
			holder.mRightTextView = (TextView) convertView.findViewById(R.id.rightTextView);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// 左布局填充数据
		int newposition = position * 2;
		final ChannelListInfo leftChannelInfo = list.get(newposition);
		holder.mLeftTextView.setText(leftChannelInfo.name);
		try {
			FileService.getBitmap(leftChannelInfo.iconUrl, holder.mLeftImageView, mDefaultBitmap, isDisplay);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
		holder.mLeftLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startChannelList(leftChannelInfo);
			}
		});

		// 右布局填充数据
		if (newposition + 1 < list.size()) {
			final ChannelListInfo rightChannelInfo = list.get(newposition + 1);
			holder.mRightTextView.setText(rightChannelInfo.name);
			try {
				FileService.getBitmap(rightChannelInfo.iconUrl, holder.mRightImageView, mDefaultBitmap, isDisplay);
			} catch (OutOfMemoryError e) {
				if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
					mDefaultBitmap.recycle();
				}
			}
			holder.mRightLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startChannelList(rightChannelInfo);
				}
			});
		}
		return convertView;
	}

	private void startChannelList(final ChannelListInfo info) {
		Intent intent = new Intent(context, ChannelListActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("channelListInfo", info);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	private static class ViewHolder {
		View mLeftLayout, mRightLayout;
		ImageView mLeftImageView, mRightImageView;
		TextView mLeftTextView, mRightTextView;
	}

	public void setDisplayNotify(boolean display) {
		isDisplay = display;
		notifyDataSetChanged();
	}
}
