/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.column.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * SELECT客户机。<br>
 * 保护类型，限本地使用，通过JNI接口查询执行一个本地检索。
 * 
 * @author scott.liang
 * @version 1.0 9/18/2017
 * @since laxcus 1.0
 */
class SelectTasker {

	/**
	 * 构造默认的SELECT客户机
	 */
	public SelectTasker() {
		super();
	}

	/**
	 * 执行本地的SELECT检索工作
	 * @param cmd SELECT命令
	 * @param stub 数据块
	 * @return 返回检索结果（是AccessStack的内存段），没有是空指针。
	 * @throws TaskException
	 */
	public byte[] process(Select cmd, long stub) throws TaskException {
		// 如果有分组，直接读取
		if (cmd.hasGroup()) {
			return direct(cmd, stub);
		}
		// 如果有函数且用户要求自动调整时，重组产生新的记录，否则直接查询输出
		else if (cmd.hasFunctions() && cmd.isAutoAdjust()) {
			return redirect(cmd, stub);
		}
		// 其他情况...
		else {
			return direct(cmd, stub);
		}
	}


	/**
	 * 使用属性列排序，把每一列重组后输出
	 * @param flag MassFlag实例
	 * @param sheet 属性排序集合
	 * @param rows 行记录数组
	 * @return 返回重组后的字节数组
	 * @throws TaskException
	 */
	private byte[] realign(ListSheet sheet, List<Row> list) {
		ClassWriter buff = new ClassWriter(10240);

		ArrayList<Row> array = new ArrayList<Row>(1);

		for(Row row : list) {
			if (array.size() > 0) {
				array.clear();
			}
			array.add(row);

			Row that = new Row();
			for (ListElement element : sheet.list()) {
				// 如果是列成员，返回列标识号；如果是函数成员，返回临时的函数编号(在列标识号之外的增加)
				short identity = element.getIdentity();

				if (element.isColumn()) {
					Column column = row.find(identity);
					if (column == null) {
						throw new ColumnException("cannot find column by %s-%d",
								element.getSpace(), identity);
					}
					that.add(column);
				} else if (element.isFunction()) {
					ColumnFunction function = ((FunctionElement) element).getFunction();
					// 根据函数，生成列成员，并且指定标识号
					Column column = function.makeup(array);
					if (column == null) {
						throw new ColumnException("cannot make column by %s-%d",
								element.getSpace(), identity);
					}
					column.setId(identity);
					that.add(column);
				}
			}

			// 转为数据流输出到写入器
			that.buildX(buff);
		}
		// 输出数据
		return buff.effuse();
	}

	/**
	 * 根据SELCT命令查找记录，并且根据SQL函数重组数据输出
	 * @param cmd SELECT命令
	 * @param stub 数据块编号
	 * @return AccessStack内容段重组后的数据
	 * @throws TaskException
	 */
	private byte[] redirect(Select cmd, long stub) throws TaskException {
//		Logger.debug(this, "redirect", "find %s from %x", cmd.getSpace(), stub);

		// 提取数据表
		Space space = cmd.getSpace();
		Table table = StaffOnDataPool.getInstance().findTable(space);
		if (table == null) {
			throw new TaskException("cannot not be find '%s'", space);
		}
		// SELECT中的全部显示成员集合，包括列成员，操作列的函数成员，不操作列的函数成员
		ListSheet listSheet = cmd.getListSheet();
		// 按照显示列，生成列属性顺序表
		Sheet sheet = listSheet.getColumnSheet(table);

		// 调用JNI解析解析
		byte[] b = null;
		try {
			b = AccessTrustor.select(cmd, stub);
		} catch (AccessException e) {
			throw new TaskAccessException(e);
		}

		// 分析处理结果，取出数据内容
		AccessStack stack = new AccessStack(b);
		// 取出数据内容，如果是空值退出
		b = stack.getContent();
		if (Laxkit.isEmpty(b)) {
			return null;
		}

		// 分析报头，确定全部数据流长度(标记头和检索数据)
		MassFlag flag = new MassFlag();
		int off = flag.resolve(b, 0, b.length);

		//		System.out.printf("SelectTasker.redirect, show sheet columns %d, sheet columns %d, flag columns %d", 
		//				listSheet.size(), sheet.size(), flag.getColumns());

		// 解析输出的结果，在原记录结果上产生新的记录，并且以字节数组输出到内存
		RowCracker cracker = new RowCracker(sheet);
		cracker.split(b, off, b.length - off);
		List<Row> results = cracker.flush();
		Logger.debug(this, "redirect", "first row size %d", results.size());

		// 重组数据，返回新的数据流
		byte[] data = realign(listSheet, results);

		// 重新定义标头信息
		flag.setColumns((short) listSheet.size());
		flag.setModel(StorageModel.NSM);
		flag.setLength(data.length);

		// 输出字节数组
		byte[] head = flag.build();
		ContentBuffer buff = new ContentBuffer(head.length + data.length);
		buff.append(head);
		buff.append(data);
		return buff.toByteArray();
	}

	/**
	 * 直接调用JNI接口并且输出数据
	 * @param cmd SELECT命令
	 * @param stub 数据块编号
	 * @return 返回AccessStack内容段数据，没有找到是空指针
	 */
	private byte[] direct(Select cmd, long stub) throws TaskException {		
		// 调用JNI.SELECT接口
		byte[] b = null;
		try {
			b = AccessTrustor.select(cmd, stub);
		} catch (AccessException e) {
			throw new TaskAccessException(e);
		}
		
		// 解析SELECT结果数据，内容域存在两种可能：字节数组是空值，或者有数据
		AccessStack stack = new AccessStack(b);
		// 返回内容域（空指针或者有数据）
		return stack.getContent();
	}

	//	/**
	//	 * 根据SELCT命令查找记录，并且根据SQL函数重组数据输出
	//	 * @param cmd SELECT命令
	//	 * @param stub 数据块编号
	//	 * @return AccessStack内容段重组后的数据
	//	 * @throws TaskException
	//	 */
	//	private byte[] redirect(Select cmd, long stub) throws TaskException {
	////		Logger.debug(this, "redirect", "find %s from %x", cmd.getSpace(), stub);
	//		
	//		// 提取数据表
	//		Space space = cmd.getSpace();
	//		Table table = StaffOnDataPool.getInstance().findTable(space);
	//		if (table == null) {
	//			throw new TaskException("cannot not be find '%s'", space);
	//		}
	//		// SELECT中的全部显示成员集合，包括列成员，操作列的函数成员，不操作列的函数成员
	//		ListSheet listSheet = cmd.getListSheet();
	//		// 按照显示列，生成列属性顺序表
	//		Sheet sheet = listSheet.getColumnSheet(table);
	//
	//		// 调用JNI解析解析
	//		byte[] b = null;
	//		try {
	//			b = AccessTrustor.select(cmd, stub);
	//		} catch (AccessException e) {
	//			throw new TaskAccessException(e);
	//		}
	//
	//		// 分析处理结果，取出数据内容
	//		AccessStack stack = new AccessStack(b);
	//		// 取出数据内容，如果是空值退出
	//		b = stack.getContent();
	//		if (Laxkit.isEmpty(b)) {
	//			return null;
	//		}
	//
	//		// 分析报头，确定全部数据流长度(标记头和检索数据)
	//		MassFlag flag = new MassFlag();
	//		int off = flag.resolve(b, 0, b.length);
	//		
	////		System.out.printf("SelectTasker.redirect, show sheet columns %d, sheet columns %d, flag columns %d", 
	////				listSheet.size(), sheet.size(), flag.getColumns());
	//
	//		// 解析输出的结果，在原记录结果上产生新的记录，并且以字节数组输出到内存
	//		RowCracker cracker = new RowCracker(sheet);
	//		// 从报头之后开始解析
	//		cracker.split(b, off, b.length - off);
	//		List<Row> rows = cracker.flush();
	//		// 根据函数重组数据，输出新的数据流
	//		return realign(flag, listSheet, rows);
	//	}

	//	/**
	//	 * 使用属性列排序，把每一列重组后输出
	//	 * @param flag MassFlag实例
	//	 * @param sheet 属性排序集合
	//	 * @param rows 行记录数组
	//	 * @return 返回重组后的字节数组
	//	 * @throws TaskException
	//	 */
	//	private byte[] realign(MassFlag flag, ListSheet sheet, List<Row> rows) throws TaskException {
	//		// 重新定义标头信息
	//		flag.setColumns((short) sheet.size());
	//		flag.setModel(StorageModel.NSM);
	//		// 生成标头字节流
	//		byte[] head = flag.build();
	//
	//		ClassWriter buff = new ClassWriter(10240);
	//		// 保存头数据
	//		buff.write(head); 
	//
	//		ArrayList<Row> array = new ArrayList<Row>(1);
	//
	//		for(Row row : rows) {
	//			if (array.size() > 0) {
	//				array.clear();
	//			}
	//			array.add(row);
	//
	//			Row that = new Row();
	//			for(ListElement e : sheet.list()) {
	//				// 如果是列成员，返回列标识号；如果是函数成员，返回临时的函数编号(在列标识号之外的增加)
	//				short identity = e.getIdentity();
	//
	//				if (e.isColumn()) {
	//					Column column = row.find(identity);
	//					if (column == null) {
	//						throw new TaskException("cannot be find column by %s-%d", e.getSpace(), identity);
	//					}
	//					that.add(column);
	//				} else if (e.isFunction()) {
	//					ColumnFunction function = ((FunctionElement) e).getFunction();
	//					// 根据函数，生成列成员，并且指定标识号
	//					Column column = function.makeup(array);
	//					if (column == null) {
	//						throw new TaskException("cannot be make column by %s-%d", e.getSpace(), identity);
	//					}
	//					column.setId(identity);
	//					that.add(column);
	//				}
	//			}
	//
	//			// 转为数据流输出到写入器
	//			that.buildX(buff);
	//		}
	//
	//		// 设置内容域长度（内容域长度 = 总长度 - 报头长度）
	//		int contentSize = buff.size() - head.length; 
	//		flag.setLength(contentSize);
	//		// 从0下标替换报头信息
	//		head = flag.build();
	//		buff.replace(0, head);
	//		// 输出数据
	//		return buff.effuse();
	//	}

	//	/**
	//	 * 使用属性列排序，把每一列重组后输出
	//	 * @param flag MassFlag实例
	//	 * @param sheet 属性排序集合
	//	 * @param rows 行记录数组
	//	 * @return 返回重组后的字节数组
	//	 * @throws TaskException
	//	 */
	//	private byte[] realign1(MassFlag flag, ListSheet sheet, List<Row> rows) throws TaskException {
	//		// 重新定义标头信息
	//		flag.setColumns((short) sheet.size());
	//		flag.setModel(StorageModel.NSM);
	//		// 生成标头字节流
	//		byte[] head = flag.build();
	//
	//		ClassWriter buff = new ClassWriter(10240);
	//		// 保存头数据
	//		buff.write(head); 
	//
	//		ArrayList<Row> array = new ArrayList<Row>(1);
	//
	//		for(Row row : rows) {
	//			if (array.size() > 0) {
	//				array.clear();
	//			}
	//			array.add(row);
	//
	//			Row that = new Row();
	//			for(ListElement e : sheet.list()) {
	//				// 如果是列成员，返回列标识号；如果是函数成员，返回临时的函数编号(在列标识号之外的增加)
	//				short identity = e.getIdentity();
	//
	//				if (e.isColumn()) {
	//					Column column = row.find(identity);
	//					if (column == null) {
	//						throw new TaskException("cannot be find column by %s-%d", e.getSpace(), identity);
	//					}
	//					that.add(column);
	//				} else if (e.isFunction()) {
	//					ColumnFunction function = ((FunctionElement) e).getFunction();
	//					// 根据函数，生成列成员，并且指定标识号
	//					Column column = function.makeup(array);
	//					if (column == null) {
	//						throw new TaskException("cannot be make column by %s-%d", e.getSpace(), identity);
	//					}
	//					column.setId(identity);
	//					that.add(column);
	//				}
	//			}
	//
	//			// 转为数据流输出到写入器
	//			that.buildX(buff);
	//		}
	//
	//		// 设置内容域长度（内容域长度 = 总长度 - 报头长度）
	//		int contentSize = buff.size() - head.length; 
	//		flag.setLength(contentSize);
	//		// 从0下标替换报头信息
	//		head = flag.build();
	//		buff.replace(0, head);
	//		// 输出数据
	//		return buff.effuse();
	//	}

}