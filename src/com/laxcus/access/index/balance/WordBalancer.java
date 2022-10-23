/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

/**
 * 字符索引平衡分割器。
 * 
 * @author scott.liang
 * @version 1.0 8/12/2009
 * @since laxcus 1.0
 */
public abstract class WordBalancer extends VariableBalancer {

	/** 大小写是否敏感，默认是TRUE(敏感) **/
	private boolean sentient;

	/**
	 * 构造默认的字符索引平衡分割器
	 */
	protected WordBalancer() {
		super();
		setSentient(true);
	}

	/**
	 * 设置大小写敏感。IS TRUE，大小写敏感，否则为NO
	 * @param b 敏感符
	 */
	public void setSentient(boolean b) {
		sentient = b;
	}

	/**
	 * 判断是否支持大小写敏感
	 * @return 返回真或者假
	 */
	public boolean isSentient() {
		return sentient;
	}

}
