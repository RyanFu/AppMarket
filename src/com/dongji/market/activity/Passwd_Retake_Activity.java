package com.dongji.market.activity;

import java.io.IOException;

import org.myjson.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.dongji.market.R;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.protocol.HttpClientApi;
import com.dongji.market.receiver.CommonReceiver;
import com.dongji.market.widget.CustomProgressDialog;
import com.umeng.analytics.MobclickAgent;

public class Passwd_Retake_Activity extends Activity implements OnClickListener {

	private EditText mEmailET;
	private Button mConfirmBtn;

	private TitleUtil titleUtil;
	private MyHandler mHandler;
	
	private InputMethodManager imm;
	
	private int responseStatus;
	private static final int EVENT_FIND_PWD = 0;
	private static final int CHECK_RESPONSE_TIMEOUT = 1;
	private static final int NET_ERROR_PROMPT = 2;
	private static final int EVENT_FIND_PWD_PROMPT = 3;
	
	private CustomProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwd_retake);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		View mTopView = findViewById(R.id.pwd_retake_top);
		titleUtil = new TitleUtil(this, mTopView, R.string.passwd_retake, null, null);

		mEmailET = (EditText) findViewById(R.id.pwd_retake_email);
		mConfirmBtn = (Button) findViewById(R.id.pwd_retake_confirm);

		// 设置获取焦点时弹出输入法
		mEmailET.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				(new Handler()).postDelayed(new Runnable() {

					@Override
					public void run() {
						imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (hasFocus) {
							imm.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
						}
					}
				}, 100);
			}
		});

		mEmailET.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					mHandler.sendEmptyMessage(EVENT_FIND_PWD);
				}
				return false;
			}
		});

		mConfirmBtn.setOnClickListener(this);

		
		initHandler();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
//		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("test");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}
	
	private boolean checkEmail() {
		String emailString=mEmailET.getText().toString();
		if(TextUtils.isEmpty(emailString)) {
			AndroidUtils.showToast(this, R.string.email_null);
			mEmailET.requestFocus();
		}else if(!AndroidUtils.isEmail(emailString)) {
			AndroidUtils.showToast(this, R.string.emailformat_is_error);
			mEmailET.requestFocus();
		}else if(!AndroidUtils.isNetworkAvailable(this)) {
			AndroidUtils.showToast(this, R.string.no_network_msg1);
		}else {
			showProgressDialog();
			return true;
		}
		return false;
	}
	
	private void showProgressDialog() {
		if(mProgressDialog==null) {
			mProgressDialog=new CustomProgressDialog(this);
			mProgressDialog.setContentText(getString(R.string.send_to_server));
		}
		if(!isFinishing() & !mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
	}
	
	private void dismissProgressDialog() {
		if(mProgressDialog!=null) {
			mProgressDialog.dismiss();
		}
	}

	private void retake_pwd_confirm() {
		String emailStr = mEmailET.getText().toString();
		try {
			int type = DataManager.newInstance().findPwd(emailStr);
			switch(type) {
				case 1:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.send_password_success);
							finish();
						}
					});
					break;
				case -1:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.send_failed);
						}
					});
					break;
				case -2:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.user_not_exist);
						}
					});
					break;
			}
		} catch (IOException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.connection_timeout);
				}
			});
		} catch (JSONException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.send_failed);
				}
			});
		}finally {
			dismissProgressDialog();
		}
	}
	
	private void pwdFindPrompt(int status) {
		switch (status) {
		case 1:			//发送成功
			AndroidUtils.showToast(this, R.string.send_success);
			finish();
			break;
		case -1:		//发送失败
			AndroidUtils.showToast(this, R.string.send_failed);
			break;
		case -2:		//无此用户
			AndroidUtils.showToast(this, R.string.user_not_exist);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pwd_retake_confirm: // 缺邮箱不存在验证
			if(checkEmail()) {
				mHandler.sendEmptyMessage(EVENT_FIND_PWD);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.getCurrentFocus() != null) {
				if (this.getCurrentFocus().getWindowToken() != null && imm!=null) {
					imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onDestroy() {
		titleUtil.unregisterMyReceiver(this);
//		HttpClientApi.getInstance().abortPostReq();
		removeMessage();
		super.onDestroy();
	}
	
	private void removeMessage() {
		if (mHandler != null) {
			if (mHandler.hasMessages(EVENT_FIND_PWD)) {
				mHandler.removeMessages(EVENT_FIND_PWD);
			}
			if (mHandler.hasMessages(CHECK_RESPONSE_TIMEOUT)) {
				mHandler.removeMessages(CHECK_RESPONSE_TIMEOUT);
			}
		}
	}
	
	private void initHandler() {
		HandlerThread thread = new HandlerThread("handler");
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
			// TODO Auto-generated method stub
			switch (msg.what) {
			case EVENT_FIND_PWD:
				retake_pwd_confirm();
				break;
			case CHECK_RESPONSE_TIMEOUT:
				if (responseStatus == AConstDefine.THRESHOLD) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.net_conn_timeout);
							mConfirmBtn.setClickable(true);
						}
					});
				}
				break;
			case NET_ERROR_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						AndroidUtils.showToast(Passwd_Retake_Activity.this, R.string.net_error);
						mConfirmBtn.setClickable(true);
					}
				});
				break;
			case EVENT_FIND_PWD_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pwdFindPrompt(responseStatus);
						mConfirmBtn.setClickable(true);
					}
				});
				break;
			default:
				break;
			}
		}
		
	}

}
