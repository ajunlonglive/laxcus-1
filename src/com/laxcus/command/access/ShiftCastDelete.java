/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.command.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发“CAST DELETE”命令
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public final class ShiftCastDelete extends ShiftCommand {

	private static final long serialVersionUID = -5083404369135264104L;

	/** 目标站点地址 **/
	private Node hub;
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftCastDelete(ShiftCastDelete that){
		super(that);
		hub = that.hub;
	}
	
	/**
	 * 构造DELETE转发命令，指定全部参数
	 * @param hub 目标站点地址（必须是DATA主站点）
	 * @param cmd CAST DELETE命令
	 * @param hook 命令钩子
	 */
	public ShiftCastDelete(Node hub, CastDelete cmd,  DeleteHook hook) {
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
	public CastDelete getCommand() {
		return (CastDelete) super.getCommand();
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
	public ShiftCastDelete duplicate() {
		return new ShiftCastDelete(this);
	}

}