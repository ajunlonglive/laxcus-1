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
 * 设置用户的云存储空间尺寸。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2021
 * @since laxcus 1.0
 */
public class SetSingleCloudSize extends SetSingleUserParameter {

	private static final long serialVersionUID = -1853533110441652261L;

	/** 云存储空间尺寸 **/
	private long cloudSize;

	/**
	 * 根据传入的设置用户的云存储空间尺寸命令，生成它的数据副本
	 * @param that 设置用户的云存储空间尺寸命令
	 */
	private SetSingleCloudSize(SetSingleCloudSize that) {
		super(that);
		cloudSize = that.cloudSize;
	}

	/**
	 * 构造默认和私有的设置用户的云存储空间尺寸命令
	 */
	private SetSingleCloudSize() {
		super();
	}

	/**
	 * 构造设置用户的云存储空间尺寸，指定数目
	 * @param siger 用户签名
	 * @param max 最大并行任务数数目
	 */
	public SetSingleCloudSize(Siger siger, long max) {
		this();
		setSiger(siger);
		setCloudSize(max);
	}

	/**
	 * 从可类化数据读取器中解析设置用户的云存储空间尺寸命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleCloudSize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户的云存储空间尺寸数
	 * @param i 最大并行任务数数
	 */
	public void setCloudSize(long i) {
		cloudSize = i;
	}

	/**
	 * 返回最大并行任务数数
	 * @return 最大并行任务数数
	 */
	public long getCloudSize() {
		return cloudSize;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleCloudSize duplicate() {
		return new SetSingleCloudSize(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(cloudSize);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		cloudSize = reader.readLong();
	}

}