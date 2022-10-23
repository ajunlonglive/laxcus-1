/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 边缘容器标签 <br>
 * 
 * 由边缘容器命名和类名称路径组成，描述一个边缘容器的基础属性。
 * 
 * @author scott.liang
 * @version 1.3 8/12/2016
 * @since laxcus 1.0
 */
public final class TubTag implements Serializable, Cloneable, Classable, Markable, Comparable<TubTag> {

	private static final long serialVersionUID = 6197490371303897047L;

	/** 边缘容器名称 */
	private Naming naming;

	/** 类名称路径 */
	private String clazzName;

	/** 标题 **/
	private String caption;

	/** 图标字节数组，和类文件一起打包 **/
	private byte[] icon;

	/** 文本提示 **/
	private String tooltip;

	/** 文本提示 **/
	private String startArgumentTooltip;
	
	/** 文本提示 **/
	private String stopArgumentTooltip;
	
	/** 资源配置，任意字符串 **/
	private String resource;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(naming);
		writer.writeString(clazzName);
		writer.writeString(resource);
		
		writer.writeString(caption);
		writer.writeString(tooltip);
		writer.writeString(startArgumentTooltip);
		writer.writeString(stopArgumentTooltip);
		
		writer.writeByteArray(icon);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		naming = new Naming(reader);
		clazzName = reader.readString();
		resource = reader.readString();
		
		caption = reader.readString();
		tooltip = reader.readString();
		startArgumentTooltip = reader.readString();
		stopArgumentTooltip = reader.readString();
		icon = reader.readByteArray();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that TubTubItem实例
	 */
	private TubTag(TubTag that) {
		this();
		naming = that.naming.duplicate();
		clazzName = that.clazzName;
		resource = that.resource;

		caption = that.caption;
		tooltip = that.tooltip;
		if (that.icon != null) {
			icon = Arrays.copyOfRange(that.icon, 0, that.icon.length);
		}
	}

	/**
	 * 构造默认的边缘容器标签
	 */
	private TubTag() {
		super();
	}

	/**
	 * 构造边缘容器标签，指定边缘容器和类名
	 * @param naming 命名
	 * @param clazzName 类路径名称
	 */
	public TubTag(Naming naming, String clazzName) {
		this();
		setNaming(naming);
		setClassName(clazzName);
	}

	/**
	 * 构造边缘容器标签，指定边缘容器和类名
	 * @param naming 命名
	 * @param clazzName 类路径名称
	 */
	public TubTag(String naming, String clazzName) {
		this(new Naming(naming), clazzName);
	}

	/**
	 * 从可类化数据读取器中解析边缘容器标签参数
	 * @param reader 可类化数据读取器
	 */
	public TubTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出边缘容器标签
	 * @param reader 标记化读取器
	 */
	public TubTag(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置边缘容器
	 * @param e 命名实例
	 */
	public void setNaming(Naming e) {
		Laxkit.nullabled(e);

		naming = e;
	}

	/**
	 * 返回边缘容器
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming;
	}
	
	/**
	 * 返回边缘容器字符串
	 * @return 字符串
	 */
	public String getNameText() {
		return naming.toString();
	}

	/**
	 * 设置类路径名称，不允许空指针
	 * @param e 类路径名称
	 */
	public void setClassName(String e) {
		Laxkit.nullabled(e);

		clazzName = e;
	}

	/**
	 * 返回类路径名称
	 * @return 类路径名称
	 */
	public String getClassName() {
		return clazzName;
	}

	/**
	 * 设置图标的字节数组，允许空指针
	 * @param e 字节数组
	 */
	public void setIcon(byte[] e) {
		icon = e;
	}

	/**
	 * 返回图标的字节数组
	 * @return 字节数组
	 */
	public byte[] getIcon() {
		return icon;
	}

	/**
	 * 设置标题
	 * @param e 标题
	 */
	public void setCaption(String e) {
		caption = e;
	}

	/**
	 * 返回标题
	 * @return 标题
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * 设置文本提示
	 * @param e 文本提示
	 */
	public void setTooltip(String e) {
		tooltip = e;
	}

	/**
	 * 返回文本提示
	 * @return 文本提示
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * 显示启动参数提示信息
	 * @param s
	 */
	public void setStartArgumentTooltip(String s) {
		startArgumentTooltip = s;
	}

	/**
	 * 返回启动参数提示信息
	 * @return
	 */
	public String getStartArgumentTooltip() {
		return startArgumentTooltip;
	}

	/**
	 * 显示停止参数提示信息
	 * @param s
	 */
	public void setStopArgumentTooltip(String s) {
		stopArgumentTooltip = s;
	}

	/**
	 * 返回停止参数提示信息
	 * @return
	 */
	public String getStopArgumentTooltip() {
		return stopArgumentTooltip;
	}

	/**
	 * 设置资源配置，允许空指针
	 * @param e 资源配置
	 */
	public void setResource(String e) {
		resource = e;
	}

	/**
	 * 返回资源配置
	 * @return 资源配置
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * 返回边缘容器标签的数据副本
	 * @return TubTubItem实例
	 */
	public TubTag duplicate() {
		return new TubTag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TubTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TubTag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return naming.hashCode() ^ clazzName.hashCode();
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
		return String.format("%s#%s", naming, clazzName);
	}

	/*
	 * 比较两个边缘容器标签的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TubTag that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		// 名称是唯一判断标准
		return Laxkit.compareTo(naming, that.naming);
	}
}