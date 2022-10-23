/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.laxcus.util.*;

/**
 * 菜单构造器
 * 根据输入KEY值，从UIReader中获得
 * 
 * @author scott.liang
 * @version 1.0 1/27/2022
 * @since laxcus 1.0
 */
public class ApplicationMenuBuilder {
	
	/**
	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
	 * @param menu 菜单栏
	 * @param method 方法名
	 * @return 返回菜单项，如果没有找到是空值
	 */
	public static JMenuItem findMenuItemByMethod(JMenu menu, String method) {
		Component[] elements = menu.getMenuComponents();
		int size = (elements == null ? 0 : elements.length);
		// 判断对象
		for (int index = 0; index < size; index++) {
			Component element = elements[index];
			// 判断是JMenu，递归，取它的子级
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				JMenuItem item = findMenuItemByMethod((JMenu) element, method);
				if (item != null) {
					return item;
				}
			} 
			// 判断是JMenuItem，取出名字，判断参数一致
			else if(Laxkit.isClassFrom(element, JMenuItem.class) ) {
				JMenuItem item = (JMenuItem)element;
				String text = item.getName();
				if (method.equals(text)) {
					return item;
				}
			}
		}

		return null;
	}
	
	/**
	 * 根据方法名称，查找菜单项
	 * @param menubar 菜单条
	 * @param method 方法名称
	 * @return 返回匹配方法名称的菜单项目，没有是空指针
	 */
	public static JMenuItem findMenuItemByMethod(JMenuBar menubar, String method) {
		int count = menubar.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = menubar.getComponent(index);
			if (element == null) {
				continue;
			}
			// 判断是JMenu
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				JMenuItem item = findMenuItemByMethod((JMenu) element, method);
				if (item != null) {
					return item;
				}
			}
		}
		return null;
	}
	
	/**
	 * 取出菜单项
	 * @param menubar
	 * @param method
	 * @return
	 */
	public static JMenuItem findMenuItemByMethod(JPopupMenu menubar, String method) {
		int count = menubar.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = menubar.getComponent(index);
			if (element == null) {
				continue;
			}
			// 判断是JMenu
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				JMenuItem item = findMenuItemByMethod((JMenu) element, method);
				if (item != null) {
					return item;
				}
			}
			// 判断是JMenuItem，取出名字，判断参数一致
			else if(Laxkit.isClassFrom(element, JMenuItem.class) ) {
				JMenuItem item = (JMenuItem)element;
				String text = item.getName();
				if (method.equals(text)) {
					return item;
				}
			}
		}
		return null;
	}

	/** 以下是菜单 **/

	/**
	 * 返回快捷键
	 * @param shortcutKey
	 * @return
	 */
	private static KeyStroke getShortcut(String shortcutKey) {
		if (shortcutKey == null) {
			return null;
		}
		// 快捷键
		String text = UIReader.getString(shortcutKey);
		if (text != null) {
			return KeyStroke.getKeyStroke(text);
		}
		return null;
	}

	/**
	 * 返回助记符
	 * @param mnemonicKey
	 * @return
	 */
	private static char getMnemonic(String mnemonicKey) {
		if (mnemonicKey == null) {
			return 0;
		}
		String ws = UIReader.getString(mnemonicKey);
		if (ws == null || ws.length() == 0) {
			return 0;
		}

		ws = ws.trim();
		if (ws.length() > 0) {
			return ws.charAt(0);
		}
		return 0;
	}

	/**
	 * 判断是字符
	 * @param w
	 * @return
	 */
	private static boolean isWord(char w) {
		return (w >= 'A' && w <= 'Z') || (w >= 'a' && w <= 'z')
				|| (w >= '0' && w <= '9');
	}

	/**
	 * 生成菜单项
	 * @param item
	 * @param iconKey
	 * @param textKey
	 * @param mnemonicKey
	 * @param shortcutKey
	 * @param methodKey
	 * @param al
	 * @param ml
	 * @return
	 */
	public static JMenuItem createMenuItem(JMenuItem item, String iconKey, String textKey, 
			String mnemonicKey, String shortcutKey, String methodKey, ActionListener al, MenuListener ml) {
		// 图标
		if (iconKey != null) {
			Icon icon = UIReader.getIcon(iconKey);
			if (icon != null) {
				item.setIcon(icon);
				item.setIconTextGap(4);
			}
		}
		// 生成文本
		if (textKey != null) {
			String text = UIReader.getString(textKey);
			if (text != null) {
				item.setText(text);
			}
		}
		// 助记符
		char w = getMnemonic(mnemonicKey);
		if (isWord(w)) {
			item.setMnemonic(w);
		}
		// 快捷键
		KeyStroke ks = getShortcut(shortcutKey);
		if (ks != null) {
			item.setAccelerator(ks);
		}
		// 方法名
		if (methodKey != null) {
			String name = UIReader.getString(methodKey);
			if (name != null) {
				item.setName(name);
			}
		}
		
		// 菜单单击事件
		if (al != null) {
			item.addActionListener(al);
		}
		// 如果是菜单时...
		if (Laxkit.isClassFrom(item, JMenu.class) && ml != null) {
			((JMenu) item).addMenuListener(ml);
		}
		
		// 默认有效
		item.setEnabled(true);

		// 边框
		//		item.setBorder(new EmptyBorder(3, 4, 3, 4));

//		item.setBorder(new EmptyBorder(4, 4, 4, 4));

		return item;
	}

	/**
	 * 生成菜单
	 * @param iconKey
	 * @param textKey
	 * @param mnemonicKey
	 * @param shortcutKey
	 * @param listener
	 * @param methodKey
	 * @return
	 */
	public static JMenuItem createMenuItem(String iconKey, String textKey,
			String mnemonicKey, String shortcutKey, String methodKey, ActionListener listener) {

		JMenuItem item = new JMenuItem();
		return createMenuItem(item, iconKey, textKey, mnemonicKey, shortcutKey,
				methodKey, listener, null);
	}
	
	/**
	 * 生成菜单
	 * @param iconKey
	 * @param textKey
	 * @param mnemonicKey
	 * @param shortcutKey
	 * @param listener
	 * @param methodKey
	 * @return
	 */
	public static JMenu createMenu(String iconKey, String textKey,
			String mnemonicKey, String shortcutKey, String methodKey,
			ActionListener listener, MenuListener ml) {

		return (JMenu) createMenuItem(new JMenu(), iconKey, textKey,
				mnemonicKey, shortcutKey, methodKey, listener, ml);
	}

}