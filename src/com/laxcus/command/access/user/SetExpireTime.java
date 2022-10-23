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
 * 设置账号到期时间。<br>
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 
 * @author scott.liang
 * @version 1.0 01/04/2020
 * @since laxcus 1.0
 */
public final class SetExpireTime extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -1427503101375972685L;
	
	/** 到期时间 **/
	private long time;

	/**
	 * 根据传入的设置设置账号到期时间，生成它的数据副本
	 * @param that 设置设置账号到期时间
	 */
	private SetExpireTime(SetExpireTime that) {
		super(that);
		time = that.time;
	}

	/**
	 * 构造默认和私有的设置设置账号到期时间
	 */
	private SetExpireTime() {
		super();
	}

	/**
	 * 构造设置到期时间，指定时间
	 * @param time 到期时间
	 */
	public SetExpireTime(long time) {
		this();
		setTime(time);
	}

	/**
	 * 构造设置设置账号到期时间，指定用户签名
	 * @param siger 用户签名
	 */
	public SetExpireTime(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置设置账号到期时间
	 * @param reader 可类化数据读取器
	 */
	public SetExpireTime(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置设置账号到期时间。
	 * 参数是LAXCUS日期时间。
	 * 
	 * @param i 设置账号到期时间
	 */
	public void setTime(long i) {
		time = i;
	}

	/**
	 * 返回设置账号到期时间
	 * @return 设置账号到期时间
	 */
	public long getTime() {
		return time;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleExpireTime e = new SetSingleExpireTime(siger, time);
			array.add(e);
		}
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetExpireTime duplicate() {
		return new SetExpireTime(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(time);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		time = reader.readLong();
	}
}