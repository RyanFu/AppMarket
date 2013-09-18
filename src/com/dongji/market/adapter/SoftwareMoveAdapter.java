package com.dongji.market.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.SoftwareMove_list_Activity;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.InstalledAppInfo;

/**
 * 软件搬家listView适配器
 * @author yvon
 *
 */
public class SoftwareMoveAdapter extends BaseAdapter {

	private Context context;
	private List<InstalledAppInfo> data;
	private LayoutInflater inflater;
	private int flag;

	public SoftwareMoveAdapter(Context context, List<InstalledAppInfo> data) {
		super();
		this.context = context;
		this.data = data;
		inflater = LayoutInflater.from(context);
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
		if (getCount() == 0) {
			return convertView;
		}
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_softwaremove, null);
			holder = new ViewHolder();
			holder.mIconImageView = (ImageView) convertView.findViewById(R.id.app_icon);
			holder.mNameTextView = (TextView) convertView.findViewById(R.id.app_name);
			holder.mVersionTextView = (TextView) convertView.findViewById(R.id.app_version);
			holder.mSizeTextView = (TextView) convertView.findViewById(R.id.app_size);
			holder.mUninstallView = (Button) convertView.findViewById(R.id.uninstallButton);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final InstalledAppInfo info = data.get(position);
		holder.mIconImageView.setImageDrawable(info.getIcon());
		holder.mNameTextView.setText(info.getName());
		holder.mVersionTextView.setText("V" + info.getVersion());
		holder.mSizeTextView.setText("/" + info.getSize());
		if (flag == SoftwareMove_list_Activity.FLAG_SDCARD) {
			holder.mUninstallView.setText(R.string.movetophonecard);
		} else {
			holder.mUninstallView.setText(R.string.movetosdcard);
		}
		if (info.moveType == DJMarketUtils.MOVEAPPTYPE_NONE) {
			holder.mUninstallView.setEnabled(false);
		} else {
			holder.mUninstallView.setEnabled(true);
		}
		holder.mUninstallView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AndroidUtils.showInstalledAppDetails(context, ((InstalledAppInfo) getItem(position)).getPkgName());
			}

		});
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AndroidUtils.showInstalledAppDetails(context, ((InstalledAppInfo) getItem(position)).getPkgName());
			}
		});
		return convertView;
	}

	private static class ViewHolder {
		ImageView mIconImageView;
		TextView mNameTextView;
		TextView mVersionTextView;
		TextView mSizeTextView;
		Button mUninstallView;
	}

	public void addData(InstalledAppInfo info) {
		data.add(info);
		notifyDataSetChanged();
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public void clear() {
		data.clear();
	}

	public void setData(List<InstalledAppInfo> data) {
		this.data = data;
		notifyDataSetChanged();
	}

	public void reflash() {
		notifyDataSetChanged();
	}

}
