/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;

/**
 * 查表命令钩子
 * 
 * @author scott.liang
 * @version 1.0 12/03/2011
 * @since laxcus 1.0
 */
public class TakeTableHook extends CommandHook {

	/**
	 * 构造查表命令钩子
	 */
	public TakeTableHook() {
		super();
	}

	/**
	 * 返回数据表
	 * @return Table实例，或者空指针
	 */
	public Table getTable() {
		return (Table) super.getResult();
	}
}
