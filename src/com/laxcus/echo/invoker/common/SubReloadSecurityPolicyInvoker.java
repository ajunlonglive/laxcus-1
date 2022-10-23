/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;

/**
 * 工作类站点的重新设置节点的安全策略命令调用器 <br>
 * 
 * 用在除TOP、HOME、FRONT、WATCH四类站点之外的所有站点，即ACCOUNT、LOG、DATA、WORK、BUILD、CALL。
 * 
 * @author scott.liang
 * @version 1.0 6/17/2018
 * @since laxcus 1.0
 */
public class SubReloadSecurityPolicyInvoker extends CommonReloadSecurityPolicyInvoker {

	/**
	 * 构造重新设置节点的安全策略命令调用器，指定命令
	 * @param cmd 重新设置节点的安全策略命令
	 */
	public SubReloadSecurityPolicyInvoker(ReloadSecurityPolicy cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 重装加载本地动态链接库
		ReloadSecurityPolicyItem item = reload();
		// 生成报告
		ReloadSecurityPolicyProduct product = new ReloadSecurityPolicyProduct();
		product.add(item);
		boolean success = replyProduct(product);
		// 退出
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
