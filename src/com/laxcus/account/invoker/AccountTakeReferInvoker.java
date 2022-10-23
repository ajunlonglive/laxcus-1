/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.refer.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 获得账号资源引用调用器。
 * 
 * 从GATE/TOP/HOME发出，ACCOUNT接受并反馈结果
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class AccountTakeReferInvoker extends AccountInvoker {

	/**
	 * 构造获得账号资源引用调用器，指定命令
	 * @param cmd 获得账号资源引用
	 */
	public AccountTakeReferInvoker(TakeRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeRefer getCommand() {
		return (TakeRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeRefer cmd = getCommand();
		Siger conferrer = cmd.getSiger();

		// 找到账号，生成被授权用户的资源引用
		Account account = StaffOnAccountPool.getInstance().readAccount(conferrer);
		boolean success = (account != null);
		if (success) {
			Refer refer = new Refer(account);
			TakeReferProduct product = new TakeReferProduct(refer);
			replyProduct(product);
			
			// debug code, start
			Logger.debug(this, "launch", "active item size:%d, passive item size:%d",
					refer.getActiveItems().size(), refer.getPassiveItems().size());
			for (ActiveItem e : refer.getActiveItems()) {
				Logger.debug(this, "launch", "active item： %s", e);
			}
			for (PassiveItem e : refer.getPassiveItems()) {
				Logger.debug(this, "launch", "passive item： %s", e);
			}
			for (Space space : refer.getTables()) {
				Logger.debug(this, "launch", "local table： %s", space);
			}
			// debug code, end
		} else {
			failed();
		}
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