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
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 打印数据表图谱命令。<br><br>
 * 
 * 命令格式1：PRINT TABLE DIAGRAM 数据库.表, 数据库.表, ... FROM ME|SIGER, ... <br>
 * 命令格式2: PRINT TABLE DIAGRAM ALL FROM ME|SIGER, ...  <br>
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class PrintTableDiagram extends MultiUser {
	
	private static final long serialVersionUID = 3720183780556721183L;

	/** 数据表名集合  **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认的打印数据表图谱命令。
	 */
	public PrintTableDiagram() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析打印数据表图谱
	 * @param reader 可类化数据读取器
	 */
	public PrintTableDiagram(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的打印数据表图谱命令，生成它的数据副本
	 * @param that PrintTableDiagram实例
	 */
	private PrintTableDiagram(PrintTableDiagram that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 判断是处理自己的数据
	 * @return 返回真或者假
	 */
	public boolean isMe() {
		return getUserSize() ==0;
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
	public PrintTableDiagram duplicate() {
		return new PrintTableDiagram(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
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
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
	}

}