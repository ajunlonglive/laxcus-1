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
 * 转发发布分布任务组件命令
 * 
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class ShiftPublishSingleTaskComponent extends ShiftCommand {

	private static final long serialVersionUID = -7429875528050748082L;

	/** 目标ACCOUNT节点 **/
	private Node remote;
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftPublishSingleTaskComponent(ShiftPublishSingleTaskComponent that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPublishSingleTaskComponent duplicate() {
		return new ShiftPublishSingleTaskComponent(this);
	}
	
	/**
	 * 构造转发发布分布任务组件命令，指定参数
	 * @param cmd 发布分布任务组件命令
	 * @param hook 命令钩子
	 */
	public ShiftPublishSingleTaskComponent(PublishSingleTaskComponent cmd, PublishSingleTaskComponentHook hook) {
		super(cmd, hook);
	}

	/**
	 * 构造转发发布分布任务组件命令，指定参数
	 * @param remote ACCOUNT站点地址
	 * @param cmd 发布分布任务组件命令
	 * @param hook 命令钩子
	 */
	public ShiftPublishSingleTaskComponent(Node remote, PublishSingleTaskComponent cmd, PublishSingleTaskComponentHook hook) {
		super(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 设置目标ACCOUNT节点
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回目标ACCOUNT节点
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
	public PublishSingleTaskComponent getCommand() {
		return (PublishSingleTaskComponent) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public PublishSingleTaskComponentHook getHook() {
		return (PublishSingleTaskComponentHook) super.getHook();
	}

}