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
 * 设置单表最大索引数目。 <br>
 * 
 * 索引数目太多，尤其是行存储模式（NSM），会占用太多磁盘空间。限制索引数，就是减少磁盘空间占用。
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/24/2018
 * @since laxcus 1.0
 */
public final class SetMaxIndexes extends SetMultiUserParameter {
	
	private static final long serialVersionUID = 8950120487168391289L;

	/** 用户最大索引数目 **/
	private int indexes;

	/**
	 * 根据传入的设置用户最大索引数目命令，生成它的数据副本
	 * @param that 设置用户最大索引数目命令
	 */
	private SetMaxIndexes(SetMaxIndexes that) {
		super(that);
		indexes = that.indexes;
	}

	/**
	 * 构造默认和私有的设置用户最大索引数目命令
	 */
	private SetMaxIndexes() {
		super();
	}

	/**
	 * 构造设置用户最大索引数目，指定数目
	 * @param max 最大索引数目目
	 */
	public SetMaxIndexes(int max) {
		this();
		setIndexes(max);
	}

	/**
	 * 构造设置用户最大索引数目命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxIndexes(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最大索引数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxIndexes(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置FRONT站点最大索引数目
	 * @param i FRONT站点最大索引数目
	 */
	public void setIndexes(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		indexes = i;
	}

	/**
	 * 返回FRONT站点最大索引数目
	 * @return FRONT站点最大索引数目
	 */
	public int getIndexes() {
		return indexes;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxIndexes e = new SetSingleMaxIndexes(siger, indexes);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxIndexes duplicate() {
		return new SetMaxIndexes(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(indexes);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		indexes = reader.readInt();
	}
}