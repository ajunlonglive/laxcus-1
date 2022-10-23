/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;

import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 分布站点的类型或者地址单元
 * 
 * @author scott.liang
 * @version 1.0 12/8/2012
 * @since laxcus 1.0
 */
public class WatchMemberBrowserListCellRenderer extends JLabel implements TreeCellRenderer {

	private static final long serialVersionUID = 2909452960955966987L;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/**
	 * 分布站点参数
	 */
	public WatchMemberBrowserListCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化界面参数
	 */
	private void init() {
		loadImages();
		loadColor();
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(5);
		setOpaque(true);
		//		setBorder(new EmptyBorder(2, 2, 2, 2));
	}

	/**
	 * 加载颜色
	 */
	private void loadColor() {
		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
	}

	/** 成员图标 **/
	private Icon register;
	private Icon online;
	private Icon register_member;
	private Icon online_member;

	/**
	 * 初始化图标
	 */
	private void loadImages() {
		ResourceLoader loader = new ResourceLoader("conf/watch/image/member/");

		int width = 16, height = 16;

		register = loader.findImage("register.png", width, height);
		online = loader.findImage("online.png", width, height);
		register_member = loader.findImage("register_member.png", width, height);
		online_member = loader.findImage("online_member.png", width, height);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		if (value == null) {
			setIcon(null);
			setText("");
			return this;
		}

		// 字体
		setFont(tree.getFont());

		// 前景/背景
		if (hasFocus || selected) {
			setForeground(textSelectForeground);
			setBackground(textSelectBackground);
		} else {
			setForeground(textForeground);
			setBackground(textBackground);
		}

		if (value.getClass() == WatchSiteMemberRootTreeNode.class) {
			WatchSiteMemberRootTreeNode e = (WatchSiteMemberRootTreeNode)value;
			Icon icon = null;
			if (e.isRegisterMember()) {
				icon = register;
			} else if (e.isOnlineMember()) {
				icon = online;
			}

			// 设置图标
			setIcon(icon);

//			FontKit.setLabelText(this, e.getDescription());
			
			setText(e.getDescription());
			FontKit.setToolTipText(this, e.getDescription());
		} else if (value.getClass() == WatchSiteMemberTreeNode.class) {
			WatchSiteMemberTreeNode e = (WatchSiteMemberTreeNode) value;
			Icon icon = null;

			if (e.isRegisterMember()) {
				icon = register_member;
			} else if (e.isOnlineMember()) {
				icon = online_member;
			}

			// 设置图标
			setIcon(icon);

//			FontKit.setLabelText(this, e.getText());
			
			setText(e.getText());
			FontKit.setToolTipText(this, e.getText());
		} else {
			setIcon(null);
			setText("");
		}

		setComponentOrientation(tree.getComponentOrientation());
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setEnabled(tree.isEnabled());

		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		loadColor();
		super.updateUI();
	}

	//	/**
	//	 * Sets the color the text is drawn with when the node is selected.
	//	 */
	//	public void setTextSelectForeground(Color newColor) {
	//		textSelectForeground = newColor;
	//	}
	//
	//	/**
	//	 * Returns the color the text is drawn with when the node is selected.
	//	 */
	//	public Color getTextSelectForeground() {
	//		return textSelectForeground;
	//	}
	//
	//	/**
	//	 * Sets the color the text is drawn with when the node isn't selected.
	//	 */
	//	public void setTextForeground(Color newColor) {
	//		textForeground = newColor;
	//	}
	//
	//	/**
	//	 * Returns the color the text is drawn with when the node isn't selected.
	//	 */
	//	public Color getTextForeground() {
	//		return textForeground;
	//	}
	//
	//	/**
	//	 * Sets the color to use for the background if node is selected.
	//	 */
	//	public void setTextSelectBackground(Color newColor) {
	//		textSelectBackground = newColor;
	//	}
	//
	//	/**
	//	 * Returns the color to use for the background if node is selected.
	//	 */
	//	public Color getTextSelectBackground() {
	//		return textSelectBackground;
	//	}
	//
	//	/**
	//	 * Sets the background color to be used for non selected nodes.
	//	 */
	//	public void setTextBackground(Color newColor) {
	//		textBackground = newColor;
	//	}
	//
	//	/**
	//	 * Returns the background color to be used for non selected nodes.
	//	 */
	//	public Color getTextBackground() {
	//		return textBackground;
	//	}



}