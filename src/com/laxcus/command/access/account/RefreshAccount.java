/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 刷新账号，包括账号签名和ACCOUNT站点两个参数。<br><br>
 * 
 * 发生条件：<br>
 * 1. BANK成功建立一个账号时，发送这个命令给HASH站点。<br>
 * 2. ACCOUNT站点上账号的密码/数据库/数据表/其它资源发生修改、增加、删除时，GATE通过BANK通知相关的HASH/GATE站点。<br><br>
 * 
 * 接收方处理：<br>
 * BANK站点：命令可能是自己/ACCOUNT/GATE站点。<br>
 * HASH站点：判断签名与自己范围一致时保存，否则忽略。<br>
 * GATE站点：当前已经记录这个账号时，刷新，否则忽略。<br><br>
 * 
 * 
 * 流程：<br>
 * ACCOUNT/GATE/BANK -> BANK -> HASH/GATE。<br>
 *  <br>
 * 
 * 
 * 这是命令只在BANK子域集群中广播，单向处理，不需要反馈结果。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class RefreshAccount extends Command {

	private static final long serialVersionUID = -2384063737430951338L;

	/** 账号位置 **/
	private Seat seat;

	/**
	 * 构造默认的刷新账号命令
	 */
	private RefreshAccount() {
		super();
	}

	/**
	 * 构造刷新账号，指定ACCOUNT站点和签名
	 * @param local ACCOUNT站点地址
	 * @param object 账号签名
	 */
	public RefreshAccount(Seat seat) {
		this();
		setSeat(seat);
	}

	/**
	 * 构造刷新账号，指定ACCOUNT站点和签名
	 * @param local ACCOUNT站点地址
	 * @param siger 账号签名
	 */
	public RefreshAccount(Node local, Siger siger) {
		this(new Seat(siger, local));
	}

	/**
	 * 生成刷新账号的数据副本
	 * @param that 刷新账号
	 */
	private RefreshAccount(RefreshAccount that) {
		super(that);
		seat = that.seat;
	}

	/**
	 * 从可类化数据读取器中解析刷新账号
	 * @param reader 可类化数据读取器
	 */
	public RefreshAccount(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号位置，不允许空指针
	 * @param e Seat实例
	 */
	public void setSeat(Seat e) {
		Laxkit.nullabled(e);
		seat = e;
	}

	/**
	 * 返回账号位置
	 * @return Seat实例
	 */
	public Seat getSeat() {
		return seat;
	}

	/**
	 * 返回账号签名
	 * @return 账号签名
	 */
	public Siger getSiger() {
		return seat.getSiger();
	}

	/**
	 * 返回账号站点地址
	 * @return Node实例
	 */
	public Node getLocal() {
		return seat.getSite();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshAccount duplicate() {
		return new RefreshAccount(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(seat);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		seat = new Seat(reader);
	}

}