/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.util;

import java.io.*;

import com.laxcus.command.rule.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 事务标记 <br>
 * 
 * 由服务器地址和事务命令两部分组成，是向目标地址发送命令。
 * 
 * @author scott.liang
 * @version 1.1 8/24/2015
 * @since laxcus 1.0
 */
public final class RuleToken implements Classable, Serializable, Cloneable, Comparable<RuleToken> {

	private static final long serialVersionUID = 4303689263327881463L;

	/** 服务器地址 **/
	private Node hub;

	/** 发送的命令 **/
	private AttachRule command;

	/**
	 * 构造默认和私有的事务标记
	 */
	private RuleToken() {
		super();
	}

	/**
	 * 根据实例，生成事务标记的数据副本
	 * @param that
	 */
	private RuleToken(RuleToken that) {
		this();
		hub = that.hub;
		command = that.command;
	}

	/**
	 * 构造事务标记，指定参数
	 * @param hub 服务器地址
	 * @param cmd 命令
	 */
	public RuleToken(Node hub, AttachRule cmd) {
		this();
		setHub(hub);
		setCommand(cmd);
	}

	/**
	 * 从可类化数据读取器中解析事务标记参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RuleToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置服务器地址
	 * @param e
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回服务器地址
	 * @return
	 */
	public Node getHub() {
		return hub;
	}

	/**
	 * 设置命令
	 * @param e
	 */
	public void setCommand(AttachRule e) {
		Laxkit.nullabled(e);

		command = e;
	}

	/**
	 * 返回命令
	 * @return
	 */
	public AttachRule getCommand() {
		return command;
	}
	
	/**
	 * 返回命令处理标识
	 * @return ProcessRuleTag
	 */
	public ProcessRuleTag getCommandTag() {
		return command.getTag();
	}

	/**
	 * 返回当前事务标记实例的浅层副本
	 * @return
	 */
	public RuleToken duplicate() {
		return new RuleToken(this);
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != RuleToken.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((RuleToken) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hub.hashCode() ^ command.hashCode();
	}

	/**
	 * 根据当前实例克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回事务标记的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", hub, command);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RuleToken that) {
		if(that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(hub, that.hub);
		if (ret == 0) {
			ret = Laxkit.compareTo(command, that.command);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(hub);
		writer.writeDefault(command);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		hub = new Node(reader);
		command = (AttachRule) reader.readDefault();
		return reader.getSeek() - seek;
	}

}