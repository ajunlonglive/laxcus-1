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
 * 检索用户在线注册的元数据。<br><br>
 * 
 * 由WATCH节点发出，通过TOP/HOME，转发给CALL/DATA/WORK/BUILD节点，收集和反馈给WATCH节点。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class SeekRegisterMetadata extends MultiUser {

	private static final long serialVersionUID = -1052744024422153701L;

	/**
	 * 构造默认的检索用户在线注册的元数据
	 */
	public SeekRegisterMetadata() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户在线注册的元数据
	 * @param reader 可类化数据读取器
	 */
	public SeekRegisterMetadata(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检索用户在线注册的元数据的数据副本
	 * @param that 检索用户在线注册的元数据
	 */
	private SeekRegisterMetadata(SeekRegisterMetadata that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekRegisterMetadata duplicate() {
		return new SeekRegisterMetadata(this);
	}

}