/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.select.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * <code>SQL SELECT</code>命令<br><br>
 * 
 * 目前可以支持的范围包括：<br>
 * <1> SQL "GROUP BY having"语句 <br>
 * <2> SQL "ORDER BY"语句 <br> 
 * <3> SQL "DISTINCT"关键字 <br>
 * <4> 嵌套检索(SUB SELECT) <br>
 * <5> SQL 函数 <br>
 * 
 * @author scott.liang
 * @version 1.2 9/27/2015
 * @since laxcus 1.0
 */
public final class Select extends Query {

	private static final long serialVersionUID = 6533213771272238734L;

	/** 消除重复，默认是false **/
	private boolean distinct;

	/** 显示范围 */
	private IntegerRange range = new IntegerRange();

	/** 显示列集合(表属性列、函数列、列计算单元) */
	private ListSheet sheet;

	/** 根据ListSheet表，当有SQL函数或者列计算单元时，是否自动处理，默认是TRUE **/
	private boolean autoAdjust;

	/** SQL "Order By" 数据排序适配器 **/
	private OrderByAdapter orderAdjuster;

	/** SQL "Group By" 数据分组适配器 **/
	private GroupByAdapter groupAdjuster;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Query#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeBoolean(distinct);
		writer.writeObject(range);

		// 排列表\ORDER BY\GROUP BY
		writer.writeInstance(sheet);
		writer.writeBoolean(autoAdjust);
		writer.writeInstance(orderAdjuster);
		writer.writeInstance(groupAdjuster);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Query#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		distinct = reader.readBoolean();
		range.resolve(reader);

		// 排列表\ORDER BY\GROUP BY
		sheet = reader.readInstance(ListSheet.class);
		autoAdjust = reader.readBoolean();
		orderAdjuster = reader.readInstance(OrderByAdapter.class);
		groupAdjuster = reader.readInstance(GroupByAdapter.class);
	}

	/**
	 * 根据传入的SELECT对象，生成一个它的副本
	 * @param that Select实例
	 */
	private Select(Select that) {
		super(that);
		distinct = that.distinct;
		range.set(that.range);
		autoAdjust = that.autoAdjust;

		if (that.sheet != null) {
			sheet = that.sheet.duplicate();
		}
		if (that.orderAdjuster != null) {
			orderAdjuster = that.orderAdjuster.duplicate();
		}
		if (that.groupAdjuster != null) {
			groupAdjuster = that.groupAdjuster.duplicate();
		}
	}

	/**
	 * 构造一个默认的SELECT对象
	 */
	public Select() {
		super(SQLTag.SELECT_METHOD);
		// 默认不消除重复
		setDistinct(false);
		// 默认是自动调整SQL函数，即根据函数和实际列，生成新的列参数
		setAutoAdjust(true);
		// 默认不显示检索范围
		range.set(0, 0);
	}

	/**
	 * 构造SELECT对象，指定数据表名
	 * @param space 数据表名
	 */
	public Select(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析SQL SELECT的参数
	 * @param reader 可类化读取器
	 * @since 1.2
	 */
	public Select(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置消重复
	 * @param b 消重复标记
	 */
	public void setDistinct(boolean b) {
		distinct = b;
	}

	/**
	 * 判断是否消除重复
	 * @return 返回真或者假
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * 设置SQL函数/其它计算单元的自动调整标记
	 * @param b 自动调整标记
	 */
	public void setAutoAdjust(boolean b) {
		autoAdjust = b;
	}

	/**
	 * 判断是否自动调整SQL函数
	 * @return 返回真或者假
	 */
	public boolean isAutoAdjust() {
		return autoAdjust;
	}

	/**
	 * 判断列中否包含了SQL函数
	 * @return 返回真或者假
	 */
	public boolean hasFunctions() {
		return sheet.hasFunctions();
	}

	/**
	 * 设置记录的显示范围
	 * @param begin 开始点
	 * @param end 结束点
	 */
	public void setRange(int begin, int end) {
		range.set(begin, end);
	}

	/**
	 * 设置从0开始的记录的显示范围
	 * @param len 记录数
	 */
	public void setTop(int len) {
		if(len < 1) {
			throw new IllegalArgumentException("illegal top");
		}
		range.set(0, len - 1);
	}

	/**
	 * 返回显示范围的开始位置
	 * @return 显示的开始位置
	 */
	public int getBegin() {
		return range.begin();
	}

	/**
	 * 返回显示范围的结束位置
	 * @return 显示的结束位置
	 */
	public int getEnd() {
		return range.end();
	}

	/**
	 * 返回列标识号数组，包括列的编号，和函数绑定的列编号
	 * @return short数组
	 */
	public short[] getColumnIds() {
		return sheet.getColumnIds(getSpace());
	}

	/**
	 * 设置显示成员表
	 * @param e ListSheet实例
	 */
	public void setListSheet(ListSheet e) {
		sheet = e;
	}

	/**
	 * 返回显示成员表
	 * @return ListSheet实例
	 */
	public ListSheet getListSheet() {
		return sheet;
	}

	/**
	 * 设置"ORDER BY"实例
	 * @param e OrderByAdapter实例
	 */
	public void setOrder(OrderByAdapter e) {
		orderAdjuster = e;
	}

	/**
	 * 返回"ORDER BY"实例
	 * @return OrderByAdapter实例
	 */
	public OrderByAdapter getOrder() {
		return orderAdjuster;
	}

	/**
	 * 判断存在"ORDER BY"实例
	 * @return  返回真或者假
	 */
	public boolean hasOrder() {
		return orderAdjuster != null;
	}

	/**
	 * 设置"GROUP BY"实例
	 * @param e GroupByAdapter实例
	 */
	public void setGroup(GroupByAdapter e) {
		groupAdjuster = e;
	}

	/**
	 * 返回"GROUP BY"实例
	 * @return GroupByAdapter实例
	 */
	public GroupByAdapter getGroup() {
		return groupAdjuster;
	}

	/**
	 * 检查存在"GROUP BY"实例
	 * @return 返回真或者假
	 */
	public boolean hasGroup() {
		return groupAdjuster != null;
	}

	/**
	 * 根据当前SELECT实例，生成它的副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Select duplicate() {
		return new Select(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		return super.createTableRules(RuleOperator.SHARE_READ);
	}

	/**
	 * 需要被抽取的列标识号数组
	 * @return 字节数组
	 */
	private byte[] buildShowIds() {
		short[] array = sheet.getColumnIds(getSpace());
		int items = (array == null ? 0 : array.length);
		ClassWriter writer = new ClassWriter();
		writer.writeInt(items);
		for (int i = 0; i < items; i++) {
			writer.writeShort(array[i]);
		}
		return buildField(FieldTag.COLUMNIDS, writer.effuse());
	}

	/**
	 * 检查被抽取的列标识号数组
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int resolveShowIds(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		int items = reader.readInt();
		if (reader.getLeft() != items * 2) {
			throw new IllegalArgumentException("column identity sizeout");
		}
		return reader.getLength();
	}

	/**
	 * 生成"ORDER BY"语句的字节数组
	 */
	private byte[] buildOrderBy() {
		if (orderAdjuster == null) {
			return null;
		}
		ClassWriter writer = new ClassWriter();
		writer.writeObject(orderAdjuster);
		return buildField(FieldTag.ORDERBY, writer.effuse());
	}

	/**
	 * 从字节数组中解析"ORDER BY"语句
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int resolveOrderBy(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		int scale = reader.getSeek();
		orderAdjuster = new OrderByAdapter(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 生成DISTINCT标记
	 * @return
	 */
	private byte[] buildDistinct() {
		ClassWriter buff = new ClassWriter();
		buff.writeBoolean(distinct);
		return buildField(FieldTag.DISTINCT, buff.effuse());
	}

	/**
	 * 解析DISTINCT标记
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int resolveDistinct(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		int scale = reader.getSeek();
		distinct = reader.readBoolean();
		return reader.getSeek() - scale;
	}

	/**
	 * 生成范围，如果没有定义返回空指针
	 * @return
	 */
	private byte[] buildRange() {
		ClassWriter buff = new ClassWriter();
		buff.writeInt(range.begin());
		buff.writeInt(range.end());
		return buildField(FieldTag.RANGE, buff.effuse());
	}

	/**
	 * 解析范围
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int resolveRange(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		int scale = reader.getSeek();
		int begin = reader.readInt();
		int end = reader.readInt();
		range.set(begin, end);
		return reader.getSeek() - scale;
	}

	/**
	 * 将显示列集合转化为字节数组输出
	 * @return
	 */
	private byte[] buildListSheet() {
		ClassWriter writer = new ClassWriter();
		writer.writeObject(sheet);
		return buildField(FieldTag.LISTSHEET, writer.effuse());
	}

	/**
	 * 从字节数组中解析显示列集合
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int resolveListSheet(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		sheet = new ListSheet(reader);
		return reader.getLength();
	}

	/**
	 * 生成"GROUP BY"语句
	 * @return
	 */
	private byte[] buildGroupby() {
		if (groupAdjuster == null) {
			return null;
		}

		ClassWriter writer = new ClassWriter();
		writer.writeObject(groupAdjuster);
		return buildField(FieldTag.GROUPBY, writer.effuse());
	}

	/**
	 * 解析"GROUP BY"语句
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int resolveGroupby(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		int scale = reader.getSeek();
		groupAdjuster = new GroupByAdapter(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 将SELECT参数写入可类化存储器。这个输出需要兼容C接口
	 * @param writer
	 * @return
	 */
	private int buildX(ClassWriter writer) {
		ClassWriter buff = new ClassWriter();
		// 数据表名
		byte[] result = buildSpace();
		buff.write(result);
		// WHERE比较条件
		result = buildCondition();
		buff.write(result);
		// 被提取列标识集合(如果有函数，这个列不在显示集合中内则包括)
		result = buildShowIds();
		buff.write(result);
		// "ORDER BY"实例
		result = buildOrderBy();
		if (result != null) {
			buff.write(result);
		}
		// 检索显示范围
		result = buildRange();
		if (result != null) {
			buff.write(result);
		}
		// DISTINCT标记
		result = buildDistinct();
		if (result != null) {
			buff.write(result);
		}
		// 显示成员列表(实体列和函数列)
		result = buildListSheet();
		if(result != null) {
			buff.write(result);
		}
		// "GROUP BY"实例
		result = buildGroupby();
		if(result != null) {
			buff.write(result);
		}
		// 输出各单元组合的数据流
		result = buff.effuse();

		int size = writer.size();
		// SELECT字节流总长度(5: 总长度占4字节，加SELECT_METHOD的1个字节)
		writer.writeInt(5 + result.length);
		// SELECT 命令操作符
		writer.write(SQLTag.SELECT_METHOD);
		// 输出各单元数据流
		writer.write(result);

		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析SELECT参数。这个方法必须兼容C接口。
	 * @param reader
	 * @return
	 */
	private int resolveX(ClassReader reader) {
		final int seek = reader.getSeek();
		if (seek + 5 > reader.getEnd()) {
			throw new IndexOutOfBoundsException();
		}

		int maxsize = reader.readInt();
		byte method = reader.read();
		if (method != SQLTag.SELECT_METHOD) {
			throw new IllegalArgumentException("invalid select identity!");
		}

		int end = seek + maxsize;
		while (reader.getSeek() < end) {
			FieldBody body = super.resolveField(reader);

			byte[] data = body.data;
			switch (body.id) {
			case FieldTag.SPACE:
				resolveSpace(data, 0, data.length); break;
			case FieldTag.CONDITION:
				resolveCondition(data, 0, data.length); break;
			case FieldTag.COLUMNIDS:
				resolveShowIds(data, 0, data.length); break;
			case FieldTag.ORDERBY:
				resolveOrderBy(data, 0, data.length); break;
			case FieldTag.RANGE:
				resolveRange(data, 0, data.length); break;
			case FieldTag.DISTINCT:
				resolveDistinct(data, 0, data.length); break;
			case FieldTag.LISTSHEET:
				resolveListSheet(data, 0, data.length); break;
			case FieldTag.GROUPBY:
				resolveGroupby(data, 0, data.length); break;
			}
		}

		return reader.getSeek() - seek;
	}

	/**
	 * 生成"SELECT"数据流。<br>
	 * <b>这个方法兼容C接口</b>
	 * @return 返回字节数组
	 */
	public byte[] buildX() {
		ClassWriter writer = new ClassWriter();
		buildX(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析SELECT参数，返回解析的数据长度。<br>
	 * <b>这个方法兼容C接口</b>
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的字节数组长度
	 */
	public int resolveX(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolveX(reader);
	}

}