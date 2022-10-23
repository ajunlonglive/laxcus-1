/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发删除账号命令
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class ShiftDropUser extends ShiftCommand {

	private static final long serialVersionUID = 7705726822024483741L;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftDropUser(ShiftDropUser that){
		super(that);
		remote = that.remote;
	}

	/**
	 * 构造转发删除账号命令，指定参数
	 * @param remote ACCOUNT站点地址
	 * @param cmd 删除账号命令
	 * @param hook 命令钩子
	 */
	public ShiftDropUser(Node remote, DropUser cmd, DropUserHook hook) {
		super(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 设置ACCOUNT站点地址
	 * @param e ACCOUNT站点地址
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return ACCOUNT站点地址
	 */
	public Node getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public DropUserHook getHook() {
		return (DropUserHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropUser duplicate() {
		return new ShiftDropUser(this);
	}
}