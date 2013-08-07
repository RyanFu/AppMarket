package com.dongji.market.exception;

/**
 * 处理客户端各种异常
 * @author zhangkai
 */
public class ClientException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6217676044416052274L;

	public ClientException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ClientException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public ClientException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public ClientException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
