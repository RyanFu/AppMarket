package com.dongji.market.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dongji.market.R;

public class CustomProgressDialog extends Dialog {
	private Context cxt;
	private TextView mContentTextView;
	private View mContentView;

	public CustomProgressDialog(Context context) {
		super(context, R.style.dialog_progress_default);
		cxt = context;
		initViews();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(false);
	}
	
	private void initViews() {
		mContentView=getLayoutInflater().inflate(R.layout.widget_progress_dialog, null);
		mContentTextView = (TextView) mContentView.findViewById(R.id.contenttextview);
	}

	public void setContentText(String text) {
		mContentTextView.setText(text);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
	}
}
