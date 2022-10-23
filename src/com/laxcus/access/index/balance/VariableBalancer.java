/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import com.laxcus.access.column.attribute.*;

/**
 * 可变长索引的平衡分割器。
 * 
 * @author scott.liang
 * @version 1.0 8/12/2009
 * @since laxcus 1.0
 */
public abstract class VariableBalancer extends Bit64Balancer {

	/** 数据封装 **/
	private Packing packing;

	/**
	 * 构造可变长索引的平衡分割器
	 */
	protected VariableBalancer() {
		super();
	}

	/**
	 * 设置数据封装，允许空值
	 * @param e 数据封装实例
	 */
	public void setPacking(Packing e) {
		packing = e;
	}

	/**
	 * 返回数据封装
	 * @return 数据封装实例
	 */
	public Packing getPacking() {
		return packing;
	}

}
