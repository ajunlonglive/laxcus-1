/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.menu;

import java.awt.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.xml.*;

/**
 * 导航菜单生成器
 * 
 * @author scott.liang
 * @version 1.0 6/4/2021
 * @since laxcus 1.0
 */
public class RayLaunchMenuCreator {
	
	private final static String CAPTION = "caption";
	private final static String ID = "id";
	private final static String HOTKEY = "hotkey";
	private final static String ASSOCIATE = "associate"; // associate中的名称必须保证唯一性
	private final static String CHECKON = "checkon";
	private final static String RADIOON = "radioon";

	private final static String TOOLTIP = "ToolTip";
	private final static String SHORTCUT_KEY = "ShortcutKey";
	private final static String ICON = "Icon";
	private final static String METHOD = "Method";
	
//	/**
//	 * 给每个MenuItem设置事件监器接口
//	 * @param menu
//	 * @param listener
//	 */
//	private void setActionListener(JMenuItem item, ActionListener listener) {
//		int count = item.getComponentCount();
//		for (int index = 0; index < count; index++) {
//			Component sub = item.getComponent(index);
//			if (Laxkit.isClassFrom(sub, JMenu.class)) {
//				continue;
//			}
//			// 菜单
//			if (Laxkit.isClassFrom(sub, JMenu.class)) {
//				JMenu next = (JMenu) item;
//				setActionListener(next, listener);
//			} else if (Laxkit.isClassFrom(sub, JMenuItem.class)) {
//				setActionListener((JMenuItem) sub, listener);
//			}
//		}
//		item.addActionListener(listener);
//	}
//
//	/**
//	 * 找到每一个MenuItem，设置事件监器接口
//	 * @param root
//	 * @param listener
//	 */
//	public void setActionListener(LaunchPopupMenu root, ActionListener listener) {
//		int size = root.getComponentCount();
//		for (int i = 0; i < size; i++) {
//			Component sub = root.getComponent(i);
//			if (Laxkit.isClassFrom(sub, JMenu.class)) {
//				continue;
//			}
//			if (Laxkit.isClassFrom(sub, JMenuItem.class)) {
//				setActionListener((JMenuItem) sub, listener);
//			}
//		}
//	}

	/**
	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
	 * @param menu 菜单栏
	 * @param method 方法名
	 * @return 返回菜单项，如果没有找到是空值
	 */
	private JMenuItem findMenuItemByMethod(JMenu menu, String method) {
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
				continue;
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
		
//		int count = menu.getItemCount();
//		for (int pos = 0; pos < count; pos++) {
//			JMenuItem item = menu.getItem(pos);
//			if (item == null) {
//				continue;
//			}
//
//			// 如果包含菜单项
//			if (Laxkit.isClassFrom(item, JMenu.class)) {
//				JMenu sub = (JMenu) item;
//				item = findMenuItemByMethod(sub, method);
//				if (item != null) {
//					return item;
//				}
//				continue;
//			}
//
//			String text = item.getName();
//			if (method.equals(text)) {
//				return item;
//			}
//		}
		
		return null;
	}

	/**
	 * 根据方法名称，查找菜单项
	 * @param rayLaunchMenu 弹出菜单
	 * @param method 方法名称
	 * @return 返回匹配方法名称的菜单项目，没有是空指针
	 */
	public JMenuItem findMenuItemByMethod(RayLaunchMenu rayLaunchMenu, String method) {
		int count = rayLaunchMenu.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = rayLaunchMenu.getComponent(index);
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
		
//		int size = menubar.getMenuCount();
//		JMenuItem item = null;
//		for (int i = 0; i < size; i++) {
//			JMenu menu = menubar.getMenu(i);
//			item = findMenuItem(menu, method);
//			if (item != null) break;
//		}
//
//		return item;
	}
	
//	/**
//	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
//	 * @param menu 菜单栏
//	 * @param associate 关联字
//	 * @return 返回菜单项，如果没有找到是空值
//	 */
//	private JMenu findMenu(JMenu menu, String associate) {
//		Component[] elements = menu.getMenuComponents();
//		int size = (elements == null ? 0 : elements.length);
//		// 判断对象
//		for (int index = 0; index < size; index++) {
//			Component element = elements[index];
//			System.out.printf("%s %s\n", element.getClass().getName(), element.getName());
//			if (!Laxkit.isClassFrom(element, JMenu.class)) {
//				continue;
//			}
//			// 判断这个菜单
//			JMenu sub = (JMenu) element;
//			// 判断名称匹配
//			String name = sub.getName();
//			if (associate.equals(name)) {
//				return sub;
//			}
//			// 查找子级
//			sub = findMenu(sub, associate);
//			if (sub != null) {
//				return sub;
//			}
//		}
//		return null;
//	}
	
	/**
	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
	 * @param menu 菜单栏
	 * @param associate 关联字
	 * @return 返回菜单项，如果没有找到是空值
	 */
	private static JMenu findMenu(JMenu menu, String associate) {
		String name = menu.getName();
		if (associate.equals(name)) {
			return menu;
		}
		
		Component[] elements = menu.getMenuComponents();
		int size = (elements == null ? 0 : elements.length);
		// 判断对象
		for (int index = 0; index < size; index++) {
			Component element = elements[index];
			if (!Laxkit.isClassFrom(element, JMenu.class)) {
				continue;
			}
			// 判断这个菜单
			JMenu sub = (JMenu) element;
			// 判断名称匹配
			name = sub.getName();
			if (associate.equals(name)) {
				return sub;
			}
			// 查找子级
			sub = findMenu(sub, associate);
			if (sub != null) {
				return sub;
			}
		}
		return null;
	}
	
	/**
	 * 查找菜单
	 * @param menubar 菜单条
	 * @param associate 关联字
	 * @return
	 */
	public static JMenu findMenu(RayLaunchMenu root, String associate) {
		if (associate == null) {
			return null;
		}
		// 找到...
		int count = root.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = root.getComponent(index);
			if (element == null) {
				continue;
			}
			// 判断是JMenu
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				JMenu sub = findMenu((JMenu) element, associate);
				if (sub != null) {
					return sub;
				}
			}
		}
		return null;
	}
	
//	/**
//	 * 给每个MenuItem设置事件监器接口
//	 * @param menu
//	 * @param listener
//	 */
//	private void setActionListener(JMenu menu, ActionListener listener) {
//		Component[] elements = menu.getMenuComponents();
//		int size = (elements == null ? 0 : elements.length);
//		// 判断对象
//		for (int index = 0; index < size; index++) {
//			Component element = elements[index];
//			// 给JMenuItem设置字体和边框
//			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
//				((JMenuItem) element).addActionListener(listener);
//			}
//			// JMenu是JMenuItem的子类，如果是，交给子级处理
//			if (Laxkit.isClassFrom(element, JMenu.class)) {
//				setActionListener((JMenu) element, listener);
//			}
//		}
//	}

//	/**
//	 * 找到每一个MenuItem，设置事件监器接口
//	 * @param root
//	 * @param listener
//	 */
//	public void setActionListener(LaunchMenu root, ActionListener listener) {
//		int count = root.getComponentCount();
//		for (int index = 0; index < count; index++) {
//			Component element = root.getComponent(index);
//			if (element == null) {
//				continue;
//			}
//			// 1. 先判断是JMenuItem
//			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
//				((JMenuItem) element).addActionListener(listener);
//			}
//			// JMenu是JMenuItem的子类，如果是，交给子级处理
//			if (Laxkit.isClassFrom(element, JMenu.class)) {
//				setActionListener((JMenu) element, listener);
//			}
//		}
//	}
	
	/**
	 * 过滤两侧空格
	 * @param text
	 * @return
	 */
	private String trim(String text) {
		Pattern pattern = Pattern.compile("^\\s*(.*?)\\s*$");
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return "";
	}

	/**
	 * 判断是空字符
	 * @param s
	 * @return
	 */
	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	/**
	 * 返回成员属性值
	 * @param root
	 * @param tag
	 * @return
	 */
	private String getAttribute(Element root, String tag) {
		String s = root.getAttribute(tag);
		return trim(s);
	}

	/**
	 * 显示文本
	 * @param root XML成员
	 * @return 返回显示文本
	 */
	private String getCaption(Element root) {
		return getAttribute(root, CAPTION);
	}

	/**
	 * 标识号。这个参数记录每个菜单的唯一身份
	 * @param root XML成员
	 * @return 返回标记字符串
	 */
	private String getId(Element root) {
		return getAttribute(root, RayLaunchMenuCreator.ID);
	}

	/**
	 * 返回助记符（热键）
	 * @param root
	 * @return
	 */
	private String getHotKey(Element root) {
		return getAttribute(root, RayLaunchMenuCreator.HOTKEY);
	}

	/**
	 * 找到关联参数
	 * @param root
	 * @return 返回字符
	 */
	private String getAssociate(Element root) {
		return getAttribute(root, RayLaunchMenuCreator.ASSOCIATE);
	}
	
//	/**
//	 * 选中
//	 * @param root
//	 * @return
//	 */
//	private String getCheckOn(Element root) {
//		return getAttribute(root, LaunchMenuCreator.CHECKON);
//	}
//
//	/**
//	 * 无线按钮
//	 * @param root
//	 * @return
//	 */
//	private String getRadioOn(Element root) {
//		return getAttribute(root, LaunchMenuCreator.RADIOON);
//	}

	/**
	 * 选中
	 * @param root
	 * @return
	 */
	private boolean isCheckOn(Element root) {
		String value = getAttribute(root, RayLaunchMenuCreator.CHECKON);
		if (value != null) {
			return value.matches("^\\s*(?i)(TRUE|YES|ON)\\s*$");
		}
		return false;
	}

	/**
	 * 无线按钮
	 * @param root
	 * @return
	 */
	private boolean isRadioOn(Element root) {
		String value = getAttribute(root, RayLaunchMenuCreator.RADIOON);
		if (value != null) {
			return value.matches("^\\s*(?i)(TRUE|YES|ON)\\s*$");
		}
		return false;
	}

//	/**
//	 * 返回子成员单元文本，只用于唯一时的选项
//	 * @param root
//	 * @param tag
//	 * @return
//	 */
//	private String getOnlyContent(Element root, String tag) {
//		NodeList list = root.getElementsByTagName(tag);
//		if (list.getLength() != 1) {
//			return null;
//		}
//		Element element = (Element) list.item(0);
//
//		return trim(element.getTextContent());
//	}
	
//	/**
//	 * 在多个选项中，取出第一个
//	 * @param root
//	 * @param tag
//	 * @return
//	 */
//	private String getFirstContent(Element root, String tag) {
//		NodeList nodes = root.getChildNodes();
//		int size = nodes.getLength();
//		for (int i = 0; i < size; i++) {
//			Node node = nodes.item(i);
//			String name = node.getNodeName();
//			// 判断一样
//			boolean success = (isElement(node) && name.equals(tag));
//			if (success) {
//				Element element = (Element) node;
//				return trim(element.getTextContent());
//			}
//		}
//		return null;
//	}
	
//	/**
//	 * 返回图标
//	 * @param root
//	 * @param defaultSize
//	 * @return
//	 */
//	private Dimension getIconSize(Element root, Dimension defaultSize) {
//		NodeList list = root.getElementsByTagName(ICON);
//		if (list.getLength() != 1) {
//			return null;
//		}
//		Element element = (Element) list.item(0);
//		String w = element.getAttribute("w");
//		String h = element.getAttribute("h");
//		// 判断
//		boolean success = ConfigParser.isInteger(w) && ConfigParser.isInteger(h);
//		if (success) {
//			return new Dimension(
//					ConfigParser.splitInteger(w, defaultSize.width),
//					ConfigParser.splitInteger(h, defaultSize.height));
//		}
//		return defaultSize;
//	}
	
//	/**
//	 * 返回图标，默认是16*16的尺寸
//	 * @param root 
//	 * @return 返回图标实例
//	 */
//	private Dimension getIconSize(Element root) {
//		return getIconSize(root, new Dimension(16, 16));
//	}
	
//	/**
//	 * 从多个标签中取出第一个
//	 * @param root
//	 * @param defaultSize
//	 * @return
//	 */
//	private Dimension getFirstIconSize(Element root, Dimension defaultSize) {
//		Element element = null;
//		NodeList nodes = root.getChildNodes();
//		int size = nodes.getLength();
//		for (int i = 0; i < size; i++) {
//			Node node = nodes.item(i);
//			String name = node.getNodeName();
//			// 判断一样
//			boolean success = (isElement(node) && name.equals(ICON));
//			if (success) {
//				element = (Element) node;
//				break;
//			}
//		}
//		// 没有，返回默认值
//		if(element == null) {
//			return defaultSize;
//		}
//		
//		String w = element.getAttribute("w");
//		String h = element.getAttribute("h");
//		// 判断
//		boolean success = ConfigParser.isInteger(w) && ConfigParser.isInteger(h);
//		if (success) {
//			return new Dimension(
//					ConfigParser.splitInteger(w, defaultSize.width),
//					ConfigParser.splitInteger(h, defaultSize.height));
//		}
//		return defaultSize;
//	}
	
//	/**
//	 * 返回从多集群中取第一个图标尺寸
//	 * @param root XML
//	 * @return 返回尺寸
//	 */
//	private Dimension getFirstIconSize(Element root) {
//		return getFirstIconSize(root, new Dimension(16, 16));
//	}

	/**
	 * 判断是ELEMENT属性
	 * @param node
	 * @return
	 */
	private boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * 判断是菜单
	 * @param node
	 * @return
	 */
	private boolean isMenu(Node node) {
		return isElement(node) && "Menu".equals(node.getNodeName());
	}
	
	/**
	 * 左侧的标题栏
	 * @param node
	 * @return
	 */
	private boolean isFlag(Node node) {
		return isElement(node) && "Flag".equals(node.getNodeName());
	}

	/**
	 * 判断是菜单项
	 * @param node
	 * @return
	 */
	private boolean isMenuItem(Node node) {
		return isElement(node) && "MenuItem".equals(node.getNodeName());
	}

	/**
	 * 判断是复选框菜单
	 * @param node
	 * @return
	 */
	private boolean isCheckBoxMenuItem(Node node) {
		return isElement(node) && "CheckBoxMenuItem".equals(node.getNodeName());
	}

	/**
	 * 判断是无线按钮菜单项
	 * @param node
	 * @return
	 */
	private boolean isRadioButtonMenuItem(Node node) {
		return isElement(node)
				&& "RadioButtonMenuItem".equals(node.getNodeName());
	}

	/**
	 * 判断是分隔符
	 * @param node
	 * @return
	 */
	private boolean isSeparator(Node node) {
		return isElement(node) && "Separator".equals(node.getNodeName());
	}

	/**
	 * 找到子成员
	 * @param root 根
	 * @param tag 标签
	 * @param index 索引顺序
	 * @return 返回XML成员
	 */
	private Element findSubElement(Element root, String tag, int index) {
		NodeList nodes = root.getChildNodes();
		int size = nodes.getLength();
		int count = 0;
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			String name = node.getNodeName();
			// 判断一样
			boolean success = (isElement(node) && name.equals(tag));
			if (success) {
				if (count == index) {
					return (Element) node;
				} else {
					count++;
				}
			}
		}
		return null;
	}
	
	/**
	 * 在多个选项中，取出第N个标签中的文本内容
	 * @param root 根
	 * @param tag 标记
	 * @param index 标签索引下标
	 * 
	 * @return 返回它的文本内容
	 */
	private String getTextContent(Element root, String tag, int index) {
		Element element = findSubElement(root, tag, index);
		if (element != null) {
			return trim(element.getTextContent());
		}
		return null;
	}
	
	/**
	 * 在多个选项中，取出第一个标签中的文本内容
	 * @param root 根
	 * @param tag 标记
	 * @return 返回文本或者空指针
	 */
	private String getTextContent(Element root, String tag) {
		return getTextContent(root, tag, 0);
	}

	/**
	 * 找到提示
	 * @param root
	 * @return
	 */
	private String getTooltip(Element root) {
		return getTextContent(root, TOOLTIP);
	}
	
	/**
	 * 找到快捷键
	 * @param root
	 * @return
	 */
	private String getShortcutKey(Element root) {
		return getTextContent(root, SHORTCUT_KEY);
	}
	
	/**
	 * 生成图标
	 * @param element
	 * @param defaultSize
	 * @return
	 */
	private ImageIcon createIcon(Element element, Dimension defaultSize) {
		String icon = trim(element.getTextContent());
		if (icon.trim().isEmpty()) {
			return null;
		}
		// 生成对象
		if (defaultSize == null) {
			defaultSize = new Dimension(16, 16);
		}
		
		String w = element.getAttribute("w");
		String h = element.getAttribute("h");
		
		// 生成图标尺寸
		Dimension d = new Dimension(ConfigParser.splitInteger(w, defaultSize.width), 
				ConfigParser.splitInteger(h, defaultSize.height));
		
		// 生成图像
		ResourceLoader loader = new ResourceLoader();
		return loader.findImage(icon, d.width, d.height);
	}

	/**
	 * 找到图标
	 * @param root
	 * @param defaultSize
	 * @return
	 */
	private ImageIcon getIcon(Element root, Dimension defaultSize) {
		// 返回图标单元，是第一个
		Element element = findSubElement(root, ICON, 0);
		if (element != null) {
			return createIcon(element, defaultSize);
		}
		return null;
	}
	
	/**
	 * 找到方法
	 * @param root
	 * @return
	 */
	private String getMethod(Element root) {
		return getTextContent(root, METHOD);
	}
	
	/**
	 * 建立菜单项
	 * @param root
	 * @return
	 */
	private JMenuItem createMenuItem(JMenuItem item, Element root) {
		// 以下四项是属性
		String caption = getCaption(root);
		String id = getId(root);
		String hotkey = getHotKey(root);
		String associate = getAssociate(root);

		// 设置显示文本
		FontKit.setButtonText(item, caption);
		// 关联字，在整个菜单集合中具有唯一性
		if (!isEmpty(associate)) {
			item.setName(associate);
		}
		// 设置助记符
		if (hotkey.length() == 1) {
			item.setMnemonic(hotkey.charAt(0));
		}
		// 标记号
		if (id.length() > 0) {
			item.getAccessibleContext().setAccessibleName(id);
		}

		// 判断是选中
		if (item.getClass() == JCheckBoxMenuItem.class) {
			boolean on = isCheckOn(root);
			((JCheckBoxMenuItem) item).setState(on);
		}
		// 判断选中
		if (item.getClass() == JRadioButtonMenuItem.class) {
			boolean on = isRadioOn(root);
			((JRadioButtonMenuItem) item).setSelected(on);
		}

		// 以下四项是单独标签，不是属性！
		String tooltip = getTooltip(root);
		String shortcut = getShortcutKey(root);
		Icon icon = getIcon(root, new Dimension(16, 16));
		String method = getMethod(root);

		if (!isEmpty(tooltip)) {
			// 标题文本
			FontKit.setToolTipText(item, tooltip);
		}
		if (!isEmpty(method)) {
			item.setName(method);
		}
		// 快捷键
		if (!isEmpty(shortcut)) {
			KeyStroke key = KeyStroke.getKeyStroke(shortcut);
			if (key != null) {
				item.setAccelerator(key);
			}
		}
		// 设置图标
		if (icon != null) {
			item.setIcon(icon);
		}

		Node node = root.getFirstChild();
		while (node != null) {
			if (isMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JMenuItem(), (Element) node);
				item.add(sub);
			} else if (isCheckBoxMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JCheckBoxMenuItem(), (Element) node);
				item.add(sub);
			} else if (isRadioButtonMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JRadioButtonMenuItem(), (Element) node);
				item.add(sub);
			} else if (isMenu(node)) {
				JMenu sub = createMenu(new JMenu(), (Element) node);
				item.add(sub);
			} 

			// 下一个成员
			node = node.getNextSibling();
		}
		
		// 默认是有效
		item.setEnabled(true);
		return item;
	}
	
	/**
	 * 建立菜单
	 * @param menu
	 * @param root
	 */
	private JMenu createMenu(JMenu menu, Element root) {
		// 以下四项是属性
		final String caption = getCaption(root);
		String id = getId(root);
		String hotkey = getHotKey(root);
		String associate = getAssociate(root);
		
		// 选择和显示合适的字体
		FontKit.setButtonText(menu, caption);
		// 关联字，在整个菜单集合中具有唯一性
		if (!isEmpty(associate)) {
			menu.setName(associate);
		}
		if (hotkey.length() == 1) {
			menu.setMnemonic(hotkey.charAt(0));
		}
		if (id.length() > 0) {
			menu.getAccessibleContext().setAccessibleName(id);
		}
		
		// 以下四项是单独的标签
		String tooltip = getTooltip(root); 
		String shortcut = getShortcutKey(root); 
		Icon icon = getIcon(root, new Dimension(16, 16));
		String method = getMethod(root); 

		// 提示
		if (!isEmpty(tooltip)) {
			// 标题文本
			FontKit.setToolTipText(menu, tooltip);
		}
		if (!isEmpty(method)) {
//			menu.setName(method.trim());
		}
		// 快捷键
		if (!isEmpty(shortcut)) {
			KeyStroke key = KeyStroke.getKeyStroke(shortcut);
			if (key != null) {
				menu.setAccelerator(key);
			}
		}
		// 设置图标
		if (icon != null) {
			menu.setIcon(icon);
		}
		
		NodeList nodes = root.getChildNodes();
		int size = nodes.getLength();		
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			if (isMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JMenuItem(), (Element) node);
				menu.add(sub);
			} else if (isCheckBoxMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JCheckBoxMenuItem(), (Element) node);
				menu.add(sub);
			} else if (isRadioButtonMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JRadioButtonMenuItem(), (Element) node);
				menu.add(sub);
			} else if (isMenu(node)) {
				JMenu sub = createMenu(new JMenu(), (Element) node);
				menu.add(sub);
			} else if (isSeparator(node)) {
				menu.addSeparator(); // 增加一个分隔栏
			} 
		}

		menu.setEnabled(true);
		return menu;
	}

	/**
	 * 解析XML文档，输出LaunchPopupMenu实例
	 * @param xml XML文档的字节数组
	 * @return LaunchPopupMenu实例
	 */
	public RayLaunchMenu create(byte[] xml) {
		Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve");
		}

		// 取出“LaunchMenu”，这是导航菜单起点
		NodeList nodes = document.getElementsByTagName("LaunchMenu");
		int size = nodes.getLength();
		if (size != 1) {
			throw new IllegalArgumentException("cannot be resolve 'MenuBar'");
		}
		
		RayLaunchMenu rayLaunchMenu = new RayLaunchMenu();
		
		// 从第一个单元开始，逐一读取并且建立菜单。最外层没有“Menu”项。
		Element root = (Element) nodes.item(0);
		nodes = root.getChildNodes();
		size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			// 左侧的垂直的FLAG（标题）栏
			if (isFlag(node)) {
				String text = ((Element) node).getTextContent();
				if (text != null) {
					rayLaunchMenu.setFlagText(text.trim());
				}
			}
			// 三种菜单项
			else if (isMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JMenuItem(), (Element) node);
				rayLaunchMenu.add(sub);
			} else if (isCheckBoxMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JCheckBoxMenuItem(), (Element) node);
				rayLaunchMenu.add(sub);
			} else if (isRadioButtonMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JRadioButtonMenuItem(), (Element) node);
				rayLaunchMenu.add(sub);
			}
			// 是菜单
			else if (isMenu(node)) {
				JMenu sub = createMenu(new JMenu(), (Element) node);
				rayLaunchMenu.add(sub);
			}
			// 分隔符
			else if (isSeparator(node)) {
				rayLaunchMenu.addSeparator(); // 增加一个分隔栏
			}
		}
		
		// 返回菜单条
		return rayLaunchMenu;
	}
	
	/**
	 * 从JAR包中的资源文件中解析和建立菜单
	 * @param name 资源文件路径（XML格式）
	 * @return DesktopLaunchMenu实例
	 */
	public RayLaunchMenu create(String name) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream in = loader.getResourceAsStream(name);
		if (in == null) {
			return null;
		}

		byte[] b = new byte[1024];
		ClassWriter buff = new ClassWriter(10240);
		try {
			while (true) {
				int len = in.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				buff.write(b, 0, len);
			}
			in.close();
		} catch (IOException exp) {
			return null;
		}

		if (buff.size() == 0) {
			return null;
		}
		// 解析和建立菜单条
		return create(buff.effuse());
	}

	public static void main(String[] args) {
		String filename = "c:/launchmenu.xml";

		File file = new File(filename);
		byte[] xml = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(xml);
			in.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		}

		RayLaunchMenuCreator e = new RayLaunchMenuCreator();
		RayLaunchMenu bar = e.create(xml);
		System.out.println("oaky!");
		System.out.printf("finish, item is %d\n", bar.getComponentCount());
	}
}



///**
// * 建立菜单
// * @param root
// * @return
// */
//private void createMenu(LaunchPopupMenu menubar, Element root) {
//	String caption = getCaption(root);
//	String id = getId(root);
//	String hotkey = getHotKey(root);
//	
//	// 建立一个菜单栏和保存它
//	JMenu menu = new JMenu();
//	FontKit.setButtonText(menu, caption);
//	menu = menubar.add(menu);
//
////	JMenu menu = menubar.add(new JMenu(caption));
//	
//	if (hotkey.length() == 1) {
//		menu.setMnemonic(hotkey.charAt(0));
//	}
//	if (id.length() > 0) {
//		menu.getAccessibleContext().setAccessibleName(id);
//	}
//
//	Node node = root.getFirstChild();
//	while (node != null) {
//		if (isMenuItem(node)) {
//			createMenuItem(menu, new JMenuItem(), (Element) node);
//		} else if (isCheckBoxMenuItem(node)) {
//			createMenuItem(menu, new JCheckBoxMenuItem(), (Element) node);
//		} else if (isRadioButtonMenuItem(node)) {
//			createMenuItem(menu, new JRadioButtonMenuItem(), (Element) node);
//		} else if (isMenu(node)) {
//			createMenu(menu, (Element) node);
//		} else if (isSeparator(node)) {
//			menu.addSeparator(); // 增加一个分隔栏
//		}
//		// 下一个成员
//		node = node.getNextSibling();
//	}
//}



///**
// * 解析XML文档，输出LaunchPopupMenu实例
// * @param xml XML文档的字节数组
// * @return LaunchPopupMenu实例
// */
//public LaunchPopupMenu create(byte[] xml) {
//	Document document = XMLocal.loadXMLSource(xml);
//	if (document == null) {
//		throw new IllegalArgumentException("cannot be resolve");
//	}
//
//	// 取出“LaunchMenu”，这是导航菜单起点
//	NodeList nodes = document.getElementsByTagName("LaunchMenu");
//	if (nodes.getLength() != 1) {
//		throw new IllegalArgumentException("cannot be resolve 'MenuBar'");
//	}
//
//	LaunchPopupMenu launchMenu = new LaunchPopupMenu();
//	Element root = (Element) nodes.item(0);
//	// 从第一个单元开始，逐一读取并且建立菜单。最外层没有“Menu”项。
//	Node node = root.getFirstChild();
//	while (node != null) {
//		// 三种菜单项
//		if (isMenuItem(node)) {
//			JMenuItem sub = createMenuItem(new JMenuItem(), (Element) node);
//			launchMenu.add(sub);
//		} else if (isCheckBoxMenuItem(node)) {
//			JMenuItem sub = createMenuItem(new JCheckBoxMenuItem(), (Element) node);
//			launchMenu.add(sub);
//		} else if (isRadioButtonMenuItem(node)) {
//			JMenuItem sub = createMenuItem(new JRadioButtonMenuItem(), (Element) node);
//			launchMenu.add(sub);
//		}
//		// 是菜单
//		else if (isMenu(node)) {
//			JMenu sub = createMenu(new JMenu(), (Element) node);
//			launchMenu.add(sub);
//		} 
//		// 分隔符
//		else if (isSeparator(node)) {
//			launchMenu.addSeparator(); // 增加一个分隔栏
//		}
//		
//		// 取下一个实例
//		node = node.getNextSibling();
//	}
//	// 返回菜单条
//	return launchMenu;
//}

