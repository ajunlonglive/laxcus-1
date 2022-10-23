/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

import java.awt.*;

import com.laxcus.util.display.*;

/**
 * WATCH节点自定义属性
 * 
 * @author scott.liang
 * @version 1.0 2/28/2020
 * @since laxcus 1.0
 */
public class WatchProperties extends LocalProperties {
	
	/** 皮肤名称 **/
	public static final String skinName = "skin.name";

	/** 窗口范围，对应Rectangle值 **/
	public static final String boundFrame = "bound.frame";
	
	/**
	 * 输入窗口范围
	 * @param e
	 * @return
	 */
	public static Object writeWindowBound(Rectangle e) {
		return putProperity(boundFrame, e);
	}

	/**
	 * 读取窗口范围
	 * @return
	 */
	public static Rectangle readWindowBound() {
		Object value = findProperity(boundFrame);
		if (value != null && value.getClass() == Rectangle.class) {
			return (Rectangle) value;
		}
		return null;
	}
	
	/** 分割条位置 **/
	public static final String dividerBrowserPane = "divider.pane.browser";
	
	/**
	 * 浏览面板的上下分割位置
	 * @param location 分割位置值
	 */
	public static Object writeBrowserPaneDeviderLocation(int location) {
		return putProperity(dividerBrowserPane, location);
	}

	/**
	 * 浏览面板的上下分割位置
	 * @return 返回整数值，没有是空指针
	 */
	public static Integer readBrowserPaneDeviderLocation() {
		return findInteger(dividerBrowserPane);
	}
	
	/** 站点浏览器分割条位置 **/
	public static final String dividerSiteBrowserPane = "divider.pane.browser.site";
	
	/**
	 * 站点浏览面板的上下分割位置
	 * @param location 分割位置值
	 */
	public static Object writeSiteBrowserPaneDeviderLocation(int location) {
		return putProperity(dividerSiteBrowserPane, location);
	}

	/**
	 * 站点浏览面板的上下分割位置
	 * @return 返回整数值，没有是空指针
	 */
	public static Integer readSiteBrowserPaneDeviderLocation() {
		return findInteger(dividerSiteBrowserPane);
	}
	
	/** 成员浏览面板分割条位置 **/
	public static final String dividerMemberBrowserPane = "divider.pane.browser.member";
	
	/**
	 * 成员浏览面板的上下分割位置
	 * @param location 分割位置值
	 */
	public static Object writeMemberBrowserPaneDeviderLocation(int location) {
		return putProperity(dividerMemberBrowserPane, location);
	}

	/**
	 * 成员浏览面板的上下分割位置
	 * @return 返回整数值，没有是空指针
	 */
	public static Integer readMemberBrowserPaneDeviderLocation() {
		return findInteger(dividerMemberBrowserPane);
	}
	
	/** 分割条位置 **/
	public static final String dividerCenterPane = "divider.pane.center";
	
	/**
	 * 中心面板的上下分割位置
	 * @param location 分割位置值
	 */
	public static Object writeCenterPaneDeviderLocation(int location) {
		return putProperity(dividerCenterPane, location);
	}

	/**
	 * 中心面板的上下分割位置
	 * @return 返回整数值，没有是空指针
	 */
	public static Integer readCenterPaneDeviderLocation() {
		return findInteger(dividerCenterPane);
	}
	

	/** 系统环境字体，对应Font值 **/
	public static final String fontSystem = "font.system";
	
	/**
	 * 读取系统环境字体
	 * @return 返回字体，没有是空指针
	 */
	public static Object writeSystemFont(Font font) {
		return putProperity(fontSystem, font);
	}
	
	/**
	 * 读取系统环境字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readSystemFont() {
		return findFont(fontSystem);
	}

	/** 集群节点浏览窗口字体，对应Font值 */
	public static final String fontBrowserSite = "font.browser.site";

	/**
	 * 写入集群节点浏览窗口字体
	 * 
	 * @param font 字体
	 * @return 返回旧的值，或者空指针
	 */
	public static Object writeBrowserSiteFont(Font font) {
		return putProperity(fontBrowserSite, font);
	}
	
	/**
	 * 读取集群节点浏览窗口字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readBrowserSiteFont() {
		return findFont(fontBrowserSite);
	}

	/** 集群注册成员浏览窗口字体，对应Font值 **/
	public static final String fontBrowserMember = "font.browser.member";

	/**
	 * 写入集群成员浏览窗口字体
	 * 
	 * @param font 字体
	 * @return 返回旧的值，或者空指针
	 */
	public static Object writeBrowserMemberFont(Font font) {
		return putProperity(fontBrowserMember, font);
	}
	
	/**
	 * 读取集群成员浏览窗口字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readBrowserMemberFont() {
		return findFont(fontBrowserMember);
	}
	
	/** 命令窗口字体，对应Font值 **/
	public static final String fontCommand = "font.command";
	
	/**
	 * 读取命令窗口字体
	 * @return 返回字体，没有是空指针
	 */
	public static Object writeCommandPaneFont(Font font) {
		return putProperity(fontCommand, font);
	}
	
	/**
	 * 读取命令窗口字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readCommandPaneFont() {
		return findFont(fontCommand);
	}
	
	/** TAB选项 **/
	public static final String fontTabbed = "font.tabbed";

	/**
	 * 写入TABBED选项窗口字体
	 * 
	 * @param font 字体
	 * @return 返回旧的值，或者空指针
	 */
	public static Object writeTabbedFont(Font font) {
		return putProperity(fontTabbed, font);
	}
	
	/**
	 * 读取TABBED选项窗口字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readTabbedFont() {
		return findFont(fontTabbed);
	}

	/** 消息窗口字体，对应Font值 **/
	public static final String fontMessage = "font.message";

	/**
	 * 写入选项栏消息字体
	 * @return 返回字体，没有是空指针
	 */
	public static Object writeTabbedMessageFont(Font font) {
		return putProperity(fontMessage, font);
	}
	
	/**
	 * 读取选项栏消息字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readTabbedMessageFont() {
		return findFont(fontMessage);
	}
	
	/** 表格窗口字体，对应Font值 **/
	public static final String fontTable = "font.table";

	/**
	 * 写入选项栏表格字体
	 * @return 返回字体，没有是空指针
	 */
	public static Object writeTabbedTableFont(Font font) {
		return putProperity(fontTable, font);
	}
	
	/**
	 * 读取选项栏表格字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readTabbedTableFont() {
		return findFont(fontTable);
	}
	
	/** 状态栏字体，对应Font值 **/
	public static final String fontSiteStatus = "font.site.status";
	
	/**
	 * 写入选项栏运行时字体
	 * @return 返回旧字体，没有是空指针
	 */
	public static Object writeTabbedRuntimeFont(Font font) {
		return putProperity(fontSiteStatus, font);
	}
	
	/**
	 * 读取选项栏运行时字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readTabbedRuntimeFont() {
		return findFont(fontSiteStatus);
	}

	/** 日志窗口字体，对应Font值 **/
	public static final String fontLog = "font.log";

	/**
	 * 写入选项栏日志字体
	 * @return 返回旧字体，没有是空指针
	 */
	public static Object writeTabbedLogFont(Font font) {
		return putProperity(fontLog, font);
	}

	/**
	 * 读取选项栏日志字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readTabbedLogFont() {
		return findFont(fontLog);
	}
	
	/** 主菜单字体，对应Font值 **/
	public static final String fontMenu = "font.menu";

	/**
	 * 写入主菜单字体
	 * @return 返回旧字体，没有是空指针
	 */
	public static Object writeMainMenuFont(Font font) {
		return putProperity(fontMenu, font);
	}

	/**
	 * 读取主菜单字体
	 * @return 返回字体，没有是空指针
	 */
	public static Font readMainMenuFont() {
		return findFont(fontMenu);
	}

	/** 帮助窗口字体类型名称，对应String值 **/
	public static final String fontHelp = "font.help";

	/**
	 * 写入帮助菜单类型
	 * @return 返回旧字体类型，没有是空指针
	 */
	public static Object writeHelpMenuFontFamily(String fontFamily) {
		return putProperity(fontHelp, fontFamily);
	}

	/**
	 * 读取帮助菜单类型
	 * @return 返回字体类型，没有是空指针
	 */
	public static String readHelpMenuFontFamily() {
		return findString(fontHelp);
	}
	
	/** 是否播放声音，对应Boolean值 **/
	public static final String soundPlay = "sound.play";

	/**
	 * 写入声音播放
	 * @return 返回旧字体类型，没有是空指针
	 */
	public static Object writeSoundPlay(boolean play) {
		return putProperity(soundPlay, play);
	}

	/**
	 * 读取声音播放
	 * @return 返回字体类型，没有是空指针
	 */
	public static Boolean readSoundPlay() {
		return findBoolean(soundPlay);
	}
	
	/** 日志显示数目，对应Integer值 **/
	public static final String logElements = "log.elements";

	/**
	 * 写入日志数目
	 * @return 返回旧字体类型，没有是空指针
	 */
	public static Object writeLogElements(int count) {
		return putProperity(logElements, count);
	}

	/**
	 * 读取日志数目
	 * @return 返回日志数目，没有是空指针
	 */
	public static Integer readLogElements() {
		return findInteger(logElements);
	}

	/** 拒绝日志显示，对应Boolean值 **/
	public static final String logForbid = "log.forbid";

	/**
	 * 写入拒绝日志显示
	 * @return 返回旧字体类型，没有是空指针
	 */
	public static Object writeLogForbid(boolean forbid) {
		return putProperity(logForbid, forbid);
	}

	/**
	 * 读取拒绝日志显示
	 * @return 返回拒绝日志显示，没有是空指针
	 */
	public static Boolean readLogForbid() {
		return findBoolean(logForbid);
	}
}