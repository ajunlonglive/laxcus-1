/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.missing.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 内存空间不足调用器。<BR>
 * 只被子级节点调用，包括LOG/ACCOUNT/GATE/HASH/ENTRANCE/DATA/CALL/WORK/BUILD。
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class SubMemoryMissingInvoker extends CommonInvoker {

	/**
	 * 构造内存空间不足调用器，指定命令
	 * @param cmd 内存空间不足
	 */
	public SubMemoryMissingInvoker(MemoryMissing cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MemoryMissing getCommand() {
		return (MemoryMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 设置本地地址
		MemoryMissing cmd = getCommand();
		cmd.setSite(getLocal());

		// 上级管理节点
		Node hub = getHub();

		// 发送到TOP/HOME/BANK站点，不需要反馈
		boolean success = directTo(hub, cmd);

		Logger.debug(this, "launch", success, "direct to %s", hub);

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
