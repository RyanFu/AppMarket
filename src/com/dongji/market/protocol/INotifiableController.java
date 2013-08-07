package com.dongji.market.protocol;

public interface INotifiableController {
	void runOnUi(DataResponse<?> response);
	
	void onError(int errorCode, String message);
}
