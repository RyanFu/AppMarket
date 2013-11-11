package com.dongji.market.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
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
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.pojo.LoginParams;

/**
 * 登陆Dialog
 * 
 * @author yvon
 * 
 */
public class LoginDialog extends Dialog implements AConstDefine {
	private Context cxt;
	private WebView mWebView;
	private View mContentView;
	private TextView tvLocalTip;
	private TextView tvCloudTip;
	private Button btnLocalBackupOrRestore, mLoginButton;
	private CustomDialog localBackupDialog, localRestoreDialog;
	private int flag;

	public LoginDialog(Context context, int flag) {
		super(context, R.style.dialog_progress_default);
		cxt = context;
		this.flag = flag;
		initViews();
	}
	
	
	private void initViews() {
		mContentView = getLayoutInflater().inflate(R.layout.widget_login_dialog, null);
		tvLocalTip = (TextView) mContentView.findViewById(R.id.tvLocalTip);
		tvCloudTip = (TextView) mContentView.findViewById(R.id.tvCloudTip);
		btnLocalBackupOrRestore = (Button) mContentView.findViewById(R.id.btnLocalBackupOrRestore);
		mLoginButton = (Button) mContentView.findViewById(R.id.btnlogin);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}

	};
	

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
				Intent intent = new Intent(cxt, Login_Activity.class);
				intent.putExtra("source", AConstDefine.LOGIN_SOURCE);
				((Activity) cxt).startActivityForResult(intent, flag);
				dismiss();
			}
		});
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
		mWebView.loadUrl(AConstDefine.DIALOG_LOGIN_URL);
		mWebView.requestFocus();
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.indexOf(AConstDefine.REGISTER_LOGIN_SUCCESS_URL) != -1) {
					String userName = DJMarketUtils.urlDecode(DJMarketUtils.getCookieValue(cxt.getApplicationContext(), url, "market_username"));
					LoginParams loginParams = ((AppMarket) cxt.getApplicationContext()).getLoginParams();
					loginParams.setUserName(userName);
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

						localBackupDialog.setTextHeight(DJMarketUtils.dip2px(cxt, getHeightFromText(R.string.cloud_backup_local_tip)));
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
						localRestoreDialog.setTextHeight(DJMarketUtils.dip2px(cxt, getHeightFromText(R.string.local_restore_tip)));
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
	
	private int getHeightFromText(int textId) {
		int textSize = getContext().getString(textId).length();
		int lines = textSize / 14 + 1;
		if (textSize % 14 == 0) {
			lines--;
		}

		System.out.println("width............lines" + lines);
		return 22 * lines;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
		refreshContent();
	}

}
