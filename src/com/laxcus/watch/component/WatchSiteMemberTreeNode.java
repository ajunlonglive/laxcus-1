/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import javax.swing.tree.DefaultMutableTreeNode;

import com.laxcus.util.*;

/**
 * 用户成员的子节点
 * 
 * @author scott.liang
 * @version 1.0 1/13/2020
 * @since laxcus 1.0
 */
public class WatchSiteMemberTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -7455746046329218606L;

	/** 根节点类型 **/
	private int family;

	/** 用户签名 **/
	private Siger siger;

	/** 用户明文 **/
	private String plainText;
	
	/**
	 * 构造用户签名的树节点，指定用户签名
	 * @param siger 用户签名
	 */
	public WatchSiteMemberTreeNode(int family, Siger siger) {
		super();
		setFamily(family);
		setSiger(siger);
	}

	public boolean isRegisterMember() {
		return family == WatchSiteMemberRootTreeNode.REGISTER_MEMBER;
	}
	
	public boolean isOnlineMember() {
		return family == WatchSiteMemberRootTreeNode.ONLINE_MEMBER;
	}

	/**
	 * 设置根节点类型
	 * @param who
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
	 * 设置用户签名
	 * @param e
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return
	 */
	public Siger getSiger() {
		return siger;
	}
	
	/**
	 * 设置用户名称的明文，允许空指针
	 * @param e 明文
	 */
	public void setPlainText(String e) {
		plainText = e;
	}

	/**
	 * 返回用户名称的明文
	 * @return 字符串
	 */
	public String getPlainText() {
		return plainText;
	}

	/**
	 * 返回显示文本，首选明文用户名称，再选用户签名
	 * @return 字符串
	 */
	public String getText() {
		if (plainText != null) {
			return plainText;
		}
		if (siger != null) {
			return siger.toString();
		}
		return "";
	}

	/**
	 * 返回节点类型描述
	 * @return
	 */
	public String getDescription() {
		return getText();
	}

}