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
 * 设置一个用户的最大定时优化表。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxRegulates extends SetSingleUserParameter {

	private static final long serialVersionUID = 5504350164714024747L;

	/** 用户最大定时优化表数 **/
	private int regulates;

	/**
	 * 根据传入的设置最大定时优化表命令，生成它的数据副本
	 * @param that 设置最大定时优化表命令
	 */
	private SetSingleMaxRegulates(SetSingleMaxRegulates that) {
		super(that);
		regulates = that.regulates;
	}

	/**
	 * 构造默认和私有的设置最大定时优化表命令
	 */
	private SetSingleMaxRegulates() {
		super();
	}

	/**
	 * 构造设置最大定时优化表，指定数目
	 * @param siger 用户签名
	 * @param max 最大定时优化表数目
	 */
	public SetSingleMaxRegulates(Siger siger, int max) {
		this();
		setSiger(siger);
		setRegulates(max);
	}

	/**
	 * 从可类化数据读取器中解析设置最大定时优化表命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxRegulates(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置最大定时优化表数
	 * @param i 最大定时优化表数
	 */
	public void setRegulates(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		regulates = i;
	}

	/**
	 * 返回最大定时优化表数
	 * @return 最大定时优化表数
	 */
	public int getRegulates() {
		return regulates;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxRegulates duplicate() {
		return new SetSingleMaxRegulates(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(regulates);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		regulates = reader.readInt();
	}

}