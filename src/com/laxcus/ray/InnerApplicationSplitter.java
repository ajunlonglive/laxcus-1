/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.laxcus.util.classable.*;
import com.laxcus.xml.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2021-8-1
 * @since laxcus 1.0
 */
class InnerApplicationSplitter {
	
	private boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	private String[] split(byte[] xml) {
		Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve");
		}

		// 取出“LaunchMenu”，这是导航菜单起点
		NodeList nodes = document.getElementsByTagName("inner-applications");
		int size = nodes.getLength();
		if (size != 1) {
			throw new IllegalArgumentException("cannot be resolve 'MenuBar'");
		}
		
		
		
		ArrayList<String> array = new ArrayList<String>();
		
		// 从第一个单元开始，逐一读取并且建立菜单。最外层没有“Menu”项。
		Element root = (Element) nodes.item(0);
		
		nodes =	root.getElementsByTagName("application");
		
		size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);

			if (!isElement(node)) {
				continue;
			}

			Element e = (Element) node;
			String link = e.getTextContent().trim();

			if (link.length() > 0) {
				array.add(link);
			}

		}
		
		if(array.isEmpty()) {
			return new String[0];
		}
		
		String[] a = new String[array.size()];
		// 返回菜单条
		return array.toArray(a);
	}
	
	public String[] split(String name) {
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
			return new String[0];
		}
		
		return split(buff.effuse());
	}
}
