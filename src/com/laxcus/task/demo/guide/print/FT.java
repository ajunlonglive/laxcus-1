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
class FT {

	public final static Sock print = new Sock("TICKER", "PRINT");

	public final static Sock track = new Sock("TICKER", "TRACK");

	// 输入
	public final static String inputSites = "并行节点数";

	public final static String inputTitle = "标题";

	public final static String inputWidth = "表格宽度";

	// NEAR阶段参数
	public final static String nearTitle = "title";

	public final static String nearWidth = "width";
}
