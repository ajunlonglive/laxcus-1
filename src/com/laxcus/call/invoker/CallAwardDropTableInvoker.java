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
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;

/**
 * 授权删表命令调用器。
 * 删表命令由HOME站点发送，要求CALL删除内存中的表资源。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus
 */
public class CallAwardDropTableInvoker extends CallInvoker {

	/**
	 * 构造授权删表命令调用器，指定命令
	 * @param cmd 授权删除表
	 */
	public CallAwardDropTableInvoker(AwardDropTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropTable getCommand() {
		return (AwardDropTable) super.getCommand();
	}

	/**
	 * 返回结果
	 * @param success 删除成功或者否
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean success) {
		AwardDropTable cmd = getCommand();
		if (cmd.isDirect()) {
			return true;
		}
		DropTableProduct product = new DropTableProduct(cmd.getSpace(), success);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardDropTable cmd = getCommand();
		Space space = cmd.getSpace();
		
		// 删除表
		boolean success = StaffOnCallPool.getInstance().allow(space);
		if (success) {
			success = StaffOnCallPool.getInstance().dropTable(space);
		}
		// 删除成功，通知重新注册
		if (success) {
			getLauncher().checkin(false);
		}

		// 反馈结果
		reply(success);

		Logger.debug(this, "launch", success, "drop '%s'", space);
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}