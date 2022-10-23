/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;

/**
 * 授权删表调用器 <br>
 * 删除DATA站点上的表。只删除一个表，用户签名不能删除。
 * 
 * @author scott.liang
 * @version 1.0 09/03/2012
 * @since laxcus 1.0
 */
public class DataAwardDropTableInvoker extends DataInvoker {

	/**
	 * 构造删表调用器，指定命令
	 * @param cmd 删表命令
	 */
	public DataAwardDropTableInvoker(AwardDropTable cmd) {
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

		// 执行删除操作
		boolean success = StaffOnDataPool.getInstance().allow(space);
		if (success) {
			success = StaffOnDataPool.getInstance().dropTable(space);
		}
		// 成功，重新注册
		if (success) {
			getLauncher().checkin(false);
		}

		// 反馈结果
		reply(success);

		Logger.debug(this, "launch", success, "drop %s", space);

		// 完成操作
		return useful(success);
	}

	/**
	 * 在DATA站点的删表操作不需要“ending”方法
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}