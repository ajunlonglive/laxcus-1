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
 * INNER JOIN的FROM阶段处理。<br>
 * 解析传入的参数，进行分片，然后分别保存。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2014
 * @since laxcus 1.0
 */
public class JoinSelectInnerFromTask extends SQLFromTask {

	/**
	 * 
	 */
	public JoinSelectInnerFromTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.system.select.from.SQLFromTask#divide(byte[], int, int)
	 */
	@Override
	protected void divide(byte[] b, int off, int len) throws TaskException {
		FromSession session = super.getSession();

		int seek = off;
		int end = off + len;

		// 解析数据头标记
		MassFlag flag = new MassFlag();
		int size = flag.resolve(b, off, len);
		seek += size;

		// 找到表配置
		Space space = flag.getSpace();
		Table table = super.findTable(space); // super.getPool().findTable(space);
		if(table == null) {
			throw new FromTaskException("cannot find table by %s", space);
		}
		// 存储类型必须一致
		if (table.getStorage() != flag.getModel()) {
			throw new FromTaskException("illegal sm:%d - %d", table.getStorage(), flag.getModel());
		}

		CastSelect cmd = (CastSelect) session.getCommand();
		Select select = cmd.getSelect(); // session.getSelect().getSelect();
		Sheet sheet = select.getListSheet().getColumnSheet(table); 

		Logger.debug("InnerJoinSelectFromTask.divide, %s", select.getPrimitive());

		// 解析数据流
		RowCracker parser = new RowCracker(sheet);
		size = parser.split(b, seek, end - seek);
		seek += size;

		// 输出已经解析过的记录集合
		List<Row> result = parser.flush();

		// 分区
		ColumnSector sector = session.getIndexSector();
		// 分片，写入磁盘
		super.splitWriteTo(sector, result);		

//		// 列空间
//		short columnId = select.getWhere().getColumnId();
//		Dock dock = new Dock(space, columnId);
//
//		// 分片，写入磁盘
//		super.write(dock, sector, result);		
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

	/*
	 * (non-Javadoc)
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
		return	writeTo(file, false, b, 0, b.length);
	}
}