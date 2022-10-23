/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help.dialog;

import javax.swing.tree.*;

import com.laxcus.util.*;
import com.laxcus.util.help.*;

/**
 * 命令解释集的树节点
 * 
 * @author scott.liang
 * @version 1.0 9/10/2018
 * @since laxcus 1.0
 */
public class CommonCommentGroupTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 5730832459787425837L;

	/** 命令解释集 **/
	private CommentGroup group;

	/**
	 * 构造编号的树节点，指定命令解释集
	 * @param group 命令解释集
	 */
	public CommonCommentGroupTreeNode(CommentGroup group) {
		super();
		setGroup(group);
	}

	/**
	 * 设置命令解释集
	 * @param e 命令解释集
	 */
	public void setGroup(CommentGroup e) {
		Laxkit.nullabled(e);
		group = e;
	}

	/**
	 * 返回命令解释集
	 * @return 命令解释集
	 */
	public CommentGroup getGroup() {
		return group;
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return group.getNo();
	}

	/**
	 * 返回文本描述
	 * 
	 * @return
	 */
	public String getText() {
		return group.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return getText();
	}

}