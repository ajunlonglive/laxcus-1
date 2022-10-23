/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import com.laxcus.application.boot.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.skin.*;
import com.laxcus.xml.*;

/**
 * 引导单元解析器
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class WTokenChanger {
	
	private final static String BootItem = "BootItem";
	
//	private final static String ATTRIBUTE_SYSTEM = "System";
	
	private final static String AttachMenu = "AttachMenu";
	
	private final static String Icon = "Icon";
	
	private final static String Title = "Title";
	
	private final static String ToolTip = "ToolTip";
	
	private final static String Version = "Version";
	
	private final static String Application = "Application";
	
	private final static String Document = "Document";
	
	private final static String JURI = "JURI";
	
	private final static String URI = "URI";
	
//	private final static String Name = "Name";
	
	private final static String OpenCommand = "OpenCommand";
	
	private final static String Command = "Command";
	
	private final static String BootClass = "BootClass";
	
	private final static String SupportTypes = "SupportTypes";
	
	/** 桌面属性，在"Application"中  **/
	private final static String DesktopAttribute = "Desktop";
	
	/** 应用坞属性，在"Application"中  **/
	private final static String DockAttribute = "Dock";
	
	/**
	 * 判断是ELEMENT属性
	 * @param node
	 * @return
	 */
	private static boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * 判断是菜单
	 * @param node
	 * @return
	 */
	private static boolean isBootItem(Node node) {
		return isElement(node) && BootItem.equals(node.getNodeName());
	}
	
	private static boolean isIcon(Node node) {
		return isElement(node) && Icon.equals(node.getNodeName());
	}
	
	/**
	 * 标题
	 * @param node
	 * @return
	 */
	private static boolean isTitle(Node node) {
		return isElement(node) && Title.equals(node.getNodeName());
	}
	
	/**
	 * 工具提示
	 * @param node
	 * @return
	 */
	private static boolean isToolTip(Node node) {
		return isElement(node) && ToolTip.equals(node.getNodeName());
	}
	
//	/**
//	 * 版本号
//	 * @param node
//	 * @return
//	 */
//	private static boolean isVersion(Node node) {
//		return isElement(node) && Version.equals(node.getNodeName());
//	}
	
	/**
	 * 应用
	 * @param node
	 * @return
	 */
	private static boolean isApplication(Node node) {
		return isElement(node) && Application.equals(node.getNodeName());
	}
	
	/**
	 * 文档
	 * @param node
	 * @return
	 */
	private static boolean isDocument(Node node) {
		return isElement(node) && Document.equals(node.getNodeName());
	}
	
	private static String trim(Node node) {
		String text = node.getTextContent();
		return text.trim();
	}
	
	private static void splitApplicationItem(WProgram item, Element element) {
		// 取出参数
		String yes = element.getAttribute(DesktopAttribute);
		item.setDesktop(ConfigParser.splitBoolean(yes, false));
		
		yes = element.getAttribute(DockAttribute);
		item.setDock(ConfigParser.splitBoolean(yes, false));

		String name = XMLocal.getValue(element, Command);
		if (name != null && name.trim().length() > 0) {
			item.setCommand(name.trim());
		}

		name = XMLocal.getValue(element, BootClass);
		if (name != null && name.trim().length() > 0) {
			WKey key = new WKey( name.trim() );
			item.setKey(key);
		}
		
		// 支持类型
		name = XMLocal.getValue(element, SupportTypes);
		if (name != null && name.trim().length() > 0) {
			item.setSupportTypes(name.trim());
		}
	}

	private static void splitDocumentItem(WDocument item, Element element) {
		// JRUI
		String text = XMLocal.getValue(element, JURI);
		if (text != null && text.trim().length() > 0) {
			item.setJURI(text.trim());
		}
		// URI
		text = XMLocal.getValue(element, URI);
		if (text != null && text.trim().length() > 0) {
			item.setURI(text.trim());
		}
		// 打开应用的命令
		String name = XMLocal.getValue(element, OpenCommand);
		if (name != null && name.trim().length() > 0) {
			item.setOpenCommand(name.trim());
		}
	}

//	/**
//	 * 解析位置
//	 * @param element
//	 * @return
//	 */
//	private BootLocation splitLocation(Element element) {
//		BootLocation bootLocation = new BootLocation();
//
//		// JRUI
//		String text = XMLocal.getValue(element, JURI);
//		if (text != null && text.trim().length() > 0) {
//			bootLocation.setJURI(text.trim());
//		}
//		// URI
//		text = XMLocal.getValue(element, URI);
//		if (text != null && text.trim().length() > 0) {
//			bootLocation.setURI(text.trim());
//		}
//
//		return bootLocation;
//	}

//	public ImageIcon getIcon(BasketBuffer buffer, int w, int h) throws IOException {
//		if (icon.getURI() != null) {
//			byte[] bytes = buffer.getURI(icon.getURI());
//			return ImageUtil.scale(bytes, w, h);
//		} else if (icon.getJURI() != null) {
//			byte[] bytes = buffer.getJURI(icon.getJURI());
//			return ImageUtil.scale(bytes, w, h);
//		}
//		return null;
//	}
	

	private static boolean isApplicationToken(Element element) {
		NodeList list = element.getChildNodes();
		int size = list.getLength();
		for(int i =0; i < size; i++) {
			Node node = list.item(i);
			if(isApplication(node)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isDocumentToken(Element element) {
		NodeList list = element.getChildNodes();
		int size = list.getLength();
		for(int i =0; i < size; i++) {
			Node node = list.item(i);
			if(isDocument(node)) {
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean isDirectoryToken(Element element) {
		return !isDocumentToken(element) &&!isApplicationToken(element);
	}
	
	private static ImageIcon getIcon(BasketBuffer buffer, Element element) throws IOException {
		// JRUI
		String text = XMLocal.getValue(element, JURI);
		if (text != null && text.trim().length() > 0) {
			byte[] b =	buffer.getJURI(text.trim());
			return ImageUtil.scale(b, 32, 32);
		}
		// URI
		text = XMLocal.getValue(element, URI);
		if (text != null && text.trim().length() > 0) {
			byte[] b = buffer.getURI(text.trim());
			return ImageUtil.scale(b, 32, 32);
		}
		return null;
	}

	/**
	 * 解析单元
	 * @param buffer
	 * @param item
	 * @param element
	 * @throws IOException
	 */
	private static void splitElement(BasketBuffer buffer, WElement item, Element element) throws IOException {
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
			// 图标
			else if (isIcon(node)) {
				// 读取...
				ImageIcon icon = getIcon(buffer, (Element) node);
				item.setIcon(icon);

//				System.out.printf("icon %s\n", (icon!=null ? "YES" : "NO"));
			}
			// 应用
			else if (isApplication(node)) {
				if (!Laxkit.isClassFrom(item, WProgram.class)) {
					throw new ClassCastException("cannot be cast to WProgram!");
				}
				splitApplicationItem((WProgram) item, (Element) node);
			}
			// 文档
			else if (isDocument(node)) {
				if (!Laxkit.isClassFrom(item, WDocument.class)) {
					throw new ClassCastException("cannot be cast to WDocument!");
				}
				splitDocumentItem((WDocument) item, (Element) node);
			}
			// 如果是子菜单
			else if (isBootItem(node)) {
				// 当前必须是目录
				if (!Laxkit.isClassFrom(item, WDirectory.class)) {
					throw new ClassCastException("cannot be cast WDirectory!");
				}
				// 三种可能。。。
				if (isApplicationToken((Element) node)) {
					WProgram sub = new WProgram();
					splitElement(buffer, sub, (Element)node);
					// 保存
					((WDirectory) item).addToken(sub);
				} else if (isDocumentToken((Element) node)) {
					WDocument sub = new WDocument();
					splitElement(buffer, sub, (Element)node);
					((WDirectory) item).addToken(sub);
				} else if (isDirectoryToken((Element) node)) {
					WDirectory sub = new WDirectory();
					splitElement(buffer, sub, element);
					((WDirectory) item).addToken(sub);
				}
			}
		}
	}
	
	/**
	 * 设置签名
	 * @param element
	 * @param hash
	 */
	private static void setHash(WElement element, SHA256Hash hash) {
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			((WProgram) element).getKey().setHash(hash);
		}
		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				setHash(sub, hash);
			}
		}
	}
	
	/**
	 * 解析字节数组
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static WRoot split(byte[] content, boolean saveContent) throws IOException {
		BasketBuffer buffer = new BasketBuffer();
		// 加载软件包
		buffer.load(content);
		byte[] xml = buffer.readBootstrap();
		
		SHA256Hash hash = Laxkit.doSHA256Hash(content);
		
		// 生成根单元
		WRoot root = new WRoot();
		root.setHash(hash);
		
		// 保存内存
		if (saveContent) {
			root.setContent(content);
		}
		
//		System.out.println(new String(xml, "UTF-8"));
//		System.out.println();
		
		Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve boot stream!");
		}
		
		NodeList list = document.getChildNodes();
		ArrayList<Element> elements = new ArrayList<Element>();
		
//		System.out.printf("item is %d\n", list.getLength());
		
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Node node = list.item(i);
			if (isBootItem(node)) {
				elements.add((Element) node);
			}
		}
		
		if (elements.size() != 1) {
			throw new IllegalArgumentException("style error!");
		}

		Element element = elements.get(0);
		// 解析标签

		// 绑定的上级菜单
		String value = element.getAttribute(AttachMenu);
		if (value != null && value.trim().length() > 0) {
			root.setAttachMenu(value.trim());
		}
		// 版本
		value = element.getAttribute(Version);
		if (value != null && value.trim().length() > 0) {
			root.setVersion(value.trim());
		}
		
		// 判断是应用，或者其它...
		if (isApplicationToken(element)) {
			WProgram sub = new WProgram();
			// 取出参数
			String yes = element.getAttribute(DesktopAttribute);
			sub.setDesktop(ConfigParser.splitBoolean(yes, false));
			
			yes = element.getAttribute(DockAttribute);
			sub.setDock(ConfigParser.splitBoolean(yes, false));

			splitElement(buffer, sub, element);
			root.setElement(sub);
			// 生成哈希码
			setHash(sub, hash);
		}
		// 是文档
		else  if(isDocumentToken(element)){
			WDocument sub = new WDocument();
			splitElement(buffer, sub, element);
			root.setElement(sub);
			setHash(sub, hash);
		} 
		// 是目录
		else if(isDirectoryToken(element)) {
			WDirectory sub = new WDirectory();
			splitElement(buffer, sub, element);
			root.setElement(sub);
			setHash(sub, hash);
		} else {
			throw new IllegalArgumentException("illegal element!");
		}

//		System.out.println(value);
//		System.out.printf("%s %s\n", root.getAttachMenu(), root.getVersion());
//		System.out.println(root.getElement().getClass().getName());
		
		// 返回结果
		root.getElement().setStart(true);
		return root;
	}
	
	/**
	 * 解析
	 * @param xml
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	public static WRoot split(File file) throws IOException, NoSuchAlgorithmException {
		// 读取字节
		boolean success = (file.exists() && file.exists());
		if (!success) {
			throw new FileNotFoundException("cannot be find " + file.toString());
		}

		// 读取字节数组
		long len = file.length();
		byte[] content = new byte[(int) len];
		FileInputStream in = new FileInputStream(file);
		in.read(content);
		in.close();

		// 解析
		WRoot root = split(content, false); // 不要保存字节数组
		// 保存路径
		root.setPath(file);
		
		// 返回结果
		return root;
	}
	
//	/**
//	 * 解析
//	 * @param xml
//	 * @return
//	 * @throws NoSuchAlgorithmException 
//	 */
//	public WRoot split(File file) throws IOException, NoSuchAlgorithmException {
//		
//		BasketBuffer buffer = new BasketBuffer();
//		// 加载软件包
//		buffer.load(file);
//		byte[] xml = buffer.readBootstrap();
//		
//		// 生成根单元
//		WRoot root = new WRoot();
//		SHA256Hash hash = Laxkit.doSHA256Hash(file);
//		root.setHash(hash);
//		root.setPath(file);
//		
////		System.out.println(new String(xml, "UTF-8"));
//		
//		Document document = XMLocal.loadXMLSource(xml);
//		if (document == null) {
//			throw new IllegalArgumentException("cannot be resolve");
//		}
//		
//		NodeList list = document.getChildNodes();
//		ArrayList<Element> elements = new ArrayList<Element>();
//		
//		System.out.printf("item is %d\n", list.getLength());
//		
//		int size = list.getLength();
//		for (int i = 0; i < size; i++) {
//			Node node = list.item(i);
//			if (isBootItem(node)) {
//				elements.add((Element) node);
//			}
//		}
//		
////		// 找到单元
////		NodeList list = document.getElementsByTagName(BootItem); //.getChildNodes();
////		System.out.printf("item is %d\n", list.getLength());
//		
//		if (elements.size() != 1) {
//			throw new IllegalArgumentException("style error!");
//		}
//		
//		Element element = elements.get(0); // (Element) list.item(0);
//		// 解析标签
//		
//		// 绑定的上级菜单
//		String value = element.getAttribute(AttachMenu);
//		if (value != null && value.trim().length() > 0) {
//			root.setAttachMenu(value.trim());
//		}
//		// 版本
//		value = element.getAttribute(Version);
//		if (value != null && value.trim().length() > 0) {
//			root.setVersion(value.trim());
//		}
//		
//		// 判断是应用，或者其它...
//		if (isApplicationToken( element )) {
//			WProgram sub = new WProgram();
//			createBootItem(buffer, sub, element);
//			root.setElement(sub);
//			setHash(sub, hash);
//		} 
//		// 是文档
//		else  if(isDocumentToken(element)){
//			WDocument sub = new WDocument();
//			createBootItem(buffer, sub, element);
//			root.setElement(sub);
//			setHash(sub, hash);
//		} 
//		// 是目录
//		else if(isDirectoryToken(element)) {
//			WDirectory sub = new WDirectory();
//			createBootItem(buffer, sub, element);
//			root.setElement(sub);
//			setHash(sub, hash);
//		}else {
//			throw new IllegalArgumentException("illegal element!");
//		}
//		
////		// 解析
////		createBootItem(buffer, root, element);
//		
//		System.out.println(value);
//		System.out.printf("%s %s\n", root.getAttachMenu(), root.getVersion());
//		
//
//		
////		NodeList nodes = element.getChildNodes();
////		int size = nodes.getLength();
////		for (int i = 0; i < size; i++) {
////			Node node = nodes.item(i);
////			// 标题
////			if (isTitle(node)) {
////				String text = trim(node);
////				if (text != null && text.trim().length() > 0) {
////					item.setTitle(text.trim());
////				}
////			}
////			// 工具提示
////			else if (isToolTip(node)) {
////				String text = trim(node);
////				if (text != null && text.trim().length() > 0) {
////					item.setToolTip(text.trim());
////				}
////			}
////			// 显示版本
////			else if (isVersion(node)) {
////				// 显示的版本
////				String version = trim(node);
////				if (version != null && version.trim().length() > 0) {
////					item.setVersion(version.trim());
////				}
////			}
////			// 图标
////			else if (isIcon(node)) {
////				BootLocation localtion = splitLocation((Element) node);
////				item.setIcon(localtion);
////			}
////			// 应用
////			else if (isApplication(node)) {
////				BootApplicationItem sub = splitApplicationItem((Element) node);
////				item.setApplication(sub);
////			}
////			// 文档
////			else if (isDocument(node)) {
////				BootDocumentItem sub = splitDocumentItem((Element) node);
////				item.setDocument(sub);
////			}
////			// 如果是子菜单
////			else if (isBootItem(node)) {
////				BootItem sub = createBootItem((Element) node);
////				item.add(sub);
////			}
////		}
//		
////		// 统计单元
////		int count = 0;
////		Element root = null;
////		// 找到第一个
////		NodeList nodes = document.getChildNodes();
////		int size = nodes.getLength();
////		for (int i = 0; i < size; i++) {
////			Node node = nodes.item(i);
////			if (isBootItem(node)) {
////				if (count == 0) {
////					root = (Element) node;
////				}
////				count++;
////			}
////		}
////		// 如果不止一个时...
////		if (count != 1) {
////			throw new IllegalArgumentException("illegal boot.xml");
////		}
////
////		
////		// 输出前，设置迭代编号
////		root.doIterateIndex();
//		
//		// 返回结果
//		return root;
//	}
	
	public void test() throws IOException, NoSuchAlgorithmException {
		File file = new File("d:/notepad.das");
		
//		file = new File("c:/console.das");
		
//		BasketBuffer buffer = new BasketBuffer();
//		// 加载软件包
//		buffer.load(file);
//		byte[] b = buffer.readBootstrap();
		
		WRoot root = split(file);
		ClassWriter writer = new ClassWriter();
		writer.writeObject(root);
		byte[] b = writer.effuse();
		
		ClassReader reader = new ClassReader(b);
		root = new WRoot(reader);
		System.out.printf("%d -> %d\n", b.length, reader.getSeek());
	}

	public static void main(String[] args) {
		WTokenChanger e = new WTokenChanger();
		
		try {
			e.test();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}