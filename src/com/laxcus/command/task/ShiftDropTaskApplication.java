/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;
import com.laxcus.site.*;

/**
 * 转发删除分布应用命令
 * 
 * @author scott.liang
 * @version 1.0 6/21/2019
 * @since laxcus 1.0
 */
public class ShiftDropTaskApplication extends ShiftCommand {

	private static final long serialVersionUID = 4128948020872714359L;

	/** 目标节点 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftDropTaskApplication(ShiftDropTaskApplication that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropTaskApplication duplicate() {
		return new ShiftDropTaskApplication(this);
	}
	
	/**
	 * 构造转发删除分布应用命令，指定参数
	 * @param cmd 删除分布应用命令
	 * @param hook 命令钩子
	 */
	public ShiftDropTaskApplication(Node remote, DropTaskApplication cmd, DropTaskApplicationHook hook) {
		super(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 设置目标节点
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回目标节点
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DropTaskApplication getCommand() {
		return (DropTaskApplication) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public DropTaskApplicationHook getHook() {
		return (DropTaskApplicationHook) super.getHook();
	}

}