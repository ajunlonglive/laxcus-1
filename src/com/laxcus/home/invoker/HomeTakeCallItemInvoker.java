/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.relate.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;

/**
 * 获取CALL站点成员调用器<br>
 * 
 * 命令发送顺序是：GATE -> BANK -> TOP -> HOME，HOME站点根据用户名称返回对应的CALL站点记录。
 * 
 * @author scott.liang
 * @version 1.0 6/23/2013
 * @since laxcus 1.0
 */
public class HomeTakeCallItemInvoker extends HomeInvoker {

	/**
	 * 获取CALL站点成员调用器，指定命令
	 * @param cmd 获取CALL站点成员命令
	 */
	public HomeTakeCallItemInvoker(TakeCallItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeCallItem getCommand() {
		return (TakeCallItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeCallItem cmd = getCommand();
		Siger siger = cmd.getUsername();

		// 查找关联的CALL站点
		ArrayList<Node> slaves = new ArrayList<Node>();
		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}

		Logger.debug(this, "launch", "<%s> sites:%d", siger, slaves.size());

		// 逐一查找
		TakeCallItemProduct product = new TakeCallItemProduct(siger);
		for (Node node : slaves){
			CallSite site = (CallSite) CallOnHomePool.getInstance().find(node);
			if (site == null) {
				Logger.error(this, "launch", "cannot be find %s", node);
				continue;
			}
			CallMember member = site.find(siger);
			if (member == null) {
				Logger.error(this, "launch", "not find %s", siger);
				continue;
			}

			// DEBUG CODE, START
			Logger.debug(this, "launch", "[%s] space size:%d, phase size:%d",
					siger, member.getTables().size(), member.getPhases().size());
			for (Space space : member.getTables()) {
				Logger.debug(this, "launch", "space is:%s", space);
			}
			for (Phase phase : member.getPhases()) {
				Logger.debug(this, "launch", "phase is:%s", phase);
			}
			// DEBUG CODE, END

			CallItem item = new CallItem(site.getPrivate(), site.getPublic(), member);
			product.add(item);
		}

		// 返回给TOP站点
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "'%s' product size is %d", siger, product.size());

		return useful(success);
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
