/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 数据块快速处理命令调用器。<br>
 * 执行数据块的“索引/数据”的“加载/卸载”操作。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopFastMassInvoker extends DesktopRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造数据块快速处理命令调用器，指定命令
	 * @param cmd 数据块命令
	 */
	protected DesktopFastMassInvoker(FastMass cmd) {
		super(cmd);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FastMass getCommand() {
		return (FastMass) super.getCommand();
	}

	/**
	 * 定义事务处理规则
	 */
	private void initRule() {
		FastMass cmd = getCommand();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		// 保存事务规则
		super.addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DesktopRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}
		// 自增1
		step++;
		// 不成功退出，或者超过2是完成退出
		return (!success || step > 2);
	}

	/**
	 * 发送命令到目标站点
	 * @return 发送成功返回真，否则假
	 */
	private boolean send() {
		FastMass cmd = getCommand();
		Space space = cmd.getSpace();
		// 1. 找到一个CALL站点，把命令发给它。
		NodeSet set = getStaffPool().findTableSites(space);
		// 顺序选择，保持调用站点的分配平衡
		Node hub = (set != null ? set.next() : null);
		// 无地址
		if (hub == null) {
			faultX(FaultTip.SITE_MISSING);
			return false;
		}
		// 发送命令到服务器
		boolean success = completeTo(hub, cmd);
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}
		return success;
	}

	/**
	 * 接收数据
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		FastMassProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(FastMassProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
		}
		
		// 判断结果
		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		// 工作完成，返回真或者假（是否退出由上层事务规则决定！！！）
		return success;
	}
	
	/**
	 * 打印结果
	 * @param array FastMassItem数组
	 */
	private void print(List<FastMassItem> array) {
		// 建立标题
		createShowTitle(new String[] { "FASTMASS/STATUS", "FASTMASS/SITE" });

		for(FastMassItem e : array) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			// 显示
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

}