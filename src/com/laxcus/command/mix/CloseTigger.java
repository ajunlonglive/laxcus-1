/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.util.classable.*;

/**
 * 关闭TIGGER操作
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public class CloseTigger extends ProcessTigger {

	private static final long serialVersionUID = 1070458509131823167L;

	/**
	 * 构造默认的关闭TIGGER操作
	 */
	public CloseTigger() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析关闭TIGGER操作
	 * @param reader 可类化数据读取器
	 */
	public CloseTigger(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造关闭TIGGER操作，指定类型
	 * @param type TIGGER操作类型
	 */
	public CloseTigger(int type) {
		this();
		setType(type);
	}

	/**
	 * 生成关闭TIGGER操作副本
	 * @param that 关闭TIGGER操作
	 */
	private CloseTigger(CloseTigger that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CloseTigger duplicate() {
		return new CloseTigger(this);
	}

}
