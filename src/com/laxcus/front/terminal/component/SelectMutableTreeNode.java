/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * 图标选中
 * 
 * @author scott.liang
 * @version 1.0 9/12/2009
 * @since laxcus 1.0
 */
public class SelectMutableTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = -7455746046329218606L;
	
	public final static int SCHEMA = 1;
	public final static int TABLE = 2;

	public final static int PRIME_FIELD = 3;
	public final static int INDEX_FIELD = 4;
	public final static int FIELD = 5;

	private int family;

	private String tooltip;

	/**
	 *
	 */
	public SelectMutableTreeNode() {
		super();
		this.family = 0;
	}

	/**
	 * @param userObject
	 */
	public SelectMutableTreeNode(Object userObject) {
		super(userObject);
		this.family = 0;
	}

	/**
	 * @param userObject
	 */
	public SelectMutableTreeNode(Object userObject, int type) {
		super(userObject);
		setFamily(type);
	}

	/**
	 * @param userObject
	 * @param allowsChildren
	 */
	public SelectMutableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
		this.family = 0;
	}

	/**
	 * @param userObject
	 * @param allowsChildren
	 * @param type
	 */
	public SelectMutableTreeNode(Object userObject, boolean allowsChildren, int type) {
		super(userObject, allowsChildren);
		setFamily(type);
	}

	public void setFamily(int i) {
		this.family = i;
	}

	public int getFamily() {
		return this.family;
	}

	public void setToolTip(String s) {
		this.tooltip = s;
	}
	public String getToolTip() {
		return this.tooltip;
	}
}
