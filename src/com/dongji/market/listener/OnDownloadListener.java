package com.dongji.market.listener;

import com.dongji.market.pojo.DownloadEntity;

public interface OnDownloadListener {
	void onDownloadStatusChanged(DownloadEntity entity);
}
