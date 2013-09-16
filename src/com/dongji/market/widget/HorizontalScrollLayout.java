package com.dongji.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @author zhangkai
 */
public class HorizontalScrollLayout extends ViewGroup {
	private static final String TAG = "HorizontalScrollLayout";
	private static final boolean DEBUG = false;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mCurScreen;
	private int mDefaultScreen = 0;
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private static final int SNAP_VELOCITY = 600;
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	private OnPageChangedListener mListener;
	private boolean isNotifyChanged; // 是否通知监听回调
	private View mInterceptView;
	private int interceptPosition;

	public HorizontalScrollLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HorizontalScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context);
		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (DEBUG)
			Log.e(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
		}

		// The children are given the same width and height as the scrollLayout
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	
	@Override
	public void computeScroll() {
		if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
		if (mScroller.isFinished() && isNotifyChanged) {
			isNotifyChanged = false;
			requestLayout();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (DEBUG)
				Log.e(TAG, "event down!");
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;

		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			int maxWidth = (getChildCount() - 1) * getWidth();
			if (mCurScreen == 0 && deltaX < 0 && getScrollX() <= 0) {
				break;
			} else if (mCurScreen == getChildCount() - 1 && deltaX > 0 && maxWidth <= getScrollX()) {
				break;
			}
			scrollBy(deltaX, 0);
			break;

		case MotionEvent.ACTION_UP:
			if (DEBUG)
				Log.e(TAG, "event : up");
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) velocityTracker.getXVelocity();

			if (DEBUG)
				Log.e(TAG, "velocityX:" + velocityX);

			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
				if (DEBUG)
					Log.e(TAG, "snap left");
				int position = mCurScreen - 1;
				snapToScreen(position);
			} else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
				if (DEBUG)
					Log.e(TAG, "snap right");
				snapToScreen(mCurScreen + 1);
			}

			else {
				snapToDestination();
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (DEBUG)
			Log.e(TAG, "onInterceptTouchEvent-slop:" + mTouchSlop);

		if (mCurScreen == interceptPosition && mInterceptView != null) {
			if (mInterceptView.getWidth() > ev.getX() && mInterceptView.getHeight() > ev.getY()) {
				return false;
			}
		}

		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			final int yDiff = (int) Math.abs(mLastMotionY - y);
			if ((xDiff > mTouchSlop && yDiff < mTouchSlop) || (xDiff > mTouchSlop && yDiff > mTouchSlop && xDiff > yDiff)) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;

		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	public void setInterceptTouchView(View v, int position) {
		mInterceptView = v;
		interceptPosition = position;
	}

	public void setOnPageChangedListener(OnPageChangedListener mListener) {
		this.mListener = mListener;
	}
	
	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
	}
	
	/**
	 * According to the position of current layout scroll to the destination
	 * page.
	 */
	public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	public void snapToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth())) {

			final int delta = whichScreen * getWidth() - getScrollX();
			int duration = Math.abs(delta) * 2;
			int num = getWidth() * 2;
			mScroller.startScroll(getScrollX(), 0, delta, 0, duration > num ? num : duration);
			if (mCurScreen != whichScreen) {
				mCurScreen = whichScreen;
				isNotifyChanged = true;
			}
			if (mListener != null) {
				mListener.onPageChanged(mCurScreen);
			}
			invalidate(); // Redraw the layout
		} else {
			if (mListener != null) {
				mListener.onPageChanged(mCurScreen);
			}
		}
	}

	/**
	 * 返回当前显示布局的下标
	 * 
	 * @return
	 */
	public int getCurScreen() {
		return mCurScreen;
	}

	public interface OnPageChangedListener {
		void onPageChanged(int position);
	}
}
