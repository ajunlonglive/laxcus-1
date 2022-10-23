/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;

/**
 * 半截符编码/解码调用器。
 * 
 * @author scott.liang
 * @version 1.0 1/26/2017
 * @since laxcus 1.0
 */
public class MeetBuildHalfInvoker extends MeetInvoker {

	/**
	 * 构造半截符编码/解码调用器，指定命令
	 * @param cmd 半截符编码/解码命令
	 */
	public MeetBuildHalfInvoker(BuildHalf cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildHalf getCommand() {
		return (BuildHalf) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BuildHalf cmd = getCommand();
		try {
			todo(cmd);
		} catch (Throwable e) {
			super.fault(e);
		} 

		// 返回
		return useful();
	}

	/**
	 * 计算半截码值
	 * @param cmd
	 */
	private void todo(BuildHalf cmd) {
		String text = cmd.getText();
		// 忽略大小写，只在编码时有效
		if (cmd.isEncode()) {
			if (cmd.isIgnore()) {
				text = text.toLowerCase();
			}
			text = Halffer.encode(text);
		} else {
			text = Halffer.decode(text);
		}

		print(text);
	}

	/**
	 * 打印结果
	 * @param text
	 */
	private void print(String text) {
		createShowTitle(new String[] { "BUILD-HALF/TEXT" });
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}