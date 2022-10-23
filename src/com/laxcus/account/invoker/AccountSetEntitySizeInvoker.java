/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 修改数据块尺寸调用器
 * 
 * @author scott.liang
 * @version 1.0 2/27/2012
 * @since laxcus 1.0
 */
public class AccountSetEntitySizeInvoker extends AccountInvoker {

	/**
	 * 构造修改数据块尺寸，指定命令
	 * @param cmd - 修改数据块尺寸命令
	 */
	public AccountSetEntitySizeInvoker(SetEntitySize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetEntitySize getCommand() {
		return (SetEntitySize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetEntitySize cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 读取账号方位
		AccountSphere e = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		// 判断成功
		boolean success = (e != null); 
		if (success) {
			Space space = cmd.getSpace();
			int size = cmd.getSize();
			// 重置参数
			success = e.getAccount().setChunkSize(space, size);
			if(success) {
				success = StaffOnAccountPool.getInstance().updateAccountSphere(e);
			}
		}

		super.replyProduct(new SetEntitySizeProduct(success));

		Logger.debug(this, "launch", success, "reset %s#%d", cmd.getSpace(), cmd.getSize());

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
