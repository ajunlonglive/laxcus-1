/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;

/**
 * 转发推送ACCOUNT站点到HASH站点
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftPushAccountSite extends ShiftCommand {

	private static final long serialVersionUID = 8454992589392405987L;

	/**
	 * 构造默认的推送ACCOUNT站点到HASH站点
	 */
	public ShiftPushAccountSite() {
		super();
	}

	/**
	 * 构造推送ACCOUNT站点到HASH站点，指定命令
	 * @param cmd
	 */
	public ShiftPushAccountSite(PushAccountSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public PushAccountSite getCommand() {
		return (PushAccountSite) super.getCommand();
	}

	/**
	 * 生成实例副本
	 * @param that
	 */
	public ShiftPushAccountSite(ShiftPushAccountSite that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPushAccountSite duplicate() {
		return new ShiftPushAccountSite(this);
	}

}
