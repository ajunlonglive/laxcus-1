/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 左手窗口显示单元。在窗体左侧。
 * 
 * @author scott.liang
 * @version 1.3 01/17/2016
 * @since laxcus 1.0
 */
public class TerminalRemoteTreeCellRenderer extends JLabel implements TreeCellRenderer {

	private static final long serialVersionUID = 2909452960955966987L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/**
	 * 构造默认的左手窗口显示单元
	 */
	public TerminalRemoteTreeCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 定义颜色
	 */
	private void loadColor() {
		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
	}

	/**
	 * 初始化它！
	 */
	private void init() {
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(6);
		loadColor();
		setOpaque(true);
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

		// 前景/背景
		if (hasFocus || selected) {
			setForeground(textSelectForeground);
			setBackground(textSelectBackground);
		} else {
			setForeground(textForeground);
			setBackground(textBackground);
		}

		if (value.getClass() == TerminalTreeSchemaRootNode.class) {
			exhibit((TerminalTreeSchemaRootNode) value);
		} else if (value.getClass() == TerminalTreeSchemaNode.class) {
			exhibit((TerminalTreeSchemaNode) value);
		} else if (value.getClass() == TerminalTreeTableNode.class) {
			exhibit((TerminalTreeTableNode) value);
		} else if (value.getClass() == TerminalTreeAttributeNode.class) {
			exhibit((TerminalTreeAttributeNode) value);
		} 
//		else if (value.getClass() == TerminalTreePhaseRootNode.class) {
//			exhibit((TerminalTreePhaseRootNode) value);
//		} else if (value.getClass() == TerminalTreePhaseNode.class) {
//			exhibit((TerminalTreePhaseNode) value);
//		} 
//		else if (value.getClass() == TerminalTreeSwiftRootNode.class) {
//			exhibit((TerminalTreeSwiftRootNode) value);
//		} 
//		else if (value.getClass() == TerminalTreeSwiftNode.class) {
//			exhibit((TerminalTreeSwiftNode) value);
//		} 
		else if (value.getClass() == TerminalTreePassiveItemRootNode.class) {
			exhibit((TerminalTreePassiveItemRootNode) value);
		} else if (value.getClass() == TerminalTreePassiveItemNode.class) {
			exhibit((TerminalTreePassiveItemNode) value);
		} else {
			setIcon(null);
			setText("");
		}
		
		// 设置参数
		setComponentOrientation(tree.getComponentOrientation());
		
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setEnabled(tree.isEnabled());

		return this;
	}

	/**
	 * 显示数据库根节点
	 * @param value
	 */
	private void exhibit(TerminalTreeSchemaRootNode value) {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		Icon icon = loader.findImage("net_database.png");
		setIcon(icon);
//		FontKit.setLabelText(this, value.getText());
		setText(value.getText());
	}

	/**
	 * 显示数据库节点
	 * @param value
	 */
	private void exhibit(TerminalTreeSchemaNode value) {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		Icon icon = loader.findImage("database.png");
		setIcon(icon);
//		FontKit.setLabelText(this, value.getFame().toString());
		setText(value.getFame().toString());
	}

	/**
	 * 显示数据表节点
	 * @param value
	 */
	private void exhibit(TerminalTreeTableNode value) {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		Icon icon = loader.findImage("table.png");
		setIcon(icon);
//		FontKit.setLabelText(this, value.getTableName().toString());
		setText(value.getTableName().toString());
	}

	/**
	 * 显示数据表的属性
	 * @param value
	 */
	private void exhibit(TerminalTreeAttributeNode value) {
		ColumnAttribute attribute = value.getAttribute();

		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		Icon icon = null;
		if (attribute.isPrimeKey()) {
			icon = loader.findImage("prime_key.png");
		} else if (attribute.isKey()) {
			icon = loader.findImage("sub_key.png");
		} else {
			icon = loader.findImage("column.png");
		}
		setIcon(icon);
//		FontKit.setLabelText(this, attribute.getNameText());
		setText(attribute.getNameText());
	}

	/**
	 * 显示共享表的根图标
	 * @param value
	 */
	private void exhibit(TerminalTreePassiveItemRootNode value) {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		Icon icon = loader.findImage("share.png");
		setIcon(icon);
		//		FontKit.setLabelText(this, value.getText());
		setText(value.getText());
	}

	/**
	 * 显示共享表
	 * @param value
	 */
	private void exhibit(TerminalTreePassiveItemNode value) {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		PassiveItem item = value.getPassiveItem();

		Icon icon = loader.findImage("share_table.png");
		setIcon(icon);
		//		FontKit.setLabelText(this, item.getSpace().toString());
		setText(item.getSpace().toString());
		setToolTipText(item.getAuthorizer().toString());
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

}