/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

/**
 * 提示输出接口 <br><br>
 * 
 * 用一个关键字编号，输出不同语言的字体文本。输出的内容显示在FRONT/WATCH站点的界面上。<br>
 * 
 * 这个接口被FRONT/WATCH站点实现，被FRONT、WATCH、SyntaxParser、END/PUT阶段分布任务组件使用。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/25/2013
 * @since laxcus 1.0
 */
public interface TipPrinter {
	
	/**
	 * 判断是控制台界面
	 * @return
	 */
	boolean isConsole();

	/**
	 * 判断是图形终端界面
	 * @return
	 */
	boolean isTerminal();

	/**
	 * 判断是驱动程序界面
	 * @return
	 */
	boolean isDriver();

	/**
	 * 判断是边缘界面
	 * @return
	 */
	boolean isEdge();

	/**
	 * 判断是WATCH节点
	 * @return 返回真或者假
	 */
	boolean isWatch();

	/**
	 * 根据提示编号，在窗口输出普通消息确认
	 * @param no 提示编号
	 * @return 接受返回真，否则假
	 */
	boolean confirm(int no);

	/**
	 * 根据提示编号，格式化普通消息并且输出窗口确认
	 * @param no 提示编号
	 * @param params 参与格式化的参数
	 * @return 接受返回真，否则假
	 */
	boolean confirm(int no, Object... params);

	/**
	 * 根据提示编号，输出普通消息
	 * @param no 提示编号
	 * @return 输出提示文本
	 */
	String message(int no);

	/**
	 * 根据提示编号，格式化普通消息并且输出
	 * @param no 提示编号
	 * @param params 参与格式化的参数
	 * @return 输出格式化提示文本
	 */
	String message(int no, Object... params);

	/**
	 * 根据提示编号，输出警告信息
	 * @param no 提示编号
	 * @return 输出提示文本
	 */
	String warning(int no);

	/**
	 * 根据提示编号，格式化警告信息并且输出
	 * @param no 提示编号
	 * @param params 参与格式化的参数
	 * @return 输出格式化提示文本
	 */
	String warning(int no, Object... params);

	/**
	 * 根据提示编号，输出错误消息
	 * @param no 提示编号
	 * @return 输出提示文本
	 */
	String fault(int no);

	/**
	 * 根据提示编号，格式错误消息并且输出
	 * @param no 提示编号
	 * @param params 参与格式化的参数
	 * @return 输出格式化提示文本
	 */
	String fault(int no, Object... params);
	
	/**
	 * 根据回显编号，输出错误消息
	 * @param no 回显编号
	 * @return 输出提示文本
	 */
	String echo(int no);

	/**
	 * 根据回显编号，格式错误消息并且输出
	 * @param no 回显编号
	 * @param params 参与格式化的参数
	 * @return 输出格式化提示文本
	 */
	String echo(int no, Object... params);
	
}
