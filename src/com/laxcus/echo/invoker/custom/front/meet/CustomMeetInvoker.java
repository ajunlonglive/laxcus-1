/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom.front.meet;

import com.laxcus.command.custom.*;
import com.laxcus.front.meet.invoker.*;

/**
 * 自定义FRONT MEET交换调用器
 *
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public abstract class CustomMeetInvoker extends MeetInvoker {

	/**
	 * 构造默认的FRONT.MEET异步调用器
	 */
	protected CustomMeetInvoker() {
		super();
	}

	/**
	 * 构造FRONT.MEET异步调用器，指定命令
	 * @param cmd 自定义异步命令
	 */
	protected CustomMeetInvoker(CustomCommand cmd) {
		super(cmd);
	}

}