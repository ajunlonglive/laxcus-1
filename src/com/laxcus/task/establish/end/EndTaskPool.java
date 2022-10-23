/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.end;

import com.laxcus.task.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“ESTABLISH.END”阶段任务管理池。部署在终端上。<br><br>
 * 
 * ESTABLISH.END阶段任务与CONDUCT.PUT阶段的性质基本一致。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/12/2012
 * @since laxcus 1.0
 */
public final class EndTaskPool extends LocalTaskPool {

	/** END阶段任务管理池（一个节点只能存在一个) **/
	private static EndTaskPool selfHandle = new EndTaskPool();

	/** 终端显示器 **/
	private MeetDisplay display;

	/**
	 * 构造“ESTABLISH.END”阶段任务管理池
	 */
	private EndTaskPool() {
		super(PhaseTag.END);
	}

	/**
	 * 返回“END”阶段任务管理池
	 * @return EndTaskPool实例
	 */
	public static EndTaskPool getInstance() {
		return EndTaskPool.selfHandle;
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

	/** END阶段资源代理 **/
	private EndTrustor trustor;

	/**
	 * 设置END阶段资源代理
	 * @param e EndTrustor实例
	 */
	public void setEndTrustor(EndTrustor e) {
		trustor = e;
	}

	/**
	 * 返回END阶段资源代理
	 * @return  EndTrustor实例
	 */
	public EndTrustor getEndTrustor() {
		return trustor;
	}

	/**
	 * 根据阶段命名建立匹配的END任务实例
	 * @param end - END阶段命名
	 * @return - 返回END阶段任务实例。没有返回空指针
	 */
	public EndTask create(Phase end) {
		EndTask task = (EndTask) super.createTask(end);
		if (task != null) {
			// 设置屏幕显示器
			if (display != null) {
				task.setDisplay(display);
			}
			// END阶段资源代理
			if (trustor != null) {
				task.setEndTrustor(trustor);
			}
		}
		return task;
	}

}