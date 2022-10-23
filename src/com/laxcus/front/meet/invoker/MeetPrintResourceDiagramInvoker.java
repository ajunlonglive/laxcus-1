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
public abstract class MeetPrintResourceDiagramInvoker extends MeetInvoker {

	/**
	 * 构造显示状态的异步调用器
	 * @param cmd 指定命令
	 */
	protected MeetPrintResourceDiagramInvoker(Command cmd) {
		super(cmd);
	}

	private String[] getTitleCells() {
		String[] cells = new String[] { "RESOURCE-DIAGRAM/ATTRIBUTE",
		"RESOURCE-DIAGRAM/VALUE" };
		return cells;
	}
	
	/**
	 * 统计列单元
	 * @return
	 */
	private int getTitleColumnsCount(){
		return getTitleCells().length;
	}
	
	/**
	 * 生成标题单元
	 */
	private void printTitle() {
		// 生成表格标题
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}
	
	/**
	 * 打印空行
	 */
	private void printGap() {
		int count = getTitleColumnsCount();
		ShowItem item = new ShowItem();
		for (int i = 0; i < count; i++) {
			ShowStringCell e = new ShowStringCell(i, "  ");
			item.add(e);
		}
		addShowItem(item);
	}
	
	/**
	 * 打印数据库状态
	 * @param array
	 */
	protected void printSchemas(List<Schema> array) {
		// 显示处理时间
		printRuntime();
		printTitle();

		int size = array.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				printGap();
			}
			Schema schema = array.get(i);
			print(schema);
		}
		
		// 输出全部记录
		flushTable();
	}

	/**
	 * 打印数据表状态
	 * @param array
	 */
	protected void printTables(List<Table> array) {
		// 显示处理时间
		printRuntime();
		printTitle();
		
		int size = array.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				printGap();
			}
			Table table = array.get(i);
			print(table);
		}
		
		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 显示一行
	 * @param xmlTitlePath XML标题路径
	 * @param value 参数值
	 */
	private void printItem(String xmlTitlePath, String value) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlTitlePath);
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, value));
		addShowItem(item);
	}
	
	/**
	 * 显示一行
	 * @param xmlPath
	 * @param value
	 */
	private void printItem(String xmlPath, Object value) {
		printItem(xmlPath, value.toString());
	}
	
	/**
	 * 显示一行
	 * @param xmlPath
	 * @param value
	 */
	private void printItem(String xmlPath, long value) {
		String text = String.format("%d", value);
		printItem(xmlPath, text);
	}

	/**
	 * 显示数据库状态参数
	 * @param schema
	 */
	private void print(Schema schema) {
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
	 * 显示数据表状态参数
	 * @param table
	 */
	private void print(Table table) {
		printItem("RESOURCE-DIAGRAM/ITEM/TABLE", table.getSpace());
		printItem("RESOURCE-DIAGRAM/ITEM/CREATE-TIME", splitLaxcusTime(table.getCreateTime()));

		// 存储模型
		if (table.isNSM()) {
			String nsm = getXMLContent("RESOURCE-DIAGRAM/ITEM/SM/NSM");
			printItem("RESOURCE-DIAGRAM/ITEM/SM", nsm);
			printItem("RESOURCE-DIAGRAM/ITEM/SM/MULTIPLE", "--");//MULTIPLE
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

}