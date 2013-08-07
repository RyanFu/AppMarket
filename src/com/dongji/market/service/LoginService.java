package com.dongji.market.service;

import java.io.IOException;

import org.myjson.JSONException;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.dongji.market.R;
import com.dongji.market.activity.Login_Activity;
import com.dongji.market.application.AppMarket;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.protocol.DataManager;

public class LoginService extends Service {
	
	private String emailStr;
	private String passwdStr;
	
	private LoginParams loginParams;
	private MyHandler mHandler;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initHandler();
	}

	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler.sendEmptyMessageDelayed(0, 10000L);//模拟网络验证操作耗时10s,实为联网登录验证过程
		return super.onStartCommand(intent, flags, startId);
	}*/
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		if(intent != null && intent.getBooleanExtra(AConstDefine.DIALOG_LOGIN, false)){
			int flag=intent.getIntExtra(AConstDefine.FLAG_ACTIVITY_BANDR, -1);
			mHandler.sendEmptyMessageDelayed(flag,3000L);
		}else{
			emailStr = intent.getStringExtra("email");
			passwdStr = intent.getStringExtra("password");
			mHandler.sendEmptyMessage(Login_Activity.EVENT_LOGIN_FLAG);
		}
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private void initHandler() {
		HandlerThread thread = new HandlerThread("thread");
		thread.start();
		mHandler = new MyHandler(thread.getLooper());
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			Intent intent=new Intent("com.dongji.market.loginReceiver");
			System.out.println("loginservice.........."+msg.what);
			switch (msg.what) {
			case AConstDefine.ACTIVITY_CLOUD_BACKUP:
				intent.putExtra(AConstDefine.BROADCAST_DIALOG_LOGIN, true);
				intent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, AConstDefine.ACTIVITY_CLOUD_BACKUP);
				break;
			case AConstDefine.ACTIVITY_CLOUD_RESTORE:
				intent.putExtra(AConstDefine.BROADCAST_DIALOG_LOGIN, true);
				intent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, AConstDefine.ACTIVITY_CLOUD_RESTORE);
				break;
			case Login_Activity.EVENT_LOGIN_FLAG:
				try {
					int responseStatus = DataManager.newInstance().login(emailStr, passwdStr);
					intent.putExtra(AConstDefine.LOGIN_STATUS_BROADCAST, true);
					intent.putExtra(AConstDefine.LOGIN_STATUS, responseStatus);
				} catch (IOException e) {
//					AndroidUtils.showToast(getApplicationContext(), R.string.login_timeout);
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			sendBroadcast(intent);
		}
		
	};
	
}
