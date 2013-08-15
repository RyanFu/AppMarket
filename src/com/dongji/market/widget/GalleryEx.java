package com.dongji.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class GalleryEx extends Gallery {

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
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int kEvent; 
		if(e2.getX() > e1.getX()){ 
			  kEvent = KeyEvent.KEYCODE_DPAD_LEFT; 
		} else{ 
			  kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		} 
		onKeyDown(kEvent, null);
		return true; 
	}
}
