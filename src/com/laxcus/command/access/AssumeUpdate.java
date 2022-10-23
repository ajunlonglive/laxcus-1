/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * UPDATE设定命令<br><br>
 * 
 * 此命令由DATA主站点发出，目标是CALL站点。DATA主站点要发送两次AssumeUpdate命令，才能实现一个完整的更新操作。
 * 
 * @author scott.liang
 * @version 1.12 9/07/2016
 * @since laxcus 1.0
 */
public final class AssumeUpdate extends AssumeConsult {

	private static final long serialVersionUID = -5483281444328503705L;

	/** 被更新的行数 **/
	private long rows;

	/** 被更新的数据内容 **/
	private byte[] data;

	/**
	 * 构造默认和私有的UPDATE设定命令
	 */
	private AssumeUpdate() {
		super();
	}

	/**
	 * 根据传入的UPDATE设定命令，生成它的数据副本
	 * @param that AssumeUpdate实例
	 */
	private AssumeUpdate(AssumeUpdate that) {
		super(that);
		rows = that.rows;
		data = that.data;
	}

	/**
	 * 构造UPDATE设定命令，指定表名和状态码
	 * @param space 表名
	 * @param status 状态码
	 */
	public AssumeUpdate(Space space, byte status) {
		this();
		setSpace(space);
		setStatus(status);
	}

	/**
	 * 从可类化数据读取器中解析UPDATE设定命令
	 * @param reader 可类化数据读取器
	 * @since 1.12
	 */
	public AssumeUpdate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置被更新行数
	 * @param i 被更新行数
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回被更新行数
	 * @return 被更新行数
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * 设置更新的数据内容
	 * @param b 数据内容的字节数组
	 */
	public void setData(byte[] b) {
		data = b;
	}

	/**
	 * 返回更新的数据内容
	 * @return 据内容的字节数组
	 */
	public byte[] getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Consult#duplicate()
	 */
	@Override
	public AssumeUpdate duplicate() {
		return new AssumeUpdate(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%d/%d", getSpace(), getStatus(), rows);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Consult#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 行数
		writer.writeLong(rows);
		// 更新数据内容
		writer.writeByteArray(data);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Consult#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 行数
		rows = reader.readLong();
		// 更新数据内容
		data = reader.readByteArray();
	}

}