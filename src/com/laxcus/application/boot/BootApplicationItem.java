/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

/**
 * 启动配置单元
 * 
 * @author scott.liang
 * @version 1.0 6/29/2021
 * @since laxcus 1.0
 */
public class BootApplicationItem  { 

	/** 命令名称 **/
	private String command;
	
	/** 启动类 **/
	private String bootClass;
	
	/** 支持的类型 **/
	private String supportTypes;
	
	/**
	 * 构造启动配置单元
	 */
	protected BootApplicationItem() {
		super();
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

	/**
	 * 设置启动类
	 * @param s
	 */
	public void setBootClass(String s) {
		bootClass = s;
	}
	
	/**
	 * 返回启动类
	 * @return
	 */
	public String getBootClass() {
		return this.bootClass;
	}

	public void setSupportTypes(String s) {
		this.supportTypes = s;
	}

	public String getSupportTypes() {
		return this.supportTypes;
	}

	public String toString() {
		return String.format("%s / %s", command, bootClass);
	}
}