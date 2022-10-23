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
 * 设置用户的BUILD节点数目。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxBuilders extends SetSingleUserParameter {

	private static final long serialVersionUID = 2777692526095900873L;

	/** BUILD节点数目 **/
	private int builders;

	/**
	 * 根据传入的设置用户的BUILD节点数目命令，生成它的数据副本
	 * @param that 设置用户的BUILD节点数目命令
	 */
	private SetSingleMaxBuilders(SetSingleMaxBuilders that) {
		super(that);
		builders = that.builders;
	}

	/**
	 * 构造默认和私有的设置用户的BUILD节点数目命令
	 */
	private SetSingleMaxBuilders() {
		super();
	}

	/**
	 * 构造设置用户的BUILD节点数目，指定数目
	 * @param siger 用户签名
	 * @param max 最大并行任务数数目
	 */
	public SetSingleMaxBuilders(Siger siger, int max) {
		this();
		setSiger(siger);
		setBuilders(max);
	}

	/**
	 * 从可类化数据读取器中解析设置用户的BUILD节点数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxBuilders(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户的BUILD节点数目数
	 * @param i 最大并行任务数数
	 */
	public void setBuilders(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		builders = i;
	}

	/**
	 * 返回最大并行任务数数
	 * @return 最大并行任务数数
	 */
	public int getBuilders() {
		return builders;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxBuilders duplicate() {
		return new SetSingleMaxBuilders(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(builders);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		builders = reader.readInt();
	}

}