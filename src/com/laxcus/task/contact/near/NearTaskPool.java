/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.near;

import com.laxcus.task.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.naming.*;

/**
 * CONTACT.NEAR阶段任务管理池
 * 
 * @author scott.liang
 * @version 1.0 5/3/2020
 * @since laxcus 1.0
 */
public final class NearTaskPool extends LocalTaskPool {

	/** CONTACT.NEAR静态句柄 **/
	private static NearTaskPool selfHandle = new NearTaskPool();

	/** 屏幕显示器 **/
	private MeetDisplay display;

	/** NEAR阶段资源代理 **/
	private NearTrustor trustor;

	/**
	 * 构造CONTACT.NEAR阶段管理池
	 */
	private NearTaskPool() {
		super(PhaseTag.NEAR);
	}

	/**
	 * 返回CONTACT.NEAR阶段管理池句柄
	 * @return NearTaskPool实例
	 */
	public static NearTaskPool getInstance() {
		return NearTaskPool.selfHandle;
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
	 * 设置NEAR阶段资源代理
	 * @param e NearTrustor实例
	 */
	public void setNearTrustor(NearTrustor e) {
		trustor = e;
	}

	/**
	 * 返回NEAR阶段资源代理
	 * @return  NearTrustor实例
	 */
	public NearTrustor getNearTrustor() {
		return trustor;
	}

	/**
	 * 根据阶段命名建立CONTACT.NEAR阶段任务
	 * @param phase NEAR阶段命名
	 * @return 返回NEAR阶段任务组件实例，没有是空指针
	 */
	public NearTask create(Phase phase) {
		NearTask task = (NearTask) super.createTask(phase);
		if (task != null) {
			// 设置屏幕显示器
			if (display != null) {
				task.setDisplay(display);
			}
			// NEAR阶段资源代理
			if (trustor != null) {
				task.setNearTrustor(trustor);
			}
		}
		return task;
	}

}