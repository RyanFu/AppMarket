package com.dongji.market.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.helper.Constants;
import com.dongji.market.helper.WxUtils;
import com.dongji.market.widget.ShareGridView;
import com.tencent.mm.sdk.openapi.SendMessageToWX;

public class ShareActivity extends Activity implements OnClickListener {

	private Intent intent;

	private ShareGridView gridView;

	private ScrollView scroll;

	private Button wxfriend;

	private Button wxtimeline;

	private ShareItemAdapter adapter;

	private ArrayList<HashMap<String, Object>> appInfoList;

	private boolean wxInstall;

	private TextView wxInstallTextView1;

	private TextView wxInstallTextView2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		getAppInfo();
		initView();
	}

	/**
	 * 获取系统分享应用列表信息
	 */
	private void getAppInfo() {
		intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(Constants.TXTTYPE);
		PackageManager manager = getPackageManager();// 获取包管理器
		List<ResolveInfo> resolveInfos = manager.queryIntentActivities(intent, 0);// 通过包管理器查询符合条件的ResolveInfo信息
		appInfoList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> tempHashMap;
		for (ResolveInfo resolveInfo : resolveInfos) {// 遍历 resolveInfo
														// 获得每个符合条件的应用信息
			ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
			ActivityInfo activityInfo = resolveInfo.activityInfo;
			CharSequence lableName = applicationInfo.loadLabel(manager).toString();
			String packageName = applicationInfo.packageName;
			String activityName = activityInfo.name;
			if (Constants.WXPKGNAME.equals(packageName)) {
				wxInstall = true;
				continue;
			}
			tempHashMap = new HashMap<String, Object>();
			tempHashMap.put("icon", activityInfo.loadIcon(manager));
			tempHashMap.put("packagename", packageName);
			tempHashMap.put("activityname", activityName);
			tempHashMap.put("name", lableName);
			appInfoList.add(tempHashMap);
		}
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		wxfriend = (Button) findViewById(R.id.wxfriend_btn);
		wxtimeline = (Button) findViewById(R.id.wxtimeline_btn);
		wxInstallTextView1 = (TextView) findViewById(R.id.wxuninstall1);
		wxInstallTextView2 = (TextView) findViewById(R.id.wxuninstall2);
		wxfriend.setOnClickListener(this);
		wxtimeline.setOnClickListener(this);
		if (!wxInstall) {
			wxInstallTextView1.setVisibility(View.VISIBLE);
			wxInstallTextView2.setVisibility(View.VISIBLE);
		}
		initGridView();
	}

	/**
	 * 初始化gridview
	 * 
	 */
	private void initGridView() {
		gridView = (ShareGridView) this.findViewById(R.id.gridview);
		adapter = new ShareItemAdapter(this, appInfoList);
		gridView.setAdapter(adapter);
		scroll = (ScrollView) findViewById(R.id.scroll);
		scroll.requestChildFocus(wxfriend, null);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ComponentName componetName = new ComponentName(appInfoList.get(position).get("packagename").toString(), appInfoList.get(position).get("activityname").toString());
				try {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setComponent(componetName);
					intent.setType(Constants.TXTTYPE);
					intent.putExtra(Intent.EXTRA_SUBJECT, "动机市场"); // 分享主题
					intent.putExtra(Intent.EXTRA_TEXT, "海量精品应用，尽在动机市场! http://www.91dongji.com");
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wxfriend_btn:
			if (wxInstall) {
				WxUtils.registWxApi(ShareActivity.this);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
				WxUtils.sendWebPageWx("www.91dongji.com", "动机市场2", "海量精品应用，尽在动机市场", bmp, Constants.THUMB_SIZE, Constants.THUMB_SIZE, SendMessageToWX.Req.WXSceneSession);
			} else {
				Toast.makeText(this, "跳转到微信详情页面！", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.wxtimeline_btn:
			if (wxInstall) {
				WxUtils.registWxApi(ShareActivity.this);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
				WxUtils.sendWebPageWx("www.91dongji.com", "动机市场1", "海量精品应用，尽在动机市场", bmp, Constants.THUMB_SIZE, Constants.THUMB_SIZE, SendMessageToWX.Req.WXSceneTimeline);
			} else {
				Toast.makeText(this, "跳转到微信详情页面！", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	/**
	 * 分享Gridview适配
	 * 
	 */
	private class ShareItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, Object>> shareItems;

		public ShareItemAdapter(Context context, ArrayList<HashMap<String, Object>> tempArrayList) {
			this.mInflater = LayoutInflater.from(context);
			this.shareItems = tempArrayList;
		}

		@Override
		public int getCount() {
			return shareItems.size();
		}

		@Override
		public Object getItem(int position) {
			return shareItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.share_girdview_item, null);
				viewHolder.iv = (ImageView) convertView.findViewById(R.id.itemImage);
				viewHolder.tv = (TextView) convertView.findViewById(R.id.itemText);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.tv.setText(shareItems.get(position).get("name").toString());
			viewHolder.iv.setImageDrawable((Drawable) shareItems.get(position).get("icon"));
			return convertView;
		}

		class ViewHolder {
			ImageView iv = null;
			TextView tv = null;
		}
	}

}
