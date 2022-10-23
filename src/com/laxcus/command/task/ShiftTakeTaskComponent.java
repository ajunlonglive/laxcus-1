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
 * 转发获得分布任务组件包命令。
 * 
 * @author scott.liang
 * @version 1.1 05/06/2015
 * @since laxcus 1.0
 */
public class ShiftTakeTaskComponent extends ShiftCommand {

	private static final long serialVersionUID = -7758917590519846968L;

	/** 目标站点 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeTaskComponent(ShiftTakeTaskComponent that){
		super(that);
		remote = that.remote;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeTaskComponent duplicate() {
		return new ShiftTakeTaskComponent(this);
	}

	/**
	 * 构造转发获得分布任务组件包命令，指定命令
	 * @param cmd TakeTaskComponent实例
	 * @param remote 目标站点
	 */
	public ShiftTakeTaskComponent(TakeTaskComponent cmd, Node remote) {
		super();
		setCommand(cmd);
		setRemote(remote);
	}

	/**
	 * 设置目标地址
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回目标地址
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/**
	 * 返回命令
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeTaskComponent getCommand() {
		return (TakeTaskComponent) super.getCommand();
	}

}