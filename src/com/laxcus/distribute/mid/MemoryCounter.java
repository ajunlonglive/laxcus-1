/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

import com.laxcus.util.*;

/**
 * 内存空间计数器。<br>
 * 只可以在指定在内存范围内存储数据，超过将取取消
 * 
 * @author scott.liang
 * @version 1.0 12/2/2012
 * @since laxcus 1.0
 */
public class MemoryCounter {

	/** 最大内存空间 **/
	private long maxSize;

	/** 已经使用的内存空间 **/
	private long used;

	/**
	 * 构造默认的内存空间计数器
	 * @param size 占用内存空间
	 */
	public MemoryCounter(long size) {
		super();
		setMaxSize(size);
		used = 0;
	}

	/**
	 * 设置最大可用内存空间
	 * @param i 最大可用内存空间
	 */
	public void setMaxSize(long i) {
		if(i < 1) {
			throw new IllegalValueException("illegal value:%d", i);
		}
		this.maxSize = i;
	}

	/**
	 * 返回最大可用内存空间
	 * @return 最大可用内存空间
	 */
	public long getMaxSize() {
		return this.maxSize;
	}

	/**
	 * 返回剩余的内存空间
	 * @return 剩余的内存空间
	 */
	public long getLeft() {
		return maxSize - used;
	}

	/**
	 * 返回已经使用的内存空间
	 * @return 已经使用的内存空间
	 */
	public long getUsed() {
		return used;
	}

	/**
	 * 分配内存空间
	 * @param size 要求申请的内存空间
	 * @return 成功返回真，否则假
	 */
	public boolean alloc(long size) {
		// 必须是正整数
		if (size < 1) {
			return false;
		}
		// 必须小于剩余空间
		else if (size > getLeft()) {
			return false;
		}
		used += size;
		return true;
	}

	/**
	 * 归还内存空间
	 * @param size 归还的内存空间
	 * @return 成功返回真，否则假
	 */
	public boolean revert(long size) {
		if (size > used) {
			return false;
		}
		used -= size;
		return true;
	}

}