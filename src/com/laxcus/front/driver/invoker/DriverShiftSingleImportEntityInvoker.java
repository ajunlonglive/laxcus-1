/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.io.*;

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
import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.SyntaxException;
import com.laxcus.access.row.Row;
import com.laxcus.access.schema.Space;
import com.laxcus.access.schema.Table;
import com.laxcus.access.util.CalendarGenerator;
import com.laxcus.access.util.NumberGenerator;
import com.laxcus.access.util.VariableGenerator;
import com.laxcus.command.access.Insert;
import com.laxcus.command.access.InsertGuide;
import com.laxcus.command.access.InsertProduct;
import com.laxcus.echo.Cabin;
import com.laxcus.echo.invoke.ReplyItem;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.Logger;
import com.laxcus.site.Node;
import com.laxcus.util.io.*;
import com.laxcus.util.set.NodeSet;
import com.laxcus.visit.VisitException;

/**
 * 数据导入命令调用器。<br>
 * 把本地磁盘上的数据导入到计算机集群。
 * 
 * @author scott.liang
 * @version 1.0 5/11/2019
 * @since research 1.0
 */
public class DriverShiftSingleImportEntityInvoker extends DriverInvoker {

	/** 文件读取器，CSV/TXT两种格式 **/
	private PlainRowReader reader;

	/** 数据表  **/
	private Table table;

	/** 统计写入行数 **/
	private long count;

	/** 循环执行步骤 **/
	private int step = 1;

	/** 列名集合  **/
	private String[] columnNames;

	/** 读实例   **/
	private Insert inject;


	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.invoker.DriverRuleInvoker#destroy()
	 */
	@Override
	public void destroy() {
		if (reader != null) {
			reader.close();
			reader = null;
		}

		// 释放
		if (table != null) {
			table = null;
		}
	}

	/**
	 * 构造数据导入命令调用器，指定命令
	 * @param cmd 数据导入命令
	 */
	public DriverShiftSingleImportEntityInvoker(DriverMission mission) {
		super(mission);
		step = 1; // 从1开始
		count = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSingleImportEntity getCommand() {
		return (ShiftSingleImportEntity) super.getCommand();
	}

	/**
	 * 实际命令
	 * @return
	 */
	private SingleImportEntity getSubCommand() {
		return getCommand().getCommand();
	}

	/**
	 * 每次读取的行数
	 * @return 记录数
	 */
	private int getReadRows() {
		return getSubCommand().getRows();
	}

	/**
	 * 循环执行上传数据
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		// 循环处理，直到完成
		while (true) {
			// 上传数据
			int no = upload();
			// 以下是上传结果
			if (no == -1) {
				reply(false);
				// 不成功退出！
				return useful(false);
			} else if (no == 0) {
				// 全部完成
				reply(true);
				// 成功退出
				return useful(true);
			} else if (no > 3) {
				//成功接收到反馈结果。步骤恢复到1，不退出，继续下一轮。
				step = 1; 
				continue;
			} else {
				// 一个阶段的成功，退出！
				break;
			}
		}

		Logger.debug(this, "todo", "writed rows:%d, next section ...", count);

		return true;
	}

	/**
	 * 反馈最后结果给调用端
	 * @param success 成功或者否
	 */
	private void reply(boolean success) {
		ShiftSingleImportEntity shift = getCommand();
		SingleImportEntity cmd = shift.getCommand();

		SingleImportEntityResult res = new SingleImportEntityResult(success, cmd.getFile(), count);

		SingleImportEntityHook hook = shift.getHook();
		// 设置结果和唤醒
		hook.setResult(res);
		hook.done();
	}

	/**
	 * 顺序依次插入数据到集群，返回值：
	 * 1. -1， 出错
	 * 2. 0，成功
	 * 3. 大于3，本轮插入数据到集群的工作已经完成，继续下一轮。
	 * 4. 小于等于3，本次操作完成，退出准备进入下一次操作。
	 * @return 返回对应的码值
	 */
	private int upload() {
		Logger.debug(this, "upload", "step is %d, read rows:%d", step, getReadRows());

		boolean success = false;
		switch(step) {
		case 1:
			// 1. 从磁盘读一组CSV数据
			inject = readCSV(table);
			success = (inject != null);
			// 2. 如果有效，建立异步操作
			if (success) {
				if (inject.size() > 0) {
					Logger.debug(this, "upload", "to attempt! size %d", inject.size());
					success = attempt();
				} else {
					Logger.warning(this, "upload", "inject is empty");
					// 跳过，直接
					inject = null;
					step = 4;
				}
			} else {
				Logger.error(this, "upload", "inject is null pointer!");
			}
			break;
		case 2:
			// 发送数据到集群
			success = send(inject);
			if (success) {
				count += inject.size(); // 统计行数
			}
			break;
		case 3:
			success = receive();
			break;
		}
		// 发生错误
		if (!success) {
			Logger.error(this, "upload", "occur error! exit!");
			return -1;
		}

		// 自增1
		step++;

		// 判断完成
		boolean finished = (step > 3 && reader == null);

		Logger.debug(this, "upload", "step： %d, send count: %d, %s",
				step, count, (finished ? "finished!" : "next ..."));

		// 插入完成，返回0，否则返回步骤值
		return (finished ? 0 : step);
	}

	/**
	 * 解析一行记录
	 * @param table
	 * @param items
	 * @return 返回行
	 * @throws IOException
	 */
	private Row splitRow(Table table, String[] items) throws IOException {
		Row row = new Row();
		for (int index = 0; index < items.length; index++) {
			// 根据列名找到相关列属性
			ColumnAttribute attribute = table.find(columnNames[index]);
			if (attribute == null) {
				Logger.error(this, "splitRow", "cannot be find %s", columnNames[index]);
				return null;
			}

			// 忽略这一行
			String value = items[index];
			if (value == null) {
				throw new IOException("column %d " + index + " is null pointer");
			} else if (value.trim().isEmpty()) {
				//				Logger.warning(this, "readCSV", "column %d is empty!", index);
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

			if (column == null) {
				Logger.error(this, "splitRow", "illegal %s", value);
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
	 * 读CSV文件，返回一个列实例 
	 * @param table 数据表
	 * @return 返回实例 ，失败返回空指针。
	 */
	private Insert readCSV(Table table) {
		// 建立一行数据
		Insert insert = new Insert(table.getSpace());
		// 逐行读取
		int readRows = getReadRows();
		try {
			// 以行为单位读取
			for (int n = 0; n < readRows; n++) {
				// 读一行记录，输出为列数组
				String[] items = reader.readRow();
				// 空指针时，是结束了
				if (items == null) {
					Logger.info(this, "readCSV", "Read file, finished!");
					// 关闭缓冲
					reader.close();
					reader = null;
					// 退出！
					break;
				}

				// 解析一行
				Row row = splitRow(table, items);
				// 保存一行记录
				if (row != null) {
					insert.add(row);
				}
			}
		} catch (IOException e) {
			Logger.error(e);
			fault(e.getMessage());
			return null;
		} catch (Throwable e) {
			Logger.fatal(e);
			fault(e.getMessage());
			return null;
		}

		Logger.debug(this, "readCSV", "insert rows: %d", insert.size());

		// 插入返回
		return insert;
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
	 * 数据写入第一步：<br>
	 * 1. 从CALL注册站点池中枚举一个地址。<br>
	 * 2. 尝试向它发送一个写入标识。<br><br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean attempt() {
		SingleImportEntity cmd = getSubCommand();
		Space space = cmd.getSpace();
		// 根据数据表名，找一个地址
		NodeSet set = getStaffPool().findTableSites(space);

		// 枚举一个站点地址
		Node hub = (set != null ? set.next() : null);
		boolean success = (hub != null);
		if (!success) {
//			faultX(FaultTip.SITE_MISSING);
			return false;
		}

		// 向CALL站点发送确认命令
		InsertGuide guide = new InsertGuide(space);
		success = fireToHub(hub, guide);

		Logger.debug(this, "attempt", success, "submit to %s", hub);

		return success;
	}

	/**
	 * 发送数据
	 * @param insert 插入命令
	 * @return 成功返回真，否则假
	 */
	private boolean send(Insert insert) {
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
//			faultX(FaultTip.FAILED_X, getCommand());
			return false;
		}

		// 3. 传输过程中不做封装，INSERT以原始数据格式发送到CALL站点，CALL站点原样转发到DATA主站点
		Cabin hub = guide.getSource();
		ReplyItem item = new ReplyItem(hub, insert.build());
		success = replyTo(item);

//		// 不成功提示
//		if (!success) {
////			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
//		}

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
		boolean success = (product != null && product.isSuccessful());

//		// 不成功，弹出错误
//		if (!success) {
//			printFault();
//		}

		Logger.debug(this, "receive", success, "from hub");

		// 工作完成，是否完成退出由上层事务决定！！！
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SingleImportEntity cmd = getSubCommand();
		// 1. 查找数据表
		Space space = cmd.getSpace();
		table = getStaffPool().findTable(space);
		if (table == null) {
			reply(false);
			return false; // 出错
		}
		
		// 2. 判断有导入数据块的权限
		boolean success = getStaffPool().canTable(space, ControlTag.IMPORT_ENTITY);
		if (!success) {
			reply(false);
			return false;
		}

		// 3. 从磁盘读数据
		File file = cmd.getFile();
		int charset = cmd.getCharset();
		// 判断是文件类型
		if (EntityStyle.isCSV(cmd.getType())) {
			reader = new CSVRowReader(file, charset);
		} else if (EntityStyle.isTXT(cmd.getType())) {
			reader = new TXTRowReader(file, charset);
		} else {
			reply(false);
			return false; // 出错
		}
		
		try {
			columnNames = reader.readTitle();
			Logger.debug(this, "launch", "%s column count %d", space, columnNames.length);
		} catch (IOException e) {
			Logger.error(e);
			reply(false);
			return false; // 出错
		}

		// 处理上传操作
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 继续上传
		return todo();
	}

}