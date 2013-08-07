package com.dongji.market.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 
 * @author zhangkai
 */
public class HttpApi {
	private static HttpApi httpApi;
	private static final int CONNECT_TIME_OUT = 10000;
	private static final int READ_TIME_OUT=15000;
	private StringBuilder sb;
	
	public synchronized static HttpApi getInstance() {
		if(httpApi==null) {
			httpApi=new HttpApi();
		}
		return httpApi;
	}
	
	private HttpApi() {
		super();
		sb=new StringBuilder();
	}
	
	private InputStream getInputStream(String url) throws IOException {
		HttpURLConnection httpUrlConnection=(HttpURLConnection)new URL(url).openConnection();
		httpUrlConnection.setConnectTimeout(CONNECT_TIME_OUT);
		httpUrlConnection.setReadTimeout(READ_TIME_OUT);
		httpUrlConnection.connect();
		return httpUrlConnection.getInputStream();
	}
	
	public String getContentFromUrl(String url) throws IOException {
		InputStream is=null;
		try{
			is=getInputStream(url);
			clearStringBuilder();
			byte[] data=new byte[1024];
			int num=0;
			while((num=is.read(data))!=-1) {
				sb.append(new String(data, 0, num));
			}
		}finally {
			if(is!=null) {
				is.close();
			}
		}
		return sb.toString();
	}
	
	private void clearStringBuilder() {
		if(sb.length()>0) {
			sb.delete(0, sb.length());
		}
	}
}
