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
 * 设置一个账号最大在线用户数目 <br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/07/2017
 * @since laxcus 1.0
 */
public final class SetMaxMembers extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -2245427709142537594L;

	/** 用户最大连接数 **/
	private int members;

	/**
	 * 根据传入的设置用户最大连接数命令，生成它的数据副本
	 * @param that 设置用户最大连接数命令
	 */
	private SetMaxMembers(SetMaxMembers that) {
		super(that);
		members = that.members;
	}

	/**
	 * 构造默认和私有的设置用户最大连接数命令
	 */
	private SetMaxMembers() {
		super();
	}

	/**
	 * 构造设置用户最大连接数，指定数目
	 * @param max 最大连接数目
	 */
	public SetMaxMembers(int max) {
		this();
		setMembers(max);
	}

	/**
	 * 构造设置用户最大连接数命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxMembers(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最大连接数命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxMembers(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置FRONT站点最大登录数目
	 * @param i FRONT站点最大登录数目
	 */
	public void setMembers(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		members = i;
	}

	/**
	 * 返回FRONT站点最大登录数目
	 * @return FRONT站点最大登录数目
	 */
	public int getMembers() {
		return members;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxMembers e = new SetSingleMaxMembers(siger, members);
			array.add(e);
		}
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxMembers duplicate() {
		return new SetMaxMembers(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(members);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		members = reader.readInt();
	}
}