package com.dongji.market.helper;

import com.dongji.market.R;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class WaterFallCell extends ImageView {

	public static final int ADD_INTO = 1;
	public static final int LOAD_PIC = 2;

	private int mColumn;
	private int mRow;
	private int mID;
	private Handler mHandler;
	private FinalBitmap mFinalBitmap;
	private WaterFallItem mItem;
	private int mColumnWidth;
	private int mHeight;
	private int mCount;
	private View mContentView;
	public boolean hadLoadPic = true;

	public WaterFallCell(Context context) {
		super(context);
	}

	public WaterFallCell(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public View getmContentView() {
		return mContentView;
	}

	public void setmContentView(View mContentView) {
		this.mContentView = mContentView;
	}

	public int getmColumn() {
		return mColumn;
	}

	public void setmColumn(int mColumn) {
		this.mColumn = mColumn;
	}

	public int getmRow() {
		return mRow;
	}

	public void setmRow(int mRow) {
		this.mRow = mRow;
	}

	public Handler getmHandler() {
		return mHandler;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public void setmFinalBitmap(FinalBitmap mFinalBitmap) {
		this.mFinalBitmap = mFinalBitmap;
	}

	public WaterFallItem getmItem() {
		return mItem;
	}

	public int getmCount() {
		return mCount;
	}

	public void setmCount(int mCount) {
		this.mCount = mCount;
	}

	public void setmItem(WaterFallItem mItem) {
		this.mItem = mItem;
	}

	public int getmColumnWidth() {
		return mColumnWidth;
	}

	public void setmColumnWidth(int mColumnWidth) {
		this.mColumnWidth = mColumnWidth;
	}

	public int getmHeight() {
		return mHeight;
	}

	public void setmHeight(int mHeight) {
		this.mHeight = mHeight;
	}

	public int getmID() {
		return mID;
	}

	public void setmID(int mID) {
		this.mID = mID;
	}

	// 计算图片的高宽，然后回调给handler添加到LL
	public void startResize() {
		// TODO
		new Thread() {
			public void run() {
				if (mItem.imgheight != 0) {
					mHeight = (mItem.imgheight * mColumnWidth) / mItem.imgwidth;
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mColumnWidth, mHeight);
					setLayoutParams(params);
					Message msg = mHandler.obtainMessage();
					msg.what = ADD_INTO;
					msg.obj = WaterFallCell.this;
					mHandler.sendMessage(msg);
				}
			};
		}.start();
	}

	public void reload() {
		if (!hadLoadPic) {
			this.setBackgroundResource(R.drawable.water_fall_cell_bg);
			mFinalBitmap.configLoadingImage(null);
			mFinalBitmap.configLoadfailImage(null);
			mFinalBitmap.display(this, mItem.img);
			hadLoadPic = true;
		}
	}

	public void recycle() {
		Drawable d = this.getDrawable();
		this.setImageBitmap(null);
		if (d != null) {
			d.setCallback(null);
		}
		hadLoadPic = false;
		this.setBackgroundResource(R.drawable.water_fall_cell_bg);
	}
}
