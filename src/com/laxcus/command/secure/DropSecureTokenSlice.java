/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.io.*;

import com.laxcus.util.naming.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 删除密钥令牌执行单元
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public final class DropSecureTokenSlice implements Serializable, Cloneable, Classable, Comparable<DropSecureTokenSlice> {

	private static final long serialVersionUID = -6644296095141805348L;

	/** 密钥令牌命名 **/
	private Naming naming;

	/** 成功标识 **/
	private boolean successful;

	/**
	 * 构造默认和私有的删除密钥令牌执行单元
	 */
	private DropSecureTokenSlice() {
		super();
		successful = false;
	}

	/**
	 * 生成删除密钥令牌执行单元数据副本
	 * @param that ReloadSecureSlice实例
	 */
	private DropSecureTokenSlice(DropSecureTokenSlice that) {
		this();
		naming = that.naming;
		successful = that.successful;
	}

	/**
	 * 构造删除密钥令牌执行单元，指定密钥令牌命名和成功标记
	 * @param naming 密钥令牌命名
	 * @param successful 成功标记
	 */
	public DropSecureTokenSlice(Naming naming, boolean successful) {
		this();
		setNaming(naming);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器解析删除密钥令牌执行单元
	 * @param reader 可类化数据读取器
	 */
	public DropSecureTokenSlice(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置密钥令牌命名
	 * @param e Naming实例
	 */
	public void setNaming(Naming e) {
		Laxkit.nullabled(e);

		naming = e;
	}

	/**
	 * 返回密钥令牌命名
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 产生数据副本
	 * @return ReloadSecureSlice实例
	 */
	public DropSecureTokenSlice duplicate() {
		return new DropSecureTokenSlice(this);
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
		if (that == null || that.getClass() != DropSecureTokenSlice.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((DropSecureTokenSlice) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return naming.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DropSecureTokenSlice that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(naming, that.naming);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(naming);
		writer.writeBoolean(successful);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		naming = new Naming(reader);
		successful = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}