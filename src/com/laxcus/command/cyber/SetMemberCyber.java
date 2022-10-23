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
 * 在WATCH节点管理员操作，分发给ACCOUNT/GATE/CALL/DATA/WORK/CALL节点。<br>
 * 
 * 语法格式：SET MEMBER CYBER 成员数 阀值 [TO ALL|site, ...]<br>
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public final class SetMemberCyber extends SetVirtualCyber {

	private static final long serialVersionUID = -3571424142939516395L;

	/**
	 * 根据传入的成员虚拟空间参数，生成它的数据副本
	 * @param that SetMemberCyber实例
	 */
	private SetMemberCyber(SetMemberCyber that) {
		super(that);	
	}

	/**
	 * 构造默认的成员虚拟空间参数。
	 */
	public SetMemberCyber() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public SetMemberCyber(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMemberCyber duplicate() {
		return new SetMemberCyber(this);
	}

}