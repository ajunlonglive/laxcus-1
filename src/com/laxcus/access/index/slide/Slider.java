/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

/**
 * 对象定位器。<br><br>
 * 
 * 对象定位器用在数据分区(IndexSector)中，是对一个数据对象进行计算，产生一个数值（java.lang.Number的子类），这个数值将确定数据对象在分区集合的下标位置。<br><br>
 * 
 * 所有对象定位器，包括系统中固定的和用户自定义的，必须继承这个接口，和实现这个接口中的方法。<br><br>
 * 
 * <b>这是数据分区的核心！！！</b><br><br>
 * 
 * 对象定位器由用户实现，通过IndexBalaner.balance(int sites, Slider slider)接口输入。<BR>
 * 
 * 目前系统的对象定位器针对列值。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2013
 * @since laxcus 1.0
 */
public interface Slider {

	/**
	 * 判断传入对象和对象定位器实例匹配，且可以实现码位计算。
	 * 
	 * @param e 与接口实例匹配的对象
	 * @return 返回真或者假
	 */
	boolean isSupport(Object e);

	/**
	 * 根据传入对象，计算它的码位值。
	 * 通过码位值，将确定传入对象在分区集合的下标位置。
	 * 
	 * @param e 接口支持的对象
	 * @return 返回java.lang.Number的子类
	 * @throws SliderException 如果不支持这个对象或者其它错误
	 */
	java.lang.Number seek(Object e) throws SliderException;
}