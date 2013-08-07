package com.dongji.market.adapter;

import java.util.List;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
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

public class SearchHistoryAdapter extends BaseAdapter implements
		RequestDataListener {
	
	private static final int REQUEST_ASSOCIATE_DATA = 0;

	private Activity activity;
	private LayoutInflater inflater;
	private List<String> data, list;
	private MyHandler mHandler;
	private SearchHistory history;
	
	private boolean isVisibleBtn = true;
	
	public SearchHistoryAdapter(Activity activity, List<String> list, SearchHistory history) {
		super();
		this.activity = activity;
		this.history = history;
		inflater = LayoutInflater.from(activity);
		data = list;
		initHandler();
	}
	
	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("handler");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}
	
	public void addData(String keyword) {
		data.add(keyword);
		if(activity!=null && !activity.isFinishing()) {
			notifyDataSetChanged();
		}
	}
	
	public void updateData(List<String> list) {
		data.clear();
		data.addAll(list);
		
		if(activity!=null && !activity.isFinishing()) {
			notifyDataSetChanged();
		}
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
//		TextView mTextView;
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

	public void request(String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			isVisibleBtn = false;
			Message msg = mHandler.obtainMessage(REQUEST_ASSOCIATE_DATA, keyword);
			mHandler.sendMessage(msg);
		} else {
			isVisibleBtn = true;
		}
		
	}

	public void cancelPreRequest() {
		// TODO Auto-generated method stub
		
	}
	
	private static class ViewHolder{
		TextView mTextView;
		ImageView mDelBtn;
	}
	
	class MyHandler extends Handler{

		public MyHandler(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			/*if (msg.what == REQUEST_ASSOCIATE_DATA) {
//				System.out.println("start request------------>" + System.currentTimeMillis());
				try {
					list = DataManager.newInstance().getSearchList((String)msg.obj);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
//				System.out.println("end request------------>" + System.currentTimeMillis());
				activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						updateData(list);
					}
				});
			}*/
		}
		
	}

}
