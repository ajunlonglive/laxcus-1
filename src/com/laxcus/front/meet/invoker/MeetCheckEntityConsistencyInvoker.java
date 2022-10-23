/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.command.access.table.*;
import com.laxcus.law.forbid.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检查表数据一致性调用器。<br>
 * 
 * 此命令由FRONT站点发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class MeetCheckEntityConsistencyInvoker extends MeetForbidInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造检查表数据一致性调用器，指定命令
	 * @param cmd 检查表数据一致性命令
	 */
	public MeetCheckEntityConsistencyInvoker(CheckEntityConsistency cmd) {
		super(cmd);
		initForbid();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckEntityConsistency getCommand() {
		return (CheckEntityConsistency) super.getCommand();
	}

	/**
	 * 初始化禁止操作单元
	 */
	private void initForbid() {
		CheckEntityConsistency cmd = getCommand();
		// 表级禁止操作单元
		TableForbidItem item = new TableForbidItem(cmd.getSpace());
		addForbidItem(item);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetForbidInvoker#process()
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
		// 不成功或者完成，退出！
		return (!success || step > 2);
	}

	/**
	 * 发送数据一致性检查性命令到任意一个CALL站点
	 * @return 命令发送成功返回真，否则假
	 */
	private boolean send() {
		CheckEntityConsistency cmd = getCommand();
		Space space = cmd.getSpace();

		NodeSet set = getStaffPool().findTableSites(space);
		Node hub = (set != null ? set.next() : null);
		// 没有站点
		if (hub == null) {
			faultX(FaultTip.SITE_MISSING);
			return false;
		}
		// 发送到指定的CALL站点
		return fireToHub(hub, cmd);
	}

	/**
	 * 第二阶段。接收反馈结果。
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		int index = findEchoKey(0);
		CheckEntityConsistencyProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CheckEntityConsistencyProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);

		// 成功，显示处理结果
		if (success) {
			CheckEntityConsistency cmd = getCommand();
			if (cmd.isDetail()) {
				detail(product);
			} else {
				print(product);
			}
		} else {
			faultX(FaultTip.IMPLEMENT_FAULT); // 执行失败
		}
		// 退出
		return success;
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(CheckEntityConsistencyProduct product) {
		// 显示时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[]{"CHECK-ENTITY-CONSISTENCY/TABLE-NAME","CHECK-ENTITY-CONSISTENCY/TOTAL-CHUNKS",
				"CHECK-ENTITY-CONSISTENCY/VALID-CHUNKS","CHECK-ENTITY-CONSISTENCY/RATE"});

		String rate = ConfigParser.splitRate(product.getValidates(), product.getStubs(), 3);

		// 显示单元
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, product.getSpace()));
		item.add(new ShowLongCell(1, product.getStubs()));
		item.add(new ShowLongCell(2, product.getValidates()));
		item.add(new ShowStringCell(3, rate));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 输出标题
	 * @return 字符串数组
	 */
	private String[] getDetailTitleCells() {
		return new String[]{"CHECK-ENTITY-CONSISTENCY/DETAIL/T1","CHECK-ENTITY-CONSISTENCY/DETAIL/T2",
				"CHECK-ENTITY-CONSISTENCY/DETAIL/T3","CHECK-ENTITY-CONSISTENCY/DETAIL/T4"};
	}

	/**
	 * 统计列单元
	 * @return
	 */
	private int getDetailTitleColumnsCount(){
		return getDetailTitleCells().length;
	}
	
	/**
	 * 生成标题单元
	 */
	private void printDetailTitle() {
		// 生成表格标题
		String[] cells = getDetailTitleCells();
		createShowTitle(cells);
	}
	
	/**
	 * 打印空行
	 */
	private ShowItem printDetailGap() {
		int count = getDetailTitleColumnsCount();
		ShowItem item = new ShowItem();
		for (int i = 0; i < count; i++) {
			ShowStringCell e = new ShowStringCell(i, "  ");
			item.add(e);
		}
		return item;
	}
	
	/**
	 * 选择块状态
	 * @param sign 数据块
	 * @return 返回数据块的字符串描述
	 */
	private String choice(StubSign sign) {
		if (sign.isChunk()) {
			return getXMLContent("CHECK-ENTITY-CONSISTENCY/DETAIL/STATUS/CHUNK");
		} else if (sign.isCache()) {
			return getXMLContent("CHECK-ENTITY-CONSISTENCY/DETAIL/STATUS/CACHE");
		} else if (sign.isCacheReflex()) {
			return getXMLContent("CHECK-ENTITY-CONSISTENCY/DETAIL/STATUS/CACHE-REFLEX");
		}
		return "";
	}
	
	/**
	 * 显示详细的记录
	 * @param product
	 */
	private void detail(CheckEntityConsistencyProduct product) {
		// 显示处理时间
		printRuntime();
		// 打印标题，是空行
		printDetailTitle();
		
		// 组织排列
		Map<Long, NodeSet> stubs = new TreeMap<Long, NodeSet>();
		Map<Node, SignTable> tables = new TreeMap<Node, SignTable>();
		// 整理参数
		for (SignSite e : product.getSites()) {
			Node node = e.getNode();
			SignTable table = e.getTable();
			// 保存节点 -> 表映像关系
			tables.put(node, table);
			// 保存数据块编号和节点映像关系
			for (Long stub : table.getStubs()) {
				NodeSet set = stubs.get(stub);
				if (set == null) {
					set = new NodeSet();
					stubs.put(stub, set);
				}
				set.add(node);
			}
		}
		
		// 一致性
		String yes = getXMLContent("CHECK-ENTITY-CONSISTENCY/DETAIL/CONSISTENCY/YES");
		String no =  getXMLContent("CHECK-ENTITY-CONSISTENCY/DETAIL/CONSISTENCY/NO");

		ArrayList<ShowItem> array = new ArrayList<ShowItem>();
				
		// 显示
		int count = 0;
		Iterator<Map.Entry<Long, NodeSet>> iterator = stubs.entrySet().iterator();
		while (iterator.hasNext()) {
			// 打印一个空行
			if (count > 0) {
				array.add(printDetailGap());
			}
			
			Map.Entry<Long, NodeSet> entry = iterator.next();
			long stub = entry.getKey();
			List<Node> nodes = entry.getValue().list();
			// 1. 显示数据块编号和一致性比较
			ArrayList<ShowItem> items = new ArrayList<ShowItem>();

			MD5Hash hash = null;
			boolean match = true;
			for (Node node : nodes) {
				// 2. 显示节点地址和数据块签名
				SignTable table = tables.get(node);
				StubSign sign =	table.find(stub);
				if (hash == null) {
					hash = sign.getHash();
				} else if (Laxkit.compareTo(hash, sign.getHash()) != 0) {
					match = false;
				}
				
				// 保存起来
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, choice(sign)));
				item.add(new ShowStringCell(1, sign.getHash()));
				item.add(new ShowStringCell(2, splitLaxcusTime(sign.getLastModified())));
				item.add(new ShowStringCell(3, node));
				items.add(item);
			}
			
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, String.format("0x%X", stub)));
			item.add(new ShowStringCell(1, (match ? yes : no), (match ? java.awt.Color.BLUE : java.awt.Color.RED)));
			item.add(new ShowStringCell(2, ""));
			item.add(new ShowStringCell(3, ""));
			// 插入到第一行
			items.add(0, item);
			
			// 保存全部
			array.addAll(items);

			// 统计值加1 
			count ++;
		}


		// 显示单元
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, product.getSpace()));
		item.add(new ShowLongCell(1, product.getStubs()));
		item.add(new ShowLongCell(2, product.getValidates()));
		String rate = ConfigParser.splitRate(product.getValidates(), product.getStubs());
		item.add(new ShowStringCell(3, rate));
		addShowItem(item);
		// 加一个空行
		addShowItem(printDetailGap());
		// 显示参数
		for (ShowItem e : array) {
			addShowItem(e);
		}
		
		// 输出全部记录
		flushTable();
	}

}