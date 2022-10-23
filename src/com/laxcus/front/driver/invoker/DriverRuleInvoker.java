/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.command.rule.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.front.util.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 基于驱动程序模式的事务调用器 <br>
 * 
 * 驱动程序事务调用器提供了默认的数据规则处理流程，子类将按照这个规则执行处理。
 * 
 * @author scott.liang
 * @version 1.23 09/06/2016
 * @since laxcus 1.0
 */
public abstract class DriverRuleInvoker extends DriverInvoker {

	/** 事务调用阶段 **/
	private SerialStage stage = SerialStage.INSURE;

	/** 事务处理规则表 **/
	private RuleSheet sheet = new RuleSheet();

	/** 建立事务表 **/
	private RuleTokenSheet tokens;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		sheet = null;
		tokens = null;
	}

	/**
	 * 构造基于驱动程序模式的事务调用器，指定驱动程序任务
	 * @param mission 驱动程序任务
	 */
	protected DriverRuleInvoker(DriverMission mission) {
		super(mission);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果拒绝管理员，且当前账号是管理员，拒绝执行！
		boolean refuse = (isRefuseAdministrator() && isAdministrator());
		if (refuse) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}
		// 执行
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 保存一批事务规则
	 * @param e 事务规则表
	 * @return 返回新保存的成员数目
	 */
	protected int addRules(RuleSheet e) {
		// 不允许空指针
		Laxkit.nullabled(e);
		// 保存全部
		return sheet.addAll(e);
	}

	/**
	 * 保存一个事务规则
	 * @param rule 事务规则
	 * @return 返回新增加的成员数目
	 */
	protected int addRule(RuleItem rule) {
		return addRules(new RuleSheet(rule));
	}

	/**
	 * 保存一组事务规则
	 * @param rules 事务规则数组
	 * @return 返回新增加的成员数目
	 */
	protected int addRules(RuleItem[] rules) {
		return addRules(new RuleSheet(rules));
	}

	/**
	 * 保存一组事务规则
	 * @param rules 事务规列表
	 * @return 返回新增加的成员数目
	 */
	protected int addRules(List<RuleItem> rules) {
		return addRules(new RuleSheet(rules));
	}

	/**
	 * 执行基于事务的数据处理流程
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		// 1. 向GATE站点申请事务
		if (stage == SerialStage.INSURE) {
			boolean success = insure();
			// 成功，进入“检查申请”阶段和正常退出；失败，退出和释放资源
			if (success) {
				stage = SerialStage.CHECK_INSURE;
				return true;
			} else {
				return useful(false);
			}
		}
		// 2. 判断GATE的事务处理结果
		if (stage == SerialStage.CHECK_INSURE) {
			boolean success = checkInsure();
			// 不成功退出和释放资源，否则进入“数据处理”阶段
			if (!success) {
				return useful(false);
			}
			stage = SerialStage.PROCESS;
		}
		// 3. 执行数据处理工作
		if (stage == SerialStage.PROCESS) {
			boolean finished = false;
			try {
				finished = process();
			} catch (Throwable e) {
				super.fault(e);
				Logger.fatal(e);
				finished = true; // 出现未知故障，默认处理完成
			}
			// 没有完成，返回“真”退出（以后继续处理）；否则进入“撤销业务”阶段。
			if (!finished) {
				return true;
			}
			stage = SerialStage.REVOKE;
		}
		// 4. 撤销事务
		if (stage == SerialStage.REVOKE) {
			boolean success = revoke();
			// 成功，进入检查撤销阶段和正常退出；失败，退出和释放资源
			if (success) {
				stage = SerialStage.CHECK_REVOKE;
				return true;
			} else {
				return useful(false); // 撤销失败
			}
		}
		// 5.检查撤销的处理结果，完全退出和释放资源
		if (stage == SerialStage.CHECK_REVOKE) {
			boolean success = checkRevoke();
			return useful(success);
		}
		// 不是以上任何一个阶段，是错误，退出
		return false;
	}

	/**
	 * 向GATE站点申请一个事务请求
	 * @return 投递事务申请到GATE站点，GATE站点接受返回真，否则假。
	 */
	private boolean insure() {
		if (sheet.isEmpty()) {
			Logger.error(this, "insure", "cannot be create rule!");
			faultX(FaultTip.EMPRY_LIST);
			return false;
		}

		// 对事务规则进行分类
		tokens = classify(sheet.list());
		if (tokens == null) {
			Logger.error(this, "insure", "cannot be classify rule!");
			faultX(FaultTip.EMPRY_LIST);
			return false;
		}

		// 生成命令单元
		List<CommandItem> array = new ArrayList<CommandItem>();
		for (RuleToken e : tokens.list()) {
			CommandItem item = new CommandItem(e.getHub(), e.getCommand());
			array.add(item);
		}
		// 以容错模式发送命令
		int count = incompleteTo(array);
		// 如果是0，完全没有发送成功，退出
		boolean success = (count > 0);

		Logger.debug(this, "insure", success,
				"send count:%d, command item size:%d", count, array.size());
		
		// 提示错误
		if (!success) {
			faultX(FaultTip.RULE_ATTACH_FAULT);
		}

		// 只要有一个发送成功，都要返回TRUE。然后等待进入“CHECK INSURE”处理
		return success;
	}

	/**
	 * 检查向GATE投递的事务申请
	 * @return 成功返回真，否则假
	 */
	private boolean checkInsure() {
		List<Integer> keys = getEchoKeys();
		// 统计成功
		ArrayList<RuleProduct> products = new ArrayList<RuleProduct>();
		try {
			for (int index : keys) {
				// 忽略非对象
				if (!isObjectable(index)) {
					continue;
				}
				// 取出参数
				RuleProduct e = getObject(RuleProduct.class, index);
				// 只保留投递成功的单元
				if (e.isSuccessful()) {
					products.add(e);
				}
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}

		// 判断存在错误
		boolean success = (products.size() == tokens.size());
		// 如果存在错误，发送撤销命令
		if (!success) {
			ArrayList<CommandItem> items = new ArrayList<CommandItem>();
			// 找到成功的单元，发送DropRule命令
			for (RuleProduct e : products) {
				RuleToken token = tokens.find(e.getTag());
				DetachRule cmd = token.getCommand().reverse(); // 反向命令
				CommandItem item = new CommandItem(token.getHub(), cmd);
				items.add(item);
			}
			// 直接发送到GATE站点，不用反馈
			directTo(items, false);
		}

		// 显示结果
		Logger.debug(this, "checkInsure", success, "result is");
		
		if (!success) {
			faultX(FaultTip.RULE_ATTACH_RETURN_FAULT);
		}

		// 返回成功
		return success;
	}

	/**
	 * 撤销事务申请
	 * @return 投递到GATE站点，且GATE接受返回真，否则假
	 */
	private boolean revoke() {
		// 生成DropRule命令
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (RuleToken e : tokens.list()) {
			DetachRule drop = e.getCommand().reverse();
			CommandItem item = new CommandItem(e.getHub(), drop);
			array.add(item);
		}

		// 以容错模式发送到不同的GATE站点
		int count = incompleteTo(array);
		// 只有一个发送成功，就是成功
		boolean success = (count > 0);

		Logger.debug(this, "revoke", success,
				"send count:%d, command items size:%d", count, array.size());

		if (!success) {
			faultX(FaultTip.RULE_DETACH_FAULT);
		}

		return success;
	}

	/**
	 * 检查撤销事务申请
	 * @return 撤销成功返回真，否则假。
	 */
	private boolean checkRevoke() {
		List<Integer> keys = getEchoKeys();

		ArrayList<RuleProduct> products = new ArrayList<RuleProduct>();
		for (int index : keys) {
			try {
				// 非对象，忽略它
				if (!isObjectable(index)) {
					continue;
				}
				// 取出参数
				RuleProduct e = getObject(RuleProduct.class, index);
				// 如果撤销成功，保存它
				if (e.isSuccessful()) {
					products.add(e);
				}
			} catch (VisitException e) {
				Logger.fatal(e);
			}
		}
		// 任何一个成功，全部成功
		boolean success = (products.size() > 0);

		Logger.debug(this, "checkRevoke", success, "product size:%d, rule token size:%d", 
				products.size(), tokens.size());
		
		// 出错提示
		if (!success) {
			faultX(FaultTip.RULE_DETACH_RETURN_FAULT);
		}
		
		// 返回结果
		return success;
	}

	/**
	 * 执行数据处理阶段的工作 <br>
	 * 这个方法由子类根据各自需求去实现，通常有多步操作。
	 * 
	 * @return 只有当数据处理阶段的工作全部完成时，才能返回真，否则都是假。<b>特别说明：数据处理错误也要返回“真”。<b>
	 */
	protected abstract boolean process();

}