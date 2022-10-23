/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.merge;

import com.laxcus.task.contact.*;
import com.laxcus.util.naming.*;

/**
 * CONTACT.MERGE阶段任务管理池。<br><br>
 * 
 * MergeTaskPool部署在CALL节点。<br>
 * 
 * 它和任务实例的主要工作：<br>
 * <1> 根据MERGE阶段命名分配对应的任务实例。
 * 
 * @author scott.liang
 * @version 1.0 5/9/2020
 * @since laxcus 1.0
 */
public final class MergeTaskPool extends CastTaskPool {

	/** MergeTaskPool 静态句柄(一个进程中只允许一个存在) */
	private static MergeTaskPool selfHandle = new MergeTaskPool();
	
	/**
	 * 构造CONTACT.MERGE阶段任务管理池
	 */
	private MergeTaskPool() {
		super(PhaseTag.MERGE);
	}

	/**
	 * 返回合并重组任务管理池实例
	 * @return MergeTaskPool实例
	 */
	public static MergeTaskPool getInstance() {
		return MergeTaskPool.selfHandle;
	}

	/**
	 * 根据命名建立匹配的初始化任务（只能建立MERGE阶段任务）。
	 * @param merge MERGE阶段命名
	 * @return 返回MERGE阶段组件实例，没有找到是空指针
	 */
	public MergeTask create(Phase merge) {
		MergeTask task = (MergeTask) super.createTask(merge);
		if (task != null) {
			task.setDistantSeeker(getDistantSeeker());
			task.setMetaTrustor(getMetaTrustor());
		}
		return task;
	}

}