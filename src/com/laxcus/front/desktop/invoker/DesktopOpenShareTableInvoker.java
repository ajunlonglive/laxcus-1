/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.cross.*;

/**
 * 开放数据表共享资源调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopOpenShareTableInvoker extends DesktopShareCrossInvoker {

	/**
	 * 构造开放数据表共享资源调用器，指定命令
	 * @param cmd
	 */
	public DesktopOpenShareTableInvoker(OpenShareTable cmd) {
		super(cmd);
	}

}