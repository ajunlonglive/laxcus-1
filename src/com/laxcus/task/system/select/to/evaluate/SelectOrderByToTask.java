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

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.command.access.sort.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.*;

/**
 * SQL "ORDER BY"在TO阶段（CONVERGE）的排序方案。<br>
 * 计算每一行记录的位置，返回排序结果<br>
 * 
 * @author scott.liang
 * @version 1.2 12/08/2014
 * @since laxcus 1.0
 */
public class SelectOrderByToTask extends SelectToEvaluateTask {

	/** 记录集合 **/
	private ArrayList<Row> records = new ArrayList<Row>(1024);

	/**
	 * 构造"ORDER BY"的TO阶段实例
	 */
	public SelectOrderByToTask() {
		super();
	}

//	/**
//	 * 建立与记录中每一列对应的"列属性顺序表"
//	 * @param space
//	 * @throws ToTaskException
//	 */
//	private void createListSheet(Space space) throws TaskException {
//		//		// 从会话中取得SELECT命令（标准SELECT查询情况：GROUP BY/ORDER BY/DISTINCT语句块）
//		//		ToSession session = getSession();
//		//		select = (Select) session.getCommand();
//		//		// 如果SELECT不在会话命令中，就是以自定义参数身份存在（嵌套查询等情况）
//		//		if (select == null) {
//		//			if (!session.hasValue(SQLTaskKit.SELECT_OBJECT)) {
//		//				throw new ToTaskException("cannot be find \"SELECT_OBJECT\"");
//		//			}
//		//			select = (Select) session.findCommand(SQLTaskKit.SELECT_OBJECT);
//		//		}
//
//		// 从会话中获得SELECT命令
//		select = fetchSelect();
//
//		// 检查表名
//		if (Laxkit.compareTo(select.getSpace(), space) != 0) {
//			throw new ToTaskException("cannot be match %s - %s", space, select.getSpace());
//		}
//
//		// 查找数据表配置
//		Table table = findTable(space);
//
//		// 有函数，并且前面有GROUP BY/DISTINCT时，GROUP BY/DISTINCT已经重组过数据，必须使用重组后的表单
//		// 数据重组后，列数目和排列会有变化
//		ListSheet sheet = select.getListSheet();
//		if (sheet.hasFunctions() && (select.hasGroup() || select.isDistinct())) {
//			this.indexSheet = sheet.getDisplaySheet(table); // 显示表包括了普通列和函数列
//		} else {
//			// 否则只提取列属性，不包括函数列
//			this.indexSheet = sheet.getColumnSheet(table);
//		}
//	}

	/**
	 * 建立与记录中每一列对应的"列属性顺序表"
	 * @param space
	 * @throws ToTaskException
	 */
	private void createListSheet(Space space) throws TaskException {
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

		// 有函数，在拿到数据前，已经重组过数据。// 并且前面有GROUP BY/DISTINCT时，GROUP BY/DISTINCT已经重组过数据，必须使用重组后的表单
		// 数据重组后，列数目和排列会有变化
		ListSheet sheet = select.getListSheet();
		if (sheet.hasFunctions()) { // && (select.hasGroup() || select.isDistinct())) {
			this.indexSheet = sheet.getDisplaySheet(table); // 显示表包括了普通列和函数列
		} else {
			// 否则只提取列属性，不包括函数列
			this.indexSheet = sheet.getColumnSheet(table);
		}
	}

	private void calculate(MassFlag flag, byte[] b, int off, int len) throws TaskException {
		// 当列属性顺序表不存在时，建立它
		if (indexSheet == null) {
			createListSheet(flag.getSpace());
		}

		RowCracker cracker = new RowCracker(indexSheet);
		// 只解析指定长度的数据
		cracker.split(b, off, len);

		// 输入数据并且保存
		List<Row> rows = cracker.flush();
		records.addAll(rows);
	}

	/**
	 * 
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
				
				// 取长度，如果是0，忽略它
				long length = flag.getLength();
				if (length == 0) {
					continue;
				}

				// 判断数据长度，防止溢出
				if(seek + length > end) {
					throw new ToTaskException("%d + %d > %d", seek, length, end);
				}

				// 读数据到磁盘
				byte[] b = new byte[(int) length];
				size = in.read(b, 0, b.length);
				if (size != b.length) {
					throw new ToTaskException("%d != %d", size, b.length);
				}
				seek += size;

				Logger.debug(getIssuer(), this, "calculate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
						length, flag.getRows(), flag.getColumns());

				calculate(flag, b, 0, b.length);
			}
			// 关闭
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
			
			long length = flag.getLength();
			// 0 长度，忽略
			if (length == 0) {
				continue;
			}

			Logger.debug(getIssuer(), this, "evaluate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
					length, flag.getRows(), flag.getColumns());

			// 判断数据长度，防止溢出
			if(seek + length > end) {
				throw new ToTaskException("%d + %d > %d", seek, length, end);
			}

			// 计算它
			calculate(flag, b, seek, (int) length);
			seek += length;
		}

		Logger.debug(getIssuer(), this, "evaluate", "completed! row size:%d", records.size());

		return true;
	}

	/**
	 * 生成一个"ORDER BY"排序器
	 * @param select
	 * @return
	 * @throws ToTaskException
	 */
	private OrderBySorter createSorter(Select select) throws TaskException {
		// 排序器
		OrderBySorter sorter = new OrderBySorter(select.getOrder());

		Space space = select.getSpace();
		Table table = findTable(space); 

		// 设置自定义的比较器
		for (short columnId : select.getOrder().listColumnIds()) {
			ColumnAttribute attribute = table.find(columnId);
			if (attribute == null) {
				throw new ToTaskException("cannot find attribute by '%s - %d'", space, columnId);
			}

			if (attribute.isChar()) {
				CharAttribute consts = (CharAttribute) attribute;
				CharComparator comparator = new CharComparator();
				comparator.setColumnId(columnId);
				comparator.setSentient(consts.isSentient());
				comparator.setPacking(consts.getPacking());
				sorter.add(comparator);
			} else if (attribute.isWChar()) {
				WCharAttribute consts = (WCharAttribute) attribute;
				WCharComparator comparator = new WCharComparator();
				comparator.setColumnId(columnId);
				comparator.setSentient(consts.isSentient());
				comparator.setPacking(consts.getPacking());
				sorter.add(comparator);
			} else if (attribute.isHChar()) {
				HCharAttribute consts = (HCharAttribute) attribute;
				HCharComparator comparator = new HCharComparator();
				comparator.setColumnId(columnId);
				comparator.setSentient(consts.isSentient());
				comparator.setPacking(consts.getPacking());
				sorter.add(comparator);
			}
		}

		return sorter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		Logger.debug(getIssuer(), this, "assemble", "into...");

		// 产生一个排序器
		OrderBySorter sorter = createSorter(this.select);
		// 调用ORDER BY调节器，对数组中的列数据，按照规则进行排序和调整位置
		Collections.sort(this.records, sorter);

		// 如果有SQL函数，但是没有GROUP BY/DISTINCT，说明之前没有做过数据重组，就在ORDER BY中完成
		ListSheet sheet = select.getListSheet();
		if(sheet.hasFunctions() && (!select.hasGroup() && !select.isDistinct())) {
			this.realign(sheet, records);
		}

		if (getSession().hasIndexSector()) {
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
		if (getSession().hasIndexSector()) {
			return super.doFluxArea();
		} else {
			Space space = select.getSpace();
			return super.doRows(space, records);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		long length = 0;

		if (getSession().hasIndexSector()) {
			byte[] b = doFluxArea();
			length = writeTo(file, false, b, 0, b.length);
		} else {
			Space space = select.getSpace();
			length = writeRows(space, records, file);
		}

		return length;
	}

}