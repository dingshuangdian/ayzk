package com.kingtangdata.inventoryassis.version;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 
 * 用来处理版本升级时的UI提醒
 * 
 * @author liyang
 */

public class VersionHandler extends Handler {
	// 用于后台检查数据版本 及下载
	//下载失败
	public final static int APK_UPDATE_FAIL = 0001;
	
	//已经是最新版本 不需要更新
	public final static int APK_UPDATE_NONEED = 0002;
	
	//正在下载
	public final static int APK_UPDATEING = 0003;
	
	//服务器返回提示
	public final static int APK_UPDATE_DESC = 0004;
	
	// 不能用Context,只有活动才能添加窗体。
	private Context context; 

	public VersionHandler(Context context) {
		this.context = context;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {

		case APK_UPDATE_FAIL:
			makeText("升级失败");
			break;
		case APK_UPDATE_NONEED:
			makeText("没有新的版本");
			break;
		case APK_UPDATEING:
			makeText("已经在下载最新版本,请稍候片刻");
			break;	
		case APK_UPDATE_DESC:
			makeText((String)msg.obj);
			break;	
		default:
			super.handleMessage(msg);
		}
	}
	
	private Toast toast = null;
	/**
	 * 全局的toast方法
	 * 
	 * @param msg
	 */
	public void makeText(String msg) {
		if (!TextUtils.isEmpty(msg)) {
			if (toast == null) {
				toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
			} else {
				toast.setText(msg);
			}
			toast.setGravity(Gravity.BOTTOM, 0, 120);
			toast.show();
		}
	}
}
