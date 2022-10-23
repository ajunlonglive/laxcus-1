/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.naming;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 任务组件分段。<br>
 * 
 * 任务组件分段由软件名称、组件根命名、组件子命名组成，软件名称和组件根命名是必须的，组件子命名是可选。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2020
 * @since laxcus 1.0
 */
public final class Tock implements Classable, Serializable, Cloneable, Comparable<Tock> {

	private static final long serialVersionUID = 2662361996510048503L;

	/** 组件根命名，包括两种：1. 组件根命名和组件子命名的组件. 2. 只有组件根命名 **/
	private static final String SUFFIX_FULL = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*[\\.]\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";
	private static final String SUFFIX_SIMPLE = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";

	/** 软件名称，必选项 **/
	private Naming ware;

	/** 组件根命名，必选项 **/
	private Naming root;
	
	/** 组件子命名，可选项。用于迭代环境下 **/
	private Naming sub;

	/**
	 * 构造任务组件名
	 */
	private Tock() {
		super();
	}

	/**
	 * 构造任务组件名的副本
	 * @param that 原本
	 */
	private Tock(Tock that) {
		this();
		if (that.ware != null) {
			ware = that.ware.duplicate();
		}
		if (that.root != null) {
			root = that.root.duplicate();
		}
		if (that.sub != null) {
			sub = that.sub;
		}
	}

	/**
	 * 构造一个任务组件分段，指定组件根命名
	 * @param ware 软件名称
	 * @param root 组件根命名
	 */
	public Tock(Naming ware, Naming root) {
		this();
		setWare(ware);
		setRoot(root);
	}

	/**
	 * 构造一个任务组件分段，指定组件根命名和组件子命名
	 * @param ware 软件名称
	 * @param root 组件根命名
	 * @param sub 组件子命名
	 */
	public Tock(Naming ware, Naming root, Naming sub) {
		this(ware, root);
		setSub(sub);
	}

	/**
	 * 构造任务组件分段，解析分布组件名
	 * @param ware 软件名称
	 * @param suffixInput 包含根命令和组件子命名的字符串
	 */
	public Tock(Naming ware, String suffixInput) {
		this();
		setWare(ware);
		split(suffixInput);
	}

	/**
	 * 从可类化数据读取器中解析任务组件分段
	 * @param reader 可类化数据读取器
	 */
	public Tock(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 返回软件名称
	 * @return Naming实例
	 */
	public Naming getWare() {
		return ware;
	}

	/**
	 * 设置软件名称，软件名称不可以是空指针
	 * @param e 命名
	 */
	public void setWare(Naming e) {
		Laxkit.nullabled(e);

		ware = e;
	}
	
	/**
	 * 返回软件名称的文本描述
	 * @return 软件名称的字符串
	 */
	public String getWareText() {
		return ware.toString();
	}

	/**
	 * 返回任务组件根命名
	 * @return Naming实例
	 */
	public Naming getRoot() {
		return root;
	}

	/**
	 * 返回任务组件根命名的文本描述
	 * @return 组件根命名的字符串
	 */
	public String getRootText() {
		return root.toString();
	}

	/**
	 * 设置组件根命名，组件根命名不可以是空指针
	 * @param e 命名
	 */
	public void setRoot(Naming e) {
		Laxkit.nullabled(e);
		root = e;
	}

	/**
	 * 设置阶段组件根命名
	 * @param e 组件根命名对象，不允许是空指针或者空符符串
	 */
	public void setRoot(String e) {
		if (e == null || e.trim().isEmpty()) {
			throw new NullPointerException("root is null");
		}
		root = new Naming(e);
	}
	
	/**
	 * 返回软件名称
	 * @return Naming实例
	 */
	public Sock getSock() {
		return new Sock(ware, root);
	}

	/**
	 * 设置软件名称
	 * @param e 软件名称对象，不允许是空指针或者空符符串
	 */
	public void setWare(String e) {
		if (e == null || e.trim().isEmpty()) {
			throw new NullPointerException("ware is null!");
		}
		setWare(new Naming(e));
	}

	/**
	 * 返回任务组件子命名(组件子命名非必要存在)
	 * @return Naming实例
	 */
	public Naming getSub() {
		return sub;
	}

	/**
	 * 返回任务组件子命名的文本描述，允许空指针
	 * @return 组件子命名字符串
	 */
	public String getSubText() {
		if (sub == null) {
			return null;
		}
		return sub.toString();
	}

	/**
	 * 设置组件子命名，组件子命名允许空指针
	 * @param e Naming实例
	 */
	public void setSub(Naming e) {
		sub = e;
	}

	/**
	 * 设置组件子命名
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
	 * @param ware 软件名称
	 * @param input 命名描述
	 */
	private void split(String input) {
		// 全命名格式(组件根命名和组件子命名)
		Pattern pattern = Pattern.compile(Tock.SUFFIX_FULL);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			root = new Naming(matcher.group(1));
			sub = new Naming(matcher.group(2));
			return;
		}

		// 组件根命名格式
		pattern = Pattern.compile(Tock.SUFFIX_SIMPLE);
		matcher = pattern.matcher(input);
		if (matcher.matches()) { 
			root = new Naming(matcher.group(1));
			return;
		}

		throw new IllegalValueException("illegal task name \"%s\"", input);
	}
	
	/**
	 * 生成一个新的深层数据副本。
	 * @return Tick 当前的数据副本
	 */
	public Tock duplicate() {
		return new Tock(this);
	}

	/*
	 * 返回当前对象的深层数据副本
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
		if (sub != null) {
			return String.format("%s.%s.%s", ware, root, sub);
		} else {
			return String.format("%s.%s", ware, root);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Tock that) {
		if (that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(ware, that.ware);
		if (ret == 0) {
			ret = Laxkit.compareTo(root, that.root);
		}
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
		// 1. 软件名称
		writer.writeObject(ware);
		// 2. 组件根命名，这个必须有
		writer.writeObject(root);
		// 3. 组件子命名，这个选择性存在
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
		// 1. 软件名称
		ware = new Naming(reader);
		// 2.组件根命名，这个必须有
		root = new Naming(reader);
		// 3.组件子命名，这个选择性存在
		sub = reader.readInstance(Naming.class);
		// 返回解析长度
		return reader.getSeek() - seek;
	}

}