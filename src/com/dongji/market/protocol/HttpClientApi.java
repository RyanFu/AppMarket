package com.dongji.market.protocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.dongji.market.helper.AndroidUtils;

import android.content.Context;
import android.widget.Toast;



/**
 * 
 * @author zhangkai
 *
 */
public class HttpClientApi {
	private static HttpClientApi httpClientApi;
	private static final int CONNECT_TIME_OUT = 15000;
	private static final int SO_TIME_OUT = 15000;
	private static final String DEFAULT_CHARSET = "utf-8";
	
	private HttpParams httpParams;
	
	private HttpPost post;
	
	private HttpClientApi() {
		super();
		httpParams=new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECT_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIME_OUT);
	}
	
	public static synchronized HttpClientApi getInstance() {
		if(httpClientApi==null) {
			httpClientApi=new HttpClientApi();
		}
		return httpClientApi;
	}
	
	private HttpClient getHttpClient() {
		return new DefaultHttpClient(httpParams);
	}
	
	public String getContentFromUrl(String url) throws IOException {
		String result=null;
		HttpClient httpClient=getHttpClient();
		HttpGet httpGet=new HttpGet(url);
		HttpResponse httpResponse=httpClient.execute(httpGet);
		HttpEntity httpEntity=httpResponse.getEntity();
		if(httpEntity!=null) {
			result=EntityUtils.toString(httpEntity, DEFAULT_CHARSET); //
			httpEntity.consumeContent();
		}
		return result;
	}
	
	public String getContentFromUrlByPost(String url, List<String[]> list) throws IOException {
		System.out.println("++++++++++++++++getContentFromUrlByPost++++++++++++++++"+list.size());
		HttpPost httpPost=new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for(int i=0;i<list.size();i++) {
			String[] arr=list.get(i);
//			System.out.println(arr[0]+", "+arr[1]);
			sb.append("{\"apk_versioncode\":\"").append(arr[0]).append("\",\"apk_packagename\":\"").append(arr[1]).append("\"}");
			if(i<list.size()-1) {
				sb.append(",");
			}
		}
		sb.append("]");
		params.add(new BasicNameValuePair("updates", sb.toString()));
		HttpEntity httpentity;
		try {
			httpentity = new UrlEncodedFormEntity(params, DEFAULT_CHARSET);
			httpPost.setEntity(httpentity);  
			// 取得默认的HttpClient  
			HttpClient httpclient = new DefaultHttpClient();  
			// 取得HttpResponse  
			HttpResponse httpResponse = httpclient.execute(httpPost);  
			// HttpStatus.SC_OK表示连接成功  
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
			// 取得返回的字符串  
				String strResult = EntityUtils.toString(httpResponse.getEntity(), DEFAULT_CHARSET);  
				System.out.println("================== update result:"+strResult);
				return strResult;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		return null;
	}
	
	public boolean postCloundBackup(String url, List<String[]> list, String userSessionId, Context context) throws IOException {
		HttpPost httpPost=new HttpPost(url);//+"&userssionid="+userSessionId
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for(int i=0;i<list.size();i++) {
			String[] arr=list.get(i);
//			System.out.println(arr[0]+", "+arr[1]);
			sb.append("{\"apk_versioncode\":\"").append(arr[0]).append("\",\"apk_packagename\":\"").append(arr[1]).append("\"}");
			if(i<list.size()-1) {
				sb.append(",");
			}
		}
		sb.append("]");
		params.add(new BasicNameValuePair("market_username", userSessionId));
		params.add(new BasicNameValuePair("backups", sb.toString()));
		HttpEntity httpentity;
		try {
			httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(httpentity);
			// 取得默认的HttpClient  
			HttpClient httpclient = new DefaultHttpClient();  
			// 取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpPost);
			// HttpStatus.SC_OK表示连接成功  
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// 取得返回的字符串
				HttpEntity httpEntity=httpResponse.getEntity();
				if(httpEntity!=null) {
					String result=EntityUtils.toString(httpEntity, DEFAULT_CHARSET);
					
					System.out.println("cloud backup result:"+result);
					httpEntity.consumeContent();
					int num=0;
					try{
						num=Integer.valueOf(result);
						if(num==1) {
							return true;
						}
					}catch(NumberFormatException e) {
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} 
		return false;
	}
	
	public String postCloundRestore(String url, String value) throws IOException {
		HttpPost httpPost=new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("market_username", value));
		HttpEntity httpentity;
		try {
			httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(httpentity);
			// 取得默认的HttpClient  
			HttpClient httpclient = new DefaultHttpClient();  
			// 取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpPost);
			// HttpStatus.SC_OK表示连接成功  
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// 取得返回的字符串
				HttpEntity httpEntity=httpResponse.getEntity();
				if(httpEntity!=null) {
					String result=EntityUtils.toString(httpEntity, DEFAULT_CHARSET);
					
					System.out.println("cloud restore result:" + result);
					httpEntity.consumeContent();
					return result;
				}
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println(e);
		}
		return null;
	}
	
	/**
	 * post方式请求数据
	 * @param url
	 * @param paramMap			//参数值map
	 * @return					//返回服务器返回的字符串
	 * @throws IOException
	 */
	public String postResponseData(String url, Map<String, String> paramMap) throws IOException {
//		HttpPost post = new HttpPost(url);
		if (post == null) {
			post = new HttpPost();
		}
		try {
			post.setURI(new URI(url));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (paramMap != null) {
			Set<Entry<String, String>> set = paramMap.entrySet();
			for (Entry<String, String> entry : set) {
//				System.out.println(entry.getKey() + ": " + entry.getValue());
				params.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
		}
		HttpResponse response = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			response = new DefaultHttpClient().execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String result = EntityUtils.toString(entity);
					entity.consumeContent();
					return result;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 中断post连接
	 */
	public void abortPostReq() {
		if (post != null && !post.isAborted()) {
			post.abort();
		}
	}
}
