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
 * 转发撤销HASH站点到GATE站点
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftDropHashSite extends ShiftCommand {

	private static final long serialVersionUID = 1967068303363893936L;

	/**
	 * 构造默认的撤销HASH站点到GATE站点
	 */
	public ShiftDropHashSite() {
		super();
	}

	/**
	 * 构造撤销HASH站点到GATE站点，指定命令
	 * @param cmd
	 */
	public ShiftDropHashSite(DropHashSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DropHashSite getCommand() {
		return (DropHashSite) super.getCommand();
	}

	/**
	 * 生成实例副本
	 * @param that
	 */
	public ShiftDropHashSite(ShiftDropHashSite that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropHashSite duplicate() {
		return new ShiftDropHashSite(this);
	}

}
