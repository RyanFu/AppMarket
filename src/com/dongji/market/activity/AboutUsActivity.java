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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.receiver.CommonReceiver;

public class AboutUsActivity extends Activity {
	private TextView mPublishDateTV, mVersionInfoTV;
	private ImageView mTopLogo;
	private CommonReceiver receiver;
	private ImageView mShareUs;

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
		mShareUs = (ImageView) findViewById(R.id.share_us);
		mTopLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBroadcast(new Intent(AConstDefine.GO_HOME_BROADCAST));
			}
		});

		if (!AndroidUtils.isTablet(this)||AndroidUtils.getPhysicalSize(this)<6) {
			return;
		}
		
		int screenHeight = AndroidUtils.getScreenSize(this).heightPixels;
		int screenWidth = AndroidUtils.getScreenSize(this).widthPixels;
		LinearLayout.LayoutParams linearParams;
		int actualHeight;
		int actualWidth;
		int leftMargin;
		int rightMargin;
		
		actualHeight = (int) (screenHeight * 0.042);
		actualWidth = actualHeight;
		leftMargin = (int) (screenWidth * 0.022);
		linearParams = (LayoutParams) mTopLogo.getLayoutParams();
		linearParams.width = actualWidth;
		linearParams.height = actualHeight;
		linearParams.leftMargin = leftMargin;
		mTopLogo.setLayoutParams(linearParams);
		
		actualHeight = (int) (screenHeight * 0.03);
		actualWidth = (int) (actualHeight * 0.692);
		rightMargin = (int) (screenWidth * 0.03);
		linearParams = (LayoutParams) mShareUs.getLayoutParams();
		linearParams.width = actualWidth;
		linearParams.height = actualHeight;
		linearParams.rightMargin = rightMargin;
		mShareUs.setLayoutParams(linearParams);
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
