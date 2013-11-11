package com.dongji.market.widget;

import com.dongji.market.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 自定义无标题Dialog
 * @author yvon
 *
 */
public class CustomNoTitleDialog extends Dialog {
	private Context context;
	private TextView mTextView;
	private Button mCancelButton;
	private Button mPromptButton;
	private View mContentView;

	public CustomNoTitleDialog(Context context) {
		super(context, R.style.dialog_progress_default);
		this.context=context;
		initViews();
	}
	
	private void initViews() {
		mContentView=LayoutInflater.from(context).inflate(R.layout.widget_notitle_dialog, null);
		mTextView=(TextView)mContentView.findViewById(R.id.textview);
		mCancelButton=(Button)mContentView.findViewById(R.id.cancelbutton);
		mPromptButton=(Button)mContentView.findViewById(R.id.promptbutton);
		defaultNeutralButtonClickListener();
		defaultNegativeButtonClickListener();
	}
	
	private void defaultNeutralButtonClickListener() {
		mPromptButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	private void defaultNegativeButtonClickListener() {
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public CustomNoTitleDialog setMessage(String text) {
		mTextView.setText(text);
		return this;
	}
	
	public CustomNoTitleDialog setMessage(int id) {
		mTextView.setText(id);
		return this;
	}
	
	public CustomNoTitleDialog setNeutralButton(String name, View.OnClickListener onClickListener) {
		mPromptButton.setText(name);
		if(onClickListener!=null) {
			mPromptButton.setOnClickListener(onClickListener);
		}
		return this;
	}
	
	public CustomNoTitleDialog setNegativeButton(String name, View.OnClickListener onClickListener) {
		mCancelButton.setText(name);
		if(onClickListener!=null) {
			mCancelButton.setOnClickListener(onClickListener);
		}
		return this;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mContentView);
		setCanceledOnTouchOutside(true);
	}
}
