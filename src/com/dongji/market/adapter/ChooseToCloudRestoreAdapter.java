package com.dongji.market.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.dongji.market.cache.FileService;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.ApkItem;

/**
 * 云恢复列表页listview适配器
 * 
 * @author yvon
 * 
 */
public class ChooseToCloudRestoreAdapter extends BaseAdapter {

	private List<ApkItem> data;
	private LayoutInflater inflater;
	private Bitmap mDefaultBitmap;
	private List<Integer> flag_isCheck;
	private Handler handler;

	public ChooseToCloudRestoreAdapter(Context context, List<ApkItem> data, Handler handler) {
		super();
		this.data = data;
		inflater = LayoutInflater.from(context);
		flag_isCheck = new ArrayList<Integer>(data != null ? data.size() : 0);
		this.handler = handler;
		for (int i = 0; i < data.size(); i++) {
			flag_isCheck.add(1);
		}
		try {
			mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_default_icon);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
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
			convertView = inflater.inflate(R.layout.item_list_choosetobackup, null);
			holder = new ViewHolder();
			holder.mIconImageView = (ImageView) convertView.findViewById(R.id.app_icon);
			holder.mNameTextView = (TextView) convertView.findViewById(R.id.app_name);
			holder.mVersionTextView = (TextView) convertView.findViewById(R.id.app_version);
			holder.mSizeTextView = (TextView) convertView.findViewById(R.id.app_size);
			holder.cbChoosetobackup = (CheckBox) convertView.findViewById(R.id.cbChoosetobackup);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final ApkItem apkItem = data.get(position);
		holder.mNameTextView.setText(apkItem.appName);
		holder.mVersionTextView.setText("V" + apkItem.version);
		holder.mSizeTextView.setText("/" + DJMarketUtils.sizeFormat((int) apkItem.fileSize));
		try {
			FileService.getBitmap(apkItem.appIconUrl, holder.mIconImageView, mDefaultBitmap, 0);
		} catch (OutOfMemoryError e) {
			if (mDefaultBitmap != null && !mDefaultBitmap.isRecycled()) {
				mDefaultBitmap.recycle();
			}
		}
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

	public void removeItemByPosition(int position) {
		if (data != null && data.size() > position) {
			data.remove(position);
			if (flag_isCheck != null && flag_isCheck.size() > position) {
				flag_isCheck.remove(position);
			}
		}
	}

	public List<Integer> getCheckdList() {
		return flag_isCheck;
	}

	public void setAllChecked(boolean isChecked) {
		if (flag_isCheck != null) {
			for (int i = 0; i < flag_isCheck.size(); i++) {
				flag_isCheck.set(i, isChecked ? 01 : 0);
			}
			notifyDataSetChanged();
		}
	}

}
