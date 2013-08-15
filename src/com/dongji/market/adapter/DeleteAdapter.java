package com.dongji.market.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.market.R;

public class DeleteAdapter extends BaseAdapter {
	Context context;
	List<HashMap<String, Object>> data;
	String[] from;
	int[] to;
	LayoutInflater inflater;

	public DeleteAdapter(Context context, List<HashMap<String, Object>> data) {
		super();
		this.context = context;
		this.data = data;
		inflater = LayoutInflater.from(context);
	}
	
	public void addListItem(HashMap<String, Object> map) {
		data.add(map);
	}

	public void setData(List<HashMap<String, Object>> data) {
		this.data = data;
		notifyDataSetChanged();
	}

	public int getCount() {
		return data == null ? 0 : data.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_delete, null);
			holder = new ViewHolder();
			holder.mIconImageView = (ImageView) convertView
					.findViewById(R.id.app_del_icon);
			holder.mNameTextView = (TextView) convertView
					.findViewById(R.id.app_del_name);
			holder.mVersionTextView = (TextView) convertView
					.findViewById(R.id.app_del_version);
			holder.mSizeTextView = (TextView) convertView
					.findViewById(R.id.app_del_size);
			holder.mDateTextView = (TextView) convertView
					.findViewById(R.id.app_del_date);
			holder.mStateTextView = (TextView) convertView
					.findViewById(R.id.app_del_state);
			holder.mDeleteView = (Button) convertView.findViewById(R.id.delete);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		final HashMap<String, Object> map = data.get(position);
		holder.mIconImageView.setImageDrawable((Drawable) map.get("icon"));
		holder.mNameTextView.setText((CharSequence) map.get("name"));
		holder.mVersionTextView.setText((CharSequence) map.get("version"));
		if (map.get("version")
				.toString()
				.equals(context.getResources()
						.getString(R.string.app_destroyed))) {
			holder.mVersionTextView.setTextColor(0xFFFF0000);
		} else {
			holder.mVersionTextView.setTextColor(0xFF858585);
		}
		holder.mSizeTextView.setText((CharSequence) map.get("size"));
		holder.mDateTextView.setText((CharSequence) map.get("date"));
		holder.mStateTextView.setText((CharSequence) map.get("isInstalled"));

		holder.mDeleteView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				deleteApp(map);
			}
		});

		return convertView;
	}

	/**
	 * 删除sdCard中的apk文件，同时从集合中清除
	 * 
	 * @param map
	 */
	private void deleteApp(HashMap<String, Object> map) {
		String appPath = map.get("path").toString();
		File file = new File(appPath);
		if (file.isFile() && file.exists()) {
			boolean flag = file.delete();
			if (flag) {
				data.remove(map);
				notifyDataSetChanged();
			}
			Toast.makeText(context, R.string.del_success, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private static class ViewHolder {
		ImageView mIconImageView;
		TextView mNameTextView;
		TextView mVersionTextView;
		TextView mSizeTextView;
		TextView mStateTextView;
		TextView mDateTextView;
		Button mDeleteView;
	}
}
