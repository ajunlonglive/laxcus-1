/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 命令解释模板，生成HTML格式文本。
 * 
 * @author scott.liang
 * @version 1.0 9/10/2018
 * @since laxcus 1.0
 */
public class CommentTemplate {
	
	/**
	 * 把颜色转成16进制字符串
	 * @param color 颜色
	 * @return 输出字节串
	 */
	private static String toHex(Color color) {
		int[] values = new int[] { color.getRed(), color.getGreen(), color.getBlue() };
		StringBuilder bf = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			String s = String.format("%X", values[i]);
			if (s.length() == 1) {
				s = "0" + s;
			}
			bf.append(s);
		}
		return bf.toString();
	}
	
	/**
	 * 生成背景
	 * @return 字符串
	 */
	public static String createBackground() {
		Color background = Skins.findHTMLHelpPanelBackground();
		if (background == null) {
			background = UIManager.getColor("TextPane.background");
			if (background == null) {
				background = Color.WHITE;
			}
		}

		String hex = CommentTemplate.toHex(background);
		return String.format("bgcolor=\"#%s\"", hex);
	}

	/**
	 * 生成文本前景色
	 * @return 字符串
	 */
	public static String createTextForeground() {
		Color foreground = Skins.findHTMLHelpPanelTextForeground();
		if (foreground == null) {
			foreground = Color.BLACK;
		}

		String hex = CommentTemplate.toHex(foreground);
		return String.format("text=\"#%s\" ", hex);
	}

	/**
	 * 生成链接的前景色
	 * @return 字符串
	 */
	public static String createLinkForeground() {
		Color color = Skins.findHTMLHelpPanelHerf();
		if (color == null) {
			color = Color.BLUE;
		}
		String hex = CommentTemplate.toHex(color);
//		return String.format("alink=\"#%s\"", hex);
		return String.format("color=\"#%s\"", hex);
	}

	private final String CRLF = "\r\n";

	/** HTML文本字体，全部一致！ **/
	private String fontName;

	/** 命令标题字体尺寸 **/
	private String commandTitleFontSize = "";

	/** 说明标题、内容字体尺寸，标题名称 **/
	private String remarkTitleFontSize = "";
	private String remarkTitle = "";
	private String remarkContentFontSize = "";

	private String syntaxTitleFontSize = "";
	private String syntaxTitle = "";
	private String syntaxContentFontSize = "";

	private String paramsTitleFontSize = "";
	private String paramsTitle = "";
	private String paramsContentFontSize = "";

	/**
	 * 构造默认的命令解释模板
	 */
	public CommentTemplate() {
		super();
	}

	/**
	 * 设置字体名
	 * @param e
	 */
	public void setFontName(String e) {
		Laxkit.nullabled(e);
		fontName = e;
	}

	/**
	 * 返回字体名
	 * @return
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * 找子节点
	 * @param parent
	 * @param tag
	 * @return
	 */
	private Element findSub(Element parent, String tag) {
		NodeList list = parent.getElementsByTagName(tag);
		if (list.getLength() == 1) {
			return (Element) list.item(0);
		}
		return null;
	}
	
	/**
	 * 获得默认的字体
	 * @return 当前环境默认字体
	 */
	private String getPreferredFont() {
		String[] names = { "新宋体", "DialogInput", "宋体", "Dialog", "Monospaced" };
		for (int i = 0; i < names.length; i++) {
			boolean success = FontKit.hasFontName(names[i]);
			if (success) {
				return names[i];
			}
		}
		
		// 环境中的字体
		String text = UIManager.getString("FontDialog.Hello");
		if (text == null) {
			text = "Hello Laxcus";
		}
		names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		if (names != null) {
			for (String name : names) {
				Font font = new Font(name, Font.PLAIN, 12);
				boolean success = FontKit.canDisplay(font, text);
				if (success) {
					return name;
				}
			}
		}
		// 没有找到
		return null;
	}
	
	/**
	 * 更新字体
	 * @return 成功返回真，否则假
	 */
	public boolean updateFont() {
		String str = UIManager.getString("FontDialog.Hello");
		if (str == null) {
			str = "Hello Laxcus";
		}

		// 找到匹配的字体
		Font font = UIManager.getFont("Label.font");
		if (font != null) {
			if (FontKit.canDisplay(font, str)) {
				fontName = font.getName();
				return true;
			}
		}
		return false;
	}

	/**
	 * 加载参数
	 * @param element
	 */
	public void load(Element element) {
		// 首先更新字体，如果不支持，用环境中默认的
		if (!updateFont()) {
			// 环境中的字体
			fontName = element.getAttribute("font-family");
			// 没有这个字体时
			if (!FontKit.hasFontName(fontName)) {
				String s = getPreferredFont();
				if (s != null) {
					fontName = s;
				}
			}
		}

		// 命令
		Element sub = findSub(element, "command");
		commandTitleFontSize = sub.getAttribute("title-font-size");

		// 说明
		sub = findSub(element, "remark");
		remarkTitleFontSize = sub.getAttribute("title-font-size");
		remarkTitle = sub.getAttribute("title");
		remarkContentFontSize = sub.getAttribute("content-font-size");

		// 语法
		sub = findSub(element, "syntax");
		syntaxTitleFontSize = sub.getAttribute("title-font-size");
		syntaxTitle = sub.getAttribute("title");
		syntaxContentFontSize = sub.getAttribute("content-font-size");

		// 变量
		sub = findSub(element, "params");
		paramsTitleFontSize = sub.getAttribute("title-font-size");
		paramsTitle = sub.getAttribute("title");
		paramsContentFontSize = sub.getAttribute("content-font-size");
	}

	/**
	 * 格式化字符串
	 * @param element
	 * @return
	 */
	public String formatConsole(CommentElement element) {
		StringBuilder bf = new StringBuilder();
		bf.append(element.getRemark());
		bf.append(CRLF);
		bf.append(CRLF);

		for (String input : element.getSyntax()) {
			bf.append(input);
			bf.append(CRLF);
		}
		bf.append(CRLF);
		// 参数段
		for (String input : element.getParams()) {
			bf.append(input);
			bf.append(CRLF);
		}
		return bf.toString();
	}

	/**
	 * 生成标题，字体加粗
	 * @param fontSize
	 * @param title
	 * @return
	 */
	private String doTitle(String fontSize, String title) {
//		title = title.replace(" ", "&nbsp;");
		return String.format("<p><span style=\" font-weight:bold; font-size:%s; font-family:'%s'; \"> %s </span></p>",
				fontSize, fontName, title);
	}

	/**
	 * 生成内容段
	 * @param fontSize
	 * @param content
	 * @return
	 */
	private String doContent(String fontSize, String content) {
//		content = content.replace(" ", "&nbsp;");
		return String.format("<p><span style=\" font-size:%s; font-family:'%s'; \"> %s </span></p>",
				fontSize, fontName, content);
	}

	/**
	 * 格式化成HTML格式
	 * @param element
	 * @return
	 */
	public String formatHTML(CommentElement element) {		
		// 命令
		StringBuilder bf = new StringBuilder();
		bf.append(doTitle(commandTitleFontSize, element.getCommand()));
		bf.append("<p></p>");

		// 说明
		bf.append(doTitle(remarkTitleFontSize, remarkTitle));
		for (String remark : element.getRemark()) {
			bf.append(doContent(remarkContentFontSize, remark));
		}
		bf.append("<p></p>");
		
		// 语法
		bf.append(doTitle(syntaxTitleFontSize, syntaxTitle));
		for (String syntax : element.getSyntax()) {
			bf.append(doContent(syntaxContentFontSize, syntax));
		}
		bf.append("<p></p>");

		// 参数
		bf.append(doTitle(paramsTitleFontSize, paramsTitle));
		for (String params : element.getParams()) {
			bf.append(doContent(paramsContentFontSize, params));
		}
		
		String background = CommentTemplate.createBackground();
		String foreground = CommentTemplate.createTextForeground();
		
		return String.format("<HTML><head><</head> <BODY %s %s>%s</BODY></HTML>", background, foreground, bf.toString());
		
//		return String.format("<HTML><head><</head> <BODY %s>%s</BODY></HTML>", foreground, bf.toString());
	}

	/**
	 * 格式化多个命令，只显示命令本身
	 * @param elements
	 * @return HTML文档
	 */
	public String formatHTMLCommands(List<CommentElement> elements) {
		String linkForeground = CommentTemplate.createLinkForeground();
		
		StringBuilder bf = new StringBuilder();
		for (CommentElement e : elements) {
			String description = e.getCommand();
//			String text = description.replace(" ", "&nbsp;");
			
			String text = description;
			String link = String.format("<p><span style=\"font-size:%s; font-family:'%s';\"><a href=\"%s\" %s>%s</a></span></p>",
					commandTitleFontSize, fontName, description, linkForeground, text);
			bf.append(link);
		}
		String background = CommentTemplate.createBackground();
		String foreground = CommentTemplate.createTextForeground();
		
		return String.format("<HTML><head><</head><BODY %s %s>%s</BODY></HTML>", background, foreground, bf.toString());
	}
}