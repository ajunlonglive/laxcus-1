/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.fast.*;

/**
 * “数据索引加载”命令的异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.1 9/2/2012
 * @since laxcus 1.0
 */
public class MeetLoadIndexInvoker extends MeetFastMassInvoker {

	/**
	 * 构造“数据索引加载”命令的异步调用器，指定数据索引加载命令。
	 * @param cmd
	 */
	public MeetLoadIndexInvoker(LoadIndex cmd) {
		super(cmd);
	}

}