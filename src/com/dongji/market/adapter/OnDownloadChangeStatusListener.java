package com.dongji.market.adapter;

import java.util.Map;


import android.widget.TextView;

import com.dongji.market.pojo.ApkItem;

public interface OnDownloadChangeStatusListener {
	void onDownload(ApkItem item, TextView mTextView, Map<String, Object> map);
	
}
