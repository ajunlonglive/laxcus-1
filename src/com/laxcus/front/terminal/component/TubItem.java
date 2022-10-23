/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;


import java.io.*;
import javax.swing.*;

import com.laxcus.tub.servlet.*;

/**
 * 边缘应用单元。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 09/25/2009
 * @since laxcus 1.0
 */
public class TubItem implements Serializable, java.lang.Cloneable {

	private static final long serialVersionUID = 6228738008223913689L;

	/** 软件LOGO图标 **/
	private Icon icon;

	/** 阶段命名 **/
	private TubTag tag;

//	/** 显示标题 **/
//	private String title;
//	
//	/** 提示文本 **/
//	private String tooltip;

	/**
	 * 构造前端边缘应用单元
	 */
	public TubItem(TubTag phase) {
		super();
		setTubTag(phase);
	}
	
	/**
	 * 生成副本
	 * @param that
	 */
	private TubItem(TubItem that) {
		super();
		icon = that.icon;
		tag = that.tag.duplicate();
//		title = that.title;
//		tooltip = that.tooltip;
	}

	/**
	 * 设置阶段命名
	 * @param e
	 */
	public void setTubTag(TubTag e) {
		tag = e;
	}

	/**
	 * 返回阶段命名
	 * @return
	 */
	public TubTag getTubTag() {
		return tag;
	}

	/**
	 * 设置LOGO图标
	 * @param e
	 */
	public void setIcon(Icon e) {
		icon = e;
	}

	/**
	 * 返回LOGO图标
	 * @return
	 */
	public Icon getIcon() {
		return icon;
	}

//	/**
//	 * 设置名称
//	 * @param e
//	 */
//	public void setTitle(String e) {
//		title = e;
//	}
//
//	/**
//	 * 返回名称
//	 * 
//	 * @return
//	 */
//	public String getTitle() {
//		return title;
//	}
//
//	/**
//	 * 设置提示信息
//	 * @param e
//	 */
//	public void setTooltip(String e) {
//		tooltip = e;
//	}
//
//	/**
//	 * 返回提示信息
//	 * 
//	 * @return
//	 */
//	public String getTooltip() {
//		return tooltip;
//	}
	
	/**
	 * 生成副本
	 * @return
	 */
	public TubItem duplicate() {
		return new TubItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
}