/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.issue;

import com.laxcus.task.establish.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“ISSUE”阶段任务管理池。
 * 
 * @author scott.liang
 * @version 1.1 01/03/2012
 * @since laxcus 1.0
 */
public final class IssueTaskPool extends SerialTaskPool {

	/** ISSUE阶段任务管理池（一个节点只能存在一个) **/
	private static IssueTaskPool selfHandle = new IssueTaskPool();

	/**
	 * 构造“ISSUE”阶段任务管理池
	 */
	private IssueTaskPool() {
		super(PhaseTag.ISSUE);
	}

	/**
	 * 返回“ISSUE”阶段任务管理池
	 * @return IssueTaskPool实例
	 */
	public static IssueTaskPool getInstance() {
		return IssueTaskPool.selfHandle;
	}

	/**
	 * 根据阶段命名建立匹配的ISSUE任务实例
	 * @param issue 阶段命名
	 * @return 返回ISSUE阶段任务实例。如果没有，返回空指针
	 */
	public IssueTask create(Phase issue) {
		IssueTask task = (IssueTask) super.createTask(issue);
		if (task != null) {
			task.setScanSeeker(getScanSeeker());
			task.setSiftSeeker(getSiftSeeker());
			task.setMetaTrustor(getMetaTrustor());
		}
		return task;
	}
}