package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.pojo.InstalledAppInfo;

public class UninstallAdapter extends BaseAdapter {

	Context context;
	List<InstalledAppInfo> data;
	LayoutInflater inflater;
	int curItem;

	Drawable appIcon;

	public UninstallAdapter(Context context, List<InstalledAppInfo> data) {
		super();
		this.context = context;
		this.data = data;
		inflater = LayoutInflater.from(context);
	}

	public void addData(InstalledAppInfo info) {
		data.add(info);
		notifyDataSetChanged();
	}

	public void clear() {
		data.clear();
	}

	public void setData(List<InstalledAppInfo> data) {
		this.data = data;
		notifyDataSetChanged();
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

	public void reflash() {
		data.remove(curItem);
		notifyDataSetChanged();
	}

	/**
	 * 根据包名删除卸载列表对应的数据，并刷新列表
	 * 
	 * @param packageName
	 */
	public void removeAppDataByPackageName(String packageName) {
		if (data != null && data.size() > 0) {
			for (int i = 0; i < data.size(); i++) {
				InstalledAppInfo info = data.get(i);
				if (null != info && info.getPkgName() != null) {
					if (packageName.equals(info.getPkgName())) {
						data.remove(i);
						notifyDataSetChanged();
						break;
					}
				}
			}
		}
	}

	/**
	 * 将新安装的应用信息添加进列表
	 * 
	 * @param info
	 */
	public void addAppData(InstalledAppInfo info) {
		if (data != null) {
			data.add(info);
			notifyDataSetChanged();
		}
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_uninstall, null);
			holder = new ViewHolder();
			holder.mIconImageView = (ImageView) convertView
					.findViewById(R.id.app_icon);
			holder.mNameTextView = (TextView) convertView
					.findViewById(R.id.app_name);
			holder.mVersionTextView = (TextView) convertView
					.findViewById(R.id.app_version);
			holder.mSizeTextView = (TextView) convertView
					.findViewById(R.id.app_size);
			holder.mUninstallView = (Button) convertView
					.findViewById(R.id.uninstallButton);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final InstalledAppInfo info = data.get(position);
		if (null != info && info.getIcon() != null) {
			appIcon = info.getIcon();
		} else {
			appIcon = context.getResources().getDrawable(
					R.drawable.app_default_icon);
		}
		holder.mIconImageView.setImageDrawable(appIcon);
		if(null != info && null!=info.getName()){
			holder.mNameTextView.setText(info.getName());
		}
		if (info != null) {
			String version = TextUtils.isEmpty(info.getVersion()) ? "1.0" : info
					.getVersion();
			holder.mVersionTextView.setText("V" + version);
		}
		if(null != info && null!=info.getSize()){
			holder.mSizeTextView.setText("/" + info.getSize());
		}

		holder.mUninstallView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				/*
				 * final CustomDialog dialog = new CustomDialog(context)
				 * .setIcon(info.getIcon()); dialog.setTitle(info.getName());
				 * dialog.setPositiveButton(
				 * context.getResources().getString(R.string.confirm), new
				 * OnClickListener() {
				 * 
				 * @Override public void onClick(View v) {
				 * uninstallApp(info.getPkgName()); data.remove(position);
				 * dialog.dismiss(); } }).setNegativeButton(
				 * context.getResources().getString(R.string.cancel), new
				 * OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { // TODO
				 * Auto-generated method stub dialog.dismiss(); } });
				 * 
				 * dialog.show();
				 */

				uninstallApp(info.getPkgName());
				// data.remove(position);
				curItem = position;
			}

		});
		return convertView;
	}

	/**
	 * 卸载软件方法
	 * 
	 * @param packageName
	 */
	private void uninstallApp(String packageName) {
		Uri packageUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		context.startActivity(uninstallIntent);
	}

	private static class ViewHolder {
		ImageView mIconImageView;
		TextView mNameTextView;
		TextView mVersionTextView;
		TextView mSizeTextView;
		Button mUninstallView;
	}
}
