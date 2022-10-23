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
import com.laxcus.access.index.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.system.select.to.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 嵌套查询在TO阶段的“IN”计算接口
 * 
 * @author scott.liang
 * @version 1.0 5/2/2014
 * @since laxcus 1.0
 */
public class SubSelectInToEvaluateTask extends SQLToEvaluateTask {

	/** 接收数据对应的参数 (在insert方法中使用) **/
	private Sheet presheet;
	private Select preselect;

	/** 发送数据对应的参数(在complete方法中使用) **/
	private Sheet sheet;
	private Select select;

	/** 列参数存储器，同值列只能存在一个 **/
	private TreeSet<Column> fromKeys = new TreeSet<Column>(new SubSelectKeyComparator());

	/**
	 * 构造默认的嵌套查询在TO阶段的“IN”计算接口
	 */
	public SubSelectInToEvaluateTask() {
		super();
//		Logger.debug(getIssuer(), this, "constructor", "helo...");
	}

	/**
	 * 建立索引表
	 * @param space
	 * @throws TaskException
	 */
	private void createSheet(Space space) throws TaskException {
		ToSession session = super.getSession();

		// 检查表名
		preselect = super.findSelect(SubSelectTaskKit.PRESELECT, session);
		if (space.compareTo(preselect.getSpace()) != 0) {
			throw new ToTaskException("cannot be match %s - %s", space, preselect.getSpace());
		}

		Table table = findTable(preselect.getSpace());
		// 建立与行中的"列"形成对应关系的"列属性顺序表"，下标从0开始
		presheet = preselect.getListSheet().getColumnSheet(table);

		select = super.findSelect(SubSelectTaskKit.SELECT, session);
		table = super.findTable(select.getSpace());

		sheet = select.getListSheet().getColumnSheet(table);
	}

	/**
	 * 根据列参数值，生成一个列索引
	 * @param space
	 * @param column
	 * @return
	 * @throws IOException
	 */
	private ColumnIndex createColumnIndex(Space space, Column column) throws IOException  {
		short columnId = column.getId();

		Table table = findTable(space); 
		if (table == null) {
			throw new ToTaskException("cannot find table by %s", space);
		}
		ColumnAttribute attribute = table.find(columnId);
		if (attribute == null) {
			throw new ToTaskException("cannot find column by %s", columnId);
		}
		if (attribute.getType() != column.getType()) {
			throw new ToTaskException("cannot match %d - %d", attribute.getType(), column.getType());
		}

		ColumnIndex index = null;
		if (attribute.isRaw()) {
			Raw that = (Raw) column;
			byte[] b = that.getValue(((RawAttribute) attribute).getPacking());
			index = IndexGenerator.createRawIndex(table.isDSM(), (RawAttribute) attribute, b);
		} else if (attribute.isDocument()) {
			Document that = (Document) column;
			byte[] b = that.getValue(((DocumentAttribute) attribute).getPacking());
			index = IndexGenerator.createDocumentIndex(table.isDSM(), (DocumentAttribute) attribute, b);
		} else if (attribute.isImage()) {
			Image that = (Image) column;
			byte[] b = that.getValue(((ImageAttribute) attribute).getPacking());
			index = IndexGenerator.createImageIndex(table.isDSM(), (ImageAttribute) attribute, b);
		} else if (attribute.isAudio()) {
			Audio that = (Audio) column;
			byte[] b = that.getValue(((AudioAttribute) attribute).getPacking());
			index = IndexGenerator.createAudioIndex(table.isDSM(), (AudioAttribute) attribute, b);
		} else if (attribute.isVideo()) {
			Video that = (Video) column;
			byte[] b = that.getValue(((VideoAttribute) attribute).getPacking());
			index = IndexGenerator.createVideoIndex(table.isDSM(), (VideoAttribute) attribute, b);
		} else if (attribute.isWord()) {
			Word that = (Word) column;
			String text = that.toString(((WordAttribute) attribute).getPacking());

			Logger.debug(getIssuer(), "SubSelectInToEvaluateTask.createColumnIndex, string is %s", text);

			index = IndexGenerator.createWordIndex(table.isDSM(), (WordAttribute) attribute, text);
		} else if (attribute.isShort()) {
			short value = ((com.laxcus.access.column.Short) column).getValue();
			index = IndexGenerator.createShortIndex(value, attribute);
		} else if (attribute.isInteger()) {
			int value = ((com.laxcus.access.column.Integer) column).getValue();
			index = IndexGenerator.createIntegerIndex(value, attribute);
		} else if (attribute.isLong()) {
			long value = ((com.laxcus.access.column.Long) column).getValue();
			index = IndexGenerator.createLongIndex(value, attribute);
		} else if (attribute.isFloat()) {
			float value = ((com.laxcus.access.column.Float) column).getValue();
			index = IndexGenerator.createFloatIndex(value, attribute);
		} else if (attribute.isDouble()) {
			double value = ((com.laxcus.access.column.Double) column).getValue();
			index = IndexGenerator.createDoubleIndex(value, attribute);
		} else if(attribute.isDate()) {
			int value = ((com.laxcus.access.column.Date) column).getValue();
			index = IndexGenerator.createDateIndex(value, attribute);
		} else if (attribute.isTime()) {
			int value = ((com.laxcus.access.column.Time) column).getValue();
			index = IndexGenerator.createTimeIndex(value, attribute);
		} else if (attribute.isTimestamp()) {
			long value = ((com.laxcus.access.column.Timestamp) column).getValue();
			index = IndexGenerator.createTimestampIndex(value, attribute);
		} else {
			throw new ToTaskException("illegal attribute");
		}

		// 设置列标识号
		index.getColumn().setId(columnId);

		Logger.debug(getIssuer(), "SubSelectInToEvaluateTask.createColumnIndex, column id:%d", columnId);

		return index;
	}

	/**
	 * 
	 * @param sheet
	 * @param columnId
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws ToTaskException
	 */
	private int evaluate(Sheet sheet, short columnId, 
			byte[] b, int off, int len) throws ToTaskException {
		// 根据列属性顺序表，解析每一行记录
		RowCracker cracker = new RowCracker( sheet);
		int size = cracker.split(b, off, len);
		if (len != size) {
			throw new ToTaskException("%d != %d", size, len);
		}
		// 弹出记录，直至完成
		while (cracker.hasRows()) {
			Row row = cracker.poll();
			Column column = row.find(columnId);
			// 保存记录
			if (column != null && !column.isNull()) {
				fromKeys.add(column);
			}
		}
		return size;
	}

	/* (non-Javadoc)
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
			// 判断长度有效
			if (seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

			// 当列属性顺序表不存在时，建立它
			if (this.preselect == null && this.select == null) {
				createSheet(flag.getSpace());
			}
			// 输入数据并且保存
			short columnId = presheet.get(0).getColumnId();

			Logger.debug(getIssuer(), this, "evaluate", "resolve sheet size is %d", presheet.size());

			size = evaluate(presheet, columnId, b, seek, (int) flag.getLength());
			seek += size;
		}

		Logger.debug(getIssuer(), this, "evaluate", "completed! column size:%d", fromKeys.size());

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, java.io.File)
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
				if(seek + flag.getLength() > end) {
					throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
				}

				// 当列属性顺序表不存在时，建立它
				if (this.preselect == null && this.select == null) {
					createSheet(flag.getSpace());
				}
				// 输入数据并且保存
				short columnId = presheet.get(0).getColumnId();

				Logger.debug(getIssuer(), this, "evaluate", "resolve sheet size is %d", presheet.size());

				// 读磁盘数据
				byte[] b = new byte[(int)flag.getLength()];
				int len = in.read(b);
				if(len != flag.getLength()) {
					throw new ToTaskException("%d != %d", size, b.length);
				}

				// 计算单元
				size = evaluate(presheet, columnId, b, 0, b.length);
				seek += size;
			}
			in.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}

		Logger.debug(getIssuer(), this, "evaluate", "column size:%d", fromKeys.size());
		Logger.debug(getIssuer(), this, "evaluate", "this is %s,%s" , field, file);

		return true;
	}

	private List<Row> split(Sheet sheet, byte[] b, int off, int len) throws ToTaskException{
		int seek = off;
		int end = off + len;

		ArrayList<Row> array = new ArrayList<Row>();

		while (seek < end) {
			// 解析结果的标记头信息
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;

			Logger.debug(getIssuer(), this, "split", "seek:%d, data len:%d, end:%d", seek, flag.getLength(), end);

			// 判断数据长度，防止溢出
			if(seek + flag.getLength() > end) {
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}

			RowCracker cracker = new RowCracker(sheet);
			// 只解析指定长度的数据
			size = cracker.split(b, seek, (int) flag.getLength());
			// 判断长度一致
			if(size != flag.getLength()) {
				throw new ToTaskException("%d != %d", size, flag.getLength());
			}
			// 移动下标
			seek += size;

			// 输入数据并且保存
			List<Row> rows = cracker.flush();
			array.addAll(rows);
		}

		return array;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		ToSession session =	super.getSession();
//		Dock dock = session.getIndexDock();
		ColumnSector sector = session.getIndexSector();

		// 调用器编号和CALL站点地址
		long invokerId = getInvokerId();
		Node source = getSource();

		ToTrustor trustor = getToTrustor() ;

		// 1. 去CALL找主站点地址
		Space space = select.getSpace();
		List<Node> nodes = trustor.findPrimeSites(invokerId, source, space);

		// 2. 去不同的DATA站点查找数据块编号
		TreeSet<java.lang.Long> stubs = new TreeSet<java.lang.Long>();
		for (Node node : nodes) {
			for (Column column : fromKeys) {				
				Select sub = new Select(space);
				sub.setListSheet(select.getListSheet());
				// 修改检索条件
				try {
					ColumnIndex index = createColumnIndex(space, column);
					Logger.debug(getIssuer(), this, "assemble", "index class is %s", index.getClass().getName());
					Where condi = new Where(CompareOperator.EQUAL, index);
					sub.setWhere(condi);
				} catch (IOException e) {
					throw new ToTaskException(e);
				}

				Logger.debug(getIssuer(), this, "assemble", "clone index is %s", sub.getWhere().getIndex().getClass());

				// 去DATA查询
				sub.setMemory(isMemory());
				List<java.lang.Long> e = trustor.filteStubs(invokerId, node, sub);

				Logger.debug(getIssuer(), this, "assemble", "stub elements:%d, from %s", (e == null ? -1 : e.size()), node);
				stubs.addAll(e);
			}
		}

		Logger.debug(getIssuer(), this, "assemble", "query stub size %d, to %s", stubs.size(), source);

		//3. 去CALL站点查关联的DATA站点，不分主从
		List<StubEntry> list = trustor.findStubSites(invokerId, source, space, new ArrayList<java.lang.Long>(stubs));

		Logger.debug(getIssuer(), this, "assemble" , "from %s, stub entry is %d", source, (list == null ? -1 : list.size()));

		boolean success = (list != null && list.size() > 0);
		if (!success) {
			throw new ToTaskException("illegal StubEntry set!");
		}

		// 显示成员表
		ListSheet display = select.getListSheet();

		Logger.debug(getIssuer(), this, "assemble", "display element size %d", display.size());

		for (StubEntry entry : list) {
			Node hub = entry.getNode(); // DATA站点地址
			for (long stub : entry.list()) {
				for(Column column : fromKeys) {
					Select sub = new Select(space);
					// 设置显示表
					sub.setListSheet(display); // select.getListSheet());
					// 默认不处理函数列
					sub.setAutoAdjust(false);

					try {
						ColumnIndex index = createColumnIndex(space, column);
						Where condi = new Where(CompareOperator.EQUAL, index);
						sub.setWhere(condi);
					} catch (IOException e) {
						throw new ToTaskException(e);
					}

					// 通过网络查询DATA主机数据
					byte[] data = trustor.select(invokerId, hub, sub, stub);

					Logger.debug(getIssuer(), this, "assemble", "select data is %d", (data == null ? -1 : data.length));

					// 允许空值
					if (Laxkit.isEmpty(data)) {
						continue;
					}

					// 解析查询数据（此时忽略函数），顺序表（顺序表建立时忽略了函数）和数据比较，解析后的记录存入缓存
					List<Row> rows = split(sheet, data, 0, data.length);
					// 有函数，且没有下一级时
					if (display.hasFunctions() && sector == null) {
						realign(display, rows);
					}

					// // 解析，和当前参数进行比较，有效记录存入缓存
					// List<Row> rows = this.split(sheet, data, 0, data.length);

					if (sector == null) {
						byte[] b = doRowsStream(0, space, rows);
						FluxWriter writer = fetchWriter();
						writer.append(0, rows.size(), b, 0, b.length);
					} else {
						// 写入数据，返回元信息
						super.splitWriteTo(sector, rows);
					}
				}
			}
		}

		// 没能子级，返回实体数据；有子级，返回FluxArea字节长度
		if (sector == null) {
			return super.length();
		} else {
			byte[] b = doFluxArea();
			return b.length;
		}
	}

	/*
	 * (non-Javadoc)
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

	/*
	 * (non-Javadoc)
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