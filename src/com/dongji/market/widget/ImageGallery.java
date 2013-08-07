package com.dongji.market.widget;

import android.content.Context;

import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * 左右透明的 Gallery
 * @author zhangkai
 */
public class ImageGallery extends Gallery {
	private int mCoveflowCenter;
	private static final int MAX_ALPHA = 255;

	public ImageGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ImageGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ImageGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		boolean flag=super.onKeyDown(keyCode, event);
//		System.out.println("flag:"+flag);
//		return flag;
//		System.out.println(computeHorizontalScrollRange()+", "+computeHorizontalScrollOffset());
		return super.onKeyDown(keyCode, event);
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
	
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		ImageView mImageView=(ImageView)child;
		if(mImageView.getLeft()>mCoveflowCenter || mImageView.getRight()<mCoveflowCenter) {
			mImageView.setAlpha(150);
		}else {
			mImageView.setAlpha(MAX_ALPHA);
		}
		return super.getChildStaticTransformation(child, t);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter=getWidth()/2;
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
