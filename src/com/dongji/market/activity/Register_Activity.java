package com.dongji.market.activity;

import java.io.IOException;

import org.myjson.JSONException;

import android.app.Activity;
import android.content.Context;
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
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.TitleUtil;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.CustomProgressDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * 注册页面
 * @author yvon
 *
 */
public class Register_Activity extends Activity implements OnClickListener {

	private EditText mEmailET;
	private EditText mPasswordET;
	private EditText mPwdAgainET;

	private Button mRegisterBtn;

	private TitleUtil titleUtil;
	private MyHandler mHandler;

	private InputMethodManager imm;

	private static final int EVENT_REGISTER_FLAG = 0;

	private static final int MAX_EMAIL_LENGTH = 50; // 邮箱最长字符限制
	private static final int MIN_PASSWORD_LENGTH = 6; // 密码最小长度不能小于6
	private CustomProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
							imm.showSoftInput(mEmailET, InputMethodManager.SHOW_IMPLICIT);
						}
					}
				}, 100);
			}
		});

		mPwdAgainET.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					mHandler.sendEmptyMessage(EVENT_REGISTER_FLAG);
				}
				return false;
			}
		});

		mRegisterBtn.setOnClickListener(this);

		initHandler();
	}

	private void initHandler() {
		HandlerThread thread = new HandlerThread("handler");
		thread.start();
		mHandler = new MyHandler(thread.getLooper());
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REGISTER_FLAG:
				register_confirm();
				break;
			}
		}
	}

	private void register_confirm() {
		final String emailStr = mEmailET.getText().toString();
		final String pwdStr = mPasswordET.getText().toString();
		String pwdAgainStr = mPwdAgainET.getText().toString();
		try {
			int type = DataManager.newInstance().register(emailStr, pwdStr, pwdAgainStr);
			System.out.println("type:" + type);
			switch (type) {
			case 1:
				AppMarket mApp = ((AppMarket) getApplication());
				mApp.getLoginParams().setUserName(emailStr);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DJMarketUtils.showToast(Register_Activity.this, R.string.register_and_login);
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
						DJMarketUtils.showToast(Register_Activity.this, R.string.email_occupied_error);
						mEmailET.requestFocus();
					}
				});
				break;
			default:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DJMarketUtils.showToast(Register_Activity.this, R.string.register_fail);
					}
				});
				break;
			}
		} catch (IOException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DJMarketUtils.showToast(Register_Activity.this, R.string.register_timeout);
				}
			});
		} catch (JSONException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DJMarketUtils.showToast(Register_Activity.this, R.string.register_fail);
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

	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		titleUtil.showOrDismissSettingPopupWindow();
		return false;
	}

	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressDialog(this);
			mProgressDialog.setContentText(getString(R.string.registering));
		}
		if (!isFinishing() & !mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reg_register: // 缺邮箱已占用验证
			if (checkRegisterParams()) {
				if (imm != null) {
					imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
				mHandler.sendEmptyMessage(EVENT_REGISTER_FLAG);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 验证注册参数的合法性
	 * 
	 * @return
	 */
	private boolean checkRegisterParams() {
		String emailString = mEmailET.getText().toString();
		String pwdStr = mPasswordET.getText().toString();
		String pwdAgainStr = mPwdAgainET.getText().toString();
		if (TextUtils.isEmpty(emailString)) {
			DJMarketUtils.showToast(this, R.string.email_null);
			mEmailET.requestFocus();
		} else if (!DJMarketUtils.isEmail(emailString)) {
			DJMarketUtils.showToast(this, R.string.email_format_error);
			mEmailET.requestFocus();
		} else if (TextUtils.isEmpty(pwdStr) || TextUtils.isEmpty(pwdAgainStr)) {
			DJMarketUtils.showToast(this, R.string.password_null);
			if (TextUtils.isEmpty(pwdStr) || (TextUtils.isEmpty(pwdStr) && TextUtils.isEmpty(pwdAgainStr))) {
				mPasswordET.requestFocus();
			} else if (TextUtils.isEmpty(pwdAgainStr)) {
				mPwdAgainET.requestFocus();
			}
		} else if (emailString.length() > MAX_EMAIL_LENGTH) {
			DJMarketUtils.showToast(this, R.string.email_is_notoverfifty);
			mEmailET.requestFocus();
		} else if (pwdStr.length() < MIN_PASSWORD_LENGTH || pwdAgainStr.length() < MIN_PASSWORD_LENGTH) {
			DJMarketUtils.showToast(this, R.string.pwd_is_notlowsix);
			if (pwdStr.length() < MIN_PASSWORD_LENGTH || (pwdStr.length() < MIN_PASSWORD_LENGTH && pwdAgainStr.length() < MIN_PASSWORD_LENGTH)) {
				mPasswordET.requestFocus();
			} else if (pwdAgainStr.length() < MIN_PASSWORD_LENGTH) {
				mPwdAgainET.requestFocus();
			}
		} else if (!pwdStr.equals(pwdAgainStr)) {
			DJMarketUtils.showToast(this, R.string.pwd_is_different);
			mPwdAgainET.requestFocus();
		} else if (!DJMarketUtils.isNetworkAvailable(this)) {
			DJMarketUtils.showToast(this, R.string.no_network_msg1);
		} else {
			showProgressDialog();
			return true;
		}
		return false;
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
		super.onResume();
		MobclickAgent.onResume(this);
		if (titleUtil != null) {
			titleUtil.sendRefreshHandler();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		if (titleUtil != null) {
			titleUtil.removeRefreshHandler();
		}
		if (isFinishing()) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	protected void onDestroy() {
		titleUtil.unregisterMyReceiver(this);
		removeMessage();
		super.onDestroy();
	}

	private void removeMessage() {
		if (mHandler != null) {
			if (mHandler.hasMessages(EVENT_REGISTER_FLAG)) {
				mHandler.removeMessages(EVENT_REGISTER_FLAG);
			}
		}
	}

}
