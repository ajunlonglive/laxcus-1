/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.guide.print;

import com.laxcus.util.naming.*;

/**
 * 标记参数
 * 
 * @author scott.liang
 * @version 1.0 7/31/2020
 * @since laxcus 1.0
 */
class MinerFT {
	
	public final static Sock sock = new Sock("TICKER", "MINER");

//	CONDUCT TICKER.MINING FROM SITES:2; PREFIX(STRING)='PentiumIX';
//	BEGIN(LONG)=0;END(LONG)=5000000; ZEROS(INT)=2; GPU(BOOL)=YES; 
//	TO SITES:2; PUT NODE(CHAR)='挖矿节点'; TEXT(CHAR)='明文'; SHA256(CHAR)='矿码';
	
	// 输入
	public final static String inputDistantSites = "分布节点数"; // 数字

	public final static String inputDistantPrefix = "前缀关键字"; // 字符串
	
	public final static String inputDistantZeros = "前置0位"; // 数字
	
	public final static String inputDistantGPU = "使用GPU加速"; // 布尔值

	public final static String inputDistantBegin = "初始值"; // 数字
	
	public final static String inputDistantEnd = "结束值"; // 数字
	
	
//	public final static String inputToSites = "计算节点数";
	
//	public final static String inputMineSite = "";
	
//	public final static String putNodes = "NODE"; // "挖矿节点";
//	public final static String putText = "TEXT"; // "矿码明文";
//	public final static String putSHA256 = "SHA256"; // "矿码(SHA25)";

//	public final static Sock print = new Sock("TICKER", "PRINT");
//
//	public final static Sock track = new Sock("TICKER", "TRACK");
//
//	// 输入
//	public final static String inputSites = "并行节点数";

//	public final static String inputTitle = "标题";
//
//	public final static String inputWidth = "表格宽度";
	
	/** 显示在NEAR阶段 **/
	public final static String codeWidth = "散列码宽度";

	public final static String textWidth = "明文宽度";

	public final static String siteWidth = "地址宽度";
	
	public final static String codeWidthTitle = "CODE-WIDH-TITLE";
	public final static String textWidthTitle = "TEXT-WIDTH-TITLE";
	public final static String siteWidthTitle = "SITE-WIDTH-TITLE";

//	// NEAR阶段参数
//	public final static String nearTitle = "title";
//
//	public final static String nearWidth = "width";
}
