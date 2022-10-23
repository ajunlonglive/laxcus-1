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
 * 单个数据文件上传转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftSingleImportEntity extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSingleImportEntity(ShiftSingleImportEntity that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSingleImportEntity duplicate() {
		return new ShiftSingleImportEntity(this);
	}

	/**
	 * 构造单个数据文件上传转发命令，指定全部参数
	 * @param cmd 单个数据文件上传
	 * @param hook 单个数据文件上传钩子
	 */
	public ShiftSingleImportEntity(SingleImportEntity cmd, SingleImportEntityHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SingleImportEntity getCommand() {
		return (SingleImportEntity) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public SingleImportEntityHook getHook() {
		return (SingleImportEntityHook) super.getHook();
	}
}