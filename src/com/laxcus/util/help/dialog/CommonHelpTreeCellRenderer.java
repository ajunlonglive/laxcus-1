/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help.dialog;

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
public class CommonHelpTreeCellRenderer extends JLabel implements TreeCellRenderer {

	private static final long serialVersionUID = 2909452960955966987L;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 图标 **/
	private Icon close;
	private Icon open;
	private Icon command;

	/**
	 * 分布站点参数
	 */
	public CommonHelpTreeCellRenderer() {
		super();
		init();
		setIconTextGap(5);
	}

	/**
	 * 加载颜色
	 */
	private void loadColor() {
		//		textForeground = Skins.findTreeTextForeground();
		//		textBackground = Skins.findTreeTextBackground();
		//		textSelectForeground = Skins.findTreeTextSelectForeground();
		//		textSelectBackground = Skins.findTreeTextSelectBackground();

		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
	}

	/**
	 * 初始化界面参数
	 */
	private void init() {
		setHorizontalAlignment(JLabel.LEFT);
		// 加载颜色
		loadColor();
		setOpaque(true);
		
		//		textForeground = Skins.findTreeTextForeground();
		//		textBackground = Skins.findTreeTextBackground();
		//		textSelectForeground = Skins.findTreeTextSelectForeground();
		//		textSelectBackground = Skins.findTreeTextSelectBackground();

		//		setTextSelectionColor(UIManager.getColor(Skins.Tree_selectionForeground)); // "Tree.selectionForeground"));
		//		setTextNonSelectionColor(UIManager.getColor(Skins.Tree_textForeground)); // "Tree.textForeground"));
		//		setBackgroundSelectionColor(UIManager.getColor(Skins.Tree_selectionBackground)); // "Tree.selectionBackground"));
		//		setBackgroundNonSelectionColor(UIManager.getColor(Skins.Tree_textBackground)); // "Tree.textBackground"));
	}

	/**
	 * 设置图标组
	 * @param closeIcon
	 * @param openIcon
	 * @param commandIcon
	 */
	public void setIcons(Icon closeIcon, Icon openIcon, Icon commandIcon) {
		close = closeIcon;
		open = openIcon;
		command = commandIcon;
		
//		System.out.printf("%s %s %s\n", close!=null , open!=null, command!=null);
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

		setFont(tree.getFont());

		if (hasFocus || selected) {
			setForeground(textSelectForeground);
			setBackground(textSelectBackground);
		} else {
			setForeground(textForeground);
			setBackground(textBackground);
		}

		if (value.getClass() == CommonCommentGroupTreeNode.class) {
			CommonCommentGroupTreeNode e = (CommonCommentGroupTreeNode) value;
			Icon icon = (expanded ? open : close);

			// 设置图标
			setIcon(icon);

			// 显示文本/提示信息
			FontKit.setLabelText(this, e.getText());
			FontKit.setToolTipText(this, e.getText());

		} else if (value.getClass() == CommonCommentElementTreeNode.class) {
			CommonCommentElementTreeNode e = (CommonCommentElementTreeNode) value;

			setIcon(command);

			// 显示文本和提示
			FontKit.setLabelText(this, e.getText());
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

	/**
	 * Sets the color the text is drawn with when the node is selected.
	 */
	public void setTextSelectForeground(Color newColor) {
		textSelectForeground = newColor;
	}

	/**
	 * Returns the color the text is drawn with when the node is selected.
	 */
	public Color getTextSelectForeground() {
		return textSelectForeground;
	}

	/**
	 * Sets the color the text is drawn with when the node isn't selected.
	 */
	public void setTextForeground(Color newColor) {
		textForeground = newColor;
	}

	/**
	 * Returns the color the text is drawn with when the node isn't selected.
	 */
	public Color getTextForeground() {
		return textForeground;
	}

	/**
	 * Sets the color to use for the background if node is selected.
	 */
	public void setTextSelectBackground(Color newColor) {
		textSelectBackground = newColor;
	}

	/**
	 * Returns the color to use for the background if node is selected.
	 */
	public Color getTextSelectBackground() {
		return textSelectBackground;
	}

	/**
	 * Sets the background color to be used for non selected nodes.
	 */
	public void setTextBackground(Color newColor) {
		textBackground = newColor;
	}

	/**
	 * Returns the background color to be used for non selected nodes.
	 */
	public Color getTextBackground() {
		return textBackground;
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
	//	 * Overridden for performance reasons.
	//	 * See the <a href="#override">Implementation Note</a>
	//	 * for more information.
	//	 */
	//	public void validate() {}
	//
	//	/*
	//	 * Overridden for performance reasons.
	//	 * See the <a href="#override">Implementation Note</a>
	//	 * for more information.
	//	 */
	//	public void invalidate() {}
	//
	//	/**
	//	 * Overridden for performance reasons.
	//	 * See the <a href="#override">Implementation Note</a>
	//	 * for more information.
	//	 */
	//	@Override
	//	public void revalidate() {}
	//
	//	/**
	//	 * Overridden for performance reasons.
	//	 * See the <a href="#override">Implementation Note</a>
	//	 * for more information.
	//	 */
	//	public void repaint(long tm, int x, int y, int width, int height) {}
	//
	//	/**
	//	 * Overridden for performance reasons.
	//	 * See the <a href="#override">Implementation Note</a>
	//	 * for more information.
	//	 */
	//	public void repaint(Rectangle r) {}
	//
	//	/**
	//	 * Overridden for performance reasons.
	//	 * See the <a href="#override">Implementation Note</a>
	//	 * for more information.
	//	 *
	//	 * @since 1.5
	//	 */
	//	public void repaint() {}
	//
	//	/*
	//	 * 重画界面，这个很重要！
	//	 * @see javax.swing.JLabel#updateUI()
	//	 */
	//	@Override
	//	public void updateUI() {
	//		super.updateUI();
	//		init();
	//	}
}