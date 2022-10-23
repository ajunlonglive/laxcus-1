/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.command.*;

/**
 * 单个数据块导出命令钩子
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class SingleExportEntityHook extends CommandHook {

	/**
	 * 构造默认的单个数据块导出命令钩子
	 */
	public SingleExportEntityHook() {
		super();
	}

	/**
	 * 返回上传结果
	 * 
	 * @return SingleExportEntityResult实例
	 */
	public SingleExportEntityResult getProduct() {
		return (SingleExportEntityResult) super.getResult();
	}

}