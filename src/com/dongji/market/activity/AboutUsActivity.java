package com.dongji.market.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.receiver.CommonReceiver;
import com.dongji.market.widget.AboutDialog;
import com.dongji.market.widget.ShareDialog;

public class AboutUsActivity extends Activity {
	private TextView mPublishDateTV, mVersionInfoTV;
	private ImageView mTopLogo, mShareUs;
	private CommonReceiver receiver;
	private MyHandler mHandler;
	
	private String share_subject, share_content, share_dialog_title;
	
	private static final int INIT_SHARE_DATA = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutus);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

		mPublishDateTV = (TextView) findViewById(R.id.publish_date);
		mVersionInfoTV = (TextView) findViewById(R.id.version_info);

		mPublishDateTV.setText(R.string.publish_date_value);
		mVersionInfoTV.setText(getVersionName());
		
		mTopLogo = (ImageView) findViewById(R.id.topLogo);
		mTopLogo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendBroadcast(new Intent(AConstDefine.GO_HOME_BROADCAST));
			}
		});
		
		initHandler();
		mShareUs = (ImageView) findViewById(R.id.share_us);
		mShareUs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, share_subject);	//分享主题
				intent.putExtra(Intent.EXTRA_TEXT, share_content);		//分享内容
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
				startActivity(Intent.createChooser(intent, share_dialog_title));//选择对话框标题
			}
		});
		
		receiver = new CommonReceiver();
		registerReceiver(receiver, new IntentFilter(AConstDefine.GO_HOME_BROADCAST));
	}

	private void initShareData() {
		share_subject = getResources().getString(R.string.share_us_subject);
		share_content = getResources().getString(R.string.share_us_content)
				+ DataManager.newInstance().getShortUrlByLongUrl(
						getResources().getString(R.string.share_us_url));
		share_dialog_title = getResources().getString(R.string.share_us_title);
	}

//	@Override
//	public void onBackPressed() {
//		// TODO Auto-generated method stub
//		super.onBackPressed();
//		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
//	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	public String getVersionName() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("ht");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
		mHandler.sendEmptyMessage(INIT_SHARE_DATA);
	}
	
	class MyHandler extends Handler {

		private MyHandler(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == INIT_SHARE_DATA) {
				initShareData();
			}
		}
		
	}
}
