/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.to;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.task.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * TO阶段任务组件管理池 <br>
 * 
 * ToTaskPool固定部署在WORK站点。
 * 
 * 
 * @author scott.liang
 * @version 1.3 12/3/2012
 * @since laxcus 1.0
 */
public final class ToTaskPool extends RemoteTaskPool {

	/** TO(CONVERGE)阶段管理池静态句柄 **/
	private static ToTaskPool selfHandle = new ToTaskPool();

	/** TO阶段资源代理 */
	private ToTrustor toTrustor;

	/** CONDUCT中间数据存取代理 **/
	private FluxTrustor fluxTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造TO阶段任务管理池
	 */
	private ToTaskPool() {
		super(PhaseTag.TO);
	}

	/**
	 * 返回TO组件管理池静态句柄(在环境中唯一)
	 * @return ToTaskPool实例
	 */
	public static ToTaskPool getInstance() {
		return ToTaskPool.selfHandle;
	}

	/**
	 * 设置TO阶段资源代理
	 * @param e ToTrustor实例
	 */
	public void setToTrustor(ToTrustor e) {
		toTrustor = e;
	}

	/**
	 * 返回TO阶段资源代理
	 * @return ToTrustor实例
	 */
	public ToTrustor getToTrustor() {
		return toTrustor;
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
	 * 根据TO阶段命名建立一个对应的TO(CONVERGE)阶段任务实例
	 * @param phase TO阶段命名
	 * @return 返回TO阶段任务组件实例，没有返回空指针
	 */
	public ToTask create(Phase phase) {
		ToTask task = (ToTask) super.createTask(phase);
		if (task != null) {
			task.setToTrustor(toTrustor);
			task.setFluxTrustor(fluxTrustor);
			task.setTalkTrustor(talkTrustor);
		}
		return task;
	}

}