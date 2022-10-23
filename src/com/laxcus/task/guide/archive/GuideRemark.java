/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.archive;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 引导备注
 * 
 * @author scott.liang
 * @version 1.0 7/26/2020
 * @since laxcus 1.0
 */
public final class GuideRemark implements Classable, Cloneable, Serializable, Comparable<GuideRemark> {

	private static final long serialVersionUID = 2975949299953438582L;

	/** 根命名 **/
	private Sock sock;
	
	/** 重定义的组件名称 **/
	private String caption;
	
	/** 重定义图标 **/
	private byte[] icon;

	/**
	 * 构造默认的被刷新处理单元
	 */
	private GuideRemark() {
		super();
	}

	/**
	 * 根据传入实例，生成引导备注的数据副本
	 * @param that BootRemark实例
	 */
	private GuideRemark(GuideRemark that) {
		super();
		sock = that.sock;
		caption = that.caption;
		icon = that.icon;
	}

	/**
	 * 构造引导备注，
	 * @param sock 根命名
	 */
	public GuideRemark(Sock sock) {
		this();
		setSock(sock);
	}

	/**
	 * 从可类化数据读取器中引导备注
	 * @param reader 可类化数据读取器
	 */
	public GuideRemark(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置根命名，不允许空值
	 * @param e 根命名
	 */
	public void setSock(Sock e) {
		Laxkit.nullabled(e);
		sock = e;
	}

	/**
	 * 返回根命名
	 * @return 站点地址
	 */
	public Sock getSock() {
		return sock;
	}
	
	/**
	 * 设置标题
	 * @param e
	 */
	public void setCaption(String e){
		Laxkit.nullabled(e);
		caption = e;
	}
	
	/**
	 * 返回标题
	 * @return 字符串
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * 成功标记
	 * @param b
	 */
	public void setIcon(byte[] b) {
		Laxkit.nullabled(b);
		icon = b;
	}

	/**
	 * 返回图标字节数组
	 * @return 图标字节数组，或者是空指针
	 */
	public byte[] getIcon() {
		return icon;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return PublishLicenceItem实例
	 */
	public GuideRemark duplicate() {
		return new GuideRemark(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((GuideRemark ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return sock.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", sock, caption);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GuideRemark that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(sock, that.sock);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数到可类化写入器
	 * @param writer 可类化数据写入器
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(sock);
		writer.writeString(caption);
		writer.writeByteArray(icon);
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		sock = new Sock(reader);
		caption = reader.readString();
		icon = reader.readByteArray();
	}
}