/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import javax.swing.tree.*;

import com.laxcus.util.*;

/**
 * 用户成员根节点
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class WatchSiteMemberRootTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 5153782137751122633L;

	/** 注册成员 **/
	public final static int REGISTER_MEMBER = 1;
	
	/** 在线成员 **/
	public final static int ONLINE_MEMBER = 2;

	/** 根节点类型 **/
	private int family;
	
	/** 标题 **/
	private String title;

	/**
	 * 构造用户成员根节点，指定类型
	 * @param family
	 */
	public WatchSiteMemberRootTreeNode(int family, String title) {
		super();
		setFamily(family);
		setTitle(title);
	}

	/**
	 * 设置标题
	 * @param e 标题
	 */
	public void setTitle(String e) {
		title = e;
	}

	/**
	 * 输出标题
	 * @return 字符串
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 判断是注册成员
	 * @return 返回真或者假
	 */
	public boolean isRegisterMember() {
		return family == WatchSiteMemberRootTreeNode.REGISTER_MEMBER;
	}

	/**
	 * 判断是在线成员
	 * @return 返回真或者假
	 */
	public boolean isOnlineMember() {
		return family == WatchSiteMemberRootTreeNode.ONLINE_MEMBER;
	}

	/**
	 * 设置根节点类型
	 * @param who 成员
	 */
	public void setFamily(int who) {
		boolean success = false;
		switch (who) {
		case WatchSiteMemberRootTreeNode.REGISTER_MEMBER:
		case WatchSiteMemberRootTreeNode.ONLINE_MEMBER:
			success = true;
		default:
			break;
		}
		if (!success) {
			throw new IllegalValueException("illegal %d", who);
		}
		family = who;
	}

	/**
	 * 返回根节点类型
	 * @return 类型
	 */
	public int getFamily() {
		return family;
	}
	
	/**
	 * 返回节点类型描述
	 * @return 字符串
	 */
	public String getDescription() {
		return getTitle();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

}