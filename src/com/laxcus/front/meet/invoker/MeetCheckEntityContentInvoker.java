/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.access.table.*;
import com.laxcus.access.column.Column;
import com.laxcus.access.column.attribute.CharAttribute;
import com.laxcus.access.column.attribute.ColumnAttribute;
import com.laxcus.access.column.attribute.DateAttribute;
import com.laxcus.access.column.attribute.DoubleAttribute;
import com.laxcus.access.column.attribute.FloatAttribute;
import com.laxcus.access.column.attribute.HCharAttribute;
import com.laxcus.access.column.attribute.IntegerAttribute;
import com.laxcus.access.column.attribute.LongAttribute;
import com.laxcus.access.column.attribute.RawAttribute;
import com.laxcus.access.column.attribute.ShortAttribute;
import com.laxcus.access.column.attribute.TimeAttribute;
import com.laxcus.access.column.attribute.TimestampAttribute;
import com.laxcus.access.column.attribute.WCharAttribute;
import com.laxcus.access.parse.SyntaxException;
import com.laxcus.access.row.Row;
import com.laxcus.access.schema.Space;
import com.laxcus.access.schema.Table;
import com.laxcus.access.util.CalendarGenerator;
import com.laxcus.access.util.NumberGenerator;
import com.laxcus.access.util.VariableGenerator;
import com.laxcus.log.client.Logger;
import com.laxcus.util.charset.*;
import com.laxcus.util.io.*;
import com.laxcus.util.tip.*;

/**
 * 导入文件内容检查命令调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/11/2019
 * @since research 1.0
 */
public class MeetCheckEntityContentInvoker extends MeetInvoker {
	
	/**
	 * 扫描报告
	 * @author scott.liang
	 * @version 1.0 9/27/2019
	 * @since laxcus 1.0
	 */
	class ScanResult {
		boolean successful = false;

		int charset = CharsetType.NONE;

		int rows = 0;

		String filename;

		ScanResult(boolean b, String name) {
			successful = b;
			filename = name;
		}

		ScanResult(boolean b, String name, int cs) {
			this(b, name);
			charset = cs;
		}

		ScanResult(boolean b, String name, int cs, int line) {
			this(b, name, cs);
			rows = line;
		}
	}

	/**
	 * 构造导入文件内容检查命令调用器，指定命令
	 * @param cmd 导入文件内容检查命令
	 */
	public MeetCheckEntityContentInvoker(CheckEntityContent cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckEntityContent getCommand() {
		return (CheckEntityContent) super.getCommand();
	}

	/**
	 * 检测文件内容
	 * @param space 表名
	 * @param file 文件名
	 * @param charset 字符集
	 * @param type 文件类型
	 * @return 返回扫描结果
	 */
	private ScanResult check(Space space, File file, int charset, int type) {
		// 检查字符集
		charset = checkCharset(file, charset);
		// 不能检测出来
		if (CharsetType.isNone(charset)) {
			faultX(FaultTip.ILLEGAL_CHARSET_X, file);
			return new ScanResult(false, file.toString());
		}
		Table table = getStaffPool().findTable(space);
		if (table == null) {
			faultX(FaultTip.NOTFOUND_X, space);
			return new ScanResult(false, file.toString(), charset); // 出错
		}

		PlainRowReader reader = null;
		if (EntityStyle.isCSV(type)) {
			reader = new CSVRowReader(file, charset);
		} else if (EntityStyle.isTXT(type)) {
			reader = new TXTRowReader(file, charset);
		} else {
			return new ScanResult(false, file.toString(), charset); // 出错
		}

		// 读列名称
		String[] columnNames = null;
		try {
			columnNames = reader.readTitle();
			//			Logger.debug(this, "resolve", "%s column count %d", space, columnNames.length);
		} catch (IOException e) {
			Logger.error(e);
			faultX(FaultTip.FAILED_X, space);
			return new ScanResult(false, file.toString(), charset); // 出错
		}

		// 读行数
		int rows = readContent(reader, table, columnNames);
		// 判断成功
		boolean success = (rows > 0);

		return new ScanResult(success, file.toString(), charset, rows);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckEntityContent cmd = getCommand();
		
		Space space = cmd.getSpace();
		int charset = cmd.getCharset();
		int type = cmd.getType();

		ArrayList<ScanResult> array = new ArrayList<ScanResult>();

		for (File file : cmd.list()) {
			// 消息
			messageX(MessageTip.CHECK_X, file.toString());
			// 检测
			ScanResult e = check(space, file, charset, type);
			array.add(e);
		}
		// 打印结果
		printX(space, array);

		return useful();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 如果没有定义，检查文件字符集
	 * @param file 文件名
	 * @param who 字符集类型
	 * @return 检测后的字符集类型
	 */
	private int checkCharset(File file, int who) {
		// 判断定义了字符集
		if (CharsetType.isCharset(who)) {
			return who;
		}

		// 实时检查字符集
		CharsetChecker checker = new CharsetChecker();
		String charset = checker.check(file);
		boolean success = (charset != null);
		if (success) {
			who = CharsetType.translate(charset);
			success = (!CharsetType.isNone(who));
			if (success) {
				return who;
			}
		}

		Logger.debug(this, "checkCharset", success, "charset is %s", charset);

		return CharsetType.NONE;
	}

	/**
	 * 一行记录中，如果有某列不存在，定义一个默认值
	 * @param row
	 * @param table
	 */
	private void fill(Row row, Table table) {
		for (ColumnAttribute attribute : table.list()) {
			short columnId = attribute.getColumnId();
			Column column = row.find(columnId);
			if (column != null) continue;
			// 生成一个默认值
			column = attribute.getDefault();
			if (column == null) {
				throw new SyntaxException("%s cannot support default", attribute.getNameText());
			}
			row.add(column);
		}
	}

	/**
	 * 解析一行记录
	 * @param table
	 * @param items
	 * @return 返回行
	 * @throws IOException
	 */
	private Row splitRow(Table table, String[] columnNames, String[] items) throws IOException {
		Row row = new Row();
		for (int index = 0; index < items.length; index++) {
			// 根据列名找到相关列属性
			ColumnAttribute attribute = table.find(columnNames[index]);
			if (attribute == null) {
				faultX(FaultTip.NOTFOUND_X , columnNames[index]);
				return null;
			}

			// 忽略这一行
			String value = items[index];
			if (value == null) {
				throw new IOException("column %d " + index + " is null pointer");
			} else if (value.trim().isEmpty()) {
				continue;
			}

			// 检查列，转换成相关数据格式
			Column column = null;
			// 二进制数字
			if (attribute.isRaw()) {
				column = VariableGenerator.createRaw(table.isDSM(), (RawAttribute) attribute, value);
			}
			// 字符串
			else if(attribute.isWord()) {
				if (attribute.isChar()) {
					column = VariableGenerator.createChar(table.isDSM(), (CharAttribute) attribute, value);
				} else if (attribute.isWChar()) {
					column = VariableGenerator.createWChar(table.isDSM(), (WCharAttribute) attribute, value);
				} else if (attribute.isHChar()) {
					column = VariableGenerator.createHChar(table.isDSM(), (HCharAttribute) attribute, value);
				}
			}
			// 日期时间，允许空字段
			else if (attribute.isCalendar()) {
				if (attribute.isDate()) {
					column = CalendarGenerator.createDate((DateAttribute) attribute, value);
				} else if (attribute.isTime()) {
					column = CalendarGenerator.createTime((TimeAttribute) attribute, value);
				} else if (attribute.isTimestamp()) {
					column = CalendarGenerator.createTimestamp((TimestampAttribute) attribute, value);
				}
			}
			// 检查数字，允许空字段
			else if (attribute.isNumber() ) {
				if (attribute.isShort()) {
					column = NumberGenerator.createShort((ShortAttribute) attribute, value);
				} else if (attribute.isInteger()) {
					column = NumberGenerator.createInteger((IntegerAttribute) attribute, value);
				} else if (attribute.isLong()) {
					column = NumberGenerator.createLong((LongAttribute) attribute, value);
				} else if (attribute.isFloat()) {
					column = NumberGenerator.createFloat((FloatAttribute) attribute, value);
				} else if (attribute.isDouble()) {
					column = NumberGenerator.createDouble((DoubleAttribute) attribute, value);
				}
			}

			// 空值，返回空指针
			if (column == null) {
				faultX(FaultTip.NOTSUPPORT_X, columnNames[index]);
				return null;
			}
			// 设置ID
			column.setId(attribute.getColumnId());
			// 保存一列
			row.add(column);
		}

		// 填充没有的列
		fill(row, table);
		// 根据列的标识号排序
		row.aligment();
		// 返回行记录
		return row;
	}

	/**
	 * 从磁盘文件读取记录，返回读取的行数
	 * @param reader 读取器
	 * @param table 表
	 * @param columnNames 列名
	 * @return 失败返回-1，成功返回读取的行数（大于等于 0）
	 */
	private int readContent(PlainRowReader reader, Table table,  String[] columnNames) {
		int count = 0;
		try {
			// 以行为单位读取
			while(true) {
				// 读一行记录，输出为列数组
				String[] items = reader.readRow();
				// 空指针时，是结束了
				if (items == null) {
					Logger.info(this, "readContent", "Read file, finished!");
					// 关闭缓冲
					reader.close();
					// 退出！
					break;
				}

				// 解析一行
				Row row = splitRow(table, columnNames, items);
				// 判断出错
				if (row == null) {
					return -1;
				}
				// 统计一行
				count++;
			}
		} catch (IOException e) {
			reader.close();
			Logger.error(e);
			fault(e.getMessage());
			return -1;
		} catch (Throwable e) {
			reader.close();
			Logger.fatal(e);
			fault(e.getMessage());
			return -1;
		}

		Logger.debug(this, "readContent", "reader rows: %d", count);

		// 插入返回
		return count;
	}
	
	/**
	 * 打印写入记录
	 * @param space 表名
	 * @param array 数组
	 */
	private void printX(Space space, List<ScanResult> array) {
		// 打印时间
		printRuntime();

		// 生成表格标题
		createShowTitle(new String[] { "CHECK-ENTITY-CONTENT/STATUS",
				"CHECK-ENTITY-CONTENT/TABLE", "CHECK-ENTITY-CONTENT/FILE",
				"CHECK-ENTITY-CONTENT/CHARSET", "CHECK-ENTITY-CONTENT/ROWS" });

		for (ScanResult e : array) {
			String charset = CharsetType.translate(e.charset);
			String rows = "";
			if (e.rows > 0) {
				rows = String.format("%d", e.rows);
			}
			Object[] a = new Object[] { e.successful, space, e.filename,
					charset, rows };
			printRow(a);
		}
		// 输出全部记录
		flushTable();
	}

}

///**
// * 导入文件内容检查命令调用器。<br>
// * 
// * @author scott.liang
// * @version 1.0 6/11/2019
// * @since research 1.0
// */
//public class MeetCheckEntityContentInvoker extends MeetInvoker {
//
//	/**
//	 * 构造导入文件内容检查命令调用器，指定命令
//	 * @param cmd 导入文件内容检查命令
//	 */
//	public MeetCheckEntityContentInvoker(CheckEntityContent cmd) {
//		super(cmd);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
//	 */
//	@Override
//	public CheckEntityContent getCommand() {
//		return (CheckEntityContent) super.getCommand();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		CheckEntityContent cmd = getCommand();
//
//		// 检查字符集
//		boolean success = checkCharset(cmd);
//		if (!success) {
//			faultX(FaultTip.ILLEGAL_CHARSET_X, cmd.getFile());
//			return useful(false);
//		}
//
//		// 查找数据表
//		Space space = cmd.getSpace();
//		Table table = getStaffPool().findTable(space);
//		if (table == null) {
//			faultX(FaultTip.NOTFOUND_X, space);
//			return useful(false); // 出错
//		}
//
//		// 从磁盘读数据
//		File file = cmd.getFile();
//		int charset = cmd.getCharset();
//		CSVRowReader reader = new CSVRowReader(file, charset);
//
//		// 读列名称
//		String[] columnNames = null;
//		try {
//			columnNames = reader.readTitle();
//			Logger.debug(this, "launch", "%s column count %d", space, columnNames.length);
//		} catch (IOException e) {
//			Logger.error(e);
//			faultX(FaultTip.FAILED_X, space);
//			return useful(false); // 出错
//		}
//
//		// 读行数
//		int rows = readContent(reader, table, columnNames);
//
//		printX(cmd, rows);
//
//		return useful();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		return false;
//	}
//
//	/**
//	 * 如果没有定义，检查文件字符集
//	 * @return 检测成功返回真，否则假
//	 */
//	private boolean checkCharset(CheckEntityContent cmd) {
//		int who = cmd.getCharset();
//		// 判断定义了字符集
//		if (CharsetType.isCharset(who)) {
//			return true;
//		}
//
//		// 实时检查字符集
//		CharsetChecker checker = new CharsetChecker();
//		String charset = checker.check(cmd.getFile());
//		boolean success = (charset != null);
//		if (success) {
//			who = CharsetType.translate(charset);
//			success = (!CharsetType.isNone(who));
//			if (success) {
//				cmd.setCharset(who);
//			}
//		}
//
//		Logger.debug(this, "checkCharset", success, "charset is %s", charset);
//
//		return success;
//	}
//
//	/**
//	 * 一行记录中，如果有某列不存在，定义一个默认值
//	 * @param row
//	 * @param table
//	 */
//	private void fill(Row row, Table table) {
//		for (ColumnAttribute attribute : table.list()) {
//			short columnId = attribute.getColumnId();
//			Column column = row.find(columnId);
//			if (column != null) continue;
//			// 生成一个默认值
//			column = attribute.getDefault();
//			if (column == null) {
//				throw new SyntaxException("%s cannot support default", attribute.getNameText());
//			}
//			row.add(column);
//		}
//	}
//
//	/**
//	 * 解析一行记录
//	 * @param table
//	 * @param items
//	 * @return 返回行
//	 * @throws IOException
//	 */
//	private Row splitRow(Table table, String[] columnNames, String[] items) throws IOException {
//		Row row = new Row();
//		for (int index = 0; index < items.length; index++) {
//			// 根据列名找到相关列属性
//			ColumnAttribute attribute = table.find(columnNames[index]);
//			if (attribute == null) {
//				faultX(FaultTip.NOTFOUND_X , columnNames[index]);
//				return null;
//			}
//
//			// 忽略这一行
//			String value = items[index];
//			if (value == null) {
//				throw new IOException("column %d " + index + " is null pointer");
//			} else if (value.trim().isEmpty()) {
//				continue;
//			}
//
//			// 检查列，转换成相关数据格式
//			Column column = null;
//			// 二进制数字
//			if (attribute.isRaw()) {
//				column = VariableGenerator.createRaw(table.isDSM(), (RawAttribute) attribute, value);
//			}
//			// 字符串
//			else if(attribute.isWord()) {
//				if (attribute.isChar()) {
//					column = VariableGenerator.createChar(table.isDSM(), (CharAttribute) attribute, value);
//				} else if (attribute.isWChar()) {
//					column = VariableGenerator.createWChar(table.isDSM(), (WCharAttribute) attribute, value);
//				} else if (attribute.isHChar()) {
//					column = VariableGenerator.createHChar(table.isDSM(), (HCharAttribute) attribute, value);
//				}
//			}
//			// 日期时间，允许空字段
//			else if (attribute.isCalendar()) {
//				if (attribute.isDate()) {
//					column = CalendarGenerator.createDate((DateAttribute) attribute, value);
//				} else if (attribute.isTime()) {
//					column = CalendarGenerator.createTime((TimeAttribute) attribute, value);
//				} else if (attribute.isTimestamp()) {
//					column = CalendarGenerator.createTimestamp((TimestampAttribute) attribute, value);
//				}
//			}
//			// 检查数字，允许空字段
//			else if (attribute.isNumber() ) {
//				if (attribute.isShort()) {
//					column = NumberGenerator.createShort((ShortAttribute) attribute, value);
//				} else if (attribute.isInteger()) {
//					column = NumberGenerator.createInteger((IntegerAttribute) attribute, value);
//				} else if (attribute.isLong()) {
//					column = NumberGenerator.createLong((LongAttribute) attribute, value);
//				} else if (attribute.isFloat()) {
//					column = NumberGenerator.createFloat((FloatAttribute) attribute, value);
//				} else if (attribute.isDouble()) {
//					column = NumberGenerator.createDouble((DoubleAttribute) attribute, value);
//				}
//			}
//
//			// 空值，返回空指针
//			if (column == null) {
//				faultX(FaultTip.NOTSUPPORT_X, columnNames[index]);
//				return null;
//			}
//			// 设置ID
//			column.setId(attribute.getColumnId());
//			// 保存一列
//			row.add(column);
//		}
//
//		// 填充没有的列
//		fill(row, table);
//		// 根据列的标识号排序
//		row.aligment();
//		// 返回行记录
//		return row;
//	}
//
//	/**
//	 * 从磁盘文件读取记录，返回读取的行数
//	 * @param reader 读取器
//	 * @param table 表
//	 * @param columnNames 列名
//	 * @return 失败返回-1，成功返回读取的行数（大于等于 0）
//	 */
//	private int readContent(CSVRowReader reader, Table table,  String[] columnNames) {
//		int count = 0;
//		try {
//			// 以行为单位读取
//			while(true) {
//				// 读一行记录，输出为列数组
//				String[] items = reader.readRow();
//				// 空指针时，是结束了
//				if (items == null) {
//					Logger.info(this, "readContent", "Read file, finished!");
//					// 关闭缓冲
//					reader.close();
//					// 退出！
//					break;
//				}
//
//				// 解析一行
//				Row row = splitRow(table, columnNames, items);
//				// 判断出错
//				if (row == null) {
//					return -1;
//				}
//				// 统计一行
//				count++;
//			}
//		} catch (IOException e) {
//			reader.close();
//			Logger.error(e);
//			fault(e.getMessage());
//			return -1;
//		} catch (Throwable e) {
//			reader.close();
//			Logger.fatal(e);
//			fault(e.getMessage());
//			return -1;
//		}
//
//		Logger.debug(this, "readContent", "reader rows: %d", count);
//
//		// 插入返回
//		return count;
//	}
//
//	/**
//	 * 打印写入记录
//	 * @param rows 行数
//	 */
//	private void printX(CheckEntityContent cmd, long rows) {
//		// 打印时间
//		printRuntime();
//
//		// 生成表格标题
//		createShowTitle(new String[] { "CHECK-ENTITY-CONTENT/STATUS",
//				"CHECK-ENTITY-CONTENT/TABLE", "CHECK-ENTITY-CONTENT/FILE",
//				"CHECK-ENTITY-CONTENT/CHARSET", "CHECK-ENTITY-CONTENT/ROWS" });
//
//		// 判断字符编码
//		String charset = CharsetType.translate(cmd.getCharset());
//		// 判断成功
//		boolean success = (rows >= 0);
//
//		// 显示单元
//		ShowItem item = new ShowItem();
//		item.add(createConfirmTableCell(0, success));
//		item.add(new ShowStringCell(1, cmd.getSpace()));
//		item.add(new ShowStringCell(2, cmd.getFile().toString()));
//		item.add(new ShowStringCell(3, charset));
//		// 检测行数
//		if (success) {
//			item.add(new ShowLongCell(4, rows));
//		} else {
//			item.add(new ShowStringCell(4, ""));
//		}
//		addShowItem(item);
//
//		// 输出全部记录
//		flushTable();
//	}
//
//}