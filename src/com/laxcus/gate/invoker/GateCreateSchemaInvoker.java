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
 * 建立数据库命令调用器。<br>
 * 每个账号的数据库名具有全网唯一性，为了保证这个唯一性，需要采用串行处理。GATE站点投递给BANK站点，由BANK站点串行操作完成。
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class GateCreateSchemaInvoker extends GateInvoker {

	/**
	 * 构造建立数据库的调用器
	 * @param cmd 建立数据库命令
	 */
	public GateCreateSchemaInvoker(CreateSchema cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateSchema getCommand() {
		return (CreateSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateSchema cmd = getCommand();
		Fame fame = cmd.getSchema().getFame();
		// 判断操作者拥有建立数据库权限
		boolean success = canCreateSchema(fame);
		if (success) {
			success = transmit(); // 转发给BANK站点
		}
		if (!success) {
			refuse();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect();
	}

}