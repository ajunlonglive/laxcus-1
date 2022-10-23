/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 查找ACCOUNT站点调用器。
 * 
 * 依据条件是账号签名。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2018
 * @since laxcus 1.0
 */
public abstract class GateSeekAccountSiteInvoker extends GateInvoker {

	/**
	 * 构造查找ACCOUNT站点调用器，指定命令
	 * @param cmd 分布命令
	 */
	protected GateSeekAccountSiteInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 根据账号签名，去HASH站点查找ACCOUNT站点
	 * @param siger 账号签名
	 * @return 成功返回真，否则假
	 */
	protected boolean seekSite(Siger siger) {
		// 查找HASH站点
		Node hash = locate(siger);
		boolean success = (hash != null);
		// 发送命令
		if (success) {
			TakeAccountSite cmd = new TakeAccountSite(siger);
			success = launchTo(hash, cmd);
		}
		return success;
	}
	
	/**
	 * 返回从HASH站点检索的ACCOUNT站点地址
	 * @return ACCOUNT站点地址，或者空指针
	 */
	protected Node replySite() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				TakeAccountSiteProduct e = getObject(TakeAccountSiteProduct.class, index);
				if (e.getRemote() != null) {
					return e.getRemote();
				}
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 根据多个签名，去多个HASH站点查找关联的ACCOUNT地址
	 * @param sigers 一组签名
	 * @param complete 完整模式
	 * @return 成功返回真，否则假
	 */
	protected boolean seekSites(Collection<Siger> sigers, boolean complete) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		// 根据签名，逐一判断HASH站点位置
		for (Siger siger : sigers) {
			// 判断HASH站点
			Node hash = locate(siger);
			// 保存一个命令
			if (hash != null) {
				TakeAccountSite cmd = new TakeAccountSite(siger);
				CommandItem item = new CommandItem(hash, cmd);
				array.add(item);
			}
		}

		// 判断一致
		boolean success = (sigers.size() == array.size());
		// 选择其中一种模式
		if(success) {
			if(complete) {
				success = completeTo(array);
			} else {
				int count = incompleteTo(array);
				success = (count>0);
			}
		}
		
		return success;
	}
	
	/**
	 * 返回检索的全部账号节点地址
	 * @return 账号位置集合
	 */
	protected List<Seat> replySites() {
		ArrayList<Seat> array = new ArrayList<Seat>();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				// 不成功，忽略
				if (!isSuccessObjectable(index)) {
					continue;
				}
				TakeAccountSiteProduct product = getObject(TakeAccountSiteProduct.class, index);
				if (product != null && product.isSuccessful()) {
					// 保存结果
					Siger siger = product.getSiger();
					Node account = product.getRemote();
					Seat seat = new Seat(siger, account);
					array.add(seat);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		return array;
	}
}