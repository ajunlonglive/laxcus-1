/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.evaluate;

import java.io.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.select.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.*;

/**
 * SQL SELECT检索"DISTINCT"关键字在TO阶段的处理方案。
 * 
 * @author scott.liang
 * @version 1.2 12/08/2014
 * @since laxcus 1.0
 */
public class SelectDistinctToTask extends SelectToEvaluateTask {

	/** 唯一行参数集合 **/
	private DistinctRecord array;

	/**
	 * 构造"SELECT DISTINCT"的TO阶段实例
	 */
	public SelectDistinctToTask() {
		super();
	}

	/**
	 * 建立列属性顺序表
	 * @param space
	 * @throws TaskException
	 */
	private void createListSheet(Space space) throws TaskException {
		//		// 取出SELECT命令
		//		ToSession session = getSession();
		//		select = (Select) session.getCommand();

		//		// 从会话中取得SELECT命令（标准SELECT查询情况：GROUP BY/ORDER BY/DISTINCT语句块）
		//		ToSession session = getSession();
		//		select = (Select) session.getCommand();
		//		// 如果SELECT不在会话命令中，就是以自定义参数身份存在（嵌套查询等情况）
		//		if (select == null) {
		//			if (!session.hasValue(SQLTaskKit.SELECT_OBJECT)) {
		//				throw new ToTaskException("cannot be find \"SELECT_OBJECT\"");
		//			}
		//			select = (Select) session.findCommand(SQLTaskKit.SELECT_OBJECT);
		//		}

		// 从会话中获得SELECT命令
		select = fetchSelect();

		// 检查表名
		if (Laxkit.compareTo(select.getSpace(), space) != 0) {
			throw new ToTaskException("cannot be match %s - %s", space, select.getSpace());
		}

		// 查找数据表配置
		Table table = findTable(space); 
		// 有函数，并且前面有GROUP BY时，GROUP BY已经重组了数据，这里必须使用重组后的表单
		// 因为经过函数处理后，列数目和排列会有变化
		ListSheet sheet = select.getListSheet();
//		if (sheet.hasFunctions() && select.hasGroup()) {
		if (sheet.hasFunctions()) {
			super.indexSheet = sheet.getDisplaySheet(table); // 显示列表，包括普通列和函数处理结果列
		} else {
			// 否则只提取列属性，不包括函数列
			super.indexSheet = sheet.getColumnSheet(table);
		}
	}

	private void calculate(MassFlag flag, byte[] b, int off, int len) throws TaskException {
		// 当列属性顺序表不存在时，建立它
		if (indexSheet == null) {
			createListSheet(flag.getSpace());
			array = new DistinctRecord(indexSheet);
		}

		// 根据列属性顺序表，解析每一行记录
		RowCracker cracker = new RowCracker( indexSheet);
		cracker.split(b, off, len);
		// 逐一输出和判断比较
		while (cracker.hasRows()) {
			array.add(cracker.poll());
		}
	}

	/**
	 * @param field
	 * @param file
	 * @return
	 * @throws TaskException
	 */
	private boolean calculate(FluxField field, File file) throws TaskException {
		long seek = 0L;
		long end = file.length();

		try {
			FileInputStream in = new FileInputStream(file);
			while (seek < end) {
				MassFlag flag = new MassFlag();
				int size = flag.resolve(in);
				seek += size;

				Logger.debug(getIssuer(), this, "calculate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
						flag.getLength(), flag.getRows(), flag.getColumns());

				if (seek + flag.getLength() > end) {
					throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
				}

				// 读数据到磁盘
				byte[] b = new byte[(int) flag.getLength()];
				size = in.read(b, 0, b.length);
				if (size != b.length) {
					throw new ToTaskException("%d != %d", size, b.length);
				}
				seek += size;

				calculate(flag, b, 0, b.length);
			}
			in.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, java.io.File)
	 */
	@Override
	public boolean evaluate(FluxField field, File file) throws TaskException {
		return calculate(field, file);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException {
		int seek = off;
		int end = off + len;

		while (seek < end) {
			// 解析结果的标记头信息
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;

			Logger.debug(getIssuer(), this, "evaluate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
					flag.getLength(), flag.getRows(), flag.getColumns());

			if(seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

			this.calculate(flag, b, seek, (int) flag.getLength());
			seek += flag.getLength();
		}

		Logger.debug(getIssuer(), this, "evaluate", "completed! row size:%d", array.size());

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		ListSheet sheet = select.getListSheet();
		// 如果有函数，但是前面没有GROUP BY，在这里进行重组
		if(sheet.hasFunctions() && !select.hasGroup()) {
			this.realign(sheet, array.list());
		}

		// 如果有子集迭代，分割和保存数据，返回FluxArea字节数组长度；否则是实体数据长度
		if (hasSessionSector()) {
			return doStepSector(array.list());
		} else {
			return doRowsCapacity(array.list()); 
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		Logger.debug(getIssuer(), this, "effuse", "into...");

		// 返回结果
		if (hasSessionSector()) {
			return doFluxArea();
		} else {
			Space space = select.getSpace();
			return doRows(space, array.list());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		long length = 0;
		if (hasSessionSector()) {
			byte[] b = doFluxArea();
			length = writeTo(file, false, b, 0, b.length);
		} else {
			Space space = select.getSpace();
			length = writeRows(space, array.list(), file);
		}
		return length;
	}
}