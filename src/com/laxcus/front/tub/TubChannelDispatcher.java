/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub;

import com.laxcus.command.*;
import com.laxcus.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.mission.*;
import com.laxcus.tub.servlet.*;

/**
 * 容器云端信道
 * 
 * @author scott.liang
 * @version 1.0 6/23/2019
 * @since laxcus 1.0
 */
public class TubChannelDispatcher implements TubChannel {
	
	/** FRONT站点 **/
	private FrontLauncher launcher;

	/**
	 * 构造默认的容器云端信道
	 */
	public TubChannelDispatcher(FrontLauncher e) {
		super();
		launcher = e;
	}

	/**
	 * 返回异步调用器管理池
	 * @return FrontInvokerPool实例 
	 */
	public FrontInvokerPool getInvokerPool() {
		return (FrontInvokerPool) launcher.getInvokerPool();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.TubChannel#submit(com.laxcus.command.Command)
	 */
	@Override
	public MissionResult submit(Command cmd) throws MissionException {
		return getInvokerPool().launchTub(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.TubChannel#submit(java.lang.String)
	 */
	@Override
	public MissionResult submit(String input) throws MissionException {
		return getInvokerPool().launchTub(input);
	}

}