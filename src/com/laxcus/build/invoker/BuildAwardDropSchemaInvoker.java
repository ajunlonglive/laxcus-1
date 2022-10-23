/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.build.pool.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.log.client.*;

/**
 * 授权删表命令调用器。
   BUILD站点删除关联的数据表。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2018
 * @since laxcus
 */
public class BuildAwardDropSchemaInvoker extends BuildInvoker {

	/**
	 * 构造授权删表命令调用器，指定命令
	 * @param cmd 授权删除表
	 */
	public BuildAwardDropSchemaInvoker(AwardDropSchema cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropSchema getCommand() {
		return (AwardDropSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardDropSchema cmd = getCommand();
		Fame fame = cmd.getFame();
		DropSchemaProduct product = new DropSchemaProduct(fame);
		
		// 找到全部数据表
		List<Space> spaces = StaffOnBuildPool.getInstance().findSpaces(fame);
		// 逐一删除
		for (Space space : spaces) {
			boolean success = StaffOnBuildPool.getInstance().dropTable(space);
			product.setSuccessful(success);
		}

		// 删除成功，通知重新注册
		if (product.getRights() > 0) {
			getLauncher().checkin(false);
		}

		// 要求回应时...
		if (cmd.isReply()) {
			return replyProduct(product);
		}

		Logger.debug(this, "launch", product.isSuccessful(), "drop '%s'", fame);

		return useful(product.isSuccessful());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}