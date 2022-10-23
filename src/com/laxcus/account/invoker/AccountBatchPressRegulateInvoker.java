/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.command.rebuild.*;

/**
 * 批量处理数据优化调用器。<br>
 * 通过BANK中转，交给GATE站点处理。
 * 
 * @author scott.liang
 * @version 1.0 7/20/2018
 * @since laxcus 1.0
 */
public class AccountBatchPressRegulateInvoker extends AccountInvoker {

	/**
	 * 构造批量处理数据优化调用器，指定命令
	 * @param cmd 批量处理数据优化
	 */
	public AccountBatchPressRegulateInvoker(BatchPressRegulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BatchPressRegulate getCommand() {
		return (BatchPressRegulate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BatchPressRegulate cmd = getCommand();
		boolean success = directToHub(cmd);
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}