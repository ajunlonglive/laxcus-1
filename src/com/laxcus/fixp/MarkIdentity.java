/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

/**
 * FIXP协议标头标识
 * 
 * @author scott.liang
 * @version 1.0 5/23/2009
 * @since laxcus 1.0
 */
public final class MarkIdentity {

	/** FIXP标题尺寸，固定7个字节 */
	public static final byte SIZE = 7;

	/** FIXP版本号 **/
	public final static short VERSION = 0x100;

	/** FIXP请求标记 **/
	public static final byte ASK = 2;

	/** FIXP应答标记 **/
	public static final byte ANSWER = 3;

}