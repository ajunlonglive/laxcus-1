/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.access.table.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 检测本地实体文件内容编码调用器 。<br><br>
 * 
 * 命令格式： CHECK ENTITY CHARSET 文件路径 <br><br>
 * 
 * 只在FRONT节点执行。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopCheckEntityCharsetInvoker extends DesktopInvoker {

	/**
	 * 字符集分析结果
	 *
	 * @author scott.liang
	 * @version 1.0 9/29/2019
	 * @since laxcus 1.0
	 */
	class CharsetResult {
		File file;

		CharsetChecker.Messy[] array;

		CharsetResult(File f, CharsetChecker.Messy[] c) {
			file = f;
			array = c;
		}
	}

	/**
	 * 构造检测本地实体文件内容编码调用器 ，指定命令
	 * @param cmd 检测命令 
	 */
	public DesktopCheckEntityCharsetInvoker(CheckEntityCharset cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckEntityCharset getCommand() {
		return (CheckEntityCharset) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckEntityCharset cmd = getCommand();
		
		ArrayList<CharsetResult> array = new ArrayList<CharsetResult>();
		for(File file : cmd.list()) {
			// 消息
			messageX(MessageTip.CHECK_X, file.toString());
			
			CharsetChecker checker = new CharsetChecker();
			// 估算字符集
			CharsetChecker.Messy[] all = checker.calculate(file);
			
			// 保存
			CharsetResult rs = new CharsetResult(file, all);
			array.add(rs);
		}
		
		if (array.size() == 1) {
			CharsetResult rs = array.get(0);
			printSingle(rs.file, rs.array);
		} else {
			printMulti(array);
		}

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 打印多行记录
	 * @param rs
	 */
	private void printMulti(List<CharsetResult> array) {
		printRuntime();

		// 生成表格标题, 磁盘文件 - 字符集/错误码统计
		createShowTitle(new String[] { "CHECK-ENTITY-CHARSET/MULTI/T1",
				"CHECK-ENTITY-CHARSET/MULTI/T2",  });

		// 排列显示
		for (int index = 0; index < array.size(); index++) {
			if (index > 0) {
				printGap(2);
			}
			CharsetResult rs = array.get(index);
			
			// 文件
			printItem("CHECK-ENTITY-CHARSET/MULTI/RESULT/FILE", rs.file);
			// 编码/错误码统计
			for (CharsetChecker.Messy e : rs.array) {
				String s = String.format("%s %d", e.getCharset(), e.getCount());
				printItem("CHECK-ENTITY-CHARSET/MULTI/RESULT/COUNT", s);
			}
		}

		// 输出全部记录
		flushTable();
	}

	/**
	 * 显示一行
	 * @param xmlTitlePath XML标题路径
	 * @param value 参数值
	 */
	private void printItem(String xmlTitlePath, String value) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlTitlePath);
		java.awt.Color color = findXMLForeground(xmlTitlePath, java.awt.Color.BLACK);
		item.add(new ShowStringCell(0, name, color));
		item.add(new ShowStringCell(1, value));
		addShowItem(item);
	}

	/**
	 * 显示一行
	 * @param xmlTitlePath
	 * @param value
	 */
	private void printItem(String xmlTitlePath, Object value) {
		printItem(xmlTitlePath, value.toString());
	}
	
	/**
	 * 打印结果
	 * @param file
	 * @param items
	 */
	private void printSingle(File file, CharsetChecker.Messy[] items) {
		printRuntime();

		// 生成表格标题
		createShowTitle(new String[] { "CHECK-ENTITY-CHARSET/SINGLE/CHARSET",
				"CHECK-ENTITY-CHARSET/SINGLE/ERRORS", "CHECK-ENTITY-CHARSET/SINGLE/FILE" });

		// 排列显示
		for (CharsetChecker.Messy e : items) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, e.getCharset()));
			item.add(new ShowIntegerCell(1, e.getCount()));
			item.add(new ShowStringCell(2, file.toString()));
			// 保存
			addShowItem(item);
		}

		// 输出全部记录
		flushTable();
	}

}