/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.local;

import java.util.*;
import java.util.regex.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.loader.*;
import com.laxcus.xml.*;

/**
 * 本地语言配置文件选择器。<br><br>
 * 
 * 通过传入一个根配置文件，匹配当前JRE的地区定义，从中选中一个最合适的配置文件。<br>
 * 
 * 配置文件格式示例：<br><br>
 * 
 * <config>
 * 	<invoker language="zh" country="CN"> <![CDATA[ conf/watch/invoker/zh_CN/invokers.xml ]]> </invoker>
 * </config>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2015
 * @since laxcus 1.0
 */
public class LocalSelector {

	/**
	 * 地区定义
	 * 
	 * @author scott.liang
	 * @version 1.0 11/25/2013
	 * @since laxcus 1.0
	 */
	class Local implements Comparable<Local> {

		/** 语言 **/
		String language;

		/** 国家 **/
		String country;

		/**
		 * 构造地区定义
		 * @param language 语言
		 * @param country 国家
		 */
		public Local(String language, String country) {
			super();
			this.language = language;
			this.country = country;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Local that) {
			int ret = Laxkit.compareTo(language, that.language, false);
			if (ret == 0) {
				ret = Laxkit.compareTo(country, that.country, false);
			}
			return ret;
		}
	}

	/** 入口文件路径 **/
	private String entryPath;

	/**
	 * 构造提示资源选择器，指定入口文件路径
	 * @param entry 入口文件路径
	 */
	public LocalSelector(String entry) {
		super();
		setEntryPath(entry);
	}

	/**
	 * 设置入口文件路径
	 * @param path 文件路径
	 */
	private void setEntryPath(String path) {
		Laxkit.nullabled(path);
		
		entryPath = path;
	}

	/**
	 * 默认定义
	 * @return
	 */
	private Local createDefaultKey() {
		return new Local("default", "default");
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
	 * 过滤两侧空格
	 * @param text 字符串
	 * @return 过滤后的字符串
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
	 * 获得对应标签的配置数据
	 * @param tag 标签
	 * @return 配置类型
	 */
	private Map<Local, String> fatch(String tag) {
		ResourceLoader loader = new ResourceLoader();
		byte[] b = loader.findAbsoluteStream(entryPath);
		// 解析XML
		Document document = XMLocal.loadXMLSource(b);
		if (document == null) {
			return null;
		}

		Map<Local, String> map = new TreeMap<Local, String>();

		NodeList list = document.getElementsByTagName(tag);
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Node node = list.item(i);
			// 判断是ELEMENT
			if (!isElement(node)) {
				continue;
			}

			Element sub = (Element) node;
			String language = trim(sub.getAttribute("language"));
			String country = trim(sub.getAttribute("country"));

			Local local = new Local(language, country);

			String path = trim(sub.getTextContent());
			map.put(local, path);
		}

		return map;
	}

	/**
	 * 根据标签查找对应的配置文件路径
	 * @param tag 标记
	 * @return 文件路径文本
	 */
	public String findPath(String tag) {
		Map<Local, String> paths = fatch(tag);
		if (paths == null) {
			return null;
		}
		// 本地语种
		Locale local = Locale.getDefault();
		String language = local.getLanguage();
		String country = local.getCountry();

		Local key = new Local(language, country);
		String path = paths.get(key);
		if(path == null) {
			key = createDefaultKey();
			path = paths.get(key);
		}
		return path;
	}

}