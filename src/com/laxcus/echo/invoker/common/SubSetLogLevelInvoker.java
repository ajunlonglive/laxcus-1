/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;

/**
 * 设置站点日志等级调用器 <br>
 * 用在工作节点上
 * 
 * @author scott.liang
 * @version 1.0 8/17/2017
 * @since laxcus 1.0
 */
public class SubSetLogLevelInvoker extends CommonSetLogLevelInvoker {

	/**
	 * 构造设置站点日志等级，指定命令
	 * @param cmd 设置站点日志等级
	 */
	public SubSetLogLevelInvoker(SetLogLevel cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 重新设置日志
		reset();

		// 生成结果
		SetLogLevelProduct product = new SetLogLevelProduct();
		product.add(getLocal(), true);
		// 发送处理结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "product size:%d", product.size());

		return useful(success);
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