/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.io.*;

import javax.swing.*;

import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 运行记录
 * 
 * @author scott.liang
 * @version 1.0 8/1/2021
 * @since laxcus 1.0
 */
public final class RTElement implements Comparable<RTElement> {

	/** 启动命令 **/
	Naming command;
	
	boolean system;
	
	/** 启动类名称 **/
	String bootstrap;
	
	/** 显示图标 **/
	ImageIcon icon;
	
	/** 磁盘文件 **/
	File file;
	
	/** 内存数组 **/
	byte[] content;
	
	/** 编号 **/
	long no;
	
	/**
	 * 构造运行记录
	 */
	public RTElement(){
		super();
	}
	
	/**
	 * 构造运行记录，指定命名
	 * @param command
	 */
	public RTElement(Naming command) {
		super();
		setCommand(command);
	}

	/**
	 * 构造运行记录，指定命名
	 * @param command
	 */
	public RTElement(String command) {
		this(new Naming(command));
	}
	
	/**
	 * 设置编号
	 * @param i
	 */
	public void setNo(long i) {
		no = i;
	}

	/**
	 * 返回编号
	 * @return
	 */
	public long getNo() {
		return no;
	}

	public void setCommand(Naming e) {
		command = e;
	}

	public Naming getCommand() {
		return command;
	}
	
	public void setSystem(boolean b){
		this.system = b;
	}
	
	public boolean isSystem(){
		return this.system;
	}

	public void setBootstrap(String s) {
		bootstrap = s;
	}

	public String getBootstrap() {
		return bootstrap;
	}

	public void setIcon(ImageIcon e) {
		icon = e;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setFile(File e) {
		file = e;
	}

	public File getFile() {
		return file;
	}

	public boolean hasFile() {
		return file != null;
	}

	public void setContent(byte[] b) {
		content = b;
	}

	public byte[] getContent() {
		return content;
	}

	public boolean hasContent() {
		return content != null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return command.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		return compareTo((RTElement) o) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RTElement that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(command, that.command);
	}

}