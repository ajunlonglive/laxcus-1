/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.evaluate;

import java.io.*;
import java.util.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.sort.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.*;

/**
 * SQL "GROUP BY"语句在TO阶段（CONVERGE）的处理方案。
 * 
 * @author scott.liang
 * @version 1.2 12/06/2014
 * @since laxcus 1.0
 */
public class SelectGroupByToTask extends SelectToEvaluateTask {

	/** 记录集合 **/
	private List<Row> records = new ArrayList<Row>(1024);

	/**
	 * 构造"GROUP BY"的TO阶段实例
	 */
	public SelectGroupByToTask() {
		super();
	}

	/**
	 * 建立行中的列形成对应关系的"列属性顺序表"，下标从0开始
	 * @param space
	 * @throws ToTaskException
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

		// 查找表
		Table table = findTable(space);
		// 建立与行中的"列"形成对应关系的"列属性顺序表"，下标从0开始
		indexSheet = select.getListSheet().getColumnSheet(table);
	}

	/**
	 * 解析和保存数据
	 * @param flag
	 * @param b
	 * @param off
	 * @param len
	 * @throws TaskException
	 */
	private void calculate(MassFlag flag, byte[] b, int off, int len) throws TaskException {
		// 如果排列表不存在，建立它
		if (indexSheet == null) {
			this.createListSheet(flag.getSpace());
		}
		// 解析数据流
		RowCracker cracker = new RowCracker(indexSheet);
		cracker.split(b, off, len);
		// 输出并且保存记录
		List<Row> rows = cracker.flush();
		// 保存记录，如果有SQL函数，在GroupSorter中处理
		records.addAll(rows);
	}

	/**
	 * 从磁盘文件读出数据后，进行计算
	 * @param field
	 * @param file
	 * @return - 成功返回真，否则假
	 * @throws TaskException
	 */
	private boolean calculate(FluxField field, File file) throws TaskException {
		long seek = 0L;
		long end = file.length();

		try {
			FileInputStream in = new FileInputStream(file);
			while (seek < end) {
				// 解析结果的标记头信息
				MassFlag flag = new MassFlag();
				int size = flag.resolve(in);
				seek += size;

				Logger.debug(getIssuer(), this, "calculate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
						flag.getLength(), flag.getRows(), flag.getColumns());

				if(seek + flag.getLength() > end) {
					throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
				}

				// 读数据到内存
				byte[] b = new byte[(int) flag.getLength()];
				size = in.read(b, 0, b.length);
				if (size != b.length) {
					throw new ToTaskException("%d != %d", size, b.length);
				}
				seek += size;

				// 计算这段数据
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

	/**
	 * 分析和保存经过FROM(DIFFUSE)阶段处理后的数据
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException {
		int seek = off;
		int end = off + len;

		while(seek < end) {
			// 解析数据头
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;

			Logger.debug(getIssuer(), this, "evaluate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
					flag.getLength(), flag.getRows(), flag.getColumns());

			if(seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

			calculate(flag, b, seek, (int) flag.getLength());
			seek += flag.getLength();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		Logger.debug(getIssuer(), this, "assemble", "into...");

		// 从配置池中取出数据库表
		Space space = select.getSpace();
		Table table = findTable(space);

		// 数据重组，返回新的集合
		GroupSorter sorter = new GroupSorter(select, table);
		List<Row> rows = sorter.align(records);

		// 删除旧数据
		records.clear();
		records.addAll(rows);

		// 如果有子集迭代，分割并且保存，返回FluxArea字节流长度；否则判断实体数据长度
		if (hasSessionSector()) {
			return doStepSector(records); 
		} else {
			return doRowsCapacity(records); 
		}	
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		Logger.debug(getIssuer(), this, "effuse", "into...");

		// 如果有子集迭代，返回FluxArea数据流；否则输出数据
		if (hasSessionSector()) {
			return doFluxArea();
		} else {
			Space space = select.getSpace();
			return doRows(space, records);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		Logger.debug(getIssuer(), this, "flushTo", "into...");

		long length = 0;
		if (hasSessionSector()) {
			byte[] b = doFluxArea();
			length = writeTo(file, false, b, 0, b.length);
		} else {
			Space space = select.getSpace();
			length = writeRows(space, records, file);
		}

		return length;
	}

}