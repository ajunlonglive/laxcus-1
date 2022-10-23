/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import com.laxcus.util.lock.*;

/**
 * 日志记录缓存
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public final class LogBuffer extends SingleHandler {

	/** 字符串缓存，最小5K字节长度 **/
	private StringBuilder buff = new StringBuilder(5120);

	/**
	 * 构造日志缓存
	 */
	public LogBuffer() {
		super();
	}

	/**
	 * 将内存空间调整为指定大小
	 * 
	 * @param capacity 指定内存尺寸
	 */
	public boolean ensure(int capacity) {
		super.lock();
		try {
			if (capacity > 10240) {
				buff.ensureCapacity(capacity);
				return true;
			}
			return false;
		} finally {
			super.unlock();
		}
	}

	/**
	 * 当前日志容量
	 * @return
	 */
	public int capacity() {
		super.lock();
		try {
			return buff.capacity();
		}finally {
			super.unlock();
		}
	}

	/**
	 * 判断日志“满”
	 * @return
	 */
	public boolean isFull() {
		super.lock();
		try {
			int len = buff.length();
			return len + 1024 >= buff.capacity();
		} finally {
			super.unlock();
		}
	}

	/**
	 * 添加日志
	 * @param log
	 */
	public void append(String log) {
		super.lock();
		try {
			if (log != null) {
				buff.append(log);
			}
		} catch (Throwable e) {

		} finally {
			super.unlock();
		}
	}

	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * 返回日志长度
	 * @return
	 */
	public int length() {
		super.lock();
		try {
			return buff.length();
		} finally {
			super.unlock();
		}
	}

	/**
	 * 清除日志
	 */
	public void clear() {
		super.lock();
		try {
			int len = buff.length();
			if (len > 0) {
				buff.delete(0, len);
			}
		} catch (Throwable e) {

		} finally {
			super.unlock();
		}
	}

	/**
	 * 移除日志
	 * @return 返回字符串
	 */
	public String remove() {
		String log = "";
		super.lock();
		try {
			int len = buff.length();
			if (len > 0) {
				log = buff.toString();
				buff.delete(0, len);
			}
		} catch (Throwable e) {

		} finally {
			super.unlock();
		}
		return log;
	}
	
	/**
	 * 输出日志，但是内存数据不清除
	 * @return 返回字符串
	 */
	public String flush() {
		String log = "";
		// 锁定！
		super.lock();
		try {
			int len = buff.length();
			if (len > 0) {
				log = buff.toString();
			}
		} catch (Throwable e) {

		} finally {
			super.unlock();
		}
		return log;
	}
}