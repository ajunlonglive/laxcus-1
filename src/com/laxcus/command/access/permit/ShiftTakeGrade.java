/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.command.*;

/**
 * 转发获取注册账号的操作权级
 * 
 * @author scott.liang
 * @version 1.0 5/31/2018
 * @since laxcus 1.0
 */
public class ShiftTakeGrade extends ShiftCommand {

	private static final long serialVersionUID = 8907984663285165476L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeGrade(ShiftTakeGrade that){
		super(that);
	}

	/**
	 * 构造转发获取注册账号的操作权级，指定参数
	 * @param cmd 获取注册账号的操作权级
	 * @param hook 命令钩子
	 */
	public ShiftTakeGrade(TakeGrade cmd, TakeGradeHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeGrade getCommand() {
		return (TakeGrade) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeGradeHook getHook() {
		return (TakeGradeHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeGrade duplicate() {
		return new ShiftTakeGrade(this);
	}
}