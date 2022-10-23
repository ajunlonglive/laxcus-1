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
 * 删除注册成员后通知WATCH节点
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class DropRegisterMember extends CastRegisterMember {

	private static final long serialVersionUID = 2657848910793366949L;

	/**
	 * 构造默认的删除注册成员后通知WATCH节点
	 */
	public DropRegisterMember() {
		super();
	}

	/**
	 * 构造删除注册成员后通知WATCH节点，指定用户基点
	 * @param seat 用户基点
	 */
	public DropRegisterMember(Seat seat) {
		this();
		add(seat);
	}
	
	/**
	 * 从可类化读取器中解析删除注册成员后通知WATCH节点
	 * @param reader 可类化读取器
	 */
	public DropRegisterMember(ClassReader reader) {
		super(reader);
	}

	/**
	 * 生成删除注册成员后通知WATCH节点的副本
	 * @param that 删除注册成员后通知WATCH节点
	 */
	private DropRegisterMember(CastRegisterMember that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropRegisterMember duplicate() {
		return new DropRegisterMember(this);
	}

}