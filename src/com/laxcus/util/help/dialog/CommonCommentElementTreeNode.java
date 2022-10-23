/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help.dialog;

import javax.swing.tree.DefaultMutableTreeNode;

import com.laxcus.util.*;
import com.laxcus.util.help.*;

/**
 * 命令解释单元的树节点
 * 
 * @author scott.liang
 * @version 1.0 9/9/2018
 * @since laxcus 1.0
 */
public class CommonCommentElementTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -7455746046329218606L;

	/** 命令解释单元 **/
	private CommentElement element;

	/**
	 * 构造命令解释单元的树节点，指定命令解释单元
	 * @param element 命令解释单元
	 */
	public CommonCommentElementTreeNode(CommentElement element) {
		super();
		setElement(element);
	}

	/**
	 * 设置命令解释单元
	 * @param e
	 */
	public void setElement(CommentElement e) {
		Laxkit.nullabled(e);
		element = e;
	}

	/**
	 * 返回命令解释单元
	 * @return
	 */
	public CommentElement getElement() {
		return element;
	}

	/**
	 * 返回显示文本
	 * @return
	 */
	public String getText() {
		return element.getCommand();
	}

}
