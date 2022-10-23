/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct;

import com.laxcus.distribute.meta.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.seeker.*;

/**
 * CONDUCT任务调度组件管理池。
 * 
 * @author scott.liang
 * @version 1.0 12/17/2011
 * @since laxcus 1.0
 */
public class DesignTaskPool extends RemoteTaskPool {

	/** FROM阶段资源向导接口（DATA站点） */
	private FromSeeker fromSeeker;

	/** TO阶段资源向导接口（WORK站点） **/
	private ToSeeker toSeeker;

	/** 元数据存取代理 **/
	private MetaTrustor metaTrustor;
	
	/**
	 * 构造管理池，指定阶段类型
	 * @param family 阶段类型
	 */
	protected DesignTaskPool(int family) {
		super(family);
	}

	/**
	 * 设置FROM阶段资源向导接口
	 * @param e FromSeeker实例
	 */
	public void setFromSeeker(FromSeeker e) {
		fromSeeker = e;
	}

	/**
	 * 返回FROM阶段资源向导接口
	 * @return FromSeeker实例
	 */
	public FromSeeker getFromSeeker() {
		return fromSeeker;
	}

	/**
	 * 设置TO阶段资源向导接口
	 * @param e ToSeeker实例
	 */
	public void setToSeeker(ToSeeker e) {
		toSeeker = e;
	}

	/**
	 * 返回TO阶段资源向导接口
	 * @return ToSeeker实例
	 */
	public ToSeeker getToSeeker() {
		return toSeeker;
	}

	/**
	 * 设置元数据存取代理。CallLauncher在启动时设置
	 * @param e MetaTrustor实例
	 */
	public void setMetaTrustor(MetaTrustor e) {
		metaTrustor = e;
	}

	/**
	 * 返回元数据存取代理
	 * @return MetaTrustor实例
	 */
	protected MetaTrustor getMetaTrustor() {
		return metaTrustor;
	}

}
