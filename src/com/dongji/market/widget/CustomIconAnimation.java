package com.dongji.market.widget;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.dongji.market.R;
import com.dongji.market.helper.DJMarketUtils;

public class CustomIconAnimation {

	private Activity cxt;

	public CustomIconAnimation(Activity cxt) {
		this.cxt = cxt;
	}

	public void startAnimation(final int initX, final int initY, Drawable icon, final ImageView fromView, final View toView) {
//		mTempIcon.setX(iconX);	//sdk2.3以上才可用
//		mTempIcon.setY(iconY - DJMarketUtils.dip2px(Search_Result_Activity.this, 2));	//sdk2.3以上才可用
		fromView.setImageDrawable(icon);
		RelativeLayout.LayoutParams mParams = (LayoutParams) fromView.getLayoutParams();
		mParams.leftMargin = initX;
		mParams.topMargin = initY - DJMarketUtils.getStatusBarInfo(cxt).top;
		fromView.setLayoutParams(mParams);
		fromView.setVisibility(View.VISIBLE);
		Animation shake = AnimationUtils.loadAnimation(cxt, R.anim.shake);
		shake.setFillBefore(true);
		shake.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
			}
			
			public void onAnimationRepeat(Animation animation) {
			}
			
			public void onAnimationEnd(Animation animation) {
				AnimationSet as = new AnimationSet(true);
				int fromX = initX - DJMarketUtils.dip2px(cxt, 10);
				int toX = DJMarketUtils.getViewLocation(toView)[0] - toView.getWidth()/2;
				int fromY = initY - DJMarketUtils.getStatusBarInfo(cxt).top - toView.getHeight()/2;
				int toY = DJMarketUtils.getStatusBarInfo(cxt).top;
				TranslateAnimation translate = new TranslateAnimation(fromX, toX, 0, -1 * fromY);
				ScaleAnimation scale = new ScaleAnimation(1.5f, 0f, 1.5f, 0f, toView.getWidth()/2, toView.getHeight()/2);
				as.setAnimationListener(new AnimationListener() {
					
					public void onAnimationStart(Animation animation) {
					}
					
					public void onAnimationRepeat(Animation animation) {
					}
					
					public void onAnimationEnd(Animation animation) {
						fromView.setVisibility(View.GONE);
					}
				});
				as.setFillBefore(true);
				as.addAnimation(scale);
				as.addAnimation(translate);
				as.setDuration(400);
				as.setStartOffset(100);
				fromView.startAnimation(as);
			}
		});
		fromView.startAnimation(shake);
	}
}
