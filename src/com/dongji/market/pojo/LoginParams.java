package com.dongji.market.pojo;

import com.tencent.weibo.oauthv2.OAuthV2;

public class LoginParams {
	private String sessionId;
	private String userName;
//	private int loginState;
	private long startLoginTime;
	private String sinaUserName;
	private String tencentUserName;
	private OAuthV2 tencent_oAuth;
	public LoginParams() {}
	public LoginParams(String sessionId, String userName, int loginState, long startLoginTime, String sinaUserName, String tencentUserName, OAuthV2 tencent_oAuth) {
		this.sessionId = sessionId;
		this.userName = userName;
//		this.loginState = loginState;
		this.startLoginTime = startLoginTime;
		this.sinaUserName = sinaUserName;
		this.tencentUserName = tencentUserName;
		this.tencent_oAuth = tencent_oAuth;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/*public int getLoginState() {
		return loginState;
	}
	public void setLoginState(int loginState) {
		this.loginState = loginState;
	}*/
	public long getStartLoginTime() {
		return startLoginTime;
	}
	public void setStartLoginTime(long startLoginTime) {
		this.startLoginTime = startLoginTime;
	}
	public String getSinaUserName() {
		return sinaUserName;
	}
	public void setSinaUserName(String sinaUserName) {
		this.sinaUserName = sinaUserName;
	}
	public String getTencentUserName() {
		return tencentUserName;
	}
	public void setTencentUserName(String tencentUserName) {
		this.tencentUserName = tencentUserName;
	}
	public OAuthV2 getTencent_oAuth() {
		return tencent_oAuth;
	}
	public void setTencent_oAuth(OAuthV2 tencent_oAuth) {
		this.tencent_oAuth = tencent_oAuth;
	}
	@Override
	public String toString() {
		return "LoginParams [sessionId=" + sessionId + ", userName=" + userName
				+ ", startLoginTime="
				+ startLoginTime + ", sinaUserName=" + sinaUserName
				+ ", tencentUserName=" + tencentUserName + ", tencent_oAuth="
				+ tencent_oAuth + "]";
	}

}
