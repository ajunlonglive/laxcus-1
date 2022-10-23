/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task.talk;

import com.laxcus.command.*;
import com.laxcus.site.*;

/**
 * 转发分布任务组件交互操作
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class ShiftTalkAsk extends ShiftCommand {

	private static final long serialVersionUID = 2817585910229495659L;

	/** 远程目标地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTalkAsk(ShiftTalkAsk that){
		super(that);
		remote = that.remote;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTalkAsk duplicate() {
		return new ShiftTalkAsk(this);
	}
	
	/**
	 * 构造转发分布任务组件交互操作命令，指定所需参数
	 * @param hub 目标站点 
	 * @param cmd 命令
	 * @param hook 命令钩子
	 */
	public ShiftTalkAsk(Node hub, TalkAsk cmd, TalkAskHook hook) {
		super(cmd, hook);
		setRemote(hub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TalkAsk getCommand() {
		return (TalkAsk) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TalkAskHook getHook() {
		return (TalkAskHook) super.getHook();
	}

	/**
	 * 设置远程目标站点
	 * @param e 远程目标站点
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回远程目标站点
	 * @return 远程目标站点
	 */
	public Node getRemote() {
		return remote;
	}

}