/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.menu;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;

/**
 * 弹出菜单
 * 
 * @author scott.liang
 * @version 1.0 7/22/2021
 * @since laxcus 1.0
 */
public class RayRockMenu extends JPopupMenu {

	private static final long serialVersionUID = 8665073016897683116L;

	/** 字体 **/
	private Font defaultFont;

	/** 边框 **/
	private Border border;

	/** 间隔 **/
	private int gap = 6;
	
	/**
	 * 构造弹出菜单
	 */
	public RayRockMenu(){
		super();
	}

	public void setParameter(Border br, int gap) {
		this.border = br;
		this.gap = gap;
	}
	
	/**
	 * 给每个MenuItem设置事件监器接口
	 * @param menu
	 * @param listener
	 */
	private void setActionListener(JMenu menu, ActionListener listener) {
		Component[] elements = menu.getMenuComponents();
		int size = (elements == null ? 0 : elements.length);
		// 判断对象
		for (int index = 0; index < size; index++) {
			Component element = elements[index];
			// 给JMenuItem设置字体和边框
			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
				((JMenuItem) element).addActionListener(listener);
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				setActionListener((JMenu) element, listener);
			}
		}
	}

	/**
	 * 找到每一个MenuItem，设置事件监器接口
	 * @param menubar
	 * @param listener
	 */
	public void setActionListener(ActionListener listener) {
		int count = getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = getComponent(index);
			if (element == null) {
				continue;
			}
			// 1. 先判断是JMenuItem
			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
				((JMenuItem) element).addActionListener(listener);
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				setActionListener((JMenu) element, listener);
			}
		}
	}

	/**
	 * 定义字体
	 */
	private void doDefaultFont() {
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM");
		if (font == null) {
			font = FontKit.findFont(new javax.swing.JLabel(), "abc");
		}
		if (font == null) {
			font = new Font("dialog", Font.PLAIN, 12);
		}
		// 确定字体
		defaultFont = new Font(font.getName(), Font.PLAIN, font.getSize());
	}
	
	/**
	 * 设置子菜单参数
	 * @param menu
	 * @param defaultFont
	 * @param border
	 * @param gap
	 */
	private void setSubMenu(JMenu menu, boolean updateUI) {
		Component[] components = menu.getMenuComponents();
		int size = (components == null ? 0 : components.length);
		// 判断对象
		for (int index = 0; index < size; index++) {
			Component component = components[index];
			
			// 判断是JComponent，刷新它
			if (updateUI) {
				if (Laxkit.isClassFrom(component, JComponent.class)) {
					((JComponent) component).updateUI();
				}
			}
			
			// 给JMenuItem设置字体和边框
			if (Laxkit.isClassFrom(component, JMenuItem.class)) {
				JMenuItem item = (JMenuItem) component;
				item.setFont(defaultFont);
				item.setBorder(border);
				item.setIconTextGap(gap);
				// 更新提示
				FontKit.updateToolTipText(item);
				
//				String res = item.getAccessibleContext().getAccessibleDescription();
//				if (res != null) {
//					FontKit.setToolTipText(item, res);
//				}
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(component, JMenu.class)) {
				setSubMenu((JMenu) component, updateUI);
			}
		}
	}
	
	/**
	 * 更新字体和边框
	 */
	public void updateFontAndBorder(boolean updateUI) {
		// 生成默认字体
		doDefaultFont();
		// 更新当前UI
		if (updateUI) {
			updateUI();
		}
		
		// 设置字体
		int count = getComponentCount();
		for (int index = 0; index < count; index++) {
			Component component = getComponent(index);
			if (component == null) {
				continue;
			}

			// 1. 先判断是JComponent，刷新它
			if (updateUI) {
				if (Laxkit.isClassFrom(component, JComponent.class)) {
					((JComponent) component).updateUI();
				}
			}
			
			// 2. 先判断是JMenuItem
			if (Laxkit.isClassFrom(component, JMenuItem.class)) {
				JMenuItem item = (JMenuItem) component;
				
				item.setFont(defaultFont);
				item.setBorder(border);
				item.setIconTextGap(gap);
				String res = item.getAccessibleContext().getAccessibleDescription();
				if (res != null) {
					FontKit.setToolTipText(item, res);
				}
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(component, JMenu.class)) {
				setSubMenu((JMenu) component, updateUI);
			}
		}
		
	}

}