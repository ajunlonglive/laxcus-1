/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.site.*;

/**
 * 转发推送在线用户命令
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class ShiftPushOnlineMember extends ShiftCommand {

	private static final long serialVersionUID = 5031095866970199545L;

	/** 用户签名 */
	private Siger siger;
	
	/** FRONT节点 **/
	private Node front;
	
	/**
	 * 构造默认的转发推送在线用户命令
	 */
	public ShiftPushOnlineMember() {
		super();
	}

	/**
	 * 构造转发推送在线用户命令，指定用户签名
	 * @param siger 用户签名
	 */
	public ShiftPushOnlineMember(Siger siger, Node front) {
		super();
		setSiger(siger);
		this.setFront(front);
	}

	/**
	 * 生成转发推送在线用户命令副本
	 * @param that 转发推送在线用户命令
	 */
	public ShiftPushOnlineMember(ShiftPushOnlineMember that) {
		super(that);
		siger = that.siger;
		this.front = that.front;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPushOnlineMember duplicate() {
		return new ShiftPushOnlineMember(this);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 设置FRONT站点地址
	 * @param e Node实例
	 */
	public void setFront(Node e) {
		Laxkit.nullabled(e);

		front = e;
	}

	/**
	 * 返回FRONT站点地址
	 * @return Node实例
	 */
	public Node getFront() {
		return front;
	}
}