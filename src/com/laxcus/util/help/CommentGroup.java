/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help;

import java.util.*;

import com.laxcus.util.*;

/**
 * 命令解释集合
 * 
 * @author scott.liang
 * @version 1.0 9/10/2018
 * @since laxcus 1.0
 */
public class CommentGroup implements Comparable<CommentGroup> {
	
	/** 所属编号 **/
	private int no;
	
	/** 标题 **/
	private String title;
	
	/** 保存命令解释单元 **/
	private ArrayList<CommentElement> array = new ArrayList<CommentElement>();

	/**
	 * 构造默认的命令解释集合
	 */
	public CommentGroup() {
		super();
	}
	
	/**
	 * 构造命令解释集合，指定编号和标题
	 * @param no 编号
	 * @param title 标题 
	 */
	public CommentGroup(int no, String title) {
		this();
		setNo(no);
		setTitle(title);
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
	 * 设置标题
	 * @param e 标题
	 */
	public void setTitle(String e) {
		Laxkit.nullabled(e);
		title = e;
	}
	
	/**
	 * 返回标题 
	 * @return 标题
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * 保存命令解释单元
	 * @param e 命令解释单元
	 * @return 保存返回真，否则假
	 */
	public boolean add(CommentElement e) {
		Laxkit.nullabled(e);
		
		// 关联编号
		e.setNo(no);

		remove(e);
		return array.add(e);
	}

	/**
	 * 删除命令解释单元
	 * @param e 命令解释单元
	 * @return 删除返回真，没有返回假
	 */
	public boolean remove(CommentElement e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}

	/**
	 * 输出全部命令解释单元
	 * @return 命令解释单元列表
	 */
	public List<CommentElement> list() {
		return new ArrayList<CommentElement>(array);
	}

	/**
	 * 成员数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CommentGroup that) {
		if(that == null){
			return 1;
		}
		return Laxkit.compareTo(no, that.no);
	}

}