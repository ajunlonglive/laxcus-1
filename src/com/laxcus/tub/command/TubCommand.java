/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.command;

import com.laxcus.util.classable.*;

/**
 * 边缘应用命令，由终端发送给边缘端。
 * 
 * @author scott.liang
 * @version 1.0 10/16/2020
 * @since laxcus 1.0
 */
public abstract class TubCommand implements Classable, Cloneable {

	/** 命令建立时间，是本地参数，不参与串行化和可类化。**/
	private transient long createTime;
	
	/** 命令原语。如果是站点间命令，这个参数是空值；如果是FRONT站点发出，设置原语。 **/
	private String primitive;

	/**
	 * 构造默认的边缘应用命令
	 */
	protected TubCommand() {
		super();
	}

	/**
	 * 生成边缘应用命令副本
	 * @param that 传入命令
	 */
	protected TubCommand(TubCommand that) {
		this();
		createTime = that.createTime;
		primitive = that.primitive;
	}
	
	/**
	 * 设置命令的语句描述。原语允许空值。
	 * @param e 命令原语文本
	 */
	public void setPrimitive(String e) {
		primitive = e;
	}

	/**
	 * 返回命令的语句描述
	 * @return 命令原语文本
	 */
	public String getPrimitive() {
		return primitive;
	}

	/**
	 * 返回操作命令的语句描述原语
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (primitive != null) {
			return primitive;
		}
		return getClass().getSimpleName();
	}

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 命令原语
		writer.writeString(primitive);
		// 调用子类接口，将子类信息写入可类化存储器
		buildSuffix(writer);
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
		// 命令原语
		primitive = reader.readString();
		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * TubCommand子类对象生成自己的浅层数据副本。<br>
	 * 浅层数据副本和标准数据副本的区别在于：浅层数据副本只赋值对象，而不是复制对象内容本身。
	 * @return TubCommand子类实例
	 */
	public abstract TubCommand duplicate();

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}
