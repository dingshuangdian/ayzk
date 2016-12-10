package com.kingtangdata.inventoryassis.activity.set;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.kingtangdata.inventoryassis.R;
import com.kingtangdata.inventoryassis.base.BaseActivity;
import com.kingtangdata.inventoryassis.http.HttpConstants;
import com.kingtangdata.inventoryassis.util.LogUtils;
import com.kingtangdata.inventoryassis.util.MessageUtil;
import com.kingtangdata.inventoryassis.util.StorageUtils;


/**
 * 基础设置类
 * @author Administrator
 *
 */
public class ActivitySetAccount extends BaseActivity {
	
	private EditText mDefaultUser;//取设置默认帐号
	private EditText mPassword; //取设置密码
	
	//选中显示密码
	private CheckBox cbPassword;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		super.setContentView(R.layout.activity_setaccount);
		super.setLeftButtonText("返回");
		super.setTopLabel("帐号设置");
		
		mDefaultUser = ((EditText)findViewById(R.id.input_default_user));
		mPassword = ((EditText)findViewById(R.id.input_default_password));
		
		cbPassword = (CheckBox)findViewById(R.id.checkbox);
		cbPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (isChecked) {
					mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					mPassword.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
			}
		});
		//显示默认的值
		String username = StorageUtils.getString(StorageUtils.USER_ACCOUNT, this, "");
		String password = StorageUtils.getString(StorageUtils.USER_PASSWORD, this, "");
	
		mDefaultUser.setText(username);
		mPassword.setText(password);
	}

	public void doSave(View view) {
		String username = mDefaultUser.getText().toString().trim();
		String password = mPassword.getText().toString().trim();

		StorageUtils.setProperty(StorageUtils.USER_ACCOUNT, username,this);
		StorageUtils.setProperty(StorageUtils.USER_PASSWORD, password, this);
		
		//提示保存成功
		makeText("保存成功");
		finish();
	}
}