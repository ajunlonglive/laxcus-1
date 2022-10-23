/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 回显报尾。异步应答尾端。<br><br>
 * 
 * 回显报文尾端是每项RPC异步处理的最后一步。当客户机收到回显报尾后，表示本次异步操作结果。<br>
 * 异步应答工作由EchoClient发起，数据经过EchoAgent转发给注册的EchoReceiver。<br><br>
 * 
 * 参数：<br>
 * 1. 成功/失败。布尔值，描述之前发送的数据是正确或者否。（完整传输了数据）。<br>
 * 2. 传输的数据长度，对应EchoHead.length，不包括重传的数据。<br>
 * 3. 数据包的传输统计（EchoField的传输次数，包括重传的次数）。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class EchoTail implements Classable, Cloneable, Serializable, Comparable<EchoTail> {

	private static final long serialVersionUID = -7330932846901907236L;

	/** 发送成功或者失败 **/
	private boolean successful;

	/** 传输的数据长度 **/
	private long length;

	/** 传输统计（发包的次数） **/
	private int count;

	/** 辅助信息。用户自定义自解释 **/
	private EchoHelp help;

	/**
	 * 将异步应答尾端参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 基本参数
		writer.writeBoolean(successful);
		writer.writeLong(length);
		writer.writeInt(count);
		// 辅助信息
		writer.writeDefault(help);
		// 返回写入字节
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析异步应答尾端参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 基本参数
		successful = reader.readBoolean();
		length = reader.readLong();
		count = reader.readInt();
		// 辅助信息
		help = (EchoHelp) reader.readDefault();
		// 返回解析长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认和私有异步应答尾端
	 */
	private EchoTail() {
		super();
	}

	/**
	 * 根据传入的异步应答尾端实例，生成它的数据副本
	 * @param that EchoTail实例
	 */
	private EchoTail(EchoTail that) {
		this();
		successful = that.successful;
		length = that.length;
		count = that.count;
		// 辅助信息副本
		if (that.help != null) {
			help = that.help.duplicate();
		}
	}

	/**
	 * 构造异步应答尾端，指定参数
	 * @param successful 发送成功或者失败
	 * @param length 数据有效长度
	 * @param count 发送次数的统计
	 */
	public EchoTail(boolean successful, long length, int count) {
		this();
		setSuccessful(successful);
		setLength(length);
		setCount(count);
	}

	/**
	 * 从可类化读取中解析异步应答尾端
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public EchoTail(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置发送成功
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断发送成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 判断发送失败
	 * @return 返回真或者假
	 */
	public boolean isFaulted() {
		return !isSuccessful();
	}

	/**
	 * 设置传输数据长度
	 * @param i 传输数据长度
	 */
	public void setLength(long i) {
		length = i;
	}

	/**
	 * 返回传输数据长度
	 * @return 传输数据长度
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 设置传输次数
	 * @param i 传输次数
	 */
	public void setCount(int i) {
		count = i;
	}

	/**
	 * 返回传输次数
	 * @return 传输次数
	 */
	public int getCount() {
		return count;
	}

	/**
	 * 设置辅助信息，允许空值
	 * @param e 辅助信息实例
	 */
	public void setHelp(EchoHelp e) {
		help = e;
	}

	/**
	 * 返回辅助信息
	 * @return 辅助信息实例
	 */
	public EchoHelp getHelp() {
		return help;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return EchoTail实例
	 */
	public EchoTail duplicate() {
		return new EchoTail(this);
	}

	/**
	 * 比较两个异步应答尾端一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != EchoTail.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EchoTail) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) ((successful ? 1 : 2) ^ length ^ count);
	}

	/**
	 * 返回异步应答尾端字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s,%d,%d", (successful ? "Successful" : "Faulty"),
				length, count);
	}

	/**
	 * 根据当前异步应答尾端，克隆它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个异步应答尾端排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EchoTail that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(successful, that.successful);
		if (ret == 0) {
			Laxkit.compareTo(length, that.length);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(count, that.count);
		}
		return ret;
	}

}