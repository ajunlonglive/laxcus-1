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
 * 开放TIGGER操作
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public class OpenTigger extends ProcessTigger {

	private static final long serialVersionUID = 555214555716149496L;

	/**
	 * 构造默认的开放TIGGER操作
	 */
	public OpenTigger() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析开放TIGGER操作
	 * @param reader 可类化数据读取器
	 */
	public OpenTigger(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造开放TIGGER操作，指定类型
	 * @param type TIGGER操作类型
	 */
	public OpenTigger(int type) {
		this();
		setType(type);
	}

	/**
	 * 生成开放TIGGER操作副本
	 * @param that 开放TIGGER操作
	 */
	private OpenTigger(OpenTigger that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public OpenTigger duplicate() {
		return new OpenTigger(this);
	}

}
