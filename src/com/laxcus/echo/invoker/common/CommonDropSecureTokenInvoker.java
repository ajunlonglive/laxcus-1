/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.secure.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.util.naming.*;

/**
 * 删除密钥令牌调用器
 * 
 * @author scott.liang
 * @version 1.0 2/14/2021
 * @since laxcus 1.0
 */
public abstract class CommonDropSecureTokenInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造删除密钥令牌调用器，指定命令
	 * @param cmd 删除密钥令牌
	 */
	protected CommonDropSecureTokenInvoker(DropSecureToken cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSecureToken getCommand() {
		return (DropSecureToken) super.getCommand();
	}

	/**
	 * 删除单元
	 * @return 返回DropSecureTokenItem实例
	 */
	protected DropSecureTokenItem pickup() {
		DropSecureTokenItem item = new DropSecureTokenItem(getLocal(), true);

		DropSecureToken cmd = getCommand();
		for (Naming naming : cmd.getNames()) {
			boolean success = SecureController.getInstance().removeToken(naming);
			DropSecureTokenSlice slice = new DropSecureTokenSlice(naming, success);
			item.add(slice);
		}

		return item;
	}

}