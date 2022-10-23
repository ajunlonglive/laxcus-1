/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import javax.swing.tree.DefaultMutableTreeNode;

import com.laxcus.site.*;

/**
 * 站点地址的树节点
 * 
 * @author scott.liang
 * @version 1.0 12/8/2012
 * @since laxcus 1.0
 */
public class WatchSiteBrowserAddressTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -7455746046329218606L;

	/** 站点地址 **/
	private Node node;

	/**
	 * 构造站点地址的树节点，指定站点地址
	 * @param node
	 */
	public WatchSiteBrowserAddressTreeNode(Node node) {
		super();
		setNode(node);
	}

	/**
	 * 设置站点地址
	 * @param e
	 */
	public void setNode(Node e) {
		node = e;
	}

	/**
	 * 返回站点地址
	 * @return
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 返回显示文本
	 * @return
	 */
	public String getText() {
		if (node == null) {
			return "null node";
		}
		return node.toString();
	}

	/**
	 * 返回站点类型
	 * @return
	 */
	public byte getFamily() {
		return node.getFamily();
	}
	
	/**
	 * 返回节点类型描述
	 * @return
	 */
	public String getDescription() {
		return SiteTag.translate(node.getFamily());
	}

	/**
	 * 判断是TOP站点
	 * @return
	 */
	public boolean isTop() {
		return node.isTop();
	}

	/**
	 * 判断是WATCH站点
	 * @return
	 */
	public boolean isWatch() {
		return node.isWatch();
	}

	/**
	 * 判断是HOME站点
	 * @return
	 */
	public boolean isHome() {
		return node.isHome();
	}

	/**
	 * 判断是LOG站点
	 * @return
	 */
	public boolean isLog() {
		return node.isLog();
	}

	/**
	 * 判断是FRONT站点
	 * @return
	 */
	public boolean isFront() {
		return node.isFront();
	}

	/**
	 * 判断是CALL站点
	 * @return
	 */
	public boolean isCall() {
		return node.isCall();
	}

	/**
	 * 判断是DATA站点
	 * @return
	 */
	public boolean isData() {
		return node.isData();
	}

	/**
	 * 判断是WORK站点
	 * @return
	 */
	public boolean isWork() {
		return node.isWork();
	}

	/**
	 * 判断是BUILD站点
	 * @return 返回真或者假
	 */
	public boolean isBuild() {
		return node.isBuild();
	}

	/**
	 * 判断是BANK站点
	 * @return 返回真或者假
	 */
	public boolean isBank() {
		return node.isBank();
	}

	/**
	 * 判断是ENTRANCE站点
	 * @return 返回真或者假
	 */
	public boolean isEntrance() {
		return node.isEntrance();
	}

	/**
	 * 判断是GATE站点
	 * @return 返回真或者假
	 */
	public boolean isGate() {
		return node.isGate();
	}

	/**
	 * 判断是HASH站点
	 * @return 返回真或者假
	 */
	public boolean isHash() {
		return node.isHash();
	}

	/**
	 * 判断是ACCOUNT站点
	 * @return 返回真或者假
	 */
	public boolean isAccount() {
		return node.isAccount();
	}

}
