/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.from;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.task.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * FROM阶段组件管理池 <br><br>
 * 
 * FromTaskPool固定部署在DATA节点 <br>
 * 
 * @author scott.liang
 * @version 1.3 12/3/2012
 * @since laxcus 1.0
 */
public final class FromTaskPool extends RemoteTaskPool {

	/** FROM阶段组件管理池静态句柄 **/
	private static FromTaskPool selfHandle = new FromTaskPool();

	/** FROM阶段资源代理 **/
	private FromTrustor fromTrustor;

	/** CONDUCT中间数据存取代理 **/
	private FluxTrustor fluxTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造FROM阶段组件管理池
	 */
	private FromTaskPool() {
		super(PhaseTag.FROM);
	}

	/**
	 * 返回FROM阶段组件管理池静态句柄
	 * @return FROM任务管理池句柄
	 */
	public static FromTaskPool getInstance() {
		return FromTaskPool.selfHandle;
	}

	/**
	 * 设置中间数据存取代理
	 * @param e  FluxTrustor实例
	 */
	public void setFluxTrustor(FluxTrustor e) {
		fluxTrustor = e;
	}

	/**
	 * 返回中间数据存取代理
	 * @return  FluxTrustor实例
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
	 * 设置FROM阶段资源代理
	 * @param e  FromTrustor实例
	 */
	public void setFromTrustor(FromTrustor e) {
		fromTrustor = e;
	}

	/**
	 * 返回FROM阶段资源代理
	 * @return FromTrustor实例
	 */
	public FromTrustor getFromTrustor() {
		return fromTrustor;
	}

	/**
	 * 根据阶段命名建立对应的FROM(DIFFUSE)阶段任务实例
	 * @param phase FROM阶段命名
	 * @return 成功返回FROM阶段组件实例，没有返回空指针
	 */
	public FromTask create(Phase phase) {
		FromTask task = (FromTask) super.createTask(phase);
		// 设置FromTask配置参数
		if (task != null) {
			task.setFromTrustor(fromTrustor);
			task.setFluxTrustor(fluxTrustor);
			task.setTalkTrustor(talkTrustor);
		}
		return task;
	}

}