/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import java.util.*;

import com.laxcus.distribute.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 分布式命令。<br><br>
 * 
 * 提供分布式处理必须的基本参数，它的子类包括：<br>
 * 1. 大规模数据计算命令：CONDUCT <br>
 * 2. 数据构建命令：ESTABLISH <br>
 * 3. 小规模迭代计算命令：CONTACT <br>
 * 
 * @author scott.liang
 * @version 1.1 3/12/2015
 * @since laxcus 1.0
 */
public abstract class DistributedCommand extends RuleCommand {

	private static final long serialVersionUID = 2332072612782986056L;

	/** 基础字 **/
	private Sock sock;
	
	/** 下次命令的基础字，形成连续的链接计算！由GuideTask调用时设置 **/
	private Sock nextSock;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(sock);
		writer.writeInstance(nextSock);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		sock = reader.readInstance(Sock.class);
		nextSock = reader.readInstance(Sock.class);
	}

	/**
	 * 构造一个默认的分布式命令
	 */
	protected DistributedCommand() {
		super();
	}

	/**
	 * 根据传入分布式命令实例，生成它的数据副本
	 * @param that 分布式命令实例
	 */
	protected DistributedCommand(DistributedCommand that) {
		super(that);
		if (that.sock != null) {
			sock = that.sock.duplicate();
		}
		if (that.nextSock != null) {
			nextSock = that.nextSock.duplicate();
		}
	}

	/**
	 * 设置基础字
	 * @param e 基础字
	 */
	public void setSock(Sock e) {
		sock = e;
	}

	/**
	 * 返回基础字
	 * @return 基础字对象
	 */
	public Sock getSock() {
		return sock;
	}

	/**
	 * 返回基础字文本
	 * @return 基础字字符串
	 */
	public String getRootText() {
		return (sock != null ? sock.toString() : null);
	}

	/**
	 * 设置下个命令的基础字
	 * @param e 基础字
	 */
	public void setNextSock(Sock e) {
		nextSock = e;
	}

	/**
	 * 返回下个命令的基础字
	 * @return 基础字对象
	 */
	public Sock getNextSock() {
		return nextSock;
	}
	
	/**
	 * 收集分布式对象的事务处理规则到集合
	 * @param all 分布式对象成员数组
	 */
	protected List<RuleItem> collect(DistributedObject[] all) {
		TreeSet<RuleItem> set = new TreeSet<RuleItem>();
		// 过滤重复的事务规则，保证每个规则都是唯一的
		for (DistributedObject e : all) {
			if (e == null) {
				continue;
			}
			// 输出事务规则和保存它们
			set.addAll(e.getRules());
		}

		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
		// 返回事务数组
		if (set.size() > 0) {
			array.addAll(set);
		} else {
			// 没有定义，生成用户级的独享事务
			UserRuleItem rule = new UserRuleItem(RuleOperator.EXCLUSIVE_WRITE);
			array.add(rule);
		}

		return array;
	}

}