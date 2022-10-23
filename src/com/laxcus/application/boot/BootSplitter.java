/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.xml.*;

/**
 * 引导单元解析器
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class BootSplitter {
	
	private final static String BootItem = "BootItem";
	
	private final static String ATTRIBUTE_SYSTEM = "System";
	
	private final static String AttachMenu = "AttachMenu";
	
	private final static String Icon = "Icon";
	
	private final static String Title = "Title";
	
	private final static String ToolTip = "ToolTip";
	
	private final static String Version = "Version";
	
	private final static String Application = "Application";
	
	private final static String Document = "Document";
	
	private final static String JURI = "JURI";
	
	private final static String URI = "URI";
	
	private final static String OpenCommand = "OpenCommand";
	
	private final static String Command = "Command";
	
	private final static String BootClass = "BootClass";
	
	private final static String SupportTypes = "SupportTypes";
	
	
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
	private boolean isBootItem(Node node) {
		return isElement(node) && BootItem.equals(node.getNodeName());
	}
	
	private boolean isIcon(Node node) {
		return isElement(node) && Icon.equals(node.getNodeName());
	}
	
	/**
	 * 标题
	 * @param node
	 * @return
	 */
	private boolean isTitle(Node node) {
		return isElement(node) && Title.equals(node.getNodeName());
	}
	
	/**
	 * 工具提示
	 * @param node
	 * @return
	 */
	private boolean isToolTip(Node node) {
		return isElement(node) && ToolTip.equals(node.getNodeName());
	}
	
	/**
	 * 版本号
	 * @param node
	 * @return
	 */
	private boolean isVersion(Node node) {
		return isElement(node) && Version.equals(node.getNodeName());
	}
	
	/**
	 * 应用
	 * @param node
	 * @return
	 */
	private boolean isApplication(Node node) {
		return isElement(node) && Application.equals(node.getNodeName());
	}
	
	/**
	 * 文档
	 * @param node
	 * @return
	 */
	private boolean isDocument(Node node) {
		return isElement(node) && Document.equals(node.getNodeName());
	}
	
	
	private BootApplicationItem splitApplicationItem(Element element) {
		BootApplicationItem item = new BootApplicationItem();

		String name = XMLocal.getValue(element, Command);
		if (name != null && name.trim().length() > 0) {
			item.setCommand(name.trim());
		}

		name = XMLocal.getValue(element, BootClass);
		if (name != null && name.trim().length() > 0) {
			item.setBootClass(name.trim());
		}
		
		name = XMLocal.getValue(element, SupportTypes);
		if (name != null && name.trim().length() > 0) {
			item.setSupportTypes(name.trim());
		}

		return item;
	}
	
	private BootDocumentItem splitDocumentItem(Element element) {
		BootDocumentItem item = new BootDocumentItem();
		
		BootLocation bootLocation = new BootLocation();
		// JRUI
		String text = XMLocal.getValue(element, JURI);
		if (text != null && text.trim().length() > 0) {
			bootLocation.setJURI(text.trim());
		}
		// URI
		text = XMLocal.getValue(element, URI);
		if (text != null && text.trim().length() > 0) {
			bootLocation.setURI(text.trim());
		}
		item.setLocation(bootLocation);

		String name = XMLocal.getValue(element, OpenCommand);
		if (name != null && name.trim().length() > 0) {
			item.setOpenCommand(name.trim());
		}
		
		return item;
	}
	
	/**
	 * 解析位置
	 * @param element
	 * @return
	 */
	private BootLocation splitLocation(Element element) {
		BootLocation bootLocation = new BootLocation();

		// JRUI
		String text = XMLocal.getValue(element, JURI);
		if (text != null && text.trim().length() > 0) {
			bootLocation.setJURI(text.trim());
		}
		// URI
		text = XMLocal.getValue(element, URI);
		if (text != null && text.trim().length() > 0) {
			bootLocation.setURI(text.trim());
		}

		return bootLocation;
	}
	
	
	private String trim(Node node) {
		String text = node.getTextContent();
		return text.trim();
	}
	
	private BootItem createBootItem(Element element) {
		BootItem item = new BootItem();

		// 系统
		String value = element.getAttribute(ATTRIBUTE_SYSTEM);
		// 判断是系统应用
		item.setSystem(ConfigParser.splitBoolean(value, false));

		//		item.setSystem(value.matches("^\\s*(?i)(SYSTEM)\\s*$"));

		// 绑定的上级菜单
		String attachMenu = element.getAttribute(AttachMenu);
		if (attachMenu != null && attachMenu.trim().length() > 0) {
			item.setAttachMenu(attachMenu.trim());
		}
		
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			// 标题
			if (isTitle(node)) {
				String text = trim(node);
				if (text != null && text.trim().length() > 0) {
					item.setTitle(text.trim());
				}
			}
			// 工具提示
			else if (isToolTip(node)) {
				String text = trim(node);
				if (text != null && text.trim().length() > 0) {
					item.setToolTip(text.trim());
				}
			}
			// 显示版本
			else if (isVersion(node)) {
				// 显示的版本
				String version = trim(node);
				if (version != null && version.trim().length() > 0) {
					item.setVersion(version.trim());
				}
			}
			// 图标
			else if (isIcon(node)) {
				BootLocation localtion = splitLocation((Element) node);
				item.setIcon(localtion);
			}
			// 应用
			else if (isApplication(node)) {
				BootApplicationItem sub = splitApplicationItem((Element) node);
				item.setApplication(sub);
			}
			// 文档
			else if (isDocument(node)) {
				BootDocumentItem sub = splitDocumentItem((Element) node);
				item.setDocument(sub);
			}
			// 如果是子菜单
			else if (isBootItem(node)) {
				BootItem sub = createBootItem((Element) node);
				item.add(sub);
			}
		}
		
		return item;
	}
	
	/**
	 * 解析
	 * @param xml
	 * @return
	 */
	public BootItem split(byte[] xml) {
		Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve");
		}
		
		// 统计单元
		int count = 0;
		Element root = null;
		// 找到第一个
		NodeList nodes = document.getChildNodes();
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			if (isBootItem(node)) {
				if (count == 0) {
					root = (Element) node;
				}
				count++;
			}
		}
		// 如果不止一个时...
		if (count != 1) {
			throw new IllegalArgumentException("illegal boot.xml");
		}

		// 生成根单元
		BootItem boot = createBootItem(root);

		// 输出前，设置迭代编号
		boot.doIterateIndex();
		// 返回结果
		return boot;
	}
	
}



//private BootItem createBootItem(Element element) {
//	BootItem item = new BootItem();
//
//	// 系统
//	String system = element.getAttribute(System);
//	item.setSystem(system.matches("^\\s*(?i)(SYSTEM)\\s*$"));
//
//	// 绑定的上级菜单
//	String attachMenu = element.getAttribute(AttachMenu);
//	if (attachMenu != null && attachMenu.trim().length() > 0) {
//		item.setAttachMenu(attachMenu.trim());
//	}
//	
//	// 显示的图标
//	NodeList nodes = element.getElementsByTagName(Icon);
//	int size = nodes.getLength();
//	if (size == 1) {
//		BootLocation localtion = splitLocation((Element)nodes.item(0));
//		item.setIcon(localtion);
//	}
//	
//	// 显示的标题
//	String title = XMLocal.getValue(element, Title);
//	if (title != null && title.trim().length() > 0) {
//		item.setTitle(title.trim());
//	}
//	// 工具提示
//	String tooltip = XMLocal.getValue(element, ToolTip);
//	if (tooltip != null && tooltip.trim().length() > 0) {
//		item.setToolTip(tooltip.trim());
//	}
//	// 显示的版本
//	String version = XMLocal.getValue(element, Version);
//	if (version != null && version.trim().length() > 0) {
//		item.setVersion(version.trim());
//	}
//	
//	// 应用单元
//	nodes = element.getElementsByTagName(Application);
//	size = nodes.getLength();
//	if (size == 1) {
//		BootApplicationItem sub = splitApplicationItem((Element) nodes.item(0));
//		item.setApplication(sub);
//	}
//
//	// 文档单元
//	nodes = element.getElementsByTagName(Document);
//	size = nodes.getLength();
//	if (size == 1) {
//		BootDocumentItem sub = splitDocumentItem((Element) nodes.item(0));
//		item.setDocument(sub);
//	}
//
//	// 它的子级单元
//	nodes = element.getChildNodes();
//	size = nodes.getLength();
//	for (int i = 0; i < size; i++) {
//		Node node = nodes.item(i);
//		if (isBootItem(node)) {
//			BootItem sub = createBootItem((Element) node);
//			item.add(sub);
//		}
//	}
//	
//	return item;
//}



//public BootItem split(byte[] xml) {
//	Document document = XMLocal.loadXMLSource(xml);
//	if (document == null) {
//		throw new IllegalArgumentException("cannot be resolve");
//	}
//
//	// 取出“LaunchMenu”，这是导航菜单起点
//	NodeList nodes = document.getElementsByTagName(BootItem);
//	int size = nodes.getLength();
//	if (size != 1) {
//		throw new IllegalArgumentException("illegal boot.xml");
//	}
//	
//	NodeList nodes =	document.getChildNodes();
//	Element root = null;
//	for(int i=0; i < nodes.getLength(); i++) {
//		
//	}
//
//	// 从第一个单元开始，逐一读取并且建立菜单。最外层没有“Menu”项。
//	Element root = (Element) nodes.item(0);
//
//	if (!isBootItem(root)) {
//		throw new IllegalArgumentException("cannot be resolve boot.xml");
//	}
//
//	// 生成根单元
//	BootItem boot = createBootItem(root);
//	
//	nodes = root.getChildNodes();
//	size = nodes.getLength();
//	for (int i = 0; i < size; i++) {
//		Node node = nodes.item(i);
//		if (isBootItem(node)) {
//			BootItem item = createBootItem((Element) node);
//			boot.add(item);
//		}
//	}
//	// 输出前，设置迭代编号
//	boot.doIterateIndex();
//	// 返回结果
//	return boot;
//}