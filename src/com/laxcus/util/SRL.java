/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.net.*;

/**
 * 存储资源定位器 <br>
 * Store Resource Locator <br>
 * 指向CALL节点下面用户磁盘存储空间 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/22/2021
 * @since laxcus 1.0
 */
public final class SRL implements Classable, Markable, Serializable, Cloneable, Comparable<SRL> {

	private static final long serialVersionUID = -3457309517335885196L;

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(CALL)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})([\\/\\w\\W]*?)\\s*$";

	/** 节点地址，一定是CALL节点**/
	private Node node;

	/** 路径，指向一个目录或者磁盘，忽略大小写 **/
	private String path;

	/**
	 * 构造默认的存储资源定位器
	 */
	public SRL() {
		super();
	}
	
	/**
	 * 从字符串解析存储资源定位器
	 * @param input 输入的语句
	 * @throws UnknownHostException
	 */
	public SRL(String input) throws UnknownHostException {
		this();
		split(input);
	}

	/**
	 * 存储资源定位器
	 * @param node 节点地址
	 */
	public SRL(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 根据传入的存储资源定位器实例，生成它的副本
	 * @param that SRL实例
	 */
	private SRL(SRL that) {
		super();
		node = that.node.duplicate();
		path = that.path;
	}

	/**
	 * 从可类化读取器中解析存储资源定位器
	 * @param reader 可类化读取器
	 */
	public SRL(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造存储资源定位器，指定参数
	 * @param node
	 * @param path
	 */
	public SRL(Node node, String path) {
		this();
		setNode(node);
		setPath(path);
	}

	/**
	 * 构造构造存储资源定位器
	 * @param node 节点
	 * @param path 目录
	 */
	public SRL(Node node, VPath path) {
		this();
		setNode(node);
		setPath(path.toString());
	}

	/**
	 * 设置节点
	 * @param e
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);
		node = e;
	}

	/**
	 * 返回节点
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 设置路径
	 * @param s 字符串
	 */
	public void setPath(String s) {
		Laxkit.nullabled(s);
		path = s;
	}

	/**
	 * 返回路径
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 判断是根目录
	 * 即如果不定义目录，或者只有一个"/"符号时，属于根目录
	 * @return 是或者否
	 */
	public boolean isRootPath() {
		return path == null || path.equals("/");
	}
	
	/**
	 * 设置路径
	 * @param parent 父目录
	 * @param name 名称
	 */
	public void setPath(String parent, String name) {
		if (parent != null && parent.length() > 0) {
			char last = parent.charAt(parent.length() - 1);
			if (last != '/') {
				path = parent + "/" + name;
			} else {
				path = parent + name;
			}
		} else {
			path = name;
		}
	}

	/**
	 * 返回父路径
	 * @return
	 */
	public String getParentPath() {
		if (path == null || path.trim().isEmpty()) {
			return null;
		}
		int last = path.lastIndexOf("/");
		if (last >= 0) {
			return path.substring(0, last);
		}
		return null;
	}

	/**
	 * 返回SRL指向的名称
	 * @return 字符串或者空指针
	 */
	public String getName() {
		if (path == null || path.trim().isEmpty()) {
			return null;
		}
		int last = path.lastIndexOf("/");
		if (last >= 0) {
			return path.substring(last + 1);
		}
		return null;
	}

	/**
	 * 返回当前对象的完整数据副本
	 * @return Node实例
	 */
	public SRL duplicate(){
		return new SRL(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SRL.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SRL) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (path != null) {
			return node.hashCode() ^ path.hashCode();
		} else {
			return node.hashCode();
		}
	}

	/**
	 * 返回节点的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (path != null) {
			return String.format("%s%s", node.toString(), path);
		} else {
			return node.toString();
		}
	}

	/**
	 * 返回Node对象的的浅层副本
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
	public int compareTo(SRL that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(path, that.path, true);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(node);
		writer.writeString(path);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		node = new Node(reader);
		path = reader.readString();
		return reader.getSeek() - seek;
	}

	/**
	 * 解析通用格式
	 * @param input 输入语句
	 * @return 成功返回真否则假
	 * @throws UnknownHostException
	 */
	private void split(String input) throws UnknownHostException {
		Pattern pattern = Pattern.compile(SRL.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配返回假
		if (!matcher.matches()) {
			throw new UnknownHostException("illegal SRL! " + input);
		}

		// 解析IP地址
		String name = matcher.group(2);
		Address address = new Address(name);
		// TCP/UDP端口号
		int tcport = Integer.parseInt(matcher.group(3));
		int udport = Integer.parseInt(matcher.group(4));

		// 设置参数
		SiteHost host = new SiteHost(address, tcport, udport);
		node = new Node(SiteTag.CALL_SITE, host);

		// 文件路径
		String s = matcher.group(5);
		if (s != null && s.length() > 0) {
			path = s;
		}
	}
	
	/**
	 * 生成一个SRL实例
	 * @param input 字符串
	 * @return 返回SRL实例
	 * @throws UnknownHostException
	 */
	public static SRL toSRL(String input) throws UnknownHostException {
		return new SRL(input);
	}

	/**
	 * 判断格式有效
	 * @param input 输入的字符串
	 * @return 真或者假
	 */
	public static boolean validate(String input) {
		// 不是有效格式，返回假
		if (input == null || input.isEmpty()) {
			return false;
		}

		// 正则表达式（不带级别）
		Pattern pattern = Pattern.compile(SRL.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
//	public static void main(String[] args) {
//		Node node = new Node(SiteTag.ACCOUNT_SITE); //"call://192.18.12.11:6500_9800");
//		SRL srl = new SRL(node, "/root/unix/sub.txt");
//		String parent = srl.getParentPath();
//		String suffix = srl.getName();
//		
//		System.out.printf("%s -> %s\n", parent, suffix);
//		
//		srl.setPath(parent, suffix + ".w3c");
//		System.out.println(srl.getPath());
//	}
}
