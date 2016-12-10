package com.kingtangdata.inventoryassis.http;

/**
 * 异常代码
 * 
 * @author leo
 * 
 */
public class HttpResponseCode {

	public static final String SUCCESS = "100";

	// 网络地址地不存在
	public static final String HTTP_404 = "404";
	
	// 系统异常
	public static final String SYSTEM_EXCEPTION = "900";

	// 数据异常
	public static final String DATA_EXCEPTION = "903";

	// 系统维护
	public static final String SYSTEM_MAINTAIN = "904";

	// http通信异常
	public static final String HTTP_ERROR = "5906";

	// http网络连接异常
	public static final String HTTP_SOKET_EXCEPTION = "5907";

	// http网络通信超时
	public static final String HTTP_SOKET_TIMEOUT = "5908";

}
