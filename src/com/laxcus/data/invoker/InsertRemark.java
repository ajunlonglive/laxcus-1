/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.util.*;

/**
 * 插入备注信息
 * 
 * @author scott.liang
 * @version 1.0 5/25/2015
 * @since laxcus 1.0
 */
class InsertRemark {

	/** 写入行数 **/
	private long rows;

	/** 完成成功 **/
	private boolean successful;

	/** CACHE到CHUNK状态的数据块统计，即是写满的数据块统计 **/
	private int completes;

	/**
	 * 构造默认的插入备注信息
	 */
	public InsertRemark() {
		super();
		rows = 0;
		successful = false;
		completes = 0;
	}

	/**
	 * 增加CACHE到CHUNK数据块统计
	 * @param n
	 */
	public void addCompletes(int n) {
		if (n < 0) {
			throw new IllegalValueException("illegal value:%d", n);
		}
		completes += n;
	}

	/**
	 * 返回CACHE到CHUNK数据块统计
	 * @return
	 */
	public int getCompletes() {
		return completes;
	}

	/**
	 * 判断有写满的缓存数据块
	 * @return
	 */
	public boolean isCompleted() {
		return completes > 0;
	}

	/**
	 * 增加写入行数
	 * @param i
	 */
	public void addRows(int n) {
		if (n < 0) {
			throw new IllegalValueException("illegal value:%d", n);
		}
		rows += n;
	}

	/**
	 * 返回写入行数
	 * @return
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * 设置完全成功标记
	 * @param b
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断完全成功
	 * @return
	 */
	public boolean isSuccessful() {
		return successful;
	}
}
