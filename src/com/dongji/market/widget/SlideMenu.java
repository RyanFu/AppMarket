package com.dongji.market.widget;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsoluteLayout;
import android.widget.ListView;

import com.dongji.market.widget.ScrollListView.OnTouchDwon;

/**
 * 使用此控件时，页面菜单部分与页面主体部分必须放在同一个FrameLayout之中
 * 
 * @author RanQing
 * 
 */
public class SlideMenu implements Runnable {

	private int menu1MaxHeight;

	private View mMenu1Layout;
	private ListView mListView;
	
	private AbsoluteLayout.LayoutParams menu1Params;
	private TouchGestureListener gestureListener;
	private GestureDetector gestureDetector;
	
	float tempY1, temp1;
	
	private boolean isFling = false;
	private boolean scrollDown;
	
	public SlideMenu() {
		gestureListener = new TouchGestureListener();
		gestureDetector = new GestureDetector(gestureListener);
	}

	public SlideMenu(int menuLayoutId, ListView mListView,
			Activity activity) {
		gestureListener = new TouchGestureListener();
		gestureDetector = new GestureDetector(gestureListener);
		mMenu1Layout = activity.findViewById(menuLayoutId);
		getMenu1MaxHeight();
		mListView.setOnTouchListener(touchListener);
		mListView.setOnScrollListener(scrollListener);
		
		if( mListView instanceof ScrollListView)
		{
			((ScrollListView)mListView).setmOnTouchDwon(new OnTouchDwon() {
				
				@Override
				public void onTouchDown(float y) {
					System.out.println("  onTouchDown  ---> " +  y );
					tempY1 = y;
				}
			});
//			System.out.println("   mListView instanceof ScrollListView  ==== ");
		}
		
	}
	

	public SlideMenu(View mMenu1Layout, ListView mListView) {
		gestureListener = new TouchGestureListener();
		gestureDetector = new GestureDetector(gestureListener);
		this.mMenu1Layout = mMenu1Layout;
		getMenu1MaxHeight();
		mListView.setOnTouchListener(touchListener);
		mListView.setOnScrollListener(scrollListener);
		
		if( mListView instanceof ScrollListView)
		{
			((ScrollListView)mListView).setmOnTouchDwon(new OnTouchDwon() {
				
				@Override
				public void onTouchDown(float y) {
					System.out.println("  onTouchDown  ---> " +  y );
					tempY1 = y;
				}
			});
//			System.out.println("   mListView instanceof ScrollListView  ==== ");
		}
	}

	private void getMenu1MaxHeight() {
		ViewTreeObserver viewTreeObserver = mMenu1Layout.getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				// TODO Auto-generated method stub
				menu1MaxHeight = mMenu1Layout.getHeight();
				return true;
			}
		});
	}
	
	private class TouchGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return true;	//返回值为true时，ListView将不会有Fling效果
		}
		
	}
	
	public OnScrollListener scrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if (scrollState == SCROLL_STATE_FLING) {
//				isFling = true;
				if (null != mMenu1Layout) {
					mMenu1Layout.post(SlideMenu.this);
//					if (scrollDown) {
//						new AsycMove().execute(50);
//					} else {
//						new AsycMove().execute(-50);
//					}
				}
			} else if (scrollState == SCROLL_STATE_IDLE) {
//				isFling = false;
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public OnTouchListener touchListener = new OnTouchListener() {

		
		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			
			// TODO Auto-generated method stub
			menu1Params = (AbsoluteLayout.LayoutParams) mMenu1Layout.getLayoutParams();
			
			if (gestureDetector.onTouchEvent(ev) && menu1Params.y > -menu1MaxHeight && menu1Params.y < 0) {
				mMenu1Layout.post(SlideMenu.this);
//				if (scrollDown) {
//					new AsycMove().execute(50);
//				} else {
//					new AsycMove().execute(-50);
//				}
				return true;
			}
			
			switch(ev.getAction()) {
			
			case MotionEvent.ACTION_DOWN:
				
				tempY1 = ev.getY();
				
//				System.out.println("  MotionEvent.ACTION_DOWN  ---> ");
				
				break;
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				
				temp1 = ev.getY() - tempY1;
//				System.out.println("temp1 ======>" + temp1);
				if (menu1Params.y >= 0 || menu1Params.y <= -menu1MaxHeight) {
					tempY1 = ev.getY();
				}
				
				if(temp1 < 0)
				{
					if (menu1Params.y > -menu1MaxHeight) {
						menu1Params.y = Math.max((int)temp1 + menu1Params.y, -menu1MaxHeight);
						mMenu1Layout.setLayoutParams(menu1Params);
					}
				} else {
					if (menu1Params.y < 0) {
						menu1Params.y = Math.min((int)temp1 + menu1Params.y, 0);
						mMenu1Layout.setLayoutParams(menu1Params);
					}
				}
//				mMenu1Layout.setLayoutParams(menu1Params);
				if (temp1 > 0) {
					scrollDown = true;
				} else if (temp1 < 0) {
					scrollDown = false;
				}
				
				break;
		}
			
		return false;
			
		}
	};
	
	public boolean isShowSettingPop() {
		return ((AbsoluteLayout.LayoutParams)mMenu1Layout.getLayoutParams()).y < -(float)menu1MaxHeight/2;
	}
	
	public void showMenu() {
		AbsoluteLayout.LayoutParams menu1Params = (AbsoluteLayout.LayoutParams) mMenu1Layout.getLayoutParams();
		menu1Params.y = 0;
		mMenu1Layout.setLayoutParams(menu1Params);
	}

	public View getMenuLayout() {
		return mMenu1Layout;
	}

	public void setMenuLayout(View mMenuLayout) {
		this.mMenu1Layout = mMenuLayout;
		getMenu1MaxHeight();
	}

	public ListView getListView() {
		return mListView;
	}

	public void setListView(ListView mListView) {
		this.mListView = mListView;
		this.mListView.setOnTouchListener(touchListener);
		mListView.setOnScrollListener(scrollListener);
		
		if( mListView instanceof ScrollListView)
		{
			((ScrollListView)mListView).setmOnTouchDwon(new OnTouchDwon() {
				
				@Override
				public void onTouchDown(float y) {
					System.out.println("  onTouchDown  ---> " +  y );
					tempY1 = y;
				}
			});
//			System.out.println("   mListView instanceof ScrollListView  ==== ");
		}
	}

	/**
	 * 刷新菜单上下移动位置
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("isFling ===========> " + isFling);
//		if (isFling) {
			if(!scrollDown)
			{
				if (menu1Params.y > -menu1MaxHeight) {
					menu1Params.y = Math.max(menu1Params.y - 100, -menu1MaxHeight);
					mMenu1Layout.setLayoutParams(menu1Params);
//					mMenu1Layout.postDelayed(this, 40);
					mMenu1Layout.post(this);
				}
				return;
			} else {
				if (menu1Params.y < 0) {
					menu1Params.y = Math.min(menu1Params.y + 100, 0);
					mMenu1Layout.setLayoutParams(menu1Params);
//					mMenu1Layout.postDelayed(this, 40);
					mMenu1Layout.post(this);
				}
				return;
			}
//		}
//		return;
	}
	
	/**
	 * 刷新菜单移动位置
	 * @author RanQing
	 *
	 */
	class AsycMove extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			int times = 0;
			if (menu1MaxHeight % Math.abs(params[0]) == 0) {
				times = menu1MaxHeight / Math.abs(params[0]);
			} else {
				times = menu1MaxHeight / Math.abs(params[0]) + 1;
			}
			for (int i = 0; i < times; i++) {
				publishProgress(params[0]);
				try {
					Thread.sleep(Math.abs(params[0]));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			if (values[0] > 0) {
				menu1Params.y = Math.min(menu1Params.y + values[0], 0);
			} else {
				menu1Params.y = Math.max(menu1Params.y + values[0], -menu1MaxHeight);
			}
			mMenu1Layout.setLayoutParams(menu1Params);
		}
		
	}

}
