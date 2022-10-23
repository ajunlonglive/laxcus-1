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
 * 转发推送GATE站点到ENTRANCE站点
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ShiftPushGateSite extends ShiftCommand {

	private static final long serialVersionUID = 8454992589392405987L;

	/**
	 * 构造默认的推送GATE站点到ENTRANCE站点
	 */
	public ShiftPushGateSite() {
		super();
	}

	/**
	 * 构造推送GATE站点到ENTRANCE站点，指定命令
	 * @param cmd
	 */
	public ShiftPushGateSite(PushGateSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public PushGateSite getCommand() {
		return (PushGateSite) super.getCommand();
	}

	/**
	 * 生成实例副本
	 * @param that
	 */
	public ShiftPushGateSite(ShiftPushGateSite that) {
		super(that);
	}

	//	/**
	//	 * @param cmd
	//	 * @param hook
	//	 */
	//	public ShiftPushGateSite(Command cmd, CommandHook hook) {
	//		super(cmd, hook);
	//		// TODO Auto-generated constructor stub
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPushGateSite duplicate() {
		return new ShiftPushGateSite(this);
	}

}
