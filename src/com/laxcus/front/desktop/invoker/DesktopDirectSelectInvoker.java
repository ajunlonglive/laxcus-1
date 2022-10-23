/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * 简单SELECT调用器 <br>
 * 
 * 不包含嵌套等查询语句的SELECT命令。 
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopDirectSelectInvoker extends DesktopRuleInvoker {

	/** 处理步骤，从1开始**/
	private int step = 1;

	/**
	 * 构造标准SELECT调用器 ，指定命令
	 * @param cmd SELECT命令
	 */
	public DesktopDirectSelectInvoker(Select cmd) {
		super(cmd);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Select getCommand() {
		return (Select) super.getCommand();
	}

	/**
	 * 建立事务规则
	 */
	private void initRule() {
		Select cmd = getCommand();
		// 保存SELECT表事务处理规则
		addRules(cmd.getRules());
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

		//		// 如果出错，在终端提示，算是完成
		//		if (!success) {
		//			fault("select failed!");
		//		}

		// 自增1
		step++;
		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 执行第一阶段操作。找到一个CALL站点，把命令发给它
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		Select cmd = getCommand();

		// 根据数据表名，找到它的对应CALL站点集合
		Space space = cmd.getSpace();
		NodeSet set = getStaffPool().findTableSites(space);
		// 顺序枚举一个CALL站点地址，保持调用平衡
		Node hub = (set != null ? set.next() : null);
		// 没有找到，弹出错误
		if (hub == null) {
			faultX(FaultTip.NOTFOUND_SITE_X, space);
			// 通知调用端
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			}
			return false;
		}

		// 发送到目标地址
		boolean success = fireToHub(hub, cmd); 

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 执行第二阶段操作，显示数据处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		ProductListener listener = getProductListener();

		// 出错
		if (isFaultCompleted()) {
			printFault();
			// 通知调用器
			if (listener != null) {
				listener.push(null);
			}
			return false;
		}

		// 判断数据全部在文件
		boolean ondisk = isEchoFiles();
		// 读磁盘文件
		byte[] content = null;

		// 判断数据在内存或者磁盘
		if (ondisk) {
			File file = findFile(0);
			try {
				content = readFile(file);
			} catch (IOException e) {
				Logger.error(e);
				fault(e);
			} catch (Throwable e) {
				Logger.fatal(e);
				fault(e);
			}
		} else {
			content = collect();
		}

		// 转交给调用器
		if (listener != null) {
			listener.push(content);
		} else {
			// 在应用上显示结果
			try {
				long rows = print(content);
				printRows(rows);
			} catch (IOException e) {
				Logger.error(e);
				fault(e);
			} catch (Throwable e) {
				Logger.fatal(e);
				fault(e);
			}
		}

		// 正常退出
		return true;
	}

	/**
	 * 显示检索行数
	 * @param rows
	 */
	private void printRows(long rows) {
		String prefix = getXMLContent("SQL/SELECT-X");
		String text = String.format(prefix, rows);
		setStatusText(text);
	}

	/**
	 * 显示标题
	 */
	private void showTitle(Sheet sheet) {
		// 显示标题
		ShowTitle title = new ShowTitle();
		int size = sheet.size();
		for(int index = 0; index < size; index++) {
			ColumnAttribute attribute = sheet.get(index);
			// 宽度默认是120像素
			int width = 120;
			if (attribute.isNumber()) {
				width = 50;
			} else if (attribute.isCalendar()) {
				width = 80;
			}
			// 保存一列标题单元
			title.add(new ShowTitleCell(index, attribute.getNameText(), width));
		}
		setShowTitle(title);
	}

	/**
	 * 读取磁盘内存
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private byte[] readFile(File file) throws IOException {
		int len = (int) file.length();
		byte[] b = new byte[len];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		return b;
	}

	/**
	 * 打印标题，返回排序表
	 * @return
	 */
	private Sheet printTitle(){
		// 打印耗时
		printRuntime();

		Select cmd = getCommand();
		ListSheet listSheet = cmd.getListSheet();

		Table table = getStaffPool().findTable(cmd.getSpace());
		Sheet sheet = listSheet.getDisplaySheet(table);

		// 打印标题
		showTitle(sheet);

		return sheet;
	}

	/**
	 * 打印数据
	 * @param b
	 * @return 返回解析的行数
	 */
	private long print(byte[] b) throws IOException {
		Logger.debug(this, "print", "select content size:%d", (b != null ? b.length : -1));

		// 标题
		Sheet sheet = printTitle();

		long end = (b != null && b.length > 0 ? b.length : 0);
		if (end < 1) {
			flushTable();
			Logger.warning(this, "print", "byte length:%d", end);
			return end;
		}

		long seek = 0L;
		long count = 0L; // 统计行数

		ByteArrayInputStream reader = new ByteArrayInputStream(b);
		while (seek < end) {
			MassFlag flag = new MassFlag();
			int len = flag.resolve(reader);
			seek += len;

			// 计算数据域有效
			long available = flag.getLength();
			// 如果0长度，忽略
			if (available == 0) {
				continue;
			} 
			// 判断范围
			if (seek + available > end) {
				throw new IllegalValueException("% + %d > %d", seek, available, end);
			}

			// 读内容
			byte[] data = new byte[(int) available];
			available = reader.read(data);

			if (data.length != available) {
				throw new IllegalValueException("%d != %d", data.length, available);
			}
			// 统计读取文件长度
			seek += available;

			// 显示一组数据
			int rows = print(sheet, flag, data);
			count += rows;
		}

		reader.close();

		// 输出全部记录
		flushTable();

		return count;
	}

	/**
	 * 打印记录
	 * @param sheet
	 * @param flag
	 * @param data
	 * @return
	 */
	private int print(Sheet sheet, MassFlag flag, byte[] data) {
		RowCracker cracker = new RowCracker(sheet);
		int len = cracker.split(data);
		if (flag.getLength() != len) {
			throw new IllegalValueException("% != %d", flag.getLength(), len);
		}

		// 输出记录
		List<Row> rows = cracker.flush();

		// 显示记录
		for (Row row : rows) {
			//			test(row);
			String[] all = TerminalKit.showRow(sheet, row);
			ShowItem item = new ShowItem();
			for (int i = 0; i < all.length; i++) {
				item.add(new ShowStringCell(i, all[i]));
			}
			addShowItem(item);
		}

		// 返回行数
		return rows.size();
	}

	//	private void test(Row row) {
	//		for(Column column : row.list()) {
	//			if(column.getClass() == Char.class) {
	//				Char c = (Char)column;
	//				byte[] is = c.getIndex();
	//				byte[] vs = c.getValue();
	//				Logger.error(this, "test", "char id:%d, index:%d, value:%d", 
	//						c.getId(), (is == null ? -1 : is.length), (vs==null ? -1 : vs.length) );
	//			}
	//		}
	//	}

}


//	/**
//	 * 根据文件，打印内容
//	 * @param file 磁盘文件
//	 * @return 返回解析行数
//	 * @throws IOException
//	 */
//	private long print(File file) throws IOException {
//		Sheet sheet = printTitle();
//
//		long end = file.length();
//		if (end < 1) {
//			flushTable();
//			Logger.warning(this, "print", "file length:%d", end);
//			return end;
//		}
//
//		long seek = 0L;
//		long rows = 0L; // 统计行数
//		
//		byte[] b = this.readFile(file);
//		ByteArrayInputStream reader = new ByteArrayInputStream(b);
//
////		FileInputStream reader = new FileInputStream(file);
//		while (seek < end) {
//			MassFlag flag = new MassFlag();
//			int len = flag.resolve(reader);
//			seek += len;
//
//			// 计算数据域有效
//			long available = flag.getLength();
//			if (seek + available > end) {
//				throw new IllegalValueException("% + %d > %d", seek, available, end);
//			}
//
//			// 读磁盘数据
//			byte[] data = new byte[(int) available];
//			available = reader.read(data);
//			if (data.length != available) {
//				throw new IllegalValueException("%d != %d", data.length, available);
//			}
//			// 统计读取文件长度
//			seek += available;
//
//			// 显示一组数据
//			int count = print(sheet, flag, data);
//			rows += count;
//		}
//
//		reader.close();
//		
//		// 输出全部记录
//		flushTable();
//
//		return rows;
//	}

//	/**
//	 * 根据文件，打印内容
//	 * @param file 磁盘文件
//	 * @return 返回解析行数
//	 * @throws IOException
//	 */
//	private long print(File file) throws IOException {
//		byte[] b = readFile(file);
//		return print(b);
//	}
