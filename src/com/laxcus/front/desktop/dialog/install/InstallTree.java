/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.install;

import java.util.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.tree.*;

import com.laxcus.util.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2/7/2022
 * @since laxcus 1.0
 */
class InstallTree extends JTree {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public InstallTree() {
		// TODO Auto-generated constructor stub
		super();
	}

	/**
	 * @param arg0
	 */
	public InstallTree(Object[] arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InstallTree(Vector<?> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InstallTree(Hashtable<?, ?> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InstallTree(TreeNode arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InstallTree(TreeModel arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public InstallTree(TreeNode arg0, boolean arg1) {
		super(arg0, arg1);
	}

//	InstallDialog.ExpandedIcon [ICON 10*10] conf/desktop/image/dialog/install/expanded.png
//	InstallDialog.CollapsedIcon [ICON 10*10] conf/desktop/image/dialog/install/collapsed.png

	/**
	 * 更新树形图标
	 */
	private void updateTreeIcon() {
		TreeUI u = getUI();		
		if (u != null && Laxkit.isClassFrom(u, MetalTreeUI.class)) {
			MetalTreeUI ui = (MetalTreeUI) u;
			Icon icon = UIManager.getIcon("InstallDialog.ExpandedIcon");
			ui.setExpandedIcon(icon);
			icon = UIManager.getIcon("InstallDialog.CollapsedIcon");
			ui.setCollapsedIcon(icon);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JTree#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		updateTreeIcon();
	}

}