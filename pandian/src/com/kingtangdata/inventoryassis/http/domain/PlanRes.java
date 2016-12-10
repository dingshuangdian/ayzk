package com.kingtangdata.inventoryassis.http.domain;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.kingtangdata.inventoryassis.bean.Plan;

public class PlanRes extends BaseRes {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Expose
	private String totalnum;

	@Expose
	private List<Plan> datas;

	public List<Plan> getDatas() {
		return datas;
	}

	public void setDatas(List<Plan> datas) {
		this.datas = datas;
	}

	public String getTotalnum() {
		return totalnum;
	}

	public void setTotalnum(String totalnum) {
		this.totalnum = totalnum;
	}
}
