/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.xml;

import java.io.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 本地XML文档解析器
 * 
 * @author scott.liang
 * @version 1.0 10/17/2008
 * @since laxcus 1.0
 */
public class XMLocal {

	/**
	 * XML文档解析器
	 */
	public XMLocal() {
		super();
	}

	/**
	 * 过滤字符串两侧的空格
	 * @param str 字符串
	 * @return 返回过滤后的字符串
	 */
	private static String trim(String str) {
		if (str == null) return "";
		return str.trim();
	}

	/**
	 * 提取标签中文本参数
	 * @param element 单元
	 * @param tag 标记
	 * @return 返回标签中的文本参数。如果标记不存在，返回空字符串。
	 */
	public static String getValue(Element element, String tag) {
		NodeList list = element.getElementsByTagName(tag);
		if (list == null || list.getLength() == 0) {
			return "";
		}
		Element that = (Element) list.item(0);
		if (that == null) {
			return "";
		}
		return XMLocal.trim(that.getTextContent());
	}
	
	/**
	 * 提取标签中文本参数
	 * @param element 单元
	 * @param tag 标记
	 * @return 返回标签中的文本参数。如果标记不存在，返回空字符串。
	 */
	public static String getAttribute(Element element, String tag, String attr) {
		NodeList list = element.getElementsByTagName(tag);
		if (list == null || list.getLength() == 0) {
			return "";
		}
		Element that = (Element) list.item(0);
		if (that == null) return "";
		return XMLocal.trim(that.getAttribute(attr));
	}

	/**
	 * 提取标签中的一组文本参数
	 * @param element 单元
	 * @param tag 标记
	 * @return 返回标签中的全部文本参数。如果不存在，返回0长度数组。
	 */
	public static String[] getValues(Element element, String tag) {
		NodeList nodes = element.getElementsByTagName(tag);
		if (nodes == null || nodes.getLength() == 0) {
			return new String[0];
		}
		int size = nodes.getLength();
		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			Element e = (Element) nodes.item(i);
			if (e == null) {
				array[i] = "";
			} else {
				array[i] = XMLocal.trim(e.getTextContent());
			}
		}
		return array;
	}

	/**
	 * 返回XML配置表某组中第一项数据
	 * @param nodes
	 * @return
	 */
	public static String getXMLValue(NodeList nodes) {
		if (nodes == null || nodes.getLength() < 1) {
			return new String();
		}
		Element element = (Element) nodes.item(0);
		if (element == null) {
			return new String();
		}
		return XMLocal.trim(element.getTextContent());
	}

	/**
	 * 判断是XML Element
	 * @param node
	 * @return
	 */
	private static boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * 返回XML配置表某组的全部数据
	 * @param nodes
	 * @return
	 */
	public static String[] getXMLValues(NodeList nodes) {
		int size = nodes.getLength();
		String[] all = new String[size];

		for (int index = 0; index < size; index++) {
			Node node = nodes.item(index);
			if (isElement(node)) {
				Element element = (Element) node;
				all[index] = XMLocal.trim(element.getTextContent());
			} else {
				all[index] = "";
			}
		}
		return all;
	}

	/**
	 * 以字节流的形式加载XML文档并且返回Document对象
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public static Document loadXMLSource(byte[] b, int off, int len) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(b, off, len);
			// 生成对象
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(bin);
		} catch (IOException e) {
			Logger.error(e);
		} catch (ParserConfigurationException e) {
			Logger.error(e);
		} catch (SAXException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 以字节流的形式加载XML文档
	 * 
	 * @param b
	 * @return
	 */
	public static Document loadXMLSource(byte[] b) {
		return XMLocal.loadXMLSource(b, 0, (b == null ? 0 : b.length));
	}

	/**
	 * 加载XML文件
	 * @param file
	 * @return
	 */
	public static Document loadXMLSource(File file) {
		if (!file.exists() || file.isDirectory()) {
			return null;
		}
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return XMLocal.loadXMLSource(b);
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 根据文件名,装载XML配置文件
	 * @param filename
	 * @return
	 */
	public static Document loadXMLSource(String filename) {
		filename = ConfigParser.splitPath(filename);
		File file = new File(filename);
		return XMLocal.loadXMLSource(file);
	}
}