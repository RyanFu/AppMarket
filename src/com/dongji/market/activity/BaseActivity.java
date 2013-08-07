package com.dongji.market.activity;

import java.util.Map;

import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ListView;

import com.dongji.market.pojo.ChannelListInfo;

public abstract class BaseActivity extends PublicActivity {
	
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
	 * @return
	 */
	public abstract boolean isAppClicked();
	
	public ListView[] getListViews() {
		return null;
	};
	
	public void OnToolBarClick() {
		
	}
	
	public abstract void onItemClick(ChannelListInfo info);
	
	protected MainActivity getParentActivity() {
		return (MainActivity)getParent();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(getParent()==null) {
			return super.onCreateOptionsMenu(menu);
		}else {
			return getParent().onCreateOptionsMenu(menu);
		}
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		if(getParent()==null) {
			return super.onMenuOpened(featureId, menu);
		}else {
			return getParent().onMenuOpened(featureId, menu);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(getParent()!=null) {
			return getParent().onKeyDown(keyCode, event);
		}else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	public void onStartDownload(Map<String, Object> map) {
//		getParentActivity().onStartDownload(map);
	}
	
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
