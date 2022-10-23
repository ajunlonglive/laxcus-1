/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.run;

import javax.swing.*;

import com.laxcus.application.manage.*;
import com.laxcus.util.*;

/**
 * 多选择命令
 * 
 * @author scott.liang
 * @version 1.0 8/13/2021
 * @since laxcus 1.0
 */
class MultiSelectCommand {

	/** 标题 **/
	private String title;

	/** 提示 **/
	private String tooltip;

	/** 图标 **/
	private ImageIcon icon;
	
	/** KEY值 **/
	private WKey key;
	
	/** 命令名称 **/
	private String command;
	
	/**
	 * 构造多选择命令
	 */
	public MultiSelectCommand() {
		super();
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

	public void setIcon(ImageIcon e){
		icon = e;
	}
	
	public ImageIcon getIcon(){
		return icon;
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
	 * 设置命令名称
	 * @param s
	 */
	public void setCommand(String s) {
		command = s;
	}

	/**
	 * 返回命令名称
	 * @return
	 */
	public String getCommand() {
		return command;
	}
}
