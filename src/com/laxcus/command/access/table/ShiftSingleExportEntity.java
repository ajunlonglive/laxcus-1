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
 * 单个数据块导出转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftSingleExportEntity extends ShiftCommand {

	private static final long serialVersionUID = -4649008607501819684L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSingleExportEntity(ShiftSingleExportEntity that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSingleExportEntity duplicate() {
		return new ShiftSingleExportEntity(this);
	}

	/**
	 * 构造单个数据块导出转发命令，指定全部参数
	 * @param cmd 单个数据块导出
	 * @param hook 单个数据块导出钩子
	 */
	public ShiftSingleExportEntity(SingleExportEntity cmd, SingleExportEntityHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SingleExportEntity getCommand() {
		return (SingleExportEntity) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public SingleExportEntityHook getHook() {
		return (SingleExportEntityHook) super.getHook();
	}
}