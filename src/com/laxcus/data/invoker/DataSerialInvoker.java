/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.data.pool.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.util.*;

/**
 * DATA事务操作调用器 <br>
 * 提供DATA站点下的表锁定和解锁的方法。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2015
 * @since laxcus 1.0
 */
public abstract class DataSerialInvoker extends DataInvoker implements SerialSchedule {

	/** 判断已经锁定资源，默认是假 **/
	private boolean attached = false;

	/**
	 * 构造默认和保护型的DATA事务操作调用器，指定命令
	 * @param cmd 异步处理命令
	 */
	protected DataSerialInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#attach()
	 */
	@Override
	public void attach() {
		attached = true;
		wakeup();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#isAttached()
	 */
	@Override
	public boolean isAttached() {
		return attached;
	}

	/**
	 * 申请串行操作，直到被受理。<br>
	 * 如果受理了，SerialSchedulePool会锁定资源名称，在它发出释放申请前，不会接受相同资源名称资源的申请。
	 * 
	 * @param resource 资源名称
	 */
	protected void lock(String resource) {
		// 申请串行操作资源（同名资源每次只允许一个）
		attached = SerialSchedulePool.getInstance().admit(resource, this);
		// 如果没有获得资源，一直等待，直到前面的同名资源操作完成，“attached”变量被触发，改为“真”退出。
		while (!attached) {
			delay(1000);
		}
	}

	/**
	 * 解除对一个资源的锁定
	 * @param resource 资源名称
	 */
	protected boolean unlock(String resource) {
		return SerialSchedulePool.getInstance().release(resource, this);
	}
	
	/**
	 * 将数据表名转移为资源名称 
	 * @param space 数据表名
	 * @return 资源名称
	 */
	private String translate(Space space) {
		return space.toString().toLowerCase();
	}

	/**
	 * 锁定一个数据表
	 * @param space 数据表名
	 */
	protected void lock(Space space) {
		// 不允许空指针
		Laxkit.nullabled(space);
		// 锁定表
		lock(translate(space));
	}

	/**
	 * 解锁一个数据表，忽略空指针。
	 * @param space 数据表名
	 */
	protected void unlock(Space space) {
		// 忽略空指针
		if (space == null) {
			return;
		}
		// 解除锁定
		unlock(translate(space));
	}
	
	/**
	 * 根据数据表名查找数据表
	 * @param space 数据表名
	 * @return 返回Table实例，没有找到返回空指针
	 */
	protected Table findTable(Space space) {
		return StaffOnDataPool.getInstance().findTable(space);
	}

	/**
	 * 根据数据表名判断数据表存在
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	protected boolean hasTable(Space space) {
		return StaffOnDataPool.getInstance().hasTable(space);
	}

	/**
	 * 判断用户签名和数据表名匹配且有效
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	protected boolean allow(Siger siger, Space space) {
		return StaffOnDataPool.getInstance().allow(siger, space);
	}

}