/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.field.*;

/**
 * 推送WORK站点元数据调用器<br>
 * 命令由HOME发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public final class CallPushWorkFieldInvoker extends CallInvoker {

	/**
	 * 推送WORK站点元数据调用器，指定授与命令。
	 * @param cmd 推送WORK站点元数据命令
	 */
	public CallPushWorkFieldInvoker(PushWorkField cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushWorkField getCommand() {
		return (PushWorkField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushWorkField cmd = getCommand();
		WorkOnCallPool.getInstance().push(cmd);
		return useful();
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
