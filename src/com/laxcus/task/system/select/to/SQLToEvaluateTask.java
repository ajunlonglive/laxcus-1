/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.classable.*;

/**
 * “EVALUATE”模式的SQL操作。<br>
 * 所有基于“EVALUATE”模式的SQL操作从这里派生。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2013
 * @since laxcus 1.0
 */
public abstract class SQLToEvaluateTask extends ToEvaluateTask {

	/**
	 * 构造SQLToEvaluateTask实例
	 */
	public SQLToEvaluateTask() {
		super();
	}

	/**
	 * 根据标题找到一个SELECT句柄
	 * @param title
	 * @param session
	 * @return
	 * @throws ToTaskException
	 */
	protected Select findSelect(String title, ToSession session) throws ToTaskException {
		TaskParameter value = session.findParameter(title);
		if (value == null || !value.isCommand()) {
			throw new ToTaskException("cannot be find SELECT object!");
		}
		return (Select) (((TaskCommand) value).getValue());
	}

	/**
	 * 数据重组操作。操作流程：<br>
	 * 取出表中的函数，根据实际列参数，产生新的行，设置到原来的位置
	 * @param sheet
	 * @param array
	 * @throws ToTaskException 
	 */
	protected void realign(ListSheet sheet, List<Row> array) throws ToTaskException {
		//	Logger.debug("SQLToTask.realign, sheet size is %d", sheet.size());

		Logger.debug(getIssuer(), this, "realign", "sheet size is %d", sheet.size());

		ArrayList<Row> list = new ArrayList<Row>(1);
		for(int index = 0; index < array.size(); index++) {
			Row row = array.get(index);

			if (list.size() > 0) list.clear();
			list.add(row);

			//			Logger.debug("SQLToTask.realign, column size %d", row.size());

			Row rs = new Row();
			for(ListElement element : sheet.list()) {
				// 如果是列成员，返回列标识号；如果是函数成员，返回临时的函数编号(在列标识号之外的增加)
				short identity = element.getIdentity();

				if (element.isColumn()) {
					Column column = row.find(identity);
					if (column == null) {
						throw new ToTaskException("cannot find column by %d", identity);
					}
					rs.add(column);
				} else if (element.isFunction()) {
					ColumnFunction function = ((FunctionElement) element).getFunction();
					// 根据函数，生成列成员，并且指定标识号
					Column column = function.makeup(list);
					if(column == null) {
						throw new ToTaskException("cannot makeup column by %d", identity);
					}
					column.setId(identity);
					rs.add(column);
				}
			}

			// 在原下标处更新
			array.set(index, rs);
		}
	}

	/**
	 * 产生基于行存储模型的数据流，包括MassFlag和行数据流
	 * @return 字节数组
	 */
	protected byte[] doRowsStream(long mod, Space space, List<Row> rows) {
		short columnCount = 0;
		if (rows.size() > 0) {
			columnCount = (short) rows.get(0).size();
		}
		// 数据头部信息(输出的数据流，确定是行存储模式)
		MassFlag flag = new MassFlag();
		flag.setMod(mod);
		flag.setRows(rows.size());
		// flag.setColumns((short) rows.get(0).size());
		flag.setColumns(columnCount);
		flag.setModel(StorageModel.NSM);
		flag.setSpace(space);
		byte[] head = flag.build();

		// 确定分配所需总空间
		int allen = head.length;
		for (Row row : rows) {
			allen += row.capacity();
		}

		// 开辟内存空间
		allen = allen - (allen % 1024) + 1024;
		ClassWriter buff = new ClassWriter(allen);
		// 保存头记录
		buff.write(head, 0, head.length);
		// 保存行记录
		for (Row row : rows) {
			row.buildX(buff);
		}

		// 输出全部数据流
		byte[] data = buff.effuse();
		// 重装定义数据流长度
		flag.setLength(data.length - head.length);
		// 更新数据头部信息
		head = flag.build();
		// 输出到磁盘开始位置
		System.arraycopy(head, 0, data, 0, head.length);

		return data;
	}

	/**
	 * 计算全部记录的数据尺寸
	 * @param rows
	 * @return
	 */
	protected long doRowsCapacity(List<Row> rows) {
		long size = 0;
		for (Row row : rows) {
			size += row.capacity();
		}
		return size;
	}

	/**
	 * 产生FluxArea数据流
	 * @return - 字节数组
	 * @throws TaskException
	 */
	protected byte[] doFluxArea() throws TaskException {
		FluxArea area = createFluxArea();
		return area.build();
	}

	/**
	 * 输出下一阶段数据，返回下一阶段的FluxArea字节数组长度
	 * @param rows - 记录
	 * @return - 返回FluxArea字节数组长度
	 * @throws TaskException
	 */
	protected long doStepSector(List<Row> rows) throws TaskException {
		ToSession session = super.getSession();
		// 取它下一级的分割器
		ColumnSector sector = session.getIndexSector();
		// Dock dock = session.getIndexDock();

		// 输出分割结果
		super.splitWriteTo(sector, rows);

		// 返回FluxArea字节数组
		byte[] b = this.doFluxArea();

		return b.length;
	}

	/**
	 * 输出数据流
	 * @param space
	 * @param rows
	 * @return
	 */
	protected byte[] doRows(Space space, List<Row> rows) {
		// 形成新的数据流
		MassFlag flag = new MassFlag();
		flag.setRows(rows.size());
		flag.setColumns((short) rows.get(0).size());
		flag.setSpace(space);
		flag.setModel(StorageModel.NSM);

		// 统计总的数据流长度
		byte[] head = flag.build();
		int total = head.length;
		for (Row row : rows) {
			total += row.capacity();
		}
		total = total - total % 128 + 128;

		// 数据输出到缓存
		ClassWriter buff = new ClassWriter(total);
		buff.write(head, 0, head.length);
		for (Row row : rows) {
			row.buildX(buff);
		}

		// 行记录数据流
		byte[] data = buff.effuse();
		// 更新尺寸
		flag.setLength(data.length - head.length);
		head = flag.build();
		System.arraycopy(head, 0, data, 0, head.length);

		return data;
	}

	/**
	 * 写数据到磁盘文件
	 * @param space
	 * @param rows
	 * @param file
	 * @throws IOException
	 */
	protected long writeRows(Space space, List<Row> rows, File file) throws TaskException {
		// 形成新的数据流
		MassFlag flag = new MassFlag();
		flag.setRows(rows.size());
		flag.setColumns((short) rows.get(0).size());
		flag.setSpace(space);
		flag.setModel(StorageModel.NSM);

		// 统计总的数据流长度
		byte[] head = flag.build();

		try {
			FileOutputStream out = new FileOutputStream(file);
			// 输出文件头
			out.write(head);
			// 逐行输出记录
			for (Row row : rows) {
				byte[] b = row.buildX();
				out.write(b);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}

		// 更新头差距数据参数
		long length = file.length();
		flag.setLength(length - head.length);
		head = flag.build();

		// 重写头部数据
		try {
			RandomAccessFile rnd = new RandomAccessFile(file, "rws");
			rnd.seek(0L);
			rnd.write(head);
			rnd.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}

		return file.length() - length;
	}


	/**
	 * 分割数据，写入磁盘，返回元信息
	 * @param results
	 * @return
	 * @throws TaskException
	 */
	protected byte[] flushNextSector(List<Row> results) throws TaskException {
		ToSession session = super.getSession();

		// 取它下一级的分割器
		ColumnSector sector = session.getIndexSector();
		//		Dock dock = session.getIndexDock();

		// 输出分割结果
		super.splitWriteTo(sector, results);

		// 返回结果
		FluxWriter writer = this.fetchWriter();
		FluxArea area = writer.collect();
		// 设置迭代编号（必须！）
		area.setIterateIndex(session.getIterateIndex());

		Logger.debug(getIssuer(), this, "effuse", "iterate index is:%d", session.getIterateIndex());

		return area.build();
	}

	/**
	 * 生成数据输出
	 * @param space
	 * @param results
	 * @return
	 */
	protected byte[] flushResult(Space space, List<Row> results) {
		// 形成新的数据流
		MassFlag flag = new MassFlag();
		flag.setRows(results.size());
		flag.setColumns((short) results.get(0).size());
		flag.setSpace(space);
		flag.setModel(StorageModel.NSM);

		// 统计总的数据流长度
		byte[] head = flag.build();
		int total = head.length;
		for (Row row : results) {
			total += row.capacity();
		}
		total = total - total % 128 + 128;

		// 数据输出到缓存
		ClassWriter buff = new ClassWriter(total);
		buff.write(head, 0, head.length);
		for (Row row : results) {
			row.buildX(buff);
		}

		// 行记录数据流
		byte[] data = buff.effuse();
		// 更新尺寸
		flag.setLength(data.length - head.length);
		head = flag.build();
		System.arraycopy(head, 0, data, 0, head.length);

		return data;
	}

}