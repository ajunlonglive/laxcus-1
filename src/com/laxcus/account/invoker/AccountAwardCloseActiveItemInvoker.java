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
 * 强制关闭授权单元调用器。<br><br>
 * 
 * 授权人要求被授权人删除账号中的授权单元，更新磁盘。
 * 
 * 
 * @author scott.liang
 * @version 1.0 7/30/2017
 * @since laxcus 1.0
 */
public class AccountAwardCloseActiveItemInvoker extends AccountInvoker {

	/**
	 * 构造强制关闭授权单元调用器，指定命令
	 * @param cmd 强制关闭授权单元
	 */
	public AccountAwardCloseActiveItemInvoker(AwardCloseActiveItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardCloseActiveItem getCommand() {
		return (AwardCloseActiveItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardCloseActiveItem cmd = getCommand();
		CrossField field = cmd.getField();
		// 被授权人签名
		Siger conferrer = field.getConferrer();
		
		// 找到被授权人
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(conferrer);
		// 判断账号有效
		Account account = (sphere != null ? sphere.getAccount() : null);
		boolean success = (account != null);
		if (!success) {
			refuse();
			return false;
		}
		
		// 从账号中删除被授权单元，成功保存到内存里
		ArrayList<CrossFlag> array = new ArrayList<CrossFlag>();
		Siger authorizer = field.getAuthorizer();
		for (CrossFlag flag : field.list()) {
			success = account.removePassiveItem(authorizer, flag);
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

		Logger.debug(this, "launch", success, "save passive items to '%s'", field.getConferrer());

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