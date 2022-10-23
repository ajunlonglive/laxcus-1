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
 * 删除在线成员通知WATCH节点
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class DropOnlineMember extends CastOnlineMember {

	private static final long serialVersionUID = -5825932773361126773L;

	/**
	 * 构造默认的删除在线成员通知WATCH节点
	 */
	public DropOnlineMember() {
		super();
	}

	/**
	 * 构造删除在线成员通知WATCH节点，指定用户基点
	 * @param seat 用户基点
	 */
	public DropOnlineMember(FrontSeat seat) {
		this();
		add(seat);
	}

	/**
	 * 从可类化读取器中解析删除在线成员通知WATCH节点
	 * @param reader 可类化读取器
	 */
	public DropOnlineMember(ClassReader reader) {
		super(reader);
	}

	/**
	 * 生成删除在线成员通知WATCH节点的副本
	 * @param that 删除在线成员通知WATCH节点
	 */
	private DropOnlineMember(CastOnlineMember that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropOnlineMember duplicate() {
		return new DropOnlineMember(this);
	}

}