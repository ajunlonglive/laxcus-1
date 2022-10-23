/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.init;

import com.laxcus.task.conduct.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT.INIT阶段任务管理池。<br><br>
 * 
 * InitTaskPool部署在CALL节点。<br>
 * 
 * 它和任务实例的主要工作：<br>
 * <1> 根据INIT阶段命名分配对应的任务实例。
 * <2> 任务实例确定后续的FROM阶段的FromSession数量，给每个FromSession分配计算资源。
 * <3> 任务实例给TO阶段处理流程确定迭代层次，但是不分配计算资源。
 * 
 * @author scott.liang
 * @version 1.2 12/3/2012
 * @since laxcus 1.0
 */
public final class InitTaskPool extends DesignTaskPool {

	/** InitTaskPool 静态句柄(一个进程中只允许一个存在) */
	private static InitTaskPool selfHandle = new InitTaskPool();

	/**
	 * 构造CONDUCT.INIT阶段任务管理池
	 */
	private InitTaskPool() {
		super(PhaseTag.INIT);
	}

	/**
	 * 返回初始化任务管理池实例
	 * @return
	 */
	public static InitTaskPool getInstance() {
		return InitTaskPool.selfHandle;
	}

	/**
	 * 根据命名建立匹配的初始化任务（只能建立INIT阶段任务）。
	 * @param init INIT阶段命名
	 * @return 返回INIT阶段组件实例，没有找到是空指针
	 */
	public InitTask create(Phase init) {
		InitTask task = (InitTask) super.createTask(init);
		if (task != null) {
			task.setFromSeeker(super.getFromSeeker());
			task.setToSeeker(super.getToSeeker());
			task.setMetaTrustor(getMetaTrustor());
		}
		return task;
	}

}