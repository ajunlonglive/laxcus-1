/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.invoke;

import com.laxcus.remote.*;

/**
 * 网络通信的远程过程调用接口（RPC）。
 * 
 * @author scott.liang
 * @version 1.0 2/17/2009
 * @since laxcus 1.0
 */
public interface VisitInvoker {
	
	/**
	 * 根据类名称查找匹配的类实例
	 * @param className 类名
	 * @return 类对象实例
	 */
	Object findVisit(String className) ;

	/**
	 * RPC调用。传入一个请求，返回一个应答结果。
	 * @param creator RPC请求
	 * @return RPC应答
	 */
	PatternExtractor invoke(PatternConstructor creator);
}