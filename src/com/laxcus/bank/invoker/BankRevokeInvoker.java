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
import com.laxcus.command.access.permit.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 解除授权调用器。<br>
 * 只有管理员或者拥有解除授权操作的用户才能执行这个操作。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class BankRevokeInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/**
	 * 构造解除授权调用器，指定命令
	 * 
	 * @param cmd 解除授权命令
	 */
	public BankRevokeInvoker(Revoke cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Revoke getCommand() {
		return (Revoke) super.getCommand();
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
	 * 执行处理步骤
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
		step++;

		// 判断处理结果，执行对应操作
		if (!success || step > 3) {
			if (!success) {
				refuse();
			}
			setQuit(true);
		}
		return success;
	}

	/**
	 * 第一步：去HASH站点查询关联的ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		Revoke cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();

		// 通过HASH站点检索全部ACCOUNT站点，以“完整”模式发送，并且全部成功
		return seekSites(sigers, true);
	}

	/**
	 * 从HASH站点接收查询的ACCOUNT站点地址结果，生成新命令，发送给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		Revoke cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();

		// 取回全部账号的ACCOUNT站点位置
		List<Seat> seats = replySites();

		// 判断成功
		boolean success = (seats.size() == sigers.size());
		if (!success) {
			return false;
		}
		
		Permit permit = cmd.getPermit();
		// 生成子级命令
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Seat seat : seats) {
			SingleRevoke sub = new SingleRevoke();
			sub.setPermit(permit);
			sub.setSiger(seat.getSiger());
			// 生成命令单元
			CommandItem item = new CommandItem(seat.getSite(), sub);
			array.add(item);
		}
		
		// 发送到不同的ACCOUNT站点
		if (success) {
			success = completeTo(array);
		}
		return success;
	}

	/**
	 * 第三步，接收ACCOUNT处理结果，返回给FRONT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		ArrayList<Seat> seats  = new ArrayList<Seat>();
		
		CertificateProduct product = new CertificateProduct();
		// 接收ACCOUNT站点反馈参数
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					SignleCertificateProduct e = getObject(SignleCertificateProduct.class, index);
					// 判断成功或者失败
					if (e.isSuccessful()) {
						product.addIssuer(e.getSeat().getSiger());
						seats.add(e.getSeat());
					} else {
						product.addIneffect(e.getSeat().getSiger());
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		
		// 向全网广播
		for(Seat seat : seats) {
			multicast(seat);
			refreshRefer(seat);
		}
		
		// 反馈最后的处理结果
		return replyProduct(product);
	}

}