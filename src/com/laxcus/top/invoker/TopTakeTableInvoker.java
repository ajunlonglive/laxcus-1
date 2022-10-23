/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.top.pool.*;
import com.laxcus.site.*;

/**
 * 查表命令调用器。<br>
 * 命令来自HOME站点，根据数据表名查找对应的表。
 * 
 * @author scott.liang
 * @version 1.0 7/9/2018
 * @since laxcus 1.0
 */
public class TopTakeTableInvoker extends TopInvoker {

	/**
	 * 构造查表命令调用器，指定命令
	 * @param cmd 查表命令
	 */
	public TopTakeTableInvoker(TakeTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeTable getCommand() {
		return (TakeTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Node slave = BankOnTopPool.getInstance().getManagerSite();
		boolean success = (slave != null);
		if (success) {
			TakeTable cmd = getCommand();
			success = launchTo(slave, cmd);
		}
		if (!success) {
			failed();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 原样转发
		return reflect();
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		boolean success = false;
	//		if (isFromAid()) {
	//			success = doAid();
	//		} else if (isFromFront()) {
	//			success = doFront();
	//		} else if(isFromHome()) {
	//			success = doHome();
	//		}
	//		if (!success) {
	//			super.replyFault(Major.FAULTED, Minor.REFUSE);
	//		}
	//
	//		// 退出
	//		return useful(success);
	//	}

	//	/**
	//	 * 处理来自AID站点的命令
	//	 * @return
	//	 */
	//	private boolean doAid() {
	//		// 判断来自AID的参数正确
	//		boolean success = isLegalAid();
	//		// 检查有这个账号
	//		if (success) {
	//			TakeTable cmd = getCommand();
	//			Siger username = cmd.getIssuer();
	//			// 判断表存在
	//			success = DictPool.getInstance().hasTable(username, cmd.getSpace());
	//			
	////			Account account = DictPool.getInstance().findAccount(username);
	////			success = (account != null);
	////			// 判断账号下有这个表配置
	////			if(success) {
	////				success = account.hasTable(cmd.getSpace());
	////			}
	//		}
	//		if (success) {
	//			search();
	//		}
	//		return success;
	//	}
	//
	//	/**
	//	 * 处理来自FRONT站点的命令
	//	 * @return
	//	 */
	//	private boolean doFront() {
	//		boolean success = isLegalFront();
	//		if (success) {
	//			search();
	//		}
	//		return success;
	//	}
	//
	//	/**
	//	 * 处理来自HOME站点的命令
	//	 * @return
	//	 */
	//	private boolean doHome() {
	//		boolean success = isLegalHome();
	//		if (success) {
	//			search();
	//		}
	//		return success;
	//	}
	//	
	//	/**
	//	 * 返回表配置
	//	 */
	//	private void search() {
	//		TakeTable cmd = getCommand();
	//		Space space = cmd.getSpace();
	//
	//		Table table = DictPool.getInstance().findTable(space);
	//
	//		boolean success = (table != null);
	//
	//		if (success) {
	//			replyObject(table);
	//		} else {
	//			super.replyFault(Major.FAULTED, Minor.NOTFOUND);
	//		}
	//
	//		Logger.debug(this, "search", success, "search '%s'", space);
	//	}


}