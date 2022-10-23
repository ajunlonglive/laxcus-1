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
import com.laxcus.command.cross.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;

/**
 * 共享资源调用器
 * 
 * @author scott.liang
 * @version 1.0 7/4/2017
 * @since laxcus 1.0
 */
public abstract class AccountShareCrossInvoker extends AccountInvoker {

	/**
	 * 构造共享资源调用器，指定命令
	 * @param cmd 共享资源命令
	 */
	protected AccountShareCrossInvoker(ShareCross cmd) {
		super(cmd);
	}
	
	/**
	 * 打印账号里的授权人、被授权人信息
	 * @param account
	 */
	protected void print(Account account) {
		List<ActiveItem> activeItems = account.getActiveItems();
		Logger.debug(this, "print", "active item count: %d", activeItems.size());
		for (ActiveItem item : activeItems) {
			Logger.debug(this, "print", "active! conferrer: %s # %s # %s", item.getConferrer(), item.getSpace(),
					CrossOperator.translate(item.getOperator()));
		}
		
		List<PassiveItem> passiveItems = account.getPassiveItems();
		Logger.debug(this, "print", "passive item count: %d", passiveItems.size());
		for (PassiveItem item : passiveItems) {
			Logger.debug(this, "print", "passive! authorizer: %s # %s # %s", item.getAuthorizer(), item.getSpace(),
					CrossOperator.translate(item.getOperator()));
		}
	}
	
	/**
	 * 依据授权操作符，判断共享操作符合已经定义操作权限。<br>
	 * 操作流程：首先判断操作符是SELECT/INSERT/DELETE/UPDATE的哪一种，进一步判断对应的操作权限已经定义。
	 * 如果没有通过返回假，全部通过返回真。
	 * 
	 * @param account 授权人账号
	 * @param operator 授权操作符
	 * @param spaces 数据表名
	 * @return 通过返回真，否则假
	 */
	protected boolean confirm(Account account, int operator, List<Space> spaces) {
		// 如果是SELECT操作
		if (CrossOperator.isSelect(operator)) {
			for (Space space : spaces) {
				// 如果SELECT不能通过，返回假
				if (!account.canSelect(space)) {
					return false;
				}
			}
		}
		// 判断是INSERT操作
		if (CrossOperator.isInsert(operator)) {
			for (Space space : spaces) {
				// 不符合INSERT权限
				if (!account.canInsert(space)) {
					return false;
				}
			}
		}
		// 判断是DELETE操作
		if (CrossOperator.isDelete(operator)) {
			for (Space space : spaces) {
				if (!account.canDelete(space)) {
					return false;
				}
			}
		}
		// 判断是UPDATE操作
		if (CrossOperator.isUpdate(operator)) {
			for (Space space : spaces) {
				if (!account.canUpdate(space)) {
					return false;
				}
			}
		}
		// 以上全部通过，返回真
		return true;
	}

	/**
	 * 依据授权操作符，判断共享操作符合已经定义操作权限。<br>
	 * 
	 * @param account 授权人账号
	 * @param operator 授权操作符
	 * @param flags 资源共享标识
	 * @return 通过返回真，否则假
	 */
	protected boolean confirm(Account account, int operator, Collection<CrossFlag> flags) {
		ArrayList<Space> spaces = new ArrayList<Space>();
		for(CrossFlag e : flags) {
			spaces.add(e.getSpace());
		}
		return confirm(account, operator, spaces);
	}

}
