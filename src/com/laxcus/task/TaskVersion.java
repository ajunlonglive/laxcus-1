/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

/**
 * 分布任务组件版本号。<br>
 * 开发者在实现自己的分布任务组件中，通过指定自己软件的版本号，提供给系统识别，让系统以最优化方式调用新的接口。<br><br>
 * 
 * 分布任务组件版本号由系统规定，开发者不能定义，不能派生。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/9/2013
 * @since laxcus 1.0
 */
public final class TaskVersion {

	/** 无定义 **/
	public final static int NONE = 0;

	/** 1.0版本号。在声明的关键字是：“1.0” **/
	public static final int VERSION_1 = 0x01000000;

	//	/** 2.0 版本号 **/
	//	public static final int VERSION2 = 0x02000000;

	/** 版本号 **/
	private int version;
	
	/**
	 * 设置分布任务组件版本号
	 * @param version
	 */
	public TaskVersion(int version) {
		super();
		setVersion(version);
	}

	/**
	 * 设置分布任务组件版本号
	 * @param i 版本号
	 */
	public void setVersion(int i) {
		version = i;
	}

	/**
	 * 返回分布任务组件版本号
	 * @return 数字
	 */
	public int getVersion() {
		return version;
	}
	
	/**
	 * 将字符串格式的版本号翻译为数字格式
	 * @param input 字符串版本号
	 * @return 数字格式版本号
	 */
	public static int translate(String input) {
		if (input.matches("^\\s*(?i)(1.0)\\s*$")) {
			return TaskVersion.VERSION_1;
		}
		// 非法
		return 0;
	}

	/**
	 * 将数字格式的版本号翻译为字符串格式
	 * @param version 版本号
	 * @return 版本号的字符串描述
	 */
	public static String translate(int version) {
		switch (version) {
		case TaskVersion.VERSION_1:
			return "1.0";
		}
		return "Illegal Task Version";
	}

	/**
	 * 判断是合法的版本类型
	 * @param version 版本号
	 * @return 返回真或者假
	 */
	public static boolean isFamily(int version) {
		switch (version) {
		case VERSION_1:
			return true;
		}
		return false;
	}

}