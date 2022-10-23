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
 * 转发申请用户签名命令。
 * 
 * @author scott.liang
 * @version 1.0 3/03/2012
 * @since laxcus 1.0
 */
public class ShiftTakeSiger extends ShiftCommand {

	private static final long serialVersionUID = -7106449355742319892L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeSiger(ShiftTakeSiger that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeSiger duplicate() {
		return new ShiftTakeSiger(this);
	}
	
	/**
	 * 构造转发申请用户签名命令，指定发送命令和命令钩子
	 * @param cmd 申请用户签名命令
	 * @param hook 申请用户签名命令钩子
	 */
	public ShiftTakeSiger(TakeSiger cmd, TakeSigerHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeSiger getCommand() {
		return (TakeSiger) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeSigerHook getHook() {
		return (TakeSigerHook) super.getHook();
	}

}