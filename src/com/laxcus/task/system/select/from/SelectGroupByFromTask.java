/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.from;

import java.io.*;

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
 * <code>SQL&nbsp;"GROUP BY"</code>操作。<br>
 * 
 * 数据从磁盘上提取后，在这里进行"GROUP BY"操作的分片。分片的依据是GROUP BY语句中指定的第一个列。<br><br>
 * 
 * 注：按照<code>DIFFUSE/CONVERGE</code>算法，<b>DIFFUSE</b>只有一次执行，<b>CONVERGE</b>允许多次迭代。
 * 
 * @author scott.liang
 * @version 1.0 11/2/2010
 * @since laxcus 1.0
 */
public class SelectGroupByFromTask extends SQLFromTask {

	/**
	 * 构造 "GROUP BY"的FROM(DIFFUSE)阶段实例
	 */
	public SelectGroupByFromTask() {
		super();
	}

	/**
	 * 根据GROUP BY的第一个参数进行数据分片，返回分片范围集合
	 * @see com.laxcus.task.system.select.from.SQLFromTask#divide(byte[], int, int)
	 */
	@Override
	protected void divide(byte[] b, int off, int len) throws TaskException {
		Logger.debug(getIssuer(), this, "divide", "data off:%d, size:%d", off, len);

		FromSession session = super.getSession();
		// GROUP BY的分区
		ColumnSector sector = session.getIndexSector();
		// 数据表名
		CastSelect cmd = (CastSelect) session.getCommand(); 
		Select select = cmd.getSelect();
		Space space = select.getSpace();
		
//		// 取出GROUP BY的第一个列标识号
//		short columnId = select.getGroup().getColumnIds()[0];
//		Dock dock = new Dock(space, columnId);

		int seek = off;
		int end = off + len;

		while(seek < end) {
			// 解析数据头标记
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek , end - seek);
			seek += size;
			// 判断数据溢出
			if(seek + flag.getLength() > end){
				throw new FromTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}
			// 判断一致，否则是错误
			if (space.compareTo(flag.getSpace()) != 0) {
				throw new FromTaskException("%s != %s", space, flag.getSpace());
			}

			// 找到表配置
			Table table = findTable(space); 
			
//			// 存储类型必须一致
//			if (table.getStorage() != flag.getModel()) {
//				throw new FromTaskException("illegal SM:%d - %d", table.getStorage(), flag.getModel());
//			}

			// 返回显示成员，包括列和被函数操作的列。SELECT在FROM中只能有一个。
			Sheet sheet = select.getListSheet().getColumnSheet(table); 

			Logger.debug(getIssuer(), this, "divide", "sheet size is:%d, show sheet size is %d",
					sheet.size(), select.getListSheet().size());

			// 解析一块数据
//			size = splitTo(sheet, dock, sector, b, seek, (int)flag.getLength());
			size = splitTo(sheet, sector, b, seek, (int)flag.getLength());
			seek += size;
		}
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
	 * @see com.laxcus.task.conduct.from.FromTask#complete()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		Logger.debug(getIssuer(), this, "effuse", "iterate index is:%d", getIterateIndex());

		FluxArea area = createFluxArea();
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