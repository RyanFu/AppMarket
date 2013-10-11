package com.dongji.market.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dongji.market.R;
import com.dongji.market.database.MarketDatabase.Setting_Service;
import com.dongji.market.download.DownloadConstDefine;
import com.dongji.market.helper.AConstDefine;

public class SettingFlowDialog extends Dialog {
	private Context cxt;
	private EditText etInputFlow;
	private Button btnCancel;
	private Button btnConfirm;
	private View mContentView;
	private InputMethodManager imm;

	public SettingFlowDialog(Context context) {
		super(context, R.style.dialog_progress_default);
		cxt = context;
		initViews();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
	}

	private void initViews() {
		mContentView = getLayoutInflater().inflate(R.layout.widget_settingflow_dialog, null);
		etInputFlow = (EditText) mContentView.findViewById(R.id.etInputFlow);
		btnCancel = (Button) mContentView.findViewById(R.id.btnCancel);
		btnConfirm = (Button) mContentView.findViewById(R.id.btnConfirm);

		etInputFlow.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				(new Handler()).postDelayed(new Runnable() {

					@Override
					public void run() {
						imm = (InputMethodManager) cxt.getSystemService(Context.INPUT_METHOD_SERVICE);
						if (hasFocus) {
							imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
						}
					}
				}, 100);
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingFlowDialog.this.dismiss();
			}
		});

		btnConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if ("".equals(etInputFlow.getText().toString()) || Integer.valueOf(etInputFlow.getText().toString()) == 0) {
					Toast.makeText(cxt, cxt.getResources().getString(R.string.flow_val_prompt), Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(AConstDefine.BROADCAST_ACTION_DIALOG_LIMITFLOWCHANGE);
				cxt.sendBroadcast(intent);

				int setttingflow = Integer.valueOf(etInputFlow.getText().toString().trim());
				// TODO 修改设置界面数据库的参数
				Setting_Service service;
				service = new Setting_Service(cxt);
				service.update("limit_flow", setttingflow);
				// 每次修改限制流量必须清零已使用流量
				SharedPreferences pref = cxt.getSharedPreferences(AConstDefine.DONGJI_SHAREPREFERENCES, Context.MODE_PRIVATE);
				Editor editor = pref.edit();
				editor.putLong(AConstDefine.SHARE_DOWNLOADSIZE, 0);
				editor.commit();

				// 设置好流量后通知 service 继续下载
				Intent trafficIntent = new Intent(DownloadConstDefine.BROADCAST_ACTION_GPRS_SETTING_CHANGE);
				Bundle bundle = new Bundle();
				bundle.putLong("limitFlow", setttingflow);
				bundle.putBoolean("isOnlyWifi", false);
				trafficIntent.putExtras(bundle);
				cxt.sendBroadcast(trafficIntent);

				SettingFlowDialog.this.dismiss();
			}
		});

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
}
