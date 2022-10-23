/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.sift;

import com.laxcus.task.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“SIFT”阶段任务管理池。
 * 
 * @author scott.liang
 * @version 1.1 01/07/2012
 * @since laxcus 1.0
 */
public final class SiftTaskPool extends RemoteTaskPool {

	/** SIFT阶段任务管理池（一个节点只能存在一个) **/
	private static SiftTaskPool selfHandle = new SiftTaskPool();

	/** SIFT操作代理器 **/
	private SiftTrustor siftTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造“SIFT”阶段任务管理池
	 */
	private SiftTaskPool() {
		super(PhaseTag.SIFT);
	}

	/**
	 * 返回“SIFT”阶段任务管理池
	 * @return SiftTaskPool实例
	 */
	public static SiftTaskPool getInstance() {
		return SiftTaskPool.selfHandle;
	}

	/**
	 * 设置SIFT操作代理器
	 * @param e SiftTrustor实例
	 */
	public void setSiftTrustor(SiftTrustor e) {
		siftTrustor = e;
	}

	/**
	 * 返回SIFT操作代理器
	 * @return SiftTrustor实例
	 */
	public SiftTrustor getSiftTrustor() {
		return siftTrustor;
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
	 * 根据阶段命名创建匹配的SIFT任务实例
	 * @param sift SIFT阶段命名
	 * @return 返回SIFT阶段任务实例。没有返回空指针
	 */
	public SiftTask create(Phase sift) {
		SiftTask task = (SiftTask) super.createTask(sift);
		if (task != null) {
			task.setSiftTrustor(siftTrustor);
			task.setTalkTrustor(talkTrustor);
		}
		return task;
	}

}