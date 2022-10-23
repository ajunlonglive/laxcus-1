/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;
import com.laxcus.util.*;
import com.laxcus.site.*;

/**
 * 重新设置节点的安全策略命令调用器。<br>
 * 当管理员修改节点conf/site.policy文件后，调用方法重置。
 * 
 * @author scott.liang
 * @version 1.0 6/17/2018
 * @since laxcus 1.0
 */
public abstract class CommonReloadSecurityPolicyInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造重新设置节点的安全策略命令调用器，指定命令
	 * @param cmd 重新设置节点的安全策略命令
	 */
	protected CommonReloadSecurityPolicyInvoker(ReloadSecurityPolicy cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReloadSecurityPolicy getCommand() {
		return (ReloadSecurityPolicy) super.getCommand();
	}
	
	/**
	 * 加载本地链接库文件
	 * @return 返回被加载的动态链接库数组
	 */
	protected ReloadSecurityPolicyItem reload() {
		boolean success = SecurityPolicyLoader.reload();

		// 反馈给请求端
		Node local = getLocal();
		return new ReloadSecurityPolicyItem(local, success);
	}

}
