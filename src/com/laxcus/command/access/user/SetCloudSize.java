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
 * 设置云存储空间尺寸。<br>
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/26/2021
 * @since laxcus 1.0
 */
public final class SetCloudSize extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -8914717748619239419L;

	/** 云存储空间尺寸 **/
	private long cloudSize;

	/**
	 * 根据传入的设置设置云存储空间尺寸，生成它的数据副本
	 * @param that 设置设置云存储空间尺寸
	 */
	private SetCloudSize(SetCloudSize that) {
		super(that);
		cloudSize = that.cloudSize;
	}

	/**
	 * 构造默认和私有的设置设置云存储空间尺寸
	 */
	private SetCloudSize() {
		super();
	}

	/**
	 * 构造云存储空间尺寸
	 * @param max云存储空间尺寸
	 */
	public SetCloudSize(long max) {
		this();
		setCloudSize(max);
	}

	/**
	 * 构造设置设置云存储空间尺寸，指定用户签名
	 * @param siger 用户签名
	 */
	public SetCloudSize(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置设置云存储空间尺寸
	 * @param reader 可类化数据读取器
	 */
	public SetCloudSize(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置设置云存储空间尺寸
	 * @param i 设置云存储空间尺寸
	 */
	public void setCloudSize(long i) {
		cloudSize = i;
	}

	/**
	 * 返回设置云存储空间尺寸
	 * @return 设置云存储空间尺寸
	 */
	public long getCloudSize() {
		return cloudSize;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleCloudSize e = new SetSingleCloudSize(siger, cloudSize);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetCloudSize duplicate() {
		return new SetCloudSize(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(cloudSize);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		cloudSize = reader.readLong();
	}
}