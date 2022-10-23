/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;

/**
 * 列函数生成器
 * 
 * @author scott.liang
 * @version 1.5 7/12/2013
 * @since laxcus 1.0
 */
public class ColumnFunctionCreator {

	/** 列函数集合 **/
	private static Class<?>[] clazzes = new Class<?>[] { Sum.class, Avg.class,
			Count.class, Max.class, Min.class, First.class, Last.class,
			Now.class, Today.class, Len.class, Format.class, Mid.class,
			UCase.class, LCase.class };
	
	/**
	 * 判断有匹配的函数
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean matches(String input) {
		for (int i = 0; i < ColumnFunctionCreator.clazzes.length; i++) {
			try {
				ColumnFunction function = (ColumnFunction) ColumnFunctionCreator.clazzes[i].newInstance();
				if (function.matches(input)) {
					return true;
				}
			} catch (InstantiationException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			}
		}
		return false;
	}

	/**
	 * 根据传入参数，建立一个列函数实例
	 * @param table 数据库表配置，允许为null
	 * @param primitive 列函数原语
	 * @return ColumnFunction实例
	 */
	public static ColumnFunction create(Table table, String primitive) {
		for (int i = 0; i < ColumnFunctionCreator.clazzes.length; i++) {
			try {
				ColumnFunction function = (ColumnFunction) ColumnFunctionCreator.clazzes[i].newInstance();
				ColumnFunction instance = function.create(table, primitive);
				if (instance != null) return instance;
			} catch (InstantiationException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			}
		}
		return null;
	}
	
}