/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.account.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 账号站点的异步命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/24/2018
 * @since laxcus 1.0
 */
public abstract class AccountInvoker extends EchoInvoker implements SerialSchedule {

	/** 判断已经锁定资源，默认是假 **/
	private boolean attached;

	/**
	 * 构造账号站点调用器，指定命令
	 * @param cmd 分布命令
	 */
	protected AccountInvoker(Command cmd) {
		super(cmd);
		attached = false;
		// ACCOUNT节点，默认都要锁定资源，shackle必须是“真”。如果有异，修改此参数
		setShackle(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public AccountLauncher getLauncher() {
		return (AccountLauncher) super.getLauncher();
	}

	/**
	 * 返回TOP主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHubHost() {
		return getHub().getHost();
	}

	/**
	 * 向请求端发送一个拒绝通知
	 */
	protected boolean refuse() {
		return replyFault(Major.FAULTED, Minor.REFUSE); 
	}

	/**
	 * 向请求端发送一个操作失败通知
	 */
	protected boolean failed() {
		return replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
	}

	/**
	 * 读取用户账号
	 * @return 返回账号实例，或者空指针！
	 */
	protected Account readAccount() {
		Siger issuer = getIssuer();
		if (issuer != null) {
			return StaffOnAccountPool.getInstance().readAccount(issuer);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#attach()
	 */
	@Override
	public void attach() {
		attached = true;
		wakeup();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#isAttached()
	 */
	@Override
	public boolean isAttached() {
		return attached;
	}

	/**
	 * 锁定串行操作，直到被串行管理池受理。<br>
	 * 受理之后，SerialSchedulePool会锁定资源名称，在当前调用器释放前，不会接受同名调用器资源的申请。
	 * 
	 * @param siger 账号签名
	 */
	private void lock(Siger siger) {
		String resource = siger.toString();
		// 申请串行操作资源（同名资源每次只允许一个）
		attached = SerialSchedulePool.getInstance().admit(resource, this);
		// 如果没有获得资源，一直等待，直到前面的同名资源操作完成，“attached”变量被触发，改为“真”退出。
		while (!attached) {
			delay(500);
		}
	}

	/**
	 * 通知串行管理池，解锁当前资源。如果有其它资源，会被释放。
	 * @param siger 账号签名
	 */
	private boolean unlock(Siger siger) {
		String resource = siger.toString();
		return SerialSchedulePool.getInstance().release(resource, this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.SiteInvoker#shackle()
	 */
	@Override
	public void shackle() {
		// 不要求绑定资源时，忽略它！
		if (!isShackle()) {
			return;
		}
		
		// 拿到签名
		Siger issuer = getIssuer();
		if (issuer != null) {
			lock(issuer);
			Logger.debug(this, "shackle", "lock resource! %s#%d", issuer, getInvokerId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.SiteInvoker#unshackle()
	 */
	@Override
	public void unshackle() {
		// 不要求绑定资源时，忽略它！
		if (!isShackle()) {
			return;
		}
		
		// 拿到签名
		Siger issuer = getIssuer();
		if (issuer != null) {
			unlock(issuer);
			Logger.debug(this, "unshackle", "unlock resouce! %s#%d", issuer, getInvokerId());
		}
	}

}