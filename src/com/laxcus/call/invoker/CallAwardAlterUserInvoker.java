/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;

/**
 * 授权修改账号密码调用器 <br>
 * 
 * 因为CALL节点需要检查注册FRONT站点的账号密码，所以当用户更新密码后，要传递给CALL保存。
 * 
 * @author scott.liang
 * @version 1.0 3/01/2012
 * @since laxcus 1.0
 */
public class CallAwardAlterUserInvoker extends CallInvoker {

	/**
	 * 构造授权修改账号密码调用器，指定命令
	 * @param cmd 授权修改账号密码命令
	 */
	public CallAwardAlterUserInvoker(AwardAlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardAlterUser getCommand() {
		return (AwardAlterUser) super.getCommand();
	}

	/**
	 * 反馈处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		AwardAlterUser cmd = getCommand();
		// 如果命令不需要反馈，退出
		if (cmd.isDirect()) {
			return;
		}
		User user = cmd.getUser();
		AlterUserProduct product = new AlterUserProduct(user.getUsername(),success);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardAlterUser cmd = getCommand();
		User user = cmd.getUser();

		boolean success = StaffOnCallPool.getInstance().alter(user);

		// 向HOME反馈处理结果
		reply(success);

		Logger.debug(this, "launch", success, "alter %s", user);

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
