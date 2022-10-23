/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dock;

import javax.swing.*;

/**
 * 关联的菜单
 * 
 * @author scott.liang
 * @version 1.0 10/6/2021
 * @since laxcus 1.0
 */
class DockMenuItem extends JMenuItem {
	
	private static final long serialVersionUID = -8505758901287031273L;
	
	/** 关联按纽 **/
	private DockButton button;

	public void setDockButton(DockButton e) {
		this.button = e;
	}

	public DockButton getDockButton() {
		return this.button;
	}

	/**
	 * 
	 */
	public DockMenuItem() {
		super();
	}

	/**
	 * @param arg0
	 */
	public DockMenuItem(Icon arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DockMenuItem(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DockMenuItem(Action arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DockMenuItem(String arg0, Icon arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DockMenuItem(String arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
