/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.command.*;

/**
 * 判断用户账号存在转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 4/12/2010
 * @since laxcus 1.0
 */
public class ShiftAssertUser extends ShiftCommand {

	private static final long serialVersionUID = -1026897170677939271L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftAssertUser(ShiftAssertUser that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAssertUser duplicate() {
		return new ShiftAssertUser(this);
	}

	/**
	 * 构造判断用户账号转发命令，指定全部参数
	 * @param cmd 判断用户账号存在
	 * @param hook 判断用户账号存在钩子
	 */
	public ShiftAssertUser(AssertUser cmd, AssertUserHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public AssertUser getCommand() {
		return (AssertUser) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public AssertUserHook getHook() {
		return (AssertUserHook) super.getHook();
	}
}