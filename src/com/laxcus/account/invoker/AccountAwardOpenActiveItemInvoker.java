/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.cross.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 强制开放授权单元调用器。<br>
 * 
 * 授权人向被授权人开放自己的数据资源，更新被授权人的账号记录。
 * 
 * @author scott.liang
 * @version 1.0 7/30/2017
 * @since laxcus 1.0
 */
public class AccountAwardOpenActiveItemInvoker extends AccountInvoker {

	/**
	 * 构造强制开放授权单元调用器，指定命令
	 * @param cmd 强制开放授权单元
	 */
	public AccountAwardOpenActiveItemInvoker(AwardOpenActiveItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardOpenActiveItem getCommand() {
		return (AwardOpenActiveItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardOpenActiveItem cmd = getCommand();
		CrossField field = cmd.getField();
		// 被授权人签名
		Siger conferrer = field.getConferrer();
		
		Logger.debug(this, "launch", "%s, cross field size:%d", conferrer, field.size());
		
		// 找到被授权人
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(conferrer);
		// 判断账号有效
		Account account = (sphere != null ? sphere.getAccount() : null);
		boolean success = (account != null);
		if (!success) {
			refuse();
			return false;
		}
		
		// 被授权单元保存到账号里
		ArrayList<CrossFlag> array = new ArrayList<CrossFlag>();
		Siger authorizer = field.getAuthorizer();
		for (CrossFlag flag : field.list()) {
			success = account.addPassiveItem(authorizer, flag);
			if (success) {
				array.add(flag);
			}
		}

		// 判断有写入成功的单元
		success = (array.size() > 0);
		// 成功，保存参数！
		if (success) {
			success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
			// 如果发生写错误...
			if (!success) {
				failed();
				return false;
			}
		}

		ShareCrossProduct product = new ShareCrossProduct();
		for (CrossFlag flag : array) {
			product.add(conferrer, flag);
		}

		// 反馈结果
		replyProduct(product);

		Logger.debug(this, "launch", success, "passive items:%d to '%s'", product.size(), conferrer);

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