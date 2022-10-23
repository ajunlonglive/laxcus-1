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
 * INSERT设定命令 <br><br>
 * 
 * 此命令由DATA主站点发出，目标是发出INSERT请求的站点（CALL/WORK）。DATA主站点要发送两次AssumeInsert命令，才能实现一个完整的添加操作。
 * 
 * @author scott.liang
 * @version 1.12 9/07/2016
 * @since laxcus 1.0
 */
public final class AssumeInsert extends AssumeConsult {

	private static final long serialVersionUID = -3059835772570890587L;

	/** 被添加的行数 **/
	private long rows;

	/**
	 * 构造默认和私有的INSERT设定命令
	 */
	private AssumeInsert() {
		super();
	}

	/**
	 * 根据传入的INSERT设定命令，生成它的数据副本
	 * @param that AssumeInsert实例
	 */
	private AssumeInsert(AssumeInsert that) {
		super(that);
		rows = that.rows;
	}

	/**
	 * 构造INSERT设定命令，指定表名和状态码
	 * @param space 表名
	 * @param status 状态码
	 */
	public AssumeInsert(Space space, byte status) {
		this();
		setSpace(space);
		setStatus(status);
	}

	/**
	 * 从可类化数据读取器中解析INSERT设定命令
	 * @param reader 可类化数据读取器
	 * @since 1.12
	 */
	public AssumeInsert(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置被添加行数
	 * @param i 添加行数
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回被添加行数
	 * @return 添加行数
	 */
	public long getRows() {
		return rows;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Consult#duplicate()
	 */
	@Override
	public AssumeInsert duplicate() {
		return new AssumeInsert(this);
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
	}

}