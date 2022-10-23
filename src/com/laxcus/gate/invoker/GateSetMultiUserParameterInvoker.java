/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 设置多用户参数调用器
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class GateSetMultiUserParameterInvoker extends GateSeekAccountSiteInvoker {
	
	/** 操作步骤，从1开始 **/
	private int step;
	
	/** 多个节点地址 **/
	private ArrayList<Seat> seats = new ArrayList<Seat>();

	/**
	 * 构造设置多用户参数调用器，指定命令
	 * @param cmd 设置多用户参数
	 */
	protected GateSetMultiUserParameterInvoker(SetMultiUserParameter cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMultiUserParameter getCommand() {
		return (SetMultiUserParameter) super.getCommand();
	}
	
	/**
	 * 分割成单用户命令
	 * @return
	 */
	private List<SetSingleUserParameter> split() {
		return getCommand().split();
	}
	
	/**
	 * 输出全部用户签名
	 * @return
	 */
	private List<Siger> getSigers() {
		ArrayList<Siger> a = new ArrayList<Siger>();
		for(SetSingleUserParameter e : split()) {
			a.add(e.getSiger());
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 没有DBA权限，不能设置
		if (!canDBA()) {
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
	 * 执行操作步骤
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
		if (!success || step > 3) {
			if (!success) {
				failed();
			}
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 第一步，去HASH站点找到全部签名的ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		List<Siger> sigers = getSigers();
		return seekSites(sigers, false);
	}
	
	/**
	 * 第二步：把参数投递到指定的ACCOUN站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		List<Seat> a = replySites();
		seats.addAll(a);

		Logger.debug(this, "doSecond", "siger seat size:%d", seats.size());

		// 判断成功
		boolean success = (seats.size() > 0);
		if (!success) {
			return false;
		}
		
		List<SetSingleUserParameter> cmds = split();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 找到签名匹配的命令，保存它
		for (Seat seat : seats) {
			for (SetSingleUserParameter e : cmds) {
				if (Laxkit.compareTo(seat.getSiger(), e.getSiger()) == 0) {
					Node account = seat.getSite();
					CommandItem item = new CommandItem(account, e);
					array.add(item);
					break;
				}
			}
		}

		// 以容错模式，把命令投递到ACCOUNT站点
		int count = incompleteTo(array);
		success = (count > 0);

		return success;
	}

	/**
	 * 第三步：接收多个ACCOUNT站点的反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		SetMultiUserParameterProduct product = new SetMultiUserParameterProduct();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					RefreshItem item = getObject(RefreshItem.class, index);
					product.add(item);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功
		boolean success = (product != null);
		if (!success) {
			return false;
		}
		
		ArrayList<Command> array = new ArrayList<Command>();
		for (RefreshItem item : product.list()) {
			// 不成功，忽略它
			if (!item.isSuccessful()) {
				continue;
			}
			for (Seat seat : seats) {
				if (Laxkit.compareTo(item.getSiger(), seat.getSiger()) == 0) {
					// BANK集群更新账号
					RefreshAccount cmd = new RefreshAccount(seat);
					array.add(cmd);
					// 通过BANK集群，向TOP/HOME集群更新资源引用
					RefreshRefer sub = new RefreshRefer(seat);
					array.add(sub);
				}
			}
		}

		// 投递给调用器
		if (array.size() > 0) {
			directToHub(array);
		}
		
		Logger.debug(this, "doThird", success, "item size:%d", product.size());
		
		// 反馈报告
		replyProduct(product);
		return success;
	}

}