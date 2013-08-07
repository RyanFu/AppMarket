package com.dongji.market.widget;

import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.dongji.market.R;
import com.dongji.market.activity.Login_Activity;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;

public class LoginDialog2 extends Dialog implements android.view.View.OnClickListener{
	
	private Context context;
	private TextView mETRandomName;
	private EditText mETPassword;
	private Button mConfirmBtn, mCancelBtn;
	private View mContentView;
	private InputMethodManager imm;
	
	private Handler handler;
	
	public LoginDialog2(Context context, Handler handler) {
		super(context, R.style.dialog_progress_default);
		this.context = context;
		this.handler = handler;
		initViews();
	}

	private void initViews() {
		mContentView = getLayoutInflater().inflate(R.layout.widget_login_dialog2, null);
		mETRandomName = (TextView) mContentView.findViewById(R.id.et_random_name);
		mETPassword = (EditText) mContentView.findViewById(R.id.et_login_password);
		mConfirmBtn = (Button) mContentView.findViewById(R.id.confirm_twitter_register);
		mCancelBtn = (Button) mContentView.findViewById(R.id.cancel_twitter_register);
		
		mETRandomName.setText("S_" + getRandomName(6));
		
//		mETPassword.requestFocus();
		
		mETPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
						if (hasFocus) {
							imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//							imm.showSoftInput(mETPassword, InputMethodManager.SHOW_FORCED);
						}
					}
				}, 100);
			}
		});
		
		mETPassword.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					regAndLogin();
				}
				return false;
			}
		});
		
		mConfirmBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null) {
				if (getCurrentFocus().getWindowToken() != null) {
					if (imm == null) {
						imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
					}
					imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.confirm_twitter_register:
			regAndLogin();
			break;
		case R.id.cancel_twitter_register:
			dismiss();
			break;
		default:
			break;
		}
	}

	private void regAndLogin() {
		if (loginMtd()) {
			context.startService(new Intent(
					"com.dongji.market.loginService"));// 启动登录服务

			SharedPreferences loginPref = context.getSharedPreferences(
					// 更改状态
					AConstDefine.DONGJI_SHAREPREFERENCES,
					Context.MODE_PRIVATE);
			Editor editor = loginPref.edit();
			editor.putInt("loginState", 1);// 正在登录
			editor.putLong("startLoginTime", System.currentTimeMillis());
			editor.commit();
			handler.sendEmptyMessage(Login_Activity.EXIT_LOGINPAGE);// 关闭登录界面
			dismiss();
		}
	}
	
	private String getRandomName(int length) {
		String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
	
	private boolean loginMtd() {
		String pwdStr = mETPassword.getText().toString();
		if (!AndroidUtils.isNetworkAvailable(context)) {
			AndroidUtils.showToast(context, R.string.net_error);
			return false;
		} else if (TextUtils.isEmpty(pwdStr)) {
			AndroidUtils.showToast(context, R.string.password_null);
			return false;
		} else if (!AndroidUtils.passwdFormat(pwdStr)) {
			AndroidUtils.showToast(context, R.string.passwd_format_error);
			return false;
		} else {
			AndroidUtils.showToast(context, R.string.is_logining);
			return true;
		}
	}

}
