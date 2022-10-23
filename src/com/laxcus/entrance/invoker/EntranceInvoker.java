/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.invoker;

import com.laxcus.entrance.*;
import com.laxcus.entrance.pool.*;
import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * ENTRANCE站点的异步命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2018
 * @since laxcus 1.0
 */
public abstract class EntranceInvoker extends EchoInvoker {

	/**
	 * 构造ENTRANCE站点调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected EntranceInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public EntranceLauncher getLauncher() {
		return (EntranceLauncher) super.getLauncher();
	}

	/**
	 * 返回BANK主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHubHost() {
		return getHub().getHost();
	}

	/**
	 * 定位账号的GATE站点
	 * @param siger 账号签名
	 * @param wide 公网地址
	 * @return 返回GATE站点
	 */
	protected Node locate(Siger siger, boolean wide) {
		return StaffOnEntrancePool.getInstance().locate(siger, wide);
	}
}