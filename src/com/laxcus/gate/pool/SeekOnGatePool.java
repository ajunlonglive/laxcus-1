/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 资源检索池
 * 
 * @author scott.liang
 * @version 1.0 7/13/2018
 * @since laxcus 1.0
 */
public abstract class SeekOnGatePool extends VirtualPool {

	/**
	 * 构造默认的资源检索池
	 */
	protected SeekOnGatePool() {
		super();
	}

	/**
	 * 根据签名找到ACCOUNT站点
	 * @param siger 用户签名
	 * @return 返回HASH站点或者空指针
	 */
	protected Node seekAccountSite(Siger siger) {
		if (siger == null) {
			return null;
		}

		//  去HASH站点，找到账号关联的ACCOUNT站点地址
		TakeAccountSite cmd = new TakeAccountSite(siger);
		TakeAccountSiteHook hook = new TakeAccountSiteHook();
		ShiftTakeAccountSite shift = new ShiftTakeAccountSite(cmd, hook);
		boolean success = getCommandPool().press(shift);
		if (!success) {
			Logger.error(this, "seekAccountSite", "cannot be press!");
			return null;
		}
		hook.await();
		
		// 返回ACCOUNT站点
		return hook.getRemote();
	}

	/**
	 * 根据签名查找一个账号
	 * @param siger 用户签名
	 * @return 返回账号实例，或者空指针
	 */
	public Account seekAccount(Siger siger) {
		if (siger == null) {
			return null;
		}
		
		// 1. 去HASH站点找ACCOUNT站点
		Node remote = seekAccountSite(siger);
		if (remote == null) {
			Logger.error(this, "seekAccount", "not found account site, by %s", siger);
			return null;
		}

		// 2. 去ACCOUNT站点加载账号
		TakeAccount cmd = new TakeAccount(siger);
		TakeAccountHook hook = new TakeAccountHook();
		ShiftTakeAccount shift = new ShiftTakeAccount(remote, cmd, hook);
		shift.setFast(true);
		
		boolean success = getCommandPool().press(shift);
		if (!success) {
			Logger.error(this, "seekAccount", "cannot be press!");
			return null;
		}
		hook.await();
		
		// 返回账号
		return hook.getAccount();
	}

	/**
	 * 去ACCOUNT站点找被授权人的资源引用
	 * @param conferrer 被授权人账号
	 * @return 返回被授权人的资源引用
	 */
	protected Refer seekConferrer(Siger conferrer) {
		if (conferrer == null) {
			return null;
		}
		
		// 去ACCOUNT站点下载被授权人的资源引用
		TakeRefer cmd = new TakeRefer(conferrer);
		TakeReferHook hook = new TakeReferHook();
		ShiftTakeRefer shift = new ShiftTakeRefer(cmd, hook);
		boolean success = getCommandPool().press(shift);
		if (!success) {
			Logger.error(this, "seekConferrer", "cannot be press!");
			return null;
		}
		hook.await();
		
		// 返回资源引用
		return hook.getRefer();
	}
}
