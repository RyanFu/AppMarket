package com.dongji.market.exception;

/**
 * 与服务器通讯所发生的异常集合
 * @author zhangkai
 */
public class ServerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2916954775816976220L;

	public ServerException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ServerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public ServerException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public ServerException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}
	
}
