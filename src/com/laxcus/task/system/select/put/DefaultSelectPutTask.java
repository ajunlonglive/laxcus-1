/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.put;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.util.display.show.*;

/**
 * SELECT显示结果任务
 * 
 * @author scott.liang
 * @version 1.0 7/23/2012
 * @since laxcus 1.0
 */
public abstract class DefaultSelectPutTask extends PutTask {
	
	/** 统计行数 **/
	private int rows;

	/**
	 * 构造SELECT显示结果任务
	 */
	public DefaultSelectPutTask() {
		super();
		rows = 0;
	}
	
	/**
	 * 返回统计行数
	 * @return 行数
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * 打印行数
	 */
	protected void printRows() {
		int rows = getRows();
		// 判断是中文或者其他语种，分别显示
		if (isSimplfiedChinese()) {
			String str = String.format("行数 %d", rows);
			setStatusText(str);
		} else {
			String str = String.format("Rows %d", rows);
			setStatusText(str);
		}
	}

	/**
	 * 建立列顺序表
	 * @return 返回Sheet实例
	 * @throws TaskException
	 */
	protected Sheet createSheet() throws TaskException {
		Conduct conduct = getCommand();
		InitObject init = conduct.getInitObject();
		TaskParameter value = init.findParameter("SELECT_OBJECT");
		Select select = (Select) ((TaskCommand) value).getValue();

		Table table = getPutTrustor().findPutTable(select.getSpace());
		return select.getListSheet().getDisplaySheet(table);
	}

//	/**
//	 * 打印SELECT检索结果
//	 * @param sheet 列顺序表
//	 * @param b 字节数组
//	 * @param off 开始下标
//	 * @param len 有效长度
//	 * @throws TaskException
//	 */
//	protected long print(Sheet sheet, byte[] b, int off, int len) throws TaskException {
//		int seek = off;
//		int end = off + len;
//		while (seek < end) {
//			MassFlag flag = new MassFlag();
//			int size = flag.resolve(b, seek, end - seek);
//			seek += size;
//			// 判断长度溢出
//			if (seek + flag.getLength() > end) {
//				throw new PutTaskException("%d + %d > %d", seek, flag.getLength(), end);
//			}
//
//			//			int left = end - seek;
//			//			// 尺寸不足时...
//			//			if(left < flag.getLength()) {
//			//				throw new PutTaskException("left:%d < length:%d", left, flag.getLength());
//			//			}
//			//			
//			//			message("mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
//			//					flag.getLength(), flag.getRows(), flag.getColumns());
//			//
//			//			Logger.info(this, "showSelect",
//			//					"answer flag size:%d, column identity size: %d", size,
//			//					sheet.size());
//
//			// 读取指定长度的数据
//			RowCracker cracker = new RowCracker(sheet);
//			size = cracker.split(b, seek, (int) flag.getLength());
//
////			Logger.debug(getIssuer(), this, "showSelect", "rows:%d, size:%d", cracker.size(), size);
//
//			if (size != flag.getLength()) {
//				throw new PutTaskException("%d != %d", size, flag.getLength());
//			}
//			// 移动下标
//			seek += size;
//
//			// 逐行显示
//			while (cracker.hasRows()) {
//				Row row = cracker.poll();
//				ShowItem item = format(sheet, row);
//				addShowItem(item);
//				// 增加统计行数
//				rows++;
//			}
//		}
//
//		// 返回读取数据长度
//		return seek - off;
//	}

	/**
	 * 打印SELECT检索结果。每个TO节点，都会返回一个MassFlag+行数据流，需要逐个解析！
	 * 
	 * @param sheet 列顺序表
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @throws TaskException
	 */
	protected long print(Sheet sheet, byte[] b, int off, int len) throws TaskException {
		int seek = off;
		int end = off + len;
		while (seek < end) {
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;
			// 取出长度
			long length = flag.getLength();
			// 如果是空值，忽略，继续下一个
			if (length == 0) {
				continue;
			}
			// 判断长度溢出
			if (seek + length > end) {
				throw new PutTaskException("%d + %d > %d", seek, length, end);
			}

			// 读取指定长度的数据
			RowCracker cracker = new RowCracker(sheet);
			size = cracker.split(b, seek, (int) length);

//			Logger.debug(getIssuer(), this, "showSelect", "rows:%d, size:%d", cracker.size(), size);

			if (size != length) {
				throw new PutTaskException("cracker error! %d != %d", size, length);
			}
			// 移动下标
			seek += size;

			// 逐行显示
			while (cracker.hasRows()) {
				Row row = cracker.poll();
				ShowItem item = format(sheet, row);
				addShowItem(item);
				// 增加统计行数
				rows++;
			}
		}

		// 返回读取数据长度
		return seek - off;
	}
	
	/**
	 * 根据列集合的排列表和行记录，返回一组列信息记录
	 * 
	 * @param sheet 顺序表实例
	 * @param row 行记录
	 * @return 返回ShowItem实例
	 */
	protected ShowItem format(Sheet sheet, Row row) {
		int size = sheet.size();
		if (size != row.size()) {
			throw new ColumnException("not match size!");
		}

		ShowItem item = new ShowItem();

		for (int index = 0; index < size; index++) {
			ColumnAttribute attribute = sheet.get(index);
			// 根据列标识号查找对应的列
			Column column = row.find(attribute.getColumnId());

			// Logger.debug("TerminalUtil.showRow, column id:%d - %d, family:%d",
			// attribute.getColumnId(), column.getId(), column.getFamily());

			if (attribute.getType() != column.getType()) {
				throw new ColumnException("illegal attribute %d as %d", attribute.getType(), column.getType());
			}


			ShowStringCell cell = new ShowStringCell(index);
			// 如果是可变长类型
			if (attribute.isRaw()) {
				String s = ((Raw) column).toString(((VariableAttribute) attribute).getPacking());
				cell.setValue(s);
			} else if (attribute.isWord()) {
				String s = ((Word) column).toString(((WordAttribute) attribute).getPacking(), -1);
				cell.setValue(s);
			} else {
				cell.setValue(column.toString());
			}
			item.add (cell);
		}

		return item;
	}

	/**
	 * 显示标题
	 * @param sheet
	 */
	protected void showTitle(Sheet sheet) {
		ShowTitle title = new ShowTitle();
		int size = sheet.size();
		for (int index = 0; index < size; index++) {
			ColumnAttribute attribute = sheet.get(index);
			// 宽度
			int width = 120;
			if (attribute.isNumber()) {
				width = 60;
			} else if (attribute.isCalendar()) {
				width = 80;
			}
			ShowTitleCell cell = new ShowTitleCell(index, attribute.getNameText(), width);
			title.add(cell);
		}
		super.setShowTitle(title);
	}

	/**
	 * 打印记录
	 * @param b
	 * @param off
	 * @param len
	 * @return 返回读取数据长度
	 * @throws TaskException
	 */
	protected long print(byte[] b, int off, int len) throws TaskException {
		Sheet sheet = createSheet();
		showTitle(sheet);
		return print(sheet, b, off, len);
	}

	/**
	 * 读磁盘文件
	 * @param file
	 * @return
	 * @throws TaskException
	 */
	protected byte[] read(File file) throws TaskException {
		// 显示行记录数目
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();
			return b;
		} catch (IOException e) {
			throw new PutTaskException(e);
		}
	}

	/**
	 * 打印SELECT数据
	 * @param files 磁盘文件数组
	 * @return 返回处理的数据长度
	 * @throws IOException
	 */
	protected long print(File[] files) throws TaskException {
		Sheet sheet = createSheet();
		showTitle(sheet);

		long size = 0;
		for (File file : files) {
			byte[] b = read(file);
			long len = print(sheet, b, 0, b.length);
			size += len;
		}
		return size;
	}

}
