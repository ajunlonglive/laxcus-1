/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 启动程序令牌
 * 
 * @author scott.liang
 * @version 1.0 8/8/2020
 * @since laxcus 1.0
 */
public class GuideToken extends CloudToken implements Comparable<GuideToken> {

	/** 软件命名 **/
	private String naming;

	/** 启动入口类  **/
	private String bootClass;

	/** 图标路径，在JAR中 **/
	private String icon;

	/** 引导标题 **/
	private String caption;

	/** 工具提示 **/
	private String tooltip;

	/**
	 * 判断数据有效
	 * @return 真或者假
	 */
	public boolean isEnabled() {
		boolean success = isEnabled(naming);
		if (success) {
			success = isEnabled(bootClass);
		}
		if (success) {
			success = isEnabled(icon);
		}
		if (success) {
			success = isEnabled(caption);
		}
		return success;
	}

	/**
	 * 判断一个参数有效
	 * @param e 参数
	 * @return 真或者假
	 */
	private boolean isEnabled(String e) {
		return e != null && e.trim().length() > 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(naming);
		writer.writeString(bootClass);
		writer.writeString(icon);
		writer.writeString(caption);
		writer.writeString(tooltip);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		naming = reader.readString();
		bootClass = reader.readString();
		icon = reader.readString();
		caption = reader.readString();
		tooltip = reader.readString();
	}

	/**
	 * 构造默认的启动程序令牌
	 */
	public GuideToken() {
		super();
	}

	/**
	 * 构造默认的启动程序令牌
	 * @param naming 命名
	 */
	public GuideToken(String naming) {
		this();
		setNaming(naming);
	}

	/**
	 * 构造默认的启动程序令牌
	 */
	public GuideToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成启动程序令牌
	 * @param that 传入类
	 */
	private GuideToken(GuideToken that) {
		this();
		naming = that.naming;
		bootClass = that.bootClass;
		icon = that.icon;
		caption = that.caption;
		tooltip = that.tooltip;
	}

	/**
	 * 设置命名
	 * @param e
	 */
	public void setNaming(String e) {
		Laxkit.nullabled(e);
		naming = e.trim();
	}

	/**
	 * 返回命名
	 * @return
	 */
	public String getNaming() {
		return naming;
	}

	/**
	 * 设置引导类
	 * @param e
	 */
	public void setBootClass(String e) {
		Laxkit.nullabled(e);
		bootClass = e;
	}

	/**
	 * 返回引导类
	 * @return
	 */
	public String getBootClass() {
		return bootClass;
	}

	/**
	 * 设置图标路径
	 * @param e
	 */
	public void setIcon(String e) {
		Laxkit.nullabled(e);
		icon = e;
	}

	/**
	 * 返回图标路径
	 * @return
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * 设置标题
	 * @param e
	 */
	public void setCaption(String e) {
		Laxkit.nullabled(e);
		caption = e;
	}

	/**
	 * 返回标题
	 * @return
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * 设置工具提示
	 * @param e
	 */
	public void setTooltip(String e) {
		tooltip = e;
	}

	/**
	 * 返回工具提示
	 * @return
	 */
	public String getTooltip(){
		return tooltip;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.command.cloud.task.CloudToken#buildXML()
//	 */
//	@Override
//	public String buildXML() {
//		StringBuilder bf = new StringBuilder();
//		bf.append(formatXML_CDATA("naming", getNaming()));
//		bf.append(formatXML_CDATA("boot-class", getBootClass()));
//		bf.append(formatXML_CDATA("icon", getIcon()));
//		bf.append(formatXML_CDATA("caption", getCaption()));
//		bf.append(formatXML_CDATA("tooltip", getTooltip()));
//		// 保存一段
//		return formatXML("guide", bf.toString());
//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#buildXML()
	 */
	@Override
	public String buildXML() {
		StringBuilder bf = new StringBuilder();
		bf.append(formatXML_CDATA(GuideMark.GUIDE_NAMING, getNaming()));
		bf.append(formatXML_CDATA(GuideMark.GUIDE_CLASS, getBootClass()));
		bf.append(formatXML_CDATA(GuideMark.GUIDE_ICON, getIcon()));
		bf.append(formatXML_CDATA(GuideMark.GUIDE_CAPTION, getCaption()));
		bf.append(formatXML_CDATA(GuideMark.GUIDE_TOOLTIP, getTooltip()));
		// 保存一段
		return formatXML(GuideMark.GUIDE, bf.toString());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#duplicate()
	 */
	@Override
	public GuideToken duplicate() {
		return new GuideToken(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return naming;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		}
		return compareTo((GuideToken) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return naming.toLowerCase().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GuideToken that) {
		if( that == null ) {
			return 1;
		}

		return naming.compareToIgnoreCase(that.naming);
	}

}