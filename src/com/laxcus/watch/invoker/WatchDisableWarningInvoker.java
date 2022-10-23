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
 * 屏蔽警告通知调用器
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class WatchDisableWarningInvoker extends WatchInvoker {

	/**
	 * 构造屏蔽警告通知，指定命令
	 * @param cmd 屏蔽警告通知
	 */
	public WatchDisableWarningInvoker(DisableWarning cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DisableWarning getCommand() {
		return (DisableWarning) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DisableWarning cmd = getCommand();
		NoticeMuffler muffler = getWarningMuffler();
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