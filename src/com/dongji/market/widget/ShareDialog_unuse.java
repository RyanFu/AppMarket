package com.dongji.market.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.dongji.market.R;
import com.dongji.market.adapter.ShareAdapter;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.ShareItem;

public class ShareDialog_unuse extends Dialog {

	private Context context;
	private View mContentView;
	private List<ShareItem> shareList;
	private ApkItem apkItem;

	private ShareAdapter adapter;

	public ShareDialog_unuse(Context context, Bundle bundle) {
		super(context, R.style.dialog_progress_default);
		this.context = context;
		if (bundle != null) {
			apkItem = bundle.getParcelable("apkItem");
		}
		initView();
	}

	private void initView() {
		initShareList();
		mContentView = LayoutInflater.from(context).inflate(
				R.layout.widget_share_dialog_unuse, null);
		GridView gridView = (GridView) mContentView
				.findViewById(R.id.share_gridview);
		adapter = new ShareAdapter(context, shareList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// AndroidUtils.showToast(context,
				// shareList.get(position).getShareName());
				switch (position) {
				case 0:
					executeSmsShare();
					break;
				case 1:
					executeTwitterShare();
					break;
				default:
					break;
				}

				dismiss();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
	}

	private void initShareList() {
		shareList = new ArrayList<ShareItem>();
		shareList.add(new ShareItem(context.getResources().getDrawable(
				R.drawable.sms_icon), context.getResources().getString(
				R.string.sms)));
		shareList.add(new ShareItem(context.getResources().getDrawable(
				R.drawable.sina_twitter_icon), context.getResources()
				.getString(R.string.sina_twitter)));
	}

	public void setShareList(List<ShareItem> list) {
		adapter.setData(list);
	}

	private void executeSmsShare() {
		Uri smsUri = Uri.parse("smsto:");
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
		if (apkItem != null) {
			intent.putExtra(
					"sms_body",
					context.getResources().getString(R.string.share_text1)
					+ apkItem.appName
					+ context.getResources()
					.getString(R.string.share_text2)
					+ apkItem.apkUrl);
		}
		context.startActivity(intent);
	}

	private void executeTwitterShare() {

	}

}
