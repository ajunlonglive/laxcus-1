/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;
import com.laxcus.site.*;

/**
 * 获得坐标范围内账号转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class ShiftTakeAccountSiger extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;
	
	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeAccountSiger(ShiftTakeAccountSiger that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeAccountSiger duplicate() {
		return new ShiftTakeAccountSiger(this);
	}

	/**
	 * 构造获得坐标范围内账号转发命令，指定全部参数
	 * @param remote ACCOUNT站点地址
	 * @param cmd 获得坐标范围内账号命令
	 * @param hook 获得坐标范围内账号命令钩子
	 */
	public ShiftTakeAccountSiger(Node remote, TakeAccountSiger cmd, TakeAccountSigerHook hook) {
		super(cmd, hook);
		setRemote(remote);
	}

	/**
	 * 设置ACCOUNT站点地址
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeAccountSiger getCommand() {
		return (TakeAccountSiger) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeAccountSigerHook getHook() {
		return (TakeAccountSigerHook) super.getHook();
	}
}