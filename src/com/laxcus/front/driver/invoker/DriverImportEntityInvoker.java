/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.access.table.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.access.schema.Space;
import com.laxcus.access.schema.Table;
import com.laxcus.law.rule.RuleOperator;
import com.laxcus.law.rule.TableRuleItem;
import com.laxcus.log.client.Logger;
import com.laxcus.util.charset.*;
import com.laxcus.util.tip.FaultTip;

/**
 * 数据导入命令调用器。<br>
 * 把本地磁盘上的数据导入到计算机集群。
 * 
 * @author scott.liang
 * @version 1.0 5/11/2019
 * @since research 1.0
 */
public class DriverImportEntityInvoker extends DriverRuleInvoker {
	
	/**
	 * 构造数据导入命令调用器，指定命令
	 * @param cmd 数据导入命令
	 */
	public DriverImportEntityInvoker(DriverMission mission) {
		super(mission);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ImportEntity getCommand() {
		return (ImportEntity) super.getCommand();
	}

	/**
	 * 建立表规则
	 */
	private void initRule() {
		ImportEntity cmd = getCommand();
		Space space = cmd.getSpace();
		// 保存成“共享写”的事务规则
		TableRuleItem item = new TableRuleItem(RuleOperator.SHARE_WRITE, space);
		addRule(item);
	}
	
	/**
	 * 如果没有定义，检查文件字符集
	 * @return 检测成功返回真，否则假
	 */
	private int checkCharset(int who, File file) {
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
	 * 判断表存在！
	 * @param space 表名
	 * @return 返回真或者假
	 */
	private boolean hasTable(Space space) {
		Table table = getStaffPool().findTable(space);
		return (table != null);
	}
	
	/**
	 * 执行事务阶段中的数据处理 <br>
	 * 这个方法由子类根据各自需求去实现。
	 * 
	 * @return 当数据处理工作全部完成时，返回真（无论数据处理是错误或者失败）；否则假。<b>特别说明：数据处理错误也要返回“真”。<b>
	 */
	@Override
	protected boolean process() {
		ImportEntity cmd = getCommand();
		
		// 判断表存在
		Space space = cmd.getSpace();
		if (!hasTable(space)) {
			faultX(FaultTip.NOTFOUND_X, space);
			return true; // 出错，停止处理
		}
		
		List<File> files = cmd.list();
		
		// 命令集
		ArrayList<SingleImportEntity> a = new ArrayList<SingleImportEntity>();
		
		// 检查字符集，任意一个有错误，忽略全部！
		for (File file : files) {
			int charset = checkCharset(cmd.getCharset(), file);
			if (CharsetType.isNone(charset)) {
				faultX(FaultTip.ILLEGAL_CHARSET_X, file);
				continue;
			}
			// 保存实例
			SingleImportEntity sub = new SingleImportEntity(space);
			sub.setFile(file);
			sub.setCharset(charset);
			sub.setRows(cmd.getRows());
			a.add(sub);
		}
		
		ArrayList<SingleImportEntityResult> array = new ArrayList<SingleImportEntityResult>();
		// 逐一发送
		for(SingleImportEntity sub : a) {
			// 生成钩子和转发命令
			SingleImportEntityHook hook = new SingleImportEntityHook();
			ShiftSingleImportEntity shift = new ShiftSingleImportEntity(sub, hook);
			
			// 交给句柄处理
			boolean success = getCommandPool().press(shift);
			if(!success) {
				SingleImportEntityResult res = new SingleImportEntityResult(false, sub.getFile(), -1);
				array.add(res);
				continue;
			}
			// 进行等待
			hook.await();
			SingleImportEntityResult res = hook.getProduct();
			if (res != null) {
				array.add(res);
			}
		}
		
		// 保存参数
		ImportEntityProduct product = new ImportEntityProduct(cmd.getSpace());
		product.addAll(array);
		// 输出结果
		setProduct(product);
		
		// 退出！
		return true;
	}
	
//	/**
//	 * 打印写入记录
//	 * @param array 记录结果
//	 */
//	private void printResult(List<SingleImportEntityResult> array) {
//		// 打印时间
//		printRuntime();
//		// 生成表格标题
//		createShowTitle(new String[] { "IMPORT-ENTITY/STATUS",
//				"IMPORT-ENTITY/TABLE", "IMPORT-ENTITY/FILE",
//				"IMPORT-ENTITY/ROWS" });
//
//		ImportEntity cmd = getCommand();
//
//		long total = 0;
//		for (SingleImportEntityResult e : array) {
//			if (e.getRows() > 0) {
//				total += e.getRows();
//			}
//			String filename = e.getFile().toString();
//			Object[] a = new Object[] { e.isSuccessful(), cmd.getSpace(), filename, e.getRows() };
//			// 写入磁盘
//			printRow(a);
//		}
//
//		// 统计值
//		Object[] a = new Object[] { "", cmd.getSpace(), "--", total };
//		printRow(a);
//
//		// 输出全部记录
//		flushTable();
//	}
	
}

///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license GNU Lesser General Public License (LGPL)
// */
//package com.laxcus.front.driver.invoker;
//
//import java.io.*;
//import java.util.regex.*;
//
//import com.laxcus.command.access.table.*;
//import com.laxcus.access.column.Column;
//import com.laxcus.access.column.attribute.CharAttribute;
//import com.laxcus.access.column.attribute.ColumnAttribute;
//import com.laxcus.access.column.attribute.DateAttribute;
//import com.laxcus.access.column.attribute.DoubleAttribute;
//import com.laxcus.access.column.attribute.FloatAttribute;
//import com.laxcus.access.column.attribute.HCharAttribute;
//import com.laxcus.access.column.attribute.IntegerAttribute;
//import com.laxcus.access.column.attribute.LongAttribute;
//import com.laxcus.access.column.attribute.RawAttribute;
//import com.laxcus.access.column.attribute.ShortAttribute;
//import com.laxcus.access.column.attribute.TimeAttribute;
//import com.laxcus.access.column.attribute.TimestampAttribute;
//import com.laxcus.access.column.attribute.WCharAttribute;
//import com.laxcus.access.parse.SyntaxException;
//import com.laxcus.access.row.Row;
//import com.laxcus.access.schema.Space;
//import com.laxcus.access.schema.Table;
//import com.laxcus.access.util.CalendarGenerator;
//import com.laxcus.access.util.NumberGenerator;
//import com.laxcus.access.util.VariableGenerator;
//import com.laxcus.command.access.Insert;
//import com.laxcus.command.access.InsertGuide;
//import com.laxcus.command.access.InsertProduct;
//import com.laxcus.echo.Cabin;
//import com.laxcus.echo.invoke.ReplyItem;
//import com.laxcus.front.driver.mission.*;
//import com.laxcus.law.rule.RuleOperator;
//import com.laxcus.law.rule.TableRuleItem;
//import com.laxcus.log.client.Logger;
//import com.laxcus.site.Node;
//import com.laxcus.util.charset.*;
//import com.laxcus.util.io.CSVRowReader;
//import com.laxcus.util.set.NodeSet;
//import com.laxcus.util.tip.FaultTip;
//import com.laxcus.visit.VisitException;
//
///**
// * 数据导入命令调用器。<br>
// * 把本地磁盘上的数据导入到计算机集群。
// * 
// * @author scott.liang
// * @version 1.0 5/11/2019
// * @since research 1.0
// */
//public class DriverImportEntityInvoker extends DriverRuleInvoker {
//
//	/** CSV格式读取器 **/
//	private CSVRowReader reader;
//
//	/** 数据表  **/
//	private Table table;
//
//	/** 统计写入行数 **/
//	private long count;
//
//	/** 循环执行步骤 **/
//	private int step = 1;
//
//	/** 列名集合  **/
//	private String[] columnNames;
//
//	/** 读实例   **/
//	private Insert inject;
//
//	/** 值参数格式  */
//	private final static String RAW = "^\\s*(?i)0x([0-9a-fA-F]+)\\s*$";
//	private final static String STRING = "^\\s*\\'([\\w\\W]+?)\\'\\s*$";
//	private final static String STRING2 = "^\\s*([\\w\\W]+?)\\s*$";
//	private final static String NUMBER = "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]*)\\s*$";
//	private final static String NUMBER2 = "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]+E\\+[0-9]+)\\s*$";
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.meet.invoker.DriverRuleInvoker#destroy()
//	 */
//	@Override
//	public void destroy() {
//		if (reader != null) {
//			reader.close();
//			reader = null;
//		}
//
//		// 释放
//		if (table != null) {
//			table = null;
//		}
//	}
//
//	/**
//	 * 构造数据导入命令调用器，指定命令
//	 * @param cmd 数据导入命令
//	 */
//	public DriverImportEntityInvoker(DriverMission mission) {
//		super(mission);
//		initRule();
//		step = 1; // 从1开始
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
//	 */
//	@Override
//	public ImportEntity getCommand() {
//		return (ImportEntity) super.getCommand();
//	}
//	
//	/**
//	 * 每次读取的行数
//	 * @return 记录数
//	 */
//	private int getReadRows() {
//		return getCommand().getRows();
//	}
//
//	/**
//	 * 建立表规则
//	 */
//	private void initRule() {
//		ImportEntity cmd = getCommand();
//		Space space = cmd.getSpace();
//		// 保存成“共享写”的事务规则
//		TableRuleItem item = new TableRuleItem(RuleOperator.SHARE_WRITE, space);
//		addRule(item);
//	}
//
//	/**
//	 * 如果没有定义，检查文件字符集
//	 * @return 检测成功返回真，否则假
//	 */
//	private boolean checkCharset(ImportEntity cmd) {
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
//		return success;
//	}
//
//	/**
//	 * 执行事务阶段中的数据处理 <br>
//	 * 这个方法由子类根据各自需求去实现。
//	 * 
//	 * @return 当数据处理工作全部完成时，返回真（无论数据处理是错误或者失败）；否则假。<b>特别说明：数据处理错误也要返回“真”。<b>
//	 */
//	@Override
//	protected boolean process() {
//		ImportEntity cmd = getCommand();
//		// 判断是第一次操作
//		boolean first = (table == null && reader == null);
//		
//		// 找到数据表和打开磁盘文件
//		if (first) {
//			boolean success = checkCharset(cmd);
//			// 不成功，退出！
//			if (!success) {
//				faultX(FaultTip.ILLEGAL_CHARSET_X, cmd.getFile());
//				return true;
//			}
//			
//			// 查找数据表
//			Space space = cmd.getSpace();
//			table = getStaffPool().findTable(space);
//			if (table == null) {
//				faultX(FaultTip.NOTFOUND_X, space);
//				return true; // 出错
//			}
//
//			// 从磁盘读数据
//			File file = cmd.getFile();
//			int charset = cmd.getCharset();
//			reader = new CSVRowReader(file, charset);
//
//			try {
//				columnNames = reader.readTitle();
//				Logger.debug(this, "process", "%s column count %d", space, columnNames.length);
//			} catch (IOException e) {
//				Logger.error(e);
//				return true; // 出错
//			}
//		}
//				
//
////		// 找到数据表和打开磁盘文件
////		if (first) {
////			File file = cmd.getFile();
////			// 查找数据表
////			Space space = cmd.getSpace();
////			table = getStaffPool().findTable(space);
////			if (table == null) {
////				faultX(FaultTip.NOTFOUND_X, space);
////				return true; // 出错
////			}
////			
////			// 从磁盘读数据
////			if (reader == null) {
////				int charset = cmd.getCharset();
////				// 字符集判断
////				if (CharsetType.isCharset(charset)) {
////					reader = new CSVRowReader(file, charset);
////				} else {
////					reader = new CSVRowReader(file);
////				}
////				try {
////					columnNames = reader.readTitle();
////					Logger.debug(this, "process", "%s column count %d", space, columnNames.length);
////				} catch (IOException e) {
////					Logger.error(e);
////					return true; // 出错
////				}
////			}
////		}
//
//		// 循环处理，直到完成
//		while (true) {
//			int no = subprocess();
//			if (no == -1) {
//				// 发生错误，决定退出，返回真。
//				faultX(FaultTip.SYSTEM_FAULT);
//				return true;
//			} else if (no == 0) {
////				// ok, finished, exit
////				String msg = String.format("数据全部导入完成！全部写入 %d 行。", count);
////				message(msg);
//				// 全部完成， 打印写入记录
//				printResult(count);
//				return true;
//			} else if (no > 3) {
////				String msg = String.format("本次数据写入成功，已经写入行数：%d。", count);
////				message(msg);
//				
////				// 在状态栏设置提示文本. XML标签参数：“%d 行”
////				String prefix = getXMLContent("IMPORT-ENTITY/WRITE-X");
////				String text = String.format(prefix, count);
////				message(text);
//				
//				step = 1; // 步骤恢复到1，继续下一轮。
//				continue;
//			} else {
//				break;
//			}
//		}
//
//		Logger.debug(this, "process", "to next ...");
//
//		return false;
//	}
//	
//	/**
//	 * 打印写入记录
//	 * @param rows 行数
//	 */
//	private void printResult(long rows) {
//		
//		ImportEntity cmd = getCommand();
//		ImportEntityProduct product = new ImportEntityProduct(cmd.getSpace());
//		product.setFilename(cmd.getFile().toString());
//		product.setRows(rows);
//		
//		// 设置报告，转交给调用接口
//		setProduct(product);
//	}
//
//	/**
//	 * 顺序依次插入数据到集群，返回值：
//	 * 1. -1， 出错
//	 * 2. 0，成功
//	 * 3. 大于3，本轮插入数据到集群的工作已经完成，继续下一轮。
//	 * 4. 小于等于3，本次操作完成，退出准备进入下一次操作。
//	 * @return 返回对应的码值
//	 */
//	private int subprocess() {
//		Logger.debug(this, "subprocess", "step is %d, read rows:%d", step, getReadRows());
//
//		boolean success = false;
//		switch(step) {
//		case 1:
//			// 1. 从磁盘读一组CSV数据
//			inject = readCSV(table);
//			success = (inject != null);
//			// 2. 如果有效，建立异步操作
//			if (success) {
//				if (inject.size() > 0) {
//					Logger.debug(this, "subprocess", "to attempt! size %d", inject.size());
//					success = attempt();
//				} else {
//					Logger.warning(this, "subprocess", "inject is empty");
//					// 跳过，直接
//					inject = null;
//					step = 4;
//				}
//			} else {
//				Logger.error(this, "subprocess", "inject is null pointer!");
//			}
//			break;
//		case 2:
//			// 发送数据到集群
//			success = send(inject);
//			if (success) {
//				count += inject.size(); // 统计行数
//			}
//			break;
//		case 3:
//			success = receive();
//			break;
//		}
//		// 发生错误
//		if (!success) {
//			Logger.error(this, "subprocess", "occur error! exit!");
//			return -1;
//		}
//
//		// 自增1
//		step++;
//
//		// 判断完成
//		boolean finished = (step > 3 && reader == null);
//
//		Logger.debug(this, "subprocess", "step： %d, send count: %d, %s",
//				step, count, (finished ? "finished!" : "next ..."));
//
//		// 插入完成，返回0，否则返回步骤值
//		return (finished ? 0 : step);
//	}
//
//	/**
//	 * 读CSV文件，返回一个列实例 
//	 * @param table 数据表
//	 * @return 返回实例 
//	 */
//	private Insert readCSV(Table table) {
//		// 建立一行数据
//		Insert insert = new Insert(table.getSpace());
//		// 逐行读取
//		int readRows = getReadRows();
//		try {
//			// 以行为单位读取
//			for (int n = 0; n < readRows; n++) {
//				// 读一行记录，输出为列数组
//				String[] items = reader.readRow();
//				// 空指针时，是结束了
//				if (items == null) {
//					Logger.info(this, "readCSV", "Read file, finished!");
//					// 关闭缓冲
//					reader.close();
//					reader = null;
//					// 退出！
//					break;
//				}
//
//				Row row = new Row();
//				for (int index = 0; index < items.length; index++) {
//					// 根据列名找到相关列属性
//					ColumnAttribute attribute = table.find(columnNames[index]);
//					if (attribute == null) {
//						Logger.error(this, "readCSV", "cannot be find %s", columnNames[index]);
//						break;
//					}
//
//					// 忽略这一行
//					String input = items[index];
//					if (input == null || input.trim().isEmpty()) {
//						//	Logger.warning(this, "readCSV", "column %d is empty or null!", index);
//						continue;
//					}
//
//					// 检查列，转换成相关数据格式
//					Column column = null;
//					if (attribute.isRaw()) {
//						// 二进制格式
//						Pattern pattern = Pattern.compile(DriverImportEntityInvoker.RAW);
//						Matcher matcher = pattern.matcher(input);
//						if (!matcher.matches()) {
//							Logger.error(this, "readCSV", "illegal raw:%s", input);
//							continue;
//						}
//						String value = matcher.group(1);
//						column = VariableGenerator.createRaw(table.isDSM(), (RawAttribute) attribute, value);
//					} else if (attribute.isCalendar() || attribute.isWord()) {
//						// 字符串格式，按照字符串格式分解
//						Pattern pattern = Pattern.compile(DriverImportEntityInvoker.STRING);
//						Matcher matcher = pattern.matcher(input);
//						if (!matcher.matches()) {
//							pattern = Pattern.compile(DriverImportEntityInvoker.STRING2);
//							matcher = pattern.matcher(input);
//							if (!matcher.matches()) {
//								Logger.error(this, "readCSV", "illegal string:%s", input);
//								continue;
//							}
//						}
//						String value = matcher.group(1);
//						if (attribute.isChar()) {
//							column = VariableGenerator.createChar(table.isDSM(), (CharAttribute) attribute, value);
//						} else if (attribute.isWChar()) {
//							column = VariableGenerator.createWChar(table.isDSM(), (WCharAttribute) attribute, value);
//						} else if (attribute.isHChar()) {
//							column = VariableGenerator.createHChar(table.isDSM(), (HCharAttribute) attribute, value);
//						} else if (attribute.isDate()) {
//							column = CalendarGenerator.createDate((DateAttribute) attribute, value);
//						} else if (attribute.isTime()) {
//							column = CalendarGenerator.createTime((TimeAttribute) attribute, value);
//						} else if (attribute.isTimestamp()) {
//							column = CalendarGenerator.createTimestamp((TimestampAttribute) attribute, value);
//						}
//					} else if (attribute.isNumber()) {
//						Pattern pattern = Pattern.compile(DriverImportEntityInvoker.NUMBER);
//						Matcher matcher = pattern.matcher(input);
//						if (!matcher.matches()) {
//							pattern = Pattern.compile(DriverImportEntityInvoker.NUMBER2);
//							matcher = pattern.matcher(input);
//							// 解析出错，报告
//							if (!matcher.matches()) {
//								Logger.error(this, "readCSV", "illegal number %s", input);
//								continue;
//							}
//						}
//						String value = matcher.group(1);
//
//						if (attribute.isShort()) {
//							column = NumberGenerator.createShort((ShortAttribute) attribute, value);
//						} else if (attribute.isInteger()) {
//							column = NumberGenerator.createInteger((IntegerAttribute)attribute, value);
//						} else if (attribute.isLong()) {
//							column = NumberGenerator.createLong((LongAttribute)attribute, value);
//						} else if (attribute.isFloat()) {
//							column = NumberGenerator.createFloat((FloatAttribute) attribute, value);
//						} else if (attribute.isDouble()) {
//							column = NumberGenerator.createDouble((DoubleAttribute) attribute, value);
//						}
//					}
//
//					if (column == null) {
//						Logger.error(this, "readCSV", "illegal %s", input);
//						continue;
//					}
//					// 设置ID
//					column.setId(attribute.getColumnId());
//					// 保存一列
//					row.add(column);
//				}
//
//				// 填充没有的列
//				fill(row, table);
//				// 根据列的标识号排序
//				row.aligment();
//
//				// 保存一行记录
//				insert.add(row);
//			}
//		} catch (IOException e) {
//			Logger.error(e);
//			return null;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//			return null;
//		}
//
//		Logger.debug(this, "readCSV", "insert rows: %d", insert.size());
//
//		// 插入返回
//		return insert;
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
//	 * 数据写入第一步：<br>
//	 * 1. 从CALL注册站点池中枚举一个地址。<br>
//	 * 2. 尝试向它发送一个写入标识。<br><br>
//	 * 
//	 * @return 成功返回真，否则假
//	 */
//	private boolean attempt() {
//		ImportEntity cmd = getCommand();
//		Space space = cmd.getSpace();
//		// 根据数据表名，找一个地址
//		NodeSet set = getStaffPool().findTableSites(space);
//
//		// 枚举一个站点地址
//		Node hub = (set != null ? set.next() : null);
//		boolean success = (hub != null);
//		if (!success) {
//			faultX(FaultTip.SITE_MISSING);
//			return false;
//		}
//
//		// 向CALL站点发送确认命令
//		InsertGuide guide = new InsertGuide(space);
//		success = fireToHub(hub, guide);
//
//		Logger.debug(this, "attempt", success, "submit to %s", hub);
//
//		return success;
//	}
//
//	/**
//	 * 发送数据
//	 * @param insert 插入命令
//	 * @return 成功返回真，否则假
//	 */
//	private boolean send(Insert insert) {
//		// 1. 确认应答
//		int index = findEchoKey(0);
//		InsertGuide guide = null;
//		try {
//			if (isSuccessObjectable(index)) {
//				guide = getObject(InsertGuide.class, index);
//			}
//		} catch (VisitException e) {
//			Logger.error(e);
//		}
//		// 2. 出错或者拒绝
//		boolean success = (guide != null);
//		if (!success) {
//			super.faultX(FaultTip.FAILED_X, getCommand());
//			return false;
//		}
//
//		// 3. 传输过程中不做封装，INSERT以原始数据格式发送到CALL站点，CALL站点原样转发到DATA主站点
//		Cabin hub = guide.getSource();
//		ReplyItem item = new ReplyItem(hub, insert.build());
//		success = replyTo(item);
//
//		// 不成功提示
//		if (!success) {
//			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
//		}
//
//		Logger.debug(this, "send", success, "send to %s", hub);
//
//		return success;
//	}
//
//	/**
//	 * 数据写入第三步：等待上传的反馈结果。
//	 * @return 成功返回真，否则假
//	 */
//	private boolean receive() {
//		// 有且只有一个索引号
//		int index = findEchoKey(0);
//		// 取对象
//		InsertProduct product = null;
//		try {
//			if (isSuccessObjectable(index)) {
//				product = getObject(InsertProduct.class, index);
//			}
//		} catch (VisitException e) {
//			Logger.error(e);
//			super.fault(e);
//		}
//		// 出错或者拒绝
//		boolean success = (product != null && product.isSuccessful());
//
//		Logger.debug(this, "receive", success, "from hub");
//
//		// 工作完成，是否完成退出由上层事务决定！！！
//		return success;
//	}
//
//}