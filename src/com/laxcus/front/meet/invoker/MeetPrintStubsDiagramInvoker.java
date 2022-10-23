/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 获得数据块编号命令异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/10/2018
 * @since laxcus 1.0
 */
public class MeetPrintStubsDiagramInvoker extends MeetRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造获得数据块编号命令异步调用器，指定获得数据块编号命令
	 * @param cmd 获得数据块编号命令
	 */
	public MeetPrintStubsDiagramInvoker(PrintStubsDiagram cmd) {
		super(cmd);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintStubsDiagram getCommand() {
		return (PrintStubsDiagram) super.getCommand();
	}

	/**
	 * 初始化事务规则
	 */
	private void initRule() {
		PrintStubsDiagram cmd = getCommand();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
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
		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 处理第一阶段
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 检查注册用户有删除的权限
		PrintStubsDiagram cmd = getCommand();
		Space space = cmd.getSpace();
		// 1. 判断数据库存在
		boolean success = false;
		try {
			success = getStaffPool().hasTable(space);
		} catch (ResourceException e) {
			Logger.error(e);
		}
		if (!success) {
			faultX(FaultTip.NOTFOUND_X, space);
			return false;
		}
		// 2. 判断拥有获得数据块编号权限
		success = getStaffPool().canTable(space, ControlTag.SELECT);
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}

		// 找到CALL站点
		NodeSet set = getStaffPool().findTableSites(space);
		// 顺序枚举一个CALL站点地址，保持调用平衡
		Node hub = (set != null ? set.next() : null);
		// 没有找到，弹出错误
		if (hub == null) {
			faultX(FaultTip.NOTFOUND_SITE_X, space);
			return false;
		}

		// 发送到目标地址
		success = fireToHub(hub, cmd);

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 处理第二阶段
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		// 判断处理结果
		PrintStubsDiagramProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(PrintStubsDiagramProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null);

		if (success) {
			print(product);
		} else {
			PrintStubsDiagram cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd);
		}

		// 返回结果
		return success;
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(PrintStubsDiagramProduct product){
		// 显示处理时间
		printRuntime();

		// 生成表格标题
		createShowTitle(new String[] { "PRINT-STUBS-DIAGRAM/T1", "PRINT-STUBS-DIAGRAM/T2" });

		int index = 0;
		for (PrintStubsDiagramItem item : product.list()) {
			if (index > 0) {
				printGap(2);
			}
			print(item);
			index++;
		}

		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 返回信息
	 * @param cell
	 * @return
	 */
	private String getInformation(PrintStubsDiagramCell cell) {
		String suffix = ConfigParser.splitCapacity(cell.getLength());
		return String.format("%X (%s)", cell.getStub(), suffix);
	}

	/**
	 * 打印结果
	 * @param diagram
	 */
	private void print(PrintStubsDiagramItem diagram) {
		// 运行中的事务规则
		String site = findXMLTitle("PRINT-STUBS-DIAGRAM/SITE");
		String cache = getXMLContent("PRINT-STUBS-DIAGRAM/STATUS/CACHE");
		String reflex = getXMLContent("PRINT-STUBS-DIAGRAM/STATUS/CACHE-REFLEX");
		String chunk = getXMLContent("PRINT-STUBS-DIAGRAM/STATUS/CHUNK");

		ShowItem root = new ShowItem();
		root.add(new ShowStringCell(0, site));
		root.add(new ShowStringCell(1, diagram.getSite().toString()));
		addShowItem(root);

		// 缓存块
		for (PrintStubsDiagramCell cell : diagram.getCacheStubs()) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, cache));
			item.add(new ShowStringCell(1, getInformation(cell)));
			addShowItem(item);
		}
		// 缓存块
		for (PrintStubsDiagramCell cell : diagram.getReflexStubs()) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, reflex));
			item.add(new ShowStringCell(1, getInformation(cell)));
			addShowItem(item);
		}
		// 存储块
		for (PrintStubsDiagramCell cell : diagram.getChunkStubs()) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, chunk));
			item.add(new ShowStringCell(1, getInformation(cell)));
			addShowItem(item);
		}

	}

}