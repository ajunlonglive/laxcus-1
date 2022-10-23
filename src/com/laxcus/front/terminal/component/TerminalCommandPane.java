/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.laxcus.front.terminal.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;
import com.laxcus.xml.*;

/**
 * 分布描述指令输入窗口。<br>
 * 这个窗口只接受用户命令。集群管理命令将拒绝。
 * 
 * 每次按键后，都对语句进行语法检查，字符分为关键字、数据类型、其它字符三种。不同的单词用不同的颜色进行显示。
 * 
 * @author scott.liang
 * @version 1.1 11/19/2017
 * @since laxcus 1.0
 */
public class TerminalCommandPane extends JTextPane {

	private static final long serialVersionUID = -6457052712264868263L;

	/** 分布描述语言语法集合 **/
	private ArrayList<SyntaxItem> array = new ArrayList<SyntaxItem>();

	/** 文本资源 **/
	protected StyleContext context;

	/** 富文本模板 **/
	protected DefaultStyledDocument document;

	/** 文本属性 **/
	private SimpleAttributeSet command, keyword, type, normal;

	/** 富文本属性 **/
	private MutableAttributeSet attributes;

	/** 制表符占位尺寸，默认是3 **/
	private int tabSize = 3;

	/**
	 * 建立默认的输入窗口
	 */
	public TerminalCommandPane() {
		super();
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 实例
		attributes = new RTFEditorKit().getInputAttributes();
		// 命令关键字颜色
		command = new SimpleAttributeSet();
		// 一般关键字颜色
		keyword = new SimpleAttributeSet();
		// 数据类型颜色，用兰色
		type = new SimpleAttributeSet();
		// 普通单词颜色
		normal = new SimpleAttributeSet();
		// 加载默认的字体颜色
		loadDefineColor();

		// 加载关键字
		loadArchive("conf/front/syntax/tokens.xml");
		
		// 生成!
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		
		// 设置新的
		setDocument(document);

		// 设置字体
		Font font = TerminalProperties.readCommandPaneFont();
		if (font == null) {
			Font f = getFont();
			if (f != null) {
				font = new Font(f.getName(), Font.BOLD, f.getSize() + 6);
			}
		}
		setSelectFont(font);

		setOpaque(true);

		// 取得键盘事件
		addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				// checkSyntax();
				addThread(new KeyThread());
			}
		});
	}
	
	/**
	 * 加载默认的颜色，如果没有使用默认颜色
	 */
	private void loadDefineColor() {
		// 命令
		Color color = Skins.findCommandPaneCommandForeground();
		if (color == null) {
			color = new Color(0, 177, 106);
		}
		setCommandColor(color);

		// 关键字
		color = Skins.findCommandPaneKeywordForeground();
		if (color == null) {
			color = new Color(20, 162, 212);
		}
		setKeywordColor(color);

		// 类型
		color = Skins.findCommandPaneTypeForeground();
		if (color == null) {
			color = new Color(0, 92, 255);
		}
		setTypeColor(color);

		// 普通字符
		color = Skins.findCommandPaneNormalForeground();
		if (color == null) {
			color = Color.BLACK;
		}
		setNormalColor(color);
	}
	
	/**
	 * 设置命令关键字颜色
	 * @param rgb RGB三原色
	 */
	public void setCommandColor(Color rgb) {
		StyleConstants.setForeground(command, rgb);
	}

	/**
	 * 设置一般关键字颜色
	 * @param rgb RGB三原色
	 */
	public void setKeywordColor(Color rgb) {
		StyleConstants.setForeground(keyword, rgb);
	}

	/**
	 * 设置数据类型颜色
	 * @param rgb RGB三原色
	 */
	public void setTypeColor(Color rgb) {
		StyleConstants.setForeground(type, rgb);
	}

	/**
	 * 设置普通单词颜色
	 * @param rgb RGB三原色
	 */
	public void setNormalColor(Color rgb) {
		StyleConstants.setForeground(normal, rgb);
	}

	/**
	 * 加载标记
	 * @param document
	 */
	private void loadToken(Document document) {
		NodeList nodes = document.getElementsByTagName("token");
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element elem = (Element) nodes.item(i);

			String commandText = XMLocal.getValue(elem, "command");
			String keywordText = XMLocal.getValue(elem, "keywords");
			String typeText = XMLocal.getValue(elem, "types");
			
			SyntaxItem item = new SyntaxItem();
			item.format(commandText, keywordText, typeText);
			item.setCommandText(commandText);
			
			array.add(item);
		}
		// 降序排序，字符串长的在前面
		Collections.sort(array);
	}

	/**
	 * 加载文件。首先检查是磁盘，没有从JAR包里查找。
	 * @param path 路径，可能是磁盘文件或者JAR资源文件
	 * @return 返回字节数组，或者空指针
	 */
	private byte[] loadFile(String path) {
		File file = new File(path);
		// 1. 判断是磁盘文件
		if(file.exists() && file.isFile()) {
			try {
				byte[] b = new byte[(int)file.length()];
				FileInputStream in = new FileInputStream(file);
				in.read(b);
				in.close();
				return b;
			} catch (IOException e) {
				Logger.error(e);
			}
			return null;
		}
		// 2. 去JAR文件里找
		ResourceLoader loader = new ResourceLoader();
		return loader.findStream(path);
	}

	/**
	 * 从JAR档案文件中解析配置参数，加载到内存中
	 * 
	 * @param path 配置文件的JAR路径
	 * @return 成功返回真，否则假
	 */
	public boolean loadArchive(String path) {
		// 如果是空值，是错误
		if (path == null || path.trim().isEmpty()) {
			Logger.warning(this, "loadArchive", "null pointer!");
			return false;
		}
		// 从磁盘或者JAR包里找
		byte[] b = loadFile(path);
		if (Laxkit.isEmpty(b)) {
			Logger.warning(this, "loadArchive", "cannot be find %s", path);
			return false;
		}

		Document document = XMLocal.loadXMLSource(b);
		if (document == null) {
			return false;
		}

		// 加载命令标签
		loadToken(document);
		return true;
	}

	/**
	 * 解析文本，先找到匹配的关键字
	 * @param input 输入语句
	 * @return 关键字集合
	 */
	private SyntaxToken[] splitToken(String input) {
		for (SyntaxItem token : array) {
			if (token.matchs(input)) {
				return token.resolve(input);
			}
		}
		return null;
	}

	/**
	 * 清除窗口显示
	 */
	public void clear() {
		// 清除文本
		addThread(new ClearThread());
	}

	/**
	 * 检查输入语句
	 */
	private void checkSyntax() {
		String input = super.getText();
		if (input.isEmpty()) {
			return;
		}

		// 解析输入，返回关键字集合
		SyntaxToken[] tokens = splitToken(input);

		document.setCharacterAttributes(0, input.length(), normal, false);

		// 空集合不处理
		if (Laxkit.isEmpty(tokens)) {
			return;
		}
		// 显示不同关键字
		for (SyntaxToken token : tokens) {
			int index = token.getIndex();
			int size = token.getSize();

			if (token.isKeyword()) {
				document.setCharacterAttributes(index, size, keyword, false);
			} else if (token.isType()) {
				document.setCharacterAttributes(index, size, type, false);
			} else if (token.isCommand()) {
				document.setCharacterAttributes(index, size, command, false);
			}
		}
		attributes.addAttributes(normal);
	}

	/**
	 * 返回选择的字体
	 * @return
	 */
	public Font getSelectFont() {
		return super.getFont();
	}

	/**
	 * 设置新的选择字体
	 * @param font
	 */
	public void setSelectFont(Font font) {
		// 忽略！
		if (font == null || font instanceof javax.swing.plaf.FontUIResource) {
			return;
		}
		
		addThread(new FontThread(font));
		addThread(new TabSizeThread(tabSize));
	}

	/**
	 * 返回TAB制表符占位尺寸
	 * @return
	 */
	public int getTabSize() {
		return tabSize;
	}

	/**
	 * 设置TAB制表符占位的尺寸。根据字体的镑值为一个计量单位
	 * @param number 空格符数量
	 */
	protected void setTabSize(int number) {
		addThread(new TabSizeThread(tabSize));
	}

	/**
	 * 替换文本
	 * @param e
	 */
	public void replaceText(String e) {
		addThread(new ReplaceThread(e));
	}
	
	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	/**
	 * 执行键盘点击事件
	 *
	 * @author scott.liang
	 * @version 1.0 3/30/2020
	 * @since laxcus 1.0
	 */
	class KeyThread extends SwingEvent {

		public KeyThread() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			checkSyntax();
		}
	}
	
	class ClearThread extends SwingEvent {
		ClearThread() {super();}
		public void process(){
			setText("");
		}
	}

	class FontThread extends SwingEvent {
		Font font;
		FontThread(Font e) {
			super();
			font = e;
		}
		public void process() {
			if (font != null) {
				setFont(font);
			}
		}
	}
	
	class ReplaceThread extends SwingEvent {
		String text;

		ReplaceThread(String s) {
			super();
			text = s;
		}

		public void process() {
			setText(text);
			checkSyntax();
		}
	}
	
	class TabSizeThread extends SwingEvent {
		int number;
		
		TabSizeThread(int size) {
			super();
			number = size;
		}
		
		public void process() {
			Font font = getFont();
			float pos =	font.getSize2D();

			TabStop stop = new TabStop(pos * (tabSize = number));
			TabSet set = new TabSet(new TabStop[] { stop });

			// 增加新的属性
			AttributeSet pset = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, set);
			// 更新
			setParagraphAttributes(pset, false);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.JTextComponent#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 修改!
		if (document != null) {
			// 加载颜色
			loadDefineColor();
			// 修改显示文本
			checkSyntax();
		}
	}

}