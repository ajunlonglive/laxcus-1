/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 多表、多列属性序列集合。<br>
 * 
 * 数据来源自终端或者应用接口上的用户录入，并且以此反馈给调用端。<br><br>
 * 
 * 它是对SQL “SELECT FROM”，或者其它多表环境之间的要求显示列的映射，ListSheet存储列成员和SQL函数成员两种，
 * 函数成员又分操作行/列的函数和不操作列的函数，操作行/列函数又分聚合函数和列函数，区别是聚合函数一次可操作多行/多列，
 * 而列函数一次只能操作一列。<br><br>
 * 
 * 与Sheet相同的是，都是按照用户输入的参数顺序存储，不同的是，ListSheet分为列和函数，
 * 在转化为Sheet表述时，函数参数转为列属性描述。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/9/2009
 * @since laxcus 1.0
 */
public final class ListSheet implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = -290537992112310460L;

	/** 显示成员集合 (包含列成员和函数成员) **/
	private ArrayList<ListElement> array = new ArrayList<ListElement>();

	/**
	 * 根据传入参数构造它的副本
	 * @param that ListSheet实例
	 */
	private ListSheet(ListSheet that) {
		super();
		// 保存数据副本
		for (ListElement e : that.array) {
			this.add(e.duplicate());
		}
		this.trim();
	}

	/**
	 * 构造一个默认的显示列表集合
	 */
	public ListSheet() {
		super();
	}
	
	/**
	 * 使用传入的可类化读取器，生成一个列显示成员集合
	 * @param reader 可类化读取器
	 */
	public ListSheet(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 检查是否存在函数成员
	 * @return 返回真或者假
	 */
	public boolean hasFunctions() {
		for(ListElement element : array) {
			if(element.isFunction()) return true;
		}
		return false;
	}

	/**
	 * 根据表名和列编号，查找这个表下属的全部聚合函数成员
	 * @param space 表名
	 * @param columnId 列编号
	 * @return 返回ListElement数组
	 */
	public ListElement[] findAggregateFunction(Space space, short columnId) {
		ArrayList<ListElement> list = new ArrayList<ListElement>();

		for (ListElement element : this.array) {
			if (!(space.equals(element.getSpace()) && element.isFunction())) {
				continue;
			}
			ColumnFunction function = ((FunctionElement) element).getFunction();
			if (function instanceof ColumnAggregateFunction) {
				if (((ColumnAggregateFunction) function).getColumnId() == columnId) {
					list.add(element);
				}
			}
		}

		if (list.isEmpty()) return null;

		ListElement[] s = new ListElement[list.size()];
		return list.toArray(s);
	}

	/**
	 * 提取显示表的数据表名称集合
	 * @return 返回Space集合
	 */
	public Set<Space> getSpaces() {
		Set<Space> set = new TreeSet<Space>();
		for (ListElement element : array) {
			set.add((Space) element.getSpace().clone());
		}
		return set;
	}

	/**
	 * 返回多个指定表下的显示成员集合
	 * @param spaces 表名数组
	 * @return ListSheet实例
	 */
	public ListSheet getListSheet(Space[] spaces) {
		ListSheet sheet = new ListSheet();
		for (ListElement that : this.array) {
			// 表名匹配，并且在集合中不存在，允许添加
			boolean okay = (this.matches(spaces, that.getSpace())
					&& !sheet.contains(that.getSpace(), that.getIdentity()));
			// 保存它的副本
			if (okay) {
				sheet.add(that.duplicate());
			}
		}
		return sheet;
	}

	/**
	 * 返回一个指定表下的显示成员集合
	 * @param space 表名
	 * @return ListSheet实例
	 */
	public ListSheet getListSheet(Space space) {
		return getListSheet(new Space[] { space });
	}

	/**
	 * 根据表名，选择一个匹配的表配置
	 * @param tables 数据表数组
	 * @param space 表名
	 * @return - 返回表的句柄，没有返回null
	 */
	private Table choice(Table[] tables, Space space) {
		for (int i = 0; i < tables.length; i++) {
			if (tables[i].getSpace().compareTo(space) == 0) {
				return tables[i];
			}
		}
		return null;
	}

	/**
	 * 根据要求的表，取出存储的显示集合。
	 * @param tables - 数据表数组
	 * @return 返回Sheet实例
	 */
	public Sheet getDisplaySheet(Table[] tables) {
		Sheet sheet = new Sheet();
		for(ListElement element : array) {
			// 从中找到一个表
			Table table = this.choice(tables, element.getSpace());
			// 如果没有，继续下一个
			if(table == null) {
				continue;
			}

			if(element.isColumn()) {
				ColumnElement that = (ColumnElement)element;
				short columnId = that.getColumnId();
				ColumnAttribute attribute = table.find(columnId);
				if (attribute == null) {
					throw new ColumnAttributeException("cannot find %d", columnId);
				}
				ColumnAttribute as = attribute.duplicate();
				as.setName(element.getName()); 	// 可能是列名或者列的别名
				sheet.add(sheet.size(), as);		// 下标从0开始
			} else if(element.isFunction()) {		
				// 根据返回类型，生成一个默认的列属性并且赋值
				ColumnAttribute attribute = ColumnAttributeCreator.create(element.getFamily());
				if (attribute == null) {
					throw new ColumnAttributeException("illegal family %d", element.getFamily());
				}
				// 标题可以是SQL原语或者别名
				attribute.setName(element.getName());
				// 列编号
				attribute.setColumnId(element.getIdentity());

				ColumnFunction function = ((FunctionElement) element).getFunction(); 
				// 如果函数中有列属性定义，找到这个列属性定义
				short columnId = 0;
				if (function instanceof ColumnFunction) {
					columnId = ((ColumnFunction) function).getColumnId();
				} else if (function instanceof ColumnAggregateFunction) {
					columnId = ((ColumnAggregateFunction) function).getColumnId();
				}

				boolean match = false;
				ColumnAttribute next = null;
				if(columnId > 0) {
					next = table.find(columnId);
					if(next == null) {
						throw new ColumnAttributeException("cannot find %d", columnId);
					}
					// 如果类型一致，使用表中的配置
					match = (attribute.getType() == next.getType());
					if (match) {
						next = next.duplicate();	//生成新副本
						next.setColumnId(element.getIdentity()); // 这里用函数列标识号
						next.setName(element.getName()); // 可以是SQL原语或者别名
					}
				}

				// 类型一致，使用表中的配置，否则使用新定义配置
				sheet.add(sheet.size(), (match ? next : attribute));
			}
		}

		return sheet;
	}

	/**
	 * 在终端/应用接口上的使用的顺序表
	 * 按照显示顺序，生成Sheet实例。如果是函数成员，返回对应的计算结果属性并且保存到Sheet
	 * @param table 数据表
	 * @return 返回Sheet实例
	 */
	public Sheet getDisplaySheet(Table table) {
		return this.getDisplaySheet(new Table[] { table });
	}

	/**
	 * 检查某一个被操作的列是否已经在集合中
	 * @param list ListElement列表
	 * @param that 另一个ListElement实例
	 * @return 返回真或者假
	 */
	private boolean inside(List<ListElement> list, ListElement that) {
		for (ListElement elem : list) {
			if (elem.getSpace().compareTo(that.getSpace()) == 0
					&& elem.getColumnId() == that.getColumnId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据表集合，找到这个显示序列中处理列的序列表集合，包括列成员和操作列的函数成员，不操作列的函数成员不计算在内
	 * @param tables 数据表数组
	 * @return 返回Sheet实例
	 */
	public Sheet getColumnSheet(Table[] tables) {
		ArrayList<ListElement> list = new ArrayList<ListElement>();
		for(ListElement that : this.array) {
			// 返回列标识号，如果是函数成员，返回它的计算列的标识号
			short columnId = that.getColumnId();
			// 不操作列的函数不计算在内
			if(columnId ==0) {
				continue;
			}
			// 从中找到一个表
			Table table = this.choice(tables, that.getSpace());
			// 如果没有，继续下一个
			if(table == null) {
				continue;
			}

			ColumnAttribute attribute = table.find(columnId);
			if (attribute == null) {
				throw new ColumnAttributeException("cannot find attribute by %s - %d", that.getSpace(), columnId);
			}

			// 如果这一列没有包括在内，保存它
			if(!this.inside(list, that)) {
				list.add(that);
			}
		}

		// 输出排列结果
		Sheet sheet = new Sheet();
		for(ListElement that : list) {
			Table table = this.choice(tables, that.getSpace());
			ColumnAttribute attribute = table.find(that.getColumnId());
			// 下标从0开始
			sheet.add(sheet.size(), attribute);
		}
		return sheet;
	}

	/**
	 * 根据表配置，查找其属下的所有显示成员，包括列成员和操作列的函数成员，不操作列的函数成员被忽略
	 * @param table 数据表
	 * @return 返回Sheet实例
	 */
	public Sheet getColumnSheet(Table table) {
		return this.getColumnSheet(new Table[] { table });
	}

	/**
	 * 检查某个表名是否在要求的集合中
	 * @param spaces 表名数组
	 * @param that 另一个表名 
	 * @return 返回真或者假
	 */
	private boolean matches(Space[] spaces, Space that) {
		for (int i = 0; i < spaces.length; i++) {
			if (spaces[i].compareTo(that) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据表名集合，返回它属下按照顺序排列的显示成员的列编号。<br>
	 * 因为有多个表，列编号存在重复的可能。<br>
	 * @param spaces 表名数组
	 * @return 按照顺序排列的列编号
	 */
	public List<java.lang.Short> findColumnIds(Space[] spaces) {
		ArrayList<ListElement> list = new ArrayList<ListElement>();
		for (ListElement that : this.array) {
			// 表名必须存在于集合
			if (!matches(spaces, that.getSpace())) {
				continue;
			}
			// 提取列编号
			short columnId = that.getColumnId();
			// 列编号不是0，并且集合中没有这个显示成员，保存它
			if (columnId != 0 && !inside(list, that)) {
				list.add(that);
			}
		}

		// 输出结果(如果是多表，存在列标识号重复的可能)
		ArrayList<java.lang.Short> results = new ArrayList<java.lang.Short>();
		for (ListElement that : list) {
			results.add(that.getColumnId());
		}
		return results;
	}

	/**
	 * 根据表名，返回它属下按照顺序排列的显示成员的列标识号，<br>
	 * 处理的对象包括：列成员、操作列的函数成员，忽略不操作列的函数成员。<br>
	 * 
	 * @param space - 表名
	 * @return java.util.List - 列标识号排列集合，按照用户的定义排列
	 */
	public List<java.lang.Short> findColumnIds(Space space) {
		return this.findColumnIds(new Space[] { space });
	}

	/**
	 * 根据表名，返回它属下按照顺序排列的显示成员的列标识号数组，<br>
	 * 处理的对象包括：列成员、操作列的函数成员，忽略不操作列的函数成员。<br>
	 * @param space - 表名
	 * @return short[] - 列标识号数组(按照显示顺序排列)，如果没有匹配返回空指针(null)
	 */
	public short[] getColumnIds(Space space) {
		List<java.lang.Short> list = this.findColumnIds(space);
		if (list.isEmpty()) {
			return null;
		}

		short[] s = new short[list.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = list.get(i).shortValue();
		}
		return s;
	}

	/**
	 * 根据表名和成员标识号(包括函数成员和列成员)，检查这个显示成员是否存在。应用于单表环境
	 * @param space - 表名
	 * @param identity - 函数成员的函数标识号，或者列成员的列标识号
	 * @return 返回真或者假
	 */
	public boolean contains(Space space, short identity) {
		for (ListElement element : array) {
			if (space.equals(element.getSpace()) && element.getIdentity() == identity) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 保存一个显示成员
	 * @param e ListElement实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ListElement e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}

	/**
	 * 返回全部显示成员
	 * @return ListElement列表
	 */
	public List<ListElement> list() {
		return new ArrayList<ListElement>(array);
	}

	/**
	 * 返回指定下标的显示成员
	 * @param index 下标
	 * @return ListElement实例
	 */
	public ListElement get(int index) {
		if(index < 0 || index >= array.size()) {
			return null;
		}
		return array.get(index);
	}

	/**
	 * 统计显示成员数目
	 * @return 成员数目
	 */
	public int size() {
		return this.array.size();
	}

	/**
	 * 收缩显示成员数组的内存空间
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 生成当前实例的数据副本
	 * @return 返回另一个ListSheet实例
	 */
	public ListSheet duplicate() {
		return new ListSheet(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();

		// 成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (int i = 0; i < array.size(); i++) {
			writer.writeObject(array.get(i));
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 成员数目
		int size = reader.readInt();
		// 读每一个成员
		for (int i = 0; i < size; i++) {
			byte kind = reader.current();
			ListElement element = ListElementCreator.create(kind);
			if (element == null) {
				throw new NullPointerException();
			}
			// 解析数据
			element.resolve(reader);
			// 保存
			array.add(element);
		}

		return reader.getSeek() - scale;
	}

}