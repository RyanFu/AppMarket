package com.dongji.market.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dongji.market.R;

public class AboutDialog extends Dialog {
	
	private Context cxt;
	private View mContentView;
	private TextView mPublishDateTV, mVersionInfoTV;
	private Button mConfirBtn;

	public AboutDialog(Context context) {
		super(context, R.style.dialog_progress_default);
		cxt = context;
		initView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
	}

	private void initView() {
		mContentView = getLayoutInflater().inflate(R.layout.widget_about_dialog, null);
		mConfirBtn = (Button) mContentView.findViewById(R.id.about_confirm);
		mPublishDateTV = (TextView) mContentView.findViewById(R.id.publish_date);
		mVersionInfoTV = (TextView) mContentView.findViewById(R.id.version_info);
		mConfirBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mVersionInfoTV.setText(getVersionName());
	}
	
	public AboutDialog setPublishDate(String dateStr) {
		mPublishDateTV.setText(dateStr);
		return this;
	}
	
	public AboutDialog setPublishDate(int id) {
		mPublishDateTV.setText(cxt.getResources().getString(id));
		return this;
	}
	
	public AboutDialog setVersionInfo(String versionStr) {
		mVersionInfoTV.setText(versionStr);
		return this;
	}
	
	public AboutDialog setVersionInfo(int id) {
		mVersionInfoTV.setText(cxt.getResources().getString(id));
		return this;
	}
	
	public String getVersionName() {
		try {
			PackageInfo info = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0);
			System.out.println("version name=======>" + info.versionName);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
