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
 * 设置一个账号最大网关数目 <br>
 * 
 * 本处网关，即CALL节点，不是GATE/ENTRANCE。<br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/24/2018
 * @since laxcus 1.0
 */
public final class SetMaxGateways extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -5142583656221557880L;

	/** 用户最大网关数目 **/
	private int gateways;

	/**
	 * 根据传入的设置用户最大网关数目命令，生成它的数据副本
	 * @param that 设置用户最大网关数目命令
	 */
	private SetMaxGateways(SetMaxGateways that) {
		super(that);
		gateways = that.gateways;
	}

	/**
	 * 构造默认和私有的设置用户最大网关数目命令
	 */
	private SetMaxGateways() {
		super();
	}

	/**
	 * 构造设置用户最大网关数目，指定数目
	 * @param max 最大网关数目目
	 */
	public SetMaxGateways(int max) {
		this();
		setGateways(max);
	}

	/**
	 * 构造设置用户最大网关数目命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxGateways(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最大网关数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxGateways(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置用户最大网关数目
	 * @param i 用户最大网关数目
	 */
	public void setGateways(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		gateways = i;
	}

	/**
	 * 返回用户最大网关数目
	 * @return 用户最大网关数目
	 */
	public int getGateways() {
		return gateways;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxGateways duplicate() {
		return new SetMaxGateways(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(gateways);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		gateways = reader.readInt();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxGateways e = new SetSingleMaxGateways(siger, gateways);
			array.add(e);
		}
		return array;
	}
}