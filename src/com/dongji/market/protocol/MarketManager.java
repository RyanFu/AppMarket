package com.dongji.market.protocol;

public class MarketManager {
	
	public static IDataRequestManager getDataRequestManager(INotifiableController controller) {
		DataRequestManager mDataManager=new DataRequestManager();
		mDataManager.setINotifiableController(controller);
		return mDataManager;
	}
}
