package com.dongji.market.helper;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class LazyScrollView extends ScrollView implements OnTouchListener{

	
	private ScrollListener mListener;
	private boolean loading = false;
	
	public LazyScrollView(Context context) {
		super(context);
		init();
	}
	
	public LazyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public LazyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		setOnTouchListener(this);
		new Thread(){
			private int nowY = 0;
			private int preY = 0;
			private boolean flag = false;
			public void run() {
				while(true){
					nowY = getScrollY();
					if(nowY == preY){
						if(!flag){
							flag = true;
							if(mListener != null){
								mListener.stopScroll(getScrollY());
							}
						}
					}else{
						flag = false;
					}
					preY = nowY;
					try {
						Thread.sleep(200);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	public void setScrollListener(ScrollListener listener){
		this.mListener = listener;
	}
	
	public interface ScrollListener{
		public void scrollToBottom();
		public void onAutoLoad(int l, int t, int oldl, int oldt);
		public void stopScroll(int nowY);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if(mListener != null){
			mListener.onAutoLoad(l, t, oldl, oldt);
		}
		LinearLayout ll = (LinearLayout)getChildAt(0);
		if(t+getMeasuredHeight()==ll.getMeasuredHeight()){
			if(!loading){
				loading = true;
				Log.d("WaterFallDemo==================", "start load");
				mListener.scrollToBottom();
			}
		}
	}
	
	public void completeLoad(){
		Log.d("==============", "complete");
		this.loading = false;
	}
}
