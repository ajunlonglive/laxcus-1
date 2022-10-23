/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.from;

/**
 * 调用JNI接口，用GPU挖矿
 * 
 * @author xiaoyang.yuan
 * @version 1.0 2015-2-12
 * @since laxcus 1.0
 */
public class GPUMiner {

//	static {
//		try {
//			System.loadLibrary("gpuminer");
//		} catch (UnsatisfiedLinkError e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * 判断计算机配置GPU
	 * @return 返回真或者假
	 */
	public static native boolean hasGPU();
	
	/**
	 * 调用GPU进行挖矿
	 * 
	 * @param prefix 前缀字符
	 * @param begin 范围开始值
	 * @param end 范围结束值
	 * @param zeros SHA256编码的前缀0数目
	 * @param full 是否把范围内的矿码完全挖出
	 * @return 返回挖出的矿码和明文，没有返回空指针
	 */
	public static native byte[] enumerate(String prefix, long begin, long end,
			int zeros, boolean full);

}