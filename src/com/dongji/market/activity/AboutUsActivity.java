package com.dongji.market.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.receiver.CommonReceiver;

public class AboutUsActivity extends Activity {
	private TextView mPublishDateTV, mVersionInfoTV;
	private ImageView mTopLogo;
	private CommonReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutus);
		initView();
		registerCommonReceiver();
	}

	private void registerCommonReceiver() {
		receiver = new CommonReceiver();
		registerReceiver(receiver, new IntentFilter(AConstDefine.GO_HOME_BROADCAST));
	}

	private void initView() {
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

		mPublishDateTV = (TextView) findViewById(R.id.publish_date);
		mVersionInfoTV = (TextView) findViewById(R.id.version_info);

		mPublishDateTV.setText(R.string.publish_date_value);
		mVersionInfoTV.setText(getVersionName());

		mTopLogo = (ImageView) findViewById(R.id.topLogo);
		mTopLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBroadcast(new Intent(AConstDefine.GO_HOME_BROADCAST));
			}
		});
	}

	public String getVersionName() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

}
