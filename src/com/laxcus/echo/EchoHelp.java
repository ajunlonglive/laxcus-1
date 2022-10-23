/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 回显辅助信息。<br><br>
 * 
 * 回显辅助信息用在“回显报头/回显报尾”中，是服务器向客户机传递的一些个性化信息，这里只定义框架，其中的数据内容由用户自行设计和解释。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public abstract class EchoHelp implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -4910886153911617150L;

	/**
	 * 将回显辅助信息写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 子类信息写入可类化存储器
		buildSuffix(writer);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析回显辅助信息
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 解析子类信息
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的回显辅助信息。
	 */
	protected EchoHelp() {
		super();
	}

	/**
	 * 根据传入的对象实例，生成它的数据副本
	 * @param that EchoHelp实例
	 */
	protected EchoHelp(EchoHelp that) {
		this();
	}
	
	/**
	 * 克隆当前对象的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 子类实例产生一个自己的命令副本
	 * @return EchoHelp实例副本
	 */
	public abstract EchoHelp duplicate();

	/**
	 * 将回显辅助信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析回显辅助信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}