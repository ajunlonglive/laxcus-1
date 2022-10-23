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
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 关闭数据库资源调用器。<br>
 * 
 * 授权人（数据持有人）向被授权人关闭自己的数据资源。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class AccountCloseShareTableInvoker extends AccountShareCrossInvoker {

	/**
	 * 构造关闭数据库资源调用器，制定命令
	 * @param cmd 关闭数据库资源
	 */
	public AccountCloseShareTableInvoker(CloseShareTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CloseShareTable getCommand() {
		return (CloseShareTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CloseShareTable cmd = getCommand();
		// 授权人
		Siger authorizer = cmd.getIssuer();

		// 从磁盘中读...
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(authorizer);
		// 判断账号有效
		Account account = (sphere != null ? sphere.getAccount() : null);
		boolean success = (account != null);
		if (!success) {
			failed();
			return false;
		}
		
		// 删除前前打印共享资源
		print(account);

		// 从账号中找到匹配的共享标识
		Set<CrossFlag> flags = pickup(account);
		
		// 判断共享操作符合已经定义操作权限
		success = confirm(account, cmd.getOperator(), flags);
		if (!success) {
			replyFault(Major.FAULTED, Minor.PERMISSION_DENIED);
			return false;
		}

		// 保存被授权人到账号
		ShareCrossProduct product = new ShareCrossProduct();
		List<Siger> conferrers = cmd.getConferrers();
		for (Siger conferrer : conferrers) {
			for (CrossFlag flag : flags) {
				// 删除授权单元
				success = account.removeActiveItem(conferrer, flag);
				// 删除成功，保存它
				if (success) {
					CrossFlag diff = flag.duplicate();
					int operator = CrossOperator.and(cmd.getOperator(), flag.getOperator()); // 取它们相同的操作符
					diff.setOperator(operator); 
					product.add(conferrer, diff);
				}
			}
		}
		
		Logger.debug(this, "launch", "product size: %d", product.size());

		// 删除后打印
		print(account);
		
		// 如果是空集合，直接输出
		if (product.size() == 0) {
			replyProduct(product);
			return false;
		}
		
		// 更新磁盘记录和输出结果
		success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
		if (success) {
			success = replyProduct(product);
		}
		// 不成功，通知写入失败
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
	 * 从账号中找到参数匹配的共享标识
	 * @param account 账号实例
	 * @return 返回已经存在且匹配的共享标识
	 */
	private Set<CrossFlag> pickup(Account account) {
		// 筛选关联的标识
		TreeSet<CrossFlag> array = new TreeSet<CrossFlag>();
		// 命令
		CloseShareTable cmd = getCommand();
		// 操作符
		int operator = cmd.getOperator(); 
		// 全部被授权人
		List<Siger> conferrers = cmd.getConferrers();
		
		// 账号里的授权单元
		List<ActiveItem> items = account.getActiveItems();

		// 找出全部授权单元
		if (cmd.isAll()) {
			for (ActiveItem item : items) {
				// 选择签名一致和...
				for (Siger conferrer : conferrers) {
					if (Laxkit.compareTo(item.getConferrer(), conferrer) != 0) {
						continue;
					}
					// 用“与”操作，取它们的相同值
					int who = CrossOperator.and(operator, item.getOperator());
					// 有共同值，保存它！
					if (!CrossOperator.isNone(who)) {
						CrossFlag flag = new CrossFlag(item.getSpace(), who);
						array.add(flag);
					}
					break;
				}
			}
		} else {
			List<Space> spaces = cmd.getSpaces();
			for (ActiveItem item : items) {
				Space space = item.getSpace();
				Siger conferrer = item.getConferrer();
				// 被授权人和表名一致！
				boolean success = (conferrers.contains(conferrer) && spaces.contains(space));
				if (success) {
					// 用“与”操作，取它们的相同值
					int who = CrossOperator.and(operator, item.getOperator());
					// 有共同值，保存它！
					if (!CrossOperator.isNone(who)) {
						CrossFlag flag = new CrossFlag(space, who);
						array.add(flag);
					}
				}
			}
		}
		
		Logger.debug(this, "pickup", "cross flag count: %d", array.size());

		return array;
	}


}