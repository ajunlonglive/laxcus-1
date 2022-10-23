/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.pool.*;
import com.laxcus.command.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示事务规则命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/2/2017
 * @since laxcus 1.0
 */
public class GateShowLockRuleInvoker extends GateInvoker {

	/**
	 * 构造显示事务规则命令调用器，指定命令
	 * @param cmd 显示事务规则命令
	 */
	public GateShowLockRuleInvoker(ShowLockRule cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowLockRule getCommand() {
		return (ShowLockRule) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowLockRule cmd = getCommand();
		Siger siger = cmd.getIssuer();

		ShowLockRuleProduct product = new ShowLockRuleProduct();

		// 查询结果
		RuleManager manager = RuleHouse.getInstance().find(siger);
		if (manager != null) {
			product.addRunRules(manager.getRunRules());
			product.addWaitRules(manager.getWaitRules());
		}

		// 发送检查结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "%s rule size is:%d", siger, product.size());

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
