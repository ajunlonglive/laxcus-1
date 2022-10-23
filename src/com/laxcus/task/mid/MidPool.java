/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.mid;

import com.laxcus.distribute.mid.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;

/**
 * 中间数据管理池。<br>
 * 提供基本的数据存储、检索服务。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2012
 * @since laxcus 1.0
 */
public abstract class MidPool extends DiskPool {

	/** 服务超时时间。默认60分钟 */
	private long timeout;
	
	/** 系统分配给每个成员的内存容量 **/
	private long memberMemorySize;
	
	/** 序列号生成器，在一段时间内唯一 **/
	private SerialGenerator serial = new SerialGenerator();

	/** 内存空间申请器。默认1M **/
	protected MemoryCounter counter = new MemoryCounter(0x100000L);

	/**
	 * 构造默认的中间数据管理池
	 */
	protected MidPool() {
		super();
		// 超时时间，默认60分钟。即60分钟后不提取自动删除
		setTimeout(60 * 60 * 1000); // 60 minute
		// 每个用户默认10K内存空间
		setMemberMemorySize(10240);
	}

	/**
	 * 设置成员内存容量
	 * @param i 成员内存容量
	 */
	public void setMemberMemorySize(long i) {
		memberMemorySize = i;
	}

	/**
	 * 返回成员内存容量
	 * @return 成员内存容量
	 */
	public long getMemberMemorySize() {
		return memberMemorySize;
	}

	/**
	 * 设置管理池可分配的最大内存空间
	 * @param size 最大内存(字节)
	 */
	public void setMaxMemory(long size) {
		counter.setMaxSize(size);
	}

	/**
	 * 返回管理池可分配的最大内存空间
	 * @return 最大内存(字节)
	 */
	public long getMaxMemory() {
		return counter.getMaxSize();
	}

	/**
	 * 设置磁盘数据超时时间(通常为60分钟，或管理员定义)
	 * @param millisecond 毫秒
	 */
	public void setTimeout(long millisecond) {
		timeout = millisecond;
	}

	/**
	 * 返回磁盘数据超时时间
	 * @return 长整型的超时时间
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 产生任务编号(此方法提供给调用者)
	 * @return 长整型的任务编号
	 */
	public long nextTaskId() {
		return serial.nextSerial();
	}

}