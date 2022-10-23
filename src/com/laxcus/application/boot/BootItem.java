/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

import java.io.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.util.skin.*;

/*
 * 格式
<BootItem attach-menu="" system="yes">

	<Title> </Title> <!-- 软件名称 -->
	<Icon>  <!-- 图标位置 -->
		<JURL> </JURL> <!-- 在JAR软件包中的路径 -->
		<URI> </URI> <!-- 在文档中的路径 -->
	</Icon> 
	<Version> </Version> <!-- 软件版本号，只在根出现 -->
	
	<Application>
		<Command> </Command>
		<Class> </Class> <!-- 启动类 -->
	</Application>
	
	<Document> 
		<JURL> </JURL> <!-- 在JAR软件包中的路径 -->
		<URI> <URI> <!-- 在文档中的路径 -->
		<OpenCommand> </OpenCommand> <!-- 打开它的文件 -->
	</Document>

	<!-- 子级单元 -->
	<BootItem>
		
	</BootItem>
	
	
</BootItem>
 */

/**
 * 引导单元
 * 
 * 包含多个零至任意多个子单元。如果存在子单元，那么这是一个父菜单
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class BootItem {
	
	/** 迭代编号 **/
	private int iterateIndex;
	
	/** 磁盘文件 **/
	private File file;
	
	/** 是系统应用 **/
	private boolean system;
	
	/** 绑定菜单 **/
	private String attchMenu;

	/** 标题，显示在UI界面上 **/
	private String title;
	
	/** 工具提示 **/
	private String tooltip;
	
	/** 安装到桌面 **/
	private boolean desktop;
	
	/** 安装到应用坞 **/
	private boolean dock;
	
	/** 图标位置路径 **/
	private BootLocation icon;

	/** 图标位置 **/
	private String version;
	
	/** 应用单元 **/
	private BootApplicationItem application;
	
	/** 文档单元 **/
	private BootDocumentItem document;

	/** 子级单元，如果包含子级单元，那么父单元在菜单条上不启动 **/
	private ArrayList<BootItem> array = new ArrayList<BootItem>();

	/**
	 * 构造引导单元
	 */
	public BootItem() {
		super();
		setIterateIndex(0);
	}
	
	/**
	 * 设置迭代编号
	 * @param i
	 */
	private void setIterateIndex(int i) {
		iterateIndex = i;
	}
	
	/**
	 * 返回迭代编号
	 * @return
	 */
	public int getIterateIndex(){
		return iterateIndex;
	}
	
	/**
	 * 只有根单元处理这个操作
	 */
	protected void doIterateIndex() {
		setIterateIndex(0);
		for (BootItem item : array) {
			item.setIterateIndex(iterateIndex + 1);
		}
	}
	
	/**
	 * 判断是子级单元，大于0即是子级对象
	 * 
	 * @return
	 */
	public boolean isSubItem() {
		return getIterateIndex() > 0;
	}
	
	/**
	 * 判断有子对象
	 * 
	 * @return 返回真或者假
	 */
	public boolean hasSubObject() {
		return array.size() > 0;
	}
	
	/**
	 * 设置文件
	 * @param e
	 */
	public void setFile(File e) {
		file = e;
		// 给子级设置文件包路径
		for (BootItem item : array) {
			item.setFile(e);
		}
	}

	/**
	 * 返回文件
	 * @return
	 */
	public File getFile() {
		return file;
	}
	
	public boolean isApplication() {
		return application != null;
	}

	public void setApplication(BootApplicationItem e) {
		application = e;
	}

	public BootApplicationItem getApplication() {
		return application;
	}

	public boolean isDocument() {
		return document != null;
	}

	public void setDocument(BootDocumentItem e) {
		document = e;
	}

	public BootDocumentItem getDocument() {
		return document;
	}
	
	public void setSystem(boolean b){
		system = b;
	}
	
	public boolean isSystem() {
		return system;
	}
	
	public boolean isUser() {
		return !system;
	}
	
	public void setAttachMenu(String s) {
		attchMenu = s;
	}
	
	public String getAttachMenu() {
		return attchMenu;
	}
	
	public void setDesktop(boolean b) {
		desktop = b;
	}
	
	public boolean isDesktop(){
		return desktop;
	}

	public void setDock(boolean b) {
		dock = b;
	}
	
	public boolean isDock(){
		return dock;
	}

	/**
	 * 设置图标位置
	 * @param s
	 */
	public void setIcon(BootLocation s) {
		icon = s;
	}

	/**
	 * 返回图标位置
	 * @return
	 */
	public BootLocation getIcon() {
		return icon;
	}
	
	public ImageIcon getIcon(BasketBuffer buffer, int w, int h) throws IOException {
		if (icon.getURI() != null) {
			byte[] bytes = buffer.getURI(icon.getURI());
			return ImageUtil.scale(bytes, w, h);
		} else if (icon.getJURI() != null) {
			byte[] bytes = buffer.getJURI(icon.getJURI());
			return ImageUtil.scale(bytes, w, h);
		}
		return null;
	}

	public ImageIcon getIcon(BasketBuffer buffer) throws IOException {
		if (icon.getURI() != null) {
			byte[] bytes = buffer.getURI(icon.getURI());
			return new ImageIcon(bytes);
		} else if (icon.getJURI() != null) {
			byte[] bytes = buffer.getJURI(icon.getJURI());
			return new ImageIcon(bytes);
		}
		return null;
	}

	/**
	 * 设置名称，显示在界面上的
	 * @param s
	 */
	public void setTitle(String s) {
		title = s;
	}

	/**
	 * 返回名称
	 * @return
	 */
	public String getTitle(){
		return title;
	}	

	/**
	 * 设置工具提示，显示在界面上的
	 * @param s
	 */
	public void setToolTip(String s) {
		tooltip = s;
	}

	/**
	 * 返回工具提示
	 * @return
	 */
	public String getToolTip(){
		return tooltip;
	}	

	/**
	 * 设置图标位置
	 * @param s
	 */
	public void setVersion(String s) {
		version = s;
	}

	/**
	 * 返回图标位置
	 * @return
	 */
	public String getVersion() {
		return version;
	}
	
	public void add(BootItem item) {
		array.add(item);
	}
	
	public List<BootItem> list () {
		return new ArrayList<BootItem>(array);
	}
	
	
}
