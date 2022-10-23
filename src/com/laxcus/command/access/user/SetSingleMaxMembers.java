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
 * 设置一个用户的最大在线用户数目。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxMembers extends SetSingleUserParameter {

	private static final long serialVersionUID = 5504350164714024747L;

	/** 用户最大在线用户数目数 **/
	private int members;

	/**
	 * 根据传入的设置最大在线用户数目命令，生成它的数据副本
	 * @param that 设置最大在线用户数目命令
	 */
	private SetSingleMaxMembers(SetSingleMaxMembers that) {
		super(that);
		members = that.members;
	}

	/**
	 * 构造默认和私有的设置最大在线用户数目命令
	 */
	private SetSingleMaxMembers() {
		super();
	}

	/**
	 * 构造设置最大在线用户数目，指定数目
	 * @param siger 用户签名
	 * @param max 最大在线用户数目数目
	 */
	public SetSingleMaxMembers(Siger siger, int max) {
		this();
		setSiger(siger);
		setMembers(max);
	}

	/**
	 * 从可类化数据读取器中解析设置最大在线用户数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxMembers(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置最大在线用户数目数
	 * @param i 最大在线用户数目数
	 */
	public void setMembers(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		members = i;
	}

	/**
	 * 返回最大在线用户数目数
	 * @return 最大在线用户数目数
	 */
	public int getMembers() {
		return members;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxMembers duplicate() {
		return new SetSingleMaxMembers(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(members);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		members = reader.readInt();
	}

}