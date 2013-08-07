package com.dongji.market.widget;

import android.R.attr;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryFlow extends Gallery {

	private Camera mCamera;
	private int mWidth;
	private int mPaddingLeft;
	private boolean flag;
	private static int firstChildWidth;
	private static int firstChildPaddingLeft;
	private int offsetX;
	private int mCoveflowCenter;

	public GalleryFlow(Context context) {
		super(context);
		mCamera = new Camera();
		this.setStaticTransformationsEnabled(true);
	}

	public GalleryFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCamera = new Camera();
		setAttributesValue(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public GalleryFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCamera = new Camera();
		setAttributesValue(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	private void setAttributesValue(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				new int[] { attr.paddingLeft });
		mPaddingLeft = typedArray.getDimensionPixelSize(0, 0);
		typedArray.recycle();
	}

	protected boolean getChildStaticTransformation(View child, Transformation t) {
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		if (flag) {
			firstChildWidth = getChildAt(0).getWidth();
			firstChildPaddingLeft = getChildAt(0).getPaddingLeft();
			flag = false;
		}
		offsetX = 0 - ((mWidth / 2 - firstChildWidth) / 2
				+ firstChildPaddingLeft + mPaddingLeft);
		mCamera.translate(offsetX, 0f, 0f);
		mCamera.getMatrix(imageMatrix);
		mCamera.restore();
		if (child.getRight() < mCoveflowCenter
				|| child.getLeft() > mCoveflowCenter) {
			((ImageView)child).setAlpha(80); 
		}else {
			((ImageView)child).setAlpha(255);
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		event.offsetLocation(-offsetX, 0);
		return super.onTouchEvent(event);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter=getWidth()/2;
		if (!flag) {
			mWidth = w * 2;
			getLayoutParams().width = mWidth;
			flag = true;
		}
		super.onSizeChanged(w, h, oldw, oldh);
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