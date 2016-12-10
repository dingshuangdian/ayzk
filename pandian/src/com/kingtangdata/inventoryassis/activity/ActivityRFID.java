package com.kingtangdata.inventoryassis.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kingtangdata.inventoryassis.R;
import com.kingtangdata.inventoryassis.adapter.ItemAdapterForBD;
import com.kingtangdata.inventoryassis.adapter.PopForStringAdapter;
import com.kingtangdata.inventoryassis.base.AsyncReqTask;
import com.kingtangdata.inventoryassis.base.BaseActivity;
import com.kingtangdata.inventoryassis.bean.Plan;
import com.kingtangdata.inventoryassis.db.PlanManager;
import com.kingtangdata.inventoryassis.hardware.nfc.NfcDispatcher;
import com.kingtangdata.inventoryassis.http.domain.BaseReq;
import com.kingtangdata.inventoryassis.http.domain.BaseRes;
import com.kingtangdata.inventoryassis.util.LogUtils;
import com.kingtangdata.inventoryassis.util.MessageUtil;
import com.kingtangdata.inventoryassis.view.SliderPopupChoicer;


/**
 * 标签绑定界面
 * @author leo
 *
 */
public class ActivityRFID extends BaseActivity {
	private PullToRefreshListView mPullRefreshListView = null;
	
	private static final int SELECTED_ALL = 0;
	private static final int SELECTED_CHECKED = 1;
	private static final int SELECTED_NOT = 2;
	//默认为全部
	private int select  =  SELECTED_ALL;
	//查找字段输入内容
	private EditText search_input;
	private TextView tvNoRecord;
	//标签编码的下拉选项框
	private SliderPopupChoicer choicer;
	//用户点击时选中的plan对象
	private Plan plan = null;
	
	private ItemAdapterForBD adapter = null;
	// 请求查看第几页面
	public int nextpage = 0; 
	
	private MyTask myTask = null;
	private DataLoadTask dataLoadTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_rfid);
		super.setStatusBarVisibility(View.VISIBLE);
		super.setTopLabel(getString(R.string.title_activity_rfid));
		super.setLeftButtonText("返回");
		super.setRightButtonText("全部");
				
		//查找条件
		search_input = (EditText)findViewById(R.id.search_input);
		tvNoRecord = (TextView)findViewById(R.id.tv_no_record);
		
		String[] mData = getResources().getStringArray(R.array.marks); 
		PopForStringAdapter psadapter = new PopForStringAdapter(this, mData); 
		choicer = (SliderPopupChoicer)findViewById(R.id.checker_choice);
		choicer.setAdapter(psadapter);
		

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		mPullRefreshListView.setPullToRefreshEnabled(false);
		mPullRefreshListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				plan = (Plan)adapterView.getItemAtPosition(i);
				
//				if(NfcDispatcher.getNfcAdapter(ActivityRFID.this) != null){
					Intent intent = new Intent(ActivityRFID.this, ActivityLabelBindForm.class);
					intent.putExtra("plan", plan);
					startActivityForResult(intent, 100);
//				}else{
//					MessageUtil.setMessage("您的手机不支持NFC功能,不能进行标签绑定。", ActivityRFID.this);
//				}
			}
		});
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				
			}
		});
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				
				if(adapter.getCount() < getTotalNum()){
					tvNoRecord.setVisibility(View.GONE);
					DataLoadTask task = new DataLoadTask();
					task.execute();
				}else{
					tvNoRecord.setVisibility(View.VISIBLE);
					return;
				}
			}
		});
		adapter = new ItemAdapterForBD(this);

		ListView listView = mPullRefreshListView.getRefreshableView();
		listView.setSelector(getResources().getDrawable(android.R.color.transparent));
		listView.setAdapter(adapter);

		//加载数据
		dataLoadTask = new DataLoadTask();
		dataLoadTask.execute();
		
		myTask = new MyTask(this);
		myTask.execute(new BaseReq());
	}
	
	@Override
	public void doClickLeftBtn() {
		onBackPressed();
	}
	
	@Override
	public void doClickRightBtn() {
		
		showBottomOperationMenu();
	}
	
	@Override
	public void onBackPressed() {

		finish();
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);

		myTask = new MyTask(this);
		myTask.execute(new BaseReq());
		//标签绑定成功后返回执行的操作
		if(resultCode == RESULT_OK && requestCode==100){
			Plan p =  (Plan)data.getSerializableExtra("plan");
			
			LogUtils.logd(getClass(), "label_code = " + p.getLabel_code());
			
			plan.copyFrom(p);
			
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	
		if(myTask != null && !myTask.isCancelled()){
			myTask.cancel(true);
		}
		
		if(dataLoadTask != null && !dataLoadTask.isCancelled()){
			myTask.cancel(true);
		}
	}
	
	private class MyTask extends AsyncReqTask{
		int allCount = 0;
		int surplusCount = 0;
		int shortageCount = 0;
		int checkedCount = 0;

		TextView tvAll = null; 
		TextView tvSurplus = null;
		TextView tvShortage = null;
		TextView tvChecked = null;
	
		public MyTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			tvAll = (TextView)statusBar.findViewById(R.id.text_all);
			tvSurplus = (TextView)statusBar.findViewById(R.id.text_surplus);
			tvShortage = (TextView)statusBar.findViewById(R.id.text_shartage);
			tvChecked = (TextView)statusBar.findViewById(R.id.text_checked);
		}

		@Override
		protected BaseRes doRequest(BaseReq request) {
			
			String querySQL = "";
			
			//开始查询总标签数
			querySQL = "select count(*) as sumId from plans where running='b'";
			allCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL, null);
			
			//开始已绑定数量
			querySQL = "select count(*) as sumId from plans where is_bind in ('0','1') and running='b'";
			shortageCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL, null);
					
			//开始未绑定数量
			querySQL = "select count(*) as sumId from plans where is_bind = 'N' and running='b'";
			checkedCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL,null);
				
			return new BaseRes();
		}

		@Override
		protected void handleResponse(BaseRes response) {
			
			if(tvAll != null){
				tvAll.setText(Html.fromHtml("<font>标签总数：</font><font color=#ff0000>"+allCount+"</font>"));
			}
						
			if(tvChecked != null){
				tvChecked.setText(Html.fromHtml("<font>未绑定：</font><font color=#ff0000>"+checkedCount+"</font>"));	
			}
			
			if(tvShortage != null){
				tvShortage.setText(Html.fromHtml("<font>已绑定：</font><font color=#339933>"+shortageCount+"</font>"));
			}
			
			if(tvSurplus != null){
				tvSurplus.setText("");
			}
		}
	}
	
	/**
	 * 查询操作
	 * @param view
	 */
	public void doSearch(View view){
		//隐藏软键盘
		hideSoftKeyboard(view);
		
		adapter.clear();
		nextpage = 0;
		
		if(adapter.getCount() < getTotalNum()){
			tvNoRecord.setVisibility(View.GONE);
			DataLoadTask task = new DataLoadTask();
			task.execute();
		}else{
			tvNoRecord.setVisibility(View.VISIBLE);
			return;
		}
	
	}

	class DataLoadTask extends AsyncTask<String,Void,Object>{
		@Override
		protected void onPreExecute() {
			
			if(adapter.getCount() < getTotalNum()){
				tvNoRecord.setVisibility(View.GONE);
				nextpage ++;
			}
			else {
				tvNoRecord.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		protected Object doInBackground(String... s) {
			
			
			StringBuffer querySQL = new StringBuffer("select * from plans where check_result <> 'py' and running='b'");
			//输入查找条件
			String where_value = search_input.getText().toString();
			
			String[] params = null;
			
			//查找array.xml查询字段信息
			String[] arrayStr = getResources().getStringArray(R.array.ids); 		
			
			//下拉选择对应的字段
			String where_col = arrayStr[choicer.getSelectIndex()];		
					
			if(!where_value.equals("")){
				querySQL.append(" and  "+where_col+" like '%'||?||'%' ");
				params =new String[]{where_value};
			}
			
			if(select == SELECTED_NOT){
				querySQL.append(" and   is_bind = 'N'");
			}else if(select == SELECTED_CHECKED){
				querySQL.append(" and   is_bind in ('0','1')");
			}
			querySQL.append(" order by dept_id, label_code, device_code");
			
			
			return PlanManager.getInstance(getApplicationContext()).getPlans(querySQL, params, nextpage);
		}
		
		@Override
		protected void onPostExecute(Object result) {
			
			List<Plan> plans = (List<Plan>)result;
			
			adapter.addList(plans);
			adapter.notifyDataSetChanged();
		}
	}
	
	protected int getTotalNum() {
		
		
		StringBuffer querySQL = new StringBuffer("select count(*) as sumId from  plans where check_result <> 'py' and running='b'");
		
		//输入查找条件
		String where_value = search_input.getText().toString();
		
		String[] params = null;
		
		//查找array.xml查询字段信息
		String[] arrayStr = getResources().getStringArray(R.array.ids); 		
		
		//下拉选择对应的字段
		String where_col = arrayStr[choicer.getSelectIndex()];		
				
		if(!where_value.equals("")){
			querySQL.append(" and "+where_col+" like '%'||?||'%' ");
			params =new String[]{where_value};
		}
		
		if(select == SELECTED_NOT){
			querySQL.append(" and   is_bind = 'N'");
		}else if(select == SELECTED_CHECKED){
			querySQL.append(" and   is_bind in ('0','1')");
		}
		
		querySQL.append(" order by dept_id, label_code, device_code");
		
		return PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL.toString(), params);
	}
	
	/**
	 * 显示底部call菜单
	 */
	public void showBottomOperationMenu() {
		final Dialog call_menu = new AlertDialog.Builder(this).create();
		call_menu.show();
		call_menu.getWindow().setWindowAnimations(R.style.PopupAnimation);
		
		final View view = View.inflate(getApplicationContext(),R.layout.ui_menu_rfid, null);
		if(select == SELECTED_ALL){
			view.findViewById(R.id.selected_all).setVisibility(View.VISIBLE);
		}else if(select == SELECTED_CHECKED){
			view.findViewById(R.id.selected_checked).setVisibility(View.VISIBLE);
		}else if(select == SELECTED_NOT){
			view.findViewById(R.id.selected_not).setVisibility(View.VISIBLE);
		}
		
		view.findViewById(R.id.ll_all).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				select = SELECTED_ALL;
				setRightButtonText("全部");
				view.findViewById(R.id.selected_all).setVisibility(View.VISIBLE);
				view.findViewById(R.id.selected_checked).setVisibility(View.GONE);
				view.findViewById(R.id.selected_not).setVisibility(View.GONE);
				xx();
				call_menu.dismiss();
			}
		});
		
		view.findViewById(R.id.ll_checked).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				select = SELECTED_CHECKED;
				setRightButtonText("已绑定");
				view.findViewById(R.id.selected_all).setVisibility(View.GONE);
				view.findViewById(R.id.selected_checked).setVisibility(View.VISIBLE);
				view.findViewById(R.id.selected_not).setVisibility(View.GONE);
				xx();
				call_menu.dismiss();
			}
		});
		
		view.findViewById(R.id.ll_not).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				select = SELECTED_NOT;
				setRightButtonText("未绑定");
				view.findViewById(R.id.selected_all).setVisibility(View.GONE);
				view.findViewById(R.id.selected_checked).setVisibility(View.GONE);
				view.findViewById(R.id.selected_not).setVisibility(View.VISIBLE);
				xx();
				call_menu.dismiss();
			}
		});
		
		view.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				call_menu.dismiss();
			}
		});
	
		Window window = call_menu.getWindow();
		WindowManager.LayoutParams wl = window.getAttributes();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int widthPixels = dm.widthPixels;
		int heightPixels = dm.heightPixels;
		wl.y = 0;
		wl.y += heightPixels / 2 - view.getHeight() / 2;
		// 对话框宽度
		wl.width = widthPixels;
		window.setAttributes(wl);
		window.setContentView(view);
	}
	
	public void xx(){
		adapter.clear();
		nextpage = 0;
		
		DataLoadTask task = new DataLoadTask();
		task.execute();
	}
}
