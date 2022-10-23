/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH命令的任务分派器。有SCAN/SIFT/RISE三种<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public abstract class EstablishDispatcher extends DistributedDispatcher {

	private static final long serialVersionUID = -2207863421685691702L;
	
	/** 自动释放会话 **/
	private boolean autoRelease;

	/*
	 * 将任务输出器参数写入可类化存储器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 保存上级数据信息
		super.buildSuffix(writer);
		writer.writeBoolean(autoRelease);
	}

	/*
	 * 从可类化读取器中读取任务输出器的参数
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析上级信息
		super.resolveSuffix(reader);
		autoRelease = reader.readBoolean();
	}

	/**
	 * 构造一个默认的任务输出器
	 */
	protected EstablishDispatcher() {
		super();
		autoRelease = false;
	}

	/**
	 * 根据传入的任务输出器，复制它的成员参数
	 * @param that EstablishDispatcher实例
	 */
	protected EstablishDispatcher(EstablishDispatcher that) {
		super(that);
		autoRelease = that.autoRelease;
	}

	/**
	 * 设置自动释放
	 * @param b 自动释放
	 */
	public void setAutoRelease(boolean b) {
		autoRelease = b;
	}

	/**
	 * 判断是自动释放
	 * @return 返回真或者假
	 */
	public boolean isAutoRelease() {
		return autoRelease;
	}

}