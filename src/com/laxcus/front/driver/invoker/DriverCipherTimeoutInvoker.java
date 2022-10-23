/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.echo.product.*;
import com.laxcus.fixp.*;
import com.laxcus.front.driver.mission.*;

/**
 * 设置FIXP本地密文超时调用器。<br>
 * 
 * 本处密文设置只在FRONT站点起作用，不影响集群其它节点。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class DriverCipherTimeoutInvoker extends DriverInvoker {

	/**
	 * 构造设置FIXP本地密文超时调用器
	 * @param mission 驱动任务
	 */
	public DriverCipherTimeoutInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CipherTimeout getCommand() {
		return (CipherTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CipherTimeout cmd = getCommand();

		long interval = cmd.getInterval();
		// 设置本地时间
		Cipher.setTimeout(interval);
		print(true);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 打印结果
	 * @param successful
	 */
	private void print(boolean successful) {
		ConfirmProduct product = new ConfirmProduct(successful);
		setProduct(product);
	}

}