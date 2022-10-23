/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.container;

/**
 * 容器应用退出时的返回码
 * 
 * @author scott.liang
 * @version 1.0 7/3/2021
 * @since laxcus 1.0
 */
public class ContainerShutdown {

	public final static int SUCCESSFUL = 0;
	
	public final static int FAILED = -1;
	
	public final static int NOTFOUND = -2;
}