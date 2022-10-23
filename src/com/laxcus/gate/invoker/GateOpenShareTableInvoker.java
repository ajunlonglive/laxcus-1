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
 * 开放共享数据表资源调用器
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class GateOpenShareTableInvoker extends GateShareCrossInvoker {
	
	/**
	 * 构造开放共享数据表资源调用器，制定命令
	 * @param cmd 开放共享数据表资源
	 */
	public GateOpenShareTableInvoker(OpenShareTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OpenShareTable getCommand() {
		return (OpenShareTable) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.gate.invoker.GateShareCrossInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断有操作权限
		if (!canOpenResource()) {
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
		return new AwardOpenActiveItem();
	}
	
}