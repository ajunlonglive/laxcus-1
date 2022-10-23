/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.fork;

import com.laxcus.task.contact.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT.FORK阶段任务管理池。<br><br>
 * 
 * ForkTaskPool部署在CALL节点。<br><br>
 * 
 * 它和任务实例的主要工作：<br>
 * <1> 根据FORK阶段命名分配对应的任务实例。<br>
 * <2> 部署新的任务组件。<br>
 * <3> 删除指定签名的任务组件。<br>
 * 
 * @author scott.liang
 * @version 1.2 12/3/2012
 * @since laxcus 1.0
 */
public final class ForkTaskPool extends CastTaskPool {

	/** ForkTaskPool 静态句柄(一个进程中只允许一个存在) */
	private static ForkTaskPool selfHandle = new ForkTaskPool();

	/**
	 * 构造CONDUCT.FORK阶段任务管理池
	 */
	private ForkTaskPool() {
		super(PhaseTag.FORK);
	}

	/**
	 * 返回初始化任务管理池实例
	 * @return
	 */
	public static ForkTaskPool getInstance() {
		return ForkTaskPool.selfHandle;
	}

	/**
	 * 根据命名建立匹配的初始化任务（只能建立FORK阶段任务）。
	 * @param init FORK阶段命名
	 * @return 返回FORK阶段组件实例，没有找到是空指针
	 */
	public ForkTask create(Phase init) {
		ForkTask task = (ForkTask) super.createTask(init);
		if (task != null) {
			task.setDistantSeeker(getDistantSeeker());
			task.setMetaTrustor(getMetaTrustor());
		}
		return task;
	}

}