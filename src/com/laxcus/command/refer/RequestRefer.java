/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 请求分配资源引用命令
 * 
 * @author scott.liang
 * @version 1.1 05/17/2015
 * @since laxcus 1.0
 */
public abstract class RequestRefer extends Command {

	private static final long serialVersionUID = 8856067829189502262L;

	/** 可用内存空间 **/
	private long size;

	/**
	 * 构造默认的请求分配资源引用命令
	 */
	protected RequestRefer() {
		super();
		size = Runtime.getRuntime().freeMemory();
	}

	/**
	 * 根据传入的请求分配资源引用命令，生成它的数据副本
	 * @param that 请求分配资源引用实例
	 */
	protected RequestRefer(RequestRefer that) {
		super(that);
		size = that.size;
	}

	/**
	 * 设置内存自由空间尺寸
	 * @param i 内存自由空间尺寸
	 */
	public void setSize(long i) {
		size = i;
	}

	/**
	 * 返回内存自由空间尺寸
	 * @return 内存自由空间尺寸
	 */
	public long getSize() {
		return  size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(size);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		size = reader.readLong();
	}

}
