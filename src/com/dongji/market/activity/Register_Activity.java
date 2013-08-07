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

public class Register_Activity extends Activity implements OnClickListener {

	private EditText mEmailET;
	private EditText mPasswordET;
	private EditText mPwdAgainET;

	private Button mRegisterBtn;

	private TitleUtil titleUtil;
	private MyHandler mHandler;
//	private LoginParams loginParams;
	
	private InputMethodManager imm;
	
	private int responseStatus;
	private static final int EVENT_REGISTER_FLAG = 0;
	private static final int CHECK_REGISTER_TIMEOUT = 1;
	private static final int NET_ERROR_PROMPT = 2;
	private static final int REQUEST_CODE_PROMPT = 3;
	
	private static final int MAX_EMAIL_LENGTH = 50; // 邮箱最长字符限制
	private static final int MIN_PASSWORD_LENGTH = 6; // 密码最小长度不能小于6
	private CustomProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		View mTopView = findViewById(R.id.regist_top);
		titleUtil = new TitleUtil(this, mTopView, R.string.register, null, null);

		mEmailET = (EditText) findViewById(R.id.reg_email);
		mPasswordET = (EditText) findViewById(R.id.reg_password);
		mPwdAgainET = (EditText) findViewById(R.id.reg_pwd_again);
		mRegisterBtn = (Button) findViewById(R.id.reg_register);

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
						}
					}
				}, 100);
			}
		});

		mPwdAgainET.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					mHandler.sendEmptyMessage(EVENT_REGISTER_FLAG);
				}
				return false;
			}
		});

		mRegisterBtn.setOnClickListener(this);

		
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
	
	/**
	 * 验证注册参数的合法性
	 * @return
	 */
	private boolean checkRegisterParams() {
		String emailString=mEmailET.getText().toString();
		String pwdStr = mPasswordET.getText().toString();
		String pwdAgainStr = mPwdAgainET.getText().toString();
		if(TextUtils.isEmpty(emailString)) {
			AndroidUtils.showToast(this, R.string.email_null);
			mEmailET.requestFocus();
		}else if(!AndroidUtils.isEmail(emailString)) {
			AndroidUtils.showToast(this, R.string.email_format_error);
			mEmailET.requestFocus();
		}else if(TextUtils.isEmpty(pwdStr) || TextUtils.isEmpty(pwdAgainStr)) {
			AndroidUtils.showToast(this, R.string.password_null);
			if(TextUtils.isEmpty(pwdStr) || (TextUtils.isEmpty(pwdStr) && TextUtils.isEmpty(pwdAgainStr))) {
				mPasswordET.requestFocus();
			}else if(TextUtils.isEmpty(pwdAgainStr)) {
				mPwdAgainET.requestFocus();
			}
		} else if (emailString.length() > MAX_EMAIL_LENGTH) {
			AndroidUtils.showToast(this, R.string.email_is_notoverfifty);
			mEmailET.requestFocus();
		}else if(pwdStr.length()<MIN_PASSWORD_LENGTH || pwdAgainStr.length()<MIN_PASSWORD_LENGTH) {
			AndroidUtils.showToast(this, R.string.pwd_is_notlowsix);
			if(pwdStr.length()<MIN_PASSWORD_LENGTH || (pwdStr.length()<MIN_PASSWORD_LENGTH && pwdAgainStr.length()<MIN_PASSWORD_LENGTH)) {
				mPasswordET.requestFocus();
			}else if(pwdAgainStr.length()<MIN_PASSWORD_LENGTH) {
				mPwdAgainET.requestFocus();
			}
		}else if(!pwdStr.equals(pwdAgainStr)) {
			AndroidUtils.showToast(this, R.string.pwd_is_different);
			mPwdAgainET.requestFocus();
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
			mProgressDialog.setContentText(getString(R.string.registering));
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

	private void register_confirm() {
		final String emailStr = mEmailET.getText().toString();
		final String pwdStr = mPasswordET.getText().toString();
		String pwdAgainStr = mPwdAgainET.getText().toString();
		try {
			int type = DataManager.newInstance().register(emailStr, pwdStr, pwdAgainStr);
			System.out.println("type:"+type);
			switch(type) {
			case 1:
				AppMarket mApp=((AppMarket)getApplication());
				mApp.getLoginParams().setUserName(emailStr);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AndroidUtils.showToast(Register_Activity.this, R.string.register_and_login);
						setResult(1, null);
						finish();
					}
				});
				break;
			case -3:
			case -6:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AndroidUtils.showToast(Register_Activity.this, R.string.email_occupied_error);
						mEmailET.requestFocus();
					}
				});
				break;
				default:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AndroidUtils.showToast(Register_Activity.this, R.string.register_fail);
						}
					});
					break;
			}
		} catch (IOException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroidUtils.showToast(Register_Activity.this, R.string.register_timeout);
				}
			});
		} catch (JSONException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroidUtils.showToast(Register_Activity.this, R.string.register_fail);
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
	 * 注册状态提示
	 * @param status 注册状态值
	 
	private void registerStatusPrompt(int status) {
		switch (status) {
		case 0:			//邮箱或密码为空
			AndroidUtils.showToast(this, R.string.email_or_pwd_null);
			break;
		case 1:			//注册成功
			AndroidUtils.showToast(this, R.string.register_and_login);
			if (loginParams == null) {
				loginParams = ((AppMarket)getApplicationContext()).getLoginParams();
			}
			loginParams.setUserName(mEmailET.getText().toString());
			loginParams.setLoginState(AConstDefine.LOGIN_SUCCESS_FLAG);
			finish();
			break;
		case -1:		//用户名不合法
			AndroidUtils.showToast(this, R.string.user_name_illegal);
			break;
		case -2:		//包含不允许注册的词语
			AndroidUtils.showToast(this, R.string.include_sensitive_word);
			break;
		case -3:		//用户名已经存在
			AndroidUtils.showToast(this, R.string.user_exist);
			break;
		case -4:		//Email格式有误
			AndroidUtils.showToast(this, R.string.email_format_error);
			break;
		case -5:		//Email不允许注册
			AndroidUtils.showToast(this, R.string.email_unallowed_reg);
			break;
		case -6:		//该 Email已经被注册
			AndroidUtils.showToast(this, R.string.email_already_reg);
			break;
		case -7:		//会员添加失败
			AndroidUtils.showToast(this, R.string.add_user_failed);
			break;
		case -8:		//两次密码不一致
			AndroidUtils.showToast(this, R.string.pwd_discord);
			break;
		case -9:		//密码长度在6-15位之间
			AndroidUtils.showToast(this, R.string.pwd_length_error);
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
	}*/

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reg_register: // 缺邮箱已占用验证
//			register_confirm();
			if(checkRegisterParams()) {
				if(imm!=null) {
					imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
				mHandler.sendEmptyMessage(EVENT_REGISTER_FLAG);
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
				if (this.getCurrentFocus().getWindowToken() != null) {
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
	protected void onDestroy() {
		titleUtil.unregisterMyReceiver(this);
//		HttpClientApi.getInstance().abortPostReq();
		removeMessage();
		super.onDestroy();
	}
	
	private void removeMessage() {
		if (mHandler != null) {
			if (mHandler.hasMessages(EVENT_REGISTER_FLAG)) {
				mHandler.removeMessages(EVENT_REGISTER_FLAG);
			}
			if (mHandler.hasMessages(CHECK_REGISTER_TIMEOUT)) {
				mHandler.removeMessages(CHECK_REGISTER_TIMEOUT);
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
			case EVENT_REGISTER_FLAG:
				register_confirm();
				break;
			case CHECK_REGISTER_TIMEOUT:
				if (responseStatus == AConstDefine.THRESHOLD) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							AndroidUtils.showToast(Register_Activity.this, R.string.net_conn_timeout);
							mRegisterBtn.setClickable(true);
						}
					});
				}
				break;
			case NET_ERROR_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						AndroidUtils.showToast(Register_Activity.this, R.string.net_error);
						mRegisterBtn.setClickable(true);
					}
				});
				break;
			/*case REQUEST_CODE_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						registerStatusPrompt(responseStatus);
						mRegisterBtn.setClickable(true);
					}
				});
				break;*/
			default:
				break;
			}
		}
		
	}

}
