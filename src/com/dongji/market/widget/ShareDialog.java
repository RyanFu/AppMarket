package com.dongji.market.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.adapter.ShareAdapter;
import com.dongji.market.helper.Constants;
import com.dongji.market.helper.WxUtils;
import com.dongji.market.pojo.ApkItem;
import com.tencent.mm.sdk.openapi.SendMessageToWX;

public class ShareDialog extends Dialog {
	private Intent intent;
	private Context context;
	private View mContentView;
	private ApkItem apkItem;
	private ShareAdapter adapter;
	private String title;// 分享标题
	private String content;// 分享内容
	private Bitmap icon;// 分享应用icon
	private String shareUrl;// 分享url
	private ArrayList<HashMap<String, Object>> appInfoList;
	private boolean wxInstall;

	public ShareDialog(Context context, Bundle bundle, boolean isApkDetailPage) {
		super(context, R.style.dialog_progress_default);
		this.context = context;
		if (isApkDetailPage && bundle != null) {
			apkItem = bundle.getParcelable("apkItem");
			title = apkItem.appName;
			content = apkItem.discription;
			icon = WxUtils.getBitmapFromFile(apkItem.apkUrl);
			shareUrl = Constants.APKDETAIL_PREFIX + intent.getStringExtra("appId");
		} else {
			title = context.getResources().getString(R.string.DJ_app_center);
			content = context.getResources().getString(R.string.share_us_content);
			icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
			shareUrl = Constants.APKDETAIL_PREFIX + "88888";
		}
		getAppInfo();
		initView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
	}

	/**
	 * 获取系统分享应用列表信息
	 */
	private void getAppInfo() {
		intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(Constants.TXTTYPE);
		PackageManager manager = context.getPackageManager();// 获取包管理器
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

	private void initView() {
		mContentView = LayoutInflater.from(context).inflate(R.layout.widget_share_dialog, null);
		LinearLayout friendLay = (LinearLayout) mContentView.findViewById(R.id.layout_wx_friend);
		LinearLayout timeLineLay = (LinearLayout) mContentView.findViewById(R.id.layout_time_line);
		LinearLayout unInstallLay = (LinearLayout) mContentView.findViewById(R.id.layout_wx_uninstall);
		if (!wxInstall) {
			timeLineLay.setVisibility(View.GONE);
			unInstallLay.setVisibility(View.VISIBLE);
		}
		friendLay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (wxInstall) {
					WxUtils.registWxApi(context);
					WxUtils.sendWebPageWx(shareUrl, title, content, icon, SendMessageToWX.Req.WXSceneSession);
				} else {
					Toast.makeText(context, "跳转到微信详情页面！", Toast.LENGTH_SHORT).show();
				}
				dismiss();
			}
		});
		timeLineLay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WxUtils.registWxApi(context);
				WxUtils.sendWebPageWx(shareUrl, title, content, icon, SendMessageToWX.Req.WXSceneTimeline);
				dismiss();
			}
		});

		GridView gridView = (GridView) mContentView.findViewById(R.id.share_gridview);
		adapter = new ShareAdapter(context, appInfoList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ComponentName componetName = new ComponentName(appInfoList.get(position).get("packagename").toString(), appInfoList.get(position).get("activityname").toString());
				try {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setComponent(componetName);
					intent.setType(Constants.TXTTYPE);
					intent.putExtra(Intent.EXTRA_SUBJECT, title); // 分享主题
					intent.putExtra(Intent.EXTRA_TEXT, content);
					context.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
				dismiss();
			}
		});

	}

}
