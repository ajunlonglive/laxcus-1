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
import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 删除数据表调用器。<br>
 * 删除数据表，以及数据表下的全部表记录。
 * 
 * @author scott.liang
 * @version 1.0 7/06/2018
 * @since laxcus 1.0
 */
public class BankDropTableInvoker extends BankDropUserResourceInvoker {

	/** 操作步骤 **/
	private int step;

	/** ACCOUNT节点地址 **/
	private Node remote;
	
	/**
	 * 构造删除数据表调用器，指定命令
	 * @param cmd 删除数据表命令
	 */
	public BankDropTableInvoker(DropTable cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropTable getCommand() {
		return (DropTable) super.getCommand();
	}
	
	/**
	 * 通知HASH/GATE更新账号
	 */
	private void multicast() {
		TreeSet<Seat> seats = new TreeSet<Seat>();
		seats.add(new Seat(getIssuer(), remote));
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
		Logger.error(this, "discontinue", "发生故障！锁定数据！");
		
		// 保存删除数据库命令到故障管理池
		FaultOnBankPool.getInstance().push(getCommand());
		// 全网广播，更新账号
		multicast();
		// 提前退出
		setQuit(true);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果故障池存在这个表，拒绝操作
		DropTable cmd = getCommand();
		Space space = cmd.getSpace();
		boolean success = FaultOnBankPool.getInstance().hasTable(space);
		if (success) {
			refuse();
			return false;
		}
		
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
	 * 执行操作
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
			success = doFourty();
			break;
		case 5:
			success = doFifthly();
			break;
		}
		// 步骤增1
		step++;

		// 三种情况：发生错误、提前退出、到期退出 
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
	 * 第一步：去HASH站点查找ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		DropTable cmd = getCommand();
		Siger siger = cmd.getIssuer();
		return seekSite(siger);
	}

	/**
	 * 第二步：从HASH站点获得ACCOUNT站点后，向ACCOUNT站点申请自己的账号数据
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		remote = replySite();
		boolean success = (remote != null);
		// 拿到账号，检查这个表有没有被授权人
		if (success) {
			DropTable cmd = getCommand();
			Siger siger = cmd.getIssuer();
			TakeAccount sub = new TakeAccount(siger);
			success = launchTo(remote, sub);
		}
		return success;
	}

	
	/**
	 * 第三步，从ACCOUNT站点获得自己的账号数据，分两个步骤
	 * 1. 解除自己账号中的表与其他账号的记录
	 * 2. 向ACCOUN站点发送删除数据表操作
	 * @return 成功返回真，否则假
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

		boolean success = (account != null);
		// 成功，两个步骤：1.申请授权的表，2.向ACCOUNT站点发出删表命令。
		if (success) {
			DropTable cmd = getCommand();

			// 1. 找到记录，删除关联账号上的授权表
			int count = dropActiveTable(account, cmd.getSpace());
			Logger.info(this, "doThird", "drop active table count: %d", count);
			
			// 发送命令给ACCOUNT站点，删除数据表
			success = launchTo(remote, cmd);
		}
		
		return success;
	}

	/**
	 * 第四步：接收ACCOUNT站点反馈结果。通过BANK站点，分发给TOP站点。
	 * @return 成功返回真，否则假
	 */
	private boolean doFourty() {
		DropTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());
		// 若ACCOUNT删除表不成功，此时可以恢复
		if (!success) {
			return false;
		}

		// 将命令转至TOP，TOP分发到HOME，删除全部关联的数据表记录
		DropTable cmd = getCommand();
		success = launchToHub(cmd);
		
		/** 
		 * 此时不成功，因为数据表已经消失，无法恢复需要做两个动作
		 * 1. 向全部GATE站点，发出RefreshAccount命令，更新本地账号（去掉了被删除的数据表）
		 * 2. 把删除数据表命令只在到故障管理池，后续由软件重新执行删除操作。ASSERT TABLE命令首先检查故障池存在这个表名。
		 */
		if (!success) {
			discontinue();
		}

		// 总是返回真！！！
		return true;
	}

	/**
	 * 接收TOP站点反馈结果，并把结果转发给GATE/FRONT站点
	 * @return 总是返回真！！！
	 */
	private boolean doFifthly() {
		DropTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.isSuccessful());
		if (success) {
			success = replyProduct(product);
			// 通知关联GATE站点，更新自己的账号记录
			multicast(); 
		} else {
			discontinue();
		}

		Logger.note(this, "doFifthly", success, "drop %s",
				getCommand().getSpace());

		return success;
	}

}