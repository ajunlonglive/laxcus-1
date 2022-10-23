/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示数据块尺寸调用器
 * 
 * @author scott.liang
 * @version 1.0 7/8/2018
 * @since laxcus 1.0
 */
public class AccountShowEntitySizeInvoker extends AccountInvoker {

	/**
	 * 构造显示数据块尺寸调用器，指定命令
	 * @param cmd 显示数据块尺寸命令
	 */
	public AccountShowEntitySizeInvoker(ShowEntitySize cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowEntitySize getCommand() {
		return (ShowEntitySize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowEntitySize cmd = getCommand();
		Space space = cmd.getSpace();
		Siger siger = getIssuer();

		int size = -1;
		// 获取数据块尺寸
		Account account = StaffOnAccountPool.getInstance().readAccount(siger);
		if (account != null) {
			size = account.findChunkSize(space);
		}

		// 反馈结果
		ShowEntitySizeProduct product = new ShowEntitySizeProduct(size);
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "%s chunk size is %d", space, size);

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
