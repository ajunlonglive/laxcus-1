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
import com.laxcus.access.schema.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.cross.*;
import com.laxcus.echo.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.*;

/**
 * 开放数据库资源调用器。<br>
 * 
 * 授权人（数据持有人）向被授权人开放自己的数据资源。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class AccountOpenShareSchemaInvoker extends AccountShareCrossInvoker {

	/**
	 * 构造开放数据库资源调用器，制定命令
	 * @param cmd 开放数据库资源
	 */
	public AccountOpenShareSchemaInvoker(OpenShareSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OpenShareSchema getCommand() {
		return (OpenShareSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShareSchema cmd = getCommand();
		// 授权人
		Siger authorizer = cmd.getIssuer();

		// 找到账号
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(authorizer);
		// 判断账号有效
		Account account = (sphere != null ? sphere.getAccount() : null);
		boolean success = (account != null);
		if (!success) {
			failed();
			return false;
		}

		// 根据数据库，找出全部关联的数据表，输出共享标识
		Set<CrossFlag> flags = pickup(account);

		// 判断共享操作符合已经定义操作权限
		success = confirm(account, cmd.getOperator(), flags);
		if (!success) {
			replyFault(Major.FAULTED, Minor.PERMISSION_DENIED);
			return useful(false);
		}

		// 保存被授权人到账号
		ShareCrossProduct product = new ShareCrossProduct();
		List<Siger> conferrers = cmd.getConferrers();
		for (Siger conferrer : conferrers) {
			for (CrossFlag flag : flags) {
				success = account.addActiveItem(conferrer, flag);
				if (success) {
					product.add(conferrer, flag);
				}
			}
		}

		// 如果没有被授权单元，返回空集，退出
		if(product.size() == 0) {
			replyProduct(product);
			return false;
		}

		// 新的结果保存到磁盘上
		success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
		if (success) {
			success = replyProduct(product);
		}
		// 不成功，反馈操作失败！
		if (!success) {
			failed();
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

	/**
	 * 根据账号和命令定义的关系，选择共享标识。
	 * @param account 账号实例
	 * @return 返回共享标识
	 */
	private Set<CrossFlag> pickup(Account account) {
		// 筛选关联的标识
		TreeSet<CrossFlag> array = new TreeSet<CrossFlag>();
		// 找到全部数据库
		List<Space> records = account.getSpaces();

		// 根据命令要求选择...
		OpenShareSchema cmd = getCommand();

		// 操作符
		int operator = cmd.getOperator(); 

		if (cmd.isAll()) {
			for (Space space : records) {
				array.add(new CrossFlag(space, operator));
			}
		} else {
			for (Fame define : cmd.getFames()) {
				for (Space space : records) {
					if (Laxkit.compareTo(space.getSchema(), define) == 0) {
						array.add(new CrossFlag(space, operator));
					}
				}
			}
		}
		return array;
	}

}