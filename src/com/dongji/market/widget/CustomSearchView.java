package com.dongji.market.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.dongji.market.R;
import com.dongji.market.helper.DJMarketUtils;

/**
 * 自定义搜索控件
 * @author yvon
 *
 */
public class CustomSearchView extends EditText {

	private Context cxt;
	private int color;
	private int mPopupMaxHeight, mPopupHeight, mPopupItemHeight;
	private int threshold;
	private int dividerHeight;
	private Drawable mListSelector, mPopupBg;
	private boolean isAutoSearching;
	private PopupWindow mPopup;
	private BaseAdapter mAdapter;
	private RequestDataListener requestDataListener;
	private PopupDataSetObserver mObserver;
	private MyListView mListView;
	private OnItemClickListener onItemClickListener;
	private OnTextChangeListener mOnTextChangeListener;
	private OnKeyDownListener mOnKeyDownListener;

	public CustomSearchView(Context context) {
		super(context);
	}

	public CustomSearchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchTextEdit);
		mPopupBg = typedArray.getDrawable(R.styleable.SearchTextEdit_popupBackground);
		color = typedArray.getColor(R.styleable.SearchTextEdit_divider, Color.BLACK);
		dividerHeight = typedArray.getDimensionPixelOffset(R.styleable.SearchTextEdit_dividerHeight, 1);
		threshold = typedArray.getInt(R.styleable.SearchTextEdit_completionThreshold, 1);
		mListSelector = typedArray.getDrawable(R.styleable.SearchTextEdit_dropdownSelector);
		initMaxPopupSize();
		addTextChangedListener(new MyTextWatcher());
		buildDropDown();
		isAutoSearching = true;
	}

	/**
	 * 设置popupWindow高度最大为屏幕高度的1/4
	 */
	private void initMaxPopupSize() {
		DisplayMetrics metrics = DJMarketUtils.getScreenSize((Activity) cxt);
		mPopupItemHeight = DJMarketUtils.dip2px(cxt, 17) + DJMarketUtils.sp2px(cxt, 17);
		mPopupMaxHeight = metrics.heightPixels / 4;
	}
	
	private void buildDropDown() {
		if (mListView == null) {
			mListView = new MyListView(getContext());
			mListView.setCacheColorHint(0x000);
			mListView.setDivider(new ColorDrawable(color));
			mListView.setDividerHeight(dividerHeight);
			mListView.setScrollbarFadingEnabled(false);
			mListView.setPadding(3, 0, 0, 0);
			if (mListSelector != null) {
				mListView.setSelector(mListSelector);
			}
			mListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
		}
	}

	public void setPopupHeight(int height) {
		this.mPopupMaxHeight = height;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setOnTextChangeListener(OnTextChangeListener listener) {
		this.mOnTextChangeListener = listener;
	}

	public void setOnKeyDownListener(OnKeyDownListener listener) {
		this.mOnKeyDownListener = listener;
	}

	private void dismissPopup() {
		if (mPopup != null && mPopup.isShowing()) {
			mPopup.dismiss();
		}
	}

	public void setSelector(Drawable drawable) {
		if (drawable != null && mListView != null) {
			mListView.setSelector(drawable);
		}
	}

	public void setDropDownOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void dismissDropDown() {
		this.dismissPopup();
	}

	public void dismissFocus() {
		this.dismissPopup();
		this.clearFocus();
	}
	
	/**
	 * 设置适配器
	 * @param adapter
	 */
	public <T extends BaseAdapter & RequestDataListener> void setAdapter(T adapter) {
		if (mObserver == null) {
			mObserver = new PopupDataSetObserver();
		}
		this.mAdapter = adapter;
		this.mAdapter.registerDataSetObserver(mObserver);
		this.requestDataListener = adapter;
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String keyword = mAdapter.getItem(position).toString();
				isAutoSearching = false;
				setText(keyword);
				isAutoSearching = true;
				if (onItemClickListener != null) {
					onItemClickListener.onItemClick(keyword);
				}
				setSelection(keyword.length());
				dismissPopup();
			}
		});
	}

	/**
	 * 监听内容改变观察者
	 * @author yvon
	 *
	 */
	private class MyTextWatcher implements TextWatcher {

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		public void afterTextChanged(Editable s) {

			if (mOnTextChangeListener != null) {
				mOnTextChangeListener.afterTextChanged(s);
			}
			if (requestDataListener == null) {
				return;
			}
			if (isAutoSearching && s.length() >= threshold) {
			} else {
				dismissPopup();
			}
		}

	}

	/**
	 * 自定义listView
	 * @author yvon
	 *
	 */
	private class MyListView extends ListView {

		public MyListView(Context context) {
			super(context);
		}

		@Override
		public boolean hasWindowFocus() {
			return true;
		}

		@Override
		@ExportedProperty
		public boolean isInTouchMode() {
			return true;
		}
	}
	
	
	/**
	 * Popup数据设置观察者
	 * @author yvon
	 *
	 */
	private class PopupDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			if (mAdapter.getCount() > 0) {
				showDropDown();
			} else {
				dismissPopup();
			}
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
		}

	}
	
	
	private void showDropDown() {
		if (mPopup == null) {
			initPopupWindow();
		}
		if (mPopup.isShowing()) {
			setSelectionTop();
			adjustPopupHeight();
			mPopup.update(this, 0, -3, getMeasuredWidth(), mPopupHeight);
		} else {
			mPopup.setWidth(getMeasuredWidth());
			adjustPopupHeight();
			mPopup.setHeight(mPopupHeight);
			mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			mPopup.showAsDropDown(this, 0, -3);
			setSelectionTop();
		}
	}
	
	private void initPopupWindow() {
		mPopup = new PopupWindow(this);
		mPopup.setAnimationStyle(R.anim.down_fade_in);
		mPopup.setOutsideTouchable(true);
		if (mPopupBg == null) {
			mPopupBg = new BitmapDrawable();
		}
		mPopup.setBackgroundDrawable(mPopupBg);
		mPopup.setContentView(mListView);
	}
	
	private void setSelectionTop() {
		mListView.requestFocusFromTouch();
		mListView.setSelected(true);
		mListView.setSelection(0);
	}
	
	private void adjustPopupHeight() {
		if (mAdapter.getCount() * mPopupItemHeight > mPopupMaxHeight) {
			mPopupHeight = mPopupMaxHeight;
		} else {
			mPopupHeight = mPopupItemHeight * mAdapter.getCount();
		}
	}
	
	
	/**
	 * 请求数据监听器
	 * @author yvon
	 *
	 */
	public interface RequestDataListener {
		void request(String keyword);
		void cancelPreRequest();
	}

	/**
	 * 条目点击监听器
	 * @author yvon
	 *
	 */
	public interface OnItemClickListener {
		void onItemClick(String keyword);
	}

	/**
	 * EditText内容改变监听器
	 * @author yvon
	 *
	 */
	public interface OnTextChangeListener {
		void afterTextChanged(Editable s);
	}

	/**
	 * 清除内容监听器
	 * @author yvon
	 *
	 */
	public interface OnKeyDownListener {
		boolean onKeyDown(int keyCode, KeyEvent event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mPopup != null && mPopup.isShowing()) {
				mPopup.dismiss();
				return true;
			}
		}
		if (mOnKeyDownListener != null) {
			boolean flag = mOnKeyDownListener.onKeyDown(keyCode, event);
			if (!flag) {
				return super.onKeyDown(keyCode, event);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
