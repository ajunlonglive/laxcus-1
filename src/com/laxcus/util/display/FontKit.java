/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;
import java.util.*;

import javax.accessibility.*;
import javax.swing.*;
import javax.swing.text.*;

import com.laxcus.util.*;

/**
 * 窗口字体工具
 * 
 * @author scott.liang
 * @version 1.0 10/20/2018
 * @since laxcus 1.0
 */
public class FontKit {

	/** 固定字体类型 **/
	public final static String[] fontNames = new String[] { Font.MONOSPACED,
		Font.SANS_SERIF, Font.DIALOG_INPUT, Font.SERIF, Font.DIALOG };

	/**
	 * 判断字体能够正确显示一行文本
	 * @param font 字体
	 * @param str 文本
	 * @return 返回真或者假
	 */
	public static boolean canDisplay(Font font, String str) {
		boolean success = (font != null && str != null);
		if (success) {
			return font.canDisplayUpTo(str) == -1;
		}
		return false;

		// return font != null && font.canDisplayUpTo(str) == -1;
	}

	/**
	 * 判断有没有指定的字体
	 * @param fontName 字体名称
	 * @return 返回真或者假 
	 */
	public static boolean hasFontName(String fontName) {
		// 从集合中找匹配
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font font : fonts) {
			String name = font.getName();
			if (name != null && name.equalsIgnoreCase(fontName)) {
				return true;
			}
		}
		return false;
	}

//	/**
//	 * 从系统的字体集合中找到全部可显示的字体
//	 * @param text 文本
//	 * @return 返回匹配文本显示的字体，没有返回空指针
//	 */
//	public static Font[] findFonts(String text) {
//		ArrayList<Font> array = new ArrayList<Font>();
//
//		// 从集合中找匹配
//		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//		for (Font font : fonts) {
//			if (FontKit.canDisplay(font, text)) {
//				array.add(font);
//			}
//		}
//		if (array.isEmpty()) {
//			return null;
//		}
//		// 找到字体
//		Font[] all = new Font[array.size()];
//		return array.toArray(all);
//	}

	/**
	 * 从系统的字体集合中找到全部可显示的字体
	 * @param text 文本
	 * @return 返回匹配文本显示的字体，没有返回空数组
	 */
	public static Font[] findFonts(String text) {
		ArrayList<Font> array = new ArrayList<Font>();
		
		// 有效字体名称
		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (String name : names) {
			Font font = new Font(name, Font.PLAIN, 12);
			if (FontKit.canDisplay(font, text)) {
				array.add(font);
			}
		}

//		// 从集合中找匹配
//		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//		for (Font font : fonts) {
//			if (FontKit.canDisplay(font, text)) {
//				array.add(font);
//			}
//		}
//		if (array.isEmpty()) {
//			return null;
//		}
		
		// 找到字体
		Font[] fonts = new Font[array.size()];
		return array.toArray(fonts);
	}

	/**
	 * 根据组件找到匹配的字体
	 * @param component 组件
	 * @param text 待显示的文件
	 * @return 返回匹配的组件
	 */
	public static Font findFont(Component component, String text) {
		// 组件匹配的字体！
		Font font = component.getFont();
		if (FontKit.canDisplay(font, text)) {
			return new Font(font.getName(), Font.PLAIN, font.getSize());
		}

		// 取名称，过滤前面的:"J"
		String name = component.getClass().getSimpleName();
		if (name.charAt(0) == 'J') {
			name = name.substring(1);
		}
		// 生成名字，如: "Button.font", "ToolTip.font", "PasswordField.font"
		String fontName = String.format("%s.font", name);
		// 从内存中找到匹配组件的字体
		font = UIManager.getFont(fontName);
		if (FontKit.canDisplay(font, text)) {
			return new Font(font.getName(), Font.PLAIN, font.getSize());
		}

		// 从集合中找匹配
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font sub : fonts) {
			if (FontKit.canDisplay(sub, text)) {
				return new Font(sub.getName(), Font.PLAIN, sub.getSize());
			}
		}
		return null;
	}

	/**
	 * 从字体集合中找到可显示的字体
	 * @param source 原字体
	 * @param text 文本
	 * @return 返回匹配文本显示的字体，没有返回空指针
	 */
	public static Font findFont(Font source, String text) {
		// 从默认的字体中找到合适的字体
		int size = (source != null ? source.getSize() : 12);
		for(String name : FontKit.fontNames) {
			Font font = new Font(name, Font.PLAIN, size);
			if (FontKit.canDisplay(font, text)) {
				return font;
			}
		}

		// 按纽字体
		Font defaultFont = UIManager.getFont("Button.font");
		if (FontKit.canDisplay(defaultFont, text)) {
			return new Font(defaultFont.getName(), Font.PLAIN, size);
		}

		// 从集合中找匹配
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font font : fonts) {
			if (FontKit.canDisplay(font, text)) {
				return new Font(font.getName(), Font.PLAIN, size);
			}
		}
		return null;
	}

	//	/**
	//	 * 从字体集合中找到可显示的字体
	//	 * @param source 原字体
	//	 * @param text 文本
	//	 * @return 返回匹配文本显示的字体，没有返回空指针
	//	 */
	//	public static Font findFont(Font source, String text) {
	////		// 标题字体
	////		Font defaultFont = UIManager.getFont("TitledBorder.font");
	////		if (FontKit.canDisplay(defaultFont, text)) {
	////			int size = (source != null ? source.getSize() : 12);
	////			return new Font(defaultFont.getName(), Font.PLAIN, size);
	////		}
	////		
	////		// 按纽字体
	////		defaultFont = UIManager.getFont("Button.font");
	////		if (FontKit.canDisplay(defaultFont, text)) {
	////			int size = (source != null ? source.getSize() : 12);
	////			return new Font(defaultFont.getName(), Font.PLAIN, size);
	////		}
	//		
	//		// 从默认的字体中找到合适的字体
	//		int size = (source != null ? source.getSize() : 12);
	//		for(String family : FontKit.families) {
	//			Font font = new Font (family, Font.PLAIN, size);
	//			if(FontKit.canDisplay(font, text)) {
	//				return font;
	//			}
	//		}
	//		
	//		// 从集合中找匹配
	//		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	//		for (Font font : fonts) {
	//			if (FontKit.canDisplay(font, text)) {
	////				int size = (source != null ? source.getSize() : 12);
	//				return new Font(font.getName(), Font.PLAIN, size);
	//			}
	//		}
	//		return null;
	//	}

	//	/**
	//	 * 设置标签文本
	//	 * @param label 标签
	//	 * @param text 显示文本
	//	 */
	//	public static void setLabelText(JLabel label, String text) {
	//		Font font = label.getFont();
	//		// 如果不能正确显示字体，从本地字体中找
	//		if (!FontKit.canDisplay(font, text)) {
	//			// 选择匹配的字体
	//			Font dest = FontKit.findFont(label, text);
	//			if (dest != null) {
	//				label.setFont(dest);
	//			}
	//		}
	//		label.setText(text);
	//	}
	//
	//	/**
	//	 * 设置按纽文本
	//	 * @param button 按纽
	//	 * @param text 显示文本
	//	 */
	//	public static void setButtonText(AbstractButton button, String text) {
	//		Font font = button.getFont();
	//		// 如果不能正确显示字体，从本地字体中找
	//		if (!FontKit.canDisplay(font, text)) {
	//			// 选择匹配的字体
	//			Font dest = FontKit.findFont(button, text);
	//			if (dest != null) {
	//				button.setFont(dest);
	//			}
	//		}
	//		button.setText(text);
	//	}
	//
	//	/**
	//	 * 设置文本框中的文本
	//	 * @param component 组件
	//	 * @param text 显示文本
	//	 */
	//	public static void setFieldText(JTextComponent component, String text) {
	//		Font font = component.getFont();
	//		// 如果不能正确显示字体，从本地字体中找
	//		if (!FontKit.canDisplay(font, text)) {
	//			// 选择匹配的字体
	//			Font dest = FontKit.findFont(component, text);
	//			if (dest != null) {
	//				component.setFont(dest);
	//			}
	//		}
	//		component.setText(text);
	//	}
	//
	//	/**
	//	 * 设置标题文本
	//	 * @param component 组件
	//	 * @param text 显示文本
	//	 */
	//	public static void setToolTipText(JComponent component, String text) {
	//		Font font = component.getFont();
	//		// 如果不能正确显示字体，从本地字体中找
	//		if (!FontKit.canDisplay(font, text)) {
	//			// 选择匹配的字体
	//			Font dest = FontKit.findFont(component, text);
	//			if (dest != null) {
	//				component.setFont(dest);
	//			}
	//		}
	//		component.setToolTipText(text);
	//	}



	/**
	 * 设置标签文本
	 * @param label 标签
	 * @param text 显示文本
	 */
	public static void setLabelText(JLabel label, String text) {
		// 设置字体
		FontKit.setDefaultFont(label, "Label.font", text);

		label.setText(text);
	}

	/**
	 * 设置按纽文本
	 * @param button 按纽
	 * @param text 显示文本
	 */
	public static void setButtonText(AbstractButton button, String text) {
		// 设置字体
		FontKit.setDefaultFont(button, "Button.font", text);

		button.setText(text);
	}

	/**
	 * 设置文本框中的文本
	 * @param component 组件
	 * @param text 显示文本
	 */
	public static void setFieldText(JTextComponent component, String text) {
		FontKit.setDefaultFont(component, "TextField.font", text);

		component.setText(text);
	}

	/**
	 * 设置标题文本
	 * @param component 组件
	 * @param text 显示文本
	 */
	public static void setToolTipText(JComponent component, String text) {
		// 工具提示
		if (text == null || text.trim().isEmpty()) {
			component.setToolTipText("");
			if (component.getAccessibleContext() != null) {
				component.getAccessibleContext().setAccessibleDescription("");
			}
			return;
		}

		// 字符串备份在setAccessibleDescription里面
		if (component.getAccessibleContext() != null) {
			component.getAccessibleContext().setAccessibleDescription(text);
		}
		// 有区别的设置字体
		Font font = UIManager.getFont("ToolTip.font");
		if (font == null) {
			component.setToolTipText(text);
		} else {
			String tooltip = String.format("<html><body><font face=\"%s\">%s</font></body></html>",
					font.getName(), text);
			component.setToolTipText(tooltip);
		}
	}
	
	/**
	 * 更新工具提示，采用指定的字体显示
	 * @param component 组件
	 */
	public static void updateToolTipText(JComponent component) {
		if (component == null) {
			return;
		}
		// 1. 先从描述中取出提示文本
		AccessibleContext ac = component.getAccessibleContext();
		String text = (ac != null ? ac.getAccessibleDescription() : null);
		// 2. 如果不成立，取出组件的提示文本
		if (text == null || text.trim().isEmpty()) {
			text = component.getToolTipText();
		}
		if (text != null && text.trim().length() > 0) {
			FontKit.setToolTipText(component, text);
		}
	}

	/**
	 * 根据类实例，返回字体
	 * @param clazz
	 * @return
	 */
	public static Font findFont(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		// 类名
		String name = clazz.getSimpleName();
		if (name.length() > 0) {
			char w = name.charAt(0);
			if (w == 'J') {
				name = name.substring(1);
			}
			// 找到匹配的字体
			String key = String.format("%s.font", name);
			Font font = UIManager.getFont(key);
			if (font != null) {
				return font;
			}
		}

		return FontKit.findFont(clazz.getSuperclass());
	}

	/**
	 * 设置组件默认的字体
	 * @param component
	 * @return
	 */
	public static boolean setDefaultFont(Component component) {
		// 找到字体
		Font font = FontKit.findFont(component.getClass());
		boolean success = (font != null);
		if (success) {
			Font rs = new Font(font.getName(), font.getStyle(), font.getSize());
			component.setFont(rs);
		}
		return success;
	}

	/**
	 * 设置默认的字体
	 * @param component
	 * @param key
	 * @param showText
	 */
	public static void setDefaultFont(Component component, String key, String showText) {
		// 找到默认的字体
		boolean success = FontKit.setDefaultFont(component);
		// 成功，退出
		if (success) {
			return;
		}

		// 返回系统规定的字体
		Font font = UIManager.getFont(key);
		if (font != null) {
			Font rs = new Font(font.getName(), font.getStyle(), font.getSize());
			component.setFont(rs);
		} else {
			font = FontKit.findFont(component, showText);
			// 字体不匹配时，选择返回的字体
			if (font != null && !font.equals(component.getFont())) {
				component.setFont(font);
			}
		}
	}

//	/**
//	 * 更新字体
//	 * 从根窗口开始，扩展到子窗口
//	 * 
//	 * @param root 根窗体
//	 * @param updateUI 更新UI界面
//	 */
//	public static void updateDefaultFonts(Component root, boolean updateUI) {
//		if (root == null) {
//			return;
//		}
//
//		// 设置字体
//		FontKit.setDefaultFont(root);
//		// 不是容器，返回
//		if (!Laxkit.isClassFrom(root, Container.class)) {
//			return;
//		}
//		// 更新UI
//		if (updateUI) {
//			if (Laxkit.isClassFrom(root, JComponent.class)) {
//				((JComponent) root).updateUI();
//			}
//		}
//
//		Component[] components = ((Container) root).getComponents();
//		int size = (components != null ? components.length : 0);
//		for (int i = 0; i < size; i++) {
//			Component component = components[i];
//			// 设置默认的字体
//			if (component == null) {
//				continue;
//			}
//			FontKit.updateDefaultFonts(component, updateUI);
//		}
//	}

	/**
	 * 设置子菜单参数
	 * @param menu
	 * @param defaultFont
	 * @param border
	 * @param gap
	 */
	private static void updateMenu(JMenu menu, boolean updateUI) {
		// 设置字体
		FontKit.setDefaultFont(menu);
		
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
				// 更新
				FontKit.setDefaultFont(item);
				FontKit.updateToolTipText(item);
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(component, JMenu.class)) {
				FontKit.updateMenu((JMenu) component, updateUI);
			}
		}
	}
	
	/**
	 * 更新字体
	 * 从根窗口开始，扩展到子窗口
	 * 
	 * @param root 根窗体
	 * @param updateUI 更新UI界面
	 */
	public static void updateDefaultFonts(Component root, boolean updateUI) {
		if (root == null) {
			return;
		}

		// 设置字体
		FontKit.setDefaultFont(root);
		// 不是容器，返回
		if (!Laxkit.isClassFrom(root, Container.class)) {
			return;
		}
		// 更新UI
		if (updateUI) {
			if (Laxkit.isClassFrom(root, JComponent.class)) {
				((JComponent) root).updateUI();
			}
		}
		
		Container container = (Container) root;
		int count = container.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component component = container.getComponent(index);
			
			// 设置默认的字体
			if (component == null) {
				continue;
			}

			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(component, JMenu.class)) {
				FontKit.updateMenu((JMenu) component, updateUI);
			} 
			FontKit.updateDefaultFonts(component, updateUI);
		}
	}
	
	/**
	 * 更新字体
	 * 从根窗口开始，扩展到子窗口
	 * 
	 * @param root 根窗体
	 */
	public static void updateDefaultFonts(Component root) {
		FontKit.updateDefaultFonts(root, false);
	}

	//	/**
	//	 * 更新字体
	//	 * 从根窗口开始，扩展到子窗口
	//	 * 
	//	 * @param root 根窗体
	//	 */
	//	public static void updateDefaultFonts(Component root) {
	//		if (root == null) {
	//			return;
	//		}
	//
	//		// 设置字体
	//		FontKit.setDefaultFont(root);
	//		// 不是容器，返回
	//		if (!Laxkit.isClassFrom(root, Container.class)) {
	//			return;
	//		}
	//		
	//		Component[] components = ((Container) root).getComponents();
	//		int size = (components != null ? components.length : 0);
	//		for (int i = 0; i < size; i++) {
	//			Component component = components[i];
	//			// 设置默认的字体
	//			if (component == null) {
	//				continue;
	//			}
	//			FontKit.updateDefaultFonts(component);
	//		}
	//	}

}