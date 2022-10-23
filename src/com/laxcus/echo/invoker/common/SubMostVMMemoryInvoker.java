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
 * 子节点最大虚拟机内存使用率限制调用器
 * 
 * @author scott.liang
 * @version 1.0 1/21/2020
 * @since laxcus 1.0
 */
public class SubMostVMMemoryInvoker extends CommonInvoker {

	/**
	 * 构造子节点最大虚拟机内存使用率限制调用器，指定命令
	 * @param cmd 子节点最大虚拟机内存使用率限制命令
	 */
	public SubMostVMMemoryInvoker(MostVMMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MostVMMemory getCommand() {
		return (MostVMMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MostVMMemory cmd = getCommand();
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
			MostVMMemoryProduct product = new MostVMMemoryProduct();
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
		return false;
	}
	
	/**
	 * 重置节点虚拟机内存限制
	 */
	private boolean reset() {
		MostVMMemory cmd = getCommand();
		EchoTransfer.setMaxVMMemoryRate(cmd.getRate());
		return true;
	}

}