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
 * 获得BANK子站点数目转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class ShiftTakeBankSubSiteCount extends ShiftCommand {

	private static final long serialVersionUID = 8418769772943470619L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeBankSubSiteCount(ShiftTakeBankSubSiteCount that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeBankSubSiteCount duplicate() {
		return new ShiftTakeBankSubSiteCount(this);
	}

	/**
	 * 构造获得BANK子站点数目转发命令，指定全部参数
	 * @param cmd 获得BANK子站点数目命令
	 * @param hook 获得BANK子站点数目命令钩子
	 */
	public ShiftTakeBankSubSiteCount(TakeBankSubSiteCount cmd, TakeBankSubSiteCountHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeBankSubSiteCount getCommand() {
		return (TakeBankSubSiteCount) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeBankSubSiteCountHook getHook() {
		return (TakeBankSubSiteCountHook) super.getHook();
	}
}