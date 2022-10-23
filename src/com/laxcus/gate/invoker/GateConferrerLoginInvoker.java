/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.command.site.gate.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;

/**
 * 被授权FRONT站点注册调用器。<br>
 * 
 * 以被授权人的身份注册和登录。
 * 
 * @author scott.liang
 * @version 1.0 7/25/2018
 * @since laxcus 1.0
 */
public class GateConferrerLoginInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造被授权FRONT站点注册调用器，指定命令
	 * @param cmd 被授权FRONT站点注册
	 */
	public GateConferrerLoginInvoker(ConferrerLogin cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ConferrerLogin getCommand() {
		return (ConferrerLogin) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ConferrerLogin cmd = getCommand();
		ConferrerSite front = cmd.getSite();
		Node node = front.getNode();

		// 只允许使用一个授权人
		List<Siger> a = front.getAuthorizers();
		if (a.size() != 1) {
			StayConferrerFrontOnGatePool.getInstance().remove(node);
			return false;
		}

		// 授权人和被授权人
		Siger authorizer = a.get(0);
		Siger conferrer = front.getConferrerUsername();

		// 授权人和被授权人
		boolean success = ConferrerStaffOnGatePool.getInstance().loadRefer(authorizer, conferrer);
		// 不成功，账号加到黑名单
		if (!success) {
			BlackOnGatePool.getInstance().add(front.getConferrer());
		}
		
		if (success) {
			success = CallOnGatePool.getInstance().loadCallSites(authorizer);
		}
		// 以上成功，被授权账号保存到被授权管理池
		if (success) {
			success = ConferrerFrontOnGatePool.getInstance().add(front);
		}
		
		// 以上不成功，删除！
		if (!success) {
			// 删除参数
			ConferrerFrontOnGatePool.getInstance().remove(node, authorizer);
			ConferrerStaffOnGatePool.getInstance().drop(conferrer);

			// 判断账号在这个三个位置存在！
			boolean exists = (RuleHouse.getInstance().contains(authorizer) || 
					FrontOnGatePool.getInstance().contains(authorizer));

			// 授权人管理池和被授权人管理池都没有授权人记录，删除这个账号
			if (!exists) {
				StaffOnGatePool.getInstance().drop(authorizer);
				CallOnGatePool.getInstance().remove(authorizer);
			}
		}

		// 以上无论是否成功，都删除驻留管理池的FRONT地址
		StayConferrerFrontOnGatePool.getInstance().remove(node);

		Logger.debug(this, "launch", success, "conferrer %s bind to %s", conferrer, authorizer);

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