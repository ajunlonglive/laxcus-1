/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.util.*;

/**
 * 转发“ASSERT INSERT”命令
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public final class ShiftAssertInsert extends ShiftCommand {

	private static final long serialVersionUID = -3375524325394733476L;

	/** 目标站点监听地址 **/
	private Cabin hub;
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftAssertInsert(ShiftAssertInsert that){
		super(that);
		hub = that.hub;
	}

	/**
	 * 构造INSERT转发命令，指定全部参数
	 * @param hub INSERT调用器监听地址（此时DATA站点上的INSERT调用器处于等待回复INSERT ASSERT命令中）
	 * @param cmd INSERT ASSERT命令
	 * @param hook 命令钩子
	 */
	public ShiftAssertInsert(Cabin hub, AssertInsert cmd, InsertHook hook) {
		super(cmd, hook);
		setHub(hub);
	}
	
	/**
	 * 设置目标站点监听地址
	 * @param e Cabin实例
	 */
	public void setHub(Cabin e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回目标站点监听地址
	 * @return Cabin实例
	 */
	public Cabin getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public AssertInsert getCommand() {
		return (AssertInsert) super.getCommand();
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
	public ShiftAssertInsert duplicate() {
		return new ShiftAssertInsert(this);
	}

}