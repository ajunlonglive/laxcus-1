/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import com.laxcus.command.*;

/**
 * 获取CALL站点成员转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class ShiftTakeCallItem extends ShiftCommand {

	private static final long serialVersionUID = -8663199355247088983L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeCallItem(ShiftTakeCallItem that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeCallItem duplicate() {
		return new ShiftTakeCallItem(this);
	}

	/**
	 * 构造获取CALL站点成员转发命令，指定全部参数
	 * @param cmd 获取CALL站点成员命令
	 * @param hook 获取CALL站点成员命令钩子
	 */
	public ShiftTakeCallItem(TakeCallItem cmd, TakeCallItemHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeCallItem getCommand() {
		return (TakeCallItem) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeCallItemHook getHook() {
		return (TakeCallItemHook) super.getHook();
	}
}