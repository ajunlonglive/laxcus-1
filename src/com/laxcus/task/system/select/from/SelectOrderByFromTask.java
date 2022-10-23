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
 * <code>SQL&nbsp;"ORDERY BY"</code>操作。<br><br>
 * 
 * 数据从磁盘提取后，在这里处理"ORDER BY"过程中的数据分片。数据根据列规则分割后，分成多组，分别顺序到磁盘，返回分片数据在磁盘的位置信息(FluxArea)。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/10/2010
 * @since laxcus 1.0
 */
public class SelectOrderByFromTask extends SQLFromTask {

	/**
	 * 构造"ORDER BY"的FROM(diffuse)阶段实例
	 */
	public SelectOrderByFromTask() {
		super();
	}

	/**
	 * 解析ROW信息，生成数据分片并且返回，实际数据保存在本地磁盘上，等待WORK节点来读取
	 * @see com.laxcus.task.system.select.from.SQLFromTask#divide(byte[], int, int)
	 */
	@Override
	protected void divide(byte[] b, int off, int len) throws TaskException {
		Logger.debug(getIssuer(), this, "divide", "data off:%d, size:%d", off, len);

		FromSession session = super.getSession();
		// ORDER BY分区
		ColumnSector sector = session.getIndexSector();
		// 属性参数
		CastSelect cmd = (CastSelect) session.getCommand(); // session.getSelect();
		Select select = cmd.getSelect();
		Space space = select.getSpace();
		
//		// 从行中取出列，根据列值选择对应的分片下标，将记录存入对应的分片
//		short columnId = select.getOrder().getColumnId();
//		Dock dock = new Dock(space, columnId);

		// 读数据
		int seek = off;
		int end = off + len;

		while(seek < end) {
			// 解析数据头
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;
			// 判断数据溢出
			if(seek + flag.getLength() > end){
				throw new FromTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}
			// 判断一致，否则是错误
			if (space.compareTo(flag.getSpace()) != 0) {
				throw new FromTaskException("%s != %s", space, flag.getSpace());
			}

			// 查找数据表配置
			Table table = super.findTable(space); 
			// 检查存储模型
			if (table.getStorage() != flag.getModel()) {
				throw new FromTaskException("sm not match:%d - %d", table.getStorage(), flag.getModel());
			}

			// 返回列成员属性序列表
			Sheet sheet = select.getListSheet().getColumnSheet(table);

			Logger.debug(getIssuer(), this, "divide", "sheet size is:%d, show sheet size is %d", sheet.size(),
					select.getListSheet().size());

			// 分割数据和保存
//			size = splitTo(sheet, dock, sector, b, seek, (int) flag.getLength());
			
			size = splitTo(sheet, sector, b, seek, (int) flag.getLength());
			// 统计解析长度
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