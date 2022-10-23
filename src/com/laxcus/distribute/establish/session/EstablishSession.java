/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.session;

import com.laxcus.distribute.*;

/**
 * 数据构建会话。<br><br>
 * 
 * 根据阶段操作需求，包括三种类型：SCAN/SIFT/RISE。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2012
 * @since laxcus 1.0
 */
public abstract class EstablishSession extends StepSession {

	private static final long serialVersionUID = 8239494500087790970L;

	/**
	 * 初始化数据构建会话实例，同时指定它的会话类型
	 * @param family 阶段会话类型
	 */
	protected EstablishSession(int family) {
		super(family);
	}

	/**
	 * 根据传入的ESTABLISH会话对象，生成它的浅层数据副本
	 * @param that ESTABLISH会话
	 */
	protected EstablishSession(EstablishSession that) {
		super(that);
	}

}