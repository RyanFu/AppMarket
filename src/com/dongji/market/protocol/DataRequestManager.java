package com.dongji.market.protocol;

import android.os.Handler;

public class DataRequestManager implements IDataRequestManager {
	private INotifiableController controller;
	private Handler mHandler;
	
	public void setINotifiableController(INotifiableController controller) {
		this.controller=controller;
	}
	
	
}
