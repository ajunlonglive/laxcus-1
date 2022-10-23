/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 检索用户日志
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class ScanUserLog extends MultiUser {
	
	private static final long serialVersionUID = 7763714763568656826L;

	/** 开始时间 **/
	private long begin;
	
	/** 结束时间 **/
	private long end;

	/**
	 * 根据传入的检索用户日志命令，生成它的数据副本
	 * @param that ScanUserLog实例
	 */
	private ScanUserLog(ScanUserLog that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 构造默认的检索用户日志命令
	 */
	public ScanUserLog() {
		super();
		begin = 0;
		end = 0;
	}

	/**
	 * 构造检索用户日志命令，指定用户签名
	 * @param siger 用户签名
	 */
	public ScanUserLog(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化读取器中解析检索用户日志命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ScanUserLog(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置检索的时间范围
	 * @param from
	 * @param to
	 */
	public void setRange(long from, long to) {
		if (from > to) {
			throw new IllegalValueException("illegal value! %d > %d", from, to);
		}
		begin = from;
		end = to;
	}

	/**
	 * 返回时间范围范围，如果是空值，输出空指针
	 * @return LongRange实例或者空指针
	 */
	public LongRange getRange() {
		if (isAll()) {
			return null;
		}
		return new LongRange(begin, end);
	}

	/**
	 * 选择全部输出
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return begin == 0 && end == 0;
	}

	/**
	 * 返回开始时间
	 * @return
	 */
	public long getBegin() {
		return begin;
	}

	/**
	 * 返回结束时间
	 * @return
	 */
	public long getEnd() {
		return end;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanUserLog duplicate() {
		return new ScanUserLog(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(begin);
		writer.writeLong(end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		begin = reader.readLong();
		end = reader.readLong();
	}

}
