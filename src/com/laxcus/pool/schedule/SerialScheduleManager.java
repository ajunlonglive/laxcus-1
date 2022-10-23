/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool.schedule;

import java.util.*;

import com.laxcus.util.*;

/**
 * 串行工作管理器
 * 
 * 串行处理工作将以资源名称为基础，采用先进先出的原则处理。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class SerialScheduleManager {
	
	/** 被锁定的资源名称 **/
	private String resource;

	/** 运行器 **/
	private SerialSchedule runner;

	/** 处于等待状态的串行处理工作接口队列，采用先进先出的原则 **/
	private ArrayList<SerialSchedule> array = new ArrayList<SerialSchedule>();

	/**
	 * 构造串行工作管理器
	 */
	private SerialScheduleManager() {
		super();
	}

	/**
	 * 构造串行工作管理器，指定资源名称
	 * @param resource 资源名称
	 */
	public SerialScheduleManager(String resource) {
		this();
		setResource(resource);
	}

	/**
	 * 设置资源名称
	 * @param e
	 */
	private void setResource(String e) {
		Laxkit.nullabled(e);

		resource = e;
	}

	/**
	 * 返回资源名称
	 * @return 资源名称
	 */
	public final String getResource() {
		return resource;
	}
	
	/**
	 * 绑定一个串行处理工作
	 * @param schedule 串行处理工作
	 * @return 绑定成功返回真，否则假
	 */
	public boolean attach(SerialSchedule schedule) {
		if (runner == null) {
			runner = schedule;
			return true;
		} else {
			array.add(schedule);
			return false;
		}
	}

	/**
	 * 解除对一个串行处理工作的绑定
	 * @param schedule 串行处理工作
	 * @return 释放成功返回真，否则假
	 */
	public boolean detach(SerialSchedule schedule) {
		boolean success = (runner == schedule);
		if (success) {
			runner = null;
		}
		return success;
	}
	
	/**
	 * 判断一个串行任务处于运行状态
	 * @param e 串行工作实例
	 * @return 返回真或者假
	 */
	public boolean isRunning(SerialSchedule e) {
		return (runner != null && runner == e);
	}

	/**
	 * 判断一个串行任务存在
	 * @param e 串行任务实例
	 * @return 返回真或者假
	 */
	public boolean contains(SerialSchedule e) {
		boolean success =  isRunning(e);
		if (!success) {
			for (SerialSchedule that : array) {
				success = (that == e);
				if (success) break;
			}
		}
		return success;
	}

	/**
	 * 判断有等待任务
	 * @return 返回真或者假
	 */
	public boolean hasNext() {
		return array.size() > 0;
	}

	/**
	 * 当前串行工作完成后（runner == null 状态），去触发下一个工作。
	 * 
	 * @return 成功返回真，否则假
	 */
	public boolean next() {
		if (runner != null) {
			return false;
		}
		if (array.isEmpty()) {
			return false;
		}
		// 取出下一个句柄
		runner = array.remove(0);
		// 唤醒下一个
		boolean success = (runner != null);
		if (success) {
			runner.attach();
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 只在退出虚拟机时使用，唤醒全部，防止任务阻塞！
	 */
	protected void wakeupAll() {
		for(SerialSchedule e : array) {
			e.attach();
		}
	}
}