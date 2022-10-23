/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.install;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;

import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
* 分布站点的类型或者地址单元
* 
* @author scott.liang
* @version 1.0 12/8/2012
* @since laxcus 1.0
*/
class InstallTreeCellRenderer extends JLabel implements TreeCellRenderer { 

	private static final long serialVersionUID = 2909452960955966987L;
	
	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;


	/**
	 * 分布站点参数
	 */
	public InstallTreeCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化界面参数
	 */
	private void init() {
		loadColor();
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(5);
		setOpaque(true);
		
//		label = new JLabel();
//		checkbox = new JCheckBox("桌面");
//		label.setHorizontalAlignment(JLabel.LEFT);
//		label.setIconTextGap(5);
//		
////		checkbox.setText("显示在桌面");
//		
//		setLayout(new BorderLayout(4, 0));
//		add(label, BorderLayout.CENTER);
//		add(checkbox, BorderLayout.EAST);
	}
	
	/**
	 * 从内存中加载颜色
	 */
	private void loadColor() {
		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		if (value == null || value.getClass() != InstallTreeNode.class) {
			setIcon(null);
			setText("");
			return this;
		}

		// 字体
		setFont(tree.getFont());

//		// 前景/背景
//		if (hasFocus || selected) {
//			setForeground(textSelectForeground);
//			setBackground(textSelectBackground);
//		} else {
//			setForeground(textForeground);
//			setBackground(textBackground);
//		}
		

		InstallTreeNode node = (InstallTreeNode) value;
		// 设置图标
		setIcon(node.getIcon());
		// 文本
		FontKit.setLabelText(this, node.getTitle());
		FontKit.setToolTipText(this, node.getTitle());

		// 设置背景和前景色
		if (selected) {
			setBackground(textSelectBackground);
			setForeground(textSelectForeground);
		} else {
			/** 
			 * 注意！这个一个JAVA的BUG！
			 * 情况：textBackground和Color.WHITE相等情况下，用setBackground(textBackground)
			 * 而不是setBackground(Color.WHITE)，背景变灰（不正常！！！），setBackground(Color.WHITE)则是显示正常。
			 * 所以采用下面办法避免故障！！！
			 **/
			if (Color.WHITE.equals(textBackground)) {
				setBackground(Color.WHITE);
			} else {
				setBackground(textBackground);
			}
			setForeground(textForeground);
		}
		
		setComponentOrientation(tree.getComponentOrientation());
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setEnabled(tree.isEnabled());
		

		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		loadColor();
	}

}

///**
//* 分布站点的类型或者地址单元
//* 
//* @author scott.liang
//* @version 1.0 12/8/2012
//* @since laxcus 1.0
//*/
//public class DesktopInstallListCellRenderer extends JLabel implements TreeCellRenderer { 
//
//	private static final long serialVersionUID = 2909452960955966987L;
//
//	/** 没选中前景/背景颜色 **/
//	private Color textForeground, textBackground;
//
//	/** 选中前景/背景颜色 **/
//	private Color textSelectForeground, textSelectBackground;
//
//
//	/**
//	 * 分布站点参数
//	 */
//	public DesktopInstallListCellRenderer() {
//		super();
//		init();
//	}
//
//	/**
//	 * 初始化界面参数
//	 */
//	private void init() {
//		loadColor();
//		setHorizontalAlignment(JLabel.LEFT);
//		setIconTextGap(5);
//	}
//	
//	/**
//	 * 从内存中加载颜色
//	 */
//	private void loadColor() {
//		textForeground = Skins.findTreeTextForeground();
//		textBackground = Skins.findTreeTextBackground();
//		textSelectForeground = Skins.findTreeTextSelectForeground();
//		textSelectBackground = Skins.findTreeTextSelectBackground();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
//	 */
//	@Override
//	public Component getTreeCellRendererComponent(JTree tree, Object value,
//			boolean selected, boolean expanded, boolean leaf, int row,
//			boolean hasFocus) {
//		
//		if (value.getClass() != InstallTreeNode.class) {
//			return this;
//		}
//
//		// 字体
//		setFont(tree.getFont());
//
//		// 前景/背景
//		if (hasFocus || selected) {
//			setForeground(textSelectForeground);
//			setBackground(textSelectBackground);
//		} else {
//			setForeground(textForeground);
//			setBackground(textBackground);
//		}
//		
//		InstallTreeNode node = (InstallTreeNode)value;
//		
//		// 设置图标
//		setIcon(node.getIcon());
//		
//		FontKit.setLabelText(this, node.getTitle());
//		FontKit.setToolTipText(this, node.getTitle());
//
//		setComponentOrientation(tree.getComponentOrientation());
//		setBorder(new EmptyBorder(4, 4, 4, 4));
//		setEnabled(tree.isEnabled());
//		setOpaque(true);
//
//		return this;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JLabel#updateUI()
//	 */
//	@Override
//	public void updateUI() {
//		loadColor();
//		super.updateUI();
//	}
//
//}