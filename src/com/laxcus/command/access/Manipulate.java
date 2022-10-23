/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * SQL操作命令 <br><br>
 * 
 * 标准SQL操作命令包括：INSERT、DELETE、UPDATE、SELECT <br>
 * 
 * @author scott.liang
 * @version 1.3 7/22/2014
 * @since laxcus 1.0
 */
public abstract class Manipulate extends RuleCommand {

	private static final long serialVersionUID = 6540641966804741102L;

	final class FieldBody {
		byte id;
		byte[] data;

		public FieldBody() {
			super();
		}

		public FieldBody(byte id, byte[] b) {
			this.id = id;
			this.data = b;
		}
	}

	/** 命令类型，见SQLTag中的定义。 */
	private byte family;

	/** 数据表名  **/
	private Space space;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 操作命令编号
		writer.write(family);
		// 数据表名
		writer.writeInstance(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// SQL操作命令编号
		family = reader.read();
		// 数据表名
		space = reader.readInstance(Space.class);
	}

	/**
	 * 构造SQL操作命令，指定SQL操作命令标识
	 * @param family SQL操作命令标识
	 */
	protected Manipulate(byte family) {
		super();
		setFamily(family);
	}

	/**
	 * 根据传入的SQL操作命令实例，生成它的数据副本
	 * @param that Manipulate实例
	 */
	protected Manipulate(Manipulate that) {
		super(that);
		family = that.family;
		if (that.space != null) {
			space = that.space.duplicate();
		}
	}

	/**
	 * 设置SQL操作命令标识，见SQLTag中的定义，只允许子类调用。
	 * @param who SQL操作命令标识
	 */
	protected void setFamily(byte who) {
		// 检查参数
		if (!SQLTag.isMethod(who)) {
			throw new IllegalArgumentException("illegal method " + who);
		}
		family = who;
	}

	/**
	 * 返回SQL操作命令标识
	 * @return SQL操作命令标识
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 根据编码，生成SQL数据单元域
	 * @param id 域标识
	 * @param data 域字节数组
	 * @return 返回字节数组
	 */
	protected byte[] buildField(byte id, byte[] data) {
		ClassWriter writer = new ClassWriter();
		int len = (data == null ? 0 : data.length);
		// 标识
		writer.write(id);
		// 数据长度
		writer.writeInt(len);
		// 写数据
		if (len > 0) {
			writer.write(data, 0, data.length);
		}
		return writer.effuse();
	}

	/**
	 * 解析一个单元域
	 * @param reader 可类化读取器
	 * @return FieldBody实例
	 */
	protected FieldBody resolveField(ClassReader reader) {
		// 读标识
		byte id = reader.read();
		// 读数据长度
		int size = reader.readInt();
		// 根据长度读数据
		byte[] bytes = reader.read(size);
		// 返回结果
		return new FieldBody(id, bytes);
	}

	/**
	 * 解析一个单元域
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return FieldBody实例
	 */
	protected FieldBody resolveField(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolveField(reader);
	}

	/**
	 * 生成生成数据表名的字节数组
	 * @return 字节数组
	 */
	protected byte[] buildSpace() {
		byte[] s = space.build();
		return buildField(FieldTag.SPACE, s);
	}

	/**
	 * 解析表的空间名称
	 * @param reader 可类化读取器
	 * @return 解析字节长度
	 */
	protected int resolveSpace(ClassReader reader) {
		int scale = reader.getSeek();
		space = new Space(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 解析表的空间名称
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析字节长度
	 */
	protected int resolveSpace(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolveSpace(reader);
	}

}