package com.dongji.market.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.Login_Activity;
import com.dongji.market.activity.Uninstall_list_Activity;
import com.dongji.market.application.AppMarket;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.LoginParams;

public class LoginDialog extends Dialog implements AConstDefine {
	private Context cxt;
	/*
	 * private EditText mEmailET; private EditText mPasswordEt; private Button
	 * mLoginBtn; private Button btnRegisterNow; private Button btnFindPassword;
	 */
	// private TextView mPrompt;
	private WebView mWebView;
	private View mContentView;
	private InputMethodManager imm;

	private TextView tvLocalTip;
	private TextView tvCloudTip;
	private Button btnLocalBackupOrRestore, mLoginButton;

	private View mContentLayout;
	private View mContentLayout2;

	private CustomDialog localBackupDialog, localRestoreDialog;

	private int flag;

	public LoginDialog(Context context, int flag) {
		super(context, R.style.dialog_progress_default);
		cxt = context;
		this.flag = flag;
		initViews();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
		refreshContent();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	};

	private void initViews() {
		mContentView = getLayoutInflater().inflate(R.layout.widget_login_dialog, null);
		// mPrompt = (TextView)
		// mContentView.findViewById(R.id.login_dialog_prompt);
		/*
		 * mEmailET = (EditText) mContentView
		 * .findViewById(R.id.login_dialog_email); mPasswordEt = (EditText)
		 * mContentView .findViewById(R.id.login_dialog_password); mLoginBtn =
		 * (Button) mContentView .findViewById(R.id.login_dialog_confirm);
		 * btnRegisterNow = (Button) mContentView
		 * .findViewById(R.id.btnRegisterNow); btnFindPassword = (Button)
		 * mContentView .findViewById(R.id.btnFindPassword);
		 */

		mContentLayout = mContentView.findViewById(R.id.contentlayout);
		mContentLayout2 = mContentView.findViewById(R.id.contentlayout2);

		tvLocalTip = (TextView) mContentView.findViewById(R.id.tvLocalTip);
		tvCloudTip = (TextView) mContentView.findViewById(R.id.tvCloudTip);
		btnLocalBackupOrRestore = (Button) mContentView.findViewById(R.id.btnLocalBackupOrRestore);

		// refreshContent();

		mLoginButton = (Button) mContentView.findViewById(R.id.btnlogin);

	}

	public void refreshContent() {
		if (flag == Uninstall_list_Activity.FLAG_BACKUP) {
			tvLocalTip.setText(cxt.getString(R.string.local_backup));
			tvCloudTip.setText(cxt.getString(R.string.cloud_backup));
			btnLocalBackupOrRestore.setText(cxt.getString(R.string.local_backup));
		} else if (flag == Uninstall_list_Activity.FLAG_RESTORE) {
			tvLocalTip.setText(cxt.getString(R.string.cloud_restore_local));
			tvCloudTip.setText(cxt.getString(R.string.cloud_restore));
			btnLocalBackupOrRestore.setText(cxt.getString(R.string.cloud_restore_local));
		}

		btnLocalBackupOrRestore.setOnClickListener(new onBtnClickListener(flag));

		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// applyRotation(0, 0, 90);
				// applyRotation(-1, 180, 90);
				Intent intent = new Intent(cxt, Login_Activity.class);
				intent.putExtra("source", AConstDefine.LOGIN_SOURCE);
				((Activity) cxt).startActivityForResult(intent, flag);
				dismiss();
			}
		});

		/*
		 * mEmailET.setOnFocusChangeListener(new OnFocusChangeListener() {
		 * 
		 * @Override public void onFocusChange(View v, final boolean hasFocus) {
		 * (new Handler()).postDelayed(new Runnable() {
		 * 
		 * @Override public void run() { imm = (InputMethodManager)
		 * cxt.getSystemService(Context.INPUT_METHOD_SERVICE); if (hasFocus) {
		 * // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		 * imm.showSoftInput(mEmailET, InputMethodManager.SHOW_IMPLICIT); } } },
		 * 100); } });
		 * 
		 * mPasswordEt.setOnEditorActionListener(new OnEditorActionListener() {
		 * 
		 * public boolean onEditorAction(TextView v, int actionId, KeyEvent
		 * event) { if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
		 * login(); } return false; } }); defaultLoginOnClickListener();
		 * registerNowOnClickListener(); forgetPasswdOnClickListener();
		 */
		initLoginView();
	}

	private void initLoginView() {
		mWebView = (WebView) mContentView.findViewById(R.id.webView);
		mWebView.setVerticalScrollBarEnabled(true);
		mWebView.setHorizontalScrollBarEnabled(false);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSaveFormData(true);
		webSettings.setSavePassword(true);
		webSettings.setSupportZoom(false);
		// webSettings.setUseWideViewPort(true);
		// webSettings.setLoadWithOverviewMode(true);
		mWebView.loadUrl(AConstDefine.DIALOG_LOGIN_URL);
		mWebView.requestFocus();
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.indexOf(AConstDefine.REGISTER_LOGIN_SUCCESS_URL) != -1) {
					// String sessionId = DJMarketUtils.getCookieValue(
					// cxt.getApplicationContext(), url, "sessionid");
					String userName = DJMarketUtils.urlDecode(DJMarketUtils.getCookieValue(cxt.getApplicationContext(), url, "market_username"));
					LoginParams loginParams = ((AppMarket) cxt.getApplicationContext()).getLoginParams();
					// loginParams.setSessionId(sessionId);
					loginParams.setUserName(userName);
					// loginParams.setLoginState(AConstDefine.LOGIN_SUCCESS_FLAG);
					if (flag == Uninstall_list_Activity.FLAG_BACKUP) {
						Intent intent = new Intent(BROADCAST_ACTION_SHOWBANDRLIST);
						intent.putExtra(FLAG_ACTIVITY_BANDR, ACTIVITY_CLOUD_BACKUP);
						cxt.sendBroadcast(intent);
						dismiss();
					} else if (flag == Uninstall_list_Activity.FLAG_RESTORE) {
						Intent intent = new Intent(BROADCAST_ACTION_SHOWBANDRLIST);
						intent.putExtra(FLAG_ACTIVITY_BANDR, ACTIVITY_CLOUD_RESTORE);
						cxt.sendBroadcast(intent);
						dismiss();
					}
				}
				return false;
			}

		});
	}

	private int getHeightFromText(int textId) {
		int textSize = getContext().getString(textId).length();
		int lines = textSize / 14 + 1;
		if (textSize % 14 == 0) {
			lines--;
		}

		System.out.println("width............lines" + lines);
		return 22 * lines;
	}

	private class onBtnClickListener implements View.OnClickListener {
		private int flag;

		public onBtnClickListener(int flag) {
			this.flag = flag;
		}

		@Override
		public void onClick(View v) {
			if (flag == Uninstall_list_Activity.FLAG_BACKUP) {
				if (!((Activity) cxt).isFinishing()) {
					if (localBackupDialog == null) {
						localBackupDialog = new CustomDialog(cxt).setIcon(R.drawable.icon);
						localBackupDialog.setTitle(R.string.local_backup);

						localBackupDialog.setTextHeight(AndroidUtils.dip2px(cxt, getHeightFromText(R.string.cloud_backup_local_tip)));
						localBackupDialog.setMessage(R.string.cloud_backup_local_tip).setPositiveButton(R.string.chooseapptobackup, new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(BROADCAST_ACTION_SHOWBANDRLIST);
								intent.putExtra(FLAG_ACTIVITY_BANDR, ACTIVITY_BACKUP);
								cxt.sendBroadcast(intent);
								localBackupDialog.dismiss();
							}
						}).setNegativeButton(R.string.exit, new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								localBackupDialog.dismiss();
							}
						});
					}
					if (localBackupDialog != null) {
						localBackupDialog.show();
						localBackupDialog.setAttributes(R.string.cloud_backup_local_tip);
					}
				}
				LoginDialog.this.dismiss();
			} else if (flag == Uninstall_list_Activity.FLAG_RESTORE) {
				if (!((Activity) cxt).isFinishing()) {
					if (localRestoreDialog == null) {
						localRestoreDialog = new CustomDialog(cxt).setIcon(R.drawable.icon);
						localRestoreDialog.setTitle(R.string.local_restore);
						localRestoreDialog.setTextHeight(AndroidUtils.dip2px(cxt, getHeightFromText(R.string.local_restore_tip)));
						localRestoreDialog.setMessage(R.string.local_restore_tip).setPositiveButton(R.string.chooseapptorestore, new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(BROADCAST_ACTION_SHOWBANDRLIST);
								intent.putExtra(FLAG_ACTIVITY_BANDR, ACTIVITY_RESTORE);
								cxt.sendBroadcast(intent);
								localRestoreDialog.dismiss();
							}
						}).setNegativeButton(R.string.exit, new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								localRestoreDialog.dismiss();
							}
						});
					}
					if (localRestoreDialog != null) {
						localRestoreDialog.show();
						localRestoreDialog.setAttributes(R.string.local_restore_tip);
					}
				}
				LoginDialog.this.dismiss();
			}

		}
	}

	private void applyRotation(int position, float start, float end) {
		final float centerX = mContentView.getWidth() / 2.0f;
		final float centerY = mContentView.getHeight() / 2.0f;

		final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
		rotation.setDuration(250);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView(position));

		mContentView.startAnimation(rotation);
	}

	private final class DisplayNextView implements Animation.AnimationListener {
		private final int mPosition;

		private DisplayNextView(int position) {
			mPosition = position;
		}

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			mContentView.post(new SwapViews(mPosition));
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}

	private final class SwapViews implements Runnable {
		private final int mPosition;

		public SwapViews(int position) {
			mPosition = position;
		}

		public void run() {
			final float centerX = mContentView.getWidth() / 2.0f;
			final float centerY = mContentView.getHeight() / 2.0f;
			Rotate3dAnimation rotation;

			if (mPosition > -1) {
				mContentLayout.setVisibility(View.GONE);
				mContentLayout2.setVisibility(View.VISIBLE);
				mContentLayout2.requestFocus();

				rotation = new Rotate3dAnimation(90, 180, centerX, centerY, 310.0f, false);//
			} else {
				mContentLayout2.setVisibility(View.GONE);
				mContentLayout.setVisibility(View.VISIBLE);
				mContentLayout.requestFocus();

				rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
			}

			rotation.setDuration(250);
			rotation.setFillAfter(false);
			rotation.setInterpolator(new DecelerateInterpolator());

			mContentView.startAnimation(rotation);
		}
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	// public LoginDialog setPrompt(int promptId) {
	// mPrompt.setText(promptId);
	// return this;
	// }

	/*
	 * private void defaultLoginOnClickListener() {
	 * mLoginBtn.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { login(); } }); }
	 * 
	 * private boolean loginMtd() { String emailStr =
	 * mEmailET.getText().toString(); String passwordStr =
	 * mPasswordEt.getText().toString(); if
	 * (!AndroidUtils.isNetworkAvailable(cxt)) { AndroidUtils.showToast(cxt,
	 * R.string.net_error); return false; } else if
	 * (TextUtils.isEmpty(emailStr)) { AndroidUtils.showToast(cxt,
	 * R.string.email_null); return false; } else if
	 * (!AndroidUtils.isEmail(emailStr)) { AndroidUtils.showToast(cxt,
	 * R.string.email_format_error); return false; } else if
	 * (TextUtils.isEmpty(passwordStr)) { AndroidUtils.showToast(cxt,
	 * R.string.password_null); return false; } else if
	 * (!AndroidUtils.passwdFormat(passwordStr)) { AndroidUtils.showToast(cxt,
	 * R.string.passwd_format_error); return false; } else {
	 * AndroidUtils.showToast(cxt, R.string.is_logining); return true; } }
	 * 
	 * private void login() { if (loginMtd()) { Intent intent=new
	 * Intent("com.dongji.market.loginService");
	 * intent.putExtra(DIALOG_LOGIN,true);
	 * if(flag==Uninstall_list_Activity.FLAG_BACKUP){
	 * intent.putExtra(FLAG_ACTIVITY_BANDR, ACTIVITY_CLOUD_BACKUP); }else{
	 * intent.putExtra(FLAG_ACTIVITY_BANDR, ACTIVITY_CLOUD_RESTORE); }
	 * 
	 * cxt.startService(intent); // 登录成功弹出成功提示，登录超时弹出超时提示 SharedPreferences
	 * loginPref = cxt.getSharedPreferences( DONGJI_SHAREPREFERENCES,
	 * Context.MODE_PRIVATE); Editor editor = loginPref.edit();
	 * editor.putInt("loginState", 1);// 正在登录 editor.putLong("startLoginTime",
	 * System.currentTimeMillis()); editor.commit(); dismiss(); } }
	 */

	/*
	 * private void registerNowOnClickListener() {
	 * btnRegisterNow.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { Intent intent = new Intent(cxt,
	 * Register_Activity.class); cxt.startActivity(intent); dismiss(); } }); }
	 */

	/*
	 * private void forgetPasswdOnClickListener() {
	 * btnFindPassword.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { Intent intent = new Intent(cxt,
	 * Passwd_Retake_Activity.class); cxt.startActivity(intent); dismiss(); }
	 * }); }
	 */

	/*
	 * private boolean loginMtd() { String emailStr =
	 * mEmailET.getText().toString(); String passwordStr =
	 * mPasswordEt.getText().toString(); if
	 * (!AndroidUtils.isNetworkAvailable(cxt)) { // Toast.makeText(this,
	 * R.string.net_error, // Toast.LENGTH_SHORT).show();
	 * AndroidUtils.showToast(cxt, R.string.net_error); return false; } else if
	 * (!AndroidUtils.isEmail(emailStr)) { // Toast.makeText(this,
	 * R.string.email_format_error, // Toast.LENGTH_SHORT).show();
	 * AndroidUtils.showToast(cxt, R.string.email_format_error); return false; }
	 * else if (!AndroidUtils.passwdFormat(passwordStr)) { //
	 * Toast.makeText(this, R.string.passwd_format_error, //
	 * Toast.LENGTH_SHORT).show(); AndroidUtils.showToast(cxt,
	 * R.string.passwd_format_error); return false; } else { if (!userIsExist())
	 * { AndroidUtils.showToast(cxt, "此用户不存在,请先注册!"); } else { //用户存在,登录中...
	 * dismiss(); } return true; } }
	 */

}
