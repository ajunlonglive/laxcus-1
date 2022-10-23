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
 * 设置多个账号最大定时优化表数目 <br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/10/2018
 * @since laxcus 1.0
 */
public final class SetMaxRegulates extends SetMultiUserParameter {
	
	private static final long serialVersionUID = 5729408977129818792L;

	/** 用户最大定时优化表数 **/
	private int regulates;

	/**
	 * 根据传入的设置用户最大定时优化表数命令，生成它的数据副本
	 * @param that 设置用户最大定时优化表数命令
	 */
	private SetMaxRegulates(SetMaxRegulates that) {
		super(that);
		regulates = that.regulates;
	}

	/**
	 * 构造默认和私有的设置用户最大定时优化表数命令
	 */
	private SetMaxRegulates() {
		super();
	}

	/**
	 * 构造设置用户最大定时优化表数，指定数目
	 * @param max 最大定时优化表数目
	 */
	public SetMaxRegulates(int max) {
		this();
		setRegulates(max);
	}

	/**
	 * 构造设置用户最大定时优化表数命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxRegulates(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最大定时优化表数命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxRegulates(ClassReader reader) {
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
	public SetMaxRegulates duplicate() {
		return new SetMaxRegulates(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(regulates);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		regulates = reader.readInt();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxRegulates e = new SetSingleMaxRegulates(siger, regulates);
			array.add(e);
		}
		return array;
	}
}