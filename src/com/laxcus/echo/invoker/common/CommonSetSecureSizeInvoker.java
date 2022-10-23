/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.secure.*;
import com.laxcus.fixp.*;

/**
 * 设置对称密钥长度调用器
 * 
 * @author scott.liang
 * @version 1.0 2/27/2021
 * @since laxcus 1.0
 */
public abstract class CommonSetSecureSizeInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造设置对称密钥长度调用器，指定命令
	 * @param cmd 设置对称密钥长度
	 */
	protected CommonSetSecureSizeInvoker(SetSecureSize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetSecureSize getCommand() {
		return (SetSecureSize) super.getCommand();
	}

	/**
	 * 设置参数
	 * @return 返回SetSecureSizeItem实例
	 */
	protected SetSecureSizeItem pickup() {
		SetSecureSizeItem item = new SetSecureSizeItem(getLocal(), true);

		SetSecureSize cmd = getCommand();
		Cipher.setClientWidthWithBits(cmd.getClientBits());
		Cipher.setServerWidthWithBits(cmd.getServerBits());
		// 保存参数
		item.setClientBits(Cipher.getClientWidthWithBits());
		item.setServerBits(Cipher.getServerWidthWithBits());

		return item;
	}

}