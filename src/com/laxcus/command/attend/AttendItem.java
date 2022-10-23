/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 签到单元。<br>
 * 记录异步调用器的来源和是否处理。
 * 
 * @author scott.liang
 * @version 1.0 3/18/2017
 * @since laxcus 1.0
 */
public final class AttendItem implements Classable, Serializable, Cloneable, Comparable<AttendItem> {
	
	private static final long serialVersionUID = 3064577107398007244L;

	/** 调用器来源地址 **/
	private Cabin cabin;
	
	/** 已经投递 **/
	private boolean post;

	/**
	 * 构造默认和私有的签到单元
	 */
	private AttendItem() {
		super();
		post = false;
	}

	/**
	 * 生成一个签到单元数据副本
	 * @param that AttendItem实例
	 */
	private AttendItem(AttendItem that) {
		this();
		cabin = that.cabin;
		post = that.post;
	}

	/**
	 * 建立签到单元，指定源头调用器地址
	 * @param from 调用器来源地址
	 */
	public AttendItem(Cabin from) {
		this();
		setCabin(from);
	}
	
	/**
	 * 建立签到单元，指定源头调用器地址
	 * @param from 调用器来源地址
	 * @param post 投递标记
	 */
	public AttendItem(Cabin from, boolean post) {
		this(from);
		setPost(post);
	}

	/**
	 * 从可类化数据读取器中解析签到单元
	 * @param reader 可类化数据读取器
	 */
	public AttendItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置调用器来源地址
	 * @param e Cabin实例
	 */
	public void setCabin(Cabin e) {
		Laxkit.nullabled(e);

		cabin = e;
	}

	/**
	 * 返回调用器来源地址
	 * @return Cabin实例
	 */
	public Cabin getCabin() {
		return cabin;
	}

	/**
	 * 设置投递标记
	 * @param b 投递标记
	 */
	public void setPost(boolean b) {
		post = b;
	}

	/**
	 * 判断已经投递
	 * @return 返回真或者假
	 */
	public boolean isPost() {
		return post;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return 返回AttendItem实例
	 */
	public AttendItem duplicate() {
		return new AttendItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttendItem that) {
		if (that == null) {
			return 1;
		}
		// 判断地址一致
		return Laxkit.compareTo(cabin, that.cabin);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(cabin);
		writer.writeBoolean(post);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		cabin = new Cabin(reader);
		post = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}