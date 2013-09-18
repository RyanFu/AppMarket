package com.dongji.market.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.database.MarketDatabase.SearchHistory;
import com.dongji.market.widget.CustomSearchView.RequestDataListener;

/**
 * 搜索历史popwindow适配器
 * 
 * @author yvon
 * 
 */
public class SearchHistoryAdapter extends BaseAdapter implements RequestDataListener {

	private Activity activity;
	private LayoutInflater inflater;
	private List<String> data;
	private SearchHistory history;

	private boolean isVisibleBtn = true;

	public SearchHistoryAdapter(Activity activity, List<String> list, SearchHistory history) {
		super();
		this.activity = activity;
		this.history = history;
		inflater = LayoutInflater.from(activity);
		data = list;
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
			convertView = inflater.inflate(R.layout.search_history_item, null);
			holder.mTextView = (TextView) convertView.findViewById(R.id.history_item);
			holder.mDelBtn = (ImageView) convertView.findViewById(R.id.del_history_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (isVisibleBtn) {
			holder.mDelBtn.setVisibility(View.VISIBLE);
		} else {
			holder.mDelBtn.setVisibility(View.GONE);
		}

		holder.mTextView.setText(data.get(position));
		holder.mDelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				history.del(data.get(position));
				data.remove(position);
				notifyDataSetChanged();
			}
		});
		return convertView;
	}

	private static class ViewHolder {
		TextView mTextView;
		ImageView mDelBtn;
	}

	public void addData(String keyword) {
		data.add(keyword);
		if (activity != null && !activity.isFinishing()) {
			notifyDataSetChanged();
		}
	}

	public void updateData(List<String> list) {
		data.clear();
		data.addAll(list);

		if (activity != null && !activity.isFinishing()) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void request(String keyword) {
	}

	@Override
	public void cancelPreRequest() {
	}
}
