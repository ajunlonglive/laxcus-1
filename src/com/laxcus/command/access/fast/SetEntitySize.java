/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * “重置数据块尺寸”命令。<br>
 * 命令格式: UPDATE ENTITY SIZE schema.table digit{M}。如：update entity size laxcus.ai 128m
 * 
 * @author scott.liang
 * @version 1.2 12/3/2013
 * @since laxcus 1.0
 */
public final class SetEntitySize extends FastSpace {

	private static final long serialVersionUID = 6903842850559523808L;

	/** 数据块的尺寸，以字节为单位 **/
	private int size;

	/**
	 * 构造默认和私有的“重置数据块尺寸”命令
	 */
	private SetEntitySize() {
		super();
	}

	/**
	 * 根据传入的“重置数据块尺寸”命令，生成它的数据副本
	 * @param that SetEntitySize实例
	 */
	private SetEntitySize(SetEntitySize that) {
		super(that);
		size = that.size;
	}

	/**
	 * 构造“重置数据块尺寸”命令，指定数据表名和数据块尺寸。
	 * @param space 数据表名
	 * @param size 数据块尺寸
	 */
	public SetEntitySize(Space space, int size) {
		super();
		setSpace(space);
		setSize(size);
	}

	/**
	 * 从可类化读取器中解析“重置数据块尺寸”命令
	 * @param reader 可类化读取器
	 * @since 1.2
	 */
	public SetEntitySize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 重置数据块尺寸
	 * @param i 数据块尺寸
	 */
	public void setSize(int i) {
		if (i < 0) {
			throw new IllegalArgumentException("illegal size");
		}
		size = i;
	}

	/**
	 * 返回数据块尺寸
	 * @return 数据块尺寸
	 */
	public int getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetEntitySize duplicate() {
		return new SetEntitySize(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(size);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		size = reader.readInt();
	}

}