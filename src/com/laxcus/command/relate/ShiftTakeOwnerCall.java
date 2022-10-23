/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import com.laxcus.command.*;

/**
 * 转发获得账号所有人的CALL站点
 * 
 * @author scott.liang
 * @version 1.0 5/31/2018
 * @since laxcus 1.0
 */
public class ShiftTakeOwnerCall extends ShiftCommand {

	private static final long serialVersionUID = 8907984663285165476L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeOwnerCall(ShiftTakeOwnerCall that){
		super(that);
	}

	/**
	 * 构造转发获得账号所有人的CALL站点，指定参数
	 * @param cmd 获得账号所有人的CALL站点
	 * @param hook 命令钩子
	 */
	public ShiftTakeOwnerCall(TakeOwnerCall cmd, TakeOwnerCallHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeOwnerCall getCommand() {
		return (TakeOwnerCall) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeOwnerCallHook getHook() {
		return (TakeOwnerCallHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeOwnerCall duplicate() {
		return new ShiftTakeOwnerCall(this);
	}
}
