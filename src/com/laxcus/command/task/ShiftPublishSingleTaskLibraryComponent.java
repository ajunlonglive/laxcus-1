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
 * 转发发布分布任务组件动态链接库命令
 * 
 * @author scott.liang
 * @version 1.0 10/18/2019
 * @since laxcus 1.0
 */
public class ShiftPublishSingleTaskLibraryComponent extends ShiftCommand {

	private static final long serialVersionUID = 8825886175928413316L;

	/** 目标CALL节点 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftPublishSingleTaskLibraryComponent(ShiftPublishSingleTaskLibraryComponent that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPublishSingleTaskLibraryComponent duplicate() {
		return new ShiftPublishSingleTaskLibraryComponent(this);
	}
	
	/**
	 * 构造转发发布分布任务组件动态链接库命令，指定参数
	 * @param cmd 发布分布任务组件动态链接库命令
	 * @param hook 命令钩子
	 */
	public ShiftPublishSingleTaskLibraryComponent(Node remote, PublishSingleTaskLibraryComponent cmd, PublishSingleTaskLibraryComponentHook hook) {
		super(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 设置目标CALL节点
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回目标CALL节点
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
	public PublishSingleTaskLibraryComponent getCommand() {
		return (PublishSingleTaskLibraryComponent) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public PublishSingleTaskLibraryComponentHook getHook() {
		return (PublishSingleTaskLibraryComponentHook) super.getHook();
	}

}