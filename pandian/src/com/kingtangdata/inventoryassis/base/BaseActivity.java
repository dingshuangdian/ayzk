package com.kingtangdata.inventoryassis.base;

import net.tsz.afinal.FinalBitmap;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kingtangdata.inventoryassis.R;
import com.kingtangdata.inventoryassis.db.PlanManager;
import com.kingtangdata.inventoryassis.http.domain.BaseReq;
import com.kingtangdata.inventoryassis.http.domain.BaseRes;
import com.kingtangdata.inventoryassis.util.StorageUtils;

/**
 * @author liyang
 * @since  2012-05-09
 * 
 *        基类,包括一些通用的UI:如顶部栏，底部栏，
 */
public abstract class BaseActivity extends Activity implements View.OnClickListener, OnNetworkAvalible{
	/**
	 * 顶部视图
	 */
	protected View topView;

	
	/**
	 * 顶部：左边Button
	 */
	protected ImageView leftButton;
	
	/**
	 * 顶部：右边Button
	 */
	protected ImageView rightButton;
	
	/**
	 * 顶部带文字的按钮
	 */
	protected Button leftTextButton;
	
	/**
	 * 右边带文字的按钮
	 */
	protected Button rightTextButton; 

	/**
	 * 顶部：标题
	 */
	protected TextView title;

	/**
	 * 底部：确定提交Button
	 */
	protected Button submitButton;

	/**
	 * 底部： 按钮菜单
	 */
	protected LinearLayout menubottom;
	
	/**
	 * 中间内容：content
	 */

	protected FrameLayout ui_content;
	
	
	/**用来显示盘点汇总的状态栏*/
	protected View statusBar;
	
	
	/**
	 * 用来显示logo的视图
	 */
	protected View topLogo;
	

	/**
	 * 异常描述提示
	 */
	protected TextView exceDesc;
	
	private MyTask task = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.setContentView(R.layout.ui_base);
		buildViewsInTopBar();
		buildViewsInMiddleContent();
		
		//处理全局异常
		CrashHandler handler = new CrashHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
				
		// 将activity添加到栈中以便管理
		ActivityManager.addActivity(this);
		
//		//设置背景图片
//		String filepath= StorageUtils.getString(StorageUtils.BG_PATH, BaseActivity.this, "");
//		if(!TextUtils.isEmpty(filepath)){
//			//显示缓存中的图片
//			FinalBitmap fb = FinalBitmap.create(BaseActivity.this);
//			fb.display(ui_content, filepath);
//		}
	}

	@Override
	protected void onResume() {
		
		super.onResume();
	}

	/**
	 * 刷新状态栏数据
	 */
	protected void refresh(){
		task = new MyTask(this);
		task.execute(new BaseReq());
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
			
			//五种不同类型的数量
			String querySQL = "";
			
			//开始查询计划数
			querySQL = "select count(*) as sumId from plans where check_result in (?,?,?) and running='c'";
			allCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL, new String[]{"kb","zc","pk"});
			
			//开始查询已盘数
			querySQL = "select count(*) as sumId from plans where check_result in (?,?) and running='c'";
			checkedCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL, new String[]{"zc","pk"});
					
			//开始查询盘亏数
			querySQL = "select count(*) as sumId from plans where check_result =? and running='c'";
			shortageCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL, new String[]{"pk"});
					
			//开始查询盘盈数
			querySQL = "select count(*) as sumId from plans where check_result =? and running='c'" ;
			surplusCount = PlanManager.getInstance(getApplicationContext()).getPlanCount(querySQL, new String[]{"py"});

			return new BaseRes();
		}

		@Override
		protected void handleResponse(BaseRes response) {
			
			if(tvAll != null){
				tvAll.setText(Html.fromHtml("<font>计划：</font><font color=#ff0000>"+allCount+"</font>"));
			}
						
			if(tvChecked != null){
				tvChecked.setText(Html.fromHtml("<font>已盘：</font><font color=#ff0000>"+checkedCount+"</font>"));	
			}
			
			if(tvShortage != null){
				tvShortage.setText(Html.fromHtml("<font>盘亏：</font><font color=#ff0000>"+shortageCount+"</font>"));
			}
			
			if(tvSurplus != null){
				tvSurplus.setText(Html.fromHtml("<font>盘盈：</font><font color=#339933>"+surplusCount+"</font>"));
			}
		}
	}
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * 退出页面时清除界面内容
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (ui_content != null) {
			ui_content.removeAllViews();
			ui_content = null;
		}
		
		if(task != null && !task.isCancelled()){
			task.cancel(true);
		}
	}
	// 设置中部内容的容器变量
	private void buildViewsInMiddleContent() {
		ui_content = (FrameLayout) findViewById(R.id.ui_content);
	}

	/**
	 * 设置顶部的4件套： leftButton, label, rightButton, logo变量
	 */
	private void buildViewsInTopBar() {
		topView = findViewById(R.id.menutop);
		title = (TextView) this.findViewById(R.id.lm_textview);

		leftButton = (ImageView) this.findViewById(R.id.mkt_top_left_btn);
		leftButton.setOnClickListener(this);

		rightButton = (ImageView) this.findViewById(R.id.mkt_top_right_btn);
		rightButton.setOnClickListener(this);
	
		leftTextButton = (Button)findViewById(R.id.top_left_btn);
		leftTextButton.setOnClickListener(this);
		
		rightTextButton = (Button)findViewById(R.id.top_right_btn);
		rightTextButton.setOnClickListener(this);
		
		statusBar = findViewById(R.id.statusBar);
		topLogo = findViewById(R.id.logo);
	}
	
	/**
	 * 设置statusbar是否可见
	 */
	public void setStatusBarVisibility(int visible){
		statusBar.setVisibility(visible);
	}
	
	/**
	 * 设置statusbar是否可见
	 */
	public void setLogoVisibility(int visible){
		topLogo.setVisibility(visible);
	}
	

	// 设置top 左边按钮背景图片的可见性
	public void setLeftButtonImage(int resid) {
		if (leftButton != null){
			leftButton.setImageResource(resid);
			leftButton.setVisibility(View.VISIBLE);
		}	
	}
	
	// 设置top 左边按钮背景图片的可见性
	public void setLeftButtonText(String text) {
		if (leftTextButton != null){
			leftTextButton.setText(text);
			leftTextButton.setVisibility(View.VISIBLE);
		}	
	}

	// 显示或者隐藏顶部label
	public void setTopViewVisibility(int visible) {
		if (topView != null)
			topView.setVisibility(visible);
	}
	

	// 显示或者隐藏顶部title
	public void setTopLabelVisibility(int visible) {
		if (title != null)
			title.setVisibility(visible);
	}

	// 设置顶部title
	public void setTopLabel(int resid) {
		if (title != null)
			title.setText(resid);
	}

	// 设置顶部label
	public void setTopLabel(String str) {
		setTopLabelVisibility(View.VISIBLE);
		if (title != null)
			title.setText(str);
	}

	// 设置top 右边按钮背景图片
	public void setRightButtonImage(int resid) {
		if (rightButton != null){
			rightButton.setImageResource(resid);
			rightButton.setVisibility(View.VISIBLE);
		}	
	}

	// 设置top 右边按钮背景图片
	public void setRightButtonText(String text) {
		if (rightTextButton != null){
			rightTextButton.setText(text);
			rightTextButton.setVisibility(View.VISIBLE);
		}
	}	
	
	/**
	 * 添加子类页面的布局内容
	 */
	@Override
	public void setContentView(int layoutResID) {
		View v = View.inflate(this, layoutResID, null);
		LayoutParams params = ui_content.getLayoutParams();
		params.width = LayoutParams.FILL_PARENT;
		params.height = LayoutParams.FILL_PARENT;
		ui_content.addView(v, params);
	}
	
	/**
	 * 顶部按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.top_left_btn:
			doClickLeftBtn();
			break;
		case R.id.top_right_btn:
			doClickRightBtn();
			break;
		case R.id.mkt_top_left_btn:
			doClickLeftBtn();
			break;
		case R.id.mkt_top_right_btn:
			doClickRightBtn();
			break;	
		default:
			break;
		}
	}

	// 具体子类覆盖
	public void doClickLeftBtn() {
		finish();
	};

	// 具体子类覆盖
	public void doClickRightBtn() {

	};

	private Toast toast = null;

	/**
	 * 全局的toast方法
	 * 
	 * @param msg
	 */
	public void makeText(String msg) {
		if (toast == null) {
			toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		} else {
			toast.setText(msg);
		}
		toast.setGravity(Gravity.BOTTOM, 0, 120);
		toast.show();
	}

	/**
	 * 隐藏软键盘
	 */
	public void hideSoftKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	/**
	 * 判断是否有网络
	 * 
	 * @param context
	 * @return boolean 是否存在网络
	 */
	@Override
	public boolean isNetworkAvalible() {		
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			makeText("网络不给力，请稍候再试");
			return false;
		} else if (!networkInfo.isAvailable()) {
			makeText("网络不给力，请稍候再试");
			return false;
		}
		return true;
	}
}
