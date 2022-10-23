/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.command.*;

/**
 * 测试数据传输速率转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 8/26/2018
 * @since laxcus 1.0
 */
public class ShiftSwarm extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSwarm(ShiftSwarm that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSwarm duplicate() {
		return new ShiftSwarm(this);
	}

	/**
	 * 构造测试数据传输速率转发命令，指定全部参数
	 * @param cmd 测试数据传输速率命令
	 * @param hook 测试数据传输速率命令钩子
	 */
	public ShiftSwarm(Swarm cmd, SwarmHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public Swarm getCommand() {
		return (Swarm) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public SwarmHook getHook() {
		return (SwarmHook) super.getHook();
	}
}