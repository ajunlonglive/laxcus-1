/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.command.*;
import com.laxcus.site.Node;
import com.laxcus.util.*;

/**
 * 转发设置映像数据命令。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public final class ShiftSetReflexData extends ShiftCommand {

	private static final long serialVersionUID = -5364979853139495881L;

	/** 从站点地址 **/
	private Node slave;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSetReflexData(ShiftSetReflexData that){
		super(that);
		slave = that.slave;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSetReflexData duplicate() {
		return new ShiftSetReflexData(this);
	}
	
	/**
	 * 构造转发设置映像数据命令，指定发送命令和命令钩子
	 * @param slave 从站点地址
	 * @param cmd 设置映像数据命令
	 * @param hook 命令钩子
	 */
	public ShiftSetReflexData(Node slave, SetReflexData cmd, SetReflexDataHook hook) {
		super(cmd, hook);
		setSite(slave);
	}

	/**
	 * 设置映像数据的从站点地址
	 * @param e 从站点地址
	 * @throws NullPointerException - 如果地址是空值
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		slave = e;
	}

	/**
	 * 返回映像数据的从站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return slave;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SetReflexData getCommand() {
		return (SetReflexData) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public SetReflexDataHook getHook() {
		return (SetReflexDataHook) super.getHook();
	}

}