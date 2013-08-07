package com.dongji.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ScrollListView extends ListView {
	private static final int SCROLL_VALUE = 30;
	private float tempY;
	private OnScrollTouchListener mOnScrollTouchListener;
	
	private OnTouchDwon mOnTouchDwon;

	public ScrollListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				tempY=ev.getRawY();
				
//				System.out.println("  ScrollListView  --->   "  + MotionEvent.ACTION_DOWN );
				
				break;
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				float temp=tempY-ev.getRawY();
				System.out.println(temp);
				if (temp > SCROLL_VALUE) {
					tempY=ev.getRawY();
					if(mOnScrollTouchListener!=null) {
						mOnScrollTouchListener.onScrollTouch(OnScrollTouchListener.SCROLL_BOTTOM);
					}
				}else if(temp<-10) {
					if(mOnScrollTouchListener!=null) {
						mOnScrollTouchListener.onScrollTouch(OnScrollTouchListener.SCROLL_TOP);
					}
					tempY=ev.getRawY();
				}
				break;
		}
		
		return super.onTouchEvent(ev);
	}
	
	public void setOnScrollTouchListener(OnScrollTouchListener listener) {
		mOnScrollTouchListener=listener;
	}
	
	public interface OnScrollTouchListener {
		static final int SCROLL_TOP = 1;
		static final int SCROLL_BOTTOM = 2;
		
		void onScrollTouch(int scrollState);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		if(ev.getAction()==MotionEvent.ACTION_DOWN)
		{
			tempY=ev.getRawY();
			
			if(mOnTouchDwon!=null)
			{
				mOnTouchDwon.onTouchDown(ev.getY());
			}
			System.out.println("  onInterceptTouchEvent  --->   "  + MotionEvent.ACTION_DOWN );
		}
		
		return super.onInterceptTouchEvent(ev);
	}
	
	public  interface OnTouchDwon{ 
		void onTouchDown(float y);
	}

	public OnTouchDwon getmOnTouchDwon() {
		return mOnTouchDwon;
	}

	public void setmOnTouchDwon(OnTouchDwon mOnTouchDwon) {
		this.mOnTouchDwon = mOnTouchDwon;
	}
	
}
