/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import com.laxcus.util.classable.*;

/**
 * 成员虚拟空间参数。<br><br>
 * 
 * 在WATCH节点管理员操作，分发给 GATE/CALL节点。
 * 
 * 语法格式：SET FRONT CYBER 成员数 阀值 [TO ALL|site, ...]<br>
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public final class SetFrontCyber extends SetVirtualCyber {

	private static final long serialVersionUID = -736211200575041483L;

	/**
	 * 根据传入的成员虚拟空间参数，生成它的数据副本
	 * @param that SetFrontCyber实例
	 */
	private SetFrontCyber(SetFrontCyber that) {
		super(that);	
	}

	/**
	 * 构造默认的成员虚拟空间参数。
	 */
	public SetFrontCyber() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public SetFrontCyber(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetFrontCyber duplicate() {
		return new SetFrontCyber(this);
	}

}