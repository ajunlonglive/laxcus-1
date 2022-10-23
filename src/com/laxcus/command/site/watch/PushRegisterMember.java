/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 推送注册成员到WATCH节点
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class PushRegisterMember extends CastRegisterMember {

	private static final long serialVersionUID = -5564270056770676008L;

	/**
	 * 构造默认的推送注册成员到WATCH节点
	 */
	public PushRegisterMember() {
		super();
	}

	/**
	 * 构造推送注册成员到WATCH节点，指定用户基点
	 * @param seat 用户基点
	 */
	public PushRegisterMember(Seat seat) {
		this();
		add(seat);
	}
	
	/**
	 * 从可类化读取器中解析推送注册成员到WATCH节点
	 * @param reader 可类化读取器
	 */
	public PushRegisterMember(ClassReader reader) {
		super(reader);
	}

	/**
	 * 生成推送注册成员到WATCH节点的副本
	 * @param that 推送注册成员到WATCH节点
	 */
	private PushRegisterMember(CastRegisterMember that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushRegisterMember duplicate() {
		return new PushRegisterMember(this);
	}

}