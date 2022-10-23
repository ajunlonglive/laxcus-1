/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.*;

/**
 * "GUIDE-INF/guides.xml"配置文档读取器。<br>
 * 
 * 解析里面的参数，以类形式返回
 * 
 * @author scott.liang
 * @version 1.0 5/12/2020
 * @since laxcus 1.0
 */
public class GuideConfigReader extends PackageConfigReader {
	
	/**
	 * 构造"GUIDE-INF/guides.xml"配置文档读取器，指定内容
	 * @param b 字节内容
	 * @param off 下标位置
	 * @param len 指定度
	 */
	public GuideConfigReader(byte[] b , int off, int len) {
		super(b, off, len);
	}
	
	/**
	 * 构造"GUIDE-INF/guides.xml"配置文档读取器，指定内容
	 * @param b 字节内容
	 */
	public GuideConfigReader(byte[] b) {
		super(b);
	}
	
	/**
	 * 构造"GUIDE-INF/guides.xml"配置文档读取器，指定磁盘文件
	 * @param file 磁盘文件
	 */
	public GuideConfigReader(File file) throws IOException {
		super(file);
	}
	
	/**
	 * 解析任务组件分段！
	 * @return 返回Sock数组，失败是空指针
	 */
	public List<Sock> readSocks() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return null;
		}

		// 取出软件标签
		WareTag tag = readWareTag();
		if (tag == null) {
			return null;
		}

		// 数组
		ArrayList<Sock> array = new ArrayList<Sock>();

		// 解析单项
		NodeList nodes = document.getElementsByTagName(GuideMark.GUIDE); // "guide");
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element element = (Element) nodes.item(i);
			// 组件命名，存在两种可能：1. "根命名.子命名" 2. "根命名"
			String naming = XMLocal.getValue(element, GuideMark.GUIDE_NAMING); //  "naming");

			// 基础字
			try {
				Sock sock = new Sock(tag.getNaming(), naming);
				array.add(sock);
			} catch (Throwable e) {
				Logger.fatal(e);
				return null;
			}
		}
		return array;
	}

	/**
	 * 读取字符串描述的软件启动配置
	 * @return GuideToken数组，或者是空指针
	 */
	public List<GuideToken> readGuideTokens() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return null;
		}

		// 数组
		ArrayList<GuideToken> array = new ArrayList<GuideToken>();
		
		// 解析单项
		NodeList nodes = document.getElementsByTagName(GuideMark.GUIDE); // "guide");
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element element = (Element) nodes.item(i);

			// 组件命名
			String naming = XMLocal.getValue(element,GuideMark.GUIDE_NAMING); //  "naming");
			// 任务类路径
			String clazz = XMLocal.getValue(element, GuideMark.GUIDE_CLASS); // "boot-class"); 
			// 显示图标在JAR包的路径
			String icon = XMLocal.getValue(element, GuideMark.GUIDE_ICON); // "icon"); 
			// 应用组件标题，展示给用户使用的，类似“product-name”
			String caption = XMLocal.getValue(element, GuideMark.GUIDE_CAPTION); // "caption"); 
			// 工具提示，展示给用户使用的
			String tooltip = XMLocal.getValue(element, GuideMark.GUIDE_TOOLTIP); // "tooltip"); 

			// 生成实例保存
			GuideToken token = new GuideToken(naming);
			token.setBootClass(clazz);
			token.setIcon(icon);
			token.setCaption(caption);
			token.setTooltip(tooltip);
			// 保存！
			array.add(token);
		}
		
		return array;
	}
}