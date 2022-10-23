/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.util.tip.*;

/**
 * 屏蔽错误通知调用器
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class WatchDisableFaultInvoker extends WatchInvoker {

	/**
	 * 构造屏蔽错误通知，指定命令
	 * @param cmd 屏蔽错误通知
	 */
	public WatchDisableFaultInvoker(DisableFault cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DisableFault getCommand() {
		return (DisableFault) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DisableFault cmd = getCommand();
		NoticeMuffler muffler = getFaultMuffler();
		// 全部屏蔽，或者指定屏蔽某些节点
		if (cmd.isAll()) {
			muffler.setAll(true);
			muffler.clear();
		} else {
			muffler.addAll(cmd.list());
		}
		messageX(MessageTip.COMMAND_PROCESSED);
		// 退出
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}