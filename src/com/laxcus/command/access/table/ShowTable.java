/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 显示数据表命令。<br><br>
 * 
 * 命令格式1：SHOW TABLE 数据库.表, 数据库.表, ... <br>
 * 命令格式2: SHOW TABLE ALL  <br>
 * 
 * @author scott.liang
 * @version 1.1 03/02/2015
 * @since laxcus 1.0
 */
public class ShowTable extends Command {
	
	private static final long serialVersionUID = 6647722667614502686L;

	/** 数据表名集合  **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认的显示数据表命令。
	 */
	public ShowTable() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示数据表
	 * @param reader 可类化数据读取器
	 */
	public ShowTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的显示数据表命令，生成它的数据副本
	 * @param that ShowTable实例
	 */
	private ShowTable(ShowTable that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 判断显示全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/**
	 * 保存一个数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean add(Space e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}

	/**
	 * 保存一批数据表
	 * @param a 数据表集合
	 * @return 返回新增数目
	 */
	public int addAll(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回数据表名列表
	 * @return Space列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回表名数目
	 * @return 表名数
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowTable duplicate() {
		return new ShowTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Space e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
	}

}
