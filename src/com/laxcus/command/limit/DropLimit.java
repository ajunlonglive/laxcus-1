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
 * 删除限制操作命令。<br>
 * 
 * 释放由“CreateLimit”命令产生的限制操作单元。<br>
 * 命令从FRONT站点发出，目标是TOP站点，并通知GATE站点。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public class DropLimit extends PostLimit {

	private static final long serialVersionUID = -8941378587051788881L;

	/**
	 * 构造默认的删除限制操作命令
	 */
	public DropLimit() {
		super();
	}

	/**
	 * 生成删除限制操作命令的数据副本
	 * @param that DropLimit实例
	 */
	private DropLimit(DropLimit that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析删除限制操作命令
	 * @param reader 可类化数据读取器
	 */
	public DropLimit(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropLimit duplicate() {
		return new DropLimit(this);
	}

}