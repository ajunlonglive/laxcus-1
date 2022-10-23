/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.distant;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.task.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * DISTANT阶段组件管理池 <br><br>
 * 
 * DistantTaskPool固定部署在WORK节点 <br>
 * 
 * @author scott.liang
 * @version 1.0 5/3/2020
 * @since laxcus 1.0
 */
public final class DistantTaskPool extends RemoteTaskPool {

	/** DISTANT阶段组件管理池静态句柄 **/
	private static DistantTaskPool selfHandle = new DistantTaskPool();

	/** DISTANT阶段资源代理 **/
	private DistantTrustor distantTrustor;

	/** CONDUCT中间数据存取代理 **/
	private FluxTrustor fluxTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造DISTANT阶段组件管理池
	 */
	private DistantTaskPool() {
		super(PhaseTag.DISTANT);
	}

	/**
	 * 返回DISTANT阶段组件管理池静态句柄
	 * @return DISTANT任务管理池句柄
	 */
	public static DistantTaskPool getInstance() {
		return DistantTaskPool.selfHandle;
	}
	
	/**
	 * 设置DISTANT阶段资源代理
	 * @param e DistantTrustor实例
	 */
	public void setDistantTrustor(DistantTrustor e) {
		distantTrustor = e;
	}

	/**
	 * 返回DISTANT阶段资源代理
	 * @return DistantTrustor实例
	 */
	public DistantTrustor getDistantTrustor() {
		return distantTrustor;
	}

	/**
	 * 设置中间数据存取代理
	 * @param e FluxTrustor实例
	 */
	public void setFluxTrustor(FluxTrustor e) {
		fluxTrustor = e;
	}

	/**
	 * 返回中间数据存取代理
	 * @return FluxTrustor实例
	 */
	protected FluxTrustor getFluxTrustor() {
		return fluxTrustor;
	}

	/**
	 * 设置CONDUCT分布任务组件运行交互代理
	 * @param e TalkTrustor实例
	 */
	public void setTalkTrustor(TalkTrustor e) {
		talkTrustor = e;
	}

	/**
	 * 返回CONDUCT分布任务组件运行交互代理
	 * @return TalkTrustor实例
	 */
	protected TalkTrustor getTalkTrustor() {
		return talkTrustor;
	}
	
	/**
	 * 根据阶段命名建立对应的DISTANT阶段任务实例
	 * @param phase DISTANT阶段命名
	 * @return 成功返回DISTANT阶段组件实例，没有返回空指针
	 */
	public DistantTask create(Phase phase) {
		DistantTask task = (DistantTask) super.createTask(phase);
		// 设置DistantTask配置参数
		if (task != null) {
			task.setDistantTrustor(distantTrustor);
			task.setFluxTrustor(fluxTrustor);
			task.setTalkTrustor(talkTrustor);
		}
		return task;
	}

}