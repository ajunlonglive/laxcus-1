/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.secure.*;

/**
 * 工作类站点的建立密钥令牌调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/14/2021
 * @since laxcus 1.0
 */
public class SubCreateSecureTokenInvoker extends CommonCreateSecureTokenInvoker {

	/**
	 * 构造建立密钥令牌调用器，指定命令
	 * @param cmd 建立密钥令牌
	 */
	public SubCreateSecureTokenInvoker(CreateSecureToken cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = reload();
		CreateSecureTokenProduct product = new CreateSecureTokenProduct();
		product.add(new CreateSecureTokenItem(getLocal(), success));
//		product.setProcessTime(getProcessTime());
		replyProduct(product);

		// 退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}