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
import com.laxcus.util.skin.*;
import com.laxcus.xml.*;

/**
 * 工具条生成器。<br>
 * 从XML配置文档中解析工具条数据，建立工具按钮，输出JToolBar。
 * 
 * @author scott.liang
 * @version 1.2 6/19/2015
 * @since laxcus 1.0
 */
public final class ToolBarCreator {

	private final static String TOOLTIP = "ToolTip";
	private final static String ICON = "Icon";
	private final static String METHOD = "Method";

	/**
	 * 构造默认的工具条生成器
	 */
	public ToolBarCreator() {
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


//	/**
//	 * 返回成员属性值
//	 * @param root
//	 * @param tag
//	 * @return
//	 */
//	private String getAttribute(Element root, String tag) {
//		String s = root.getAttribute(tag);
//		return trim(s);
//	}

//	/**
//	 * 显示文本
//	 * @param root
//	 * @return
//	 */
//	private String getCaption(Element root) {
//		return getAttribute(root, CAPTION);
//	}
//
//	/**
//	 * 快捷键
//	 * @param root
//	 * @return
//	 */
//	private String getMnemonic(Element root) {
//		return getAttribute(root, M);
//	}

	/**
	 * 返回子成员单元文本
	 * @param root
	 * @param tag
	 * @return
	 */
	private String getItemContent(Element root, String tag) {
		NodeList list = root.getElementsByTagName(tag);
		if (list.getLength() != 1) {
			return null;
		}
		Element element = (Element) list.item(0);

		return trim(element.getTextContent());
	}

	/**
	 * 判断是ELEMENT属性
	 * @param node
	 * @return
	 */
	private boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}
	
	/**
	 * 判断是按钮
	 * @param node
	 * @return
	 */
	private boolean isButton(Node node) {
		return isElement(node) && "Button".equals(node.getNodeName());
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
	 * 建立按钮
	 * @param root
	 * @param width 宽度
	 * @param height 高度
	 * @return
	 */
	private JButton createButton(Element root, int width, int height, double brightness) {
		JButton button = new JButton();

		// 不要有边框
		button.setBorderPainted(false);
		// 不要设置焦点
		button.setFocusPainted(false);
		
		//		// 设置假
		//		button.setContentAreaFilled(false);

		String tooltip = getItemContent(root, TOOLTIP);
		String icon = getItemContent(root, ICON);
		String method = getItemContent(root, METHOD);

		if (!isEmpty(tooltip)) {
//			button.setToolTipText(tooltip);
			
			// 设置标题文本
			FontKit.setToolTipText(button, tooltip);
		}
		if (!isEmpty(method)) {
			button.setName(method);
		}
		// 加载和设置图标
		if (!isEmpty(icon)) {
//			int index = icon.lastIndexOf('/');
//			String path = icon.substring(0, index);
//			String name = icon.substring(index + 1);
////			ResourceLoader loader = new ResourceLoader(path);
			
			ResourceLoader loader = new ResourceLoader();
			ImageIcon image = loader.findImage(icon, width, height);
			button.setIcon(image);
			
			// 其它状态的...
			if (brightness > 0.0f) {
				ImageIcon other = ImageUtil.brighter(image, brightness);
				button.setPressedIcon(other);
				button.setSelectedIcon(other);
				button.setRolloverIcon(other);
				button.setRolloverSelectedIcon(other);
			}
		}

		return button;
	}
	
	/**
	 * 从JAR包中的资源文件中解析和建立工具条
	 * @param name 资源文件路径（XML格式）
	 * @return JToolBar实例
	 */
	public JToolBar create(String name) {
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
		} catch (IOException e) {
			return null;
		}

		if (buff.size() == 0) {
			return null;
		}
		// 解析和建立工具条
		return create(buff.effuse());
	}

	/**
	 * 解析XML文档，输出JToolBar实例
	 * @param xml XML文档的字节数组
	 * @return JToolBar实例
	 */
	public JToolBar create(byte[] xml){
		Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve");
		}

		// 取出“MenuBar”，这是菜单起点
		NodeList nodes = document.getElementsByTagName("ToolBar");
		if (nodes.getLength() != 1) {
			throw new IllegalArgumentException("cannot be resolve 'ToolBar'");
		}
		
		JToolBar toolbar = new JToolBar();
		
		Element root = (Element) nodes.item(0);
		
		// 图标的宽度和高度
		String b = root.getAttribute("icon-brightness");
		String w = root.getAttribute("icon-width");
		String h = root.getAttribute("icon-height");
		int width = ConfigParser.splitInteger(w, 32);
		int height = ConfigParser.splitInteger(h, 32);
		
		// 亮度，双浮点值
		double brightness = 0.0f;
		if (b != null && ConfigParser.isDouble(b)) {
			brightness = ConfigParser.splitDouble(b, 0.0f);
		}
		
		// 从第一个单元开始，逐一读取并且建立菜单
		Node node = root.getFirstChild();
		while (node != null) {
			if (isButton(node)) { 
				JButton button = createButton((Element) node, width, height, brightness);
				toolbar.add(button);
			} else if(isSeparator(node)) {
				toolbar.addSeparator();
			}
			// 取下一个实例
			node = node.getNextSibling();
		}
		
		return toolbar;
	}
	
	/**
	 * 设置事件监听接口
	 * @param toolbar 工具条
	 * @param listener 事件监听器
	 */
	public void setActionListener(JToolBar toolbar, ActionListener listener) {
		int size = toolbar.getComponentCount();
		for (int i = 0; i < size; i++) {
			Component e = toolbar.getComponent(i);
			if (e.getClass() == JButton.class) {
				((JButton) e).addActionListener(listener);
//				System.out.printf("jbutton name is %s\n", ((JButton)e).getToolTipText());
			}
		}
	}
}
