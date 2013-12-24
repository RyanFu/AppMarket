package com.dongji.market.activity;

import android.view.KeyEvent;
import android.view.Menu;

/**
 * MainActivity子Activity的基类
 * 主要负责以下事情：
 * 1.定义一个点击顶部tooBar父类方法，由子类重载实现
 * 2.截获子Activity的菜单按钮事件，将其传递给MainActivity处理
 * 3.截获子Activity的按钮事件，将其传递给MainActivity处理
 * 4.定义了onAppClick(),onGameClick(),isAppclick()抽象方法，子类实现
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

	/**
	 * 获取父activityGroup MainActivity
	 * @return
	 */
	protected MainActivity getParentActivity() {
		return (MainActivity) getParent();
	}

	/**
	 * 获取父控件的菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getParent() == null) {
			return super.onCreateOptionsMenu(menu);
		} else {
			return getParent().onCreateOptionsMenu(menu);
		}
	}

	/**
	 * 打开父控件的菜单
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (getParent() == null) {
			return super.onMenuOpened(featureId, menu);
		} else {
			return getParent().onMenuOpened(featureId, menu);
		}
	}

	/**
	 * 将按键事件分发到父控件处理
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (getParent() != null) {
			return getParent().onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 返回调用
	 */
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
