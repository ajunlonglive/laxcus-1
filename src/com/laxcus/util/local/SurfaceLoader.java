/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.local;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.xml.*;

/**
 * 界面资源加载和分析器 <br>
 * 从JAR文件中加载不同语言的XML文档，根据指定的路径，获取不同的文本数据。
 * 
 * @author scott.liang
 * @version 1.2 9/15/2014
 * @since laxcus 1.0
 */
public class SurfaceLoader {

	/** XML文档根 **/
	private Document document;

	/** 标签路径 -> 单元实例 **/
	private TreeMap<String, SurfaceElement> elements = new TreeMap<String, SurfaceElement>();

	/**
	 * 构造默认的界面资源加载和分析器
	 */
	public SurfaceLoader() {
		super();
	}

	/**
	 * 构造界面资源加载和分析器，指定目录
	 * @param path XML文档资源文件
	 */
	public SurfaceLoader(String path) {
		this();
		load(path);
	}

	/**
	 * 判断已经加载
	 * @return 返回真或者假
	 */
	public boolean isLoaded() {
		return document != null;
	}

	/**
	 * 从JAR文件中读取一个XML文本
	 * @return 返回字节数组
	 */
	public byte[] findAbsoluteStream(String path) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream in = loader.getResourceAsStream(path);
		if (in == null) {
			return null;
		}

		byte[] b = new byte[1024];
		ClassWriter buff = new ClassWriter(1024);
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
			return new byte[0];
		}
		return buff.effuse();
	}

	/**
	 * 加载XML文档
	 * @param path XML文档路径
	 */
	public void load(String path) {
		byte[] xml = findAbsoluteStream(path);
		if (Laxkit.isEmpty(xml)) {
			throw new IllegalArgumentException("cannot be load " + path);
		}
		document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve byte array");
		}

		// 加载参数
		loadXMLDocument();
	}

	/**
	 * 判断成员匹配（是ELEMENT且名称一致）
	 * @param node 节点
	 * @param name 名称
	 * @return 返回真或者假
	 */
	private boolean matchs(Node node, String name) {
		boolean success = isElement(node);
		if(success) {
			success = node.getNodeName().equals(name);
		}
		return success;
	}

	/**
	 * 根据标签找到根成员
	 * @param roots 节点集
	 * @param name 根标签名
	 * @return Element或者空指针
	 */
	private Element findRootElement(NodeList roots, String name) {
		int size = roots.getLength();
		for (int i = 0; i < size; i++) {
			// 根节点
			Node node = roots.item(i);
			// 判断匹配
			if (matchs(node, name)) {
				return (Element) node;
			}
			// 子级节点
			NodeList subs = node.getChildNodes();
			if (subs.getLength() > 0) {
				Element element = findRootElement(subs, name);
				if (element != null) return element;
			}
		}
		return null;
	}

	/**
	 * 根据XML路径标签查找XML成员
	 * @param tags XML标签
	 * @return  返回XML成员
	 */
	public Element findElement(String[] tags) {
		// 以递归方式找到根节点
		Element element = findRootElement(document.getChildNodes(), tags[0]);
		if (element == null) {
			return null;
		}

		// 逐一比较每一个节点的名称
		for (int index = 1; index < tags.length; index++) {
			NodeList nodes = element.getChildNodes();
			int seek = 0;
			for (; seek < nodes.getLength(); seek++) {
				Node node = nodes.item(seek);
				// 判断匹配（是ELEMENT，且名称一致）
				if (matchs(node, tags[index])) {
					element = (Element) node;
					break;
				}
			}
			// 没能匹配的名称，退出
			if (seek == nodes.getLength()) {
				return null;
			}
		}
		// 返回ELEMENT
		return element;
	}

	/**
	 * 判断是XML Element
	 * @param node
	 * @return
	 */
	private boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * 判断是属性
	 * @param node
	 * @return
	 */
	private boolean isAttribute(Node node) {
		return node.getNodeType() == Node.ATTRIBUTE_NODE;
	}

	/**
	 * 从根目录加载XML文档中的参数
	 */
	private void loadXMLDocument() {
		NodeList nodes = document.getChildNodes();
		int size = nodes.getLength();
		// 找到根节点
		Element root = null;
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			if (isElement(node)) {
				root = (Element) node;
			}
		}
		// 没有根节点，忽略它！
		if (root == null) {
			return;
		}

		// 提取根节点下面的子节点
		nodes = root.getChildNodes();
		size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			if (isElement(node)) {
				Element element = (Element) node;
				resolve(element.getNodeName(), element);
			}
		}
	}

	/**
	 * 忽略两侧空格和不可见字符
	 * @param input 输入的字符串
	 * @return 格式化后的字符串
	 */
	private String ignore(String input) {
		final String regex = "^(?:\\s*)(.+?)(?:\\s*)$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return input;
	}

	/**
	 * 分解参数
	 * @param path 路径
	 * @param parent 单元实例
	 */
	private void resolve(String path, Element parent) {
		SurfaceElement member = new SurfaceElement(path);

		// 当前属性参数
		NamedNodeMap map = parent.getAttributes();
		if (map != null) {
			int size = map.getLength();
			for (int i = 0; i < size; i++) {
				Node node = map.item(i);
				if (isAttribute(node)) {
					Attr attr = (Attr) node;
					String name = attr.getName().trim();
					String value = attr.getValue().trim();
					// 保存属性
					SurfaceAttribute attribute = new SurfaceAttribute(name, value);
					member.addAttribute(attribute);
				}
			}
		}

		// 子节点参数
		int count = 0;
		NodeList nodes = parent.getChildNodes();
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			if (!isElement(node)) {
				continue;
			}

			// 解析子节点
			Element sub = (Element) node;
			String name = String.format("%s/%s", path, sub.getNodeName());
			resolve(name, sub);
			count++;
		}

		// 没有子节点时，保存它的文本数据
		if (count == 0) {
			String content = ignore(parent.getTextContent());
			member.setContent(content);
		}

		// 保存
		elements.put(member.getPath(), member);

		//		// 打印结果
		//		print(member);
		//		System.out.printf("%s 下级节点数目：%d\n", path, count);
	}

	/**
	 * 以XML路径分隔符为依据，将XML路径切割成多个分段
	 * @param xmlPath
	 * @return 字符串数组
	 */
	private String[] split(String xmlPath) {
		return xmlPath.split("/");
	}

	/**
	 * 解析单元
	 * @param xmlPath
	 * @return
	 */
	private SurfaceElement find(String xmlPath) {
		return elements.get(xmlPath);
	}

	/**
	 * 找到一个属性。XML格式路径大小写敏感。这个一定不能错
	 * @param xmlPath XML路径
	 * @return 返回路径中的属性参数，没有返回空值
	 */
	public String getAttribute(String xmlPath) {
		// 分割XML路径
		String[] tags = split(xmlPath);

		// 忽略最后一个路径名，这个名称是XML属性名称
		String[] paths = new String[tags.length - 1];
		StringBuilder prefix = new StringBuilder();
		for (int i = 0; i < paths.length; i++) {
			if (prefix.length() > 0) {
				prefix.append("/");
			}
			prefix.append(tags[i]);
		}

		// 以路径分段，找到关联的XML成员
		SurfaceElement element = find(prefix.toString());
		if (element == null) {
			return null;
		}

		// 取属性名称
		String name = tags[tags.length - 1];
		// 返回属性参数
		SurfaceAttribute attribute = element.findAttribute(name);
		// 如果是空字符串，输出空指针；否则是参数原值
		if (attribute != null) {
			return attribute.getValue();
		}
		return null;
	}

	/**
	 * 根据XML路径，找到一块文本
	 * @param xmlPath XML路径
	 * @return 文本信息
	 */
	public String getContent(String xmlPath) {
		SurfaceElement element = find(xmlPath);
		if (element == null) {
			return null;
		}
		// 取XML成员中的内容
		return element.getContent();
	}


	//	/**
	//	 * 从根目录加载XML文档中的参数
	//	 */
	//	private void loadXMLDocument1() {
	//		// 取根节点
	//		Node root = document.getFirstChild();
	//		
	//		System.out.printf("root node name:%s\n", root.getNodeName());
	//
	//		// 子节点参数
	//		NodeList nodes = root.getChildNodes();
	//		int size = nodes.getLength();
	//		
	//		System.out.printf("sub node size:%d\n", size);
	//		
	//		for (int i = 0; i < size; i++) {
	//			Node node = nodes.item(i);
	//			if (isElement(node)) {
	//				Element element = (Element) node;
	//				resolve(element.getNodeName(), element);
	//			}
	//		}
	//	}

	//	public void load(File file) {
	//		int len = (int) file.length();
	//		byte[] xml = new byte[len];
	//		try {
	//			FileInputStream in = new FileInputStream(file);
	//			in.read(xml);
	//			in.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		XMLocal local = new XMLocal();
	//
	//		document = local.loadXMLSource(xml);
	//		if (document == null) {
	//			throw new IllegalArgumentException("cannot be resolve byte array");
	//		}
	//
	//		loadXMLDocument();
	//	}
	//
	//	private void print(SurfaceElement element) {
	//		System.out.printf("\n根：[%s]\n", element.getPath());
	//		for( SurfaceAttribute attribute : element.getAttributes()) {
	//			System.out.printf("属性：[%s]\n", attribute);
	//		}
	//
	//		System.out.printf("文本： [%s]\n", element.getContent());
	//	}
	//
	//	public static void main(String[] args) {
	//		String filename = "D:/lexst/conf/watch/invoker/zh_CN/invoker.xml";
	//		filename = "D:/lexst/conf/front/invoker/zh_CN/invoker.xml";
	//		SurfaceLoader loader = new SurfaceLoader();
	//		loader.load(new File(filename));
	//
	//		System.out.printf("element size:%s\n\n", loader.elements.size());
	//
	//		System.out.printf("%s\n", loader.getAttribute("MULTI-TRAFFIC/RETRIES/title"));
	//		System.out.printf("%s\n", loader.getContent("REFRESH-RESOURCE/SITE/INVALID"));
	//		System.out.printf("%s\n", loader.getContent("LIMIT-ITEM/OPERATOR/READ"));
	//
	//		//			String xmlPath = "Dialog/Font/Style/Plain/title";
	//		//			//		xmlPath = "Dialog/Login/Account/Username/title";
	//		//			//		xmlPath = "Dialog/Login/Account/Password/title";
	//		//			xmlPath = "COMMAND-MODE/MODE/title";
	//		//			String attr = loader.getAttribute(xmlPath);
	//		//			System.out.printf("RESULT IS %s\n", attr);
	//		//	
	//		//			xmlPath = "COMMAND-MODE/MODE/width";
	//		//			attr = loader.getAttribute(xmlPath);
	//		//			System.out.printf("RESULT IS [%s]\n", attr);
	//
	//	}

}

//public final class SurfaceLoader {
//
//	/** XML文档根 **/
//	private Document document;
//
//	/**
//	 * 构造默认的界面资源加载和分析器
//	 */
//	public SurfaceLoader() {
//		super();
//	}
//
//	/**
//	 * 构造界面资源加载和分析器，指定目录
//	 * @param path XML文档资源文件
//	 */
//	public SurfaceLoader(String path) {
//		this();
//		load(path);
//	}
//
//	/**
//	 * 判断已经加载
//	 * @return 返回真或者假
//	 */
//	public boolean isLoaded() {
//		return document != null;
//	}
//
//	/**
//	 * 加载XML文档
//	 * @param path XML文档路径
//	 */
//	public void load(String path) {
//		byte[] xml = findAbsoluteStream(path);
//		if (Laxkit.isEmpty(xml)) {
//			throw new IllegalArgumentException("cannot be load " + path);
//		}
//		XMLocal local = new XMLocal();
//		document = local.loadXMLSource(xml);
//		if (document == null) {
//			throw new IllegalArgumentException("cannot be resolve byte array");
//		}
//	}
//
//	/**
//	 * 从JAR文件中读取一个XML文本
//	 * @return 返回字节数组
//	 */
//	public byte[] findAbsoluteStream(String path) {
//		ClassLoader loader = ClassLoader.getSystemClassLoader();
//		InputStream in = loader.getResourceAsStream(path);
//		if(in == null) {
//			return null;
//		}
//
//		byte[] b = new byte[1024];
//		ClassWriter buff = new ClassWriter(1024);
//		try {
//			while (true) {
//				int len = in.read(b, 0, b.length);
//				if (len == -1) {
//					break;
//				}
//				buff.write(b, 0, len);
//			}
//			in.close();
//		} catch (IOException e) {
//			return null;
//		}
//
//		if (buff.size() == 0) {
//			return new byte[0];
//		}
//		return buff.effuse();
//	}
//
//	/**
//	 * 以XML路径分隔符为依据，将XML路径切割成多个分段
//	 * @param xmlPath
//	 * @return 字符串数组
//	 */
//	private String[] split(String xmlPath) {
//		return xmlPath.split("/");
//	}
//
//	/**
//	 * 判断成员匹配（是ELEMENT且名称一致）
//	 * @param node
//	 * @param name
//	 * @return
//	 */
//	private boolean matchs(Node node, String name) {
//		boolean success = (node.getNodeType() == Node.ELEMENT_NODE);
//		if(success) {
//			success = node.getNodeName().equals(name);
//		}
//		return success;
//	}
//
//	/**
//	 * 根据标签找到根成员
//	 * @param roots 节点集
//	 * @param name 根标签名
//	 * @return Element或者空指针
//	 */
//	private Element findRootElement(NodeList roots, String name) {
//		int size = roots.getLength();
//		for (int i = 0; i < size; i++) {
//			// 根节点
//			Node node = roots.item(i);
//			// 判断匹配
//			if (matchs(node, name)) {
//				return (Element) node;
//			}
//			// 子级节点
//			NodeList subs = node.getChildNodes();
//			if (subs.getLength() > 0) {
//				Element element = findRootElement(subs, name);
//				if (element != null) return element;
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * 根据XML路径标签查找XML成员
//	 * @param tags XML标签
//	 * @return  返回XML成员
//	 */
//	public Element findElement(String[] tags) {
//		// 以递归方式找到根节点
//		Element element = findRootElement(document.getChildNodes(), tags[0]);
//		if (element == null) {
//			return null;
//		}
//
//		// 逐一比较每一个节点的名称
//		for (int index = 1; index < tags.length; index++) {
//			NodeList nodes = element.getChildNodes();
//			int seek = 0;
//			for (; seek < nodes.getLength(); seek++) {
//				Node node = nodes.item(seek);
//				// 判断匹配（是ELEMENT，且名称一致）
//				if (matchs(node, tags[index])) {
//					element = (Element) node;
//					break;
//				}
//			}
//			// 没能匹配的名称，退出
//			if (seek == nodes.getLength()) {
//				return null;
//			}
//		}
//		// 返回ELEMENT
//		return element;
//	}
//
//	/**
//	 * 找到一个属性。XML格式路径大小写敏感。这个一定不能错
//	 * @param xmlPath XML路径
//	 * @return 返回路径中的属性参数，没有返回空值
//	 */
//	public String getAttribute(String xmlPath) {
//		// 分割XML路径
//		String[] tags = split(xmlPath);
//
//		// 忽略最后一个路径名，这个名称是XML属性名称
//		String[] paths = new String[tags.length - 1];
//		for (int i = 0; i < paths.length; i++) {
//			paths[i] = tags[i];
//		}
//
//		// 以路径分段，找到关联的XML成员
//		Element element = findElement(paths);
//		if (element == null) {
//			return null;
//		}
//
//		// 取属性名称
//		String name = tags[tags.length - 1];
//		// 返回属性参数
//		String attribute = element.getAttribute(name);
//		// 如果是空字符串，输出空指针；否则是参数原值
//		if (attribute.isEmpty()) {
//			return null;
//		}
//		return attribute;
//	}
//
//	/**
//	 * 根据XML路径，找到一块文本
//	 * @param xmlPath XML路径
//	 * @return 文本信息
//	 */
//	public String getContent(String xmlPath) {
//		String[] tags = split(xmlPath);
//		Element element = findElement(tags);
//		if (element == null) {
//			return null;
//		}
//		// 取XML成员中的内容
//		return element.getTextContent();
//	}
//
//	//	private boolean isElement(Node node) {
//	//		return node.getNodeType() == Node.ELEMENT_NODE;
//	//	}
//
//	//	/**
//	//	 * 根据XML路径标签查找XML成员
//	//	 * @param tags - XML标签
//	//	 * @return - XML成员
//	//	 */
//	//	public Element findElement(String[] tags) {
//	//		byte[] xml = findAbsoluteStream();
//	//		XMLocal local = new XMLocal();
//	//		Document document = local.loadXMLSource(xml);
//	//		if (document == null) {
//	//			throw new IllegalArgumentException("cannot be resolve");
//	//		}
//	//
//	//		// 找到根节点，只能有一个
//	//		NodeList nodes = document.getElementsByTagName(tags[0]);
//	//		if (nodes == null || nodes.getLength() < 1) {
//	//			return null;
//	//		}
//	//
//	//		Element element = (Element) nodes.item(0);
//	//		for (int index = 1; index < tags.length; index++) {
//	//			// 逐一比较每一个节点的名称
//	//			nodes = element.getChildNodes();
//	//			int size = nodes.getLength();
//	//			int seek = 0;
//	//			for (; seek < size; seek++) {
//	//				Node node = nodes.item(seek);
//	//				// 判断匹配（是ELEMENT，且名称一致）
//	//				if (matchs(node, tags[index])) {
//	//					element = (Element) node;
//	//					break;
//	//				}
//	//			}
//	//			// 没能匹配的名称，退出
//	//			if (seek == size) {
//	//				return null;
//	//			}
//	//		}
//	//		// 返回ELEMENT
//	//		return element;
//	//	}
//
//	//	public Element findElement3(String[] tags) {
//	//		byte[] xml = null;// findAbsoluteStream();
//	//		
//	//		try {
//	//			File file = new File(this.path);
//	//			xml = new byte[(int)file.length()];
//	//			FileInputStream in = new FileInputStream(file);
//	//			in.read(xml);
//	//			in.close();
//	//		} catch (IOException e) {
//	//			e.printStackTrace();
//	//		}
//	//		
//	//		XMLocal local = new XMLocal();
//	//		Document document = local.loadXMLSource(xml);
//	//		if (document == null) {
//	//			throw new IllegalArgumentException("cannot be resolve");
//	//		}
//	//		
//	//		Node root = document;
//	//		for (int index = 0; index < tags.length; index++) {
//	//			NodeList nodes = root.getChildNodes();
//	//			int seek = 0;
//	//			int size = nodes.getLength();
//	//			for (; seek < size; seek++) {
//	//				Node node = nodes.item(seek);
//	//
//	//				System.out.printf("NODE NAME:%s\n", node.getNodeName());
//	//
//	//				if (matchs(node, tags[index])) {
//	//					root = node;
//	//				}
//	//
//	//				// if (!isElement(node)) {
//	//				// continue;
//	//				// }
//	//				// // 找到匹配的标签
//	//				// Element sub = (Element) node;
//	//				// if (sub.getNodeName().equals(tags[index])) {
//	//				// root = node;
//	//				// break;
//	//				// }
//	//			}
//	//			// 没能匹配的名称，退出
//	//			if (seek == size) {
//	//				return null;
//	//			}
//	//		}
//	//
//	//		return (Element) root;
//	//	}
//
//	//	public Element findElement3(String[] tags) {
//	//		byte[] xml = findAbsoluteStream();
//	//
//	//		//		try {
//	//		//			File file = new File(this.path);
//	//		//			xml = new byte[(int)file.length()];
//	//		//			FileInputStream in = new FileInputStream(file);
//	//		//			in.read(xml);
//	//		//			in.close();
//	//		//		} catch (IOException e) {
//	//		//			e.printStackTrace();
//	//		//		}
//	//
//	//		XMLocal local = new XMLocal();
//	//		Document document = local.loadXMLSource(xml);
//	//		if (document == null) {
//	//			throw new IllegalArgumentException("cannot be resolve");
//	//		}
//	//
//	//		// 找到根节点，只能有一个
//	//		NodeList nodes = document.getElementsByTagName(tags[0]);
//	//		if (nodes.getLength() != 1) {
//	//			return null;
//	//		}
//	//
//	//		Element element = (Element) nodes.item(0);
//	//		for (int index = 1; index < tags.length; index++) {
//	//			// 逐一比较每一个节点的名称
//	//			nodes = element.getChildNodes();
//	//			int size = nodes.getLength();
//	//			int seek = 0;
//	//			for (; seek < size; seek++) {
//	//				Node node = nodes.item(seek);
//	//
//	////				System.out.printf("NODE NAMEs:%s\n", node.getNodeName());
//	//
//	//				// 判断匹配（是ELEMENT，且名称一致）
//	//				if(matchs(node, tags[index])) {
//	//					element = (Element)node;
//	//					break;
//	//				}
//	//			}
//	//			// 没能匹配的名称，退出
//	//			if (seek == size) {
//	//				return null;
//	//			}
//	//		}
//	//
//	//		return element;
//	//	}
//
//	//	public Element findElement2(String[] tags) {
//	//		byte[] xml = findAbsoluteStream();
//	//		XMLocal local = new XMLocal();
//	//		Document document = local.loadXMLSource(xml);
//	//		if (document == null) {
//	//			throw new IllegalArgumentException("cannot be resolve");
//	//		}
//	//
//	//		NodeList nodes = document.getElementsByTagName(tags[0]);
//	////		if (nodes.getLength() != 1) {
//	////			return null;
//	////		}
//	//		
//	//		if (nodes == null || nodes.getLength() < 1) {
//	//			return null;
//	//		}
//	//
//	//		Element element = (Element) nodes.item(0);
//	//		for (int index = 1; index < tags.length; index++) {
//	//			nodes = element.getElementsByTagName(tags[index]);
//	////			if (nodes.getLength() != 1) {
//	////				return null;
//	////			}
//	//			if (nodes == null || nodes.getLength() < 1) {
//	//				return null;
//	//			}
//	//			element = (Element) nodes.item(0);
//	//		}
//	//
//	//		return element;
//	//	}
//
//	//	public void load(File file) {
//	//		int len = (int) file.length();
//	//		byte[] xml = new byte[len];
//	//		try {
//	//			FileInputStream in = new FileInputStream(file);
//	//			in.read(xml);
//	//			in.close();
//	//		} catch (IOException e) {
//	//			e.printStackTrace();
//	//		}
//	//		XMLocal local = new XMLocal();
//	//		document = local.loadXMLSource(xml);
//	//		if (document == null) {
//	//			throw new IllegalArgumentException("cannot be resolve byte array");
//	//		}
//	//	}
//	//
//	//	public static void main(String[] args) {
//	//		//			String filename ="D:/lexst/conf/watch/resource/zh_CN/resource.xml";
//	//		String filename = "D:/lexst/conf/watch/invoker/zh_CN/invoker.xml";
//	//		SurfaceLoader loader = new SurfaceLoader();
//	//		loader.load(new File(filename));
//	//		String xmlPath = "Dialog/Font/Style/Plain/title";
//	//		//		xmlPath = "Dialog/Login/Account/Username/title";
//	//		//		xmlPath = "Dialog/Login/Account/Password/title";
//	//		xmlPath = "COMMAND-MODE/MODE/title";
//	//		String attr = loader.getAttribute(xmlPath);
//	//		System.out.printf("RESULT IS %s\n", attr);
//	//
//	//		xmlPath = "COMMAND-MODE/MODE/width";
//	//		attr = loader.getAttribute(xmlPath);
//	//		System.out.printf("RESULT IS [%s]\n", attr);
//	//
//	//	}
//
//}