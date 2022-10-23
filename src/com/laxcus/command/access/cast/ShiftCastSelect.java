/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发“CastSelect”命令
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public final class ShiftCastSelect extends ShiftCommand {

	private static final long serialVersionUID = -4648266633806626288L;

	/** DATA站点地址 **/
	private Node hub;
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftCastSelect(ShiftCastSelect that){
		super(that);
		hub = that.hub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftCastSelect duplicate() {
		return new ShiftCastSelect(this);
	}

	/**
	 * 构造“CastSelect”转发命令，指定全部参数
	 * @param hub DATA站点地址
	 * @param cmd SELECT投递命令
	 * @param hook 命令钩子
	 */
	public ShiftCastSelect(Node hub, CastSelect cmd, CastSelectHook hook) {
		super(cmd, hook);
		setHub(hub);
	}

	/**
	 * 设置DATA站点地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回DATA站点地址
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
	public CastSelect getCommand() {
		return (CastSelect) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public CastSelectHook getHook() {
		return (CastSelectHook) super.getHook();
	}

}