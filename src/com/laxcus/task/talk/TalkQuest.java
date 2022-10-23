/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件对话查询。 <br><br>
 * 
 * 只用于交互过程中，在网络间传输。
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public final class TalkQuest implements Classable, Cloneable {

	/** 交互标识 **/
	private TalkFalg flag;

	/** 原始数据，用户自己定义和解析 **/
	private byte[] primitive;

	/**
	 * 构造默认的分布任务组件对话查询
	 */
	private TalkQuest() {
		super();
	}

	/**
	 * 构造对话查询，指定本地地址
	 * @param flag 交互标识
	 */
	public TalkQuest(TalkFalg flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 构造对话查询，指定参数
	 * @param flag 交互标识
	 * @param primitive 原始数据
	 */
	public TalkQuest(TalkFalg flag, byte[] primitive) {
		this(flag);
		setPrimitive(primitive);
	}

	/**
	 * 生成分布任务组件对话查询的数据副本
	 * @param that 分布任务组件对话查询
	 */
	private TalkQuest(TalkQuest that) {
		super();
		flag = that.flag;
		primitive = that.primitive;
	}

	/**
	 * 从可类化读取器中解析分布任务组件对话查询
	 * @param reader 可类化读取器
	 */
	public TalkQuest(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置交互标识<br>
	 * @param e TalkTag实例
	 */
	public void setFlag(TalkFalg e) {
		Laxkit.nullabled(e);
		flag = e;
	}

	/**
	 * 返回交互标识
	 * @return TalkTag实例
	 */
	public TalkFalg getFlag() {
		return flag;
	}

	/**
	 * 设置原始数据，用户自己定义和解析。
	 * @param b 命令原语文本
	 */
	public void setPrimitive(byte[] b) {
		primitive = b;
	}

	/**
	 * 返回原始数据，用户自己定义和解析
	 * @return 原始数据
	 */
	public byte[] getPrimitive() {
		return primitive;
	}

	/**
	 * 返回原始数据的字节长度
	 * @return 字节长度
	 */
	public int getPrimitiveLength() {
		return (primitive == null ? -1 : primitive.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成动态交互的数据副本
	 * 
	 * @return TalkQuest副本
	 */
	public TalkQuest duplicate() {
		return new TalkQuest(this);
	}

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 交互标识
		writer.writeObject(flag);
		// 写入原始数据
		writer.writeByteArray(primitive);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析命令参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 交互标识
		flag = new TalkFalg(reader);
		// 读取原始数据
		primitive = reader.readByteArray();
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

}