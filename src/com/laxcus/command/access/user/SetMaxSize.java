/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置一个账号最大磁盘空间，单位以字节计。<br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/24/2018
 * @since laxcus 1.0
 */
public final class SetMaxSize extends SetMultiUserParameter {

	private static final long serialVersionUID = 6419083423134031988L;

	/** 用户最大磁盘空间 **/
	private long size;

	/**
	 * 根据传入的设置用户最大磁盘空间命令，生成它的数据副本
	 * @param that 设置用户最大磁盘空间命令
	 */
	private SetMaxSize(SetMaxSize that) {
		super(that);
		size = that.size;
	}

	/**
	 * 构造默认和私有的设置用户最大磁盘空间命令
	 */
	private SetMaxSize() {
		super();
	}

	/**
	 * 构造设置用户最大磁盘空间，指定数目
	 * @param max 最大磁盘空间目
	 */
	public SetMaxSize(long max) {
		this();
		setSize(max);
	}

	/**
	 * 构造设置用户最大磁盘空间命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxSize(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最大磁盘空间命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxSize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户最大磁盘空间
	 * @param i 用户最大磁盘空间
	 */
	public void setSize(long i) {
		if (i < 0) {
			throw new IllegalValueException("illegal %d", i);
		}
		size = i;
	}

	/**
	 * 返回用户最大磁盘空间
	 * @return 用户最大磁盘空间
	 */
	public long getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxSize e = new SetSingleMaxSize(siger, size);
			array.add(e);
		}
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxSize duplicate() {
		return new SetMaxSize(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(size);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		size = reader.readLong();
	}
}