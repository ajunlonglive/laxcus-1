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
 * 回显码。<br><br>
 * 回显码是回显报头的一部分，标记一个异步应答数据的具体情况。<br>
 * 回显码包括主码和辅助码。主码标记成功/失败，辅助码说明成功/失败的具体原因。<br><br>
 * 
 * 主码分成功和失败两种。等于大于0是成功，小于0是失败。<br>
 * 主码的定义见Major，辅助码的定义见Minor。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class EchoCode implements Classable, Cloneable, Serializable, Comparable<EchoCode> {

	private static final long serialVersionUID = 7614212313792897618L;

	/** 主码。见Major中的定义。大于等于0是成功，小于0是错误。 **/
	private short major;

	/** 辅助码。这个编号是在主码之下的，对一个应答的具体定义。默认是0，无定义 **/
	private short minor;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 主码
		writer.writeShort(major);
		// 辅助码
		writer.writeShort(minor);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 主码
		major = reader.readShort();
		// 辅助码
		minor = reader.readShort();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入参数，生成它的数据副本
	 * @param that EchoCode实例
	 */
	private EchoCode(EchoCode that) {
		super();
		major = that.major;
		minor = that.minor;
	}

	/**
	 * 构造默认和私有的回显码
	 */
	private EchoCode() {
		super();
		major = 0;
		minor = 0;
	}

	/**
	 * 构造回显码。指定主码，辅助码默认是0。
	 * @param major 主码
	 */
	public EchoCode(short major) {
		this();
		setMajor(major);
	}

	/**
	 * 构造回显码，指定主码和辅助码
	 * @param major 主码
	 * @param minor 辅助码
	 */
	public EchoCode(short major, short minor) {
		this(major);
		setMinor(minor);
	}
	
	/**
	 * 从可类化数据读取器中解析回显码
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public EchoCode(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置主码
	 * @param i 主码
	 */
	public void setMajor(short i) {
		major = i;
	}

	/**
	 * 返回主码
	 * @return 主码
	 */
	public short getMajor() {
		return major;
	}

	/**
	 * 设置辅助码
	 * @param i 辅助码
	 */
	public void setMinor(short i) {
		minor = i;
	}

	/**
	 * 返回辅助码
	 * @return 辅助码
	 */
	public short getMinor() {
		return minor;
	}

	/**
	 * 判断异步处理成功。主码大于等于0是成功。见Answer中定义。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isSuccessful() {
		return Major.isSuccessful(major);
	}

	/**
	 * 判断异步处理失败。主码小于0是失败。见Anwser中的定义。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isFaulted() {
		return Major.isFaulted(major);
	}

	/**
	 * 判断是对象化标识（不区分成功或者失败，只判断是对象）
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isObjectable() {
		return major == Major.SUCCESSFUL_OBJECT
				|| major == Major.FAULTED_OBJECT;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return EchoCode实例
	 */
	public EchoCode duplicate() {
		return new EchoCode(this);
	}

	/**
	 * 检查两个回显码一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != EchoCode.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EchoCode) that) == 0;
	}

	/**
	 * 返回回显码的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (minor == 0) {
			return major;
		} else {
			return major ^ minor;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回回显码的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d,%d", major, minor);
	}

	/**
	 * 比较两个回显码相同
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EchoCode that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(major, that.major);
		if (ret == 0) {
			ret = Laxkit.compareTo(minor, that.minor);
		}
		return ret;
	}

}