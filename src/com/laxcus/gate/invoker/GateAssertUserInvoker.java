/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;

/**
 * 判断账号存在调用器。<br>
 * 命令转发给BANK站点。<br>
 * 管理员和拥有管理员身份的用户，可以检查全部注册账号。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class GateAssertUserInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造判断账号存在调用器，指定命令
	 * @param cmd 判断账号存在命令
	 */
	public GateAssertUserInvoker(AssertUser cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须拥有DBA的权限
		boolean success = canDBA();
		if (!success) {
			refuse();
			return false;
		}

		// 转发给BANK站点
		success = transmit();
		if (!success) {
			failed();
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

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	//	 */
	//	@Override
	//	public AssertUser getCommand() {
	//		return (AssertUser) super.getCommand();
	//	}
	//
	//	/**
	//	 * 返回结果
	//	 * @param successful 成功标识
	//	 * @return 发送成功返回真，否则假
	//	 */
	//	private boolean reply(boolean successful) {
	//		AssertUser cmd = getCommand();
	//		AssertUserProduct product = new AssertUserProduct(cmd.getUsername(), successful);
	//		return replyProduct(product);
	//	}
	//	
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		// 必须拥有DBA的权限
	//		boolean success = canDBA();
	//		if (!success) {
	//			refuse();
	//			return false;
	//		}
	//
	//		// 根据签名找到HASH主机地址
	//		AssertUser cmd = getCommand();
	//		Siger siger = cmd.getUsername();
	//		// 发出查找ACCOUN站点命令
	//		success = seekSite(siger);
	//		// 不成功，向FRONT发送一个拒绝通知
	//		if (!success) {
	//			failed();
	//		}
	//
	//		Logger.debug(this, "launch", success, "check %s", siger);
	//
	//		return success;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		// 接收ACCOUNT站点
	//		Node account = replySite();
	//		boolean success = (account != null);
	//		// 反馈结果
	//		reply(success);
	//
	//		Logger.debug(this, "ending", "%s %s!", getCommand().getUsername(),
	//				(success ? "存在" : "不存在"));
	//
	//		return useful(success);
	//	}

}