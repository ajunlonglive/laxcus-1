/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.*;

/**
 * 转发关闭授权表命令
 * 
 * @author scott.liang
 * @version 1.0 7/21/2018
 * @since laxcus 1.0
 */
public final class ShiftCloseShareTable extends ShiftCommand {

	private static final long serialVersionUID = 5604418784018961022L;

	/**
	 * 构造默认转发关闭授权表
	 */
	private ShiftCloseShareTable() {
		super();
	}

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftCloseShareTable(ShiftCloseShareTable that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftCloseShareTable duplicate() {
		return new ShiftCloseShareTable(this);
	}

	/**
	 * 构造转发关闭授权表命令，指定命令和钩子
	 * @param cmd 命令
	 * @param hook 命令钩子
	 */
	public ShiftCloseShareTable(CloseShareTable cmd, CloseShareTableHook hook) {
		this();
		setCommand(cmd);
		setHook(hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public CloseShareTable getCommand() {
		return (CloseShareTable) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public CloseShareTableHook getHook() {
		return (CloseShareTableHook) super.getHook();
	}

}