package com.dongji.market.activity;

import java.io.IOException;

import org.myjson.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.dongji.market.R;
import com.dongji.market.application.AppMarket;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.protocol.HttpClientApi;
import com.dongji.market.receiver.CommonReceiver;
import com.dongji.market.widget.CustomProgressDialog;
import com.umeng.analytics.MobclickAgent;

public class Login_Activity extends Activity {

	private EditText mEmailET;
	private EditText mPasswordET;
	private Button mLoginBtn;
	private Button mRegisterBtn;
	private Button mPwdRetakeBtn;
	private LinearLayout mSinaLoginLL;
	private TextView mOtherLoginTV;

	private InputMethodManager imm;

//	private SharedPreferences loginPref;

	private TitleUtil titleUtil;
	private MyHandler mHandler;

	private Intent intent = new Intent();

//	private static final int IS_LOGINING = 0;
	public static final int SINA_LOGIN_SUCCESS = 999;
	public static final int EXIT_LOGINPAGE = 1000;
	public static final int EVENT_LOGIN_FLAG = 0;
	
	public static final int LOGIN_RESPONSE_STATUS = 2;
	private static final int NET_ERROR_PROMPT = 3;
	private static final int EVENT_LOGIN_PROMPT = 4;
	
	private int responseStatus;
	
	private static final int REGISTER_REQUEST_CODE = 1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		View mTopView = findViewById(R.id.login_top);
		// imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		titleUtil = new TitleUtil(this, mTopView, R.string.login, null, null);

		mEmailET = (EditText) findViewById(R.id.email);
		mPasswordET = (EditText) findViewById(R.id.password);
		mLoginBtn = (Button) findViewById(R.id.login);
		mRegisterBtn = (Button) findViewById(R.id.register);
		mPwdRetakeBtn = (Button) findViewById(R.id.passwd_retake);
		mSinaLoginLL = (LinearLayout) findViewById(R.id.sina_login);
		mOtherLoginTV = (TextView) findViewById(R.id.other_id_login);

		otherLoginSeparator();

		mLoginBtn.setOnClickListener(listener);
		mRegisterBtn.setOnClickListener(listener);
		mPwdRetakeBtn.setOnClickListener(listener);
		mSinaLoginLL.setOnClickListener(listener);

		// 设置获取焦点时弹出输入法
		mEmailET.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				(new Handler()).postDelayed(new Runnable() {

					@Override
					public void run() {
						imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (hasFocus) {
//							imm.toggleSoftInput(0,
//									InputMethodManager.HIDE_NOT_ALWAYS);
							imm.showSoftInput(mEmailET, InputMethodManager.SHOW_IMPLICIT);
						} /*
						 * else {
						 * imm.hideSoftInputFromWindow(mEmailET.getWindowToken
						 * (), 0); }
						 */
					}
				}, 100);
			}
		});

		mPasswordET.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					mHandler.sendEmptyMessage(EVENT_LOGIN_FLAG);
//					login();
				}
				return false;
			}

		});

		initHandler();
	}


	private void login() {
		final String emailStr = mEmailET.getText().toString();
		final String pwdStr = mPasswordET.getText().toString();
		DataManager dataManager=DataManager.newInstance();
		try{
			int type=dataManager.login(emailStr, pwdStr);
			switch(type) {
				case 1:
					AppMarket mApp=((AppMarket)getApplication());
					mApp.getLoginParams().setUserName(emailStr);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(Login_Activity.this, R.string.login_success);
							finish();
						}
					});
					break;
				case -1:
				case -2:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(Login_Activity.this, R.string.email_or_password_error);
						}
					});
					break;
					default:
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								AndroidUtils.showToast(Login_Activity.this, R.string.login_failed);
							}
						});
						break;
			}
		}catch(IOException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroidUtils.showToast(Login_Activity.this, R.string.login_timeout);
				}
			});
		}catch(JSONException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroidUtils.showToast(Login_Activity.this, R.string.login_failed);
				}
			});
		} finally {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					dismissProgressDialog();
				}
			});
		}
	}
	
	/**
	 * 登录状态提示
	 * @param status 登录状态值
	
	private void loginStatusPrompt(int status) {
		switch (status) {
		case 0:			//邮箱或密码为空
			AndroidUtils.showToast(this, R.string.email_or_pwd_null);
			break;
		case 1:			//登陆成功
			if (loginParams == null) {
				loginParams = ((AppMarket)getApplicationContext()).getLoginParams();
			}
			loginParams.setLoginState(AConstDefine.LOGIN_SUCCESS_FLAG);
			loginParams.setUserName(mEmailET.getText().toString());
			System.out.println("user name ======> " + loginParams.getUserName());
			AndroidUtils.showToast(this, R.string.login_success);
			
			if (getIntent().getStringExtra("source") != null && getIntent().getStringExtra("source").equals(AConstDefine.LOGIN_SOURCE)) {
				setResult(Activity.RESULT_OK);
			}
			mHandler.sendEmptyMessage(EXIT_LOGINPAGE);
			break;
		case -1:		//用户不存在
			AndroidUtils.showToast(this, R.string.user_not_exist);
			break;
		case -2:		//密码错误
			AndroidUtils.showToast(this, R.string.pwd_incorrect);
			break;
		case -3:		//会员注册登陆状态失败
			AndroidUtils.showToast(this, R.string.user_status_failed);
			break;
		case -10000:	//登陆失败(服务器错误)
			AndroidUtils.showToast(this, R.string.login_failed);
			break;
		case -9999:		//提交方式错误
			AndroidUtils.showToast(this, R.string.commit_error);
			break;
		default:
			break;
		}
	} */

//	private void exitLoginPage() {
//		mHandler.sendEmptyMessageDelayed(IS_LOGINING, 500);
//	}

	/*private int checkLoginState() {
		if (loginParams == null) {
			loginParams = ((AppMarket)getApplicationContext()).getLoginParams();
		}
		return loginParams.getLoginState();
	}
	
	private void loginFailed() {
		if (loginParams == null) {
			loginParams = ((AppMarket)getApplicationContext()).getLoginParams();
		}
		loginParams.setLoginState(AConstDefine.LOGIN_OUT_FLAG);
	}*/

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

	/**
	 * 根据屏幕大小，分隔线长度自适应显示
	 */
	private void otherLoginSeparator() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		TextPaint mPaint = mOtherLoginTV.getPaint();
		mOtherLoginTV.measure(0, 0);
		float charWidth = mPaint.measureText("-");
		float strWidth = mPaint.measureText(mOtherLoginTV.getText().toString());
		int charCount = (int) ((screenWidth - dip2px(25 * 2) - (int) strWidth) / 2 / (charWidth));
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < charCount * 2; i++) {
			builder.append("-");
			if (i == charCount - 1) {
				builder.append(mOtherLoginTV.getText().toString());
			}
		}
		mOtherLoginTV.setText(builder);
	}

	OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.register:
				intent.setClass(Login_Activity.this, Register_Activity.class);
				startActivityForResult(intent, REGISTER_REQUEST_CODE);
				break;
			case R.id.passwd_retake:
				intent.setClass(Login_Activity.this,
						Passwd_Retake_Activity.class);
				startActivity(intent);
				break;
			case R.id.login:
				if(checkLoginParams()) {
					if(imm!=null) {
						imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
					mHandler.sendEmptyMessage(EVENT_LOGIN_FLAG);
				}
//				login();
				break;
			case R.id.sina_login:
//				DJMarketUtils.sinaLogin(Login_Activity.this, handler);
				break;
			default:
				break;
			}
		}

	};
	
	private static final int MAX_EMAIL_LENGTH = 50; // 邮箱最长字符限制
	private static final int MIN_PASSWORD_LENGTH = 6; // 密码最小长度不能小于6
	private CustomProgressDialog mProgressDialog;
	
	/**
	 * 验证登录参数的合法性
	 * @return
	 */
	private boolean checkLoginParams() {
		String emailString=mEmailET.getText().toString();
		String passwordString=mPasswordET.getText().toString();
		if(TextUtils.isEmpty(emailString)) {
			AndroidUtils.showToast(this, R.string.email_null);
			mEmailET.requestFocus();
		}else if(!AndroidUtils.isEmail(emailString)) {
			AndroidUtils.showToast(this, R.string.emailformat_is_error);
			mEmailET.requestFocus();
		}else if(TextUtils.isEmpty(passwordString)) {
			AndroidUtils.showToast(this, R.string.password_null);
			mPasswordET.requestFocus();
		} else if (emailString.length() > MAX_EMAIL_LENGTH) {
			AndroidUtils.showToast(this, R.string.email_is_notoverfifty);
			mEmailET.requestFocus();
		}else if(passwordString.length()<MIN_PASSWORD_LENGTH) {
			AndroidUtils.showToast(this, R.string.pwd_is_notlowsix);
			mPasswordET.requestFocus();
		}else if(!AndroidUtils.isNetworkAvailable(this)) {
			AndroidUtils.showToast(this, R.string.no_web_to_login);
		}else {
			showProgressDialog();
			return true;
		}
		return false;
	}
	
	private void showProgressDialog() {
		if(mProgressDialog==null) {
			mProgressDialog=new CustomProgressDialog(this);
			mProgressDialog.setContentText(getString(R.string.logining));
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

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public int dip2px(float dpValue) {
		final float scale = this.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.getCurrentFocus() != null) {
				if (this.getCurrentFocus().getWindowToken() != null && imm!=null) {
					imm.hideSoftInputFromWindow(this.getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
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
		if(titleUtil!=null) {
			titleUtil.sendRefreshHandler();
		}
 	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
		if(titleUtil!=null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REGISTER_REQUEST_CODE && resultCode==1) {
			finish();
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
			if (mHandler.hasMessages(EVENT_LOGIN_FLAG)) {
				mHandler.removeMessages(EVENT_LOGIN_FLAG);
			}
			if (mHandler.hasMessages(LOGIN_RESPONSE_STATUS)) {
				mHandler.removeMessages(LOGIN_RESPONSE_STATUS);
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
			case EVENT_LOGIN_FLAG:
				login();
				break;
			case EXIT_LOGINPAGE:
				finish();
				break;
			/*case LOGIN_RESPONSE_STATUS:
				responseStatus = msg.arg1;
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						loginStatusPrompt(responseStatus);
						mLoginBtn.setClickable(true);
					}
				});
				if (msg.arg1 != 1) {
					loginFailed();
				}
				break;*/
			case NET_ERROR_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						AndroidUtils.showToast(Login_Activity.this, R.string.net_error);
						mLoginBtn.setClickable(true);
					}
				});
				break;
			default:
				break;
			}
		}
		
	}

}
