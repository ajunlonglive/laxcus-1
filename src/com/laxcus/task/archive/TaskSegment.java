/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件分段。<br>
 * 
 * 分布任务组件分段由根命名和子命名组成，根命名是必须的，子命名是可选。
 * 
 * @author scott.liang
 * @version 1.1 1/23/2016
 * @since laxcus 1.0
 */
public final class TaskSegment implements Classable, Serializable, Cloneable, Comparable<TaskSegment> {

	private static final long serialVersionUID = 1879994979180331994L;

	/** 命名正则表达式 **/
	private static final String NAMING_FULL = "^\\s*(\\w+)\\s*[\\,\\.]\\s*(\\w+)\\s*$";
	private static final String NAMING_ROOT = "^\\s*(\\w+)\\s*$";

	/** 根命名，必选项 **/
	private Naming root;

	/** 子命名，可选项。用于迭代环境下 **/
	private Naming sub;

	/**
	 * 构造分布任务组件名
	 */
	private TaskSegment() {
		super();
	}

	/**
	 * 构造一个分布任务组件分段，指定根命名
	 * @param root 根命名
	 */
	public TaskSegment(Naming root) {
		this();
		setRoot(root);
	}

	/**
	 * 构造一个分布任务组件分段，指定根命名和子命名
	 * @param root 根命名
	 * @param sub 子命名
	 */
	public TaskSegment(Naming root, Naming sub) {
		this(root);
		setSub(sub);
	}

	/**
	 * 构造分布任务组件分段，解析分布组件名
	 * @param input 包含根命令和子命名的字符串
	 */
	public TaskSegment(String input) {
		this();
		split(input);
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件分段
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskSegment(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回任务根命名
	 * @return Naming实例
	 */
	public Naming getRoot() {
		return root;
	}

	/**
	 * 返回任务根命名的文本描述
	 * @return 根命名的字符串
	 */
	public String getRootText() {
		return root.toString();
	}

	/**
	 * 设置根命名，根命名不可以是空指针
	 * @param e 命名
	 */
	public void setRoot(Naming e) {
		Laxkit.nullabled(e);

		root = e.duplicate();
	}

	/**
	 * 设置阶段根命名
	 * @param e 根命名对象，不允许是空指针或者空符符串
	 */
	public void setRoot(String e) {
		if (e == null || e.trim().isEmpty()) {
			throw new NullPointerException("cannot be null");
		}
		root = new Naming(e);
	}

	/**
	 * 返回任务子命名(子命名非必要存在)
	 * @return Naming实例
	 */
	public Naming getSub() {
		return sub;
	}

	/**
	 * 返回任务子命名的文本描述，允许空指针
	 * @return 子命名字符串
	 */
	public String getSubText() {
		if (sub == null) {
			return null;
		}
		return sub.toString();
	}

	/**
	 * 设置子命名，子命名允许空指针
	 * @param e Naming实例
	 */
	public void setSub(Naming e) {
		sub = e;
	}

	/**
	 * 设置子命名
	 * @param text 命名的字符串描述
	 */
	public void setSub(String text) {
		if(text == null || text.trim().isEmpty()) {
			sub = null;
		} else {
			sub = new Naming(text);
		}
	}

	/**
	 * 从输入参数中解析参数，建立阶段命名
	 * @param input 命名描述
	 */
	private void split(String input) {
		// 全命名格式(根命名和子命名)
		Pattern pattern = Pattern.compile(TaskSegment.NAMING_FULL);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			root = new Naming(matcher.group(1));
			sub = new Naming(matcher.group(2));
			return;
		}

		// 根命名格式
		pattern = Pattern.compile(TaskSegment.NAMING_ROOT);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			root = new Naming(matcher.group(1));
			return;
		}

		throw new IllegalPhaseException("illegal task name \"%s\"", input);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (sub != null) {
			return String.format("%s.%s", root, sub);
		} else {
			return root.toString();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskSegment that) {
		if (that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(root, that.root);
		if (ret == 0) {
			ret = Laxkit.compareTo(sub, that.sub);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 1. 根命名，这个必须有
		writer.writeObject(root);
		// 2. 子命名，这个选择性存在
		writer.writeInstance(sub);
		// 生成的字节数组长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 1.根命名，这个必须有
		root = new Naming(reader);
		// 2.子命名，这个选择性存在
		sub = reader.readInstance(Naming.class);
		// 返回解析长度
		return reader.getSeek() - seek;
	}

}