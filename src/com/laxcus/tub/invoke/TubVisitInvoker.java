/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.invoke;

import com.laxcus.tub.turn.*;

/**
 * 网络通信的远程过程调用接口（RPC）。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2020
 * @since laxcus 1.0
 */
public interface TubVisitInvoker {
	
	/**
	 * 根据类名称查找匹配的类实例
	 * @param className 类名
	 * @return 类对象实例
	 */
	Object findTubVisit(String className) ;

	/**
	 * RPC调用。传入一个请求，返回一个应答结果。
	 * @param creator RPC请求
	 * @return RPC应答
	 */
	TubExtractor invoke(TubConstructor creator);
}