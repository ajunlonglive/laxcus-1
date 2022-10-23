/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.rise;

import com.laxcus.task.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.RISE阶段任务管理池 <br>
 * 
 * 管理RISE阶段任务组件 
 * 
 * @author scott.liang
 * @version 1.1 01/07/2012
 * @since laxcus 1.0
 */
public final class RiseTaskPool extends RemoteTaskPool {

	/** RISE阶段任务管理池（一个节点只能存在一个) **/
	private static RiseTaskPool selfHandle = new RiseTaskPool();

	/** RISE阶段工作委托器 **/
	private RiseTrustor riseTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造“RISE”阶段任务管理池
	 */
	private RiseTaskPool() {
		super(PhaseTag.RISE);
	}

	/**
	 * 返回“RISE”阶段任务管理池
	 * @return
	 */
	public static RiseTaskPool getInstance() {
		return RiseTaskPool.selfHandle;
	}

	/**
	 * 设置RISE阶段工作委托器
	 * @param e RISE阶段工作委托器
	 */
	public void setRiseTrustor(RiseTrustor e) {
		riseTrustor = e;
	}

	/**
	 * 返回RISE阶段工作委托器
	 * @return
	 */
	public RiseTrustor getRiseTrustor() {
		return riseTrustor;
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
	 * 根据阶段命名建立匹配的RISE任务实例
	 * @param rise RISE阶段命名
	 * @return 返回RISE阶段任务实例
	 */
	public RiseTask create(Phase rise) {
		RiseTask task = (RiseTask) super.createTask(rise);
		if (task != null) {
			task.setRiseTrustor(riseTrustor);
			task.setTalkTrustor(talkTrustor);
		}
		return task;
	}

}