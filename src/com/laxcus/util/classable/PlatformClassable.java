/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

/**
 * 跨平台可类化接口。在继承本地平台Classable的基础上，提供标签服务。
 * 标签服务是跨平台的标记，通过这个标记对应到实际的类实例，然后生成它的类实例。
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public interface PlatformClassable extends Classable {

	/**
	 * 返回平台可类化接口的标记。通过标记可以找到不同平台上的类对象定义。
	 * @return 数据签名人
	 */
	String getSigner();

}
