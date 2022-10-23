/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;

/**
 * 显示状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public abstract class WatchPrintResourceDiagramInvoker extends WatchInvoker {

	/**
	 * 构造显示状态的异步调用器
	 * @param cmd 指定命令
	 */
	protected WatchPrintResourceDiagramInvoker(Command cmd) {
		super(cmd);
	}
	
	/**
	 * 单元
	 * @return
	 */
	private String[] getTitleCells() {
		return new String[] { "RESOURCE-DIAGRAM/ATTRIBUTE",
		"RESOURCE-DIAGRAM/VALUE" };
	}
	
	/**
	 * 标题统计
	 * @return
	 */
	private int getTitleColumnsCount() {
		return getTitleCells().length;
	}

	/**
	 * 生成标题单元
	 */
	protected void printTitle() {
		// 生成表格标题
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}

	/**
	 * 打印空行
	 */
	protected void printGap() {
		int count = getTitleColumnsCount();
		ShowItem item = new ShowItem();
		for (int i = 0; i < count; i++) {
			ShowStringCell e = new ShowStringCell(i, "  ");
			item.add(e);
		}
		addShowItem(item);
	}

	/**
	 * 打印没有找到
	 * @param username 用户明文或者数字签名
	 */
	protected void printNotFound(String username) {
		ShowItem item = new ShowItem();

		// 第一段
		String name = findXMLTitle("RESOURCE-DIAGRAM/USER");
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, username, java.awt.Color.RED));
		addShowItem(item);
		
		// 第二段
		item = new ShowItem();
		name = findXMLTitle("RESOURCE-DIAGRAM/USER/STATUS");
		String notfound = getXMLContent("RESOURCE-DIAGRAM/USER/STATUS/NOTFOUND");
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, notfound, java.awt.Color.RED));
		addShowItem(item);
	}
	
	/**
	 * 打印空记录
	 * @param username 用户明文或者数字签名
	 */
	protected void printEmpty(String username) {
		ShowItem item = new ShowItem();

		// 第一段
		String name = findXMLTitle("RESOURCE-DIAGRAM/USER");
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, username, java.awt.Color.BLUE));
		addShowItem(item);
		
		// 第二段
		item = new ShowItem();
		name = findXMLTitle("RESOURCE-DIAGRAM/USER/STATUS");
		String notfound = getXMLContent("RESOURCE-DIAGRAM/USER/STATUS/EMPTY");
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, notfound, java.awt.Color.BLUE));
		addShowItem(item);
	}

	/**
	 * 显示一行
	 * @param xmlTitle XML标题
	 * @param value 数值
	 */
	private void printItem(String xmlTitle, String value) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlTitle);
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, value));
		addShowItem(item);
	}

//	/**
//	 * 解析时间参数
//	 * @param time
//	 * @return
//	 */
//	private String splitCreateTime(long time) {
//		Date date = com.laxcus.util.datetime.SimpleTimestamp.format(time);
//		DateFormat dt =  DateFormat.getDateTimeInstance(); // 系统默认的日期/时间格式
//		return dt.format(date);
//	}

	/**
	 * 解析空间容量
	 * @param size
	 * @return
	 */
	private String splitCapacity(long size) {
		return ConfigParser.splitCapacity(size);
	}

	/**
	 * 显示一行
	 * @param xmlTitle
	 * @param value
	 */
	private void printItem(String xmlTitle, Object value) {
		printItem(xmlTitle, value.toString());
	}

	/**
	 * 显示一行
	 * @param xmlTitle
	 * @param value
	 */
	private void printItem(String xmlTitle, long value) {
		String text = String.format("%d", value);
		printItem(xmlTitle, text);
	}

	/**
	 * 打印数据库
	 * 
	 * @param username 用户明文
	 * @param array 数据库列表
	 */
	protected void printSchemas(String username, List<Schema> array) {
		int size = array.size();
		for (int index = 0; index < size; index++) {
			Schema schema = array.get(index);
			if (index > 0) printGap();
			print(username, schema);
		}
	}

	/**
	 * 显示数据库状态参数
	 * @param schema
	 */
	private void print(String username, Schema schema) {
		printItem("RESOURCE-DIAGRAM/USER", username);
		printItem("RESOURCE-DIAGRAM/ITEM/DATABASE", schema.getFame());

		// 建立时间
		printItem("RESOURCE-DIAGRAM/ITEM/CREATE-TIME", splitLaxcusTime(schema.getCreateTime()));
		// 最大容量
		long size = schema.getMaxSize();
		if (size == 0) {
			String value = getXMLContent("RESOURCE-DIAGRAM/ITEM/CAPACITY/UNLIMIT");
			printItem("RESOURCE-DIAGRAM/ITEM/CAPACITY", value);
		} else {
			printItem("RESOURCE-DIAGRAM/ITEM/CAPACITY", splitCapacity(size));
		}
		// 数据表数目
		printItem("RESOURCE-DIAGRAM/ITEM/TABLE-COUNT", schema.getSpaces().size());
	}

	/**
	 * 显示数据表状态参数
	 * @param username 用户名
	 * @param table 表
	 */
	private void print(String username, Table table) {
		printItem("RESOURCE-DIAGRAM/USER", username);
		printItem("RESOURCE-DIAGRAM/ITEM/TABLE", table.getSpace());
		printItem("RESOURCE-DIAGRAM/ITEM/CREATE-TIME", splitLaxcusTime(table.getCreateTime()));

		// 存储模型
		if (table.isNSM()) {
			String nsm = getXMLContent("RESOURCE-DIAGRAM/ITEM/SM/NSM");
			printItem("RESOURCE-DIAGRAM/ITEM/SM", nsm);
			printItem("RESOURCE-DIAGRAM/ITEM/SM/MULTIPLE", "--");
		} else if (table.isDSM()) {
			String dsm = getXMLContent("RESOURCE-DIAGRAM/ITEM/SM/DSM");
			printItem("RESOURCE-DIAGRAM/ITEM/SM", dsm);
			String multiple = String.format("%d", table.getMultiple());
			printItem("RESOURCE-DIAGRAM/ITEM/SM/MULTIPLE", multiple);
		}

		// 列数
		printItem("RESOURCE-DIAGRAM/ITEM/COLUMN-COUNT", table.size());
		// 数据块尺寸/数据块数/主站点数目
		printItem("RESOURCE-DIAGRAM/ITEM/CHUNK-SIZE", splitCapacity(table.getChunkSize()));
		printItem("RESOURCE-DIAGRAM/ITEM/CHUNK-COPY", table.getChunkCopy());
		printItem("RESOURCE-DIAGRAM/ITEM/PRIME-SITES", table.getPrimeSites());
		// 站点模型
		String share = getXMLContent("RESOURCE-DIAGRAM/ITEM/SITE-MODE/SHARE");
		String exclusive = getXMLContent("RESOURCE-DIAGRAM/ITEM/SITE-MODE/EXCLUSIVE");
		if (table.isShare()) {
			printItem("RESOURCE-DIAGRAM/ITEM/SITE-MODE", share);
		} else if (table.isExclusive()) {
			printItem("RESOURCE-DIAGRAM/ITEM/SITE-MODE", exclusive);
		}
	}

	/**
	 * 打印数据表
	 * 
	 * @param username 用户明文
	 * @param array 数据表列表
	 */
	protected void printTables(String username, List<Table> array) {
		int size = array.size();
		for (int index = 0; index < size; index++) {
			if (index > 0) printGap();
			Table table = array.get(index);
			print(username, table);
		}
	}

}