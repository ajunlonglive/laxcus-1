/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.bank.*;
import com.laxcus.bank.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * BANK站点异步调用器。<br>
 * 
 * 在BANK站点上运行的异步调用器都从这里派生。
 * 
 * @author scott.liang
 * @version 1.1 6/25/2018
 * @since laxcus 1.0
 */
public abstract class BankInvoker extends EchoInvoker {

	/**
	 * 构造BANK站点异步调用器，指定异步命令
	 * @param cmd 异步命令
	 */
	protected BankInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public BankLauncher getLauncher() {
		return (BankLauncher) super.getLauncher();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommandPool()
	 */
	@Override
	public BankCommandPool getCommandPool() {
		return getLauncher().getCommandPool();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getInvokerPool()
	 */
	@Override
	public BankInvokerPool getInvokerPool() {
		return getLauncher().getInvokerPool();
	}

	/**
	 * 判断是管理站点
	 * @return - 返回真或者假
	 */
	public boolean isManager() {
		BankLauncher launcher = getLauncher();
		return launcher.isManager();
	}

	/**
	 * 判断是监视站点
	 * @return 返回真或者假
	 */
	public boolean isMonitor() {
		BankLauncher launcher = getLauncher();
		return launcher.isMonitor();
	}
	
	/**
	 * 向请求端发送一个拒绝通知
	 * @return 发送成功返回真，否则假
	 */
	protected boolean refuse() {
		return replyFault(Major.FAULTED, Minor.REFUSE);
	}

	/**
	 * 向请求端发送一个操作失败通知
	 * @return 发送成功返回真，否则假
	 */
	protected boolean failed() {
		return replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
	}

	/**
	 * 通过账号签名定位HASH站点
	 * @param siger 用户签名
	 * @return 返回HASH站点
	 */
	protected Node locate(Siger siger){
		return HashOnBankPool.getInstance().locate(siger);
	}
	
	/**
	 * BANK向全部HASH/GATE站点广播更新一个更新的账号，包括账号签名和ACCOUNT站点地址
	 * @param accountSite ACCOUNT站点地址
	 * @param siger 账号签名
	 * @return 投递成功返回真，否则假
	 */
	protected boolean multicast(Node accountSite, Siger siger) {
		// 收集HASH/GATE站点
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(HashOnBankPool.getInstance().detail());
		array.addAll(GateOnBankPool.getInstance().detail());
		// 以容错模式群发给全部HASH/GATE站点，不需要回应
		RefreshAccount cmd = new RefreshAccount(accountSite, siger);
		int count = directTo(array, cmd, false);
		return (count > 0);
	}
	
	/**
	 * BANK向全部HASH/GATE站点广播一组需要更新的账号，包括账号签名和ACCOUNT站点地址
	 * @param seats ACCOUNT用户基点
	 * @return 返回发送成功的数目
	 */
	protected int multicast(Collection<Seat> seats) {
		// 收集HASH/GATE站点
		ArrayList<Node> sites = new ArrayList<Node>();
		sites.addAll(HashOnBankPool.getInstance().detail());
		sites.addAll(GateOnBankPool.getInstance().detail());

		// 命令单元
		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		for (Seat seat : seats) {
			// ACCOUT站点，用户签名
			RefreshAccount cmd = new RefreshAccount(seat.getSite(), seat.getSiger());
			for (Node remote : sites) {
				CommandItem item = new CommandItem(remote, cmd);
				items.add(item);
			}
		}
		// 投递到全部HASH/GATE
		return directTo(items, false);
	}
	
	/**
	 * BANK向全部HASH/GATE站点广播更新一个更新的账号，包括账号签名和ACCOUNT站点地址
	 * @param seat 账号位置
	 * @return 命令发送到BANK站点返回真，否则假
	 */
	protected boolean multicast(Seat seat) {
		return multicast(seat.getSite(), seat.getSiger());
	}

	/**
	 * 向TOP站点发送更新资源引用
	 * @param seat ACCOUNT站点和签名
	 */
	protected void refreshRefer(Seat seat) {
		RefreshRefer cmd = new RefreshRefer(seat);
		getCommandPool().admit(cmd);
	}
}