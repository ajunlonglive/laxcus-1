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
 * 转发撤销GATE站点到ENTRANCE站点
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftDropGateSite extends ShiftCommand {

	private static final long serialVersionUID = 7117340191966702125L;

	/**
	 * 构造默认的撤销GATE站点到ENTRANCE站点
	 */
	public ShiftDropGateSite() {
		super();
	}

	/**
	 * 构造撤销GATE站点到ENTRANCE站点，指定命令
	 * @param cmd
	 */
	public ShiftDropGateSite(DropGateSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DropGateSite getCommand() {
		return (DropGateSite) super.getCommand();
	}

	/**
	 * 生成实例副本
	 * @param that
	 */
	public ShiftDropGateSite(ShiftDropGateSite that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropGateSite duplicate() {
		return new ShiftDropGateSite(this);
	}

}
