/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.traffic.*;

/**
 * 并行流量测试调用器
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopParallelMultiSwarmInvoker extends DesktopInvoker {

	/**
	 * 构造并行流量测试调用器，指定命令
	 * @param cmd 并行流量测试命令
	 */
	public DesktopParallelMultiSwarmInvoker(ParallelMultiSwarm cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ParallelMultiSwarm getCommand() {
		return (ParallelMultiSwarm) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ParallelMultiSwarm cmd = getCommand();
		MultiSwarm sub = cmd.getMultiSwarm();
		int count = cmd.getIterate();

		// 生成调用器，并行发送
		for (int i = 0; i < count; i++) {
			MultiSwarm multi = sub.duplicate();
			multi.setSerial(i + 1);
			DesktopMultiSwarmInvoker invoker = new DesktopMultiSwarmInvoker(multi);
			getInvokerPool().launch(invoker, getDisplay());
			delay(100);
		}
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}