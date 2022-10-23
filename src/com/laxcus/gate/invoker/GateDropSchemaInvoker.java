/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;

/**
 * 删除数据库调用器。<br>
 * 删除数据库，以及数据库下的全部表记录。
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class GateDropSchemaInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造删除数据库调用器，指定命令
	 * @param cmd 删除数据库命令
	 */
	public GateDropSchemaInvoker(DropSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSchema getCommand() {
		return (DropSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropSchema cmd = getCommand();
		Fame fame = cmd.getFame();
		// 判断拥有删除数据库权限
		boolean success = canDropSchema(fame);
		if (success) {
			success = transmit();// 命令转交给BANK站点去执行
		}
		if (!success) {
			refuse();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect();
	}

}