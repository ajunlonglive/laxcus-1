/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 打印数据库图谱命令。<br><br>
 * 
 * 命令格式1：PRINT DATABASE DIAGRAM 数据库, 数据库, ... FROM ME|SIGER, ... <br>
 * 命令格式2: PRINT DATABASE DIAGRAM ALL FROM ME|SIGER, ... <br>
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class PrintSchemaDiagram extends MultiUser {

	private static final long serialVersionUID = -3539120007994125556L;

	/** 数据库名集合  **/
	private TreeSet<Fame> array = new TreeSet<Fame>();

	/**
	 * 构造默认的打印数据库图谱命令。
	 */
	public PrintSchemaDiagram() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析打印数据库图谱
	 * @param reader 可类化数据读取器
	 */
	public PrintSchemaDiagram(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的打印数据库图谱命令，生成它的数据副本
	 * @param that PrintSchemaDiagram实例
	 */
	private PrintSchemaDiagram(PrintSchemaDiagram that) {
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
	 * 保存一个数据库名
	 * @param e Fame实例
	 * @return 返回真或者假
	 */
	public boolean add(Fame e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 保存一批数据库
	 * @param a 数据库集合
	 * @return 返回新增数目
	 */
	public int addAll(Collection<Fame> a) {
		int size = array.size();
		for (Fame e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回数据库名列表
	 * @return Fame列表
	 */
	public List<Fame> list() {
		return new ArrayList<Fame>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回数据库数目
	 * @return 数据库数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PrintSchemaDiagram duplicate() {
		return new PrintSchemaDiagram(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (Fame e : array) {
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
			Fame e = new Fame(reader);
			array.add(e);
		}
	}

}