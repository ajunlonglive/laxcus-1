/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.command.*;

/**
 * 判断数据表存在转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 8/18/2015
 * @since laxcus 1.0
 */
public class ShiftAssertTable extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftAssertTable(ShiftAssertTable that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAssertTable duplicate() {
		return new ShiftAssertTable(this);
	}

	/**
	 * 构造判断数据表转发命令，指定全部参数
	 * @param cmd 判断数据表存在
	 * @param hook 判断数据表存在钩子
	 */
	public ShiftAssertTable(AssertTable cmd, AssertTableHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public AssertTable getCommand() {
		return (AssertTable) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public AssertTableHook getHook() {
		return (AssertTableHook) super.getHook();
	}
}