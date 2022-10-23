/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.command.*;
import com.laxcus.site.*;

/**
 * 转发申请用户账号命令
 * 
 * @author scott.liang
 * @version 1.1 4/9/2015
 * @since laxcus 1.0
 */
public class ShiftTakeAccount extends ShiftCommand {

	private static final long serialVersionUID = -9089779448766765760L;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeAccount(ShiftTakeAccount that){
		super(that);
		remote = that.remote;
	}

	/**
	 * 构造转发申请用户账号命令，指定参数
	 * @param cmd 申请用户账号命令
	 * @param hook 命令钩子
	 */
	public ShiftTakeAccount(TakeAccount cmd, TakeAccountHook hook) {
		super(cmd, hook);
	}

	/**
	 * 构造转发申请用户账号命令，指定参数
	 * @param remote ACCOUNT站点地址
	 * @param cmd 申请用户账号命令
	 * @param hook 命令钩子
	 */
	public ShiftTakeAccount(Node remote, TakeAccount cmd, TakeAccountHook hook) {
		super(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 设置ACCOUNT站点地址
	 * @param e ACCOUNT站点地址
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return ACCOUNT站点地址
	 */
	public Node getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeAccount getCommand() {
		return (TakeAccount) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeAccountHook getHook() {
		return (TakeAccountHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeAccount duplicate() {
		return new ShiftTakeAccount(this);
	}
}