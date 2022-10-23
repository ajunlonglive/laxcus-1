/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

/**
 * 强制分享授权资源 <br><br>
 * 
 * 把被授权人资源分配给授权人，分为“增加/删除”两个动作。
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public abstract class AwardShareActiveItem extends AwardShareCross {

	private static final long serialVersionUID = 3061090160435432117L;

	/**
	 * 构造默认的强制分享授权资源
	 */
	protected AwardShareActiveItem() {
		super();
	}

	/**
	 * 生成强制分享授权资源的数据副本
	 * @param that AwardShareActiveItem实例
	 */
	protected AwardShareActiveItem(AwardShareActiveItem that) {
		super(that);
	}

}