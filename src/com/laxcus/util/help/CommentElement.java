/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.util.*;

/**
 * 命令解释单元
 * 
 * @author scott.liang
 * @version 1.0 9/9/2018
 * @since laxcus 1.0
 */
public class CommentElement implements Cloneable, Comparable<CommentElement> {

	/** 所属编号 **/
	private int no;

	/** 字符命令 **/
	private String command;

	/** 命令的正则表达式 **/
	private String regex;

//	/** 注释 **/
//	private String remark;

	/** 注释 **/
	private ArrayList<String> remark = new ArrayList<String>();

	/** 语法 **/
	private ArrayList<String> syntax = new ArrayList<String>();

	/** 参数，多行 **/
	private ArrayList<String> params = new ArrayList<String>();

	/**
	 * 构造默认的命令解释单元
	 */
	public CommentElement() {
		super();
	}

	/**
	 * 构造命令解释单元，指定字符命令
	 * @param command 字符命令 
	 */
	public CommentElement(String command) {
		this();
		setCommand(command);
	}

	/**
	 * 构造命令解释单元，指定编号和字符命令
	 * @param no 编号
	 * @param command 字符命令 
	 */
	public CommentElement(int no, String command) {
		this(command);
		setNo(no);
	}

	/**
	 * 生成命令解释单元的副本
	 * @param that 命令解释单元
	 */
	private CommentElement(CommentElement that) {
		this();
		no = that.no;
		command = that.command;
		remark = that.remark;
		syntax = that.syntax;
		params.addAll(that.params);
	}

	/**
	 * 设置编号
	 * @param i
	 */
	public void setNo(int i) {
		no = i;
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return no;
	}

	/**
	 * 设置字符命令
	 * @param e 字符命令
	 */
	public void setCommand(String e) {
		Laxkit.nullabled(e);
		command = e.trim();

		// 生成正则表达式
		String[] subs = command.split("\\s+");
		StringBuilder bf = new StringBuilder();
		for (String sub : subs) {
			if (bf.length() > 0) {
				bf.append("\\s+");
			}
			bf.append(sub);
		}
		regex = String.format("^\\s*(?i)(%s)\\s*$", bf.toString());
	}

	/**
	 * 返回字符命令 
	 * @return 字符命令
	 */
	public String getCommand(){
		return command;
	}
	
	/**
	 * 判断是非法字符
	 * @param str
	 * @return
	 */
	private boolean isIllegalWord(String str) {
		if (str == null || str.trim().isEmpty()) {
			return true;
		}
		return str.matches("^\\s*([\\*]+)\\s*$");
	}

	/**
	 * 判断是前缀字符命令
	 * @param str 字符串
	 * @return 返回真或者假
	 */
	public boolean contains(String str) {
		// 判断是非法字符
		if (isIllegalWord(str)) {
			return false;
		}
		
		// 生成正则表达式
		String[] subs = str.split("\\s+");
		StringBuilder bf = new StringBuilder();
		for (String sub : subs) {
			if (bf.length() > 0) {
				bf.append("\\s+");
			}
			bf.append(sub);
		}
		String style = String.format("^(.*)(?i)(%s)(.*)$", bf.toString());
		
		// 判断
		Pattern pattern = Pattern.compile(style);
		Matcher matcher = pattern.matcher(command);
		return matcher.matches();
	}

	/**
	 * 判断是完整的字符命令，忽略大小写
	 * @param str 字符串
	 * @return 返回真或者假
	 */
	public boolean isCommand(String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * 设置注释
	 * @param input 输入文本
	 */
	public void setRemark(String input) {
		Laxkit.nullabled(input);
		remark.addAll(split(input));
	}

	/**
	 * 返回注释
	 * @return 字符串列表
	 */
	public List<String> getRemark() {
		return new ArrayList<String>(remark);
	}

	/**
	 * 切割字符串
	 * @param input
	 * @return 输出字符串列表
	 */
	private List<String> split(String input) {
		// 切换回车换行符
		input = input.replace("\\r\\n", "\r\n");
		input = input.replace("\\n", "\r\n");

		ArrayList<String> a = new ArrayList<String>();
		try {
			StringReader str = new StringReader(input);
			BufferedReader reader = new BufferedReader(str);
			do {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				// 过滤空格
				line = line.trim();
				if (line.length() > 0) {
					a.add(line);
				}
			} while (true);
			reader.close();
			str.close();
		} catch (IOException e) {

		}
		return a;
	}

	/**
	 * 设置语法
	 * @param input
	 */
	public void setSyntax(String input) {
		syntax.addAll(split(input));
	}

	/**
	 * 返回语法
	 * @return
	 */
	public List<String> getSyntax() {
		return new ArrayList<String>(syntax);
	}

	/**
	 * 设置参数
	 * @param input
	 */
	public void setParams(String input) {
		params.addAll(split(input));
	}

	/**
	 * 返回参数
	 * @return
	 */
	public List<String> getParams() {
		return new ArrayList<String>(params);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CommentElement that) {
		if (that == null) {
			return 1;
		}
		return command.compareToIgnoreCase(that.command);
	}

	/**
	 * 生成参数副本
	 * @return 参数副本
	 */
	public CommentElement duplicate(){
		return new CommentElement(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null) {
			return false;
		} else if (that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((CommentElement) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return command.toLowerCase().hashCode();
	}
	
//	public static void main(String[] args) {
//		CommentElement e = new CommentElement("  MULTI    SWARM   ");
//		System.out.printf("command is [%s]\n",  e.getCommand());
//		System.out.printf("is command %s\n", e.isCommand(" multi swarm"));
//		System.out.printf("contains is %s\n", e.contains(" ti   s") );
//	}

}