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
 * 工作类站点的删除密钥令牌调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/14/2021
 * @since laxcus 1.0
 */
public class SubDropSecureTokenInvoker extends CommonDropSecureTokenInvoker {

	/**
	 * 构造删除密钥令牌调用器，指定命令
	 * @param cmd 删除密钥令牌
	 */
	public SubDropSecureTokenInvoker(DropSecureToken cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropSecureTokenItem item = pickup();
		DropSecureTokenProduct product = new DropSecureTokenProduct();
		product.add(item);
//		product.setProcessTime(getProcessTime());
		replyProduct(product);

		// 退出
		return useful(item.isSuccessful());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}