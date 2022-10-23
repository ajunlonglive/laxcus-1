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
 * 解除授权调用器。<br>
 * 管理员注销给用户的权利。
 * 
 * @author scott.liang
 * @version 1.0 2/23/2012
 * @since laxcus 1.0
 */
public class MeetRevokeInvoker extends MeetCertificateInvoker {

	/**
	 * 构造解除授权调用器，指定“解除授权”命令
	 * @param cmd REVOKE命令
	 */
	public MeetRevokeInvoker(Revoke cmd) {
		super(cmd);
	}

}