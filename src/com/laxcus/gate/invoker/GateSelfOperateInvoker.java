/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.refer.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 自操作调用器。<br><br>
 * 
 * 必须是用户自己操作的账号命令，为避免一个账号多用户竞用，每个用户在发出命令前，通过GATE上的公共锁限制。
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public abstract class GateSelfOperateInvoker extends GateSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/** ACCOUNT地址 **/
	private Node remote;
	
	/** 刷新本地操作 **/
	private boolean refresh;
	
	/** 工作完成广播，默认是假，由子类设置 */
	private boolean multicast;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		remote = null;
	}

	/**
	 * 建立自操作调用器，指定命令
	 * @param cmd 自有命令
	 */
	protected GateSelfOperateInvoker(Command cmd) {
		super(cmd);
		step = 1;
		setRefresh(false);
		setMulticast(false);
	}

	/**
	 * 设置刷新操作
	 * @param b 是或否
	 */
	protected void setRefresh(boolean b) {
		refresh = b;
	}

	/**
	 * 判断进行刷新操作
	 * @return 返回真或者假
	 */
	public boolean isRefresh() {
		return refresh;
	}
	
	/**
	 * 设置广播操作
	 * @param b 是或否
	 */
	protected void setMulticast(boolean b) {
		multicast = b;
	}

	/**
	 * 判断进行广播操作
	 * @return 返回真或者假
	 */
	public boolean isMulticast() {
		return multicast;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 广播操作账号。交由命令管理器，由另一个调用器在本地执行
	 */
	private void refresh() {
		Siger siger = getIssuer();
		RefreshRefer cmd = new RefreshRefer(remote, siger);
		// 提交给BANK站点
		launchToHub(cmd);
	}
	
	/**
	 * 向经BANK转发，向TOP/HOME集群广播
	 */
	private void multicast() {
		Siger siger = getIssuer();
		RefreshAccount cmd = new RefreshAccount(remote, siger);
		getCommandPool().admit(cmd);
		// 提交给BANK站点
		launchToHub(cmd);
	}

	/**
	 * 按照顺序，执行分布调用操作
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		}
		step++;
		// 不成功，或者步骤完成，退出！
		if (!success || step > 3) {
			if (success) {
				if (isRefresh()) refresh();
				if (isMulticast()) multicast();
			} else {
				failed();
			}
			setQuit(true);
		}
		return success;
	}

	/**
	 * 第一步：去HASH站点查找ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger siger = getIssuer();
		return seekSite(siger);
	}

	/**
	 * 第二步：把命令投递给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		remote = replySite();
		boolean success = (remote != null);
		if (success) {
			Command cmd = getCommand();
			success = launchTo(remote, cmd);
		}
		return success;
	}

	/**
	 * 返回是终结果
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		return reflect();
	}


	//		ArrayList<RefreshAccount> array = new ArrayList<RefreshAccount>();
	//		List<Siger> all = getSigers();
	//		for(Siger siger : all) {
	//			RefreshAccount cmd = new RefreshAccount(remote, siger);
	//		}

	//	/** 反馈的账号地址 **/
	//	private ArrayList<Seat> seats = new ArrayList<Seat>();


	//	/**
	//	 * 第一步：根据签名，去HASH站点查找一组ACCOUNT站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doFirst() {
	//		List<Siger> sigers = getSigers();
	//		return seekSites(sigers, false);
	//	}
	//	
	//	private boolean doSecond() {
	//		List<Seat> all = replySites();
	//		
	//		if(all != null){
	//			seats.addAll(all);
	//		}
	//		
	//		boolean success = (seats.size()>0);
	//		if(success) {
	//			Command cmd = getCommand();
	//			
	//		}
	//		
	//	}

}