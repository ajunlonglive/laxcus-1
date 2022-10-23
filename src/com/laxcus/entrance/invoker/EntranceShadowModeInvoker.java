/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.invoker;

import com.laxcus.command.site.entrance.*;

/**
 * 定位GATE站点调用器
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class EntranceShadowModeInvoker extends EntranceInvoker {

	/**
	 * 构造定位GATE站点调用器，指定命令
	 * @param cmd 定位GATE站点
	 */
	public EntranceShadowModeInvoker(ShadowMode cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShadowMode getCommand() {
		return (ShadowMode) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShadowMode cmd = getCommand();
		getLauncher().setHash(cmd.isHash());

		// 反馈结果，是ENTRANCE内网地址！
		ShadowModeProduct product = new ShadowModeProduct();
		product.add(getLocal());
		replyProduct(product);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
