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
 * 生效警告通知调用器
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class WatchEnableWarningInvoker extends WatchInvoker {

	/**
	 * 构造生效警告通知，指定命令
	 * @param cmd 生效警告通知
	 */
	public WatchEnableWarningInvoker(EnableWarning cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public EnableWarning getCommand() {
		return (EnableWarning) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		EnableWarning cmd = getCommand();
		NoticeMuffler muffler = getWarningMuffler();
		// 如果是全部，清除全部记录；否则删除指定的节点
		if (cmd.isAll()) {
			muffler.setAll(false);
			muffler.clear();
		} else {
			muffler.removeAll(cmd.list());
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
