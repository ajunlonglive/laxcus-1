/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.classable.*;

/**
 * 提交故障锁定命令。<br>
 * 
 * 此命令由FRONT注册用户发送，目标是GATE站点。发送此命令的原因是分布处理过程中出现“写”错误，如INSERT、DELETE、UPDATE错误。<br>
 * 命令被GATE接受后，根据处理级别，相关的数据资源将被锁定，直到用户检查资源正确后，才能解除锁定。<br>
 * 
 * 本命令语句是：“CREATE FAULT LOCK”。对应命令是“DropFault”。
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public class CreateFault extends PostFault {

	private static final long serialVersionUID = 5324256601283359132L;

	/**
	 * 构造默认的提交故障锁定命令
	 */
	public CreateFault() {
		super();
	}

	/**
	 * 构造提交故障锁定命令，指定一个锁定单元
	 * @param item - 锁定单元
	 */
	public CreateFault(FaultItem item) {
		this();
		add(item);
	}

	/**
	 * 构造提交故障锁定命令，指定一批锁定单元
	 * @param array - 锁定单元数组
	 */
	public CreateFault(Collection<FaultItem> array) {
		this();
		addAll(array);
	}
	
	/**
	 * 生成提交故障锁定命令的数据副本
	 * @param that
	 */
	private CreateFault(CreateFault that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析提交故障锁定命令
	 * @param reader - 可类化数据读取器
	 */
	public CreateFault(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Command duplicate() {
		return new CreateFault(this);
	}

}