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
import javax.swing.border.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.xml.*;

/**
 * 弹出菜单生成器
 * 
 * @author scott.liang
 * @version 1.0 6/12/2021
 * @since laxcus 1.0
 */
public class RayRockMenuCreator {

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
	public static JMenu findMenu(RayRockMenu root, String associate) {
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
	
	/**
	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
	 * @param menu 菜单栏
	 * @param method 方法名
	 * @return 返回菜单项，如果没有找到是空值
	 */
	private static JMenuItem findMenuItemByMethod(JMenu menu, String method) {
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
			else if (Laxkit.isClassFrom(element, JMenuItem.class)) {
				JMenuItem item = (JMenuItem) element;
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
	 * @param root 根弹出菜单
	 * @param method 方法名称
	 * @return 返回匹配方法名称的菜单项目，没有是空指针
	 */
	public static JMenuItem findMenuItemByMethod(RayRockMenu root, String method) {
		int count = root.getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = root.getComponent(index);
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
			// 判断是JMenuItem
			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
				JMenuItem item = (JMenuItem) element;
				String text = item.getName();
				if (method.equals(text)) {
					return item;
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
//	 * @param menubar
//	 * @param listener
//	 */
//	public void setActionListener(RockMenu root, ActionListener listener) {
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
		return s == null || s.length() == 0;
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
		return getAttribute(root, RayRockMenuCreator.ID);
	}

	/**
	 * 返回助记符（热键）
	 * @param root
	 * @return
	 */
	private String getHotKey(Element root) {
		return getAttribute(root, RayRockMenuCreator.HOTKEY);
	}

	/**
	 * 找到关联参数
	 * @param root
	 * @return 返回字符
	 */
	private String getAssociate(Element root) {
		return getAttribute(root, RayRockMenuCreator.ASSOCIATE);
	}
	
	/**
	 * 选中
	 * @param root
	 * @return
	 */
	private boolean isCheckOn(Element root) {
		String value = getAttribute(root, RayRockMenuCreator.CHECKON);
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
		String value = getAttribute(root, RayRockMenuCreator.RADIOON);
		if (value != null) {
			return value.matches("^\\s*(?i)(TRUE|YES|ON)\\s*$");
		}
		return false;
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
		if (associate != null && associate.length() > 0) {
			menu.setName(associate);
		}
		if (hotkey.length() == 1) {
			menu.setMnemonic(hotkey.charAt(0));
		}
		if (id.length() > 0) {
			menu.getAccessibleContext().setAccessibleName(id);
		}
		
		// 以下四项是单独的标签
		String tooltip = getTooltip(root); // getFirstContent(root, TOOLTIP);
		String shortcut = this.getShortcutKey(root); // getFirstContent(root, SHORTCUT_KEY);
//		String icon = getFirstContent(root, ICON);
		String method = this.getMethod(root); // getFirstContent(root, METHOD);
		Icon icon = this.getIcon(root, new Dimension(16, 16));
		
		// 提示
		if (!isEmpty(tooltip)) {
			// 标题文本
			FontKit.setToolTipText(menu, tooltip);
		}
		if (!isEmpty(method)) {
			menu.setName(method);
		}
		// 快捷键
		if (!isEmpty(shortcut)) {
			KeyStroke key = KeyStroke.getKeyStroke(shortcut);
			if (key != null) {
				menu.setAccelerator(key);
			}
		}
//		// 加载和设置图标
//		if (!isEmpty(icon)) {
//			Dimension d = getFirstIconSize(root);
//			ResourceLoader loader = new ResourceLoader();
//			ImageIcon image = loader.findImage(icon, d.width, d.height);
//			menu.setIcon(image);
//		}
		
		if(icon != null) {
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
		
		menu.setBorder(new EmptyBorder(3,4,3,4));

		menu.setEnabled(true);
		return menu;
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
		if (associate != null && associate.length() > 0) {
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

			//			String value = getCheckOn(root);
			//			if (value != null) {
			//				boolean on = value.matches("^\\s*(?i)(TRUE|ON)\\s*$");
			//				((JCheckBoxMenuItem) item).setState(on);
			//			}
		}
		// 判断选中
		if (item.getClass() == JRadioButtonMenuItem.class) {
			boolean on = isRadioOn(root);
			((JRadioButtonMenuItem) item).setSelected(on);

			//			String value = getRadioOn(root);
			//			if (value != null) {
			//				boolean on = value.matches("^\\s*(?i)(TRUE|ON)\\s*$");
			//				((JRadioButtonMenuItem) item).setSelected(on);
			//			}
		}

//		// 以下四项是单独行
//		String tooltip = getOnlyContent(root, TOOLTIP);
//		String shortcut = getOnlyContent(root, SHORTCUT_KEY);
//		String icon = getOnlyContent(root, ICON);
//		String method = getOnlyContent(root, METHOD);

		// 以下四项是单独的标签
		String tooltip = getTooltip(root); // getFirstContent(root, TOOLTIP);
		String shortcut = this.getShortcutKey(root); // getFirstContent(root, SHORTCUT_KEY);
//		String icon = getFirstContent(root, ICON);
		String method = this.getMethod(root); // getFirstContent(root, METHOD);
		Icon icon = this.getIcon(root, new Dimension(16, 16));
		
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
//		// 加载和设置图标
//		if (!isEmpty(icon)) {
//			Dimension d = getIconSize(root);
//			ResourceLoader loader = new ResourceLoader();
//			ImageIcon image = loader.findImage(icon, d.width, d.height);
//			item.setIcon(image);
//		}
		
		// 设置图标
		if(icon !=null){
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
		
		// 设置边框
		item.setBorder(new EmptyBorder(4, 4, 4, 4));

		// 默认是有效
		item.setEnabled(true);
		return item;
	}
	
	/**
	 * 生成一个弹出菜单
	 * @param root
	 * @return
	 */
	public RayRockMenu create(Element root) {
		NodeList nodes = root.getChildNodes();
		int size = nodes.getLength();
		
		RayRockMenu rayRockMenu = new RayRockMenu();
		
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			// 三种菜单项
			if (isMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JMenuItem(), (Element) node);
				rayRockMenu.add(sub);
			} else if (isCheckBoxMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JCheckBoxMenuItem(), (Element) node);
				rayRockMenu.add(sub);
			} else if (isRadioButtonMenuItem(node)) {
				JMenuItem sub = createMenuItem(new JRadioButtonMenuItem(), (Element) node);
				rayRockMenu.add(sub);
			}
			// 是菜单
			else if (isMenu(node)) {
				JMenu sub = createMenu(new JMenu(), (Element) node);
				rayRockMenu.add(sub);
			}
			// 分隔符
			else if (isSeparator(node)) {
				rayRockMenu.addSeparator(); // 增加一个分隔栏
			}
		}
		
		return rayRockMenu;
	}
	
	/**
	 * 解析XML文档
	 * @param name
	 * @return
	 */
	private byte[] splitXML(String name) {
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
		return buff.effuse();
	}
	
	/**
	 * 生成弹出菜单
	 * @param name JAR包中的文件名
	 * @param xmlRoot 根标签
	 * @return 返回弹出菜单
	 */
	public RayRockMenu create(String name, String xmlRoot) {
		// 解析
		byte[] xmlText = splitXML(name);
		Document document = XMLocal.loadXMLSource(xmlText);
		NodeList list = document.getElementsByTagName(xmlRoot);
		if (list.getLength() != 1) {
			return null;
		}

		Element element = (Element) list.item(0);
		return create(element);
	}

}