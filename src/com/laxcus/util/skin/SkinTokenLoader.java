/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.util.*;
import java.util.regex.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.loader.*;
import com.laxcus.xml.*;

/**
 * 颜色配置标记加载器
 * 
 * @author scott.liang
 * @version 1.0 2/17/2020
 * @since laxcus 1.0
 */
public class SkinTokenLoader {

	/** 标签名称，全部在 "config.xml"中定义 **/
	private static final String SKIN = "skin";

	private static final String MODE = "mode";

	private static final String TITLE = "title";

	private static final String NAME = "name";
	
	private static final String METHOD = "method";
	
	private static final String LAF = "laf";
	
	private static final String THEME_CLASS = "theme-class";
	
	private static final String ICON = "icon";
	
	/** 效果图链接 **/
	private static final String IMPRESS = "impress";
	
	/** 标签：语言 **/
	private static final String LANGUAGE = "language";

	private static final String COUNTRY = "country";

	/** 保存语言匹配的颜色配置方案 **/
	private ArrayList<SkinToken> tokens = new ArrayList<SkinToken>();

	/**
	 * 构造颜色配置标记加载器
	 */
	public SkinTokenLoader() {
		super();
	}

	/**
	 * 统计皮肤配置方案数目
	 * @return 整数
	 */
	public int size() {
		return tokens.size();
	}

	/**
	 * 找到被选中的皮肤方案
	 * @return SkinToken实例，或者空指针
	 */
	public SkinToken findCheckedSkinToken() {
		for (SkinToken e : tokens) {
			if (e.isChecked()) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 输出全部
	 * @return
	 */
	public List<SkinToken> getSkinTokens() {
		return new ArrayList<SkinToken>(tokens);
	}
	
	/**
	 * 返回全部方法名称
	 * @return 字符串数组
	 */
	public String[] getSkinMethods() {
		int size = tokens.size();
		String[] s = new String[size];
		for (int i = 0; i < size; i++) {
			SkinToken e = tokens.get(i);
			s[i] = e.getMethod();
		}
		return s;
	}
	
	/**
	 * 修改某个皮肤配色
	 * @param skinName 皮肤名称
	 * @return 成功返回真，否则假
	 */
	public boolean exchangeCheckedSkinToken(String skinName) {
		boolean success = false;
		for (SkinToken e : tokens) {
			e.setChecked(false);
			if (e.getName().equalsIgnoreCase(skinName)) {
				success = true;
				e.setChecked(true);
			}
		}
		return success;
	}

	/**
	 * 根据传入的名称，查找匹配的皮肤方案
	 * @param name 名称
	 * @return 颜色方案，没有是空指针
	 */
	public SkinToken findSkinTokenByName(String name) {
		// 忽略空值
		if (name == null || name.isEmpty()) {
			return null;
		}
		for (SkinToken e : tokens) {
			int ret = Laxkit.compareTo(name, e.getName(), false);
			if (ret == 0) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * 根据传入的方法名称，查找匹配的皮肤方案
	 * @param method 方法名称
	 * @return 皮肤颜色方案，没有返回空指针
	 */
	public SkinToken findSkinTokenByMethod(String method) {
		// 忽略空值
		if (method == null || method.isEmpty()) {
			return null;
		}
		for (SkinToken e : tokens) {
			int ret = Laxkit.compareTo(method, e.getMethod(), false);
			if (ret == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 返回皮肤名称
	 * @return 皮肤名称列表
	 */
	public List<String> getSkinNames() {
		ArrayList<String> a = new ArrayList<String>();
		for (SkinToken e : tokens) {
			a.add(e.getName());
		}
		return a;
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
	 * 解析参数
	 * @param element
	 * @return 成功返回真，否则假
	 */
	private boolean resolve(Element element) {
		NodeList list = element.getElementsByTagName(MODE);
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Node node = list.item(i);
			// 判断是ELEMENT
			if (!isElement(node)) {
				continue;
			}

			// 读取属性
			Element sub = (Element) node;
			String titleText = sub.getAttribute(TITLE);
			String nameText = sub.getAttribute(NAME);
			String methodText = sub.getAttribute(METHOD);
			String lafText = sub.getAttribute(LAF);
			String themeClass = sub.getAttribute(THEME_CLASS);
			String iconLink = sub.getAttribute(ICON);
			String impressLink = sub.getAttribute(IMPRESS);
			// 链接文件
			String linkText = sub.getTextContent();
			
			// 不存在，保存参数
			SkinToken token = new SkinToken(titleText, nameText, methodText, lafText, themeClass, linkText);
			token.setIcon(iconLink);
			token.setImpress(impressLink);
			if (!tokens.contains(token)) {
				tokens.add(token);
			}
		}
		return tokens.size() > 0;
	}

	/**
	 * 从配置链中加载
	 * 
	 * @param entryPath JAR文件里的config.xml路径
	 * @return 成功返回真，否则假
	 */
	public boolean load(String entryPath) {
		ResourceLoader loader = new ResourceLoader();
		byte[] b = loader.findAbsoluteStream(entryPath);
		// 解析XML
		Document document = XMLocal.loadXMLSource(b);
		if (document == null) {
			return false;
		}

		// 本地语种
		Locale local = Locale.getDefault();

		Element defaultElement = null;

		NodeList list = document.getElementsByTagName(SKIN);
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Node node = list.item(i);

			// 判断是ELEMENT
			if (!isElement(node)) {
				continue;
			}

			Element element = (Element) node;
			String language = trim(element.getAttribute(LANGUAGE));
			String country = trim(element.getAttribute(COUNTRY));

			// 提取默认单元环境（英语）
			if (language.equalsIgnoreCase("default") && country.equalsIgnoreCase("default")) {
				defaultElement = element;
				continue;
			}

			// 判断语言环境匹配
			boolean match = (language.equalsIgnoreCase(local.getLanguage()) && 
					country.equalsIgnoreCase(local.getCountry()));
			if (match) {
				return resolve(element);
			}
		}

		// 取出默认值
		if (defaultElement != null) {
			return resolve(defaultElement);
		}

		return false;
	}
}
