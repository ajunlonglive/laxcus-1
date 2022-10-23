/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import com.laxcus.command.*;

/**
 * 绑定规则转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class ShiftAttachRule extends ShiftCommand {

	private static final long serialVersionUID = -3181649788370599989L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftAttachRule(ShiftAttachRule that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAttachRule duplicate() {
		return new ShiftAttachRule(this);
	}

	/**
	 * 构造绑定规则转发命令，指定全部参数
	 * @param cmd 绑定规则存在
	 * @param hook 绑定规则存在钩子
	 */
	public ShiftAttachRule(AttachRule cmd, AttachRuleHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public AttachRule getCommand() {
		return (AttachRule) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public AttachRuleHook getHook() {
		return (AttachRuleHook) super.getHook();
	}
}