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
 * 删除备注信息
 * 
 * @author scott.liang
 * @version 1.0 5/25/2015
 * @since laxcus 1.0
 */
class DeleteRemark {

	/** 删除行数 **/
	private long rows;

	/** 完成成功 **/
	private boolean successful;

	/**
	 * 构造默认的删除备注信息
	 */
	public DeleteRemark() {
		super();
		rows = 0;
		successful = false;
	}

	/**
	 * 增加删除行数
	 * @param n
	 */
	public void addRows(int n) {
		if (n < 0) {
			throw new IllegalValueException("illegal value:%d", n);
		}
		rows += n;
	}

	/**
	 * 返回删除行数
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