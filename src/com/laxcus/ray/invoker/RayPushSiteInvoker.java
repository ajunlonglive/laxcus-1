/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.ray.runtime.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 推送新注册站点调用器
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class RayPushSiteInvoker extends RayCastElementInvoker {

	/**
	 * 构造推送新注册站点调用器，指定命令
	 * @param cmd 推送新注册站点
	 */
	public RayPushSiteInvoker(PushSite cmd) {
		super(cmd);
		setSound(false); //不要发出声音
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushSite getCommand() {
		return (PushSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CastSite cmd = getCommand();
		Node node = cmd.getSite();
		
		// 判断这个节点存在
		boolean exists = SiteOnRayPool.getInstance().contains(node);
		
		// 保存节点
		boolean success = pushSite(node);
		
		if (success) {
			if (exists) {
				messageX(MessageTip.TIMEING_REFRESH_NODE_X, node); // 刷新节点
			} else {
				messageX(MessageTip.PUSH_NODE_X, node); // 显示成功
			}
		}
	
		Logger.debug(this, "launch", success, "node is %s", node);

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
