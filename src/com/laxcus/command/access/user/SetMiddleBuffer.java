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
 * 设置中间缓存尺寸。<br>
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 01/10/2021
 * @since laxcus 1.0
 */
public final class SetMiddleBuffer extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -8914717748619239419L;

	/** 中间缓存尺寸 **/
	private long middleBuffer;

	/**
	 * 根据传入的设置设置中间缓存尺寸，生成它的数据副本
	 * @param that 设置设置中间缓存尺寸
	 */
	private SetMiddleBuffer(SetMiddleBuffer that) {
		super(that);
		middleBuffer = that.middleBuffer;
	}

	/**
	 * 构造默认和私有的设置设置中间缓存尺寸
	 */
	private SetMiddleBuffer() {
		super();
	}

	/**
	 * 构造中间缓存尺寸
	 * @param max中间缓存尺寸
	 */
	public SetMiddleBuffer(long max) {
		this();
		setMiddleBuffer(max);
	}

	/**
	 * 构造设置设置中间缓存尺寸，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMiddleBuffer(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置设置中间缓存尺寸
	 * @param reader 可类化数据读取器
	 */
	public SetMiddleBuffer(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置设置中间缓存尺寸
	 * @param i 设置中间缓存尺寸
	 */
	public void setMiddleBuffer(long i) {
		middleBuffer = i;
	}

	/**
	 * 返回设置中间缓存尺寸
	 * @return 设置中间缓存尺寸
	 */
	public long getMiddleBuffer() {
		return middleBuffer;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMiddleBuffer e = new SetSingleMiddleBuffer(siger, middleBuffer);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMiddleBuffer duplicate() {
		return new SetMiddleBuffer(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(middleBuffer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		middleBuffer = reader.readLong();
	}
}