/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.factory;

import javax.swing.*;

import com.laxcus.application.manage.*;
import com.laxcus.util.*;

/**
 * 启动令牌
 * 
 * @author scott.liang
 * @version 1.0 1/28/2022
 * @since laxcus 1.0
 */
public class StartToken implements Comparable<StartToken> {
	
	/** KEY **/
	private WKey key;

	/** 标题 **/
	private String title;

	/** 图标 **/
	private ImageIcon icon;

	/** 提示 **/
	private String tooltip;

	/**
	 * 构造启动令牌
	 * @param key
	 */
	public StartToken(WKey key) {
		super();
		setKey(key);
	}
	
	/**
	 * 构造启动令牌
	 * @param key
	 * @param title
	 * @param icon
	 * @param tooltip
	 */
	public StartToken(WKey key, String title, ImageIcon icon, String tooltip) {
		this(key);
		setTitle(title);
		setIcon(icon);
		setToolTip(tooltip);
	}

	/**
	 * 设置KEY
	 * @param e
	 */
	public void setKey(WKey e) {
		Laxkit.nullabled(e);
		key = e;
	}
	
	/**
	 * 返回KEY
	 * @return
	 */
	public WKey getKey(){
		return key;
	}
	
	/**
	 * 设置名称，显示在界面上的
	 * @param s
	 */
	public void setTitle(String s) {
		title = s;
	}

	/**
	 * 返回名称
	 * @return
	 */
	public String getTitle(){
		return title;
	}	

	/**
	 * 设置工具提示，显示在界面上的
	 * @param s
	 */
	public void setToolTip(String s) {
		tooltip = s;
	}

	/**
	 * 返回工具提示
	 * @return
	 */
	public String getToolTip(){
		return tooltip;
	}	

	/**
	 * 设置图标
	 * @param e
	 */
	public void setIcon(ImageIcon e){
		icon = e;
	}
	
	/**
	 * 返回图标
	 * @return
	 */
	public ImageIcon getIcon(){
		return icon;
	}

	/**
	 * 返回图像
	 * @return
	 */
	public java.awt.Image getImage() {
		if(icon != null) {
			return icon.getImage();
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((StartToken) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StartToken that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(key, that.key);
	}

}