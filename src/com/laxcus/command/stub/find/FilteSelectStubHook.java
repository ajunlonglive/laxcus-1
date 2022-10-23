/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import com.laxcus.command.*;
import com.laxcus.command.stub.*;

/**
 * FilteSelectStub命令钩子
 * 
 * @author scott.liang
 * @version 1.0 06/22/2013
 * @since laxcus 1.0
 */
public class FilteSelectStubHook extends CommandHook {

	/**
	 * 构造默认的FindWhereStub命令钩子
	 */
	public FilteSelectStubHook() {
		super();
	}

	/**
	 * 返回数据块编号集
	 * @return StubProduct实例，或者空指针
	 */
	public StubProduct getProduct() {
		return (StubProduct) super.getResult();
	}
}