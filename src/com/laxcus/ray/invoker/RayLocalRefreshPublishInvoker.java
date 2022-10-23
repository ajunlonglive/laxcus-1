/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.account.*;
import com.laxcus.site.*;

/**
 * 刷新已经发布的任务组件调用器。<br>
 * 这是一个本地定时触发命令。当管理员从WATCH站点成功发布任务组件后，系统延时一段时间，通知关联的CALL/DATA/BUILD/WORK节点去更新组件。
 * 
 * @author scott.liang
 * @version 1.0 7/27/2018
 * @since laxcus 1.0
 */
public class RayLocalRefreshPublishInvoker extends RayInvoker {

	/**
	 * 构造刷新已经发布的任务组件调用器，指定命令
	 * @param cmd 刷新已经发布的任务组件
	 */
	public RayLocalRefreshPublishInvoker(RefreshPublish cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshPublish getCommand() {
		return (RefreshPublish) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Node hub = getHub();
		// 必须是BANK站点才能发送
		boolean success = hub.isBank();
		if (success) {
			RefreshPublish cmd = getCommand();
			success = directTo(hub, cmd);
		}
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