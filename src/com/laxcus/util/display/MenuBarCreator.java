/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.loader.*;
import com.laxcus.xml.*;

/**
 * 菜单条生成器。<br>
 * 从XML配置文档中解析数据，建立菜单项，输出JMenuBar。
 * 
 * @author scott.liang
 * @version 1.2 6/19/2015
 * @since laxcus 1.0
 */
public final class MenuBarCreator {

	private final static String CAPTION = "caption";
	private final static String ID = "id";
	private final static String HOTKEY = "hotkey";
	private final static String ASSOCIATE = "associate";
	private final static String CHECKON = "checkon";
	private final static String RADIOON = "radioon";

	private final static String TOOLTIP = "ToolTip";
	private final static String SHORTCUT_KEY = "ShortcutKey";
	private final static String ICON = "Icon";
	private final static String METHOD = "Method";

	/**
	 * 构造默认的菜单条生成器
	 */
	public MenuBarCreator() {
		super();
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
		return getAttribute(root, MenuBarCreator.ID);
	}

	/**
	 * 返回助记符（热键）
	 * @param root
	 * @return
	 */
	private String getHotKey(Element root) {
		return getAttribute(root, MenuBarCreator.HOTKEY);
	}

	/**
	 * 找到关联参数
	 * @param root
	 * @return 返回字符
	 */
	private String getAssociate(Element root) {
		return getAttribute(root, MenuBarCreator.ASSOCIATE);
	}
	
	/**
	 * 选中
	 * @param root
	 * @return
	 */
	private boolean isCheckOn(Element root) {
		String value = getAttribute(root, MenuBarCreator.CHECKON);
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
		String value = getAttribute(root, MenuBarCreator.RADIOON);
		if (value != null) {
			return value.matches("^\\s*(?i)(TRUE|YES|ON)\\s*$");
		}
		return false;
	}
	
//	/**
//	 * 选中
//	 * @param root
//	 * @return
//	 */
//	private String getCheckOn(Element root) {
//		return getAttribute(root, MenuBarCreator.CHECKON);
//	}
//
//	/**
//	 * 无线按钮
//	 * @param root
//	 * @return
//	 */
//	private String getRadioOn(Element root) {
//		return getAttribute(root, MenuBarCreator.RADIOON);
//	}
	
//	/**
//	 * 返回子成员单元文本
//	 * @param root
//	 * @param tag
//	 * @return
//	 */
//	private String getItemContent(Element root, String tag) {
//		NodeList list = root.getElementsByTagName(tag);
//		if (list.getLength() != 1) {
//			return null;
//		}
//		Element element = (Element) list.item(0);
//
//		return trim(element.getTextContent());
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
	 * 从JAR包中的资源文件中解析和建立菜单
	 * @param name 资源文件路径（XML格式）
	 * @return JMenuBar实例
	 */
	public JMenuBar create(String name) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream in = loader.getResourceAsStream(name);
		if(in == null) {
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

	/**
	 * 解析XML文档，输出JMenuBar实例
	 * @param xml XML文档的字节数组
	 * @return JMenuBar实例
	 */
	public JMenuBar create(byte[] xml) {
		Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve");
		}

		// 取出“MenuBar”，这是菜单起点
		NodeList nodes = document.getElementsByTagName("MenuBar");
		if (nodes.getLength() != 1) {
			throw new IllegalArgumentException("cannot be resolve 'MenuBar'");
		}

		JMenuBar menubar = new JMenuBar();
		Element root = (Element) nodes.item(0);
		// 从第一个单元开始，逐一读取并且建立菜单
		Node node = root.getFirstChild();
		while (node != null) {
			if (isMenu(node)) {
				createMenu(menubar, (Element) node);
			}
			// 取下一个实例
			node = node.getNextSibling();
		}
		// 返回菜单条
		return menubar;
	}

	/**
	 * 建立菜单
	 * @param root
	 * @return
	 */
	private void createMenu(JMenuBar menubar, Element root) {
		String caption = getCaption(root);
		String id = getId(root);
		String hotkey = getHotKey(root);
		
		// 建立一个菜单栏和保存它
		JMenu menu = new JMenu();
		FontKit.setButtonText(menu, caption);
		menu = menubar.add(menu);

//		JMenu menu = menubar.add(new JMenu(caption));
		
		if (hotkey.length() == 1) {
			menu.setMnemonic(hotkey.charAt(0));
		}
		if (id.length() > 0) {
			menu.getAccessibleContext().setAccessibleName(id);
		}

		Node node = root.getFirstChild();
		while (node != null) {
			if (isMenuItem(node)) {
				createMenuItem(menu, new JMenuItem(), (Element) node);
			} else if (isCheckBoxMenuItem(node)) {
				createMenuItem(menu, new JCheckBoxMenuItem(), (Element) node);
			} else if (isRadioButtonMenuItem(node)) {
				createMenuItem(menu, new JRadioButtonMenuItem(), (Element) node);
			} else if (isMenu(node)) {
				createMenu(menu, (Element) node);
			} else if (isSeparator(node)) {
				menu.addSeparator(); // 增加一个分隔栏
			}
			// 下一个成员
			node = node.getNextSibling();
		}
	}

	/**
	 * 建立菜单
	 * @param parent
	 * @param root
	 */
	private void createMenu(JMenu parent, Element root) {
		String caption = getCaption(root);
		String id = getId(root);
		String hotkey = getHotKey(root);
		String associate = getAssociate(root);

		JMenu child = new JMenu();
		child = (JMenu) parent.add(child);
		// 关联字
		if (associate != null && associate.length() > 0) {
			child.setName(associate);
		}
		
		// 选择和显示合适的字体
		FontKit.setButtonText(child, caption);
		
		if (hotkey.length() == 1) {
			child.setMnemonic(hotkey.charAt(0));
		}
		if (id.length() > 0) {
			child.getAccessibleContext().setAccessibleName(id);
		}

		Node node = root.getFirstChild();
		while (node != null) {
			if (isMenuItem(node)) {
				createMenuItem(child, new JMenuItem(), (Element) node);
			} else if (isCheckBoxMenuItem(node)) {
				// createCheckBoxMenuItem(menu, (Element) node);
				createMenuItem(child, new JCheckBoxMenuItem(), (Element) node);
			} else if (isRadioButtonMenuItem(node)) {
				createMenuItem(child, new JRadioButtonMenuItem(), (Element) node);
			} else if (isMenu(node)) {
				createMenu(child, (Element) node); // parent, Element root)
			} else if (isSeparator(node)) {
				child.addSeparator(); // 增加一个分隔栏
			}
			// 下一个成员
			node = node.getNextSibling();
		}
	}

	//	private JMenu createMenu(Element root) {
	//		String caption = getCaption(root);
	//		String word = getMnemonic(root);
	//
	//		JMenu menu = new JMenu(caption);
	//		if (word.length() == 1) {
	//			menu.setMnemonic(word.charAt(0));
	//		}
	//
	//		Node node = root.getFirstChild();
	//		while (node != null) {
	//			if (isMenuItem(node)) {
	//				//				JMenuItem item = createMenuItem((Element) node);
	//				//				menu.add(item);
	//
	//				createMenuItem(menu, (Element) node);
	//			} else if (isSeparator(node)) {
	//				menu.addSeparator(); // 增加一个分隔栏
	//			}
	//			// 下一个成员
	//			node = node.getNextSibling();
	//		}
	//
	//		return menu;
	//	}

	/**
	 * 建立菜单项
	 * @param root
	 * @return
	 */
	private void createMenuItem(JMenu menu, JMenuItem item, Element root) {
		// 属性
		String caption = getCaption(root);
		String id = getId(root);
		String word = getHotKey(root);

		// 添加菜单项
		item = menu.add(item);
		
//		// 设置显示文本
//		item.setText(caption);
		
		// 设置显示文本
		FontKit.setButtonText(item, caption);
		
		// 设置助记符
		if (word.length() == 1) {
			item.setMnemonic(word.charAt(0));
		}
		// 标记号
		if (id.length() > 0) {
			item.getAccessibleContext().setAccessibleName(id);
		}

		// 判断是选中
		if (item.getClass() == JCheckBoxMenuItem.class) {
//			String value = getCheckOn(root);
//			if (value != null) {
//				boolean on = value.matches("^\\s*(?i)(TRUE|YES|ON)\\s*$");
//				((JCheckBoxMenuItem) item).setState(on);
//			}
			
			boolean on = isCheckOn(root);
			((JCheckBoxMenuItem) item).setState(on);
		}
		// 判断选中
		if (item.getClass() == JRadioButtonMenuItem.class) {
//			String value = getRadioOn(root);
//			if (value != null) {
//				boolean on = value.matches("^\\s*(?i)(TRUE|YES|ON)\\s*$");
//				((JRadioButtonMenuItem) item).setSelected(on);
//			}
			
			boolean on = isRadioOn(root);
			((JRadioButtonMenuItem) item).setSelected(on);
		}

//		String tooltip = getItemContent(root, TOOLTIP);
//		String shortcut = getItemContent(root, SHORTCUT_KEY);
//		String icon = getItemContent(root, ICON);
//		String method = getItemContent(root, METHOD);
		
		String tooltip = getTooltip(root);
		String shortcut = getShortcutKey(root); // getItemContent(root, SHORTCUT_KEY);
//		String icon = getItemContent(root, ICON);
		String method = getMethod(root); // getItemContent(root, METHOD);
		Icon icon = getIcon(root, new Dimension(16, 16));
		
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
//			ResourceLoader loader = new ResourceLoader();
//			ImageIcon image = loader.findImage(icon);
//			item.setIcon(image);
//		}

		// 设置图标
		if (icon != null) {
			item.setIcon(icon);
		}
	}

	//	/**
	//	 * 建立菜单项
	//	 * @param root
	//	 * @return
	//	 */
	//	private void createMenuItem(JMenu menu, Element root) {
	//		String caption = getCaption(root);
	//		String id = getId(root);
	//		String hotkey = getHotKey(root);
	//
	//		JMenuItem item = menu.add(new JMenuItem(caption));
	//		if (hotkey.length() == 1) {
	//			item.setMnemonic(hotkey.charAt(0));
	//		}
	//		if (id.length() > 0) {
	//			item.getAccessibleContext().setAccessibleName(id);
	//		}
	//
	//		String tooltip = getItemContent(root, TOOLTIP);
	//		String shortcut = getItemContent(root, SHORTCUT_KEY);
	//		String icon = getItemContent(root, ICON);
	//		String method = getItemContent(root, METHOD);
	//
	//		if (!isEmpty(tooltip)) {
	//			item.setToolTipText(tooltip);
	//		}
	//		if (!isEmpty(method)) {
	//			item.setName(method);
	//		}
	//		// 快捷键
	//		if (!isEmpty(shortcut)) {
	//
	//		}
	//		// 加载和设置图标
	//		if (!isEmpty(icon)) {
	//			ResourceLoader loader = new ResourceLoader();
	//			ImageIcon image = loader.findImage(icon);
	//			item.setIcon(image);
	//		}
	//
	//
	//
	//		//		item.setIconTextGap(10);
	//
	//	}

	//	private JMenuItem createMenuItem(Element root) {
	//		String caption = getCaption(root);
	//		String word = getMnemonic(root);
	//
	//		JMenuItem item = new JMenuItem(caption);
	//		if (word.length() == 1) {
	//			item.setMnemonic(word.charAt(0));
	//		}
	//
	//		String tooltip = getItemContent(root, TOOLTIP);
	//		String shortcut = getItemContent(root, SHORTCUT);
	//		String icon = getItemContent(root, ICON);
	//		String method = getItemContent(root, METHOD);
	//
	//		if (!isEmpty(tooltip)) {
	//			item.setToolTipText(tooltip);
	//		}
	//		if (!isEmpty(method)) {
	//			item.setName(method);
	//		}
	//		// 快捷键
	//		if (!isEmpty(shortcut)) {
	//
	//		}
	//		// 加载和设置图标
	//		if (!isEmpty(icon)) {
	//			//			int index = icon.lastIndexOf('/');
	//			//			String path = icon.substring(0, index);
	//			//			String name = icon.substring(index + 1);
	//			//			ResourceLoader loader = new ResourceLoader(path);
	//			//			ImageIcon image = loader.findImage(name);
	//
	//			ResourceLoader loader = new ResourceLoader();
	//			ImageIcon image = loader.findImage(icon);
	//			item.setIcon(image);
	//		}
	//
	//		item.setIconTextGap(10);
	//		item.setArmed(true);
	//
	//		return item;
	//	}

	/**
	 * 给每个MenuItem设置事件监器接口
	 * @param menu
	 * @param listener
	 */
	private void setActionListener(JMenu menu, ActionListener listener) {
		int count = menu.getItemCount();
		for (int pos = 0; pos < count; pos++) {
			JMenuItem item = menu.getItem(pos);
			if (item == null) {
				continue;
			}

			if (item.getClass() == JMenu.class) {
				JMenu sub = (JMenu) item;
				setActionListener(sub, listener);
			} else {
				item.addActionListener(listener);
			}
		}
	}

	/**
	 * 找到每一个MenuItem，设置事件监器接口
	 * @param menubar
	 * @param listener
	 */
	public void setActionListener(JMenuBar menubar, ActionListener listener) {
		int size = menubar.getMenuCount();
		for (int i = 0; i < size; i++) {
			JMenu menu = menubar.getMenu(i);
			setActionListener(menu, listener);
		}
	}

	/**
	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
	 * @param menu 菜单栏
	 * @param method 方法名
	 * @return 返回菜单项，如果没有找到是空值
	 */
	private JMenuItem findMenuItem(JMenu menu, String method) {
		int count = menu.getItemCount();
		for (int pos = 0; pos < count; pos++) {
			JMenuItem item = menu.getItem(pos);
			if (item == null) {
				continue;
			}

			// 如果包含菜单项
			if (item.getClass() == JMenu.class) {
				JMenu sub = (JMenu) item;
				item = findMenuItem(sub, method);
				if (item != null) {
					return item;
				}
				continue;
			}

			String text = item.getName();
			if (method.equals(text)) {
				return item;
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
	public JMenuItem findMenuItem(JMenuBar menubar, String method) {
		int size = menubar.getMenuCount();
		JMenuItem item = null;
		for (int i = 0; i < size; i++) {
			JMenu menu = menubar.getMenu(i);
			item = findMenuItem(menu, method);
			if (item != null) break;
		}

		return item;
	}

	/**
	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
	 * @param menu 菜单栏
	 * @param associate 关联字
	 * @return 返回菜单项，如果没有找到是空值
	 */
	private JMenu findMenu(JMenu menu, String associate) {
		int count = menu.getItemCount();
		for (int pos = 0; pos < count; pos++) {
			JMenuItem item = menu.getItem(pos);
			// 所在位置不是菜单，返回空指针！或者不是JMenu，这两个情况忽略！
			if (item == null) {
				continue;
			} else if (item.getClass() != JMenu.class) {
				continue;
			}
			
			//			// 所在位置不是菜单，返回空指针，或者不是JMenu，忽略！
			//			if (item == null || item.getClass() != JMenu.class) {
			//				continue;
			//			}

			// 判断这个菜单
			JMenu sub = (JMenu) item;
			// 判断名称匹配
			String name = sub.getName();
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
	public JMenu findMenu(JMenuBar menubar, String associate) {
		int size = menubar.getMenuCount();
		for (int i = 0; i < size; i++) {
			JMenu menu = menubar.getMenu(i);
			JMenu sub = findMenu(menu, associate);
			if (sub != null) {
				return sub;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		String filename = "D:/lexst/zh_CN.xml";

		File file = new File(filename);
		byte[] xml = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(xml);
			in.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		}

		MenuBarCreator e = new MenuBarCreator();
		JMenuBar bar = e.create(xml);
		//		JMenuBar bar = e.create(filename);
		System.out.printf("finish, item is %d,%d\n", bar.getComponentCount(), bar.getMenuCount());
	}
}
