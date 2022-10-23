/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.traffic.*;

/**
 * 并行流量测试调用器
 * @author scott.liang
 * @version 1.0 10/5/2018
 * @since laxcus 1.0
 */
public class RayParallelMultiGustInvoker extends RayInvoker {

	/**
	 * 构造并行流量测试调用器，指定命令
	 * @param cmd 并行流量测试命令
	 */
	public RayParallelMultiGustInvoker(ParallelMultiGust cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ParallelMultiGust getCommand() {
		return (ParallelMultiGust) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ParallelMultiGust cmd = getCommand();
		MultiGust sub = cmd.getMultiGust();
		int count = cmd.getIterate();

		// 生成调用器，并行发送
		for (int i = 0; i < count; i++) {
			MultiGust real = sub.duplicate();
			real.setSerial(i + 1);
			RayMultiGustInvoker invoker = new RayMultiGustInvoker(real);
			invoker.setDisplay(getDisplay());
			getInvokerPool().launch(invoker);
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