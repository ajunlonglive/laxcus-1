/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.limit.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * INSERT命令调用器。<br><br>
 * 
 * 提供了事务控制能力。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopInsertInvoker extends DesktopRuleInvoker {

	/** 数据插入步骤，从1开始 **/
	private int step = 1;

	/**
	 * 构造INSERT命令调用器，指定数据插入命令。
	 * @param cmd INSERT命令实例。
	 */
	public DesktopInsertInvoker(Insert cmd) {
		super(cmd);
		// 建立表规则
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Insert getCommand() {
		return (Insert) super.getCommand();
	}

	/**
	 * 建立表规则
	 */
	private void initRule() {
		Insert cmd = getCommand();
		// 保存INSERT的“共享写”事务规则
		addRules(cmd.getRules());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DesktopRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch(step) {
		case 1:
			success = attempt();
			break;
		case 2:
			success = send();
			break;
		case 3:
			success = receive();
			break;
		}
		// 自增1
		step++;

		// 以上不成功，提交锁定报告到AID站点
		if (!success) {
			sendFaultItem();
		}

		// 大于3是完成，否则是没有完成
		return (!success || step > 3);
	}

	/**
	 * 向AID站点提交故障报告，要求锁定相关表
	 */
	private void sendFaultItem() {
		TableFaultItem item = new TableFaultItem(getCommand().getSpace());
		CreateFault cmd = new CreateFault(item);
		Node hub = getHub();
		directTo(hub, cmd);
	}

	/**
	 * 数据写入第一步：<br>
	 * 1. 从CALL注册站点池中枚举一个地址。<br>
	 * 2. 尝试向它发送一个写入标识。<br><br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean attempt() {
		Insert cmd = getCommand();
		Space space = cmd.getSpace();
		// 根据数据表名，找一个地址
		NodeSet set = getStaffPool().findTableSites(space);

		// 枚举一个站点地址
		Node hub = (set != null ? set.next() : null);
		boolean success = (hub != null);
		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.SITE_MISSING);
			}
			return false;
		}

		// 向CALL站点发送确认命令
		InsertGuide guide = new InsertGuide(space);
		success = fireToHub(hub, guide);

		// 不成功时...
		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			}
		}

		Logger.debug(this, "attempt", success, "submit to %s", hub);

		return success;
	}

	/**
	 * 数据写入第二步：<br>
	 * 1. 接收CALL站点反馈。<br>
	 * 2. 如果被接受，向CALL站点发送INSERT命令，等待结果。<br>
	 * @return 成功返回真，否则假。
	 */
	private boolean send() {
		// 1. 确认应答
		int index = findEchoKey(0);
		InsertGuide guide = null;
		try {
			if (isSuccessObjectable(index)) {
				guide = getObject(InsertGuide.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 2. 出错或者拒绝
		boolean success = (guide != null);
		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.FAILED_X, getCommand());
			}
			return false;
		}

		// 3. 传输过程中不做封装，INSERT以原始数据格式发送到CALL站点，CALL站点原样转发到DATA主站点
		Cabin hub = guide.getSource();
		Insert cmd = getCommand();
		ReplyItem item = new ReplyItem(hub, cmd.build());
		success = replyTo(item);

		// 不成功提示
		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.CANNOT_SUBMIT_X, hub);
			}
		}

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 数据写入第三步：等待上传的反馈结果。
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		// 有且只有一个索引号
		int index = findEchoKey(0);
		// 取对象
		InsertProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(InsertProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
		}
		// 出错或者拒绝
		boolean success = (product != null);

		ProductListener listener = getProductListener();

		// 显示插入的记录
		if (success) {
			if (listener != null) {
				listener.push(product); // 把结果放入接收器中
			} else {
				print(product);
			}
		} else {
			if (listener != null) {
				listener.push(null);
			} else {
				Insert cmd = getCommand();
				faultX(FaultTip.INSERT_FAILED_X, cmd.getSpace());
			}
		}
		// 工作完成，是否完成退出由上层事务决定！！！
		return success;
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(InsertProduct product) {
		// 显示运行时间
		printRuntime();

		// 设置标题
		createShowTitle(new String[] { "INSERT/RESULT", "INSERT/SPACE", "INSERT/ROWS" });

		ShowItem item = new ShowItem();

		long rows = product.getRows();

		// 图标
		item.add(createConfirmTableCell(0, product.isSuccessful()));
		// 表名
		item.add(new ShowStringCell(1, product.getSpace()));
		// 插入行数
		item.add(new ShowLongCell(2, rows));
		// 显示
		addShowItem(item);

		// 输出全部记录
		flushTable();

		// 在状态栏显示行数
		if (rows > 0) {
			printRows(rows);
		}
	}

	/**
	 * 显示检索行数
	 * @param rows
	 */
	private void printRows(long rows) {
		String prefix = getXMLContent("SQL/INSERT-X");
		String text = String.format(prefix, rows);
		setStatusText(text);
	}

}