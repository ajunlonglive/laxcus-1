/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to;

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
import com.laxcus.task.conduct.util.*;
import com.laxcus.task.system.select.util.*;

/**
 * @author scott.liang
 *
 */
public class JoinSelectInnerToTask extends SQLToEvaluateTask {

	private Join join;

	private Sheet leftSheet;

	private Sheet rightSheet;

	private TreeMap<ColumnKey, RowBuffer> leftBuffer = new TreeMap<ColumnKey, RowBuffer>();

	private TreeMap<ColumnKey, RowBuffer> rightBuffer = new TreeMap<ColumnKey, RowBuffer>();

//	private TreeMap<Column, RowBuffer> leftBuffer = new TreeMap<Column, RowBuffer>(new ColumnComparator());
//
//	private TreeMap<Column, RowBuffer> rightBuffer = new TreeMap<Column, RowBuffer>(new ColumnComparator());

	/**
	 * 
	 */
	public JoinSelectInnerToTask() {
		super();
	}

	private void createSheet() throws TaskException {
		ToSession session = super.getSession();
		// 取出当前接收的SELECT实例
		TaskParameter value = session.findParameter(JoinTaskKit.JOIN_OBJECT);
		if (value == null || !value.isCommand()) {
			throw new ToTaskException("cannot find SELECT object!");
		}
		this.join = (Join) (((TaskCommand) value).getValue());

		// 左侧表
		Table table = findTable(join.getLeftSpace()); // super.getToAssistor().findToTable(join.getLeftSpace());
		if (table == null) {
			throw new ToTaskException("cannot find table by '%s'", join.getLeftSpace());
		}
		// 建立与行中的"列"形成对应关系的"列属性顺序表"，下标从0开始
		this.leftSheet = join.getListSheet().getColumnSheet(table);

		// 右侧表
		table = findTable(join.getRightSpace()); // super.getToAssistor().findToTable(join.getRightSpace());
		if (table == null) {
			throw new ToTaskException("cannot find table by '%s'", join.getRightSpace());
		}
		this.rightSheet = join.getListSheet().getColumnSheet(table);
	}
	
	private boolean calculate(FluxField field, File file) throws TaskException, IOException {
		long seek = 0L;
		long end = file.length();
		FileInputStream in = new FileInputStream(file);
		
		while (seek < end) {
			// 解析结果的标记头信息
			MassFlag flag = new MassFlag();
			int size = flag.resolve(in);
			seek += size;
			
			if (seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}
			// 读数据到磁盘
			byte[] b = new byte[(int) flag.getLength()];
			size = in.read(b, 0, b.length);
			if (size != b.length) {
				throw new ToTaskException("%d != %d", size, b.length);
			}
			seek += size;

			// 当列属性顺序表不存在时，建立它
			if (this.leftSheet == null && this.rightSheet == null) {
				this.createSheet();
			}

			short columnId = 0;
			Sheet sheet = null;
			TreeMap<ColumnKey, RowBuffer> rows = null;

			Space space = flag.getSpace();
			if (space.equals(join.getLeftSpace())) {
				sheet = this.leftSheet;
				rows = this.leftBuffer;
				columnId = join.getIndex().getLeft().getColumnId();
			} else if (space.equals(join.getRightSpace())) {
				sheet = this.rightSheet;
				rows = this.rightBuffer;
				columnId = join.getIndex().getRight().getColumnId();
			}
			
			Table table = findTable(space); 
			if(table == null) {
				throw new ToTaskException("cannot find %s", space);
			}
			ColumnAttribute	attribute = table.find(columnId);
			if(attribute == null) {
				throw new ToTaskException("cannot find %s-%d", space, columnId);
			}
			
			// 这代码只用于测试, BEGIN
			if(leftBuffer.size() <= rightBuffer.size()) {
				sheet = this.leftSheet;
				rows = this.leftBuffer;
			} else {
				sheet = this.rightSheet;
				rows = this.rightBuffer;
			}
			// 这代码只用于测试, END

			Logger.debug("SelectInnerJoinToTask.inject, left sheet size:%d, right sheet size:%d",
					this.leftSheet.size(), this.rightSheet.size());

			// 根据列属性顺序表，解析每一行记录
			RowCracker cracker = new RowCracker(sheet);
			size = cracker.split(b, 0, b.length);
			seek += size;

			while (cracker.hasRows()) {
				Row row = cracker.poll();

				Column column = row.find(columnId);
				ColumnKey key = new ColumnKey(column, attribute);
				RowBuffer buff = rows.get(key);

				if (buff == null) {
					buff = new RowBuffer(0, space);
					rows.put(key, buff);
				}
				buff.add(row);
			}

		}
		
		in.close();
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToComputeTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, java.io.File)
	 */
	@Override
	public boolean evaluate(FluxField field, File file) throws TaskException {
		boolean success = false;		
		try {
			success = this.calculate(field, file);
		} catch (TaskException e) {			
			throw e;
		} catch (Throwable e) {
			throw new TaskException(e);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToComputeTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException {

		int seek = off;
		int end = off + len;

		// 解析结果的标记头信息
		MassFlag flag = new MassFlag();
		int size = flag.resolve(b, seek, end - seek);
		seek += size;

		// 当列属性顺序表不存在时，建立它
		if (this.leftSheet == null && this.rightSheet == null) {
			this.createSheet();
		}

		short columnId = 0;
		Sheet sheet = null;
		TreeMap<ColumnKey, RowBuffer> rows = null;

		Space space = flag.getSpace();
		if (space.equals(join.getLeftSpace())) {
			sheet = this.leftSheet;
			rows = this.leftBuffer;
			columnId = join.getIndex().getLeft().getColumnId();
		} else if (space.equals(join.getRightSpace())) {
			sheet = this.rightSheet;
			rows = this.rightBuffer;
			columnId = join.getIndex().getRight().getColumnId();
		}
		
		Table table = findTable(space); //  super.getToAssistor().findToTable(space);
		if(table == null) {
			throw new ToTaskException("cannot find %s", space);
		}
		ColumnAttribute	attribute = table.find(columnId);
		if(attribute == null) {
			throw new ToTaskException("cannot find %s-%d", space, columnId);
		}
		
		// 这代码只用于测试, BEGIN
		if(leftBuffer.size() <= rightBuffer.size()) {
			sheet = this.leftSheet;
			rows = this.leftBuffer;
		} else {
			sheet = this.rightSheet;
			rows = this.rightBuffer;
		}
		// 这代码只用于测试, END

		Logger.debug("SelectInnerJoinToTask.inject, left sheet size:%d, right sheet size:%d",
				this.leftSheet.size(), this.rightSheet.size());

		// 根据列属性顺序表，解析每一行记录
		RowCracker cracker = new RowCracker(sheet);
		size = cracker.split(b, seek, end - seek);
		seek += size;

		while (true) {
			Row row = cracker.poll();
			if (row == null) break;

			Column column = row.find(columnId);
			ColumnKey key = new ColumnKey(column, attribute);
			RowBuffer buff = rows.get(key);

			if (buff == null) {
				buff = new RowBuffer(0, space);
				rows.put(key, buff);
			}
			buff.add(row);
		}

		Logger.debug("SelectInnerJoinToTask.inject, left rows:%d, right rows:%d",
				leftBuffer.size(), rightBuffer.size());

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#complete()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		//		OnIndex index = join.getIndex();
		
		// 将相等的取出来，汇总左右两行记录为一行，输出
		RowBuffer buffer = new RowBuffer(0, new Space("join","join"));
		
		for(ColumnKey key : leftBuffer.keySet()) {
			RowBuffer right = rightBuffer.get(key);
			// 不相同的则忽略
			if(right == null) {
				continue;
			}
			// 两者共有，合为一行输出
			RowBuffer left = leftBuffer.get(key);

			// 如果左、右记录有函数的情况，考虑重构
			for (Row row1 : left.list()) {
				Row row = new Row();
				row.add(row1.list());
				for (Row row2 : right.list()) {
					row.add(row2.list());
				}
				buffer.add(row);
			}

			//			// 选择一个最大的行数
			//			int size = (right.size() < left.size() ? left.size() : right.size());
			//			
			//			for (int index = 0; index < size; index++) {
			//				Row leftrow = left.get(index);
			//				Row rightrow = right.get(index);
			//				if (leftrow == null) {
			//					// 生成空记录
			//				} else if (rightrow == null) {
			//					// 生成空记录
			//				}
			//				// 左、右侧记录重构，如果有函数的情况
			//
			//				Row row = new Row();
			//				for (int i = 0; i < leftrow.size(); i++) {
			//					row.add(leftrow.get(i));
			//				}
			//				for (int i = 0; i < rightrow.size(); i++) {
			//					row.add(rightrow.get(i));
			//				}
			//				// 保存一行
			//				buffer.add(row);
			//			}
		} 

		// 只用于测试
		if(buffer.size() == 0){
			Logger.debug("SelectInnerJoinToTask.complete, temp row is empty");
			for(RowBuffer left : leftBuffer.values()) {
				// 如果左、右记录有函数的情况，考虑重构
				for (Row row1 : left.list()) {
					Row row = new Row();
					row.add(row1.list());
					row.add(row1.list());
					buffer.add(row);
				}
			}		
		}
		// 只用于测试
		
		Logger.debug("SelectInnerJoinToTask.complete, row size %d", buffer.size());
		
		if(buffer.size() == 0) {
			return null;
		}

		byte[] res = buffer.build();
		Logger.debug("SelectInnerJoinToTask.complete, row size is %d, byte size %d",
				buffer.size(), res.length);

		// 返回结果
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return writeTo(file, false, b, 0, b.length);
	}

}