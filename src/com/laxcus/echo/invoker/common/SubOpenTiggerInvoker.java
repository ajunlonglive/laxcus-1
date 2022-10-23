/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 子站点开放Tigger操作类型调用器
 * 
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public class SubOpenTiggerInvoker extends CommonInvoker {

	/**
	 * 构造子站点开放Tigger操作类型调用器，指定命令
	 * @param cmd 子站点开放Tigger操作类型命令
	 */
	public SubOpenTiggerInvoker(OpenTigger cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OpenTigger getCommand() {
		return (OpenTigger) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		OpenTigger cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 根据命令，选择设置节点最小内存限制
		if (success) {
			success = reset();
		}

		// 要求反馈结果时...
		if (cmd.isReply()) {
			ProcessTiggerProduct product = new ProcessTiggerProduct();
			product.add(getLocal(), Tigger.getDefaultType(), success);
			replyProduct(product);
		}

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 重置TIGGER操作
	 */
	private boolean reset() {
		OpenTigger cmd = getCommand();
		// 加操作
		Tigger.add(cmd.getType());

		return true;
	}

}