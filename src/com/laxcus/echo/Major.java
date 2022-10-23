/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

/**
 * 回显主码。<br><br>
 * 
 * 回显主码用在EchoCode中。<br><br>
 * 
 * 回显主码有4种定义：<br>
 * 1. 0表示通用型正确。<br>
 * 2. -1表示通用型错误。<br>
 * 3. 大于0的正整数表示某个具体的正确应答。<br>
 * 4. 小于0且不等于-1的负整数表示某个具体的错误应答。<br>
 * 5. 含“对象化”数据的应答码有错误和正确两种，它们除了符合上述特征外，在位序列的0位置都是1。通过 (code & 0x1) ==1 判断它们是“对象化”数据。<br>
 * 
 * @author scott.liang
 * @version 1.0 05/17/2009
 * @since laxcus 1.0
 */
public final class Major {

	/** 正确 **/
	public static final short SUCCESSFUL = 0;

	/** 正确并且传输的是可对象化数据 **/
	public static final short SUCCESSFUL_OBJECT = 0x1;

	/** 正确并且传输的是文件 **/
	public static final short SUCCESSFUL_FILE = 0x2;
	
	/** 正确并且传输的数据 **/
	public static final short SUCCESSFUL_DATA = 0X3;
	
	/** 错误 **/
	public static final short FAULTED = -1;

	/** 错误并且传输的是类对象化数据 **/
	public static final short FAULTED_OBJECT = (short) 0x8001;
	
	/** 错误，并且传输的是文件 **/
	public static final short FAULTED_FILE = (short)0x8002;
	
	/** 错误，并且传输的是数据 **/
	public static final short FAULTED_DATA = (short)0x8003;
	
	/**
	 * 判断成功
	 * @param major 主码
	 * @return 返回真或者假
	 */
	public static boolean isSuccessful(short major) {
		switch (major) {
		case Major.SUCCESSFUL:
		case Major.SUCCESSFUL_FILE:
		case Major.SUCCESSFUL_OBJECT:
		case Major.SUCCESSFUL_DATA:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isSuccessfulObject(short major) {
		return major == Major.SUCCESSFUL_OBJECT;
	}

	public static boolean isSuccessfulFile(short major) {
		return major == Major.SUCCESSFUL_FILE;
	}
	
	public static boolean isSuccessfulData(short major) {
		return major == Major.SUCCESSFUL_DATA;
	}
	
	/**
	 * 判断发生故障
	 * @param major 主码
	 * @return 返回真或者假
	 */
	public static boolean isFaulted(int major) {
		switch (major) {
		case Major.FAULTED:
		case Major.FAULTED_OBJECT:
		case Major.FAULTED_FILE:
		case Major.FAULTED_DATA:
			return true;
		default:
			return false;
		}
	}

	public static boolean isFaultedObject(short major) {
		return major == Major.FAULTED_OBJECT;
	}

	public static boolean isFaultedFile(short major) {
		return major == Major.FAULTED_FILE;
	}

	public static boolean isFaultedData(short major) {
		return major == Major.FAULTED_DATA;
	}
	
	/**
	 * 不论成功/失败，判断传输的是可对象化数据
	 * 
	 * @param major 主码
	 * @return 返回真或者假
	 */
	public static boolean isObjectable(short major) {
		return major == Major.SUCCESSFUL_OBJECT
				|| major == Major.FAULTED_OBJECT;
	}
	
	/**
	 * 不论成功/失败，判断传输的是文件数据
	 * 
	 * @param major 主码
	 * @return 返回真或者假
	 */
	public static boolean isFileable(short major) {
		return major == Major.SUCCESSFUL_FILE
				|| major == Major.FAULTED_FILE;
	}
	
	/**
	 * 不论成功/失败，判断传输的是二进制数据
	 * 
	 * @param major 主码
	 * @return 返回真或者假
	 */
	public static boolean isDatable(short major) {
		return major == Major.SUCCESSFUL_DATA
				|| major == Major.FAULTED_DATA;
	}

}
