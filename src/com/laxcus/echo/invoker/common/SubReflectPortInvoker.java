/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.watch.*;

/**
 * 设置映射端口调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/22/2020
 * @since laxcus 1.0
 */
public class SubReflectPortInvoker extends CommonReflectPortInvoker {

	/**
	 * 构造设置映射端口调用器，指定命令
	 * @param cmd 设置映射端口
	 */
	public SubReflectPortInvoker(ReflectPort cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReflectPortProduct product = reload();
		// 通知调用端
		replyProduct(product);
		// 退出
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
