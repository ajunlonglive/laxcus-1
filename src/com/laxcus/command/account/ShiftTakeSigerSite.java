/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import com.laxcus.command.*;

/**
 * 获得签名的ACCOUNT站点的转发命令
 * 
 * @author scott.liang
 * @version 1.1 7/28/2018
 * @since laxcus 1.0
 */
public final class ShiftTakeSigerSite extends ShiftCommand {

	private static final long serialVersionUID = -435432648793673106L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 获得签名的ACCOUNT站点的转发命令
	 */
	private ShiftTakeSigerSite(ShiftTakeSigerSite that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeSigerSite duplicate() {
		return new ShiftTakeSigerSite(this);
	}

	/**
	 * 构造获得签名的ACCOUNT站点的转发命令
	 * @param cmd 获得签名的ACCOUNT站点的转发命令
	 * @param hook 命令钩子
	 */
	public ShiftTakeSigerSite(TakeSigerSite cmd, TakeSigerSiteHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeSigerSite getCommand() {
		return (TakeSigerSite) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeSigerSiteHook getHook() {
		return (TakeSigerSiteHook) super.getHook();
	}

}
