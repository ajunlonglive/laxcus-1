/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.account.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;

/**
 * 获取分布组件标识调用器
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public class AccountTakeTaskTagInvoker extends AccountInvoker {

	/**
	 * 构造获取分布组件标识调用器，指定命令
	 * @param cmd TakeTaskTag命令
	 */
	public AccountTakeTaskTagInvoker(TakeTaskTag cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeTaskTag getCommand() {
		return (TakeTaskTag) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeTaskTag cmd = getCommand();

		TaskPart part = cmd.getPart();
		// 查找部件
		TaskTag tag = TaskOnAccountPool.getInstance().findTag(part);
		// 判断存在
		boolean success = (tag != null);
		// 发送命令
		if (success) {
			TakeTaskTagProduct product = new TakeTaskTagProduct(tag);
			success = replyProduct(product);
		} else {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
		}
		
		Logger.debug(this, "launch", success, "find '%s'", part);

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
