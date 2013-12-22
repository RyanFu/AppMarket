package com.dongji.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class GalleryEx extends Gallery {
	private OnFlingBorderlineListener mOnFlingBorderlineListener;

	public static int FLAG_END = 1;

	public GalleryEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public GalleryEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public GalleryEx(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// return super.getCount();
		return getAdapter().getCount();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
//		int kEvent;
		if (e2.getX() > e1.getX()) {
//			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
			FLAG_END = 1;
		} else {
//			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
			// if (mOnFlingBorderlineListener != null && getChildCount() > 0
			// && getSelectedItemPosition() == getChildCount() - 1) {
			// mOnFlingBorderlineListener
			// .onFlingBorderline(OnFlingBorderlineListener.FLING_RIGHT);
			// }
			System.out.println("1206....onfling....." + FLAG_END);
			if (mOnFlingBorderlineListener != null && getCount() > 0
					&& getSelectedItemPosition() == getCount() - 1) {
				if (FLAG_END == 1) {
					FLAG_END = 2;
				} else {
					mOnFlingBorderlineListener
							.onFlingBorderline(OnFlingBorderlineListener.FLING_RIGHT);
				}
			}
		}
		// onKeyDown(kEvent, null);

		return false;
	}

	public void setOnFlingBorderlineListener(
			OnFlingBorderlineListener mOnFlingBorderlineListener) {
		this.mOnFlingBorderlineListener = mOnFlingBorderlineListener;
	}

	public interface OnFlingBorderlineListener {
		static final int FLING_LEFT = 1;
		static final int FLING_RIGHT = 2;

		void onFlingBorderline(int fling);
	}
}
