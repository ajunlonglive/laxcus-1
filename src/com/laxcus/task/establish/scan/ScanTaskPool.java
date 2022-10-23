/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.scan;

import com.laxcus.task.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“SCAN”阶段任务管理池。
 * 
 * @author scott.liang
 * @version 1.1 01/07/2012
 * @since laxcus 1.0
 */
public final class ScanTaskPool extends RemoteTaskPool {

	/** SCAN阶段任务管理池（一个节点只能存在一个) **/
	private static ScanTaskPool selfHandle = new ScanTaskPool();

	/** 数据表空间扫描委托器 **/
	private ScanTrustor scanTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造“SCAN”阶段任务管理池
	 */
	private ScanTaskPool() {
		super(PhaseTag.SCAN);
	}

	/**
	 * 返回“SCAN”阶段任务管理池
	 * @return  SCAN管理池句柄
	 */
	public static ScanTaskPool getInstance() {
		return ScanTaskPool.selfHandle;
	}

	/**
	 * 设置数据表空间扫描委托器
	 * @param e  ScanTrustor实例
	 */
	public void setScanTrustor(ScanTrustor e) {
		scanTrustor = e;
	}

	/**
	 * 返回表空间扫描委托器
	 * @return ScanTrustor实例
	 */
	public ScanTrustor getScanTrustor() {
		return scanTrustor;
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
	 * 根据阶段命名建立匹配的SCAN任务实例
	 * @param scan  SCAN阶段命名
	 * @return  返回SCAN阶段任务实例。没有返回空指针
	 */
	public ScanTask create(Phase scan) {
		ScanTask task = (ScanTask) super.createTask(scan);
		if (task != null) {
			task.setScanTrustor(scanTrustor);
			task.setTalkTrustor(talkTrustor);
		}
		return task;
	}

}