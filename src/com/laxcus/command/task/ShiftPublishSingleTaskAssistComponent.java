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
 * 转发发布分布任务组件应用附件命令
 * 
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class ShiftPublishSingleTaskAssistComponent extends ShiftCommand {

	private static final long serialVersionUID = 8825886175928413316L;

	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftPublishSingleTaskAssistComponent(ShiftPublishSingleTaskAssistComponent that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPublishSingleTaskAssistComponent duplicate() {
		return new ShiftPublishSingleTaskAssistComponent(this);
	}
	
	/**
	 * 构造转发发布分布任务组件应用附件命令，指定参数
	 * @param cmd 发布分布任务组件应用附件命令
	 * @param hook 命令钩子
	 */
	public ShiftPublishSingleTaskAssistComponent(Node remote, PublishSingleTaskAssistComponent cmd, PublishSingleTaskAssistComponentHook hook) {
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
	public PublishSingleTaskAssistComponent getCommand() {
		return (PublishSingleTaskAssistComponent) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public PublishSingleTaskAssistComponentHook getHook() {
		return (PublishSingleTaskAssistComponentHook) super.getHook();
	}

}