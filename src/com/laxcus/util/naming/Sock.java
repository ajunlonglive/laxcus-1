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
 * 任务组件基础字，标记组件的基本属性。<br>
 * 
 * 任务组件基础字由软件名称、组件根命名组成，软件名称和组件根命名都是必须的。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2020
 * @since laxcus 1.0
 */
public final class Sock implements Classable, Serializable, Cloneable, Comparable<Sock> {

	private static final long serialVersionUID = 8012245587175395611L;
	
	/** 基础字正则表达式， 软件名称和根名称组成，中间用点号分开，软件名称限制在16个字符。 **/
	private static final String PREFIX = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]{1,16}?)\\s*[\\.]\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";
	
	/** "SYSTEM/SYSTEMS"关键字正则表达式 **/
	public final static String SYSTEM_REGEX = "^\\s*(?i)(SYSTEM|SYSTEMS)\\s*$";

	/** 系统级组件。软件中的命令："SYSTEM"是保留字，用户不能使用。 **/
	public static final Naming SYSTEM_WARE = new Naming("SYSTEM");

	/** 软件名称 **/
	private static final String REGEX_WARE = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]{1,16}?)\\s*$";
	
	/** 根命名，包括两种：1. 根命名和子命名的组件. 2. 只有根命名 **/
	private static final String SUFFIX_FULL = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*[\\.]\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";
	private static final String SUFFIX_SIMPLE = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";

	/** 软件名称，必选项 **/
	private Naming ware;

	/** 组件根命名，必选项 **/
	private Naming root;

	/**
	 * 构造任务组件名
	 */
	private Sock() {
		super();
	}

	/**
	 * 构造任务组件基础字的副本
	 * @param that 原本
	 */
	private Sock(Sock that) {
		this();
		if (that.ware != null) {
			ware = that.ware.duplicate();
		}
		if (that.root != null) {
			root = that.root.duplicate();
		}
	}

	/**
	 * 生成
	 * @param ware
	 * @param root
	 */
	protected Sock(Naming ware, Naming root) {
		this();
		setWare(ware);
		setRoot(root);
	}
	
	/**
	 * 构造一个任务组件基础字，指定根命名
	 * @param ware 软件名称
	 * @param suffixInput 组件根命令
	 */
	public Sock(Naming ware, String suffixInput) {
		this();
		setWare(ware);
		setRoot(splitSuffix(suffixInput));
	}

	/**
	 * 构造一个任务组件基础字，指定组件根命名
	 * @param ware 软件名称
	 * @param suffixInput 组件根命名
	 */
	public Sock(String wareInput, String suffixInput) {
		this();
		setWare(splitWare(wareInput));
		setRoot(splitSuffix(suffixInput));
	}

	/**
	 * 从可类化数据读取器中解析任务组件基础字
	 * @param reader 可类化数据读取器
	 */
	public Sock(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造组件基座，解析组件基座
	 * @param input 包含软件名称和组件根命名的字符串表述
	 */
	public Sock(String input) {
		this();
		split(input);
	}

	/**
	 * 返回软件名称
	 * @return Naming实例
	 */
	public Naming getWare() {
		return ware;
	}

	/**
	 * 返回软件名称的文本描述
	 * @return 软件名称的字符串
	 */
	public String getWareText() {
		return ware.toString();
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
	 * 判断是系统级应用。
	 * 如果WARE是“SYSTEM”关键字，即是系统应用！
	 * @return 返回真或者假
	 */
	public boolean isSystemLevel() {
		return ware != null && ware.toString().matches(Sock.SYSTEM_REGEX);
	}
	
	/**
	 * 判断是用户层应用
	 * @return 返回真或者假
	 */
	public boolean isUserLevel() {
		return !isSystemLevel();
	}

	/**
	 * 设置软件名称
	 * @param e 软件名称对象，不允许是空指针或者空符符串
	 */
	public void setWare(String e) {
		if (e == null || e.trim().isEmpty()) {
			throw new NullPointerException("ware is null!");
		}
		ware = new Naming(e);
	}

	/**
	 * 返回组件根命名
	 * @return Naming实例
	 */
	public Naming getRoot() {
		return root;
	}

	/**
	 * 返回组件根命名的文本描述
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

		root = e;
	}

	/**
	 * 设置阶段根命名
	 * @param e 根命名对象，不允许是空指针或者空符符串
	 */
	public void setRoot(String e) {
		if (e == null || e.trim().isEmpty()) {
			throw new NullPointerException("root is null");
		}
		root = new Naming(e);
	}
	
	/**
	 * 解析任务组件基础字，包括软件名和组件名
	 * @param input 输入语句
	 * @return 返回Sock
	 * @throws SyntaxException 如果语句错误时...
	 */
	private void split(String input) {
		Pattern pattern = Pattern.compile(Sock.PREFIX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new IllegalValueException("illegal ware name \"%s\"", input);
		}
		setWare(matcher.group(1));
		setRoot(matcher.group(2));
	}
	
	/**
	 * 判断有效
	 * @param input 字符串
	 * @return 返回真或者假
	 */
	public static boolean validate(String input) {
		Pattern pattern = Pattern.compile(Sock.PREFIX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析软件名称
	 * @param input 输入
	 * @return 命名
	 */
	private Naming splitWare(String input) {
		// 全命名格式(根命名和子命名)
		Pattern pattern = Pattern.compile(Sock.REGEX_WARE);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return new Naming(matcher.group(1));
		}

		throw new IllegalPhaseException("illegal ware name \"%s\"", input);
	}
	
	/**
	 * 解析后缀，存在“根命名.子命名”和“根命名”两种可能
	 * @param input 输入语句
	 * @return Naming命名实例
	 */
	private Naming splitSuffix(String input) {
		// 全命名格式(根命名和子命名)
		Pattern pattern = Pattern.compile(Sock.SUFFIX_FULL);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return new Naming(matcher.group(1));
		}

		// 根命名格式
		pattern = Pattern.compile(Sock.SUFFIX_SIMPLE);
		matcher = pattern.matcher(input);
		if (matcher.matches()) { 
			return new Naming(matcher.group(1));
		}

		throw new IllegalPhaseException("illegal task name \"%s\"", input);
	}

	/**
	 * 生成一个新的深层数据副本。
	 * @return Sock 当前的数据副本
	 */
	public Sock duplicate() {
		return new Sock(this);
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
		return String.format("%s.%s", ware, root);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ware.hashCode() ^ root.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Sock that) {
		if (that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(ware, that.ware);
		if (ret == 0) {
			ret = Laxkit.compareTo(root, that.root);
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
		// 2. 根命名，这个必须有
		writer.writeObject(root);
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
		// 2.根命名，这个必须有
		root = new Naming(reader);
		// 返回解析长度
		return reader.getSeek() - seek;
	}

	/**
	 * 生成系统级的任务组件基础字。
	 * 软件名称默认是“SYSTEM”
	 * @param root 根命名
	 * @return 返回Sock实例
	 */
	public static Sock doSystemSock(Naming root) {
		Laxkit.nullabled(root);
		return new Sock(Sock.SYSTEM_WARE, root);
	}

	/**
	 * 生成系统级的任务组件基础字。
	 * 软件名称默认是“SYSTEM”
	 * @param root 根命名
	 * @return 返回Sock实例
	 */
	public static Sock doSystemSock(String root) {
		Laxkit.nullabled(root);
		return Sock.doSystemSock(new Naming(root));
	}
}