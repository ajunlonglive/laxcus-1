/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 实时会话标识。<br>
 * 由用户签名、来源站点、CALL站点调用器地址数字签名组成
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public final class TalkFalg implements Classable, Serializable, Cloneable, Comparable<TalkFalg> {

	private static final long serialVersionUID = -4656660451034963525L;

	/** 发起人签名 **/
	private Siger issuer;

	/** 来源站点 **/
	private Node from;

	/** 数字指纹。CALL站点调用器回显地址的数字签名，做为判断分布任务组件同一来源的标记 **/
	private SHA256Hash master;

	/**
	 * 将实时会话标识配置参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 发起人签名
		writer.writeObject(issuer);
		// 来源地址
		writer.writeObject(from);
		// CALL站点调用器地址
		writer.writeObject(master);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析实时会话标识配置参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 发起人签名
		issuer = new Siger(reader);
		// 来源地址
		from = new Node(reader);
		// CALL站点调用器地址
		master = new SHA256Hash(reader);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个私有的实时会话标识配置
	 */
	private TalkFalg() {
		super();
	}

	/**
	 * 根据传入的实时会话标识，生成它的数据副本
	 * @param that TalkTag实例
	 */
	private TalkFalg(TalkFalg that) {
		this();
		issuer = that.issuer;
		from = that.from;
		master = that.master;
	}

	/**
	 * 构造实时会话标识，指定发起人签名和CALL站点调用器地址
	 * @param issuer 发起人签名
	 * @param from 来源地址
	 * @param master CALL站点调用器地址的数字签名
	 */
	public TalkFalg(Siger issuer, Node from, SHA256Hash master) {
		this();
		setIssuer(issuer);
		setFrom(from);
		setMaster(master);
	}

	/**
	 * 从可类化数据读取器中解析实时会话标识配置参数
	 * @param reader 可类化数据读取器
	 */
	public TalkFalg(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析实时会话标识参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public TalkFalg(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 设置发起人签名
	 * @param e 发起人签名
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);
		issuer = e;
	}

	/**
	 * 返回发起人签名
	 * @return 发起人签名
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置来源站点
	 * @param e
	 */
	public void setFrom(Node e) {
		Laxkit.nullabled(e);
		from = e;
	}

	/**
	 * 返回来源站点
	 * @return Node实例
	 */
	public Node getFrom() {
		return from;
	}

	/**
	 * 设置数字指纹。CALL站点调用器地址，是判断的分布任务组件同一来源的标记。
	 * @param e SHA256Hash 实例
	 */
	public void setMaster(SHA256Hash e) {
		Laxkit.nullabled(e);
		master = e;
	}

	/**
	 * 返回数字指纹。CALL站点调用器地址，是判断的分布任务组件同一来源的标记
	 * @return SHA256Hash 实例
	 */
	public SHA256Hash getMaster() {
		return master;
	}

	/**
	 * 建立一个当前实时会话标识的数据副本
	 * @return TalkTag实例
	 */
	public TalkFalg duplicate() {
		return new TalkFalg(this);
	}

	/**
	 * 比较两个实时会话标识一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TalkFalg.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TalkFalg) that) == 0;
	}

	/**
	 * 返回实时会话标识的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return issuer.hashCode() ^ from.hashCode() ^ master.hashCode();
	}

	/**
	 * 返回实时会话标识的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{%s}/%s/%s", issuer, from, master);
	}

	/**
	 * 根据当前实时会话标识配置，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个实时会话标识的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TalkFalg that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较发起人签名、来源地址、CALL站点调用器地址
		int ret = issuer.compareTo(that.issuer);
		if (ret == 0) {
			ret = from.compareTo(that.from);
		}
		if (ret == 0) {
			ret = master.compareTo(that.master);
		}
		return ret;
	}

	/**
	 * 实时会话标识生成数据流输出
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析实时会话标识，返回解析的长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}