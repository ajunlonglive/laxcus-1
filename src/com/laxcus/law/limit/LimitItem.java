/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import java.io.*;

import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 限制操作单元。<br><br>
 * 
 * 限制操作发生在分布数据的“写操作”失败后，为保证分布数据的一致性和完整性，避免错误扩大化，
 * 系统根据用户预先定义的限制操作要求，将集群中的故障区域暂时锁定起来，直到数据修复后解决锁定。<br><br>
 * 
 * “分布写”故障通常是硬件的损坏或者失效，如计算机、硬盘、网络故障。<br>
 * 修复工作分为两步，首先由集群管理员将网络和计算机恢复，
 * 然后用户操作数据一致性检查和数据一致性恢复指令（CHECK ONLY、RESTORE ONLY），将数据恢复。
 * 数据恢复后，通知系统解除对故障区域的锁定。<br><br>
 * 
 * 限制操作级别与事务规则级别一致，从高到低三个级别：<1> 用户级 <2> 数据库级 <3> 表级。上一级拥有下一级的全部资源。<br>
 * 限制操作符有“READ、WRITE”两种。“READ”生效后，表示相关读操作被拒绝。“WRITE”生效后，表示相关写操作被拒绝。<br><br>
 * 
 * 按照CAP理论，发生故障并启动限制后，因为一部分数据业务在申请时将被拒绝，系统的可用性（A）有所降低。<br>
 * 这个操作是可选项，由用户设置。此操作主要是满足不同用户的业务需求。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public abstract class LimitItem implements Classable, Markable, Serializable, Cloneable, Comparable<LimitItem> {

	private static final long serialVersionUID = 7028530688029668579L;

	/** 限制操作标识 **/
	private LimitFlag flag;

	/**
	 * 构造默认的限制操作单元
	 */
	protected LimitItem() {
		super();
	}

	/**
	 * 构造限制操作单元，指定限制操作级别和限制操作符
	 * @param rank 限制操作级别
	 * @param operator 限制操作符
	 */
	protected LimitItem(byte rank, byte operator) {
		this();
		flag = new LimitFlag(rank, operator);
	}

	/**
	 * 根据传入的限制操作单元，生成它的数据副本
	 * @param that LimitItem实例
	 */
	protected LimitItem(LimitItem that) {
		this();
		flag = that.flag.duplicate();
	}

	/**
	 * 返回限制操作单元标识
	 * @return LimitFlag实例
	 */
	public LimitFlag getFlag() {
		return flag;
	}

	/**
	 * 返回限制操作符
	 * @return 限制操作符
	 */
	public byte getOperator() {
		return flag.getOperator();
	}

	/**
	 * 返回限制操作符的文本描述
	 * @return 限制操作符的文本描述
	 */
	public String getOperatorText() {
		return flag.getOperatorText();
	}

	/**
	 * 返回限制操作级别
	 * @return 限制操作级别
	 */
	public byte getRank() {
		return flag.getRank();
	}

	/**
	 * 返回限制操作级别的文本描述
	 * @return 限制操作级别的文本描述
	 */
	public String getRankText() {
		return flag.getRankText();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		// 比较两个限制操作规则参数完成一致
		return compareTo((LimitItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return flag.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LimitItem that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(flag, that.flag);
	}
	
	/**
	 * 将站点参数写入可类化数据存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 限制操作标识
		writer.writeObject(flag);
		// 写入子类数据
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析站点参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 限制操作标识
		flag = new LimitFlag(reader);
		// 读子类数据
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 将当前限制操作规则生成一个新的数据副本
	 * @return LimitItem实例
	 */
	public abstract LimitItem duplicate();
	
	/**
	 * 判断传入的限制操作单是当前对象的子集。<br>
	 * 即用户级囊括用户级、数据库级、表级、行级。数据库级囊括数据库级、表级、行级
	 * 表级囊括表级、行级。行级囊括行级。
	 * @param item 被比较的对象
	 * @return 返回真或者假
	 */
	public abstract boolean embrace(LimitItem item);
	
	/**
	 * 判断当前的限制操作单元和传入的锁定单元是匹配的。<br>
	 * 特别说明：在进行比较前，已经默认用户签名是一致的。
	 * 
	 * @param item 锁定单元
	 * @return 返回真或者假
	 */
	public abstract boolean match(FaultItem item);

	/**
	 * 当前限制操作规则与传入的事务规则进行比较，判断它们存在冲突。<br>
	 * 冲突表示事务操作不能执行，必须等冲突消除后才可以执行。
	 * 
	 * @param rule 被比较的事务操作规则
	 * @return 存在冲突返回“真”，否则“假”。
	 */
	public abstract boolean conflict(RuleItem rule);

	/**
	 * 将子类数据写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中读取子类数据
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}