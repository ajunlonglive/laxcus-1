/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发获得授权人的CALL站点
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class ShiftTakeAuthorizerCall extends ShiftCommand {

	private static final long serialVersionUID = 8907984663285165476L;
	
	/** 指定连接的GATE节点地址 **/
	private Node hub;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeAuthorizerCall(ShiftTakeAuthorizerCall that){
		super(that);
		hub = that.hub;
	}

	/**
	 * 构造转发获得授权人的CALL站点，指定参数
	 * @param cmd 获得授权人的CALL站点
	 * @param hook 命令钩子
	 */
	public ShiftTakeAuthorizerCall(TakeAuthorizerCall cmd, TakeAuthorizerCallHook hook) {
		super(cmd, hook);
	}

	/**
	 * 指定GATE节点地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);
		// 必须是GATE节点
		if (!e.isGate()) {
			throw new IllegalValueException("illegal site type! %s", e);
		}
		hub = e;
	}

	/**
	 * 返回GATE节点地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeAuthorizerCall getCommand() {
		return (TakeAuthorizerCall) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeAuthorizerCallHook getHook() {
		return (TakeAuthorizerCallHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeAuthorizerCall duplicate() {
		return new ShiftTakeAuthorizerCall(this);
	}
}
