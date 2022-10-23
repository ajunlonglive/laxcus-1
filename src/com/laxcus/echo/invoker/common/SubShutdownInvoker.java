/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.shutdown.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 子站得远程关闭调用器
 * 
 * @author scott.liang
 * @version 1.0 9/8/2019
 * @since laxcus 1.0
 */
public class SubShutdownInvoker extends CommonInvoker {

	/**
	 * 构造子站得远程关闭调用器，指定命令
	 * @param cmd 子站得远程关闭命令
	 */
	public SubShutdownInvoker(Shutdown cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Shutdown getCommand() {
		return (Shutdown) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Shutdown cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 根据命令，选择设置节点最小内存限制
		if (success) {
			shutdown(cmd.isReply(), cmd.getDelay());
		}

		// 退出
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
	
	/**
	 * 关闭本地站点
	 * @param reply 要求异步应答
	 * @param delay 延时时间
	 */
	private void shutdown(boolean reply, long delay) {
		// 反馈应答
		if (reply) {
			ShutdownProduct product = new ShutdownProduct();
			product.add(getLocal(), true);
			replyProduct(product);
		}
		
		Logger.debug(this, "shutdown", "delay %d", delay);

		// 进入延时状态
		delay(delay);
		// 通知节点关闭
		getLauncher().stop();
	}
	
}