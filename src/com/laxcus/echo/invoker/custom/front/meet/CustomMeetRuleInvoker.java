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
 * 基于交互模式的事务规则的自定义调用器
 * 
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public abstract class CustomMeetRuleInvoker extends MeetRuleInvoker {

	/**
	 * 构造自定义的事务规则调用器，指定命令
	 * @param cmd 自定义异步命令
	 */
	protected CustomMeetRuleInvoker(CustomCommand cmd) {
		super(cmd);
	}

}