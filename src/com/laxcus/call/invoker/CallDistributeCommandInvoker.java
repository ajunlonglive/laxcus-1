/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.*;

/**
 * 分布操作调用器
 * 
 * @author scott.liang
 * @version 1.0 12/25/2020
 * @since laxcus 1.0
 */
public abstract class CallDistributeCommandInvoker extends CallInvoker {
	
	/** 故障信息 **/
	protected String faultText;
	
	/**
	 * 构造分布处理调用器
	 * @param cmd
	 */
	protected CallDistributeCommandInvoker(DistributedCommand cmd) {
		super(cmd);
	}

	/**
	 * 设置错误信息
	 * @param text 错误信息
	 */
	protected void setFaultText(String text) {
		faultText = text;
	}

	/**
	 * 设置错误信息
	 * @param e
	 */
	protected void setFaultText(Throwable e) {
		String text = e.getMessage();
		if (text != null && text.length() > 0) {
			setFaultText(text);
		}
	}

}