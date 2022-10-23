/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 建立数据表调用器。
 * 并行建立数据表，即允许同时产生。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class BankCreateTableInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/** ACCOUNT站点地址 **/
	private Node remote;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		if (remote != null) {
			remote = null;
		}
	}

	/**
	 * 构造建立数据表命令，指定转发命令
	 * @param cmd 建立数据表命令
	 */
	public BankCreateTableInvoker(CreateTable cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateTable getCommand() {
		return (CreateTable) super.getCommand();
	}
	
	/**
	 * 检查表参数
	 * @param table 数据表
	 */
	private void check(Table table) {
		// 检查DSM表压缩倍数，超过3则修改
		if (table.isDSM() && table.getMultiple() > 3) {
			table.setMultiple(3);
		}
	}
	
	/**
	 * 判断数据表存在故障池中
	 * @return 返回真或者假
	 */
	private boolean isFaultTable() {
		// 如果表名在故障池中，不允许建立或者删除
		CreateTable cmd = getCommand();
		Space space = cmd.getTable().getSpace();
		return FaultOnBankPool.getInstance().hasTable(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = isFaultTable();
		if (success) {
			refuse();
			return false;
		}

		// 检查表参数合理性
		CreateTable cmd = getCommand();
		check(cmd.getTable());

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
	 * 通知全部GATE站点，刷新关联的CALL站点
	 */
	private void refreshCall() {
		Siger siger = getCommand().getIssuer();
		RefreshUser cmd = new RefreshUser(siger);
		List<Node> slaves = GateOnBankPool.getInstance().detail();
		directTo(slaves, cmd);
	}

	/**
	 * 通知GATE站点，刷新某个账号，没有这个账号则忽略
	 */
	private void multicast() {
		// 取出用户签名
		Siger siger = getIssuer();
		ArrayList<Node> slaves = new ArrayList<Node>();
		slaves.addAll(GateOnBankPool.getInstance().detail());

		// 通知全部GATE站点，理新账号记录。为了简化处理，增加冗余操作
		RefreshAccount cmd = new RefreshAccount(remote, siger);
		directTo(slaves, cmd);
	}

	/**
	 * 操作步骤
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
		case 4:
			success = doFourly();
			break;
		}
		// 步骤增1
		step++;
		
		// 以上不成功或者完成时，退出
		if (!success || step > 4) {
			// 如果不成功，通知GATE站点
			if (success) {
				multicast();
				refreshCall();
			} else {
				failed();
			}
			// 退出
			setQuit(true);
		}
		// 返回结果
		return success;
	}

	/**
	 * 从HASH站点中查出账号所在节点，并且发送给这个HASH节点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger siger = getIssuer();
		return seekSite(siger);
	}

	/**
	 * 接收HASH站点返回的ACCOUNT站点地址，发送命令给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		remote = replySite(); // 找到ACCOUNT站点
		boolean success = (remote != null);
		if (success) {
			CreateTable cmd = getCommand();
			success = launchTo(remote, cmd);
		}
		return success;
	}

	/**
	 * 接收ACCOUNT站点反馈结果，并把结果反馈给GATE站点
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		// 判断处理结果
		CreateTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());
		// 不成立，反馈错误
		if(!success) {
			return false;
		}

		// 建表命令投递给TOP站点
		success = launchToHub();
		// 不成功，把命令放进故障管理池
		if (!success) {
			boolean released = cancel();
			if (!released) {
				FaultOnBankPool.getInstance().push(getCommand());
			}
		}
		
		return success;
	}

	/**
	 * 接收TOP的反馈
	 * @return 成功返回真，否则假
	 */
	private boolean doFourly() {
		// 判断处理结果
		CreateTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		// 不成功，尝试删除数据表记录。如果仍然失败，放在故障管理池
		if (success) {
			replyProduct(product);
		} else {
			boolean released = cancel();
			if (!released) {
				FaultOnBankPool.getInstance().push(getCommand());
			}
			
			// 生成错误报告反馈给GATE节点
			CreateTable cmd = getCommand();
			CreateTableProduct sub = new CreateTableProduct(cmd.getSpace(), false);
			replyProduct(sub);
		}

		return success;
	}
	
	/**
	 * 如果账号建立不成功，删除ACCOUNT上记录
	 */
	private boolean cancel() {
		Space space = getCommand().getSpace();
		DropTable sub = new DropTable(space);		
		DropTableHook hook = new DropTableHook();
		ShiftDropTable shift = new ShiftDropTable(remote, sub, hook);
		shift.setIssuer(getIssuer()); // 签名人

		// 直接处理
		boolean success = getCommandPool().press(shift);
		if (!success) {
			return false;
		}
		// 等待
		hook.await();
		// 反馈结果
		DropTableProduct product = hook.getProduct();
		success = (product != null && product.isSuccessful());

		Logger.debug(this, "cancel", success, "from %s", remote);
		
		return success;
	}
}