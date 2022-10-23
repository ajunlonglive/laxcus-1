/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.put;

import com.laxcus.task.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT.PUT阶段任务管理池
 * 
 * @author scott.liang
 * @version 1.3 12/8/2012
 * @since laxcus 1.0
 */
public final class PutTaskPool extends LocalTaskPool {

	/** CONDUCT.PUT静态句柄 **/
	private static PutTaskPool selfHandle = new PutTaskPool();

	/** 屏幕显示器 **/
	private MeetDisplay display;

	/** PUT阶段资源代理 **/
	private PutTrustor trustor;

	/**
	 * 构造CONDUCT.PUT阶段管理池
	 */
	private PutTaskPool() {
		super(PhaseTag.PUT);
	}

	/**
	 * 返回CONDUCT.PUT阶段管理池句柄
	 * @return PutTaskPool实例
	 */
	public static PutTaskPool getInstance() {
		return PutTaskPool.selfHandle;
	}

	/**
	 * 设置屏幕显示器
	 * @param e MeetDisplay实例
	 */
	public void setDisplay(MeetDisplay e) {
		display = e;
	}

	/**
	 * 设置屏幕显示器
	 * @return MeetDisplay实例
	 */
	public MeetDisplay getDisplay() {
		return display;
	}

	/**
	 * 设置PUT阶段资源代理
	 * @param e PutTrustor实例
	 */
	public void setPutTrustor(PutTrustor e) {
		trustor = e;
	}

	/**
	 * 返回PUT阶段资源代理
	 * @return  PutTrustor实例
	 */
	public PutTrustor getPutTrustor() {
		return trustor;
	}

	/**
	 * 根据阶段命名建立CONDUCT.PUT阶段任务
	 * @param phase PUT阶段命名
	 * @return 返回PUT阶段任务组件实例，没有是空指针
	 */
	public PutTask create(Phase phase) {
		PutTask task = (PutTask) super.createTask(phase);
		if (task != null) {
			// 设置屏幕显示器
			if (display != null) {
				task.setDisplay(display);
			}
			// PUT阶段资源代理
			if(trustor!=null){
			task.setPutTrustor(trustor);
			}
		}
		return task;
	}

}