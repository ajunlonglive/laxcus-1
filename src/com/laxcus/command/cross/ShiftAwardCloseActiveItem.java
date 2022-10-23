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
 * 转发强制关联授权单元命令 <br>
 * 
 * 这个操作针对被授权单元，由授权人发起！
 * 
 * @author scott.liang
 * @version 1.0 5/27/2019
 * @since laxcus 1.0
 */
public final class ShiftAwardCloseActiveItem extends ShiftCommand {

	private static final long serialVersionUID = 5604418784018961022L;

	/**
	 * 构造默认转发强制关联授权单元命令
	 */
	private ShiftAwardCloseActiveItem() {
		super();
	}

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftAwardCloseActiveItem(ShiftAwardCloseActiveItem that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAwardCloseActiveItem duplicate() {
		return new ShiftAwardCloseActiveItem(this);
	}

	/**
	 * 构造转发强制关联授权单元命令，指定命令和钩子
	 * @param cmd 命令
	 * @param hook 命令钩子
	 */
	public ShiftAwardCloseActiveItem(AwardCloseActiveItem cmd, AwardCloseActiveItemHook hook) {
		this();
		setCommand(cmd);
		setHook(hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public AwardCloseActiveItem getCommand() {
		return (AwardCloseActiveItem) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public AwardCloseActiveItemHook getHook() {
		return (AwardCloseActiveItemHook) super.getHook();
	}

}