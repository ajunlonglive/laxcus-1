/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.permit.*;

/**
 * 授权命令调用器。<br>
 * 管理员为用户分配操作权限
 * 
 * @author scott.liang
 * @version 1.0 2/23/2012
 * @since laxcus 1.0
 */
public class MeetGrantInvoker extends MeetCertificateInvoker {

	/**
	 * 构造授权命令调用器，指定“授权”命令。
	 * @param cmd GRANT命令
	 */
	public MeetGrantInvoker(Grant cmd) {
		super(cmd);
	}

}