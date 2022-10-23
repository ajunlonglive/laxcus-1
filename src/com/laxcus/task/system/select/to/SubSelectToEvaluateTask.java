/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.system.select.util.*;

/**
 * 嵌套检索TO阶段基础类。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2014
 * @since laxcus 1.0
 */
public abstract class SubSelectToEvaluateTask extends SQLToEvaluateTask {

	/** 接收数据对应的参数 (在insert方法中使用) **/
	protected Sheet resolveSheet;
	protected Select resolveSelect;

	/** 发送数据对应的参数(在complete方法中使用) **/
	protected Sheet dispatchSheet;
	protected Select dispatchSelect;

	/**
	 * 构造嵌套检索的TO阶段基础实例
	 */
	public SubSelectToEvaluateTask() {
		super();
	}

	/**
	 * 建立行中的列形成对应关系的"列属性顺序表"，下标从0开始
	 * @param space
	 * @throws ToTaskException
	 */
	protected void createSheet(Space space) throws TaskException {
		ToSession session = super.getSession();
		// 取出当前接收的SELECT实例
		TaskParameter value = session.findParameter(SQLTaskKit.SELECT_OBJECT);
		if (value == null || !value.isCommand()) {
			throw new ToTaskException("cannot find SELECT object!");
		}
		resolveSelect = (Select) (((TaskCommand) value).getValue());

		// 检查表名
		if (space.compareTo(resolveSelect.getSpace()) != 0) {
			throw new ToTaskException("cannot match %s - %s", space, resolveSelect.getSpace());
		}

		// 查找表
		Table table = findTable(space); 
		// 建立与行中的"列"形成对应关系的"列属性顺序表"，下标从0开始
		resolveSheet = resolveSelect.getListSheet().getColumnSheet(table);

		// 在SELECT之后的检索查询
		value = session.findParameter(SubSelectTaskKit.NEXT_SELECT_OBJECT);
		if (value == null || !value.isCommand()) {
			throw new ToTaskException("cannot find SUB-SELECT object!");
		}
		dispatchSelect = (Select) (((TaskCommand) value).getValue());

		space = dispatchSelect.getSpace();
		table = findTable(space);
		dispatchSheet = dispatchSelect.getListSheet().getColumnSheet(table);
	}

	/**
	 * 根据列参数值，生成一个列索引
	 * @param space
	 * @param column
	 * @return
	 * @throws IOException
	 */
	protected ColumnIndex createColumnIndex(Space space, Column column) throws IOException  {
		short columnId = column.getId();

		Table table = findTable(space); 
		ColumnAttribute attribute = table.find(columnId);
		if (attribute == null) {
			throw new ToTaskException("cannot be find column by %s", columnId);
		}
		if (attribute.getType() != column.getType()) {
			throw new ToTaskException("cannot be match %d - %d", attribute.getType(), column.getType());
		}

		ColumnIndex index = null;
		if (attribute.isRaw()) {
			Raw that = (Raw) column;
			byte[] b = that.getValue(((RawAttribute) attribute).getPacking());
			index = IndexGenerator.createRawIndex(table.isDSM(), (RawAttribute) attribute, b);
		} else if (attribute.isWord()) {
			Word that = (Word) column;
			String text = that.toString(((WordAttribute) attribute).getPacking());

			Logger.debug(getIssuer(), "SubSelectToEvaluateTask.createColumnIndex, string is %s", text);

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

		Logger.debug(getIssuer(), "SubSelectToEvaluateTask.createColumnIndex, column id:%d", columnId);

		return index;
	}

}