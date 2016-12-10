//package com.kingtangdata.inventoryassis.tabs;
//
//import android.app.AlertDialog;
//import android.app.TabActivity;
//import android.app.AlertDialog.Builder;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.GridView;
//import android.widget.TabHost;
//
//import com.kingtangdata.inventoryassis.R;
//import com.kingtangdata.inventoryassis.act.ActivityBarcodeQuery;
//import com.kingtangdata.inventoryassis.act.ActivityCkeckTask;
//import com.kingtangdata.inventoryassis.act.ActivityDownloadTask;
//import com.kingtangdata.inventoryassis.act.ActivityRFID;
//import com.kingtangdata.inventoryassis.act.ActivityPK;
//import com.kingtangdata.inventoryassis.act.ActivityPY;
//import com.kingtangdata.inventoryassis.act.ActivityUploadData;
//import com.kingtangdata.inventoryassis.adapter.HomeGridAdapter;
//
//public class TabsOfTop extends TabActivity implements OnItemClickListener{
//
//	private static final String TAB_1 = "tab1";
//	private static final String TAB_2 = "tab2";
//	private static final String TAB_3 = "tab3";
//	private static final String TAB_4 = "tab4";
//	private static final String TAB_5 = "tab5";
//	private static final String TAB_6 = "tab6";
//	private static final String TAB_7 = "tab7";
//	
//	private GridView gridView ;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		super.setContentView(R.layout.ui_top_tabs);
//		
//		HomeGridAdapter adapter = new HomeGridAdapter(this);
//		gridView = (GridView)findViewById(R.id.gridview);
//		gridView.setAdapter(adapter);
//		gridView.setOnItemClickListener(this);
//		
//
//		Intent intent1 = new Intent();
//		intent1.setClass(this, ActivityDownloadTask.class);
//
//		Intent intent2 = new Intent();
//		intent2.setClass(this, ActivityUploadData.class);
//
//		Intent intent3 = new Intent();
//		intent3.setClass(this, ActivityCkeckTask.class);
//
//		Intent intent4 = new Intent();
//		intent4.setClass(this, ActivityPK.class);
//		
//		Intent intent5 = new Intent();
//		intent5.setClass(this, ActivityPY.class);
//		
//		Intent intent7 = new Intent();
//		intent7.setClass(this, ActivityRFID.class);
//
//		TabHost tabHost = getTabHost();
//		tabHost.addTab(tabHost.newTabSpec(TAB_1).setIndicator("下载任务")
//				.setContent(intent1));
//		tabHost.addTab(tabHost.newTabSpec(TAB_2).setIndicator("上传结果")
//				.setContent(intent2));
//		tabHost.addTab(tabHost.newTabSpec(TAB_3).setIndicator("盘点任务")
//				.setContent(intent3));
//		tabHost.addTab(tabHost.newTabSpec(TAB_4).setIndicator("盘亏处理")
//				.setContent(intent4));
//		tabHost.addTab(tabHost.newTabSpec(TAB_5).setIndicator("盘盈处理")
//				.setContent(intent5));
//		tabHost.addTab(tabHost.newTabSpec(TAB_6).setIndicator("条码打印")
//				.setContent(intent6));
//		tabHost.addTab(tabHost.newTabSpec(TAB_7).setIndicator("RFID")
//				.setContent(intent7));
//	}
//	
//	@Override
//	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//		// TODO Auto-generated method stub
//		//点击下载任务
//		//隐藏gridview
//		gridView.setVisibility(View.GONE);
//		//显示tab页
//		getTabHost().setCurrentTab(i);
//	}
//	
//	
//	@Override
//	public void onBackPressed() {
//		// TODO Auto-generated method stub
//		Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle("温馨提示");
//		builder.setMessage("确认要退出应用？");
//		builder.setNegativeButton("取消", null);
//		builder.setNeutralButton("确定", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				finish();
//			}
//		});
//		builder.create().show();
//	}
//}
