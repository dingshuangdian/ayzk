package com.kingtangdata.inventoryassis.activity;
/*
 * 标签绑定Form界面
 */
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.kingtangdata.inventoryassis.R;
import com.kingtangdata.inventoryassis.base.BaseActivity;
import com.kingtangdata.inventoryassis.bean.Plan;
import com.kingtangdata.inventoryassis.db.PlanManager;
import com.kingtangdata.inventoryassis.hardware.nfc.Convert;
import com.kingtangdata.inventoryassis.hardware.nfc.NfcDispatcher;
import com.kingtangdata.inventoryassis.util.LogUtils;
import com.kingtangdata.inventoryassis.util.MessageUtil;
import com.zxing.activity.CaptureActivity;

public class ActivityLabelBindForm extends BaseActivity{

	private TextView etMarks;        //标签编码
	private TextView etMeanscode;    //资产编号
	private TextView etDevicecode;   //设备编号
	private TextView etDevicename;   //设备名称
	private TextView etXinghao;      //设备型号
	private TextView etGuige;        //设备规格
	private TextView etChuchangcode; //出厂编号
	private TextView etChuchangdate; //出厂年月
	private TextView et_factory;     //制造厂
	private TextView etBuildaddress; //安装地点
	
	private Plan plan = null;
	
	private String rfid = ""; //扫描到的标签编码
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.ui_labelbind_modify);
		super.setTopLabel(getString(R.string.title_activity_rfid));
		super.setLeftButtonText("返回");
		
		//控件初始化
		etMarks = (TextView)findViewById(R.id.et_marks);
		etMeanscode = (TextView)findViewById(R.id.et_meanscode);
		etDevicecode = (TextView)findViewById(R.id.et_devicecode);
		etDevicename = (TextView)findViewById(R.id.et_devicename);
		etXinghao = (TextView)findViewById(R.id.et_xinghao);
		etGuige = (TextView)findViewById(R.id.et_guige);
		etChuchangcode = (TextView)findViewById(R.id.et_chuchangcode);
		etChuchangdate = (TextView)findViewById(R.id.et_chuchangdate);
		et_factory = (TextView)findViewById(R.id.et_factory);
		etBuildaddress = (TextView)findViewById(R.id.et_buildaddress);
		
		plan = (Plan) getIntent().getSerializableExtra("plan");
		if (plan != null) {
			setPlanText(plan);
		}
		
		//启动NFC适配器
		NfcDispatcher.initial(this, NfcDispatcher.TECH_DISCOVERED_FILTERS);
		//判断手机是否带有nfc模块
		if(NfcDispatcher.getNfcAdapter(this) != null){
			super.setRightButtonImage(R.drawable.rfid_connect2);
		} else {
			super.setRightButtonImage(R.drawable.rfid_disconnect);
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(NfcDispatcher.getNfcAdapter(this) != null){
			Intent intent = new Intent();
			intent.setClass(this,this.getClass());
			NfcDispatcher.enableDispatch(this,intent,NfcDispatcher.TECH_DISCOVERED_FILTERS,NfcDispatcher.ALL_TECH_LISTS); //NFC适配器生效
		}
	}	

	@Override
	protected void onPause() {
		super.onPause();
		
		if(NfcDispatcher.getNfcAdapter(this) != null){
			NfcDispatcher.disableDispatch(this); //NFC适配器失效
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		LogUtils.logd(getClass(), "onNewIntent");

		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			//应用程序接收到Intent对象时，从该Intent对象中获取Tag对象
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			//【读取的标签UID为：12345678 】
			rfid = Convert.bytesToHexString(tagFromIntent.getId());
			
			etMarks.setText(rfid);
			
			//在日志种打印扫描到的rfid数据，最好保留
			LogUtils.logd(getClass(), "rfid = " + rfid);
		}
	}
	
	/**
	 * 扫描操作
	 * @param view
	 */
	public void doScanner(View view){
		Intent intent = new Intent(ActivityLabelBindForm.this, CaptureActivity.class);
		intent.putExtra("function", "bind");
		startActivityForResult(intent, 101);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {			
		if (resultCode == Activity.RESULT_OK && requestCode == 101) {
			if (data.hasExtra("value")) {
				Bundle extras = data.getExtras();
				String barcode = extras.getString("value");
				if (!barcode.equals(""))
					etMarks.setText(barcode);
			}
		}
	}

	/**
	 * 根据plan对象给当前界面赋值
	 * @param plan
	 */
	public void setPlanText(Plan plan) {
		
		rfid = plan.getLabel_code();
		etMarks.setText(plan.getLabel_code());
		etMeanscode .setText(plan.getAssetno());
		etDevicecode .setText(plan.getDevice_code());
		etDevicename .setText(plan.getDevice_name());
		etXinghao.setText(plan.getDevice_type());
		etGuige.setText(plan.getDevice_size());
		etChuchangcode .setText(plan.getOutno());
		etChuchangdate .setText(plan.getOutdate());
		et_factory.setText(plan.getFactory());
		etBuildaddress .setText(plan.getInstall_place());
	}
	
	/**
	 * 保存方法
	 * @param view
	 */
	public void doSave(View view){
		
		if(plan == null){
			return;
		}
		
		String label_code = etMarks.getText().toString();
	
		if(TextUtils.isEmpty(label_code)){
			MessageUtil.setMessage("请您先绑定标签编码", this);
			return;
		}
		
		//开始根据扫描到的rfid数据从数据库查找对应的记录
		StringBuffer buffer = new StringBuffer();
		buffer.append("select count(*) sumId from plans where label_code = ? and det_id <> ?");
		int cnt = PlanManager.getInstance(this).getPlanCount(buffer.toString(), new String[]{label_code, plan.getDet_id()});
		
		//查找此标签编码是否已被使用
		if(cnt > 0) {
			MessageUtil.setMessage("此标签编码已被使用，不能保存！", this);
		}
		else {
			plan.setLabel_code(label_code);
			plan.setIs_bind("0");
			boolean result = PlanManager.getInstance(this).updatePlan(plan, plan.getDet_id());
			
			if(result){
				makeText("保存成功");
				Intent intent = new Intent();
				intent.putExtra("plan",plan);
				setResult(RESULT_OK,intent);
				finish();
			}else{
				makeText("保存失败");
			}
		}
	}	
}
