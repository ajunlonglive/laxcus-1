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
 * 推送在线成员到WATCH节点
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class PushOnlineMember extends CastOnlineMember {

	private static final long serialVersionUID = 5473435860903892039L;

	/**
	 * 构造默认的推送在线成员到WATCH节点
	 */
	public PushOnlineMember() {
		super();
	}

	/**
	 * 构造推送在线成员通知WATCH节点，指定FRONT用户基点
	 * @param seat FRONT用户基点
	 */
	public PushOnlineMember(FrontSeat seat) {
		this();
		add(seat);
	}

	/**
	 * 从可类化读取器中解析推送在线成员到WATCH节点
	 * @param reader 可类化读取器
	 */
	public PushOnlineMember(ClassReader reader) {
		super(reader);
	}

	/**
	 * 生成推送在线成员到WATCH节点的副本
	 * @param that 推送在线成员到WATCH节点
	 */
	private PushOnlineMember(CastOnlineMember that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushOnlineMember duplicate() {
		return new PushOnlineMember(this);
	}

}