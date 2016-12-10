package com.kingtangdata.inventoryassis.activity;
/*
 * 盘查任务盘查的类
 */
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kingtangdata.inventoryassis.R;
import com.kingtangdata.inventoryassis.base.BaseActivity;
import com.kingtangdata.inventoryassis.bean.Plan;
import com.kingtangdata.inventoryassis.db.PlanManager;

public class ActivityCheckTaskForm extends BaseActivity{
	
	private TextView etMeanscode;
	private TextView etDevicecode;
	private TextView etDevicename;
	private TextView etXinghao;
	private TextView etGuige;
	private TextView etChuchangcode;
	private TextView etChuchangdate;
	private TextView etBuildaddress;
	private EditText etRemarks;
	
	//相符
	private RadioButton radioRight;
	//不相符
	private RadioButton radioWrong;

	//隐藏和显示相符不相符的VIEW
	private View hiddenView;
	//保存按钮
	private Button btn_save;
	//盘亏和盘查控制标识
	private String type = "";
	
	private Plan plan = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_checktask_form);
		super.setLeftButtonText("返回");
		
		//控件初始化
		etMeanscode = (TextView)findViewById(R.id.et_meanscode);
		etDevicecode = (TextView)findViewById(R.id.et_devicecode);
		etDevicename = (TextView)findViewById(R.id.et_devicename);
		etXinghao = (TextView)findViewById(R.id.et_xinghao);
		etGuige = (TextView)findViewById(R.id.et_guige);
		etChuchangcode = (TextView)findViewById(R.id.et_chuchangcode);
		etChuchangdate = (TextView)findViewById(R.id.et_chuchangdate);
		etBuildaddress = (TextView)findViewById(R.id.et_buildaddress);
		etRemarks = (EditText)findViewById(R.id.et_remarks);
		
		radioRight = (RadioButton)findViewById(R.id.radio_right);//相符
		radioWrong = (RadioButton)findViewById(R.id.radio_wrong);//不相符
		
		//隐藏相符不相符的布局
		hiddenView = (View)findViewById(R.id.radioLayout);
		btn_save = (Button)findViewById(R.id.btn_save);
		
	    type = getIntent().getStringExtra("type");
		if(ActivityCheckTask.PD_CHECK.equals(type)){
			//盘点处理
			super.setTopLabel(R.string.title_check);
		}else{
			//盘亏处理
			super.setTopLabel(R.string.title_pankui);
			hiddenView.setVisibility(View.GONE);
			btn_save.setText("盘亏");
		}
		
		plan = (Plan)getIntent().getSerializableExtra("plan");
		if(plan != null){
			setPlanText(plan);
		}
	}
	
	
	/**
	 * 根据plan对象给当前界面赋值
	 * @param plan
	 */
	public void setPlanText(Plan plan){
		
		if(plan.isNormal()){
			radioRight.setChecked(true);
		}else{
			radioWrong.setChecked(true);
		}
		
		etMeanscode .setText(plan.getAssetno());
		etDevicecode .setText(plan.getDevice_code());
		etDevicename .setText(plan.getDevice_name());
		etXinghao.setText(plan.getDevice_type());
		etGuige.setText(plan.getDevice_size());
		etChuchangcode .setText(plan.getOutno());
		etChuchangdate .setText(plan.getOutdate());
		etBuildaddress .setText(plan.getInstall_place());
		
		if(getIntent().getBooleanExtra("flag", false)){
			if(TextUtils.isEmpty(plan.getMemo())){
				etRemarks .setText("标签损坏");
			}else{
				etRemarks .setText(plan.getMemo());
			}
		}else{
			etRemarks .setText(plan.getMemo());
		}
	}
	
	
	/**
	 * 保存方法
	 * @param view
	 */
	public void doSave(View view){
		
		if(plan == null){
			return;
		}
		
		//如果
		if(ActivityCheckTask.PK_CHECK.equals(type)){
			plan.setCheck_result("pk");
			plan.setMemo(etRemarks.getText().toString());
			plan.setIs_normal(plan.getIs_normal());
			plan.setIs_upload("0"); //表示该记录已经变化  需要重新上传
			
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
		
		//表示是修改
		if(ActivityCheckTask.PD_CHECK.equals(type)) {
			plan.setIs_normal(radioRight.isChecked()?"1":"0");
			plan.setMemo(etRemarks.getText().toString());
			plan.setCheck_result("zc");
			plan.setIs_upload("0"); //表示该记录已经变化  需要重新上传
			
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
