/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;

/**
 * 获得BANK子站点转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class ShiftTakeBankSubSites extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeBankSubSites(ShiftTakeBankSubSites that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeBankSubSites duplicate() {
		return new ShiftTakeBankSubSites(this);
	}

	/**
	 * 构造获得BANK子站点转发命令，指定全部参数
	 * @param cmd 获得BANK子站点命令
	 * @param hook 获得BANK子站点命令钩子
	 */
	public ShiftTakeBankSubSites(TakeBankSubSites cmd, TakeBankSubSitesHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeBankSubSites getCommand() {
		return (TakeBankSubSites) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeBankSubSitesHook getHook() {
		return (TakeBankSubSitesHook) super.getHook();
	}
}