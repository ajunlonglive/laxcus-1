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
 * 转发查询授权单元命令
 * 
 * @author scott.liang
 * @version 1.0 8/15/2017
 * @since laxcus 1.0
 */
public final class ShiftSeekActiveItem extends ShiftCommand {

	private static final long serialVersionUID = 6343617309787578845L;

	/**
	 * 构造默认转发查询授权单元
	 */
	private ShiftSeekActiveItem() {
		super();
	}

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSeekActiveItem(ShiftSeekActiveItem that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSeekActiveItem duplicate() {
		return new ShiftSeekActiveItem(this);
	}
	
	/**
	 * 构造转发查询授权单元命令，指定发送命令和钩子
	 * @param cmd 发送的命令
	 * @param hook 命令钩子
	 */
	public ShiftSeekActiveItem(SeekActiveItem cmd, SeekActiveItemHook hook) {
		this();
		setCommand(cmd);
		setHook(hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SeekActiveItem getCommand() {
		return (SeekActiveItem) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public SeekActiveItemHook getHook() {
		return (SeekActiveItemHook) super.getHook();
	}

}