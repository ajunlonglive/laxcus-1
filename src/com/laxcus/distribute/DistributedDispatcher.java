/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute;

import com.laxcus.util.classable.*;

/**
 * 分布式任务分派器 <BR>
 * 是CONDUCT/ESTABLISH/CONTACT的超类
 * 
 * @author scott.liang
 * @version 1.0 12/23/2020
 * @since laxcus 1.0
 */
public abstract class DistributedDispatcher extends AccessObject {
	
	private static final long serialVersionUID = -7946466020324688523L;
	
	/** 默认的返回值，如果有定义，首先返回它 **/
	private byte[] defaultReturnValue;

	/*
	 * 将任务输出器参数写入可类化存储器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 保存上级数据信息
		super.buildSuffix(writer);
		// 默认的返回值 
		writer.writeByteArray(defaultReturnValue);
	}

	/*
	 * 从可类化读取器中读取任务输出器的参数
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析上级信息
		super.resolveSuffix(reader);
		// 解析分区
		defaultReturnValue = reader.readByteArray();
	}
	
	/**
	 * 构造分布式任务分派器
	 */
	protected DistributedDispatcher() {
		super();
	}

	/**
	 * 分布式任务分派器副本
	 * @param that
	 */
	protected DistributedDispatcher(DistributedDispatcher that) {
		super(that);
		defaultReturnValue = that.defaultReturnValue;
	}

	/**
	 * 设置默认的返回值
	 * @param b 字节数组
	 */
	public void setDefaultReturnValue(byte[] b) {
		defaultReturnValue = b;
	}

	/**
	 * 返回默认的返回值
	 * @return 字节数组
	 */
	public byte[] getDefaultReturnValue() {
		return defaultReturnValue;
	}

	/**
	 * 判断有默认的返回值
	 * @return 返回真或者假
	 */
	public boolean hasDefaultReturnValue() {
		return defaultReturnValue != null
				&& defaultReturnValue.length > 0;
	}

}