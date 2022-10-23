/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.range;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 数值范围。<br><br>
 * 
 * 数值范围描述一段数据的开始到结束的区域。一定是开始值小于等于结束值，否则即是错误。<br>
 * 数值范围是一个虚接口，具体的值定义由子类实现。<br>
 * 
 * @author scott.liang
 * @version 1.0 1/17/2009
 * @since laxcus 1.0
 */
public abstract class Range implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 3713137339605532961L;

	/**
	 * 构造一个默认的数值范围类
	 */
	protected Range() {
		super();
	}

	/**
	 * 根据传入的数值范围类，生成它的副本
	 * @param that Range实例
	 */
	protected Range(Range that) {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成一个当前实例副本
	 * @return Range子类实例
	 */
	public abstract Range duplicate();
}