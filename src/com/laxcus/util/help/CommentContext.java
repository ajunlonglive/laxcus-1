/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.local.*;
import com.laxcus.xml.*;

/**
 * 命令解释语境
 * 
 * @author scott.liang
 * @version 1.0 9/10/2018
 * @since laxcus 1.0
 */
public class CommentContext {

	/** 解释模板 **/
	private CommentTemplate template = new CommentTemplate();

	/** 保存命令解释单元 **/
	private ArrayList<CommentGroup> array = new ArrayList<CommentGroup>();

	/**
	 * 构造默认的命令解释语境
	 */
	public CommentContext() {
		super();
	}


	/**
	 * 返回解释模板
	 * @return 解释模板实例
	 */
	public CommentTemplate getTemplate() {
		return template;
	}

	/**
	 * 返回模板的字体类型
	 * @return 字体类型
	 */
	public String getTemplateFontName() {
		return template.getFontName();
	}

	/**
	 * 设置模板的字体类型
	 * @param name 字体类型
	 */
	public void setTemplateFontName(String name) {
		template.setFontName(name);
	}

	/**
	 * 把到匹配的参数
	 * @param command 命令的部分
	 * @return 命令语句集
	 */
	public List<CommentElement> findAllComments(String command) {
		ArrayList<CommentElement> a = new ArrayList<CommentElement>();

		for (CommentGroup group : array) {
			List<CommentElement> items = group.list();
			for (CommentElement e : items) {
				if (e.contains(command)) {
					// 不存在时，保存它
					if (!a.contains(e)) {
						a.add(e);
					}
				}
			}
		}

		return a;
	}

	/**
	 * 查找匹配的参数
	 * @param command
	 * @return
	 */
	public CommentElement findComment(String command) {
		for (CommentGroup group : array) {
			List<CommentElement> items = group.list();
			for (CommentElement e : items) {
				if (e.isCommand(command)) {
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * 保存命令解释单元
	 * @param e 命令解释单元
	 * @return 保存返回真，否则假
	 */
	public boolean add(CommentGroup e) {
		Laxkit.nullabled(e);

		remove(e);
		return array.add(e);
	}

	/**
	 * 删除命令解释单元
	 * @param e 命令解释单元
	 * @return 删除返回真，没有返回假
	 */
	public boolean remove(CommentGroup e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}

	/**
	 * 输出全部命令解释单元
	 * @return 命令解释单元列表
	 */
	public List<CommentGroup> list() {
		return new ArrayList<CommentGroup>(array);
	}

	/**
	 * 成员数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 格式化成控制台格式
	 * @param element 命令注释单元
	 * @return 字符串
	 */
	public String formatConsole(CommentElement element) {
		return template.formatConsole(element);
	}

	/**
	 * 格式化成HTML格式
	 * @param element 命令注释单元
	 * @return 字符串
	 */
	public String formatHTML(CommentElement element) {
		return template.formatHTML(element);
	}

	/**
	 * 格式化多个命令
	 * @param elements 命令注释单元集合
	 * @return 字符串
	 */
	public String formatHTMLCommands(List<CommentElement> elements) {
		return template.formatHTMLCommands(elements);
	}

	/**
	 * 更新字体
	 */
	public void updateFont() {
		template.updateFont();
	}

	/**
	 * 从JAR包中的配置文件中加载
	 * @param path JAR包中的文件路径
	 */
	public void load(String path, boolean highScreen) {
		SurfaceLoader loader = new SurfaceLoader();

		// 读出字符
		byte[] xml = loader.findAbsoluteStream(path);
		if (Laxkit.isEmpty(xml)) {
			throw new IllegalArgumentException("cannot be load " + path);
		}
		org.w3c.dom.Document document = XMLocal.loadXMLSource(xml);
		if (document == null) {
			throw new IllegalArgumentException("cannot be resolve byte array");
		}

		// 解释参数
		NodeList list = document.getElementsByTagName("group");
		for (int i = 0; i < list.getLength(); i++) {
			Element parent = (Element) list.item(i);

			String no = parent.getAttribute("no");
			String title = parent.getAttribute("title");

			// 命令集
			CommentGroup group = new CommentGroup();
			group.setNo(Integer.parseInt(no));
			group.setTitle(title);

			// 命令单元
			NodeList subs = parent.getElementsByTagName("item");
			for (int n = 0; n < subs.getLength(); n++) {
				Element sub = (Element) subs.item(n);

				// 取出参数
				title = sub.getAttribute("title");
				String remark = XMLocal.getValue(sub, "remark");
				String syntax = XMLocal.getValue(sub, "syntax");
				String params = XMLocal.getValue(sub, "params");

				// 命令单元
				CommentElement node = new CommentElement(title);
				node.setRemark(remark);
				node.setSyntax(syntax);
				node.setParams(params);

				// 保存命令单元
				group.add(node);
			}
			// 保存命令集
			add(group);
		}

		// 高分辨率
		if (highScreen) {
			list = document.getElementsByTagName("max-template");
			if (list.getLength() == 1) {
				Element sub = (Element) list.item(0);
				template.load(sub);
			}
		}
		// 普通分辨率
		else {
			list = document.getElementsByTagName("template");
			if (list.getLength() == 1) {
				Element sub = (Element) list.item(0);
				template.load(sub);
			}
		}

		// 检查字体正确显示
		checkFontFamily();
	}

	/**
	 * 判断字体能够正确显示一行文本
	 * @param font 字体
	 * @param str 文本
	 * @return 返回真或者假
	 */
	private boolean canDisplay(Font font, String str) {
		return font.canDisplayUpTo(str) == -1;
	}

	/**
	 * 判断这个字体能够正确显示保存的参数
	 * @param font 字体
	 * @return 正确显示返回真，否则假
	 */
	private boolean canDisplay(Font font) {
		for (CommentGroup group : array) {
			for (CommentElement element : group.list()) {
				for (String remark : element.getRemark()) {
					if (!canDisplay(font, remark)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 检查字体，选择一个可以正确显示的。
	 */
	private void checkFontFamily() {
		Font font = new Font(template.getFontName(), Font.PLAIN, 12);
		boolean success = canDisplay(font);

		// 使用系统默认字体，检查全部字符串
		if (!success) {
			for (String name : FontKit.fontNames) {
				font = new Font(name, Font.PLAIN, 12);
				success = (font != null && canDisplay(font));
				if (success) {
					template.setFontName(font.getName());
				}
			}
		}

		// 使用按纽的字体
		if (!success) {
			font = UIManager.getFont("Button.font");
			success = (font != null && canDisplay(font));
			if (success) {
				template.setFontName(font.getName());
			}
		}

		// 如果不能正确显示，从本地选择一个字体
		if (!success) {
			Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for (int i = 0; fonts != null && i < fonts.length; i++) {
				font = fonts[i];
				if (canDisplay(font)) {
					template.setFontName(font.getName());
					break;
				}
			}
		}
	}

}