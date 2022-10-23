/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

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
public class ShiftDropTable extends ShiftCommand {

	private static final long serialVersionUID = 5026768190385129561L;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftDropTable(ShiftDropTable that){
		super(that);
		remote = that.remote;
	}

	/**
	 * 构造转发删除账号命令，指定参数
	 * @param cmd 删除账号命令
	 * @param hook 命令钩子
	 */
	public ShiftDropTable(DropTable cmd, DropTableHook hook) {
		super(cmd, hook);
	}

	/**
	 * 构造转发删除账号命令，指定参数
	 * @param remote ACCOUNT站点地址
	 * @param cmd 删除账号命令
	 * @param hook 命令钩子
	 */
	public ShiftDropTable(Node remote, DropTable cmd, DropTableHook hook) {
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
	public DropTable getCommand() {
		return (DropTable) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public DropTableHook getHook() {
		return (DropTableHook) super.getHook();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropTable duplicate() {
		return new ShiftDropTable(this);
	}
}