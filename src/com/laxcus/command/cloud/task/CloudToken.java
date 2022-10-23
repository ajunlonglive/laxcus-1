/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 组件令牌
 * 
 * @author scott.liang
 * @version 1.0 8/10/2020
 * @since laxcus 1.0
 */
public abstract class CloudToken implements Classable, Cloneable {

	/**
	 * 构造默认的组件令牌
	 */
	protected CloudToken() {
		super();
	}

	/**
	 * 生成组件令牌副本
	 * @param that 组件令牌
	 */
	protected CloudToken(CloudToken that) {
		this();
	}

	/**
	 * 格式化CDATA格式的XML标签
	 * @param tag
	 * @param value
	 * @return 字符串
	 */
	public static String formatXML_CDATA(String tag, String value) {
		Laxkit.nullabled(tag);
		if (value == null) {
			value = "";
		}
		return String.format("<%s><![CDATA[%s]]></%s>\n", tag, value, tag);
	}

	/**
	 * 格式化XML标签
	 * @param tag
	 * @param value
	 * @return
	 */
	public static String formatXML(String tag, String value) {
		Laxkit.nullabled(tag);
		if (value == null) {
			value = "";
		}
		return String.format("<%s>\n%s</%s>\n", tag, value, tag);
	}

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
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
		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * 复制CloudToken子类对象的浅层数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成信息令牌的XML数据
	 * @return 字符串
	 */
	public abstract String buildXML();

	/**
	 * CloudToken子类对象生成自己的浅层数据副本。<br>
	 * 浅层数据副本和标准数据副本的区别在于：浅层数据副本只赋值对象，而不是复制对象内容本身。
	 * @return CloudToken子类实例
	 */
	public abstract CloudToken duplicate();

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}
