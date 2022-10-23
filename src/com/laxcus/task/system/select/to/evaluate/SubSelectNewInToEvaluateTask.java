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
import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.system.select.to.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.classable.*;

/**
 * 子查询IN语句
 * 
 * 产生计算数据，进行合并
 * 
 * @author scott.liang
 * @version 1.0 12/14/2020
 * @since laxcus 1.0
 */
public class SubSelectNewInToEvaluateTask extends SQLToEvaluateTask {

	/** 接收数据对应的参数 (在insert方法中使用) **/
	private Sheet presheet;
	private Select preselect;
	private ArrayList<Row> prerows = new ArrayList<Row>();

	/** 发送数据对应的参数(在complete方法中使用) **/
	private Sheet sheet;
	private Table selectTable;
	private Select select;
	private ArrayList<Row> rows = new ArrayList<Row>();

	/**
	 * 
	 */
	public SubSelectNewInToEvaluateTask() {
		super();
	}
	
	/**
	 * 建立索引表
	 * @param space
	 * @throws TaskException
	 */
	private void createSheet() throws TaskException {
		ToSession session = super.getSession();

		// 被检查表
		preselect = super.findSelect(SubSelectTaskKit.PRESELECT, session);
		Table table = findTable(preselect.getSpace());
		// 建立与行中的"列"形成对应关系的"列属性顺序表"，下标从0开始
		presheet = preselect.getListSheet().getColumnSheet(table);

		// 检查表
		select = super.findSelect(SubSelectTaskKit.SELECT, session);
		selectTable = super.findTable(select.getSpace());
		sheet = select.getListSheet().getColumnSheet(selectTable);
	}

	private int evaluate(Space space, /*Sheet sheet, short columnId, */ 
			byte[] b, int off, int len) throws TaskException {
		// 属性顺序表不存在时，建立它
		if (preselect == null && select == null) {
			createSheet();
		}

		int size = 0;
		if (select.getSpace().compareTo(space) == 0) {
			// 根据列属性顺序表，解析每一行记录
			RowCracker cracker = new RowCracker(sheet);
			size = cracker.split(b, off, len);
			if (len != size) {
				throw new ToTaskException("%d != %d", size, len);
			}
			rows.addAll(cracker.flush());
		} else if (preselect.getSpace().compareTo(space) == 0) {
			// 根据列属性顺序表，解析每一行记录
			RowCracker cracker = new RowCracker(presheet);
			size = cracker.split(b, off, len);
			if (len != size) {
				throw new ToTaskException("%d != %d", size, len);
			}
			prerows.addAll(cracker.flush());
		} else {
			throw new ToTaskException("invalie table! %s", space);
		}
		
		Logger.debug(getIssuer(), this, "evaluate", "resolve %s size is %d", space, size);
		
//		// 弹出记录，直至完成
//		while (parser.hasRows()) {
//			Row row = parser.poll();
//			Column column = row.find(columnId);
//			// 保存记录
//			if (column != null && !column.isNull()) {
////				fromKeys.add(column);
//			}
//		}
		return size;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, byte[], int, int)
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
			// 判断长度有效
			if (seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

//			// 输入数据并且保存
//			short columnId = presheet.get(0).getColumnId();
//			Logger.debug(getIssuer(), this, "evaluate", "resolve sheet size is %d", presheet.size());
//			size = evaluate(flag.getSpace(), presheet, columnId, b, seek, (int) flag.getLength());
			
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

//				// 输入数据并且保存
//				short columnId = presheet.get(0).getColumnId();
//				Logger.debug(getIssuer(), this, "evaluate", "resolve sheet size is %d", presheet.size());

				// 读磁盘数据
				byte[] b = new byte[(int) flag.getLength()];
				int len = in.read(b);
				if (len != flag.getLength()) {
					throw new ToTaskException("%d != %d", size, b.length);
				}

				// 计算单元
//				size = evaluate(flag.getSpace(), presheet, columnId, b, 0, b.length);
				size = evaluate(flag.getSpace(), b, 0, b.length);
				seek += size;
			}
			in.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}

//		Logger.debug(getIssuer(), this, "evaluate", "column size:%d", fromKeys.size());
//		Logger.debug(getIssuer(), this, "evaluate", "this is %s,%s" , field, file);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		ToSession session =	super.getSession();
		ColumnSector sector = session.getIndexSector();
		
		ArrayList<Row> temp = new ArrayList<Row>();
		
		// 检索列
		short columnId = select.getWhere().getColumnId();
		ColumnAttribute attr1 = selectTable.find(columnId);
		// 前一段查询的列表属性
		ColumnAttribute attr2 = presheet.get(0);
		
		// 逐个比较
		for (Row row1 : rows) {
			Column c1 = row1.find(attr1.getColumnId());
			for (Row row2 : prerows) {
				Column c2 = row2.find(attr2.getColumnId());
				// 类定义不一致时，下一个
				if (c1.getClass() != c2.getClass()) {
					continue;
				}
				// 两行一致时，保存，退出！
				if (c1.compare(c2) == 0) {
					temp.add(row1);
					break;
				}
			}
		}

		Logger.debug(getIssuer(), this, "assemble", "%s -> %s, 驱动表 %d 行，被驱动表 %d 行，结果 %d 行", 
				select.getSpace(), preselect.getSpace(), rows.size(), prerows.size(),  temp.size());

		if (sector == null) {
			byte[] b = doRowsStream(0, select.getSpace(), temp);
			FluxWriter writer = fetchWriter();
			writer.append(0, temp.size(), b, 0, b.length);
		} else {
			// 写入数据，返回元信息
			super.splitWriteTo(sector, temp);
		}
		
		// 没能子级，返回实体数据；有子级，返回FluxArea字节长度
		if (sector == null) {
			return super.length();
		} else {
			byte[] b = doFluxArea();
			return b.length;
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		// 有子级，返回FluxArea字节数组
		if (hasSessionSector()) {
			return super.doFluxArea();
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
