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
 * 推送DATA站点元数据调用器。<br>
 * 
 * 这个命令由DATA发出，CALL站点接收。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public final class CallPushDataFieldInvoker extends CallInvoker {

	/**
	 * 构造推送DATA站点元数据调用器，指定命令
	 * @param cmd 推送DATA站点元数据命令
	 */
	public CallPushDataFieldInvoker(PushDataField cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushDataField getCommand() {
		return (PushDataField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushDataField cmd = getCommand();
		DataOnCallPool.getInstance().push(cmd);
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
