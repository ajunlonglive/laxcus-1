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

/**
 * 设置一个用户可以使用的最大磁盘空间。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxSize extends SetSingleUserParameter {

	private static final long serialVersionUID = 5504350164714024747L;

	/** 用户可以使用的最大磁盘空间 **/
	private long size;

	/**
	 * 根据传入的设置最大表数目命令，生成它的数据副本
	 * @param that 设置最大表数目命令
	 */
	private SetSingleMaxSize(SetSingleMaxSize that) {
		super(that);
		size = that.size;
	}

	/**
	 * 构造默认和私有的设置最大表数目命令
	 */
	private SetSingleMaxSize() {
		super();
	}

	/**
	 * 构造设置最大表数目，指定数目
	 * @param siger 用户签名
	 * @param max 可以使用的最大磁盘空间目
	 */
	public SetSingleMaxSize(Siger siger, long max) {
		this();
		setSiger(siger);
		setSize(max);
	}

	/**
	 * 从可类化数据读取器中解析设置最大表数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxSize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置可以使用的最大磁盘空间
	 * @param i 可以使用的最大磁盘空间
	 */
	public void setSize(long i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		size = i;
	}

	/**
	 * 返回可以使用的最大磁盘空间
	 * @return 可以使用的最大磁盘空间
	 */
	public long getSize() {
		return size;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxSize duplicate() {
		return new SetSingleMaxSize(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(size);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		size = reader.readLong();
	}

}