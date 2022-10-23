/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 应用令牌
 * 
 * @author scott.liang
 * @version 1.0 8/3/2021
 * @since laxcus 1.0
 */
public class WProgram extends WElement {
	
	/** KEY值 **/
	private WKey key;
	
	/** 命令名称 **/
	private String command;
	
	/** 支持类型 **/
	private String supportTypes;
	
	/** 安装到桌面 **/
	private boolean desktop;
	
	/** 安装到应用坞 **/
	private boolean dock;
	
	/** 系统应用或者否 **/
	private boolean system;
	
	/** 在桌面显示然后被删除时，这个参数是“真” **/
	private boolean shiftout;

	/**
	 * 构造默认的应用令牌
	 */
	public WProgram() {
		super();
		desktop = false;
		dock = false;
		system = false;
		shiftout = false;
	}

	/**
	 * 生成应用令牌的数据副本
	 * @param that
	 */
	protected WProgram(WProgram that) {
		super(that);
		key = that.key;
		command = that.command;
		supportTypes = that.supportTypes;
		desktop = that.desktop;
		dock = that.dock;
		system = that.system;
		shiftout = that.shiftout;
	}

	/**
	 * 从可类化读取中解析应用令牌
	 * @param reader 可类化读取器
	 */
	public WProgram(ClassReader reader) {
		this();
		resolve(reader);
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

	/**
	 * 设置支持类型
	 * @param s
	 */
	public void setSupportTypes(String s) {
		supportTypes = s;
	}

	/**
	 * 返回支持类型
	 * @return
	 */
	public String getSupportTypes() {
		return supportTypes;
	}
	
	/**
	 * 安装到桌面
	 * @param b
	 */
	public void setDesktop(boolean b) {
		desktop = b;
	}

	/**
	 * 判断安装到桌面
	 * @return 真或者假
	 */
	public boolean isDesktop() {
		return desktop;
	}

	/**
	 * 安装到应用坞
	 * @param b
	 */
	public void setDock(boolean b) {
		dock = b;
	}

	/**
	 * 判断安装到应用坞
	 * @return 真或者假
	 */
	public boolean isDock() {
		return dock;
	}

	/**
	 * 设置为系统组件
	 * @param b
	 */
	public void setSystem(boolean b) {
		system = b;
	}

	/**
	 * 判断是系统组件
	 * @return
	 */
	public boolean isSystem() {
		return system;
	}
	
	/**
	 * 设置从桌面移除
	 * @param b
	 */
	public void setShiftout(boolean b) {
		shiftout = b;
	}

	/**
	 * 判断从桌面移除
	 * @return
	 */
	public boolean isShiftout() {
		return shiftout;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WToken that) {
		int ret = super.compareTo(that);
		// 比较KEY对象
		if (ret == 0) {
			ret = Laxkit.compareTo(that.getClass().getName(), WProgram.class.getName());
			if (ret == 0) {
				ret = Laxkit.compareTo(key, ((WProgram) that).key);
			}
		}
		return ret;
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
	 * @see com.laxcus.application.manage.WToken#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((WToken) that) == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#duplicate()
	 */
	@Override
	public WProgram duplicate() {
		return new WProgram(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(key);
		writer.writeString(command);
		writer.writeString(supportTypes);
		writer.writeBoolean(desktop);
		writer.writeBoolean(dock);
		writer.writeBoolean(system);
		writer.writeBoolean(shiftout);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		key = new WKey(reader);
		command = reader.readString();
		supportTypes = reader.readString();
		desktop = reader.readBoolean();
		dock = reader.readBoolean();
		system = reader.readBoolean();
		shiftout = reader.readBoolean();
	}

}