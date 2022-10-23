/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.invoke;

import java.io.IOException;
import java.io.OutputStream;

import com.laxcus.fixp.*;

/**
 * 数据流任务调用接口，由各个节点分别实现具体的类
 * 
 * @author scott.liang
 * @version 1.1 11/7/2011
 * @since laxcus 1.0
 */
public interface StreamInvoker {

	/**
	 * FIXP数据流调用。数据流可以是请求或者应答的任何一种。是否回应调用，由实现类决定。
	 * @param stream FIXP数据流（请求/应答）
	 * @param resp FIXP数据输出流（用户调用这个接口输出数据）
	 * @throws IOException
	 */
	void invoke(Stream stream, OutputStream resp) throws IOException;

}