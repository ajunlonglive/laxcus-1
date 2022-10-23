/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发“INSERT”命令
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public final class ShiftInsert extends ShiftCommand {

	private static final long serialVersionUID = -164933733906217162L;

	/** 目标站点地址 **/
	private Node hub;
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftInsert(ShiftInsert that){
		super(that);
		hub = that.hub;
	}

	/**
	 * 构造INSERT转发命令，指定全部参数
	 * @param hub 目标站点地址（必须是DATA主站点）
	 * @param cmd INSERT命令
	 * @param hook 命令钩子
	 */
	public ShiftInsert(Node hub, Insert cmd, InsertHook hook) {
		super(cmd, hook);
		setHub(hub);
	}
	
	/**
	 * 设置服务器地址
	 * @param e Cabin实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回服务器地址
	 * @return Cabin实例
	 */
	public Node getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public Insert getCommand() {
		return (Insert) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public InsertHook getHook() {
		return (InsertHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftInsert duplicate() {
		return new ShiftInsert(this);
	}

}