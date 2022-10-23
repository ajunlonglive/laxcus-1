/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.user.*;

/**
 * 设置云存储空间尺寸调用器
 * 
 * @author scott.liang
 * @version 1.0 10/26/2021
 * @since laxcus 1.0
 */
public class MeetSetCloudSizeInvoker extends MeetSetMultiUserParameterInvoker {

	/**
	 * 构造设置云存储空间尺寸调用器，指定命令 
	 * @param cmd 设置云存储空间尺寸
	 */
	public MeetSetCloudSizeInvoker(SetCloudSize cmd) {
		super(cmd);
	}

}