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
 * 建立数据库转发命令。<br><br>
 * 
 * 数据库名具有全网唯一性，需要以串行方式执行，才能避免冲突。<br>
 * 
 * 这个命令在BANK站点的SerialCommandPool生成，交给BankCommandPool处理。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class ShiftCreateSchema extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftCreateSchema(ShiftCreateSchema that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftCreateSchema duplicate() {
		return new ShiftCreateSchema(this);
	}

	/**
	 * 构造建立数据库转发命令，指定全部参数
	 * @param cmd 建立数据库命令
	 * @param hook 建立数据库命令钩子
	 */
	public ShiftCreateSchema(CreateSchema cmd, CreateSchemaHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public CreateSchema getCommand() {
		return (CreateSchema) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public CreateSchemaHook getHook() {
		return (CreateSchemaHook) super.getHook();
	}
}