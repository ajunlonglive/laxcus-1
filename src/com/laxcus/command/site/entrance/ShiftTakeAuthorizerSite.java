/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import com.laxcus.command.*;

/**
 * 转发获得授权人账号注册地址
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class ShiftTakeAuthorizerSite extends ShiftCommand {

	private static final long serialVersionUID = 8907984663285165476L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeAuthorizerSite(ShiftTakeAuthorizerSite that){
		super(that);
	}

	/**
	 * 构造转发获得授权人账号注册地址，指定参数
	 * @param cmd 获得授权人账号注册地址
	 * @param hook 命令钩子
	 */
	public ShiftTakeAuthorizerSite(TakeAuthorizerSite cmd, TakeAuthorizerSiteHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeAuthorizerSite getCommand() {
		return (TakeAuthorizerSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeAuthorizerSiteHook getHook() {
		return (TakeAuthorizerSiteHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeAuthorizerSite duplicate() {
		return new ShiftTakeAuthorizerSite(this);
	}
}
