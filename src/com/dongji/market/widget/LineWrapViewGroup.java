package com.dongji.market.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class LineWrapViewGroup extends ViewGroup {
	
	private static final int VIEW_MARGIN = 5;

	public LineWrapViewGroup(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		
		for (int index = 0; index < getChildCount(); index++) {
			View child = getChildAt(index);
			child.measure(widthMeasureSpec, heightMeasureSpec);
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int count = getChildCount();
		int row = 0;	//行号
		int lengthX = l;	//右下角横坐标
		int lengthY = t;	//右下角纵坐标
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();
			lengthX += width + VIEW_MARGIN;
			lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height + t;
			
			if (lengthX > r) {	//超过当前行长度，则换行
				lengthX = width + VIEW_MARGIN + l;
				row++;
				lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height + t;
			}
			
			child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
		}
		
	}

}
