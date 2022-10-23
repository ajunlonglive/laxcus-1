/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.user.*;

/**
 * 设置一个表最大索引数目调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopSetMaxIndexesInvoker extends DesktopSetMultiUserParameterInvoker {

	/**
	 * 构造设置一个表最大索引数目调用器，指定命令 
	 * @param cmd 设置一个表最大索引数目
	 */
	public DesktopSetMaxIndexesInvoker(SetMaxIndexes cmd) {
		super(cmd);
	}

}