/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.guide.fast;

import com.laxcus.util.naming.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2020-7-31
 * @since laxcus 1.0
 */
public class MineFT {


	public final static Sock sock = new Sock("FAST", "MINING");

//	CONDUCT TICKER.MINING FROM SITES:2; PREFIX(STRING)='PentiumIX';
//	BEGIN(LONG)=0;END(LONG)=5000000; ZEROS(INT)=2; GPU(BOOL)=YES; 
//	TO SITES:2; PUT NODE(CHAR)='挖矿节点'; TEXT(CHAR)='明文'; SHA256(CHAR)='矿码';
	
	// 输入
	public final static String inputFromSites = "FROM节点数"; // 数字

	public final static String inputFromPrefix = "选择关键字"; // 字符串
	
	public final static String inputFromZeros = "前置0位"; // 数字
	
	public final static String inputFromGPU = "支持GPU"; // 布值值

	public final static String inputFromBegin = "开始值"; // 数字
	
	public final static String inputFromEnd = "结束值"; // 数字
	
	
	public final static String inputToSites = "TO节点数";
	
//	public final static String inputMineSite = "";
	
	public final static String putNodes = "NODE"; // "挖矿节点";
	public final static String putText = "TEXT"; // "矿码明文";
	public final static String putSHA256 = "SHA256"; // "矿码(SHA25)";
	
}
