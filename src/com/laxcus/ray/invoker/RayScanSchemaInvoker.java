/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.scan.*;

/**
 * 扫描数据库命令调用器
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class RayScanSchemaInvoker extends RayScanReferenceInvoker {

	/**
	 * 构造扫描数据库命令调用器，指定命令
	 * @param cmd 扫描数据库命令
	 */
	public RayScanSchemaInvoker(ScanSchema cmd) {
		super(cmd);
	}

}