/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.assign;

import com.laxcus.task.establish.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建“ASSIGN”阶段任务管理池。
 * 
 * @author scott.liang
 * @version 1.1 1/23/2012
 * @since laxcus 1.0
 */
public final class AssignTaskPool extends SerialTaskPool {

	/** ASSIGN阶段任务管理池（一个节点只能存在一个) **/
	private static AssignTaskPool selfHandle = new AssignTaskPool();

	/**
	 * 构造“ASSIGN”阶段任务管理池
	 */
	private AssignTaskPool() {
		super(PhaseTag.ASSIGN);
	}

	/**
	 * 返回“ASSIGN”阶段任务管理池
	 * @return AssignTaskPool实例
	 */
	public static AssignTaskPool getInstance() {
		return AssignTaskPool.selfHandle;
	}

	/**
	 * 根据阶段命名建立匹配的ASSIGN任务实例
	 * @param assign ASSIGN阶段命名
	 * @return 返回ASSIGN阶段任务实例
	 */
	public AssignTask create(Phase assign) {
		AssignTask task = (AssignTask) super.createTask(assign);
		if (task != null) {
			task.setScanSeeker(getScanSeeker());
			task.setSiftSeeker(getSiftSeeker());
			task.setMetaTrustor(getMetaTrustor());
		}
		return task;
	}

}