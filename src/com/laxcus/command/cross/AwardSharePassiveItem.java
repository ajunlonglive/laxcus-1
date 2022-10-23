/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

/**
 * 强制分享被授权资源 <br><br>
 * 
 * 把授权人资源分配给被授权人，分为“增加/删除”两个动作。
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public abstract class AwardSharePassiveItem extends AwardShareCross {

	private static final long serialVersionUID = 2119069419133277977L;

	/**
	 * 构造默认的强制分享被授权资源
	 */
	protected AwardSharePassiveItem() {
		super();
	}

	/**
	 * 生成强制分享被授权资源的数据副本
	 * @param that AwardSharePassiveItem实例
	 */
	protected AwardSharePassiveItem(AwardSharePassiveItem that) {
		super(that);
	}

}