package com.dongji.market.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongji.market.R;

public class CustomDialog extends Dialog {
	private Context cxt;
	private ImageView mIcon;
	private TextView mTitle;
	private TextView mMessage;
	private Button mPositiveBtn;
	private Button mNegativeBtn;
	private View mContentView;

	public CustomDialog(Context context) {
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
		mContentView = getLayoutInflater().inflate(R.layout.widget_uninstall_dialog, null);
		mIcon = (ImageView) mContentView.findViewById(R.id.uninstall_dialog_icon);
		mTitle = (TextView) mContentView.findViewById(R.id.uninstall_dialog_name);
		mMessage = (TextView) mContentView.findViewById(R.id.uninstall_dialog_message);
		mPositiveBtn = (Button) mContentView.findViewById(R.id.confirm_uninstall);
		mNegativeBtn = (Button) mContentView.findViewById(R.id.cancel_uninstall);
		defaultPositiveOnClickListener();
		defaultNegativeOnClickListener();
	}

	public CustomDialog setIcon(Drawable drawable) {
		mIcon.setImageDrawable(drawable);
		return this;
	}

	public CustomDialog setTitle(String title) {
		mTitle.setText(title);
		return this;
	}

	public CustomDialog setAttributes(int messageId) {
		Window dialogWindow = this.getWindow();
		WindowManager windowManager = dialogWindow.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams params = dialogWindow.getAttributes();
		int dis_width = display.getWidth();
		float lv = 0.8f;
		if (dis_width < 400) {
			lv = 0.9f;
		}
		params.width = (int) (display.getWidth() * lv);
		dialogWindow.setAttributes(params);
		return this;
	}

	public CustomDialog setMessage(String message) {
		mMessage.setText(message);
		return this;
	}

	public CustomDialog setTextHeight(int height) {
		LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) mMessage.getLayoutParams();
		mParams.height = height;
		mMessage.setLayoutParams(mParams);
		return this;
	}

	public CustomDialog setIcon(int id) {
		mIcon.setImageDrawable(cxt.getResources().getDrawable(id));
		return this;
	}

	public void setTitle(int id) {// 实则返回UninstallDialog类型，???
		if (null != cxt.getResources().getString(id)) {
			mTitle.setText(cxt.getResources().getString(id));
		}
		// return this;
	}

	public CustomDialog setMessage(int id) {
		if (mMessage != null) {
			mMessage.setText(cxt.getResources().getString(id));
		}
		return this;
	}

	private void defaultPositiveOnClickListener() {
		mPositiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	private void defaultNegativeOnClickListener() {
		mNegativeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public CustomDialog setPositiveButton(String name, View.OnClickListener listener) {
		mPositiveBtn.setText(name);
		if (listener != null) {
			mPositiveBtn.setOnClickListener(listener);
		}
		return this;
	}

	public CustomDialog setNegativeButton(String name, View.OnClickListener listener) {
		mNegativeBtn.setText(name);
		if (listener != null) {
			mNegativeBtn.setOnClickListener(listener);
		}
		return this;
	}

	public CustomDialog setPositiveButton(int sId, View.OnClickListener listener) {
		mPositiveBtn.setText(cxt.getResources().getString(sId));
		if (listener != null) {
			mPositiveBtn.setOnClickListener(listener);
		}
		return this;
	}

	public CustomDialog setNegativeButton(int sId, View.OnClickListener listener) {
		mNegativeBtn.setText(cxt.getResources().getString(sId));
		if (listener != null) {
			mNegativeBtn.setOnClickListener(listener);
		}
		return this;
	}
}
