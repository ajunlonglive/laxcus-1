/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 子站点分布处理超时调用器
 * 
 * @author scott.liang
 * @version 1.0 9/15/2019
 * @since laxcus 1.0
 */
public class SubDistributedTimeoutInvoker extends CommonInvoker {

	/**
	 * 构造子站点分布处理超时调用器，指定命令
	 * @param cmd 分布处理超时命令
	 */
	public SubDistributedTimeoutInvoker(DistributedTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DistributedTimeout getCommand() {
		return (DistributedTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DistributedTimeout cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 根据命令，设置分布处理超时
		if (success) {
			if (cmd.isCommand()) {
				EchoTransfer.setCommandTimeout(cmd.getInterval());
			} else {
				EchoTransfer.setInvokerTimeout(cmd.getInterval());
			}
		}

		// 要求反馈结果时...
		if (cmd.isReply()) {
			DistributedTimeoutProduct product = new DistributedTimeoutProduct();
			product.add(getLocal(), success);
			replyProduct(product);
		}

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
