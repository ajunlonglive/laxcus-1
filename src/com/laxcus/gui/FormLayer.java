/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui;

/**
 * 窗口层级
 * 
 * @author scott.liang
 * @version 1.0 3/24/2022
 * @since laxcus 1.0
 */
public class FormLayer {

	/** 默认级别 **/
	public final static int DEFAULT = 0;

	/** 主窗口，和普通窗口同级 **/
	public final static int FRAME = 100;

	/** 普通窗口 **/
	public final static int NORMAL_DIALOG = 100;

	/** 模态窗口 **/
	public final static int MODAL_DIALOG = 200;

	/** 应用坞窗口 **/
	public final static int DOCK = 300;

	/** 全屏显示 **/
	public final static int FULL_SCREEN = 400;
	
	/** 顶层窗口 **/
	public final static int TOP_WINDOW = 500;
}
