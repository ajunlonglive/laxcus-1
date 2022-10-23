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
import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 删除账号调用器。<br>
 * 删除账号，以及账号下的全部表和数据库记录。这个命令只能由管理员来操作。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class BankDropUserInvoker extends BankDropUserResourceInvoker {

	/** 操作步骤 **/
	private int step;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 构造删除账号调用器，指定命令
	 * @param cmd 删除账号命令
	 */
	public BankDropUserInvoker(DropUser cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/**
	 * 通知全部HASH/GATE站点，释放一个账号
	 */
	private void breakoff() {
		List<Node> slaves = new ArrayList<Node>();
		slaves.addAll(HashOnBankPool.getInstance().detail());
		slaves.addAll(GateOnBankPool.getInstance().detail());
		Siger siger = getCommand().getUsername();
		AwardDropRefer award = new AwardDropRefer(siger);
		directTo(slaves, award);
	}

	/**
	 * 更新被授权人记录
	 */
	private void multicastConfferrers() {
		TreeSet<Seat> seats = new TreeSet<Seat>();
		// 被授权人
		for (Seat e : dropConferrers) {
			seats.add(new Seat(e.getSiger(), e.getSite()));
		}
		// 投递到目标地址
		multicast(seats);
	}

	/**
	 * 如果ACCOUNT站点的数据库已经删除，但是后续工作没有正确完成，这时就中断执行。
	 */
	private void discontinue() {
		Logger.error(this, "discontinue", "发生故障！");

		// 保存删除数据库命令到故障管理池
		FaultOnBankPool.getInstance().push(getCommand());
		// 全网广播，更新账号
		breakoff();
		// 提前退出
		setQuit(true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
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
	 * 删除操作
	 * @return
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		case 4:
			success = doFourty();
			break;
		case 5:
			success = doFifthly();
			break;
		}
		// 步骤增1
		step++;

		if (!success || isQuit() || step > 5) {
			if (!success) {
				failed();
			}
			// 通知线程退出
			setQuit(true);
		}
		return success;
	}

	/**
	 * 去HASH站点查找ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		DropUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		return seekSite(siger);
	}

	//	/**
	//	 * 从HASH站点获得ACCOUNT站点，把命令发给ACCOUNT站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doSecond1() {
	//		Node account = replySite();
	//		// 判断ACCOUNT站点有效
	//		boolean success = (account != null);
	//		if (success) {
	//			DropUser cmd = getCommand();
	//			success = launchTo(account, cmd);
	//		}
	//		return success;
	//	}


	/**
	 * 第二步：从HASH站点获得ACCOUNT站点，去ACCOUNT站点去申请账号实例
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		remote = replySite();
		// 判断ACCOUNT站点有效
		boolean success = (remote != null);
		if (success) {
			DropUser cmd = getCommand();
			TakeAccount sub = new TakeAccount(cmd.getUsername());
			success = launchTo(remote, sub);
		}
		return success;
	}
	
//	/**
//	 * 删除用户自己授权的所有表
//	 * @param account 账号
//	 * @return 返回解除授权人成功的数目
//	 */
//	private int dropActiveTables(Account account) {
//		// 检查
//		List<ActiveItem> items = account.getActiveItems();
//		if (items.isEmpty()) {
//			return 0;
//		}
//
//		int count = 0;
//		// 逐一检查，去ACCOUNT站点删除
//		for (ActiveItem item : items) {
//			// 生成命令，不用指定表名，默认就是全部
//			CloseShareTable cmd = new CloseShareTable();
//			cmd.setOperator(item.getOperator());
//			cmd.addSpace(item.getSpace());
//			cmd.addConferrer(item.getConferrer());
//
//			// 生成命令钩子和转发命令
//			CloseShareTableHook hook = new CloseShareTableHook();
//			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
//			shift.setIssuer(account.getUsername()); // 确定执行人
//			shift.setFast(true); // 跳过检查，快速处理
//
//			// 交给命令管理池，快速处理！
//			boolean	success = getCommandPool().admit(shift);
//			if (success) {
//				hook.await();
//			}
//
//			// 返回报告
//			ShareCrossProduct product = hook.getProduct();
//			// 判断成功
//			success = (product != null && product.size() > 0);
//			if (success) {
//				count++;
//			}
//
//			Logger.note(this, "dropActiveTables", success, "drop %s", item.getConferrer());
//		}
//		// 返回结果
//		return count;
//	}
//	
//	/**
//	 * 以授权人身份，向被授权人的账号发送解除授权表的命令
//	 * @param account 账号
//	 * @return 返回解除授权的成功数目
//	 */
//	private int dropPassiveTables(Account account) {
//		List<PassiveItem> items = account.getPassiveItems();
//		if (items.isEmpty()) {
//			return 0;
//		}
//
//		int count = 0;
//		// 逐一删除
//		for(PassiveItem item : items) {
//			// 生成命令，不用指定表名，默认就是全部
//			CloseShareTable cmd = new CloseShareTable();
//			cmd.setOperator(item.getOperator());
//			cmd.addSpace(item.getSpace());
//			cmd.addConferrer(account.getUsername()); // 被授权人是账号持有人
//
//			// 生成命令钩子和转发命令
//			CloseShareTableHook hook = new CloseShareTableHook();
//			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
//			shift.setIssuer( item.getAuthorizer() ); // 确定授权人
//			shift.setFast(true); // 跳过检查，快速处理
//
//			// 交给命令管理池，快速处理！
//			boolean	success = getCommandPool().admit(shift);
//			if (success) {
//				hook.await();
//			}
//
//			// 返回报告
//			ShareCrossProduct product = hook.getProduct();
//			// 判断成功
//			success = (product != null && product.size() > 0);
//			if (success) {
//				count++;
//			}
//
//			Logger.note(this, "dropPassiveTables", success, "drop %s", item.getAuthorizer());
//		}
//
//		return count;
//	}

	/**
	 * 第三步：从ACCOUNT站点拿到账号，检查是否存在被授权人，并且删除这些被授权人。然后删除ACCOUNT站点上的账号
	 * @return 成功返回真，否则假。
	 */
	private boolean doThird() {
		Account account = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				TakeAccountProduct product = getObject(TakeAccountProduct.class, index);
				account = product.getAccount();
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		// 判断成功
		boolean success = (account != null);
		// 授权表和被授权表，一并删除
		if (success) {
			int count = dropActiveTables(account);
			Logger.debug(this, "doThird", "drop active tables: %d", count);
			count = dropPassiveTables(account);
			Logger.debug(this, "doThird", "drop passive tables: %d", count);

			// 去ACCOUNT站点删除账号
			DropUser cmd = getCommand();
			success = launchTo(remote, cmd);
		}
		
		return success;

//		boolean success = (account != null);
//		// 此时不成功，尚可以恢复
//		if (!success) {
//			return false;
//		}
//
//		// 检查有被授权人参数
//		List<Siger> conferrers = account.getActiveConferrers();
//		if (conferrers.size() > 0) {
//			// 生成命令，不用指定表名，默认就是全部
//			CloseShareTable cmd = new CloseShareTable();
//			cmd.setOperator(CrossOperator.ALL);
//			cmd.addConferrers(conferrers);
//
//			CloseShareTableHook hook = new CloseShareTableHook();
//			ShiftCloseShareTable shift = new ShiftCloseShareTable(cmd, hook);
//			shift.setIssuer(account.getUsername()); // 确定执行人
//			shift.setFast(true); // 跳过检查，快速处理
//			
//			success = getCommandPool().admit(shift);
//			if (success) {
//				hook.await();
//			}
//
//			/*
//			 * 操作步骤
//1. 以授权人的身份解除账号中的全部授权
//2. 读取反馈结果，向被授权人账号发出解除授权的命令
//3. 接收被授权人发出的反馈结果
//			 */
//		}
//
//		// 在此执行转发命令，删除ACCOUNT站点上的被授权人的授权资源
//
//		Logger.debug(this, "doThird", "conferrer size:%d", conferrers.size());
//
//		// 去ACCOUNT站点删除账号
//		DropUser cmd = getCommand();
//		return launchTo(remote, cmd);
	}

	/**
	 * 第四步：接收ACCOUNT站点反馈结果。通过BANK站点，分发给TOP站点。
	 * @return 成功返回真，否则假
	 */
	private boolean doFourty() {
		DropUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());
		// 此时不成功，尚可以恢复
		if (!success) {
			return false;
		}

		// 将命令提交TOP站点，删除下属HOME集群下与签名有关的记录
		DropUser cmd = getCommand();
		success = launchToHub(cmd);

		/** 
		 * 此时不成功，因为账号已经消失，无法恢复需要做两个动作
		 * 1. 向全部GATE/HASH站点，发出AwardDropRefer命令
		 * 2. 把被删除的账号签名留在磁盘上，备以后删除。在此期间，ASSERT USER命令将自动忽略这个命令
		 */
		if (!success) {
			discontinue();
		}

		return true;
	}

	/**
	 * 第五步：接收TOP站点反馈结果，并把结果转发给GATE/FRONT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFifthly() {
		DropUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.isSuccessful());
		// 反馈结果
		if (success) {
			// 反馈结果给GATE，GATE再转发给FRONT
			success = replyProduct(product);
			// 通知GATE/HASH，删除账号，在发送结果之后进行
			breakoff();
			// 更新被授权人记录
			multicastConfferrers();
		} else {
			discontinue();
		}

		Logger.note(this, "doFifthly", success, "drop %s", getCommand().getUsername());

		return success;
	}

}