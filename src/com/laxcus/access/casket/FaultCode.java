/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

/**
 * 故障码。<br>
 * 故障码是小于0的一组整数数字，包括JNI返回的故障和JRE环境操作发生的故障
 * 
 * @author scott.liang
 * @version 1.3 7/12/2017
 * @since laxcus 1.0
 */
public final class FaultCode {

	/** 一般性故障，无特别指向 **/
	public final static int DEFAULT = -1;

	/** 权限不足故障 **/
	public final static int PERMISSION_FAILED = -10000;

	/** 远程备份故障。特点是将一个数据块在两个站点之间传输时发生的故障 **/
	public final static int REMOTE_BACKUP_FAILED = -10001;
	
	/** 重新插入故障，是发生在DELETE之后的插入操作。 **/
	public final static int REINSERT_FAILED = -10002;

	/** 插入失败 **/
	public final static int INSERT_FAILED = -10004;
	/** 撤销失败 **/
	public final static int INSERT_LEAVE_FAILED = -10005;
}