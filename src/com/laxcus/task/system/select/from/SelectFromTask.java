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
import com.laxcus.command.access.select.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.util.*;

/**
 * 通用的<code>SELECT</code>的<code>FROM</code>阶段检索分割操作。<br>
 * 
 * 数据从磁盘上提取后，在这里进行分片操作，分片的依据是被指定的列标识号。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 11/2/2010
 * @since laxcus 1.0
 */
public class SelectFromTask extends SQLFromTask {

	/**
	 * 构造标准SQL SELECT检索的FROM(DIFFUSE)阶段实例
	 */
	public SelectFromTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.system.select.from.SQLFromTask#divide(byte[], int, int)
	 */
	@Override
	protected void divide(byte[] b, int off, int len) throws TaskException {
		Logger.debug(getIssuer(), this, "divide", "数据开始下标: %d, 长度: %d", off, len);

		FromSession session = getSession();
		CastSelect cmd = (CastSelect) session.getCommand();
		Select select = cmd.getSelect();
		Space space = select.getSpace();
		// 分区参考标准和分区列空间
		ColumnSector sector = session.getIndexSector();
		if (sector == null) {
			throw new FromTaskException("cannot be find IndexSector");
		}

		ListSheet listSheet = select.getListSheet();
		// 查找表配置
		Table table = findTable(space);

		// // 按照显示列，生成列属性顺序表
		// Sheet sheet = listSheet.getColumnSheet(table);

		// 按照显示顺序，生成列属性顺序表，注意！是包括函数！
		Sheet sheet = listSheet.getDisplaySheet(table);
		// 如果有分组，只取列属性顺序表
		if (select.hasGroup()) {
			sheet = listSheet.getColumnSheet(table);
		}

		Logger.debug(getIssuer(), this, "divide", "%s, sheet size is:%d, show sheet size is %d, group by %s",
				space, sheet.size(), listSheet.size(), (select.hasGroup() ? "Yes" : "No"));

		int seek = off;
		int end = off + len;
		while (seek < end) {
			// 解析数据头标记
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;
			
			long length = flag.getLength();
			// 如果是0长度，忽略！
			if (length == 0) {
				continue;
			}

			// 检查数据溢出
			if (seek + length > end) {
				throw new FromTaskException("%d + %d > %d", seek, length, end);
			}
			// 判断表名一致
			if (Laxkit.compareTo(space, flag.getSpace()) != 0) {
				throw new FromTaskException("%s != %s", space, flag.getSpace());
			}

			//			// 查找表配置
			//			Table table = findTable(space);
			//			
			////			// 存储类型必须一致
			////			if (table.getStorage() != flag.getModel()) {
			////				throw new FromTaskException("illegal SM:%s - %s",
			////					StorageModel.translate(table.getStorage()), StorageModel.translate(flag.getModel()));
			////			}
			//
			//			// 返回显示成员，包括列和被函数操作的列。SELECT在FROM中只能有一个。
			//			Sheet sheet = select.getListSheet().getColumnSheet(table);
			//
			//			Logger.debug(getIssuer(), this, "divide", "sheet size is:%d, show sheet size is %d",
			//					sheet.size(), select.getListSheet().size());

			// 解析数据流
			size = splitTo(sheet, sector, b, seek, (int) length);
			// 统计长度
			seek += size;
		}
	}

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
		return	writeTo(file, false, b, 0, b.length);
	}

}