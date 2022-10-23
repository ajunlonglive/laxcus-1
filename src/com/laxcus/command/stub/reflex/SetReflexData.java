/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.command.stub.transfer.*;
import com.laxcus.util.classable.*;

/**
 * 设置映像数据命令 <br>
 * 映像数据分为缓存映像数据（长度和内容可变）和存储映像数据（内容可变、长度不变）两种。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public abstract class SetReflexData extends TransferMass {

	private static final long serialVersionUID = -2438024934673847787L;

	/** 映像数据 **/
	private byte[] data;

	/**
	 * 构造围住的映像数据
	 */
	protected SetReflexData() {
		super();
	}

	/**
	 * 根据传入的映像数据实例，生成它的数据副本
	 * @param that SetReflexData实例
	 */
	protected SetReflexData(SetReflexData that) {
		super(that);
		data = that.data;
	}

	/**
	 * 设置映像数据
	 * @param b 字节数组
	 */
	public void setData(byte[] b) {
		this.data = b;
	}

	/**
	 * 返回映像数据
	 * @return 字节数组
	 */
	public byte[] getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 映像数据
		writer.writeByteArray(data);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 映像数据
		data = reader.readByteArray();
	}

}