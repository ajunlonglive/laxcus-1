/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.account.*;

/**
 * 刷新发布的任务组件调用器。<br>
 * 
 * BANK转发给TOP站点，由TOP站点再转发给HOME子域集群。
 * 
 * @author scott.liang
 * @version 1.0 3/13/2018
 * @since laxcus 1.0
 */
public class BankRefreshPublishInvoker extends BankInvoker {

	/**
	 * 构造刷新发布的任务组件调用器，指定命令
	 * @param cmd 刷新发布的任务组件
	 */
	public BankRefreshPublishInvoker(RefreshPublish cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshPublish getCommand() {
		return (RefreshPublish) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是监视器节点，忽略这个命令
		if (isMonitor()) {
			return useful(false);
		}
		
		RefreshPublish cmd = getCommand();
		boolean success = directToHub(cmd);
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