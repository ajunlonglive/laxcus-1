/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;

/**
 * 数据转变命令。<br>
 * 是ExportEntity / ImportEntity的超类。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2019
 * @since laxcus 1.0
 */
abstract class ExchangeEntity extends ProcessTable {

	private static final long serialVersionUID = 1368681805698323782L;

	/** 字符集，见 com.laxcus.util.charset.CharsetType中的定义 **/
	private int charset;
	
	/** 磁盘文件类型，见 EntityStyle 定义  **/
	private int type;
	
//	/** 磁盘文件名，不做可类化处理 **/
//	private File file;
	
	/**
	 * 构造默认和私有的数据转变命令。
	 */
	protected ExchangeEntity() {
		super();
		charset = CharsetType.NONE;
		type = EntityStyle.NONE;
	}

	/**
	 * 生成数据转变命令的数据副本
	 * @param that 数据转变命令
	 */
	protected ExchangeEntity(ExchangeEntity that) {
		super(that);
		charset = that.charset;
		type = that.type;
//		file = that.file;
//		array.addAll(that.array);
	}

	/**
	 * 构造数据转变命令，指定数据表名
	 * @param space
	 */
	protected ExchangeEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 设置字符集
	 * @param who 字符集类型
	 */
	public void setCharset(int who) {
		if (!CharsetType.isCharset(who)) {
			throw new IllegalValueException("illegal charset %d", who);
		}
		charset = who;
	}

	/**
	 * 设置字符集
	 * @param who 字符集的字符串描述
	 */
	public void setCharset(String who) {
		int value = CharsetType.translate(who);
		setCharset(value);
	}

	/**
	 * 返回字符集
	 * 
	 * @return 字符集描述
	 */
	public int getCharset() {
		return charset;
	}

	/**
	 * 设置文件类型，见  com.laxcus.command.access.table.EntityStyle 定义
	 * @param who 文件类型
	 */
	public void setType(int who) {
		if (!EntityStyle.isType(who)) {
			throw new IllegalValueException("illegal type %d", who);
		}
		type = who;
	}
	
	/**
	 * 返回文件类型。见 com.laxcus.command.access.table.EntityStyle 定义
	 * @return 文件类型
	 */
	public int getType() {
		return type;
	}
	


//	/**
//	 * 设置文件
//	 * @param e
//	 */
//	public void setFile(File e) {
//		file = e;
//	}
//
//	/**
//	 * 返回文件名
//	 * @return
//	 */
//	public File getFile() {
//		return file;
//	}
//
//	/**
//	 * 判断文件类型
//	 * @param who
//	 * @param e
//	 */
//	public void setFile(int who, File e) {
//		if (!EntityStyle.isType(who)) {
//			throw new IllegalValueException("illegal type %d", who);
//		}
//		type = who;
//		file = e;
//	}
//
//	/**
//	 * 判断是输出为文件
//	 * @return 返回真或者假
//	 */
//	public boolean hasFile() {
//		return EntityStyle.isType(type) && file != null;
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(charset);
		writer.writeInt(type);


		// String s = (file != null ? file.toString() : null);
		// writer.writeString(s);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		charset = reader.readInt();
		type = reader.readInt();

		
	}

}
