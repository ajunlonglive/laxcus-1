/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.command.*;

/**
 * 获取系统管理员账号转发命令
 * 
 * @author scott.liang
 * @version 1.0 7/28/2018
 * @since laxcus 1.0
 */
public class ShiftTakeAdministrator extends ShiftCommand {

	private static final long serialVersionUID = -3751406179553155627L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeAdministrator(ShiftTakeAdministrator that){
		super(that);
	}

	/**
	 * 构造获取系统管理员账号转发命令，指定参数
	 * @param cmd 申请系统管理员命令
	 * @param hook 命令钩子
	 */
	public ShiftTakeAdministrator(TakeAdministrator cmd, TakeAdministratorHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeAdministrator getCommand() {
		return (TakeAdministrator) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeAdministratorHook getHook() {
		return (TakeAdministratorHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeAdministrator duplicate() {
		return new ShiftTakeAdministrator(this);
	}
}