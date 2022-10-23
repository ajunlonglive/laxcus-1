/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.from;

import java.io.*;
import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;

/**
 * 嵌套检索(SUB SELECT)的FROM阶段的"NOT IN"操作。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2014
 * @since laxcus 1.0
 */
public class SubSelectNotInFromTask extends SQLFromTask {

	/**
	 * 构造NOT IN实例
	 */
	public SubSelectNotInFromTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.system.select.from.SQLFromTask#divide(byte[], int, int)
	 */
	@Override
	protected void divide(byte[] b, int off, int len) throws TaskException {
		Logger.debug(getIssuer(), this, "divide", "begin ...");

		FromSession session = super.getSession();
		// 取出在INIT阶段定义的，后续TO阶段需要的列分组区域
		ColumnSector sector = session.getIndexSector();
		// 取出SELECT句柄
		CastSelect cmd = (CastSelect) session.getCommand();
		Select select = cmd.getSelect();
		// 查找表空间
		Space space = select.getSpace();
		Table table = findTable(space);
		// 取出显示序列表
		Sheet sheet = select.getListSheet().getColumnSheet(table);
		// 必须只有1列
		if (sheet.size() != 1) {
			throw new FromTaskException("cannot support ");
		}

//		// 列空间
//		short columnId = sheet.get(0).getColumnId();
//		Dock dock = new Dock(space, columnId);

		// 根据这个列进行数据分片
		int seek = off;
		int end = off + len;
		// 逐段解析
		while(seek < end) {
			// 解析数据头标记
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;

			if (space.compareTo(select.getSpace()) != 0) {
				throw new FromTaskException("%s != %s", space, select.getSpace());
			}
			if (seek + flag.getLength() > end) {
				throw new FromTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

			// 解析数据流
			RowCracker cracker = new RowCracker(sheet);
			size = cracker.split(b, seek, (int) flag.getLength());
			if (size != flag.getLength()) {
				throw new FromTaskException("%d != %d", size, flag.getLength());
			}
			// 记录长度
			seek += size;
			// 取出解析后的记录
			List<Row> rows = cracker.flush();
			// 分片，然后写入数据
			this.splitWriteTo(sector, rows);
//			this.write(dock, sector, rows);
		}

		Logger.debug(getIssuer(), this, "divide", "end!");
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.system.select.from.SQLFromTask#assemble()
//	 */
//	@Override
//	protected long assemble() throws TaskException {
//		// 统计映像数据长度
//		FluxArea area = createFluxArea();
//		byte[] b = area.build();
//		return b.length;
//	}

	//	public void divide(byte[] b, int off, int len) throws TaskException {
	//		Logger.debug("SubSelectNotInFromTask.divide, into...");
	//
	//		FromSession session = super.getSession();
	//		// 取出在INIT阶段定义的，后续TO阶段需要的列分组区域
	//		IndexSector sector = session.getIndexSector();
	//		// 取出SELECT句柄
	//		Select select = session.getSelect().getSelect();
	//
	//		// 根据这个列进行数据分片
	//		int seek = off;
	//		int end = off + len;
	//		// 解析数据头标记
	//		AnswerFlag flag = new AnswerFlag();
	//		int size = flag.resolve(b, off, len);
	//		seek += size;
	//
	//		// 找到表配置
	//		Space space = flag.getSpace();
	//		if (space.compareTo(select.getSpace()) != 0) {
	//			throw new FromTaskException("cannot match: %s,%s", space, select.getSpace());
	//		}
	//		Table table = super.findTable(space); // super.getPool().findTable(space);
	//		if(table == null) {
	//			throw new FromTaskException("cannot find table by %s", space);
	//		}
	//		// 存储类型必须一致
	//		if (table.getStorage() != flag.getStorage()) {
	//			throw new FromTaskException("illegal sm: %d", flag.getStorage());
	//		}
	//
	//		// 取出显示序列表
	//		Sheet sheet = select.getListSheet().getColumnSheet(table);
	//		// 必须只有1列
	//		if (sheet.size() != 1) {
	//			throw new FromTaskException("cannot support ");
	//		}
	//
	//		// 解析数据流
	//		RowSplitter parser = new RowSplitter(flag, sheet);
	//		size = parser.split(b, seek, end - seek);
	//		seek += size;
	//
	//		// 列空间
	//		short columnId = sheet.get(0).getColumnId();
	//		Dock dock = new Dock(space, columnId);
	//		// 取出解析后的记录
	//		List<Row> result = parser.flush();
	//
	//		// 分片，然后写入数据
	//		this.write(dock, sector, result);
	//	}



	/**
	 * 收集数据分片结果，返回FluxArea对象
	 * @see com.laxcus.task.conduct.from.FromTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		FluxArea area = super.createFluxArea();
		return area.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return writeTo(file, false, b, 0, b.length);
	}
}
