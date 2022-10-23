/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact;

import com.laxcus.distribute.meta.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.seeker.*;

/**
 * CONTACT任务调度组件管理池。
 * 
 * @author scott.liang
 * @version 1.0 12/17/2011
 * @since laxcus 1.0
 */
public class CastTaskPool extends RemoteTaskPool {

	/** DISTANT阶段资源向导接口（WORK站点） **/
	private DistantSeeker distantSeeker;

	/** 元数据存取代理 **/
	private MetaTrustor metaTrustor;
	
	/**
	 * 构造管理池，指定阶段类型
	 * @param family 阶段类型
	 */
	protected CastTaskPool(int family) {
		super(family);
	}

	/**
	 * 设置DISTANT阶段资源向导接口
	 * @param e DistantSeeker实例
	 */
	public void setDistantSeeker(DistantSeeker e) {
		distantSeeker = e;
	}

	/**
	 * 返回DISTANT阶段资源向导接口
	 * @return DistantSeeker实例
	 */
	public DistantSeeker getDistantSeeker() {
		return distantSeeker;
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