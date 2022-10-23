/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 事务处理标识<br><br>
 * 
 * 事务处理标识由调用器本地站点和回显标识组成，做为发送命令的客户机参数，传递到服务器，保存在服务器上，做为每个命令的唯一符号。
 * 
 * @author scott.liang
 * @version 1.1 10/06/2016
 * @since laxcus 1.0
 */
public final class ProcessRuleTag implements Classable, Serializable, Cloneable, Comparable<ProcessRuleTag> {

	private static final long serialVersionUID = 3652565291139014402L;

	/** 调用器地址 **/
	private Node local;

	/** 回显标识 **/
	private EchoFlag flag;

	/**
	 * 将事务处理标识配置参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 调用器地址
		writer.writeObject(local);
		// 回显标识
		writer.writeObject(flag);		
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析事务处理标识配置参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 调用器地址
		local = new Node(reader);
		// 回显标识
		flag = new EchoFlag(reader);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个私有的事务处理标识配置
	 */
	private ProcessRuleTag() {
		super();
	}

	/**
	 * 根据传入的事务处理标识，生成它的数据副本
	 * @param that ProcessRuleTag实例
	 */
	private ProcessRuleTag(ProcessRuleTag that) {
		this();
		local = that.local.duplicate();
		flag = that.flag.duplicate();
	}

	/**
	 * 构造事务处理标识，指定调用器地址和回显标识
	 * @param local 调用器地址（FIXP地址）
	 * @param flag 回显标识
	 */
	public ProcessRuleTag(Node local, EchoFlag flag) {
		this();
		setLocal(local);
		setFlag(flag);
	}

	/**
	 * 构造一个事务处理标识，指定它的全部参数
	 * @param local 调用器地址（FIXP地址）
	 * @param invokerId 调用器编号
	 * @param index 子任务在集合中的下标
	 */
	public ProcessRuleTag(Node local, long invokerId, int index) {
		this(local, new EchoFlag(invokerId, index));
	}

	/**
	 * 构造一个事务处理标识，指定它的全部参数
	 * @param family 站点属性
	 * @param host 站点监听地址
	 * @param flag 回显标识
	 */
	public ProcessRuleTag(byte family, SiteHost host, EchoFlag flag) {
		this(new Node(family, host), flag);
	}

	/**
	 * 构造一个事务处理标识，指定它的全部参数
	 * @param family 站点属性
	 * @param host 站点监听地址
	 * @param invokerId 调用器编号
	 * @param index 子任务在集合中的下标
	 */
	public ProcessRuleTag(byte family, SiteHost host, long invokerId, int index) {
		this(new Node(family, host), new EchoFlag(invokerId, index));
	}

	/**
	 * 从可类化数据读取器中解析事务处理标识配置参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ProcessRuleTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析事务处理标识参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public ProcessRuleTag(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 返回调用器地址
	 * @return 调用器地址
	 */
	public Node getLocal() {
		return local;
	}

	/**
	 * 设置调用器地址
	 * @param e 调用器地址
	 */
	public void setLocal(Node e) {
		Laxkit.nullabled(e);

		local = e.duplicate();
	}

	/**
	 * 返回调用器地址类型
	 * @return 调用器地址类型
	 */
	public byte getFamily() {
		return local.getFamily();
	}

	/**
	 * 返回事务处理标识的主机地址
	 * @return 主机地址
	 */
	public SiteHost getHost() {
		return local.getHost();
	}

	/**
	 * 设置回显标识
	 * @param e EchoFlag实例
	 */
	public void setFlag(EchoFlag e) {
		Laxkit.nullabled(e);

		flag = e.duplicate();
	}

	/**
	 * 返回回显标识
	 * @return EchoFlag实例
	 */
	public EchoFlag getFlag() {
		return flag;
	}

	/**
	 * 返回命令源异步调用器的编号
	 * @return 长整型，大于或者等于0。
	 */
	public long getInvokerId() {
		return flag.getInvokerId();
	}

	/**
	 * 返回命令源异步调用器的子任务编号
	 * @return 整型，大于或者等于0。
	 */
	public int getIndex() {
		return flag.getIndex();
	}

	/**
	 * 建立一个当前事务处理标识的数据副本
	 * @return ProcessRuleTag实例
	 */
	public ProcessRuleTag duplicate() {
		return new ProcessRuleTag(this);
	}

	/**
	 * 比较两个事务处理标识一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ProcessRuleTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ProcessRuleTag) that) == 0;
	}

	/**
	 * 返回事务处理标识的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return local.hashCode() ^ flag.hashCode();
	}

	/**
	 * 返回事务处理标识的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", local, flag);
	}

	/**
	 * 根据当前事务处理标识配置，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个事务处理标识的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ProcessRuleTag that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较调用器地址和回显标识
		int ret = local.compareTo(that.local);
		if (ret == 0) {
			ret = flag.compareTo(that.flag);
		}
		return ret;
	}

	/**
	 * 事务处理标识生成数据流输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析事务处理标识，返回解析的长度
	 * @param b  字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}