package com.dongji.market.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.BackupOrRestoreActivity;
import com.dongji.market.pojo.InstalledAppInfo;

public class ChooseToBackupAdapter extends BaseAdapter {

	Context context;
	List<InstalledAppInfo> data;
	LayoutInflater inflater;
	int curItem;

	List<Integer> flag_isCheck;
	Handler handler;

	public ChooseToBackupAdapter(Context context, List<InstalledAppInfo> data,
			Handler handler) {
		super();
		this.context = context;
		this.data = data;
		inflater = LayoutInflater.from(context);
		this.handler = handler;
	}

	public void addData(InstalledAppInfo info) {
		data.add(info);
		flag_isCheck = new ArrayList<Integer>();
		for (int i = 0; i < data.size(); i++) {
			flag_isCheck.add(1);
		}
		notifyDataSetChanged();
	}

	public void clear() {
		data.clear();
	}

	public List<Integer> getCheckdList() {
		return flag_isCheck;
	}

	// public void setData(List<InstalledAppInfo> data) {
	// this.data = data;
	// notifyDataSetChanged();
	// }

	public void setAllChecked(boolean isChecked) {
		if (flag_isCheck != null) {
			for (int i = 0; i < flag_isCheck.size(); i++) {
				flag_isCheck.set(i, isChecked ? 1 : 0);
			}
			notifyDataSetChanged();
		}
	}

	public int getCount() {
		return data == null ? 0 : data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	// public void reflash() {
	// data.remove(curItem);
	// notifyDataSetChanged();
	// }

	public void removeItemByPosition(int position) {
		if (data != null && data.size() > position) {
			data.remove(position);
			if (flag_isCheck != null && flag_isCheck.size() > position) {
				flag_isCheck.remove(position);
			}
		}
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_choosetobackup,
					null);
			holder = new ViewHolder();
			holder.mIconImageView = (ImageView) convertView
					.findViewById(R.id.app_icon);
			holder.mNameTextView = (TextView) convertView
					.findViewById(R.id.app_name);
			holder.mVersionTextView = (TextView) convertView
					.findViewById(R.id.app_version);
			holder.mSizeTextView = (TextView) convertView
					.findViewById(R.id.app_size);
			holder.cbChoosetobackup = (CheckBox) convertView
					.findViewById(R.id.cbChoosetobackup);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final InstalledAppInfo info = data.get(position);
		holder.mIconImageView.setImageDrawable(info.getIcon());
		holder.mNameTextView.setText(info.getName());
		holder.mVersionTextView.setText("V" + info.getVersion());
		holder.mSizeTextView.setText("/" + info.getSize());
		if (flag_isCheck.get(position) == 0) {
			holder.cbChoosetobackup.setChecked(false);
		} else {
			holder.cbChoosetobackup.setChecked(true);
		}
		holder.cbChoosetobackup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					flag_isCheck.set(position, 1);
				} else {
					flag_isCheck.set(position, 0);
				}
				handler.sendEmptyMessage(BackupOrRestoreActivity.EVENT_CHECKCHANGE);
			}
		});
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v.findViewById(R.id.cbChoosetobackup);
				if (flag_isCheck.get(position) == 0) {
					flag_isCheck.set(position, 1);
					cb.setChecked(true);
				} else {
					flag_isCheck.set(position, 0);
					cb.setChecked(false);
				}
				handler.sendEmptyMessage(BackupOrRestoreActivity.EVENT_CHECKCHANGE);
			}
		});
		return convertView;
	}

	private static class ViewHolder {
		ImageView mIconImageView;
		TextView mNameTextView;
		TextView mVersionTextView;
		TextView mSizeTextView;
		CheckBox cbChoosetobackup;
	}
}
