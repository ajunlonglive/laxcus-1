/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 获得授权人注册站点调用器<br><br>
 * 
 * 方案流程：FRONT -> ENTRANCE(判断GATE站点) -> FRONT <br><br>
 * 
 * GateOnFrontPool执行注册、注销。
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public class FrontTakeAuthorizerSiteInvoker extends FrontInvoker {

	/**
	 * 构造获得授权人注册站点调用器，指定命令
	 * @param cmd 获得授权人注册站点
	 */
	public FrontTakeAuthorizerSiteInvoker(TakeAuthorizerSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAuthorizerSite getCommand() {
		return (TakeAuthorizerSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 取得初始登录站点（ENTRANCE站点）
		Node hub = getLauncher().getInitHub();
		// 发送这个命令给ENTRANCE站点
		TakeAuthorizerSite cmd = getCommand();
		return launchTo(hub, cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		TakeAuthorizerSiteProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAuthorizerSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			
			Logger.debug(this, "ending", "authorizer item size:%d", product.size());
			
			for (AuthorizerItem item : product.list()) {
				Siger authorizer = item.getAuthorizer(); // 授权人
				Node gate = item.getSite(); // GATE站点
				
				Logger.debug(this, "ending", "authorizer:%s login to: %s", authorizer, gate);
				
				// 建立FRONT -> 授权者GATE节点映射
				checkPock(gate);
				// 注册到GATE站点
				boolean logined = AuthroizerGateOnFrontPool.getInstance().login(gate, authorizer);
				Logger.note(this, "ending", logined, "[%s] login to %s", authorizer, gate);

				// 注册成功，去获取授权人的CALL站点和授权表名
				if (logined) {
					Siger conferrer = getUsername(); // 当前用户的身份是被授权人
					// 加载授权人的CALL站点
					loadAuthorizerCallSite(authorizer, conferrer);
					// 加载授权人的表实例
					loadAuthorizerTable(authorizer, conferrer);
				}
			}
		}

		// 退出
		return useful(success);
	}
	
	/**
	 * 加载授权人的CALL站点
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 */
	private void loadAuthorizerCallSite(Siger authorizer, Siger conferrer) {
		TakeAuthorizerCall cmd = new TakeAuthorizerCall(authorizer, conferrer);
		FrontTakeAuthorizerCallInvoker invoker = new FrontTakeAuthorizerCallInvoker(cmd);
		getInvokerPool().launch(invoker);
	}

	/**
	 * 被授权人加载授权人的数据表，保存到本地
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 */
	private void loadAuthorizerTable(Siger authorizer, Siger conferrer) {
		TakeAuthorizerTable cmd = new TakeAuthorizerTable(authorizer, conferrer);
		FrontTakeAuthorizerTableInvoker invoker = new FrontTakeAuthorizerTableInvoker(cmd);
		getInvokerPool().launch(invoker);
	}
}