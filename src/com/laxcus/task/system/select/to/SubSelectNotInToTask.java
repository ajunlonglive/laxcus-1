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
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.system.select.util.*;

/**
 * 嵌套检索的TO阶段"NOT IN"操作。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2014
 * @since laxcus 1.0
 */
public class SubSelectNotInToTask extends SQLToEvaluateTask {

	/** 接收数据对应的参数 (在inject方法中使用) **/
	private Sheet presheet;
	private Select preselect;
	private ArrayList<Row> prearray = new ArrayList<Row>(1024);

	/** 发送数据对应的参数(在complete方法中使用) **/
	private Sheet sheet;
	private Select select;
	private ArrayList<Row> array = new ArrayList<Row>(1024);

	/**
	 * 构造嵌套检索"NOT IN"操作的TO阶段实例
	 */
	public SubSelectNotInToTask() {
		super();
	}

	/**
	 * 取出参数
	 * @param space
	 * @throws ToTaskException
	 */
	private void createSheet() throws TaskException {
		ToSession session = super.getSession();
		
		// 取出前一组
		this.preselect = super.findSelect(SubSelectTaskKit.PRESELECT, session);
		Table table = super.findTable(preselect.getSpace());
		this.presheet = preselect.getListSheet().getColumnSheet(table);

		// 取出当前组
		this.select = super.findSelect(SubSelectTaskKit.SELECT, session);
		table = super.findTable(select.getSpace());
		this.sheet = select.getListSheet().getColumnSheet(table);
		
		Logger.debug(getIssuer(), "SubSelectNotInToTask.createSheet, presheet is %d, sheet is %d",
			presheet.size(), sheet.size());
	}

	private boolean calculate(FluxField field, File file) throws TaskException, IOException {
		long seek = 0L;
		long end = file.length();
		FileInputStream in = new FileInputStream(file);
		
		while (seek < end) {
			MassFlag flag = new MassFlag();
			int size = flag.resolve(in);
			seek += size;
			
			Space space = flag.getSpace();

			// 当列属性顺序表不存在时，建立它
			if (this.preselect == null && this.select == null) {
				this.createSheet();
				if (space.compareTo(select.getSpace()) != 0
						&& space.compareTo(preselect.getSpace()) != 0) {
					throw new ToTaskException("not match %s", space);
				}
			}

			Sheet delegate = null;
			ArrayList<Row> rows = null;
			if (space.compareTo(preselect.getSpace()) == 0 && flag.getColumns() == presheet.size()) {
				delegate = this.presheet;
				rows = this.prearray;
			} else if (space.compareTo(select.getSpace()) == 0 && flag.getColumns() == sheet.size()) {
				delegate = this.sheet;
				rows = this.array;
			} else {
				throw new ToTaskException("cannot match!");
			}
			
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

			Logger.debug(getIssuer(), "SubSelectNotInToTask.inject, resolve %s size is %d", space, delegate.size());

			// 根据列属性顺序表，解析每一行记录
			RowCracker cracker = new RowCracker(delegate);
			size = cracker.split(b, 0, b.length);
			// 弹出记录，直至完成
			while (cracker.hasRows()) {
				rows.add( cracker.poll());
			}
		}
		
		in.close();
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, java.io.File)
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
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len)
			throws TaskException {
		int seek = off;
		int end = off + len;

		// 解析结果的标记头信息
		MassFlag flag = new MassFlag();
		int size = flag.resolve(b, seek, end - seek);
		seek += size;

		Space space = flag.getSpace();

		// 当列属性顺序表不存在时，建立它
		if (this.preselect == null && this.select == null) {
			this.createSheet();
			
			if (space.compareTo(select.getSpace()) != 0
					&& space.compareTo(preselect.getSpace()) != 0) {
				throw new ToTaskException("not match %s", space);
			}
		}

		Sheet delegate = null;
		ArrayList<Row> rows = null;
		if (space.compareTo(preselect.getSpace()) == 0 && flag.getColumns() == presheet.size()) {
			delegate = this.presheet;
			rows = this.prearray;
		} else if (space.compareTo(select.getSpace()) == 0 && flag.getColumns() == sheet.size()) {
			delegate = this.sheet;
			rows = this.array;
		} else {
			throw new ToTaskException("cannot match!");
		}
		
//		// debug code, start
//		if(flag.getColumns() == 1) {
//			delegate = this.presheet;
//			rows = this.prearray;
//		} else if(flag.getColumns() == 5) {
//			delegate = this.sheet;
//			rows = this.array;
//		}
//		// debug code, end

		Logger.debug(getIssuer(), "SubSelectNotInToTask.evaluate, resolve %s size is %d", space, delegate.size());

		// 根据列属性顺序表，解析每一行记录
		RowCracker cracker = new RowCracker(delegate);
		size = cracker.split(b, seek, end - seek);
		seek += size;
		// 弹出记录，直至完成
		while (true) {
			Row row = cracker.poll();
			if (row == null) break;
			rows.add(row);
		}

		Logger.debug(getIssuer(), "SubSelectNotInToTask.evaluate, completed! rows size:%d", rows.size());

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
		short preid = presheet.get(0).getColumnId();
		short id = select.getWhere().getColumnId();

		ArrayList<java.lang.Integer> a = new ArrayList<java.lang.Integer>();
		int size = array.size();
		for (int index = 0; index < size; index++) {
			Row r1 = this.array.get(index);
			Column s1 = r1.find(id);

			boolean match = false;
			for (Row r2 : this.prearray) {
				Column s2 = r2.find(preid);
				// 如果是相同的情况,不保存
				match = (s1.compare(s2) == 0);
				if (match) break;
			}

			// 如果全部找过是没有匹配的情况时,保存它
			if (!match) {
				a.add(index);
			}
		}
		
		Logger.debug(getIssuer(), this, "effuse", "pre array:%d, array:%d, filte size:%d",
			prearray.size(), array.size(), a.size()	);

		ArrayList<Row> rows = new ArrayList<Row>();
		for (int index : a) {
			rows.add(array.get(index));
		}

		// 输入分区或者实际结果
		if (super.getSession().hasIndexSector()) {
			return super.flushNextSector(rows);
		} else {
			return this.doRowsStream(0, select.getSpace(), rows);
		}
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