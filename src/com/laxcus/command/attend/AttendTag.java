/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

/**
 * 签到标记
 * 
 * @author scott.liang
 * @version 1.0 3/18/2014
 * @since laxcus 1.0
 */
public class AttendTag {

	/** 成功接受 **/
	public final static int CONFORM = 1;

	/** 接受，但是会延迟。这种情况由调用器自己处理 **/
	public final static int DELAY = 2;

	/** 拒绝 **/
	public final static int REFUSE = 3;

}