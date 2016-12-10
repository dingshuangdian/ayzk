package com.kingtangdata.inventoryassis.activity;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kingtangdata.inventoryassis.R;
import com.kingtangdata.inventoryassis.adapter.PopForTaskAdapter;
import com.kingtangdata.inventoryassis.base.AsyncReqTask;
import com.kingtangdata.inventoryassis.base.BaseActivity;
import com.kingtangdata.inventoryassis.bean.Dept;
import com.kingtangdata.inventoryassis.bean.Plan;
import com.kingtangdata.inventoryassis.bean.Task;
import com.kingtangdata.inventoryassis.bean.User;
import com.kingtangdata.inventoryassis.db.DeptManager;
import com.kingtangdata.inventoryassis.db.PlanManager;
import com.kingtangdata.inventoryassis.db.UserManager;
import com.kingtangdata.inventoryassis.http.HttpConstants;
import com.kingtangdata.inventoryassis.http.HttpResponseCode;
import com.kingtangdata.inventoryassis.http.business.SystemProvider;
import com.kingtangdata.inventoryassis.http.domain.BaseReq;
import com.kingtangdata.inventoryassis.http.domain.BaseRes;
import com.kingtangdata.inventoryassis.http.domain.DeptRes;
import com.kingtangdata.inventoryassis.http.domain.PlanReq;
import com.kingtangdata.inventoryassis.http.domain.PlanRes;
import com.kingtangdata.inventoryassis.http.domain.TaskReq;
import com.kingtangdata.inventoryassis.http.domain.TaskRes;
import com.kingtangdata.inventoryassis.http.domain.UserRes;
import com.kingtangdata.inventoryassis.util.DialogUtil;
import com.kingtangdata.inventoryassis.util.LogUtils;
import com.kingtangdata.inventoryassis.util.StorageUtils;
import com.kingtangdata.inventoryassis.view.SliderPopupChoicer;

/**
 * 任务下载界面
 * @author leo
 */
public class ActivityDownloadTask extends BaseActivity{
	int PAGE_SIZE = 10;  
	//进度条
	ProgressBar progressBarUser;
	ProgressBar progressBarPlan;
	ProgressBar progressBarBind;
	
	//文本框
	TextView   progressTextUser;
	TextView   progressTextPlan;
	TextView   progressTextBind;
	
	//复选框
	CheckBox downloadPlanCheckBox;
	CheckBox downloadUserCheckBox;
	CheckBox downloadBindCheckBox;
	
	//下拉选项框控件
	SliderPopupChoicer choicerPlan;
	SliderPopupChoicer choicerBind;
	
	//异步任务
	TestNetworkTask tnTask = null;
	DownloadUserTask duTask = null;
	GetPlanListTask glTask = null;
	GetBindListTask gbTask = null;
	DownloadPlanTask dpTask = null;
	DownloadBindTask dbTask = null;
	
	//正在下载中的标志
	boolean isDownloadingUser = false;
	boolean isDownloadingPlan = false;
	boolean isDownloadingBind = false;
	
	boolean isDownloadUserSuccess = false;
	boolean isDownloadPlanSuccess = false;
	boolean isDownloadBindSuccess = false;
	
	DialogUtil dialogUtil = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_download_task);
		super.setTopLabel("下载任务");
		super.setLeftButtonText("返回");
		
		dialogUtil = new DialogUtil(this);
		
		downloadUserCheckBox = (CheckBox)findViewById(R.id.check_person);
		downloadPlanCheckBox = (CheckBox)findViewById(R.id.check_plan);
		downloadBindCheckBox = (CheckBox)findViewById(R.id.check_bind);
		
		progressBarUser = (ProgressBar)findViewById(R.id.progress_download1);
		progressBarPlan = (ProgressBar)findViewById(R.id.progress_download2);
		progressBarBind = (ProgressBar)findViewById(R.id.progress_download3);
		
		progressTextUser =  (TextView)findViewById(R.id.progress_text1);
		progressTextPlan =  (TextView)findViewById(R.id.progress_text2);
		progressTextBind =  (TextView)findViewById(R.id.progress_text3);
		
		choicerPlan = (SliderPopupChoicer)findViewById(R.id.checker_choice_plan);
		choicerBind = (SliderPopupChoicer)findViewById(R.id.checker_choice_bind);
		
		//如果是第一次初始化的时候隐藏下载任务和下载标签
		if(!StorageUtils.getBoolean(StorageUtils.ISINIT, this)){
			findViewById(R.id.plan_view).setVisibility(View.GONE);
			findViewById(R.id.bind_view).setVisibility(View.GONE);
		}
		
		//获取任务列表   dept_id为当前登录用户的部门id
		String user_id = StorageUtils.getString(StorageUtils.USER_ID, this, "");
		TaskReq req1 = new TaskReq();
		req1.setUser_id(user_id);
		req1.setRunning("c");
		glTask = new GetPlanListTask(this);
		glTask.execute(req1);
		
		TaskReq req2 = new TaskReq();
		req2.setUser_id(user_id);
		req2.setRunning("b");
		gbTask = new GetBindListTask(this);
		gbTask.execute(req2);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!isNetworkAvalible()){
			setRightButtonImage(R.drawable.icon_network_no);
		}else{
			tnTask = new TestNetworkTask();
			tnTask.execute();
		}
	}
	
	
	@Override
	public void onBackPressed() {
		if(isDownloadingPlan || isDownloadingUser){
			showDialog(0);
		}else{
			finish();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == 0){
			AlertDialog.Builder builder= new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("正在从服务器下载数据，是否确认要退出？");
			builder.setNeutralButton("确定", new LeftListener());
			builder.setNegativeButton("取消", null);
			return builder.create();
		}
		else if(id == 1){
			AlertDialog.Builder builder= new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("下载成功！");
			builder.setNegativeButton("确定", new SureListener());
			return builder.create();
		}
		else {
			return super.onCreateDialog(id);
		}
	}
	
	class LeftListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	}
	
	class SureListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			setResult(RESULT_OK);
			finish();
		}
	}
	
	@Override
	public void doClickLeftBtn() {
		onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//在界面销毁的时候销毁所有的进程
		if(tnTask != null && !tnTask.isCancelled()){
			tnTask.cancel(true);
		}
		
		if(duTask != null && !duTask.isCancelled()){
			duTask.cancel(true);
		}
		
		if(glTask != null && !glTask.isCancelled()){
			glTask.cancel(true);
		}
		
		if(gbTask != null && !gbTask.isCancelled()){
			gbTask.cancel(true);
		}
		
		if(dpTask != null && !dpTask.isCancelled()){
			dpTask.cancel(true);
		}
		
		if(dbTask != null && !dbTask.isCancelled()){
			dbTask.cancel(true);
		}
	}
	
	/**
	 * 点击开始下载的方法
	 * @param view
	 */
	public void doStart(View view){		
		//至少有一个选中才开始下载
		if(downloadUserCheckBox.isChecked() && !isDownloadingUser && !isDownloadUserSuccess){
			//先判断一下网络是否可用  不可用会给出提示
			if(isNetworkAvalible()){
				duTask = new DownloadUserTask(this);
				duTask.execute(new BaseReq());
			}else{
				return;
			}
		}
		
		if(downloadBindCheckBox.isChecked() && !isDownloadingBind && !isDownloadBindSuccess){
			if(choicerBind.getSelectItem() == null){
				//没选列表的时候  可以在此处给出相应有好提示
			}
			else if(isNetworkAvalible()){
				Object o = choicerBind.getSelectItem();
				//加这样一个控制主要是进一步保证类型转换不会出错
				if(o instanceof Task){
					Task t = (Task)o;
					
					//第一次请求数据
					PlanReq req = new PlanReq();
					req.setCheckId(t.getCheck_id());
					req.setRunning("b");
					req.setEachnum(PAGE_SIZE);
					
					dbTask = new DownloadBindTask(this);
					dbTask.execute(req);
				}
			}
		}
		
		if(downloadPlanCheckBox.isChecked() && !isDownloadingPlan  && !isDownloadPlanSuccess){
			
			if(choicerPlan.getSelectItem() == null){
				//没选列表的时候  可以在此处给出相应有好提示
			}
			
			else if(isNetworkAvalible()){
				Object o = choicerPlan.getSelectItem();
				//加这样一个控制主要是进一步保证类型转换不会出错
				if(o instanceof Task){
					Task t = (Task)o;
					
					//第一次请求数据
					PlanReq req = new PlanReq();
					req.setCheckId(t.getCheck_id());
					req.setRunning("c");
					req.setEachnum(PAGE_SIZE);
					
					dpTask = new DownloadPlanTask(this);
					dpTask.execute(req);
				}
			}else{
				return;
			}
		}
	} 
	
	/**
	 * 获取百分比
	 * @param x
	 * @param total
	 * @return
	 */
	 public int div(int v1, int v2) {
		  BigDecimal b1 = new BigDecimal(Double.toString(v1));
		  BigDecimal b2 = new BigDecimal(Double.toString(v2));
		  double d = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
		  return (int)(d*100);
	 }   
	
	/**
	 * 测试服务器是否连通
	 * @author Administrator
	 *
	 */
	class TestNetworkTask extends AsyncTask<String, Integer, Boolean>{
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			try {
				String ip_address = StorageUtils.getString(StorageUtils.IP_ADDRESS, ActivityDownloadTask.this, HttpConstants.DEFAULT_IP);
				String port = StorageUtils.getString(StorageUtils.PORT, ActivityDownloadTask.this, HttpConstants.DEFAULT_PORT);
				String app = StorageUtils.getString(StorageUtils.APP, ActivityDownloadTask.this, HttpConstants.DEFAULT_APP);
				String httpAddress = "http://" + ip_address + ":" + port + "/" + app + HttpConstants.TEST_ACTION;
				
				URL url = new URL(httpAddress);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setReadTimeout(HttpConstants.CONNECT_TIME_OUT);
				connection.setConnectTimeout(HttpConstants.READ_TIME_OUT);
				LogUtils.logd(getClass(), "code = " + connection.getResponseCode());
				
				return connection.getResponseCode() == 200 ;
			} catch (Exception e) {
				return false;
			}
		}
	
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				//目前判断服务器是否能连接成功就根据是否能调获取任务列表接口
				setRightButtonImage(R.drawable.icon_network_ok);
			}else{
				setRightButtonImage(R.drawable.icon_network_no);
			}
		}
	}
	
	/**
	 * 获取任务
	 * @author Administrator
	 *
	 */
	class GetPlanListTask extends AsyncReqTask{

		public GetPlanListTask(Context context) {
			super(context);
		}

		@Override
		protected BaseRes doRequest(BaseReq request) {
			return SystemProvider.getInstance(getApplicationContext()).getTask(request);
		}

		@Override
		protected void handleResponse(BaseRes response) {
			if(response == null){
				return ;
			}
			
			if(HttpResponseCode.SUCCESS.equals(response.getCode())){
				TaskRes res = (TaskRes)response;
				List<Task> tasks = res.getDatas();
				
				PopForTaskAdapter adapter = new PopForTaskAdapter(ActivityDownloadTask.this, tasks);
				choicerPlan.setAdapter(adapter);
				
				//目前判断服务器是否能连接成功就根据是否能调获取任务列表接口
				setRightButtonImage(R.drawable.icon_network_ok);
			}else{
				setRightButtonImage(R.drawable.icon_network_no);
			}
		}
	}
	
	/**
	 * 获取任务
	 * @author Administrator
	 *
	 */
	class GetBindListTask extends AsyncReqTask{

		public GetBindListTask(Context context) {
			super(context);
		}

		@Override
		protected BaseRes doRequest(BaseReq request) {
			return SystemProvider.getInstance(getApplicationContext()).getTask(request);
		}

		@Override
		protected void handleResponse(BaseRes response) {
			if(response == null){
				return ;
			}
			
			if(HttpResponseCode.SUCCESS.equals(response.getCode())){
				TaskRes res = (TaskRes)response;
				List<Task> tasks = res.getDatas();
				
				PopForTaskAdapter adapter = new PopForTaskAdapter(ActivityDownloadTask.this, tasks);
				choicerBind.setAdapter(adapter);
				
				//目前判断服务器是否能连接成功就根据是否能调获取任务列表接口
				setRightButtonImage(R.drawable.icon_network_ok);
			}else{
				setRightButtonImage(R.drawable.icon_network_no);
			}
		}
	}
	
	class DownloadPlanTask extends AsyncReqTask{
		//总记录数
		int totalNum = 0;
		//总页数
		int totalPage = 0;
		//页码
		int pageNum = 0;
		
		public DownloadPlanTask(Context context) {
			super(context);
		}

		
		@Override
		protected void onPreExecute() {
			isDownloadingPlan = true;
		}
		
		@Override
		protected BaseRes doRequest(BaseReq request) {
			PlanReq req = (PlanReq)request;
			PlanRes pres = SystemProvider.getInstance(getApplicationContext()).getPlan(req);
			
			if(!HttpResponseCode.SUCCESS.equals(pres.getCode())){
				//下载结束
				isDownloadingPlan = false;
				return pres;
			}
			
			LogUtils.logd(getClass(), "checkid = " + req.getCheckId());
			//保存checkid
			StorageUtils.setProperty(StorageUtils.CHECK_ID, req.getCheckId(),ActivityDownloadTask.this);
			//总记录数
			totalNum = Integer.valueOf(pres.getTotalnum());
			
			//计算总页数
			if(totalNum % PAGE_SIZE == 0){
				totalPage = totalNum/PAGE_SIZE;
			}else{
				totalPage = totalNum/PAGE_SIZE + 1;
			}
			
			//如果总页数小于等于0直接返回
			if(totalPage <= 0){
				isDownloadingPlan = false;
				return pres;
			}
			
			PlanManager.getInstance(getApplicationContext()).deletePlans(null,null);
			//根据总页数循环去服务器获取数据插入本地数据库
			for (int i = 0; i < totalPage; i++) {
				req.setPagenum(i+1);
				pres =  SystemProvider.getInstance(getApplicationContext()).getPlan(req);
				List<Plan> plans = pres.getDatas();
				PlanManager.getInstance(getApplicationContext()).addPlans(plans);
				
				//清除缓存，更好的利用缓存加快下载速度
				if(plans != null){
					plans.clear();
					plans = null;
				}
				
				if(pres != null){
					if(HttpResponseCode.SUCCESS.equals(pres.getCode())){
						//最后完成显示100%
						if(i == totalPage - 1){
							publishProgress(100);
							//下载任务成功的标志
							isDownloadPlanSuccess = true;
						}else{
							publishProgress(div(i, totalPage));
						}
					}else{
						//下载结束
						isDownloadingPlan = false;
						return pres;
					}
				}
			}
			//下载结束
			isDownloadingPlan = false;
			
			BaseRes res = new BaseRes();
			res.setCode(HttpResponseCode.SUCCESS);
			res.setDesc("成功");
			return res;
		}

		@Override
		protected void handleResponse(BaseRes response) {
			if(!HttpResponseCode.SUCCESS.equals(response.getCode())){
				makeText(response.getDesc());
			}else {
				invalidateResult();
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBarPlan.setProgress(values[0]);
			if(values[0] == 100){
				progressTextPlan.setText("已完成");
			}else{
				progressTextPlan.setText("已下载"+values[0] + "%");
			}
		}
	}
	
	
	class DownloadBindTask extends AsyncReqTask{
		//总记录数
		int totalNum = 0;
		//总页数
		int totalPage = 0;
		
		public DownloadBindTask(Context context) {
			super(context);
		}

		
		@Override
		protected void onPreExecute() {
			isDownloadingBind = true;
		}
		
		@Override
		protected BaseRes doRequest(BaseReq request) {
			PlanReq req = (PlanReq)request;
			PlanRes pres = SystemProvider.getInstance(getApplicationContext()).getPlan(req);
			
			if(!HttpResponseCode.SUCCESS.equals(pres.getCode())){
				//下载结束
				isDownloadingBind = false;
				return pres;
			}
			
			//总记录数
			totalNum = Integer.valueOf(pres.getTotalnum());
			
			//计算总页数
			if(totalNum % PAGE_SIZE == 0){
				totalPage = totalNum/PAGE_SIZE;
			}else{
				totalPage = totalNum/PAGE_SIZE + 1;
			}
			
			//如果总页数小于等于0直接返回
			if(totalPage <= 0){
				isDownloadingBind = false;
				return pres;
			}
			
			PlanManager.getInstance(getApplicationContext()).deletePlans(null,null);
			//根据总页数循环去服务器获取数据插入本地数据库
			for (int i = 0; i < totalPage; i++) {
				req.setPagenum(i+1);
				pres =  SystemProvider.getInstance(getApplicationContext()).getPlan(req);
				List<Plan> plans = pres.getDatas();
				PlanManager.getInstance(getApplicationContext()).addPlans(plans);
				
				//清除缓存，更好的利用缓存加快下载速度
				if(plans != null){
					plans.clear();
					plans = null;
				}
				
				if(HttpResponseCode.SUCCESS.equals(pres.getCode())){
					//最后完成显示100%
					if(i == totalPage - 1){
						publishProgress(100);
						//下载任务成功的标志
						isDownloadBindSuccess = true;
					}else{
						publishProgress(div(i, totalPage));
					}
					pres = null;
				}else{
					//下载结束
					isDownloadingBind = false;
					return pres;
				}
			}
			//下载结束
			isDownloadingBind = false;
			
			BaseRes res = new BaseRes();
			res.setCode(HttpResponseCode.SUCCESS);
			res.setDesc("成功");
			return res;
		}

		@Override
		protected void handleResponse(BaseRes response) {
			if(!HttpResponseCode.SUCCESS.equals(response.getCode())){
				makeText(response.getDesc());
			}else {
				invalidateResult();
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBarBind.setProgress(values[0]);
			if(values[0] == 100){
				progressTextBind.setText("已完成");
			}else{
				progressTextBind.setText("已下载"+values[0] + "%");
			}
		}
	}

	/**
	 * 下载完成后显示提示
	 */
	private void  invalidateResult(){
		StringBuffer buffer =  new StringBuffer();
		if(isDownloadUserSuccess){
			buffer.append("同步用户数据成功");
		}
		
		if(isDownloadBindSuccess){
			buffer.append("\n");
			buffer.append("同步绑定标签成功");
		}
		
		if(isDownloadPlanSuccess){
			buffer.append("\n");
			buffer.append("同步盘点任务成功");
		}
		
		if((!isDownloadingUser && !isDownloadingBind && !isDownloadingPlan)&&(isDownloadUserSuccess || isDownloadBindSuccess || isDownloadPlanSuccess)){
			if(StorageUtils.getBoolean(StorageUtils.ISINIT, ActivityDownloadTask.this)){
				dialogUtil.show("提示", buffer.toString(), "确定", null);
			}else{
				//设置已经初始化的标志位true
				StorageUtils.setProperty(StorageUtils.ISINIT, true, ActivityDownloadTask.this);
				showDialog(1);
			}
		}
	}
	
	/**
	 * 异步任务  
	 * @author Administrator
	 *
	 */
	class DownloadUserTask extends AsyncReqTask{
		UserRes ures = null;
		DeptRes dres = null;
		
		public DownloadUserTask(Context context) {
			super(context);
		}
		
		
		@Override
		protected void onPreExecute() {
			isDownloadingUser = true;
		}

		@Override
		protected BaseRes doRequest(BaseReq request) {
			List<User> users= null;
			List<Dept> depts = null;
			
			//计算总个数  用来判断百分比
			int totalcount = 0;
			int current = 0;
			
			//如果选中了同步用户选项框
			if(downloadUserCheckBox.isChecked()){
				ures =  SystemProvider.getInstance(getApplicationContext()).getUser(request);
				dres =  SystemProvider.getInstance(getApplicationContext()).getDept(request);
			
				//如果下载用户失败
				if(!HttpResponseCode.SUCCESS.equals(ures.getCode())){
					isDownloadingUser = false;
					return ures;
				}
				
				if(!HttpResponseCode.SUCCESS.equals(dres.getCode())){
					isDownloadingUser = false;
					return dres;
				}
				
				users = ures.getDatas();
				depts = dres.getDatas(); 
				
				if(users != null ){
					totalcount  += users.size();  
				}
				
				if(depts != null ){
					totalcount  += depts.size();  
				}
			}
			
			if(users!=null && !users.isEmpty()){
				UserManager.getInstance(getApplicationContext()).deleteUsers();
				for (int i = 0; i < users.size(); i++) {
					UserManager.getInstance(getApplicationContext()).addUser(users.get(i));
					publishProgress(div(i, totalcount));
				}
			}
			
			if(depts!=null && !depts.isEmpty()){
				if(users!=null)current = users.size(); 
				DeptManager.getInstance(getApplicationContext()).deleteDepts(null);
				for (int i = 0; i < depts.size(); i++) {
					DeptManager.getInstance(getApplicationContext()).addDept(depts.get(i));
					
					//最后完成显示100%
					if(i == depts.size() - 1){
						publishProgress(100);
						
						//下载任务成功的标志
						isDownloadUserSuccess = true;
					}else{
						publishProgress(div(current+i, totalcount));
					}
				}
			}
			
			isDownloadingUser = false;
			
			BaseRes res = new BaseRes();
			res.setCode(HttpResponseCode.SUCCESS);
			res.setDesc("成功");
			return res;
		}

		@Override
		protected void handleResponse(BaseRes response) {
			if(!HttpResponseCode.SUCCESS.equals(ures.getCode())){
				makeText(ures.getDesc());
			}else if(!HttpResponseCode.SUCCESS.equals(dres.getCode())){
				makeText(dres.getDesc());
			}else {
				invalidateResult();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBarUser.setProgress(values[0]);
			if(values[0] == 100){
				progressTextUser.setText("已完成");
			}else{
				progressTextUser.setText("已下载"+values[0] + "%");
			}
		}
	}
}