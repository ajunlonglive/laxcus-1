/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;
import com.laxcus.site.*;

/**
 * 转发获得账号资源引用命令
 * 
 * @author scott.liang
 * @version 1.0 7/30/2017
 * @since laxcus 1.0
 */
public final class ShiftTakeRefer extends ShiftCommand {

	private static final long serialVersionUID = -3611604314699226307L;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 构造默认转发获得账号资源引用
	 */
	private ShiftTakeRefer() {
		super();
	}

	/**
	 * 构造转发获得账号资源引用命令，指定发送命令和钩子
	 * @param cmd 发送的命令
	 * @param hook 命令钩子
	 */
	public ShiftTakeRefer(TakeRefer cmd, TakeReferHook hook) {
		this();
		setCommand(cmd);
		setHook(hook);
	}
	
	/**
	 * 构造转发获得账号资源引用命令，指定发送命令和钩子
	 * @param cmd 发送的命令
	 * @param hook 命令钩子
	 */
	public ShiftTakeRefer(Node remote, TakeRefer cmd, TakeReferHook hook) {
		this(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeRefer(ShiftTakeRefer that){
		super(that);
		remote = that.remote;
	}
	
	/**
	 * 设置ACCOUNT站点地址，允许空值
	 * @param e ACCOUNT站点地址
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return ACCOUNT站点地址
	 */
	public Node getRemote() {
		return remote;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeRefer duplicate() {
		return new ShiftTakeRefer(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeRefer getCommand() {
		return (TakeRefer) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeReferHook getHook() {
		return (TakeReferHook) super.getHook();
	}

}