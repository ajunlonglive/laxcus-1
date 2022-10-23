/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 强制站点重新注册调用器。<br>
 * 这个公共调用器被集群的各子站点使用。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2017
 * @since laxcus 1.0
 */
public class CommonRefreshLoginInvoker extends CommonInvoker {

	/**
	 * 构造强制站点重新注册调用器，指定命令
	 * @param cmd 强制站点重新注册
	 */
	public CommonRefreshLoginInvoker(RefreshLogin cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshLogin getCommand() {
		return (RefreshLogin) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshLogin cmd = getCommand();
		// 命令来源地址
		Node node = cmd.getSourceSite();
		// 上级站点地址
		Node hub = getHub();

		// 必须比较一致
		boolean success = (Laxkit.compareTo(node, hub) == 0);
		// 通知重新注册
		if (success) {
			getLauncher().checkin(true);
		}

		// 返回结果
		RefreshLoginProduct product = new RefreshLoginProduct();
		product.add(getLocal(), success);
		replyProduct(product);

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