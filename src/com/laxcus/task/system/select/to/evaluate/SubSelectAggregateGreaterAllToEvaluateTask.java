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

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.system.select.to.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.classable.*;

/**
 * 计算最大列值任务
 * 
 * @author scott.liang
 * @version 1.0 12/20/2020
 * @since laxcus 1.0
 */
public class SubSelectAggregateGreaterAllToEvaluateTask extends SQLToEvaluateTask {

	/** 最大的列 **/
	private Column max;

	private Select select;
	private Sheet sheet;

	/**
	 * 构造计算最大列值任务
	 */
	public SubSelectAggregateGreaterAllToEvaluateTask() {
		super();
	}

	/**
	 * 建立索引表
	 * @param space
	 * @throws TaskException
	 */
	private void createSheet(Space space) throws TaskException {
		ToSession session = super.getSession();

		//		// 检查表名
		//		preselect = super.findSelect(SubSelectTaskKit.PRESELECT, session);
		//		if (space.compareTo(preselect.getSpace()) != 0) {
		//			throw new ToTaskException("cannot be match %s - %s", space, preselect.getSpace());
		//		}
		//
		//		Table table = findTable(preselect.getSpace());
		//		// 建立与行中的"列"形成对应关系的"列属性顺序表"，下标从0开始
		//		presheet = preselect.getListSheet().getColumnSheet(table);

		select = findSelect(SubSelectTaskKit.SELECT, session);
		Table table = findTable(select.getSpace());
		sheet = select.getListSheet().getColumnSheet(table);
	}

	private int evaluate(Space space, byte[] b, int off, int len)
			throws TaskException {
		// 生成对象
		if (sheet == null) {
			createSheet(space);
		}

		ColumnAttribute attribute =	sheet.get(0);
		short columnId = attribute.getColumnId();

		// 根据列属性顺序表，解析每一行记录
		RowCracker cracker = new RowCracker(sheet);
		int size = cracker.split(b, off, len);
		if (len != size) {
			throw new ToTaskException("%d != %d", size, len);
		}

		// 取出全部列，逐个判断，取出最大列值
		while (cracker.hasRows()) {
			Row row = cracker.poll();
			// 找到一列
			Column sub = row.find(columnId);
			if (max == null) {
				max = sub;
			} else {
				if (sub.getClass() != max.getClass()) {
					Logger.error(getIssuer(), this, "evaluate", "%s != %s", sub.getClass().getName(), max.getClass().getName());
					continue;
				} else if (sub.compare(max) > 0) {
					max = sub;
				}
			}
		}

		return size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len)
	throws TaskException {

		int seek = off;
		int end = off + len;
		while (seek < end) {
			// 解析结果的标记头信息
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;
			// 判断长度有效
			if (seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}
			size = evaluate(flag.getSpace(), b, seek, (int) flag.getLength());
			seek += size;
		}

		//		Logger.debug(getIssuer(), this, "evaluate", "completed! column size:%d", fromKeys.size());

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, java.io.File)
	 */
	@Override
	public boolean evaluate(FluxField field, File file) throws TaskException {
		long seek = 0;
		long end = file.length();
		try {
			FileInputStream in = new FileInputStream(file);
			while(seek < end) {
				// 解析结果的标记头信息
				MassFlag flag = new MassFlag();
				int size = flag.resolve(in);
				seek += size;
				// 判断长度有效
				if (seek + flag.getLength() > end) {
					throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
				}
				// 读磁盘数据
				byte[] b = new byte[(int) flag.getLength()];
				int len = in.read(b);
				if (len != flag.getLength()) {
					throw new ToTaskException("%d != %d", size, b.length);
				}
				// 计算单元
				size = evaluate(flag.getSpace(), b, 0, b.length);
				seek += size;
			}
			in.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}

		//		Logger.debug(getIssuer(), this, "evaluate", "this is %s,%s" , field, file);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		Logger.debug(getIssuer(), this, "assemble", "%s, 最大列是：%s", 
				select.getSpace(), max.toString());

		// 结果
		byte[] b = max.build();
		if (hasSessionSector()) {
			FluxWriter writer = fetchWriter();
			writer.append(0, 1, b, 0, b.length);
			byte[] n = doFluxArea();
			return n.length;
		} else {
			FluxWriter writer = fetchWriter();
			writer.append(0, 1, b, 0, b.length);
			return super.length();
		}

		//		ToSession session =	super.getSession();
		//		ColumnSector sector = session.getIndexSector();		

		//		if (sector == null) {
		//			byte[] b = doRowsStream(0, select.getSpace(), temp);
		//			FluxWriter writer = fetchWriter();
		//			byte[] bu = max.build();
		//			writer.append(0, 1, b, 0, b.length);
		//		} else {
		//			// 写入数据，返回元信息
		//			super.splitWriteTo(sector, temp);
		//		}
		//
		//		// 没能子级，返回实体数据；有子级，返回FluxArea字节长度
		//		if (sector == null) {
		//			return super.length();
		//		} else {
		//			byte[] b = doFluxArea();
		//			return b.length;
		//		}


		//		// 没能子级，返回实体数据；有子级，返回FluxArea字节长度
		//		byte[] b = max.build();
		//		if (sector == null) {
		//			FluxWriter writer = fetchWriter();
		//			writer.append(0, 1, b, 0, b.length);
		//			// return b.length;
		//			return super.length();
		//		} else {
		//			// splitWriteTo(sector, rows)
		//			FluxWriter writer = fetchWriter();
		//			writer.append(0, 1, b, 0, b.length);
		//			byte[] n = doFluxArea();
		//			return n.length;
		//		}

		//		// 写入元数据
		//		byte[] b = max.build();
		//		FluxWriter writer = fetchWriter();
		//		writer.append(0, 1, b, 0, b.length);
		//		return b.length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		TaskClassable param = new TaskClassable("column", max); 

		// 有子级，返回FluxArea字节数组
		if (hasSessionSector()) {
			FluxArea area = createFluxArea();
			FluxField field = area.list().get(0);
			field.addValue(param);
			return area.build();
		} else {
			// 没有子级，读出实体数据返回
			FluxArea area = super.createFluxArea();
			long size = area.length();

			ClassWriter buff = new ClassWriter((int) size);
			FluxReader reader = fetchReader();
			for (FluxField field : area.list()) {
				byte[] b = reader.read(field.getMod(), 0, (int) field.length());
				buff.write(b);
			}
			return buff.effuse();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		long count = 0L;
		// 有子级分区，输出FluxArea数据流；否则，写实体数据到磁盘

		if (hasSessionSector()) {
			byte[] b = super.doFluxArea();
			count = writeTo(file, false, b, 0, b.length);
		} else {
			FluxArea area = super.createFluxArea();
			List<FluxField> fields = area.list();
			FluxReader reader = fetchReader();
			for (int index = 0; index < fields.size(); index++) {
				FluxField field = fields.get(index);
				byte[] b = reader.read(field.getMod(), 0, (int) field.length());
				// 第1次是覆盖，以后是添加
				boolean append = (index > 0);
				// 数据写入磁盘
				long len = writeTo(file, append, b, 0, b.length);
				// 统计写入长度
				count += len;
			}
		}
		// 返回写入长度
		return count;
	}

}
