/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.limit.*;
import com.laxcus.command.rule.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.invoker.*;
import com.laxcus.front.util.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 基于交互模式的事务调用器 <br>
 * 
 * 事务调用器提供了默认的数据规则处理流程，子类将按照这个规则执行处理。
 * 
 * @author scott.liang
 * @version 1.23 09/06/2016
 * @since laxcus 1.0
 */
public abstract class MeetRuleInvoker extends MeetInvoker {

	/** 事务调用阶段 **/
	private SerialStage stage = SerialStage.INSURE;

	/** 事务处理规则表 **/
	private RuleSheet sheet = new RuleSheet();

	/** 事务标记表 **/
	private RuleTokenSheet tokens;
	
	/** 锁定资源，默认是假 **/
	private boolean lockScheduleRefresh;
	
	/** 跨过刷新，默认是真 **/
	private boolean skipScheduleRefresh;

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
	 * 构造FRONT站点事务调用器
	 * @param cmd 命令
	 * @param lockScheduleRefresh 申请锁定资源，防止FrontScheduleRefreshInvoker同时激活
	 */
	protected MeetRuleInvoker(Command cmd, boolean lockScheduleRefresh) {
		super(cmd);
		setLockScheduleRefresh(lockScheduleRefresh);
		setSkipScheduleRefresh(true);
	}
	
	/**
	 * 构造FRONT站点事务调用器
	 * @param cmd 命令
	 */
	protected MeetRuleInvoker(Command cmd) {
		this(cmd, false);
	}
	
	/**
	 * 设置为跨过刷新
	 * @param b 真或者假
	 */
	protected void setSkipScheduleRefresh(boolean b) {
		skipScheduleRefresh = b;
	}
	
	/**
	 * 判断是跨过刷新
	 * @return 真或者假
	 */
	protected boolean isSkipScheduleRefresh() {
		return skipScheduleRefresh;
	}

	/**
	 * 设置锁定资源更新。获得这个权限后，FrontScheduleRefreshInvoker或者其他调用器不能并行处理资源。
	 * @param b 真或者假
	 */
	protected void setLockScheduleRefresh(boolean b) {
		lockScheduleRefresh = b;
	}

	/**
	 * 判断锁定资源更新
	 * @return 真或者假
	 */
	protected boolean isLockScheduleRefresh() {
		return lockScheduleRefresh;
	}

	/**
	 * 当前节点锁定资源更新
	 */
	private void lockScheduleRefresh() {
		Logger.debug(this, "lockScheduleRefresh", "lock schedule...");
		do {
			boolean locked = ScheduleLock.lock();
			if (locked) {
				break;
			}
			// 没有锁定，延时1秒再试
			delay(1000);
		} while (true);
	}

	/**
	 * 解决锁定
	 */
	private void unlockScheduleRefresh() {
		Logger.debug(this, "unlockScheduleRefresh", "unlock schedule...");
		ScheduleLock.unlock();
	}

	/**
	 * 检测子集许可协议。
	 * 子类如果有需要，重写这个方法
	 * @return 接受返回真，否则假
	 */
	protected boolean checkSubPermission() {
		return true;
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
		
		// 检测子类许可
		boolean success = checkSubPermission();
		if (!success) {
			faultX(FaultTip.COMMAND_REFUSED);
			return false;
		}
		
		// 要求锁定时，锁定资源更新
		if (isLockScheduleRefresh()) {
			lockScheduleRefresh();
		}

		// 执行处理
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = todo();

		// 如果要求退出或者处理不成功时，并且是资源锁定状态时，这时要解决资源更新
		if (isQuit() || !success) {
			if (isLockScheduleRefresh()) {
				// 通知跨越刷新
				if (isSkipScheduleRefresh()) {
					getStaffPool().skipScheduleRefresh();
				}
				// 解除锁定
				unlockScheduleRefresh();
			}
		}
		return success;
	}

	/**
	 * 保存一批事务规则
	 * @param e 事务规则表
	 * @return 返回新保存的成员数目
	 */
	protected int addRules(RuleSheet e) {
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
	 * 返回保存的事务规则
	 * @return 事务规则列表
	 */
	protected List<RuleItem> getRules() {
		return sheet.list();
	}

	/**
	 * 向GATE站点提交故障锁定命令，要求锁定关联资源。<br>
	 * 是否锁定由GATE站点根据用户手工发送的“LimitItem”进行判断，然后决定是否锁定。
	 */
	protected void sendFaultItems() {
		CreateFault cmd = new CreateFault();
		for (RuleItem rule : sheet.list()) {
			cmd.add(rule.createFaultItem());
		}
		// 空集合不处理
		if (cmd.isEmpty()) {
			return;
		}
		// 命令发送到GATE站点
		Node hub = getHub();
		directTo(hub, cmd);
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
				Logger.error(this, "todo", "insure failed!");
				return useful(false);
			}
		}
		// 2. 判断GATE的事务处理结果
		if (stage == SerialStage.CHECK_INSURE) {
			boolean success = checkInsure();
			// 不成功退出和释放资源，否则进入“数据处理”阶段
			if (!success) {
				Logger.error(this, "todo", "check insure failed!");
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
				Logger.error(this, "todo", "revoke failed!");
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
			Logger.error(this, "insure", "rule sheet is empty!");
			faultX(FaultTip.EMPRY_LIST);
			return false;
		}

		// 把事务规则进行分类，找到不同的GATE站点
		tokens = classify(sheet.list());
		if(tokens == null) {
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

		// 只要有一个发送成功，都要返回TRUE。然后等待进入“CHECK INSURE”处理
		boolean success = (count > 0);

		// 提示出错
		if (success) {
			messageX(false, MessageTip.RULE_ATTACH, getInvokerId());
		} else {
			faultX(FaultTip.RULE_ATTACH_FAULT, getInvokerId());
		}

		Logger.debug(this, "insure", success, "send count:%d, command item size:%d", count, array.size());
		return success;
	}

	/**
	 * 检查GATE反馈的建立事务结果
	 * @return 成功返回真，否则假
	 */
	private boolean checkInsure() {
		List<Integer> keys = getEchoKeys();
		// 统计成功
		ArrayList<RuleProduct> array = new ArrayList<RuleProduct>();
		try {
			for (int index : keys) {
				// 忽略非对象
				if (!isObjectable(index)) {
					continue;
				}
				// 取出参数
				RuleProduct product = getObject(RuleProduct.class, index);
				// 只保留投递成功的单元
				if (product.isSuccessful()) {
					array.add(product);
				}
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}

		// 判断存在错误
		boolean success = (array.size() == tokens.size());
		// 如果存在错误，发送撤销命令
		if (!success) {
			ArrayList<CommandItem> items = new ArrayList<CommandItem>();
			// 找到成功的单元，发送DropRule命令
			for (RuleProduct e : array) {
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

		// 失败提示
		if (success) {
			// 打印信息，接受事务处理
			messageX(false, MessageTip.RULE_ATTACH_RETURN_OK, getInvokerId());
		} else {
			faultX(FaultTip.RULE_ATTACH_RETURN_FAULT, getInvokerId());
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
			DetachRule cmd = e.getCommand().reverse();
			CommandItem item = new CommandItem(e.getHub(), cmd);
			array.add(item);
		}

		// 以容错模式发送到不同的GATE站点
		int count = incompleteTo(array);
		// 只有一个发送成功，就是成功
		boolean success = (count > 0);

		Logger.debug(this, "revoke", success,
				"send count:%d, command items size:%d", count, array.size());
		
		// 成功或者故障！
		if (success) {
			messageX(false, MessageTip.RULE_DETACH, getInvokerId());
		} else {
			faultX(FaultTip.RULE_DETACH_FAULT, getInvokerId());
		}

		return success;
	}

	/**
	 * 检查撤销事务申请
	 * @return 撤销成功返回真，否则假。
	 */
	private boolean checkRevoke() {
		List<Integer> keys = getEchoKeys();

		ArrayList<RuleProduct> array = new ArrayList<RuleProduct>();
		for (int index : keys) {
			try {
				// 非对象，忽略它
				if (!isObjectable(index)) {
					continue;
				}
				// 取出参数
				RuleProduct product = getObject(RuleProduct.class, index);
				// 如果撤销成功，保存它
				if (product.isSuccessful()) {
					array.add(product);
				}
			} catch (VisitException e) {
				Logger.fatal(e);
			}
		}
		// 判断成功
		boolean success = (array.size() > 0);

		Logger.debug(this, "checkRevoke", success,
				"product size:%d, rule token size:%d", array.size(), tokens.size());

		// 成功或者故障！
		if (success) {
			messageX(false, MessageTip.RULE_DETACH_RETURN_OK, getInvokerId());
		} else {
			faultX(FaultTip.RULE_DETACH_RETURN_FAULT, getInvokerId());
		}

		// 返回结果
		return success;
	}

	/**
	 * 执行事务阶段中的数据处理 <br>
	 * 这个方法由子类根据各自需求去实现。
	 * 
	 * @return 当子类实现接口决定退出时，返回真（无论数据处理是成功或者失败），否则假。<b>特别说明：数据处理错误也要返回“真”。<b>
	 */
	protected abstract boolean process();

}