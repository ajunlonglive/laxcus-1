/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.command.stub.transfer.*;

/**
 * 查询映像数据块站点。<br>
 * 
 * 这个命令由DATA/BUILD站点发出，目标是它上属的CALL站点。CALL检索本地保存的映像数据块注册记录，返回对应的DATA站点。
 * 映像数据块分为CACHE和CHUNK两种状态。
 * 
 * @author scott.liang
 * @version 1.1 09/07/2013
 * @since laxcus 1.0
 */
public abstract class FindReflexStubSite extends TransferMass {

	private static final long serialVersionUID = 1541278564491380872L;

	/**
	 * 构造默认查询映像数据块站点
	 */
	protected FindReflexStubSite() {
		super();
	}

	/**
	 * 根据传入的查询映像数据块站点实例，生成它的浅层数据副本
	 * @param that - 查询映像数据块站点命令
	 */
	protected FindReflexStubSite(FindReflexStubSite that) {
		super(that);
	}

}
