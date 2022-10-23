/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.*;
import com.laxcus.util.display.show.*;

/**
 * 显示数据资源的异步调用器。
 * 
 * @author scott.liang
 * @version 1.1 10/03/2014
 * @since laxcus 1.0
 */
public abstract class MeetShowReferenceInvoker extends MeetInvoker {

	/**
	 * 构造显示数据资源的异步调用器，指定命令
	 * @param cmd 显示数据资源
	 */
	protected MeetShowReferenceInvoker(Command cmd) {
		super(cmd);
	}
	
	/**
	 * 输出标题单元
	 * @return
	 */
	private String[] getTitleCells() {
		// 生成表格标题
		String[] cells = new String[] { "SHOW-TABLE/DATABASE",
				"SHOW-TABLE/TABLE", "SHOW-TABLE/COLUMN/NAME",
				"SHOW-TABLE/COLUMN/TYPE", "SHOW-TABLE/COLUMN/KEY",
				"SHOW-TABLE/COLUMN/CASE", "SHOW-TABLE/COLUMN/LIKE",
		"SHOW-TABLE/COLUMN/NULL", "SHOW-TABLE/COLUMN/REMARK" };
		
		return cells;
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
	private void printTitle() {
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}

	/**
	 * 设置一个空格
	 */
	private void printGap() {
		int count = getTitleColumnsCount();
		ShowItem item = new ShowItem();
		item.addAll(createBlanks(0, count));
		addShowItem(item);
	}

	/**
	 * 打印数据库
	 * @param array 数据库集合
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
	 * 打印数据库记录
	 * @param schema
	 */
	private void print(Schema schema) {
		int count = getTitleColumnsCount();
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, schema.getFame()));
		item.addAll(createBlanks(1, count - 1));
		addShowItem(item);

		List<Table> array = schema.list();
		int size = array.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				printGap();
			}
			Table table = array.get(i);
			print(table);
		}
	}

	/**
	 * 打印数据表
	 * @param array
	 */
	protected void printTables(List<Table> array) {
		// 显示处理时间
		printRuntime();

		printTitle();
		
		int size = array.size();
		for (int i = 0; i < size; i++) {
			// 空隔
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
	 * 打印表
	 * @param table
	 */
	private void print(Table table) {
		int count = getTitleColumnsCount();
		
		Space space = table.getSpace();
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, space.getSchemaText()));
		item.add(new ShowStringCell(1, space.getTableText()));
		item.addAll(createBlanks(2, count -  2));
		addShowItem(item);

		for (ColumnAttribute attribute : table.list()) {
			print(attribute);
		}
	}

	/**
	 * 打印属性
	 * @param attribute
	 */
	private void print(ColumnAttribute attribute) {
		ShowItem item = new ShowItem();
		String blank = (isConsole() ? "| " : " ");
		item.add(new ShowStringCell(0, blank));
		item.add(new ShowStringCell(1, blank));

		item.add(new ShowStringCell(2, attribute.getNameText()));
		String type = ColumnType.translate(attribute.getType());
		item.add(new ShowStringCell(3, type));

		String ignore = (isConsole() ? "| - " : "-");
		// 键值
		String prime = getXMLContent("SHOW-TABLE/COLUMN/KEY/PRIME");
		String slave = getXMLContent("SHOW-TABLE/COLUMN/KEY/SLAVE");
		if (attribute.isPrimeKey()) {
			item.add(new ShowStringCell(4, prime));
		} else if (attribute.isSlaveKey()) {
			item.add(new ShowStringCell(4, slave));
		} else {
			item.add(new ShowStringCell(4, ignore));
		}

		// 字符串属性
		if (ColumnType.isWord(attribute.getType())) {
			WordAttribute see = (WordAttribute) attribute;
			String yes = getXMLContent("SHOW-TABLE/COLUMN/CASE/YES");
			String no = getXMLContent("SHOW-TABLE/COLUMN/CASE/NO");
			if (see.isSentient()) {
				item.add(new ShowStringCell(5, yes));
			} else {
				item.add(new ShowStringCell(5, no));
			}
			if (see.isLike()) {
				item.add(new ShowStringCell(6, yes));
			} else {
				item.add(new ShowStringCell(6, no));
			}
			if (see.isNullable()) {
				item.add(new ShowStringCell(7, yes));
			} else {
				item.add(new ShowStringCell(7, no));
			}
		} else {
			item.add(new ShowStringCell(5, ignore));
			item.add(new ShowStringCell(6, ignore));
			item.add(new ShowStringCell(7, ignore));
		}
		
		// 备注
		String remark = (attribute.getComment() != null ? attribute.getComment() : "");
		item.add(new ShowStringCell(8, remark));

		// 保存到队列
		addShowItem(item);
	}

	/**
	 * 生成一组空白单元
	 * @param index
	 * @param size
	 * @return
	 */
	private List<ShowItemCell> createBlanks(int index, int size) {
		ArrayList<ShowItemCell> array = new ArrayList<ShowItemCell>();
		String blank = (isConsole() ? "| " : " ");
		for (int i = 0; i < size; i++) {
			ShowStringCell e = new ShowStringCell(index, blank);
			array.add(e);
			index++;
		}
		return array;
	}

}
