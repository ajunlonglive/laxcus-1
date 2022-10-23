/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 工作类站点的FlushSecureToken命令调用器 <br>
 * 
 * 用在除TOP、HOME、FRONT、WATCH四类站点之外的所有站点
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class SubFlushSecureTokenInvoker extends CommonFlushSecureTokenInvoker {

	/**
	 * 构造FlushSecureToken命令调用器，指定命令
	 * @param cmd FlushSecureToken命令
	 */
	public SubFlushSecureTokenInvoker(FlushSecureToken cmd) {
		super(cmd);
	}

	/**
	 * 向请求端返回处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		Node node = getLocal();
		FlushSecureTokenProduct product = new FlushSecureTokenProduct(node, success);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = reload();
		// 通知调用端
		reply(success);
		
		Logger.debug(this, "launch", success, "this is");
		
		// 退出
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
