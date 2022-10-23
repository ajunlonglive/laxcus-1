/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.distant.random;

/**
 * 测试调用JAR组件
 * 
 * @author scott.liang
 * @version 1.0 2020-5-15
 * @since laxcus 1.0
 */
public class DistantRandom {

	/**
	 * 
	 */
	public DistantRandom() {
		super();
	}

//	public String show() {
//		return "国庆.东风41 / 中华高光时刻! ";
//	}
	
	/**
	 * 随机数
	 * @return
	 */
	public long random() {
		java.util.Random e = new java.util.Random(System.currentTimeMillis());
		return e.nextLong();
	}

}