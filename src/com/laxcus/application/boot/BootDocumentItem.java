/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

/**
 * 文档配置单元
 * 
 * @author scott.liang
 * @version 1.0 6/29/2021
 * @since laxcus 1.0
 */
public class BootDocumentItem  {

	/** 位置 **/
	private BootLocation bootLocation;
	
	/** 打开的命令 **/
	private String openCommand;
	
	/**
	 * 构造默认的文档配置单元
	 */
	public BootDocumentItem() {
		super();
	}

//	/**
//	 * 设置文档
//	 * @param s
//	 */
//	public void setName(String s) {
//		name = s;
//	}
//
//	/**
//	 * 返回文档
//	 * @return
//	 */
//	public String getName() {
//		return name;
//	}

	/**
	 * 设置文件位置
	 * @param s
	 */
	public void setLocation(BootLocation s) {
		bootLocation = s;
	}

	/**
	 * 返回文件位置
	 * @return
	 */
	public BootLocation getLocation() {
		return bootLocation;
	}
	
	/**
	 * 设置打开的命令
	 * @param s
	 */
	public void setOpenCommand(String s) {
		openCommand = s;
	}

	/**
	 * 返回打开的命令
	 * @return
	 */
	public String getOpenCommand() {
		return openCommand;
	}

}