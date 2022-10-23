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
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;

/**
 * 导出数据块命令。<br><br>
 * 
 * 命令格式：
 * 1. EXPORT ENTITY 数据库.表 数据块编号 <br>
 * 2. EXPORT ENTITY 数据库.表  数据块编号 TO 磁盘文件  TYPE [CSV|TXT] CHARSET [GBK|UTF8|UTF16|UTF32] <br><BR>
 * 
 * 此操作由FRONT节点发出，通过CALL站点，分发到DATA主站点。<br>
 * FRONT -> CALL -> MASTER DATA (ALL SITES)<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class ExportEntity extends ExchangeEntity {

	private static final long serialVersionUID = -218616825553498182L;

	//	/** 数据块编号 **/
	//	private long stub;

	/** 数据块导出单元 **/
	private ArrayList<ExportEntityItem> array = new ArrayList<ExportEntityItem>();

	/**
	 * 构造默认和私有导出数据块命令
	 */
	private ExportEntity() {
		super();
		setCharset(CharsetType.UTF8); // 不定义，默认是UTF8编码
		setType(EntityStyle.CSV); // 默认是CSV样式
	}

	/**
	 * 从传入的导出数据块命令，生成它的数据副本
	 * @param that ExportEntity实例
	 */
	private ExportEntity(ExportEntity that) {
		super(that);
		//		stub = that.stub;
		array.addAll(that.array);
	}

	/**
	 * 构造导出数据块命令，指定数据表名
	 * @param space 数据表名
	 */
	public ExportEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析导出数据块命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ExportEntity(ClassReader reader) {
		this();
		resolve(reader);
	}

	//	/**
	//	 * 设置数据块编号
	//	 * @param id 数据块编号
	//	 */
	//	public void setStub(long id) {
	//		stub = id;
	//	}
	//
	//	/**
	//	 * 返回数据块编号
	//	 * @return 数据块编号
	//	 */
	//	public long getStub() {
	//		return stub;
	//	}


	/**
	 * 保存数据块导出单元
	 * @param item 数据块导出单元
	 * @return 返回真或者假
	 */
	public boolean add(ExportEntityItem item) {
		Laxkit.nullabled(item);
		if (array.contains(item)) {
			return false;
		}
		return array.add(item);
	}

	/**
	 * 输出数据块导出单元
	 * @return
	 */
	public List<ExportEntityItem> list() {
		return new ArrayList<ExportEntityItem>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ExportEntity duplicate() {
		return new ExportEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (ExportEntityItem e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ExportEntityItem e = new ExportEntityItem(reader);
			add(e);
		}
	}
}