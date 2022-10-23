/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.missing.*;
import com.laxcus.site.Node;

/**
 * JVM内存空间不足调用器。<BR>
 * 只被子级节点调用，包括LOG/ACCOUNT/GATE/HASH/ENTRANCE/DATA/CALL/WORK/BUILD。
 * 
 * @author scott.liang
 * @version 1.0 10/30/2019
 * @since laxcus 1.0
 */
public class SubVMMemoryMissingInvoker extends CommonInvoker {

	/**
	 * 构造JVM内存空间不足调用器，指定命令
	 * @param cmd JVM内存空间不足
	 */
	public SubVMMemoryMissingInvoker(VMMemoryMissing cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public VMMemoryMissing getCommand() {
		return (VMMemoryMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 设置本地地址
		VMMemoryMissing cmd = getCommand();
		cmd.setSite(getLocal());

		// 上级管理节点
		Node hub = getHub();

		// 发送到TOP/HOME/BANK站点，不需要反馈
		boolean success = directTo(hub, cmd);

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
