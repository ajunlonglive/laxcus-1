/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.cross.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 删除用户资源调用器。 <br>
 * 
 * 是DropTable、DropSchemae、DropUser命令的超类。
 * 
 * @author scott.liang
 * @version 1.0 5/27/2019
 * @since laxcus 1.0
 */
public abstract class BankDropUserResourceInvoker extends BankSeekAccountSiteInvoker {
	
	/** 被删除的授权人账号  和  ACCOUNT站点 **/
	protected TreeSet<Seat> dropConferrers = new TreeSet<Seat>();

	/**
	 * 构造删除用户资源调用器，指定命令
	 * @param cmd 公共命令
	 */
	protected BankDropUserResourceInvoker(Command cmd) {
		super(cmd);
	}
	
	/**
	 * 去被授权人的ACCOUNT站点，要求被授权人强制删除授权单元
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 * @param flag 资源共享标识
	 */
	private boolean doAwardDropPassiveItem(Siger authorizer, Siger conferrer, CrossFlag flag) {
		CrossField field = new CrossField(authorizer, conferrer);
		field.add(flag);
		AwardCloseActiveItem cmd = new AwardCloseActiveItem(field);

		AwardCloseActiveItemHook hook = new AwardCloseActiveItemHook();
		ShiftAwardCloseActiveItem shift = new ShiftAwardCloseActiveItem(cmd, hook);
		
		// 立即执行
		boolean success = getCommandPool().press(shift);
		// 成功，等待返回结果
		if (success) {
			// 等待
			hook.await();
		}
		// 取结果
		ShareCrossProduct product = hook.getProduct();
		success = (product != null && product.size() > 0);

		// 成功，保存被授权人的账号
		if (success) {
			Node account = hook.getAccountSite();
			if (account != null) {
				Seat seat = new Seat(conferrer, account);
				dropConferrers.add(seat);
			}
		}
		
		Logger.debug(this, "doAwardDropPassiveItem", "release %s # %s",
				conferrer, flag);
		
		return success;
	}
	
	/**
	 * 删除用户自己授权的某个表
	 * @param account 账号
	 * @return 返回解除授权人成功的数目
	 */
	protected int dropActiveTable(Account account, Space space) {
		// 检查
		List<ActiveItem> items = account.getActiveItems();
		if (items.isEmpty()) {
			return 0;
		}

		int count = 0;
		// 逐一检查，去ACCOUNT站点删除
		for (ActiveItem item : items) {
			// 不是同名表，忽略它！
			if (Laxkit.compareTo(item.getSpace(), space) != 0) {
				continue;
			}

			// 生成命令，不用指定表名，默认就是全部
			CloseShareTable cmd = new CloseShareTable();
			cmd.setOperator(item.getOperator());
			cmd.addSpace(item.getSpace());
			cmd.addConferrer(item.getConferrer()); // 被授权人单元里的被授权人

			// 生成命令钩子和转发命令
			CloseShareTableHook hook = new CloseShareTableHook();
			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
			shift.setIssuer(account.getUsername()); // 执行人是账号持有人

			// 交给命令管理池，快速处理！
			boolean	success = getCommandPool().press(shift);
			if (success) {
				hook.await();
			}

			// 返回报告
			ShareCrossProduct product = hook.getProduct();
			// 判断成功
			success = (product != null && product.size() > 0);
			if (success) {
				count++;

				// 去被授权的ACCOUN站点，删除关联的记录
				doAwardDropPassiveItem(account.getUsername(), item.getConferrer(),
						item.getFlag());
			}

			Logger.note(this, "dropActiveTable", success, "drop %s # %s", 
					item.getConferrer(), item.getSpace());
		}
		// 返回结果
		return count;
	}
	
	/**
	 * 删除用户自己授权的某个数据库下的所有表
	 * @param account 账号
	 * @return 返回解除授权人成功的数目
	 */
	protected int dropActiveSchema(Account account, Fame fame) {
		// 检查
		List<ActiveItem> items = account.getActiveItems();
		if (items.isEmpty()) {
			return 0;
		}

		int count = 0;
		// 逐一检查，去ACCOUNT站点删除
		for (ActiveItem item : items) {
			// 不是同名数据库，忽略它！
			if (Laxkit.compareTo(item.getSchema(), fame) != 0) {
				continue;
			}
			
			// 生成命令，不用指定表名，默认就是全部
			CloseShareTable cmd = new CloseShareTable();
			cmd.setOperator(item.getOperator());
			cmd.addSpace(item.getSpace());
			cmd.addConferrer(item.getConferrer()); // 被授权人单元里的被授权人

			// 生成命令钩子和转发命令
			CloseShareTableHook hook = new CloseShareTableHook();
			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
			shift.setIssuer(account.getUsername()); // 执行人是账号持有人

			// 交给命令管理池，快速处理！
			boolean	success = getCommandPool().press(shift);
			if (success) {
				hook.await();
			}

			// 返回报告
			ShareCrossProduct product = hook.getProduct();
			// 判断成功
			success = (product != null && product.size() > 0);
			if (success) {
				count++;

				doAwardDropPassiveItem(account.getUsername(), item.getConferrer(),
						item.getFlag());
			}

			Logger.note(this, "dropActiveSchema", success, "drop %s # %s",
					item.getConferrer(), item.getSpace());
		}
		// 返回结果
		return count;
	}

	/**
	 * 删除用户自己授权的所有表
	 * 
	 * @param account 账号
	 * @return 返回解除授权人成功的数目
	 */
	protected int dropActiveTables(Account account) {
		// 检查
		List<ActiveItem> items = account.getActiveItems();
		if (items.isEmpty()) {
			return 0;
		}

		int count = 0;
		// 逐一检查，去ACCOUNT站点删除
		for (ActiveItem item : items) {
			// 生成命令，不用指定表名，默认就是全部
			CloseShareTable cmd = new CloseShareTable();
			cmd.setOperator(item.getOperator());
			cmd.addSpace(item.getSpace());
			cmd.addConferrer(item.getConferrer()); // 被授权人单元里的被授权人

			// 生成命令钩子和转发命令
			CloseShareTableHook hook = new CloseShareTableHook();
			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
			shift.setIssuer(account.getUsername()); // 执行人是账号持有人

			// 交给命令管理池，快速处理！
			boolean	success = getCommandPool().press(shift);
			if (success) {
				hook.await();
			}

			// 返回报告
			ShareCrossProduct product = hook.getProduct();
			// 判断成功
			success = (product != null && product.size() > 0);
			if (success) {
				count++;
				
				doAwardDropPassiveItem(account.getUsername(), item.getConferrer(), item.getFlag());
			}

			Logger.note(this, "dropActiveTables", success, "drop %s # %s", item.getConferrer(), item.getSpace());
		}
		// 返回结果
		return count;
	}
	
	/**
	 * 删除账号持有人被授权的所有表。
	 * 账号持有人，找到自己内部的被授权单元。系统以授权人的身份，删除账号持有人内部的被授权单元。
	 * 
	 * @param account 用户自己的账号
	 * @return 返回解除授权的成功数目
	 */
	protected int dropPassiveTables(Account account) {
		List<PassiveItem> items = account.getPassiveItems();
		if (items.isEmpty()) {
			return 0;
		}

		int count = 0;
		// 逐一删除
		for(PassiveItem item : items) {
			// 生成命令，不用指定表名，默认就是全部
			CloseShareTable cmd = new CloseShareTable();
			cmd.setOperator(item.getOperator());
			cmd.addSpace(item.getSpace());
			cmd.addConferrer(account.getUsername()); // 被授权人是账号持有人

			// 生成命令钩子和转发命令
			CloseShareTableHook hook = new CloseShareTableHook();
			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
			shift.setIssuer( item.getAuthorizer() ); // 执行人是被授权单元里的数据持有人

			// 交给命令管理池，快速处理！
			boolean	success = getCommandPool().press(shift);
			if (success) {
				hook.await();
			}

			// 返回报告
			ShareCrossProduct product = hook.getProduct();
			// 判断成功
			success = (product != null && product.size() > 0);
			if (success) {
				count++;
			}

			Logger.note(this, "dropPassiveTables", success, "drop %s # %s", item.getAuthorizer(), item.getSpace());
		}

		return count;
	}
}
