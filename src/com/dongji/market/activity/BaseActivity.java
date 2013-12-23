package com.dongji.market.activity;

import android.view.KeyEvent;
import android.view.Menu;

/**
 * activity基类
 * 
 * @author yvon
 * 
 */
public abstract class BaseActivity extends PublicActivity {

	/**
	 * 顶部toolBar点击
	 */
	public void OnToolBarClick() {
	}

	protected MainActivity getParentActivity() {
		return (MainActivity) getParent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getParent() == null) {
			return super.onCreateOptionsMenu(menu);
		} else {
			return getParent().onCreateOptionsMenu(menu);
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (getParent() == null) {
			return super.onMenuOpened(featureId, menu);
		} else {
			return getParent().onMenuOpened(featureId, menu);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (getParent() != null) {
			return getParent().onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	/*************************************** 抽象类定义，子类实现 **********************************/

	/**
	 * 当点击应用时处理
	 */
	public abstract void onAppClick();

	/**
	 * 当点击游戏时处理
	 */
	public abstract void onGameClick();

	/**
	 * 是否应用按钮被点击
	 * 
	 * @return
	 */
	public abstract boolean isAppClicked();
	/*************************************** 抽象类定义，子类实现 **********************************/

}
