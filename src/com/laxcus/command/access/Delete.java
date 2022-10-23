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
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;

/**
 * SQL删除命令。<br>
 * 是“DELETE FROM ...”语句实现。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2009
 * @since laxcus 1.0
 */
public final class Delete extends Query {

	private static final long serialVersionUID = 6177205894497529848L;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Query#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Query#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

	/**
	 * 生成当前DELETE命令的数据副本
	 * @param that Delete实例
	 */
	private Delete(Delete that) {
		super(that);
	}

	/**
	 * 构造默认的DELETE实例
	 */
	public Delete() {
		super(SQLTag.DELETE_METHOD);
	}

	/**
	 * 构造DELETE实例，和指定数据表名
	 * @param space 表名
	 */
	public Delete(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从传入的可类化读取器中获取DELETE实例参数
	 * @param reader - 可类化读取器
	 */
	public Delete(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Delete duplicate() {
		return new Delete(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		return createTableRules(RuleOperator.EXCLUSIVE_WRITE);
	}

	/**
	 * 将DELETE命令转为字节，写入可类化写入器。<br>
	 * <b>这个方法兼容C接口！！！</b>
	 * 
	 * @param writer 可类化写入器
	 * @return 返回生成的字节数组长度
	 */
	private int buildX(ClassWriter writer) {
		ClassWriter buff = new ClassWriter(1024);
		// 表空间名
		byte[] result = buildSpace();
		buff.write(result);
		// 检索条件
		result = buildCondition();
		buff.write(result);

		// 输出结果
		result = buff.effuse();

		final int scale = writer.size();
		writer.writeInt(5 + result.length);
		writer.write(SQLTag.DELETE_METHOD);
		writer.write(result);
		return writer.size() - scale;
	}

	/**
	 * 从数据流中解析参数到DELETE命令。<br>
	 * <b>这个方法兼容C接口！！！</b>
	 * 
	 * @param reader 可类化读取器
	 * @return 返回解析的字节长度
	 */
	private int resolveX(ClassReader reader) {
		final int scale = reader.getSeek();
		if (scale + 5 > reader.getEnd()) {
			throw new IndexOutOfBoundsException();
		}

		int maxsize = reader.readInt();
		byte method = reader.read();
		if (method != SQLTag.DELETE_METHOD) {
			throw new IllegalArgumentException("invalid delete identity!");
		}

		int end = scale + maxsize;
		while (reader.getSeek() < end) {
			FieldBody body = super.resolveField(reader);
			byte[] data = body.data;
			switch (body.id) {
			case FieldTag.SPACE:
				resolveSpace(data, 0, data.length); break;
			case FieldTag.CONDITION:
				resolveCondition(data, 0, data.length); break;
			}
		}
		return reader.getSeek() - scale;
	}

	/**
	 * 生成DELETE命令数据流。<br>
	 * <b>这个方法兼容C接口！！！</b>
	 * 
	 * @return 字节数组
	 */
	public byte[] buildX() {
		ClassWriter writer = new ClassWriter();
		buildX(writer);
		return writer.effuse();
	}

	/**
	 * 解析数据流，返回解析的字节长度。<br>
	 * <b>这个方法兼容C接口！！！</b>
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的字节长度
	 */
	public int resolveX(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolveX(reader);
	}

}