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
 * 回显报头。或称“RPC异步应答报头”。<br><br>
 * 
 * 回显报头用在异步数据的反馈阶段，是异步任务的开始。
 * 回显报头由EchoClient发送到EchoAgent，再由EchoAgent转发给EchoReceiver。<br><br>
 * 
 * 回显报头参数：<br>
 * 1. 回显码。标记异步应答数据的状态。<br>
 * 2. 数据长度。(在回显报头之后数据流长度。0表示没有数据，-1表示长度不确定，大于0表示有效数据长度）。<br>
 * 3. 辅助信息。（由用户自定义的信息，由网络通信双方自行解释，回显报头只负责传递）。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class EchoHead implements Classable, Cloneable, Serializable, Comparable<EchoHead> {

	private static final long serialVersionUID = -5172062810131521925L;

	/** 回显码 **/
	private EchoCode code;

	/** 后续返回的数据长度。-1表示长度不确定，0是没有数据，大于0表示确定数据长度。 **/
	private long length;

	/** 快速投递标识，用在cast/exit命令组的通信 **/
	private CastFlag flag;

	/** 辅助信息。用户自定义自解释 **/
	private EchoHelp help;

	/**
	 * 将回显报头参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 回显码
		writer.writeObject(code);
		// 数据长度
		writer.writeLong(length);
		// RPC快速通信标识
		writer.writeInstance(flag);
		// 辅助信息
		writer.writeDefault(help);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析回显报头参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 回显码
		code = new EchoCode(reader);
		// 数据长度
		length = reader.readLong();
		// RPC快速通信标识
		flag = reader.readInstance(CastFlag.class);
		// 辅助信息
		help = (EchoHelp) reader.readDefault();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认和私有的回显报头
	 */
	private EchoHead() {
		super();
	}

	/**
	 * 根据传入的回显报头，生成它的数据副本
	 * @param that EchoHead实例
	 */
	private EchoHead(EchoHead that) {
		this();
		code = that.code.duplicate();
		length = that.length;
		if (that.flag != null) {
			flag = that.flag.duplicate();
		}
		// 辅助信息副本
		if (that.help != null) {
			help = that.help.duplicate();
		}
	}

	/**
	 * 构造回显报头，指定回显码
	 * @param code 回显码
	 */
	public EchoHead(EchoCode code) {
		this();
		setCode(code);
	}

	/**
	 * 构造回显报头，指定回显码和数据长度
	 * @param code 回显码
	 * @param length 数据长度
	 */
	public EchoHead(EchoCode code, long length) {
		this(code);
		setLength(length);
	}

	/**
	 * 构造构造回显报头，指定全部参数
	 * @param code 回显码
	 * @param length 数据长度
	 * @param help 辅助参数
	 */
	public EchoHead(EchoCode code, long length, EchoHelp help) {
		this(code, length);
		setHelp(help);
	}

	/**
	 * 构造构造回显报头，指定全部参数
	 * @param code 回显码
	 * @param length 数据长度
	 * @param flag 快速通信标识
	 */
	public EchoHead(EchoCode code, long length, CastFlag flag) {
		this(code, length);
		setCastFlag(flag);
	}

	/**
	 * 构造回显报头，指定基础参数
	 * @param major 应答码（主码）
	 * @param minor 应答辅助码（次码，标注一个具体的操作）
	 * @param length 应答数据长度
	 */
	public EchoHead(short major, short minor, long length) {
		this(new EchoCode(major, minor), length);
	}

	/**
	 * 构造构造回显报头，指定全部参数
	 * @param major 应答码（主码）
	 * @param minor 应答辅助码（次码，标注一个具体的操作）
	 * @param length 应答数据长度
	 * @param help 回显辅助信息
	 */
	public EchoHead(short major, short minor, long length, EchoHelp help) {
		this(major, minor, length);
		setHelp(help);
	}

	/**
	 * 构造回显报头，指定基础参数
	 * @param major - 应答码
	 * @param length - 应答数据长度，-1表示长度不确定
	 */
	public EchoHead(short major, long length) {
		this(new EchoCode(major), length);
	}

	/**
	 * 构造构造回显报头，指定全部参数
	 * @param major 应答码
	 * @param length 侍传输的数据长度
	 * @param help 回显辅助信息
	 */
	public EchoHead(short major, long length, EchoHelp help) {
		this(major, length);
		setHelp(help);
	}

	/**
	 * 构造构造回显报头，指定全部参数
	 * @param major 应答码
	 * @param length 侍传输的数据长度
	 * @param flag 快速异步通信标识
	 */
	public EchoHead(short major, long length, CastFlag flag) {
		this(major, length);
		setCastFlag(flag);
	}

	/**
	 * 从可类化读取器中解析回显报头参数
	 * @param reader - 可类化读取器
	 * @since 1.1
	 */
	public EchoHead(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 判断异步处理成功。应答码大于等于0是成功。见Answer中定义。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isSuccessful() {
		return code.isSuccessful();
	}

	/**
	 * 判断异步处理失败。应答码小于0是失败。见Anwser中的定义。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isFaulted() {
		return code.isFaulted();
	}

	/**
	 * 设置回显码，不允许空值。
	 * @param e 回显码
	 */
	public void setCode(EchoCode e) {
		Laxkit.nullabled(e);

		code = e;
	}

	/**
	 * 返回回显码
	 * @return 回显码
	 */
	public EchoCode getCode() {
		return code;
	}

	/**
	 * 判断是对象化数据（包含一个协商双方共同遵守定义的对象）
	 * @return 返回真或者假
	 */
	public boolean isObjectable() {
		return code.isObjectable();
	}

	/**
	 * 设置异步回应数据长度
	 * @param i 异步回应数据长度
	 */
	public void setLength(long i) {
		length = i;
	}

	/**
	 * 返回异步回应数据长度
	 * @return 异步回应数据长度
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 设置异步通信标识
	 * @param e 异步通信标识实例
	 */
	public void setCastFlag(CastFlag e) {
		flag = e;
	}

	/**
	 * 返回异步通信标识
	 * @return 异步通信标识实例
	 */
	public CastFlag getCastFlag() {
		return flag;
	}

	/**
	 * 判断异步通信标识包含指定的辅助信息
	 * @param clazz 指定类
	 * @return 返回真或者假
	 */
	public boolean isCastHelp(Class<?> clazz) {
		return flag != null && flag.isHelp(clazz);
	}

	/**
	 * 返回指定类的类实例
	 * @param <T> 类类型
	 * @param clazz 指定类
	 * @return 返回类对象实例，或者空指针
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCastHelp(java.lang.Class<?> clazz) {
		if (isCastHelp(clazz)) {
			return (T) flag.getHelp(clazz);
		}
		return null;
	}

	/**
	 * 设置辅助信息，允许空值
	 * @param e 回显辅助信息
	 */
	public void setHelp(EchoHelp e) {
		help = e;
	}

	/**
	 * 返回辅助信息
	 * @return 回显辅助信息
	 */
	public EchoHelp getHelp() {
		return help;
	}

	/**
	 * 判断是指定的类实例
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean isHelp(java.lang.Class<?> clazz) {
		if (help == null) {
			return false;
		}
		return Laxkit.isClassFrom(help, clazz);
	}

	/**
	 * 返回指定类的类实例
	 * @param <T> 类类型
	 * @param clazz 指定类
	 * @return 返回类对象实例，或者空指针
	 */
	@SuppressWarnings("unchecked")
	public <T> T getHelp(java.lang.Class<?> clazz) {
		if (isHelp(clazz)) {
			return (T) help;
		}
		return null;
	}

	/**
	 * 返回当前实例的数据副本
	 * @return EchoHead实例
	 */
	public EchoHead duplicate() {
		return new EchoHead(this);
	}

	/**
	 * 检查两个回显报头一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != EchoHead.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EchoHead) that) == 0;
	}

	/**
	 * 返回回显报头的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return code.hashCode();
	}

	/**
	 * 返回回显报头的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (flag == null) {
			return String.format("%s & %d", code, length);
		} else {
			return String.format("%s & %d & %s", code, length, flag);
		}
	}

	/**
	 * 根据当前回显报头实例，生成它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个回显报头的排列位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EchoHead that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(code, that.code);
		if (ret == 0) {
			ret = Laxkit.compareTo(length, that.length);
		}
		return ret;
	}

}