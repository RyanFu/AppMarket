package com.dongji.market.widget;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dongji.market.R;
import com.dongji.market.adapter.GalleryDetailAdapter;
import com.dongji.market.helper.AndroidUtils;

public class CustomGalleryDialog extends Dialog {
	private View mContentView;
	private Context context;
	private Gallery mGallery;
	
	public CustomGalleryDialog(Context context) {
		super(context, R.style.dialog_progress_default);
		this.context = context;
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
		mContentView = LayoutInflater.from(context).inflate(
				R.layout.activity_detail_gallery, null);
		mGallery = (Gallery) mContentView.findViewById(R.id.gallery);
		FrameLayout mFrameLayout = (FrameLayout) mContentView.findViewById(R.id.framelayout);
		DisplayMetrics dm = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int num1 = AndroidUtils.dip2px(context, 30);
		int num2 = AndroidUtils.dip2px(context, 60);
		LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) mFrameLayout
				.getLayoutParams();
		mParams.width = dm.widthPixels - (num1 * 2);
		mParams.height = dm.heightPixels - (num2 * 2);
		mFrameLayout.setLayoutParams(mParams);
	}
	
	public void setImageSource(List<String> arr) {
		GalleryDetailAdapter mAdapter=new GalleryDetailAdapter(context, arr);
		mGallery.setAdapter(mAdapter);
		mGallery.setOnItemSelectedListener(new ItemSelectedListener(arr.size()));
		initSwitchIdResources(arr.size());
	}
	
	public void showPosition(int position) {
		mGallery.setSelection(position);
		if(!isShowing() && !((Activity) context).isFinishing()) {
			show();
		}
	}
	
	/**
	 * 指示按钮
	 */
	private LinearLayout mSwithBtnContainer;
	/**
	 * 初始化海报指示标
	 * @param count
	 */
	private void initSwitchIdResources(int count) {
		mSwithBtnContainer = (LinearLayout) mContentView.findViewById(R.id.switcherbtn_container);

		ImageView localImageView = null;
		SwitchBtnClickListener localSwitchBtnClickListener = new SwitchBtnClickListener();
		LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(
				20, 20);
		localLayoutParams.leftMargin = 5;
		localLayoutParams.rightMargin = 5;

		for (int j = 0; j < count; j++) {
			localImageView = new ImageView(context);
			localImageView.setLayoutParams(localLayoutParams);
			localImageView
					.setBackgroundResource(R.drawable.selector_image_switcher);
			localImageView.setOnClickListener(localSwitchBtnClickListener);
			mSwithBtnContainer.addView(localImageView);
		}
	}

	/**
	 * 画廊点击联动指示按钮
	 * @author 
	 *
	 */
	public class ItemSelectedListener implements OnItemSelectedListener {

		int mCount;

		public ItemSelectedListener(int count) {
			super();
			mCount = count;
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view,
				int position, long id) {
			setSelectedSwitchBtn(position % mCount);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {

		}
	}

	/**
	 * 切换按钮的点击事件响应
	 * @author Administrator
	 *
	 */
	public class SwitchBtnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int position = mSwithBtnContainer.indexOfChild(v);
			setSelectedSwitchBtn(position);
			setSelectedGalleryImg(position);
		}
	}

	/**
	 * 设置聚焦指示按钮
	 */
	private ImageView mSelectedSwitchButton;
	private void setSelectedSwitchBtn(int paramInt) {
		if (this.mSelectedSwitchButton != null)
			this.mSelectedSwitchButton.setSelected(false);
		ImageView localImageView = (ImageView) this.mSwithBtnContainer
				.getChildAt(paramInt);
		this.mSelectedSwitchButton = localImageView;
		this.mSelectedSwitchButton.setSelected(true);
	}

	/**
	 * 设置聚焦画廊海报图片
	 * @param position
	 */
	private void setSelectedGalleryImg(int position) {
		mGallery.setSelection(position);
	}
}
