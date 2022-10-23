/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 设置缓存映像数据块命令调用器。
 * 
 * DATA站点向HOME站点发送缓存映像数据块，发送即退出，不等待HOME站点的回应，HOME站点没有异常应答。
 * 
 * @author scott.liang
 * @version 1.0 4/10/2011
 * @since laxcus 1.0
 */
public class DataSetCacheReflexStubInvoker extends DataInvoker {

	/**
	 * 构造设置缓存映像数据块命令调用器，指定命令
	 * @param cmd SetCacheReflexStub命令
	 */
	public DataSetCacheReflexStubInvoker(SetCacheReflexStub cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetCacheReflexStub getCommand() {
		return (SetCacheReflexStub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetCacheReflexStub cmd = getCommand();

		Node hub = getHub();
		boolean success = directTo(hub, cmd);

		Logger.debug(this, "launch", success, "%s direct to %s", cmd.getClass().getSimpleName(), hub);

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
