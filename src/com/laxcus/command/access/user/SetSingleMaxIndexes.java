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
 * 设置一个用户的每个表的最大索引数目。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxIndexes extends SetSingleUserParameter {

	private static final long serialVersionUID = 5504350164714024747L;

	/** 用户每个表的最大索引数目数 **/
	private int indexes;

	/**
	 * 根据传入的设置每个表的最大索引数目命令，生成它的数据副本
	 * @param that 设置每个表的最大索引数目命令
	 */
	private SetSingleMaxIndexes(SetSingleMaxIndexes that) {
		super(that);
		indexes = that.indexes;
	}

	/**
	 * 构造默认和私有的设置每个表的最大索引数目命令
	 */
	private SetSingleMaxIndexes() {
		super();
	}

	/**
	 * 构造设置每个表的最大索引数目，指定数目
	 * @param siger 用户签名
	 * @param max 每个表的最大索引数目数目
	 */
	public SetSingleMaxIndexes(Siger siger, int max) {
		this();
		setSiger(siger);
		setIndexes(max);
	}

	/**
	 * 从可类化数据读取器中解析设置每个表的最大索引数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxIndexes(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置每个表的最大索引数目数
	 * @param i 每个表的最大索引数目数
	 */
	public void setIndexes(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		indexes = i;
	}

	/**
	 * 返回每个表的最大索引数目数
	 * @return 每个表的最大索引数目数
	 */
	public int getIndexes() {
		return indexes;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxIndexes duplicate() {
		return new SetSingleMaxIndexes(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(indexes);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		indexes = reader.readInt();
	}

}