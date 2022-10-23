/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import com.laxcus.command.*;

/**
 * 转发查询FRONT用户的登录站点命令。
 * 
 * @author scott.liang
 * @version 1.0 3/03/2012
 * @since laxcus 1.0
 */
public class ShiftFindFrontLoginSite extends ShiftCommand {

	private static final long serialVersionUID = 4327763743914178182L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFindFrontLoginSite(ShiftFindFrontLoginSite that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFindFrontLoginSite duplicate() {
		return new ShiftFindFrontLoginSite(this);
	}
	
	/**
	 * 构造转发查询FRONT用户的登录站点命令，指定发送命令和命令钩子
	 * @param cmd FindFrontLoginSite命令
	 * @param hook FindFrontLoginSite命令钩子
	 */
	public ShiftFindFrontLoginSite(FindFrontLoginSite cmd, FindFrontLoginSiteHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public FindFrontLoginSite getCommand() {
		return (FindFrontLoginSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public FindFrontLoginSiteHook getHook() {
		return (FindFrontLoginSiteHook) super.getHook();
	}

}