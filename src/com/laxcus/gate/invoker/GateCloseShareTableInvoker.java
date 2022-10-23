/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.cross.*;

/**
 * 关闭共享数据表资源调用器
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class GateCloseShareTableInvoker extends GateShareCrossInvoker {

	/**
	 * 构造关闭共享数据表资源调用器，制定命令
	 * @param cmd 关闭共享数据表资源
	 */
	public GateCloseShareTableInvoker(CloseShareTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gate.invoker.GateShareCrossInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断有操作权限
		if (!canCloseResource()) {
			refuse();
			return false;
		}
		return super.launch();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gate.invoker.GateShareCrossInvoker#createAward()
	 */
	@Override
	protected AwardShareCross createAward() {
		return new AwardCloseActiveItem();
	}
}