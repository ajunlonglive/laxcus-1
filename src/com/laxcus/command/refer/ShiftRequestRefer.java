/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;

/**
 * 转发请求资源引用命令
 * 
 * @author scott.liang
 * @version 1.1 3/23/2013
 * @since laxcus 1.0
 */
public final class ShiftRequestRefer extends ShiftCommand {

	private static final long serialVersionUID = -3611604314699226307L;

	/**
	 * 构造默认转发请求资源引用命令
	 */
	private ShiftRequestRefer() {
		super();
	}
	
	/**
	 * 生成转发请求资源引用命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftRequestRefer(ShiftRequestRefer that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftRequestRefer duplicate() {
		return new ShiftRequestRefer(this);
	}

	/**
	 * 构造命令，指定发送命令和钩子
	 * @param cmd 发送的命令
	 * @param hook 命令钩子
	 */
	public ShiftRequestRefer(RequestRefer cmd, RequestReferHook hook) {
		this();
		setCommand(cmd);
		setHook(hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public RequestRefer getCommand() {
		return (RequestRefer) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public RequestReferHook getHook() {
		return (RequestReferHook) super.getHook();
	}

}