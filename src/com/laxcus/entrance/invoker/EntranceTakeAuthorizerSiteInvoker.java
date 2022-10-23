/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.invoker;

import com.laxcus.command.site.entrance.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 获得授权人注册站点调用器 <br>
 * 
 * ENTRANCE站点根据FRONT站点提供的签名，找到GATE站点地址，返回给FRONT站点。
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public class EntranceTakeAuthorizerSiteInvoker extends EntranceInvoker {

	/**
	 * 构造获得授权人注册站点调用器，指定命令
	 * @param cmd 获得授权人注册站点
	 */
	public EntranceTakeAuthorizerSiteInvoker(TakeAuthorizerSite cmd) {
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
		TakeAuthorizerSite cmd = getCommand();
		
		// 判断命令来自公网
		boolean fromWide = cmd.getSourceSite().getAddress().isWideAddress();

		TakeAuthorizerSiteProduct product = new TakeAuthorizerSiteProduct();

		// 从ENTRANCE资源管理池中定位GATE站点
		for (Siger authorizer : cmd.list()) {
			Node gate = locate(authorizer, fromWide);
			if (gate != null) {

				// 找到映射地址
				SiteHost host = gate.getHost();
				// 如果有映射地址，采用映射地址
				boolean success = (fromWide && host.hasReflectTCPort() && host.hasReflectUDPort());
				if (success) {
					SiteHost reflect = new SiteHost(host.getAddress(), host.getReflectTCPort(), host.getReflectUDPort());
					gate.setHost(reflect);
				}
				
				Logger.debug(this, "launch", "gate site is %s, has reflect port %s", gate, (success ? "Yes" : "No"));
				
				product.add(authorizer, gate);
			}
		}
		// 返回给FRONT站点
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "site size:%d", product.size());
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