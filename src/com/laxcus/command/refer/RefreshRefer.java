/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 刷新资源引用，包括账号签名和ACCOUNT站点两个参数。<br><br>
 * 
 * 当GATE/BANK中的一个账号参数发生改变后，向TOP/HOME发出这个命令。<br><br>
 * 
 * 流程：<br>
 *  GATE(BANK) -> BANK -> TOP/HOME -> HOME从ACCOUNT提取资源引用，保存在本地 -> CALL (SET REFER命令）。<br>
 * 
 * 是单向处理命令，不需要反馈结果。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class RefreshRefer extends Command {

	private static final long serialVersionUID = -2384063737430951338L;

	/** 账号位置 **/
	private Seat seat;

	/**
	 * 构造默认的刷新资源引用命令
	 */
	private RefreshRefer() {
		super();
	}

	/**
	 * 构造刷新资源引用，指定ACCOUNT站点和签名
	 * @param local ACCOUNT站点地址
	 * @param object 账号签名
	 */
	public RefreshRefer(Seat seat) {
		this();
		setSeat(seat);
	}

	/**
	 * 构造刷新资源引用，指定ACCOUNT站点和签名
	 * @param local ACCOUNT站点地址
	 * @param siger 账号签名
	 */
	public RefreshRefer(Node local, Siger siger) {
		this(new Seat(siger, local));
	}

	/**
	 * 生成刷新资源引用的数据副本
	 * @param that 刷新资源引用
	 */
	private RefreshRefer(RefreshRefer that) {
		super(that);
		seat = that.seat;
	}

	/**
	 * 从可类化数据读取器中解析刷新资源引用
	 * @param reader 可类化数据读取器
	 */
	public RefreshRefer(ClassReader reader) {
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
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		if (seat != null) {
			return seat.toString();
		}
		return super.toString();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshRefer duplicate() {
		return new RefreshRefer(this);
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