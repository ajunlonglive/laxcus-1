/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct;

/**
 * TO阶段对象和分布任务组件的工作模式 <br>
 * 
 * TO阶段工作模式有两种：产生数据、计算数据。GENERATE、EVALUATE。
 * 
 * @author scott.liang
 * @version 1.0 8/20/2014
 * @since laxcus 1.0
 */
public final class ToMode {

	/** “产生数据”模式 **/
	public final static int GENERATE = 1;

	/** “计算数据”模式 **/
	public final static int EVALUATE = 2;

	/**
	 * 判断是“产生数据”模式
	 * @param who TO阶段模式
	 * @return 返回真或者假
	 */
	public static boolean isGenerate(int who) {
		return who == ToMode.GENERATE;
	}

	/**
	 * 判断是“计算数据”模式
	 * @param who TO阶段模式
	 * @return 返回真或者假
	 */
	public static boolean isEvaluate(int who) {
		return who == ToMode.EVALUATE;
	}

	/**
	 * 判断是有效的模式
	 * 
	 * @param who TO阶段模式
	 * @return 有效返回“真”，否则“假”。
	 */
	public static boolean isMode(int who) {
		switch (who) {
		case ToMode.GENERATE:
		case ToMode.EVALUATE:
			return true;
		default:
			return false;
		}
	}
}