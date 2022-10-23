/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.product;

import com.laxcus.util.classable.*;

/**
 * 边缘计算应用报告，与TubCommand配合，由边缘端返回给终端。
 * 
 * @author scott.liang
 * @version 1.0 10/16/2020
 * @since laxcus 1.0
 */
public abstract class TubProduct implements Classable, Cloneable {

	/**
	 * 构造默认的边缘计算应用报告
	 */
	protected TubProduct() {
		super();
	}
	
	/**
	 * 生成副本
	 * @param that
	 */
	protected TubProduct(TubProduct that) {
		this();
	}

	/**
	 * 返回经过可类化的字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 将参数写入可类化存储器
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
	 * 从可类化读取器中解析参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
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
	 * 子类实例产生一个自己的命令副本
	 * @return TubProduct实例副本
	 */
	public abstract TubProduct duplicate();

	/**
	 * 将边缘计算应用报告写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析边缘计算应用报告
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}