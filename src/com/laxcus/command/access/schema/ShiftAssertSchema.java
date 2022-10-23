/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.command.*;

/**
 * 判断数据库存在转发命令。<br><br>
 * 
 * 数据库名具有全网唯一性，需要以串行方式执行，才能避免冲突。<br>
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class ShiftAssertSchema extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftAssertSchema(ShiftAssertSchema that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAssertSchema duplicate() {
		return new ShiftAssertSchema(this);
	}

	/**
	 * 构造判断数据库转发命令，指定全部参数
	 * @param cmd 判断数据库存在
	 * @param hook 判断数据库存在钩子
	 */
	public ShiftAssertSchema(AssertSchema cmd, AssertSchemaHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public AssertSchema getCommand() {
		return (AssertSchema) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public AssertSchemaHook getHook() {
		return (AssertSchemaHook) super.getHook();
	}
}