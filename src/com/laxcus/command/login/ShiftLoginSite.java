/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.login;

import com.laxcus.command.*;

/**
 * 转发注册站点命令
 * 
 * @author scott.liang
 * @version 1.0 12/4/2017
 * @since laxcus 1.0
 */
public class ShiftLoginSite extends ShiftCommand {

	private static final long serialVersionUID = 8304190884400165325L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftLoginSite(ShiftLoginSite that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftLoginSite duplicate() {
		return new ShiftLoginSite(this);
	}
	
	/**
	 * 构造转发注册站点命令，指定参数
	 * @param cmd 注册站点命令
	 * @param hook 命令钩子
	 */
	public ShiftLoginSite(LoginSite cmd, LoginSiteHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public LoginSite getCommand() {
		return (LoginSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public LoginSiteHook getHook() {
		return (LoginSiteHook) super.getHook();
	}
}
