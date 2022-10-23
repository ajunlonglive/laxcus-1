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
 * 更新备注信息
 * 
 * @author scott.liang
 * @version 1.0 5/25/2015
 * @since laxcus 1.0
 */
class UpdateRemark {

	/** 更新行数 **/
	private long rows;

	/** 更新成功 **/
	private boolean successful;

	/** CACHE到CHUNK状态的数据块统计，即是写满的数据块统计 **/
	private int completes;

	/**
	 * 构造默认的更新备注信息
	 */
	public UpdateRemark() {
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
	 * 增加更新行数
	 * @param n
	 */
	public void addRows(int n) {
		if (n < 0) {
			throw new IllegalValueException("illegal value:%d", n);
		}
		rows += n;
	}

	/**
	 * 返回更新行数
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