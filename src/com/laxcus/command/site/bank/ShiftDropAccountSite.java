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
 * 转发撤销ACCOUNT站点到HASH站点
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftDropAccountSite extends ShiftCommand {

	private static final long serialVersionUID = 8454992589392405987L;

	/**
	 * 构造默认的撤销ACCOUNT站点到HASH站点
	 */
	public ShiftDropAccountSite() {
		super();
	}

	/**
	 * 构造撤销ACCOUNT站点到HASH站点，指定命令
	 * @param cmd
	 */
	public ShiftDropAccountSite(DropAccountSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DropAccountSite getCommand() {
		return (DropAccountSite) super.getCommand();
	}

	/**
	 * 生成实例副本
	 * @param that
	 */
	public ShiftDropAccountSite(ShiftDropAccountSite that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropAccountSite duplicate() {
		return new ShiftDropAccountSite(this);
	}

}