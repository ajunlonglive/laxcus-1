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
 * 查表转发命令
 * 
 * @author scott.liang
 * @version 1.0 12/03/2011
 * @since laxcus 1.0
 */
public final class ShiftTakeTable extends ShiftCommand {
	
	private static final long serialVersionUID = -2204365637869614800L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeTable(ShiftTakeTable that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeTable duplicate() {
		return new ShiftTakeTable(this);
	}
	
	/**
	 * 构造查表转发命令，指定发送命令和命令钩子
	 * @param cmd TakeTable命令
	 * @param hook TakeTable命令钩子
	 */
	public ShiftTakeTable(TakeTable cmd, TakeTableHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeTable getCommand() {
		return (TakeTable) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeTableHook getHook() {
		return (TakeTableHook)super.getHook();
	}

}
