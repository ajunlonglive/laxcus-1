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
import com.laxcus.command.access.select.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.classable.*;

/**
 * 查询插入的TO阶段任务
 * 
 * @author scott.liang
 * @version 1.0 12/5/2020
 * @since laxcus 1.0
 */
public class InjectSelectToEvaluateTask extends ToEvaluateTask {

	/** 统计写入的行数 **/
	private long writeRows;

	//	/** 写入表 **/
	//	private Space insertSpace;

	/** 写入表 **/
	private Table injectTable;

	/** INJECT列排列顺序 **/
	private Sheet injectSheet;

	/** 查询表 **/
	private Table selectTable;

	/** SELECT查询结果 **/
	private Sheet selectSheet;

	/** 记录集合 **/
	private ArrayList<Row> records = new ArrayList<Row>(1024);

	/**
	 * 构造默认的查询插入的TO阶段任务
	 */
	public InjectSelectToEvaluateTask() {
		super();
		writeRows = 0;
	}

	//	/**
	//	 * 建立与记录中每一列对应的"列属性顺序表"
	//	 * @param space
	//	 * @throws ToTaskException
	//	 */
	//	private void createListSheet(Space space1) throws TaskException {
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
	//		ToSession session = getSession();
	//		InjectSelect cmd = (InjectSelect) session.getCommand();
	//		Select select = cmd.getSelect();
	//		
	//		
	//
	//		//		Space sub = select.getSpace();
	//		//		if (space.compareTo(sub) != 0) {
	//		//			throw new ToTaskException("%s != %s", space, sub);
	//		//		}
	//		//		// 从会话中获得SELECT命令
	//		//		select = fetchSelect();
	//
	//		// 检查表名
	//		if (Laxkit.compareTo(select.getSpace(), space) != 0) {
	//			throw new ToTaskException("not match! %s - %s", space, select.getSpace());
	//		}
	//		
	//		// 如果空值，设置它!
	//		if (insertSpace == null) {
	//			insertSpace = cmd.getSpace();
	//		}
	//		
	//		// 查找对象的表
	//		if (injectTable == null) {
	//			injectTable = findTable(cmd.getSpace());
	//			if (injectTable == null) {
	//				throw new TaskNotFoundException("cannot be find %s", cmd.getSpace());
	//			}
	//			ListSheet sheet = cmd.getListSheet();
	//			injectSheet = sheet.getColumnSheet(injectTable);
	//		}
	//		
	//		if(selectTable == null) {
	//			
	//		}
	//
	//		// 查找数据表配置
	//		Table table = findTable(space);
	//
	//		ListSheet sheet = select.getListSheet();
	//		selectSheet = sheet.getColumnSheet(table);
	//
	//		//		// 有函数，并且前面有GROUP BY/DISTINCT时，GROUP BY/DISTINCT已经重组过数据，必须使用重组后的表单
	//		//		// 数据重组后，列数目和排列会有变化
	//		//		ListSheet sheet = select.getListSheet();
	//		//		if (sheet.hasFunctions() && (select.hasGroup() || select.isDistinct())) {
	//		//			indexSheet = sheet.getDisplaySheet(table); // 显示表包括了普通列和函数列
	//		//		} else {
	//		//			// 否则只提取列属性，不包括函数列
	//		//			indexSheet = sheet.getColumnSheet(table);
	//		//		}
	//	}

	/**
	 * 建立与记录中每一列对应的"列属性顺序表"
	 * @param space
	 * @throws ToTaskException
	 */
	private void createListSheet() throws TaskException {
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

		ToSession session = getSession();
		InjectSelect cmd = (InjectSelect) session.getCommand();




		//		Space sub = select.getSpace();
		//		if (space.compareTo(sub) != 0) {
		//			throw new ToTaskException("%s != %s", space, sub);
		//		}
		//		// 从会话中获得SELECT命令
		//		select = fetchSelect();

		//		// 检查表名
		//		if (Laxkit.compareTo(select.getSpace(), space) != 0) {
		//			throw new ToTaskException("not match! %s - %s", space, select.getSpace());
		//		}

		//		// 如果空值，设置它!
		//		if (insertSpace == null) {
		//			insertSpace = cmd.getSpace();
		//		}

		// 查找对象的表
		if (injectTable == null) {
			injectTable = findTable(cmd.getSpace());
			if (injectTable == null) {
				throw new TaskNotFoundException("cannot be find %s", cmd.getSpace());
			}
			ListSheet sheet = cmd.getListSheet();
			injectSheet = sheet.getColumnSheet(injectTable);
		}

		// 查询表
		if (selectTable == null) {
			Select select = cmd.getSelect();
			selectTable = findTable(select.getSpace());
			if (selectTable == null) {
				throw new TaskNotFoundException("cannot be find %s", select.getSpace());
			}
			ListSheet sheet = select.getListSheet();
			selectSheet = sheet.getColumnSheet(selectTable);
		}

		// 比较行
		if (injectSheet.size() != selectSheet.size()) {
			throw new ToTaskException("sheet size error! %d != %d",
					injectSheet.size(), selectSheet.size());
		}

		//		// 查找数据表配置
		//		Table table = findTable(space);
		//
		//		ListSheet sheet = select.getListSheet();
		//		selectSheet = sheet.getColumnSheet(table);

		//		// 有函数，并且前面有GROUP BY/DISTINCT时，GROUP BY/DISTINCT已经重组过数据，必须使用重组后的表单
		//		// 数据重组后，列数目和排列会有变化
		//		ListSheet sheet = select.getListSheet();
		//		if (sheet.hasFunctions() && (select.hasGroup() || select.isDistinct())) {
		//			indexSheet = sheet.getDisplaySheet(table); // 显示表包括了普通列和函数列
		//		} else {
		//			// 否则只提取列属性，不包括函数列
		//			indexSheet = sheet.getColumnSheet(table);
		//		}
	}

	private void calculate(MassFlag flag, byte[] b, int off, int len) throws TaskException {
		//		// 当列属性顺序表不存在时，建立它
		//		if (selectSheet == null) {
		//			createListSheet(flag.getSpace());
		//		}

		// 生成索引表
		createListSheet();

		RowCracker cracker = new RowCracker(selectSheet);
		// 只解析指定长度的数据
		cracker.split(b, off, len);

		// 输入数据并且保存
		List<Row> rows = cracker.flush();

		for (Row row : rows) {
			Row temp = new Row();

			for (int index = 0; index < selectSheet.size(); index++) {
				ColumnAttribute src = selectSheet.get(index);

				ColumnAttribute dest = injectSheet.get(index);
				if (src.getType() != dest.getType()) {
					throw new ToTaskException("not match! %s != %s", src.toString(), dest.toString());
				}

				Column column = row.find(src.getColumnId());
				if (column == null) {
					throw new TaskNotFoundException("not found column! %s", src.toString());
				}
				Column clone =	column.duplicate();
				clone.setId(dest.getColumnId());
				// 保存一列数据
				temp.add(clone);
			}

			// 填充剩余列
			fill(temp, injectTable);

			// 保存一行数据
			records.add(temp);
		}

		//		records.addAll(rows);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException {
		int seek = off;
		int end = off + len;

		while (seek < end) {
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;

			Logger.debug(getIssuer(), this, "evaluate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
					flag.getLength(), flag.getRows(), flag.getColumns());

			// 判断数据长度，防止溢出
			if(seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

			// 计算它
			calculate(flag, b, seek, (int) flag.getLength());
			seek += flag.getLength();
		}

		Logger.debug(getIssuer(), this, "evaluate", "completed! row size:%d", records.size());

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, java.io.File)
	 */
	@Override
	public boolean evaluate(FluxField field, File file) throws TaskException {
		long seek = 0L;
		long end = file.length();

		try {
			FileInputStream in = new FileInputStream(file);
			while (seek < end) {
				MassFlag flag = new MassFlag();
				int size = flag.resolve(in);
				seek += size;

				// 判断数据长度，防止溢出
				if(seek + flag.getLength() > end) {
					throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
				}

				// 读数据到磁盘
				byte[] b = new byte[(int) flag.getLength()];
				size = in.read(b, 0, b.length);
				if (size != b.length) {
					throw new ToTaskException("%d != %d", size, b.length);
				}
				seek += size;

				Logger.debug(getIssuer(), this, "calculate", "mod:%d, length:%d, rows:%d, columns:%d", flag.getMod(),
						flag.getLength(), flag.getRows(), flag.getColumns());

				calculate(flag, b, 0, b.length);
			}
		} catch (IOException e) {
			throw new TaskException(e);
		}

		return true;
	}

	/**
	 * 一行记录中，如果有某列不存在，定义一个默认值
	 * @param row
	 * @param table
	 */
	private void fill(Row row, Table table) throws TaskException {
		for (ColumnAttribute attribute : table.list()) {
			short columnId = attribute.getColumnId();
			Column column = row.find(columnId);
			if (column != null) {
				continue;
			}
			// 生成一个默认值
			column = attribute.getDefault();
			if (column == null) {
				throw new TaskNotFoundException("cannot be create default! %s", attribute.getName());
			}
			row.add(column);
		}
	}

	//	/**
	//	 * 填充记录
	//	 * @param rows
	//	 * @param table
	//	 * @throws TaskException
	//	 */
	//	private void fill(List<Row> rows, Table table) throws TaskException {
	//		for(Row row : rows) {
	//			fill(row, table);
	//		}
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		Logger.debug(getIssuer(), this, "assemble", "into...");
		// 远程写入数据

		//		// 填充数据
		//		Table table = findTable(insertSpace);
		//		if (table == null) {
		//			throw new TaskNotFoundException("cannot be find table! %s", insertSpace);
		//		}
		//		fill(records, table);

		// 查找主节点
		Node callSite = getSource();
		List<Node> hubs = getToTrustor().findPrimeSites(getInvokerId(), callSite, injectTable.getSpace()); // insertSpace);
		Node hub = ((hubs == null || hubs.isEmpty()) ? null : hubs.get(0));
		if (hub == null) {
			throw new TaskNotFoundException("cannot be find master site!");
		}

		// 生成命令
		Insert insert = new Insert(injectTable.getSpace()); // insertSpace);
		insert.addAll(records);

		// 写入远端DATA主节点地址
		AssumeInsert assume = getToTrustor().insert(getInvokerId(), hub, insert);

		// 如果成功，忽略！
		if (!assume.isConfirmSuccess()) {
			throw new ToTaskException("insert failed!");
		}

		// 返回写入行数
		writeRows = assume.getRows();

		Logger.debug(getIssuer(), this, "assemble", "write rows: %d", writeRows);

		return writeRows;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		ClassWriter writer = new ClassWriter();
		writer.writeObject(injectTable.getSpace()); // insertSpace);
		writer.writeLong(writeRows);
		return writer.effuse();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return  writeTo(file, false, b, 0, b.length);
	}

}