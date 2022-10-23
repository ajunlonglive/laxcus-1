/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import com.laxcus.util.classable.*;

/**
 * 基础单元
 * 
 * @author scott.liang
 * @version 1.0 8/3/2021
 * @since laxcus 1.0
 */
public abstract class WElement extends WToken {
	
	/** 开始 **/
	private boolean start;

	/**
	 * 构造基础单元
	 */
	protected WElement() {
		super();
		start = false;
	}

	/**
	 * 生成基础单元副本
	 * @param that 基础单元实例
	 */
	protected WElement(WElement that) {
		super(that);
		start = that.start;
	}

	/**
	 * 设置为开始
	 * @param b
	 */
	public void setStart(boolean b) {
		start = b;
	}

	/**
	 * 判断是开始
	 * @return
	 */
	public boolean isStart() {
		return start;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WElement#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(start);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WElement#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		start = reader.readBoolean();
	}

}