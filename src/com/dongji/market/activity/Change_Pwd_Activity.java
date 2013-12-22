package com.dongji.market.activity;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.dongji.market.R;
import com.dongji.market.application.AppMarket;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.CustomProgressDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * 修改密码
 * @author yvon
 *
 */
public class Change_Pwd_Activity extends Activity implements OnClickListener{
	
	private EditText mOldPwdET;
	private EditText mNewPwdET;
	private EditText mNewPwdRepeatET;
	
	private Button mConfirmBtn;
	
	private TitleUtil titleUtil;
	
	private MyHandler mHandler;
	private LoginParams loginParams;
	
	private InputMethodManager imm;
	
	private int responseStatus;
	private static final int EVENT_MODIFY_PWD = 0;
	private static final int CHECK_RESPONSE_TIMEOUT = 1;
	private static final int NET_ERROR_PROMPT = 2;
	private static final int EVENT_MODIFY_PWD_PROMPT = 3;
	
	private String emailString;
	
	private static final int MIN_PASSWORD_LENGTH = 6; // 密码最小长度不能小于6
	private CustomProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pwd);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		View mTopView = findViewById(R.id.chg_pwd_top);
		titleUtil = new TitleUtil(this,mTopView,R.string.change_pwd, null, null);
		
		AppMarket mApp=(AppMarket)getApplication();
		emailString=mApp.getLoginParams().getUserName();
		
		mOldPwdET = (EditText) findViewById(R.id.old_pwd);
		mNewPwdET = (EditText) findViewById(R.id.new_pwd);
		mNewPwdRepeatET = (EditText) findViewById(R.id.new_pwd_repeat);
		mConfirmBtn = (Button) findViewById(R.id.chg_pwd_confirm);
		
		//设置获取焦点时弹出输入法
		mOldPwdET.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				(new Handler()).postDelayed(new Runnable() {
					
					@Override
					public void run() {
						imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (hasFocus) {
//							imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
							imm.showSoftInput(mOldPwdET, InputMethodManager.SHOW_IMPLICIT);
						} /*else {
							imm.hideSoftInputFromWindow(mEmailET.getWindowToken(), 0);
						}*/
					}
				}, 100);
			}
		});
		
		mConfirmBtn.setOnClickListener(this);
		
		initHandler();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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
	
	private void change_confirm() {
		String oldPwdStr = mOldPwdET.getText().toString();
		String newPwdStr = mNewPwdET.getText().toString();
		String newPwdRepeatStr = mNewPwdRepeatET.getText().toString();
		
		try {
			int type = DataManager.newInstance().modifyPwd(emailString, oldPwdStr, newPwdStr, newPwdRepeatStr);
			switch(type) {
				case 1:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.change_pwd_success);
							finish();
						}
					});
					break;
				case -1:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.old_password_error);
							mOldPwdET.requestFocus();
						}
					});
					break;
					default:
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.modify_password_fail);
							}
						});
						break;
			}
		} catch (IOException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.connection_timeout);
				}
			});
		} catch (JSONException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.modify_password_fail);
				}
			});
		}finally {
			dismissProgressDialog();
		}
	}
	
	private void modifyPwdPrompt(int status) {
		switch (status) {
		case 0:			//必填项不能为空
			DJMarketUtils.showToast(this, R.string.must_input);
			break;
		case 1:			//修改成功
			DJMarketUtils.showToast(this, R.string.modify_success);
			if (loginParams == null) {
				loginParams = ((AppMarket)getApplicationContext()).getLoginParams();
			}
//			loginParams.setLoginState(AConstDefine.LOGIN_SUCCESS_FLAG);
			finish();
			break;
		case -1:		//旧密码不正确
			DJMarketUtils.showToast(this, R.string.old_pwd_error);
			break;
		case -2:		//两次密码不一致
			DJMarketUtils.showToast(this, R.string.pwd_discord);
			break;
		case -4:		//Email格式有误
			DJMarketUtils.showToast(this, R.string.email_format_error);
			break;
		case -5:		//Email不允许注册
			DJMarketUtils.showToast(this, R.string.email_unallowed_reg);
			break;
		case -6:		//该 Email已经被注册
			DJMarketUtils.showToast(this, R.string.email_already_reg);
			break;
		case -8:		//该用户受保护，无权限更改
			DJMarketUtils.showToast(this, R.string.no_authority_modify);
			break;
		case -9:		//密码长度在6-15位之间
			DJMarketUtils.showToast(this, R.string.pwd_length_error);
			break;
		case -10000:	//登陆失败(服务器错误)
			DJMarketUtils.showToast(this, R.string.login_failed);
			break;
		case -9999:		//提交方式错误
			DJMarketUtils.showToast(this, R.string.commit_error);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chg_pwd_confirm:		//缺邮箱已占用验证
			if (checkRegisterParams()) {
				mHandler.sendEmptyMessage(EVENT_MODIFY_PWD);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 验证注册参数的合法性
	 * @return
	 */
	private boolean checkRegisterParams() {
		String oldPwdStr=mOldPwdET.getText().toString();
		String pwdStr = mNewPwdET.getText().toString();
		String pwdAgainStr = mNewPwdRepeatET.getText().toString();
		if(TextUtils.isEmpty(oldPwdStr)) {
			DJMarketUtils.showToast(this, R.string.pwd_is_notnull);
			mOldPwdET.requestFocus();
		}else if(oldPwdStr.length()<MIN_PASSWORD_LENGTH) {
			DJMarketUtils.showToast(this, R.string.pwd_is_notlowsix);
			mOldPwdET.requestFocus();
		}else if(TextUtils.isEmpty(pwdStr) || TextUtils.isEmpty(pwdAgainStr)) {
			DJMarketUtils.showToast(this, R.string.pwd_is_notnull);
			if(TextUtils.isEmpty(pwdStr) || (TextUtils.isEmpty(pwdStr) && TextUtils.isEmpty(pwdAgainStr))) {
				mNewPwdET.requestFocus();
			}else if(TextUtils.isEmpty(pwdAgainStr)) {
				mNewPwdRepeatET.requestFocus();
			}
		} else if(pwdStr.length()<MIN_PASSWORD_LENGTH || pwdAgainStr.length()<MIN_PASSWORD_LENGTH) {
			DJMarketUtils.showToast(this, R.string.pwd_is_notlowsix);
			if(pwdStr.length()<MIN_PASSWORD_LENGTH || (pwdStr.length()<MIN_PASSWORD_LENGTH && pwdAgainStr.length()<MIN_PASSWORD_LENGTH)) {
				mNewPwdET.requestFocus();
			}else if(pwdAgainStr.length()<MIN_PASSWORD_LENGTH) {
				mNewPwdRepeatET.requestFocus();
			}
		}else if(!pwdStr.equals(pwdAgainStr)) {
			DJMarketUtils.showToast(this, R.string.pwd_is_different);
			mNewPwdRepeatET.requestFocus();
		}else if(!DJMarketUtils.isNetworkAvailable(this)) {
			DJMarketUtils.showToast(this, R.string.no_web_to_login);
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.getCurrentFocus() != null) {
				if (this.getCurrentFocus().getWindowToken() != null) {
					if(imm==null) {
						imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					}
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
			if (mHandler.hasMessages(EVENT_MODIFY_PWD)) {
				mHandler.removeMessages(EVENT_MODIFY_PWD);
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
			case EVENT_MODIFY_PWD:
				change_confirm();
				break;
			case CHECK_RESPONSE_TIMEOUT:
				if (responseStatus == AConstDefine.THRESHOLD) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.net_conn_timeout);
							mConfirmBtn.setClickable(true);
						}
					});
				}
				break;
			case EVENT_MODIFY_PWD_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						modifyPwdPrompt(responseStatus);
						mConfirmBtn.setClickable(true);
					}
				});
				break;
			case NET_ERROR_PROMPT:
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DJMarketUtils.showToast(Change_Pwd_Activity.this, R.string.net_error);
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
