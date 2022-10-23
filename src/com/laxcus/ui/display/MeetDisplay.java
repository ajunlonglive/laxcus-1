/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

import java.util.*;

import com.laxcus.util.display.graph.*;
import com.laxcus.util.display.show.*;

/**
 * 交互式的显示接口。<br><br>
 * 
 * 显示接口处理的内容包括提示信息，以及数据处理结果内容两种可能。
 * 用于图形/字符客户端，以及FRONT站点的图形/字符/应用驱动接口（API）上的数据显示。具体内容的显示由实现端来处理。<br>
 * 
 * 使用显示接口的包括LAXCUS/SQL指令，CONDUCT.PUT/ESTABLISH.END/CONTACT.NEAR阶段。
 * 
 * @author scott.liang
 * @version 1.2 11/9/2013
 * @since laxcus1.0
 */
public interface MeetDisplay {

	/**
	 * 判断显示接口处于可用的状态。即显示接口没有释放，可以输出参数到显示接口。
	 * 
	 * @return 返回真或者假
	 */
	boolean isUsabled();

	/**
	 * 批准信息
	 * @param text
	 * @return 返回真或者假
	 */
	boolean approveLicence(String text);

	/**
	 * 在屏幕上追加一行普通信息
	 * @param text 字符文本
	 * @param sound 发出声音
	 */
	void message(String text, boolean sound);

	/**
	 * 在屏幕上追加一行普通信息
	 * @param text 字符文本
	 */
	void message(String text);

	/**
	 * 在屏幕上追加一行警告信息
	 * @param text 字符文本
	 */
	void warning(String text);

	/**
	 * 在屏幕上追加一行警告信息
	 * @param text 字符文本
	 * @param sound 发出声音
	 */
	void warning(String text, boolean sound);

	/**
	 * 在屏幕上追加一行故障信息
	 * @param text 字符文本
	 */
	void fault(String text);

	/**
	 * 在屏幕上追加一行故障信息
	 * @param text 字符文本
	 * @param focus 发出声音
	 */
	void fault(String text, boolean sound);

	/**
	 * 清除全部文本
	 */
	void clearPrompt();

	/**
	 * 返回标题单元数目
	 * @return 单元数目
	 */
	int getTitleCellCount();

	/**
	 * 在显示接口的标题栏设置一组标题。 标题的功能类似于“列属性集合”。
	 * 标题设置前，旧标题和内容将被删除。
	 * @param title ShowTitle实例
	 */
	void setShowTitle(ShowTitle title);

	/**
	 * 在内容栏增加一组显示项。显示项的功能于“列”等同。
	 * 注意，增加的单元数组必须与标题尺寸一致，否则将弹出异常。
	 * @param item ShowItem实例
	 */
	void addShowItem(ShowItem item);

	/**
	 * 显示表
	 * @param title 标题
	 * @param items 单元
	 */
	void showTable(ShowTitle title, Collection<ShowItem> items);

	/**
	 * 释放表格显示内容
	 */
	void clearShowItems();

	/**
	 * 在状态栏设置消息
	 * @param text
	 */
	void setStatusText(String text);

	/**
	 * 显示一项图文
	 * @param item GraphItem实例
	 */
	void flash(GraphItem item);

	/**
	 * 清除图文
	 */
	void clearGraph();

	/**
	 * 返回异步处理结果监听器
	 * 如果是第三方应用，并且没有标准化的输出接口时，但是需要输出接口时，它必须存在。
	 * @return ProductListener实例
	 */
	ProductListener getProductListener();

}