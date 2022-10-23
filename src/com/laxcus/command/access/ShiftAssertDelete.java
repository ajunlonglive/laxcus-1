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
 * 转发“ASSERT DELETE”命令
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public final class ShiftAssertDelete extends ShiftCommand {

	private static final long serialVersionUID = 817064259450678740L;

	/** 目标站点监听地址 **/
	private Cabin hub;
	
	/**
	 * 生成转发命令副本
	 * @param that
	 */
	private ShiftAssertDelete(ShiftAssertDelete that) {
		super(that);
		hub = that.hub;
	}

	/**
	 * 构造DELETE转发命令，指定全部参数
	 * @param hub DELETE调用器监听地址（此时DATA站点上的DELETE调用器处于等待回复DELETE ASSERT命令中）
	 * @param cmd DELETE ASSERT命令
	 * @param hook 命令钩子
	 */
	public ShiftAssertDelete(Cabin hub, AssertDelete cmd, DeleteHook hook) {
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
	public AssertDelete getCommand() {
		return (AssertDelete) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public DeleteHook getHook() {
		return (DeleteHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAssertDelete duplicate() {
		return new ShiftAssertDelete(this);
	}

}