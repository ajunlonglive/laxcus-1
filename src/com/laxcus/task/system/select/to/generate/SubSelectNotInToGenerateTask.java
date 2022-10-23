/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.generate;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;

/**
 * @author scott.liang
 *
 */
public class SubSelectNotInToGenerateTask extends SQLToGenerateTask {

	/**
	 * 
	 */
	public SubSelectNotInToGenerateTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToGenerateTask#divide(byte[], int, int)
	 */
	@Override
	public void divide(byte[] b, int off, int len) throws TaskException {
		ToSession session = super.getSession();
		if (session == null) {
			throw new ToTaskException("cannot be find session");
		}
		// 取出在INIT阶段定义的，后续TO阶段需要的列分组区域
		ColumnSector sector = session.getIndexSector();
		if (sector == null) {
			throw new ToTaskException("cannot be find sector");
		}
		// 取出SELECT句柄
		CastSelect cmd = (CastSelect) session.getCommand(); // session.getSelect();
		if (cmd == null) {
			throw new ToTaskException("cannot be find select");
		}
		Select select = cmd.getSelect();

		// 根据这个列进行数据分片
		int seek = off;
		int end = off + len;
		// 解析数据头标记
		MassFlag flag = new MassFlag();
		int size = flag.resolve(b, off, len);
		seek += size;

		// 找到表配置
		Space space = flag.getSpace();
		if (space.compareTo(select.getSpace()) != 0) {
			throw new ToTaskException("cannot match: %s,%s", space, select.getSpace());
		}
		Table table = findTable(space); // super.getToAssistor().findToTable(space);
		if (table == null) {
			throw new ToTaskException("cannot be find table by %s", space);
		}
		// 存储类型必须一致
		if (table.getStorage() != flag.getModel()) {
			throw new ToTaskException("illegal sm: %d", flag.getModel());
		}

		// 取出显示序列表
		Sheet sheet = select.getListSheet().getColumnSheet(table);
		// 必须只有1列
		if (sheet.size() != 1) {
			throw new ToTaskException("sheet size:%d, must be 1!", sheet.size());
		}

		// 解析数据流
		RowCracker cracker = new RowCracker(sheet);
		size = cracker.split(b, seek, end - seek);
		seek += size;

		// 取出解析后的记录
		List<Row> result = cracker.flush();

		// 分片，然后写入数据到磁盘
		splitWriteTo(sector, result);
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.to.ToGenerateTask#divide(java.io.File)
	//	 */
	//	@Override
	//	public void divide(File file) throws TaskException {
	//		ToSession session = super.getSession();
	//		if (session == null) {
	//			throw new ToTaskException("cannot be find session");
	//		}
	//		// 取出在INIT阶段定义的，后续TO阶段需要的列分组区域
	//		IndexSector sector = session.getIndexSector();
	//		if (sector == null) {
	//			throw new ToTaskException("cannot be find sector");
	//		}
	//		// 取出SELECT句柄
	//		CastSelect cmd = (CastSelect) session.getCommand(); // session.getSelect();
	//		if (cmd == null) {
	//			throw new ToTaskException("cannot be find select");
	//		}
	//		Select select = cmd.getSelect();
	//		Space space = select.getSpace();
	//		// 查找表
	//		Table table = findTable(space);
	//		// 取出显示序列表
	//		Sheet sheet = select.getListSheet().getColumnSheet(table);
	//		// 必须只有1列
	//		if (sheet.size() != 1) {
	//			throw new ToTaskException("sheet size:%d, must be 1!", sheet.size());
	//		}
	//
	//		long seek = 0L;
	//		long end = file.length();
	//		try {
	//			FileInputStream in = new FileInputStream(file);
	//			while (seek < end) {
	//				// 解析结果的标记头信息
	//				MassFlag flag = new MassFlag();
	//				int size = flag.resolve(in);
	//				seek += size;
	//				// 比较数据表名
	//				if (space.compareTo(flag.getSpace()) != 0) {
	//					throw new ToTaskException("cannot match: %s,%s", space, flag.getSpace());
	//				}
	//				// 存储类型必须一致
	//				if (table.getStorage() != flag.getStorage()) {
	//					throw new ToTaskException("illegal sm: %d", flag.getStorage());
	//				}
	//				
	//				// 读数据到内存
	//				byte[] b = new byte[(int) flag.getLength()];
	//				size = in.read(b, 0, b.length);
	//				if (size != b.length) {
	//					throw new ToTaskException("%d != %d", size, b.length);
	//				}
	//				seek += size;
	//
	//				// 解析数据流
	//				RowParser parser = new RowParser(sheet);
	//				size = parser.split(b, 0, b.length);
	//				// 取出解析后的记录
	//				List<Row> result = parser.flush();
	//				// 分片，然后写入数据到磁盘
	//				this.splitWriteTo(sector, result);
	////				this.write(dock, sector, result);
	//			}
	//			in.close();
	//		} catch (IOException e) {
	//			throw new TaskException(e);
	//		}
	//
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	//	 */
	//	@Override
	//	public long assemble() throws TaskException {
	//		return 0;
	//	}

}