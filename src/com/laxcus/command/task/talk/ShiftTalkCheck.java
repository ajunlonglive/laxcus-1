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
 * 转发分布任务组件状态查询
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class ShiftTalkCheck extends ShiftCommand {

	private static final long serialVersionUID = 2817585910229495659L;

	/** 远程目标地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTalkCheck(ShiftTalkCheck that){
		super(that);
		remote = that.remote;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTalkCheck duplicate() {
		return new ShiftTalkCheck(this);
	}
	
	/**
	 * 构造代理上传分布任务组件命令，指定所需参数
	 * @param hub 远程目标站点
	 * @param cmd 上传命令
	 * @param hook 异步钩子
	 */
	public ShiftTalkCheck(Node hub, TalkCheck cmd, TalkCheckHook hook) {
		super(cmd, hook);
		setRemote(hub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TalkCheck getCommand() {
		return (TalkCheck) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TalkCheckHook getHook() {
		return (TalkCheckHook) super.getHook();
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