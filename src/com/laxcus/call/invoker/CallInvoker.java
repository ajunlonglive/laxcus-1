/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.call.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;
import com.laxcus.util.*;

/**
 * CALL站点的异步命令调用器。
 * 
 * @author scott.liang
 * @version 1.1 10/05/2013
 * @since laxcus 1.0
 */
public abstract class CallInvoker extends EchoInvoker {

	/** 记录日志用户操作行为或者否 **/
	private static boolean userLogging = true;

	/**
	 * 设置记录用户操作行为
	 * @param b
	 */
	public static void setUserLogging(boolean b) {
		CallInvoker.userLogging = b;
	}

	/**
	 * 判断记录用户操作行为
	 * @return 返回真或者假
	 */
	public static boolean isUserLogging() {
		return CallInvoker.userLogging;
	}

	/**
	 * 构造CALL站点调用器，指定CALL站点命令
	 * @param cmd 分布给CALL站点的异步命令
	 */
	protected CallInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public CallLauncher getLauncher() {
		return (CallLauncher) super.getLauncher();
	}

	/**
	 * 返回内网地址
	 * @return Node实例
	 */
	public Node getPrivate() {
		CallSite site = getLauncher().getSite();
		return site.getPrivate();
	}

	/**
	 * 返回网关地址
	 * @return Node实例
	 */
	public Node getGateway() {
		CallSite site = getLauncher().getSite();
		return site.getPublic();
	}

	/**
	 * 发送命令到指定的站点
	 * @param hub 目标站点地址
	 * @param cmd 被发送的命令
	 * @return 建立回显缓存和成功发送命令返回“真”，否则“假”。
	 */
	protected boolean launchToHub(Node hub, Command cmd) {
		return completeTo(hub, cmd);
	}

	/**
	 * 判断获得操作许可
	 * @param siger 用户签名
	 * @param space 表名 
	 * @return 返回真或者假
	 */
	protected boolean allow(Siger siger, Space space) {
		return StaffOnCallPool.getInstance().allow(siger, space);
	}

	/**
	 * 判断获得操作许可
	 * @param siger 用户签名
	 * @param flag 共享资源标识
	 * @return 返回真或者假
	 */
	protected boolean allow(Siger siger, CrossFlag flag) {
		return StaffOnCallPool.getInstance().allow(siger, flag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 在释放资源前，生成本次记录
		if (isUserLogging() && isAlive() && getIssuer() != null) {
			// 异步调用日志
			EchoLog log = new EchoLog();
			log.setCommand(getCommand().getClass().getSimpleName());
			log.setPerfectly(isPerfectly());
			log.setMemory(isMemory());
			log.setLaunchTimestamp(new java.util.Date(getLaunchTime())); // 启动时间
			log.setRunTime(getRunTime());
			log.setReceiveFlowSize(getReceiveFlowSize()); // 接收的数据流量
			log.setSendFlowSize(getSendFlowSize()); // 发送的数据流量

			// 用户日志
			UserLog userLog = new UserLog();
			userLog.setIssuer(getIssuer());
			userLog.setLog(log);

			// 投递给调用器
			UserLogPool.getInstance().add(userLog);
		}

		// 销毁上级的数据资源
		super.destroy();
	}
}