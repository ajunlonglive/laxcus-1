/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.cross.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 转发关闭授权调用器。<br>
 * BANK站点将随机选择一个GATE站点，把命令投递给它处理。
 * 
 * @author scott.liang
 * @version 1.0 7/21/2018
 * @since laxcus 1.0
 */
public class BankShiftCloseShareTableInvoker extends BankSeekAccountSiteInvoker {
	
	/** 操作步骤  **/
	private int step;
	
	/** 保存记录集合  **/
	private TreeSet<Seat> seats = new TreeSet<Seat>();

	/**
	 * 构造转发关闭授权调用器，指定命令
	 * @param cmd 转发关闭授权
	 */
	public BankShiftCloseShareTableInvoker(ShiftCloseShareTable cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCloseShareTable getCommand() {
		return (ShiftCloseShareTable) super.getCommand();
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		ShiftCloseShareTable shift = getCommand();
//		CloseShareTable cmd = shift.getCommand();
//
//		// 随机选择一个GATE站点执行解除授权操作
//		Node slave = GateOnBankPool.getInstance().next();
//		boolean success = (slave != null);
//		if (success) {
//			success = launchTo(slave, cmd);
//		}
//		if (!success) {
//			shift.getHook().done();
//		}
//
//		return success;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		ShareCrossProduct product = null;
//		int index = findEchoKey(0);
//		try {
//			if (isSuccessObjectable(index)) {
//				product = getObject(ShareCrossProduct.class, index);
//			}
//		} catch (VisitException e) {
//			Logger.error(e);
//		}
//		// 判断成功
//		boolean success = (product != null);
//		ShiftCloseShareTable shift = getCommand();
//		if (success) {
//			shift.getHook().setResult(product);
//		}
//		shift.getHook().done();
//
//		return useful(success);
//	}
	
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
	 * 失败通知
	 */
	private void faulted() {
		ShiftCloseShareTable shift = getCommand();
		shift.getHook().done();
	}
	
	/**
	 * 通知全部GATE站点，更新某几个账号
	 */
	private void multicast() {
		// 通知全部HASH/GATE站点，理新账号记录。为了简化处理，增加冗余操作
		for (Seat seat : seats) {
			multicast(seat);
		}

		// // 取出用户签名
		// ArrayList<Node> slaves = new ArrayList<Node>();
		// slaves.addAll(GateOnBankPool.getInstance().detail());
		//
		// ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		//
		// // 通知全部GATE站点，理新账号记录。为了简化处理，增加冗余操作
		// for (Seat seat : seats) {
		// Node remote = seat.getLocal();
		// Siger siger = seat.getSiger();
		// RefreshAccount cmd = new RefreshAccount(remote, siger);
		// for (Node node : slaves) {
		// CommandItem item = new CommandItem(node, cmd);
		// array.add(item);
		// }
		// }
		//
		// Logger.debug(this, "multicast", "command items: %d", array.size());
		//
		// // 直投，不用反馈结果
		// directTo(array, false);
	}

	/**
	 * 按照顺序依次执行
	 * @return 成功返回真，否则假
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
		}
		// 完成后，自增1
		step++;
		
		// 2种情况：发生错误、到期退出
		if (!success || step > 3) {
			if (!success) {
				faulted();
			}
			// 通知线程退出
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 第一步，去HASH站点检索关联的ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		ShiftCloseShareTable shift = getCommand();
		CloseShareTable cmd = shift.getCommand();
		List<Siger> sigers = cmd.getConferrers();
		return seekSites(sigers, false);
	}
	
	/**
	 * 第二步：收到HASH反馈结果，筛选，发送命令给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		List<Seat> a = replySites();
		// 保存全部
		seats.addAll(a);
		
		ShiftCloseShareTable shift = getCommand();
		CloseShareTable cmd = shift.getCommand();

		// 筛选比较，生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Seat seat : seats) {
			Node remote = seat.getSite();
			Siger siger = seat.getSiger();

			// 包含这个用户签名
			if (cmd.getConferrers().contains(siger)) {
				CloseShareTable sub = new CloseShareTable();
				sub.addSpaces(cmd.getSpaces());
				sub.addConferrer(siger);
				sub.setOperator(cmd.getOperator());

				CommandItem item = new CommandItem(remote, sub);
				array.add(item);
			}
		}
		
		// 判断有效
		boolean success = (array.size() > 0);
		// 发送给ACCOUNT站点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
		}

		return success;
	}
	
	/**
	 * 第三步，接收全部ACCOUNT反馈结果
	 * @return 总是返回真
	 */
	private boolean doThird() {
		ShareCrossProduct product = new ShareCrossProduct();
		// 保存
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				// 不成功，忽略它
				if (!isSuccessObjectable(index)) {
					Logger.error(this, "doThird", "index %d failed!", index);
					continue;
				}
				// 保存全部单元
				ShareCrossProduct e = getObject(ShareCrossProduct.class, index);
				product.addAll(e);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		Logger.debug(this, "doThird", "delete items: %d", product.size());

		// 判断成功
		boolean success = (product.size() > 0);
		// 通知命令钩子
		ShiftCloseShareTable shift = getCommand();
		CloseShareTableHook hook = shift.getHook();
		if (success) {
			hook.setResult(product);
		}
		hook.done();
		
		// 广播站点
		multicast();

		// 结束，总是返回“真！”
		return true;
	}

}