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
 * 设置账号到期时间。
 * 
 * @author scott.liang
 * @version 1.0 01/04/2020
 * @since laxcus 1.0
 */
public class SetSingleExpireTime extends SetSingleUserParameter {


	private static final long serialVersionUID = 4281626777537533125L;

	/** 账号到期时间 **/
	private long time;

	/**
	 * 根据传入的设置账号到期时间命令，生成它的数据副本
	 * @param that 设置账号到期时间命令
	 */
	private SetSingleExpireTime(SetSingleExpireTime that) {
		super(that);
		time = that.time;
	}

	/**
	 * 构造默认和私有的设置账号到期时间命令
	 */
	private SetSingleExpireTime() {
		super();
	}

	/**
	 * 构造设置账号到期时间，指定数目
	 * @param siger 用户签名
	 * @param time 账号到期时间目
	 */
	public SetSingleExpireTime(Siger siger, long time) {
		this();
		setSiger(siger);
		setTime(time);
	}

	/**
	 * 从可类化数据读取器中解析设置账号到期时间命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleExpireTime(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号到期时间数
	 * @param i 账号到期时间
	 */
	public void setTime(long i) {
		time = i;
	}

	/**
	 * 返回账号到期时间
	 * @return 账号到期时间
	 */
	public long getTime() {
		return time;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleExpireTime duplicate() {
		return new SetSingleExpireTime(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(time);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		time = reader.readLong();
	}

}