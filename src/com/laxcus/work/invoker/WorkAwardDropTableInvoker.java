/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.work.pool.*;

/**
 * 授权删表调用器。<br>
 * 
 * 删表命令由HOME站点发送，WORK站点无条件接受和执行删除。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class WorkAwardDropTableInvoker extends WorkInvoker {

	/**
	 * 构造授权删表调用器，指定命令
	 * @param cmd 授权删表命令
	 */
	public WorkAwardDropTableInvoker(AwardDropTable cmd) {
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

		// 判断是获得授权的数据表，包括专属表和分享表
		boolean success = StaffOnWorkPool.getInstance().allow(space);
		// 删除数据表
		if (success) {
			success = StaffOnWorkPool.getInstance().dropTable(space);
		}
		// 重新注册
		if (success) {
			getLauncher().checkin(false);
		}

		Logger.debug(this, "launch", success, "drop '%s'", space);

		// 反馈结果
		reply(success);

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
