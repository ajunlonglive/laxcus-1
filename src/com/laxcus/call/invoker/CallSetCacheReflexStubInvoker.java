/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;

/**
 * 设置缓存映像块调用器
 * 
 * @author scott.liang
 * @version 1.0 12/21/2014
 * @since laxcus 1.0
 */
public class CallSetCacheReflexStubInvoker extends CallInvoker {

	/**
	 * 构造设置缓存映像块调用器，指定命令
	 * @param cmd - 设置缓存映像块
	 */
	public CallSetCacheReflexStubInvoker(SetCacheReflexStub cmd) {
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

		int count = 0;
		for (CacheReflexStub item : cmd.list()) {
			Space space = item.getSpace();
			boolean success = StaffOnCallPool.getInstance().allow(space);
			if (success) {
				success = CacheReflexStubOnCallPool.getInstance().refresh(item);
			}
			if (success) {
				count++;
			}
		}

		boolean success = (count == cmd.size());

		Logger.debug(this, "launch", success, "sucessful size %d, item size %d", count, cmd.size());

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
