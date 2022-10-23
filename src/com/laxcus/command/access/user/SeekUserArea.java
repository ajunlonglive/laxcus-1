/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.classable.*;

/**
 * 检索用户在集群的分布区域。<br>
 * 供WATCH节点使用，如果用户在TOP节点，检索范围包括BANK/HOME集群。如果在BAN/HOME集群，只检索其下节点。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2019
 * @since laxcus 1.0
 */
public class SeekUserArea extends MultiUser {

	private static final long serialVersionUID = -7858255332909279079L;

	/**
	 * 构造默认的检索用户在集群的分布区域
	 */
	public SeekUserArea() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户在集群的分布区域
	 * @param reader 可类化数据读取器
	 */
	public SeekUserArea(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成检索用户在集群的分布区域的数据副本
	 * @param that 原来样本
	 */
	private SeekUserArea(SeekUserArea that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekUserArea duplicate() {
		return new SeekUserArea(this);
	}

}