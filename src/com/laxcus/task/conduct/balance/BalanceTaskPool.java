/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.balance;

import com.laxcus.task.conduct.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT.BALANCE阶段任务管理池。<br><br>
 * 
 * BalanceTaskPool部署在CALL站点上。<br>
 * 数据平衡计算操作发生在FROM(DIFFUSE)操作之后，每个TO(CONVERGE)操作之前。<br>
 * 对应"CONDUCT"语句的"BALANCE"子语句。<br><br>
 * 
 * 它和任务实例的主要任务：<br>
 * <1> 根据BALANCE阶段命名产生对应的任务实例，分配任务实例需要的资源。<br>
 * <2> 任务实例根据输入的元数据确定最佳的可平衡计算的数据量。<br>
 * <3> 任务实例确定ToSession数量，给每个ToSession分配计算资源。<br>
 * 
 * @author scott.liang 
 * @version 1.1 10/19/2014
 * @since laxcus 1.0
 */
public final class BalanceTaskPool extends DesignTaskPool {

	/** 管理池句柄 **/
	private static BalanceTaskPool selfHandle = new BalanceTaskPool();

	/**
	 * 构造BALANCE任务管理池
	 */
	private BalanceTaskPool() {
		super(PhaseTag.BALANCE);
	}

	/**
	 * 返回静态的数据平均分配任务管理池句柄
	 * @return BalanceTaskPool实例
	 */
	public static BalanceTaskPool getInstance() {
		return BalanceTaskPool.selfHandle;
	}

	/**
	 * 根据BALANCE阶段命名建立对应的任务实例
	 * @param balance BALANCE阶段命名
	 * @return 返回BalanceTask实例，没有找到是空指针
	 */
	public BalanceTask create(Phase balance) {
		BalanceTask task = (BalanceTask) super.createTask(balance);
		if (task != null) {
			task.setFromSeeker(getFromSeeker());
			task.setToSeeker(getToSeeker());
			task.setMetaTrustor(getMetaTrustor());
		}
		return task;
	}
}