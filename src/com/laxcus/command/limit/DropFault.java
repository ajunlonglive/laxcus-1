/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import com.laxcus.util.classable.*;

/**
 * 撤销故障锁定命令。<br>
 * 
 * 此命令由FRONT注册用户发送，目标是GATE站点。GATE站点收到这个命令后，被锁定的数据资源将解除锁定。<br>
 * 
 * 本命令语句是：“DROP FAULT LOCK”。对应命令是“CreateFault”。
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public class DropFault extends PostFault {

	private static final long serialVersionUID = 53352453994199715L;

	/**
	 * 构造默认的撤销故障锁定命令
	 */
	public DropFault() {
		super();
	}

	/**
	 * 生成撤销故障锁定命令的数据副本
	 * @param that DropFault实例
	 */
	private DropFault(DropFault that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析撤销故障锁定命令
	 * @param reader 可类化数据读取器
	 */
	public DropFault(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropFault duplicate() {
		return new DropFault(this);
	}

}