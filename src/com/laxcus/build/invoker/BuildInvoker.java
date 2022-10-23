/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.build.*;
import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;

/**
 * BUILD站点的异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/23/2011
 * @since laxcus 1.0
 */
public abstract class BuildInvoker extends EchoInvoker {

	/**
	 * 构造BUILD站点调用器，指定命令
	 * @param cmd 分布命令
	 */
	protected BuildInvoker(Command cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public BuildLauncher getLauncher() {
		return (BuildLauncher) super.getLauncher();
	}

	/**
	 * 发送命令到指定的站点
	 * @param hub 目标站点地址
	 * @param cmd 被发送的命令
	 * @return 建立回显缓存和成功发送命令返回“真”，否则“假”。
	 */
	protected boolean launchToHub(Node hub, Command cmd) {
		Node[] hubs = new Node[] { hub };
		return completeTo(hubs, cmd);
	}


}