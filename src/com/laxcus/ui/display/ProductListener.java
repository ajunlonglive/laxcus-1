/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

/**
 * 异步处理结果监听器
 * 
 * @author scott.liang
 * @version 1.0 10/23/2021
 * @since laxcus 1.0
 */
public interface ProductListener {

	//	/**
	//	 * 向结果监听器推送结果。
	//	 * 在调用器处理完成后，打印结果时，它拥有最高优先级
	//	 * 
	//	 * @param product 异步处理结果句柄，如果是空指针属于错误通知
	//	 */
	//	void push(EchoProduct product);

	/**
	 * 向结果监听器推送结果。
	 * 在调用器处理完成后，打印结果时，它拥有最高优先级。
	 * 
	 * @param object 结果实例，由接收端判断。异步处理结果句柄，如果是空指针属于错误通知
	 */
	void push(Object object);

}